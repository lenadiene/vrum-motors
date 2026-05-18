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
        StatusPedido statusFiltro = (filtroStatusStr != null && !filtroStatusStr.isEmpty())
                ? StatusPedido.valueOf(filtroStatusStr) : null;

        pedidosFiltrados = todosPedidos.stream()
                .filter(p -> {
                    if (filtroVendedorId != null) {
                        if (filtroVendedorId.equals(-1L)) {
                            if (p.getVendedor() != null) return false;
                        } else {
                            if (p.getVendedor() == null || !filtroVendedorId.equals(p.getVendedor().getId())) return false;
                        }
                    }
                    if (filtroVeiculoId != null && !filtroVeiculoId.equals(p.getVeiculo().getId())) return false;
                    if (statusFiltro != null && p.getStatus() != statusFiltro) return false;
                    if (buscaCliente != null && !buscaCliente.trim().isEmpty()) {
                        String lower = buscaCliente.trim().toLowerCase();
                        String nome = p.getCliente().getNome();
                        if (nome == null || !nome.toLowerCase().contains(lower)) return false;
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
        if (pedidoSelecionado == null) return;
        try {
            if (editVendedorId == null) {
                pedidoSelecionado.setVendedor(null);
            } else {
                Vendedor v = vendedores.stream()
                        .filter(vnd -> editVendedorId.equals(vnd.getId()))
                        .findFirst().orElse(null);
                pedidoSelecionado.setVendedor(v);
            }

            if (editStatusStr != null && !editStatusStr.isEmpty()) {
                pedidoSelecionado.setStatus(StatusPedido.valueOf(editStatusStr));
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
    public Gerente getGerente() { return gerente; }
    public Concessionaria getConcessionaria() { return concessionaria; }
    public List<Vendedor> getVendedores() { return vendedores; }
    public List<Veiculo> getVeiculos() { return veiculos; }
    public List<Pedido> getPedidosFiltrados() { return pedidosFiltrados; }
    public Long getFiltroVendedorId() { return filtroVendedorId; }
    public void setFiltroVendedorId(Long id) { this.filtroVendedorId = id; }
    public Long getFiltroVeiculoId() { return filtroVeiculoId; }
    public void setFiltroVeiculoId(Long id) { this.filtroVeiculoId = id; }
    public String getFiltroStatusStr() { return filtroStatusStr; }
    public void setFiltroStatusStr(String s) { this.filtroStatusStr = s; }
    public String getBuscaCliente() { return buscaCliente; }
    public void setBuscaCliente(String s) { this.buscaCliente = s; }
    public Pedido getPedidoSelecionado() { return pedidoSelecionado; }
    public List<Anexo> getAnexosPedidoSelecionado() { return anexosPedidoSelecionado; }
    public Long getEditVendedorId() { return editVendedorId; }
    public void setEditVendedorId(Long id) { this.editVendedorId = id; }
    public String getEditStatusStr() { return editStatusStr; }
    public void setEditStatusStr(String s) { this.editStatusStr = s; }
    public LocalDate getEditPrazoFabricacao() { return editPrazoFabricacao; }
    public void setEditPrazoFabricacao(LocalDate d) { this.editPrazoFabricacao = d; }
    public String getEditFormaPagamento() { return editFormaPagamento; }
    public void setEditFormaPagamento(String s) { this.editFormaPagamento = s; }
    public Part getArquivoAnexo() { return arquivoAnexo; }
    public void setArquivoAnexo(Part p) { this.arquivoAnexo = p; }
}
