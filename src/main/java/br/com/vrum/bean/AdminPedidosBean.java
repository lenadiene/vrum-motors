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
import java.util.stream.Collectors;

@Named("adminPedidosBean")
@ViewScoped
public class AdminPedidosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Pedido> todosPedidos;
    private List<Pedido> pedidosFiltrados = new ArrayList<>();
    private List<Cliente> clientes;
    private List<Veiculo> veiculos;
    private List<Concessionaria> concessionarias;
    private List<Vendedor> vendedores;

    // Filtros
    private String buscaCliente;
    private Long filtroVendedorId;
    private Long filtroVeiculoId;
    private String filtroStatusStr;
    private Long filtroConcessionariaId;

    // Pedido em edição
    private Pedido pedidoSelecionado;
    private List<Anexo> anexosPedidoSelecionado = Collections.emptyList();
    private Part arquivoAnexo;

    // Campos de edição
    private Long editClienteId;
    private Long editVeiculoId;
    private Long editConcessionariaId;
    private Long editVendedorId;
    private String editStatusStr;
    private String editCorEscolhida;
    private String editFormaPagamento;
    private LocalDate editPrazoFabricacao;
    private LocalDate editPrazoEntrega;
    private LocalDate editDataRetirada;
    private String editObservacoes;

    private final PedidoService pedidoService = new PedidoService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final VeiculoService veiculoService = new VeiculoService();
    private final ConcessionariaService concService = new ConcessionariaService();

    @PostConstruct
    public void init() {
        clientes = usuarioService.listarPorPerfil(PerfilUsuario.CLIENTE)
                .stream().map(u -> (Cliente) u).collect(Collectors.toList());
        veiculos = veiculoService.listarTodos();
        concessionarias = concService.listarTodas();
        vendedores = usuarioService.listarPorPerfil(PerfilUsuario.VENDEDOR)
                .stream().map(u -> (Vendedor) u).collect(Collectors.toList());
        todosPedidos = pedidoService.listarTodos();
        aplicarFiltro();
    }

    public void aplicarFiltro() {
        StatusPedido statusFiltro = (filtroStatusStr != null && !filtroStatusStr.isEmpty())
                ? StatusPedido.valueOf(filtroStatusStr) : null;

        pedidosFiltrados = todosPedidos.stream()
                .filter(p -> {
                    if (buscaCliente != null && !buscaCliente.trim().isEmpty()) {
                        String lower = buscaCliente.trim().toLowerCase();
                        String nome = p.getCliente().getNome();
                        if (nome == null || !nome.toLowerCase().contains(lower)) return false;
                    }
                    if (filtroVendedorId != null) {
                        if (filtroVendedorId.equals(-1L)) {
                            if (p.getVendedor() != null) return false;
                        } else {
                            if (p.getVendedor() == null || !filtroVendedorId.equals(p.getVendedor().getId())) return false;
                        }
                    }
                    if (filtroVeiculoId != null && !filtroVeiculoId.equals(p.getVeiculo().getId())) return false;
                    if (statusFiltro != null && p.getStatus() != statusFiltro) return false;
                    if (filtroConcessionariaId != null && !filtroConcessionariaId.equals(p.getConcessionaria().getId())) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    public void limparFiltros() {
        buscaCliente = null;
        filtroVendedorId = null;
        filtroVeiculoId = null;
        filtroStatusStr = null;
        filtroConcessionariaId = null;
        pedidosFiltrados = new ArrayList<>(todosPedidos);
    }

    public void cancelarEdicao() {
        pedidoSelecionado = null;
        anexosPedidoSelecionado = Collections.emptyList();
    }

    public void selecionarPedido(Pedido p) {
        pedidoSelecionado = pedidoService.buscarPorId(p.getId());
        anexosPedidoSelecionado = pedidoService.listarAnexos(pedidoSelecionado);
        editClienteId = pedidoSelecionado.getCliente() != null ? pedidoSelecionado.getCliente().getId() : null;
        editVeiculoId = pedidoSelecionado.getVeiculo() != null ? pedidoSelecionado.getVeiculo().getId() : null;
        editConcessionariaId = pedidoSelecionado.getConcessionaria() != null ? pedidoSelecionado.getConcessionaria().getId() : null;
        editVendedorId = pedidoSelecionado.getVendedor() != null ? pedidoSelecionado.getVendedor().getId() : null;
        editStatusStr = pedidoSelecionado.getStatus() != null ? pedidoSelecionado.getStatus().name() : null;
        editCorEscolhida = pedidoSelecionado.getCorEscolhida();
        editFormaPagamento = pedidoSelecionado.getFormaPagamento();
        editPrazoFabricacao = pedidoSelecionado.getPrazoFabricacao();
        editPrazoEntrega = pedidoSelecionado.getPrazoEntrega();
        editDataRetirada = pedidoSelecionado.getDataRetirada();
        editObservacoes = pedidoSelecionado.getObservacoes();
        arquivoAnexo = null;
    }

    public void salvarPedido() {
        if (pedidoSelecionado == null) return;
        try {
            if (editClienteId != null) {
                Cliente c = clientes.stream().filter(x -> editClienteId.equals(x.getId())).findFirst().orElse(null);
                pedidoSelecionado.setCliente(c);
            }
            if (editVeiculoId != null) {
                Veiculo v = veiculos.stream().filter(x -> editVeiculoId.equals(x.getId())).findFirst().orElse(null);
                pedidoSelecionado.setVeiculo(v);
            }
            if (editConcessionariaId != null) {
                Concessionaria c = concessionarias.stream().filter(x -> editConcessionariaId.equals(x.getId())).findFirst().orElse(null);
                pedidoSelecionado.setConcessionaria(c);
            }
            if (editVendedorId == null) {
                pedidoSelecionado.setVendedor(null);
            } else {
                Vendedor v = vendedores.stream().filter(x -> editVendedorId.equals(x.getId())).findFirst().orElse(null);
                pedidoSelecionado.setVendedor(v);
            }
            if (editStatusStr != null && !editStatusStr.isEmpty()) {
                pedidoSelecionado.setStatus(StatusPedido.valueOf(editStatusStr));
            }
            pedidoSelecionado.setCorEscolhida(editCorEscolhida);
            pedidoSelecionado.setFormaPagamento(editFormaPagamento);
            pedidoSelecionado.setPrazoFabricacao(editPrazoFabricacao);
            pedidoSelecionado.setPrazoEntrega(editPrazoEntrega);
            pedidoSelecionado.setDataRetirada(editDataRetirada);
            pedidoSelecionado.setObservacoes(editObservacoes);

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

            todosPedidos = pedidoService.listarTodos();
            aplicarFiltro();
            pedidoSelecionado = pedidoService.buscarPorId(pedidoSelecionado.getId());
            anexosPedidoSelecionado = pedidoService.listarAnexos(pedidoSelecionado);
            arquivoAnexo = null;

        } catch (Exception e) {
            addErro("Erro ao salvar: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
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
                    ? anexo.getTipoArquivo() : "application/octet-stream";
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
                while ((read = in.read(buf)) != -1) out.write(buf, 0, read);
            }
            fc.responseComplete();
        } catch (Exception e) {
            addErro("Erro ao baixar: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    public StatusPedido[] getStatusPedidoValues() { return StatusPedido.values(); }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    // Getters & Setters
    public List<Pedido> getPedidosFiltrados() { return pedidosFiltrados; }
    public List<Cliente> getClientes() { return clientes; }
    public List<Veiculo> getVeiculos() { return veiculos; }
    public List<Concessionaria> getConcessionarias() { return concessionarias; }
    public List<Vendedor> getVendedores() { return vendedores; }
    public Pedido getPedidoSelecionado() { return pedidoSelecionado; }
    public List<Anexo> getAnexosPedidoSelecionado() { return anexosPedidoSelecionado; }
    public Part getArquivoAnexo() { return arquivoAnexo; }
    public void setArquivoAnexo(Part p) { this.arquivoAnexo = p; }
    public String getBuscaCliente() { return buscaCliente; }
    public void setBuscaCliente(String s) { this.buscaCliente = s; }
    public Long getFiltroVendedorId() { return filtroVendedorId; }
    public void setFiltroVendedorId(Long id) { this.filtroVendedorId = id; }
    public Long getFiltroVeiculoId() { return filtroVeiculoId; }
    public void setFiltroVeiculoId(Long id) { this.filtroVeiculoId = id; }
    public String getFiltroStatusStr() { return filtroStatusStr; }
    public void setFiltroStatusStr(String s) { this.filtroStatusStr = s; }
    public Long getFiltroConcessionariaId() { return filtroConcessionariaId; }
    public void setFiltroConcessionariaId(Long id) { this.filtroConcessionariaId = id; }
    public Long getEditClienteId() { return editClienteId; }
    public void setEditClienteId(Long id) { this.editClienteId = id; }
    public Long getEditVeiculoId() { return editVeiculoId; }
    public void setEditVeiculoId(Long id) { this.editVeiculoId = id; }
    public Long getEditConcessionariaId() { return editConcessionariaId; }
    public void setEditConcessionariaId(Long id) { this.editConcessionariaId = id; }
    public Long getEditVendedorId() { return editVendedorId; }
    public void setEditVendedorId(Long id) { this.editVendedorId = id; }
    public String getEditStatusStr() { return editStatusStr; }
    public void setEditStatusStr(String s) { this.editStatusStr = s; }
    public String getEditCorEscolhida() { return editCorEscolhida; }
    public void setEditCorEscolhida(String s) { this.editCorEscolhida = s; }
    public String getEditFormaPagamento() { return editFormaPagamento; }
    public void setEditFormaPagamento(String s) { this.editFormaPagamento = s; }
    public LocalDate getEditPrazoFabricacao() { return editPrazoFabricacao; }
    public void setEditPrazoFabricacao(LocalDate d) { this.editPrazoFabricacao = d; }
    public LocalDate getEditPrazoEntrega() { return editPrazoEntrega; }
    public void setEditPrazoEntrega(LocalDate d) { this.editPrazoEntrega = d; }
    public LocalDate getEditDataRetirada() { return editDataRetirada; }
    public void setEditDataRetirada(LocalDate d) { this.editDataRetirada = d; }
    public String getEditObservacoes() { return editObservacoes; }
    public void setEditObservacoes(String s) { this.editObservacoes = s; }
}
