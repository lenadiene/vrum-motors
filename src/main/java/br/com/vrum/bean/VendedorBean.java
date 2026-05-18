package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.PedidoService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Named("vendedorBean")
@ViewScoped
public class VendedorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Vendedor vendedor;
    private List<Pedido> pedidosDisponiveis;
    private List<Pedido> meusPedidos;
    private Pedido pedidoSelecionado;
    private LocalDate prazoFabricacao;
    private LocalDate prazoEntrega;
    private LocalDate dataRetirada;
    private String formaPagamento;
    private String observacao;
    private Part arquivoAnexo;

    private final PedidoService service = new PedidoService();

    @PostConstruct
    public void init() {
        Object obj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (obj instanceof Vendedor) {
            vendedor = (Vendedor) obj;
            carregarPedidos();
        }
    }

    public void carregarPedidos() {
        pedidosDisponiveis = service.listarDisponiveisPorConcessionaria(vendedor.getConcessionaria());
        meusPedidos = service.listarPorVendedor(vendedor);
    }

    public void assumirPedido(Pedido pedido) {
        try {
            service.assumirPedido(pedido, vendedor);
            addSucesso("Pedido assumido com sucesso! Entre em contato com o cliente.");
            carregarPedidos();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void selecionarPedido(Pedido pedido) {
        this.pedidoSelecionado = service.buscarPorId(pedido.getId());
    }

    public void enviarParaFabricacao() {
        try {
            service.enviarParaFabricacao(pedidoSelecionado, prazoFabricacao, formaPagamento);
            addSucesso("Pedido enviado para fabricação!");
            carregarPedidos();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void marcarProntoEntrega() {
        try {
            service.marcarProntoEntrega(pedidoSelecionado);
            addSucesso("Veículo marcado como pronto para entrega!");
            carregarPedidos();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void finalizarPedido() {
        try {
            service.finalizarPedido(pedidoSelecionado, dataRetirada);
            addSucesso("Pedido finalizado com sucesso!");
            carregarPedidos();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void uploadAnexo() {
        if (pedidoSelecionado == null) {
            addErro("Selecione um pedido antes de enviar o arquivo.");
            return;
        }
        if (arquivoAnexo == null || arquivoAnexo.getSize() == 0) {
            addErro("Selecione um arquivo para enviar.");
            return;
        }
        try {
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
            service.adicionarAnexo(pedidoSelecionado.getId(), nomeArq, caminho,
                    arquivoAnexo.getContentType(), arquivoAnexo.getSize());
            addSucesso("Contrato enviado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            addErro("Erro ao enviar arquivo: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    public String gerarLinkWhatsapp(Pedido pedido) {
        String tel = pedido.getCliente().getTelefone();
        if (tel == null) return "#";
        tel = tel.replaceAll("[^0-9]", "");
        String msg = "Olá " + pedido.getCliente().getNome() + "! Sou " + vendedor.getNome() +
                " da Vrum Motors. Estou em contato sobre seu pedido do " +
                pedido.getVeiculo().getNome() + " (Pedido: " + pedido.getNumeroPedido() + ").";
        try {
            return "https://wa.me/55" + tel + "?text=" +
                    java.net.URLEncoder.encode(msg, "UTF-8");
        } catch (Exception e) {
            return "https://wa.me/55" + tel;
        }
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    // Getters e Setters
    public List<Pedido> getPedidosDisponiveis() { return pedidosDisponiveis; }
    public List<Pedido> getMeusPedidos() { return meusPedidos; }
    public Pedido getPedidoSelecionado() { return pedidoSelecionado; }
    public void setPedidoSelecionado(Pedido p) { this.pedidoSelecionado = p; }
    public LocalDate getPrazoFabricacao() { return prazoFabricacao; }
    public void setPrazoFabricacao(LocalDate d) { this.prazoFabricacao = d; }
    public LocalDate getPrazoEntrega() { return prazoEntrega; }
    public void setPrazoEntrega(LocalDate d) { this.prazoEntrega = d; }
    public LocalDate getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDate d) { this.dataRetirada = d; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String f) { this.formaPagamento = f; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String o) { this.observacao = o; }
    public Part getArquivoAnexo() { return arquivoAnexo; }
    public void setArquivoAnexo(Part p) { this.arquivoAnexo = p; }
    public Vendedor getVendedor() { return vendedor; }
}
