package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.*;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Named("gerPedidosBean")
@ViewScoped
public class GerPedidosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Gerente gerente;
    private Concessionaria concessionaria;
    private List<Vendedor> vendedores;
    private List<Veiculo> veiculos;
    private List<Pedido> todosPedidos;
    private List<Pedido> pedidosFiltrados = new ArrayList<>();

    // Filtros
    private Long filtroVendedorId;
    private Long filtroVeiculoId;
    private String filtroStatusStr;
    private String buscaCliente;

    // Pedido em edição
    private Pedido pedidoSelecionado;
    private List<Anexo> anexosPedidoSelecionado = Collections.emptyList();

    // Campos de edição
    private Long editVendedorId;
    private String editStatusStr;
    private LocalDate editPrazoFabricacao;
    private String editFormaPagamento;
    private Part arquivoAnexo;

    private final PedidoService pedidoService = new PedidoService();
    private final ConcessionariaService concService = new ConcessionariaService();
    private final VeiculoService veiculoService = new VeiculoService();

    private static final int MAX_BUSCA_CLIENTE = 100;
    private static final int MAX_FORMA_PAGAMENTO = 100;
    private static final int MAX_NOME_ANEXO = 200;
    private static final long MAX_TAMANHO_ANEXO = 5 * 1024 * 1024;

    @PostConstruct
    public void init() {
        Object obj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (obj instanceof Gerente) {
            gerente = (Gerente) obj;
            concessionaria = gerente.getConcessionaria();
            carregarDados();
        }
    }

    private void carregarDados() {
        vendedores = concService.listarVendedores(concessionaria);
        veiculos = veiculoService.listarTodos();
        todosPedidos = pedidoService.listarPorConcessionaria(concessionaria);
        aplicarFiltro();
    }

    public void aplicarFiltro() {
        if (buscaCliente != null) {
            buscaCliente = buscaCliente.trim();
            if (buscaCliente.length() > MAX_BUSCA_CLIENTE) {
                addErro("A busca por cliente deve ter no máximo 100 caracteres.");
                pedidosFiltrados = new ArrayList<>(todosPedidos);
                return;
            }
        }

        StatusPedido statusFiltro = null;
        if (filtroStatusStr != null && !filtroStatusStr.isEmpty()) {
            try {
                statusFiltro = StatusPedido.valueOf(filtroStatusStr);
            } catch (IllegalArgumentException e) {
                addErro("Filtro de status inválido.");
                pedidosFiltrados = new ArrayList<>(todosPedidos);
                return;
            }
        }

        final StatusPedido statusFiltroFinal = statusFiltro;
        final String buscaClienteFiltro = buscaCliente;

        pedidosFiltrados = todosPedidos.stream()
                .filter(p -> {
                    if (filtroVendedorId != null) {
                        if (filtroVendedorId.equals(-1L)) {
                            if (p.getVendedor() != null)
                                return false;
                        } else {
                            if (p.getVendedor() == null || !filtroVendedorId.equals(p.getVendedor().getId()))
                                return false;
                        }
                    }
                    if (filtroVeiculoId != null && !filtroVeiculoId.equals(p.getVeiculo().getId()))
                        return false;
                    if (statusFiltroFinal != null && p.getStatus() != statusFiltroFinal)
                        return false;
                    if (buscaClienteFiltro != null && !buscaClienteFiltro.isEmpty()) {
                        String lower = buscaClienteFiltro.toLowerCase();
                        String nome = p.getCliente().getNome();
                        if (nome == null || !nome.toLowerCase().contains(lower))
                            return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public void limparFiltros() {
        filtroVendedorId = null;
        filtroVeiculoId = null;
        filtroStatusStr = null;
        buscaCliente = null;
        pedidosFiltrados = new ArrayList<>(todosPedidos);
    }

    public void cancelarEdicao() {
        pedidoSelecionado = null;
        anexosPedidoSelecionado = Collections.emptyList();
    }

    public void selecionarPedido(Pedido p) {
        pedidoSelecionado = pedidoService.buscarPorId(p.getId());
        anexosPedidoSelecionado = pedidoService.listarAnexos(pedidoSelecionado);
        editVendedorId = pedidoSelecionado.getVendedor() != null ? pedidoSelecionado.getVendedor().getId() : null;
        editStatusStr = pedidoSelecionado.getStatus() != null ? pedidoSelecionado.getStatus().name() : null;
        editPrazoFabricacao = pedidoSelecionado.getPrazoFabricacao();
        editFormaPagamento = pedidoSelecionado.getFormaPagamento();
        arquivoAnexo = null;
    }

    public void salvarPedido() {
        if (pedidoSelecionado == null)
            return;

        // 1. Segurança Server-Side: Verificar integridade da sessão do Gerente
        if (gerente == null || gerente.getPerfil() != PerfilUsuario.GERENTE) {
            addErro("Ação não permitida para o seu perfil de usuário.");
            return;
        }

        if (!validarCamposPedido()) {
            return;
        }

        try {
            Vendedor vendedorAlvo = null;
            if (editVendedorId != null) {
                vendedorAlvo = vendedores.stream()
                        .filter(vnd -> editVendedorId.equals(vnd.getId()))
                        .findFirst().orElse(null);

                // Critério 1: Validar se o vendedor pertence à concessionária do Gerente
                if (vendedorAlvo == null || !vendedorAlvo.getConcessionaria().getId().equals(concessionaria.getId())) {
                    addErro("Vendedor inválido ou não pertence a esta unidade.");
                    return;
                }
            }

            StatusPedido novoStatus = null;
            if (editStatusStr != null && !editStatusStr.isEmpty()) {
                novoStatus = StatusPedido.valueOf(editStatusStr);
            }

            // Critério 2: Validar regra de status vs vendedor associado
            if (vendedorAlvo != null && novoStatus == StatusPedido.AGUARDANDO_ATENDIMENTO) {
                addErro("Um pedido com vendedor associado não pode estar 'Aguardando Atendimento'. Altere o status.");
                return;
            }

            if (vendedorAlvo == null && novoStatus != StatusPedido.AGUARDANDO_ATENDIMENTO
                    && novoStatus != StatusPedido.CANCELADO) {
                addErro("Pedidos em andamento precisam de um Vendedor Responsável.");
                return;
            }

            // Se passar nas regras de negócio, atribui os campos e salva
            pedidoSelecionado.setVendedor(vendedorAlvo);
            if (novoStatus != null) {
                pedidoSelecionado.setStatus(novoStatus);
            }
            pedidoSelecionado.setPrazoFabricacao(editPrazoFabricacao);
            pedidoSelecionado.setFormaPagamento(editFormaPagamento);

            pedidoService.atualizarPedido(pedidoSelecionado);

            if (arquivoAnexo != null && arquivoAnexo.getSize() > 0) {
                try {
                    processarAnexo();
                    addSucesso("Pedido atualizado com novo anexo!");
                } catch (Exception e) {
                    addSucesso("Pedido atualizado.");
                    addErro("Não foi possível salvar o anexo: " +
                            (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                }
            } else {
                addSucesso("Pedido atualizado com sucesso!");
            }

            todosPedidos = pedidoService.listarPorConcessionaria(concessionaria);
            aplicarFiltro();
            pedidoSelecionado = pedidoService.buscarPorId(pedidoSelecionado.getId());
            anexosPedidoSelecionado = pedidoService.listarAnexos(pedidoSelecionado);
            arquivoAnexo = null;

        } catch (Exception e) {
            addErro("Erro ao salvar: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private boolean validarCamposPedido() {
        StatusPedido novoStatus = null;
        if (editStatusStr == null || editStatusStr.trim().isEmpty()) {
            addErro("Selecione o status do pedido.");
            return false;
        }

        try {
            novoStatus = StatusPedido.valueOf(editStatusStr);
        } catch (IllegalArgumentException e) {
            addErro("Status de pedido inválido.");
            return false;
        }

        if (editPrazoFabricacao != null
                && pedidoSelecionado.getDataPedido() != null
                && editPrazoFabricacao.isBefore(pedidoSelecionado.getDataPedido().toLocalDate())) {
            addErro("O prazo de fabricação não pode ser anterior à data do pedido.");
            return false;
        }

        if (editFormaPagamento != null) {
            editFormaPagamento = editFormaPagamento.trim();
            if (editFormaPagamento.length() > MAX_FORMA_PAGAMENTO) {
                addErro("A forma de pagamento deve ter no máximo 100 caracteres.");
                return false;
            }
            if (editFormaPagamento.matches(".*[\\p{Cntrl}&&[^\r\n\t]].*")) {
                addErro("A forma de pagamento contém caracteres inválidos.");
                return false;
            }
            if (editFormaPagamento.isEmpty()) {
                editFormaPagamento = null;
            }
        }

        if (arquivoAnexo != null && arquivoAnexo.getSize() > 0 && !validarAnexo()) {
            return false;
        }

        if (novoStatus == StatusPedido.AGUARDANDO_ATENDIMENTO && editVendedorId != null) {
            addErro("Um pedido com vendedor associado não pode estar 'Aguardando Atendimento'. Altere o status.");
            return false;
        }

        if (editVendedorId == null && novoStatus != StatusPedido.AGUARDANDO_ATENDIMENTO
                && novoStatus != StatusPedido.CANCELADO) {
            addErro("Pedidos em andamento precisam de um Vendedor Responsável.");
            return false;
        }

        return true;
    }

    private boolean validarAnexo() {
        if (arquivoAnexo.getSize() > MAX_TAMANHO_ANEXO) {
            addErro("O anexo deve ter no máximo 5 MB.");
            return false;
        }

        String nomeArquivo = Paths.get(arquivoAnexo.getSubmittedFileName()).getFileName().toString();
        if (nomeArquivo.isBlank()) {
            addErro("Selecione um arquivo válido para anexar.");
            return false;
        }
        if (nomeArquivo.length() > MAX_NOME_ANEXO) {
            addErro("O nome do anexo deve ter no máximo 200 caracteres.");
            return false;
        }

        String lower = nomeArquivo.toLowerCase(Locale.ROOT);
        boolean extensaoPermitida = lower.endsWith(".pdf")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png");
        if (!extensaoPermitida) {
            addErro("Anexe apenas arquivos PDF, JPG ou PNG.");
            return false;
        }

        String contentType = arquivoAnexo.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            boolean tipoPermitido = contentType.equals("application/pdf")
                    || contentType.equals("image/jpeg")
                    || contentType.equals("image/png");
            if (!tipoPermitido) {
                addErro("Tipo de anexo inválido. Use PDF, JPG ou PNG.");
                return false;
            }
        }

        return true;
    }

    private void processarAnexo() throws Exception {
        String nomeArq = Paths.get(arquivoAnexo.getSubmittedFileName()).getFileName().toString();
        String uploadDir = FacesContext.getCurrentInstance()
                .getExternalContext().getRealPath("/uploads/contratos/");
        if (uploadDir == null) {
            uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "vrum-contratos";
        }
        new File(uploadDir).mkdirs();
        String caminho = uploadDir + File.separator + System.currentTimeMillis() + "_" + nomeArq;
        try (InputStream is = arquivoAnexo.getInputStream()) {
            Files.copy(is, Paths.get(caminho));
        }
        pedidoService.adicionarAnexo(pedidoSelecionado.getId(), nomeArq, caminho,
                arquivoAnexo.getContentType(), arquivoAnexo.getSize());
    }

    public void downloadAnexo(Anexo anexo) {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            File file = new File(anexo.getCaminhoArquivo());
            if (!file.exists()) {
                addErro("Arquivo não encontrado: " + anexo.getNomeArquivo());
                return;
            }
            String contentType = (anexo.getTipoArquivo() != null && !anexo.getTipoArquivo().isBlank())
                    ? anexo.getTipoArquivo()
                    : "application/octet-stream";
            jakarta.faces.context.ExternalContext ec = fc.getExternalContext();
            ec.responseReset();
            ec.setResponseContentType(contentType);
            ec.setResponseContentLength((int) file.length());
            ec.setResponseHeader("Content-Disposition",
                    "attachment; filename=\"" + anexo.getNomeArquivo() + "\"");
            try (FileInputStream in = new FileInputStream(file);
                    OutputStream out = ec.getResponseOutputStream()) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = in.read(buf)) != -1)
                    out.write(buf, 0, read);
            }
            fc.responseComplete();
        } catch (Exception e) {
            addErro("Erro ao baixar: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    public StatusPedido[] getStatusPedidoValues() {
        return StatusPedido.values();
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    // Getters & Setters
    public Gerente getGerente() {
        return gerente;
    }

    public Concessionaria getConcessionaria() {
        return concessionaria;
    }

    public List<Vendedor> getVendedores() {
        return vendedores;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public List<Pedido> getPedidosFiltrados() {
        return pedidosFiltrados;
    }

    public Long getFiltroVendedorId() {
        return filtroVendedorId;
    }

    public void setFiltroVendedorId(Long id) {
        this.filtroVendedorId = id;
    }

    public Long getFiltroVeiculoId() {
        return filtroVeiculoId;
    }

    public void setFiltroVeiculoId(Long id) {
        this.filtroVeiculoId = id;
    }

    public String getFiltroStatusStr() {
        return filtroStatusStr;
    }

    public void setFiltroStatusStr(String s) {
        this.filtroStatusStr = s;
    }

    public String getBuscaCliente() {
        return buscaCliente;
    }

    public void setBuscaCliente(String s) {
        this.buscaCliente = s;
    }

    public Pedido getPedidoSelecionado() {
        return pedidoSelecionado;
    }

    public List<Anexo> getAnexosPedidoSelecionado() {
        return anexosPedidoSelecionado;
    }

    public Long getEditVendedorId() {
        return editVendedorId;
    }

    public void setEditVendedorId(Long id) {
        this.editVendedorId = id;
    }

    public String getEditStatusStr() {
        return editStatusStr;
    }

    public void setEditStatusStr(String s) {
        this.editStatusStr = s;
    }

    public LocalDate getEditPrazoFabricacao() {
        return editPrazoFabricacao;
    }

    public void setEditPrazoFabricacao(LocalDate d) {
        this.editPrazoFabricacao = d;
    }

    public String getEditFormaPagamento() {
        return editFormaPagamento;
    }

    public void setEditFormaPagamento(String s) {
        this.editFormaPagamento = s;
    }

    public Part getArquivoAnexo() {
        return arquivoAnexo;
    }

    public void setArquivoAnexo(Part p) {
        this.arquivoAnexo = p;
    }
}
