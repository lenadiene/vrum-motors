package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.PedidoService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Named("fabricaBean")
@ViewScoped
public class FabricaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Pedido> pedidos;
    private Pedido pedidoSelecionado;
    private StatusPedido novoStatus;
    private LocalDate prazoEntrega;
    private String observacoes;

    private final PedidoService service = new PedidoService();
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PostConstruct
    public void init() {
        pedidos = service.listarParaFabricacao();
    }

    public void selecionarPedido(Pedido p) {
        this.pedidoSelecionado = service.buscarPorId(p.getId());

        this.novoStatus = null;
        this.prazoEntrega = pedidoSelecionado.getPrazoEntrega();
        this.observacoes = pedidoSelecionado.getObservacoesFabrica();
    }

    public void atualizarStatus() {
        try {
            sincronizarPrazoEntregaSubmetido();
            service.atualizarStatusFabricacao(pedidoSelecionado, novoStatus, prazoEntrega, observacoes);
            addSucesso("Status atualizado para: " + novoStatus.getDescricao());
            pedidos = service.listarParaFabricacao();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public List<StatusPedido> getStatusPermitidos() {
        if (pedidoSelecionado == null) {
            return List.of();
        }

        return service.obterStatusPermitidosFabricacao(
                pedidoSelecionado.getStatus());
    }


    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void sincronizarPrazoEntregaSubmetido() {
        if (prazoEntrega != null) {
            return;
        }

        Map<String, String> parametros = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();
        String prazoInformado = parametros.get("fabricaForm:prazoEntrega");
        if (prazoInformado == null) {
            prazoInformado = parametros.entrySet().stream()
                    .filter(e -> e.getKey().endsWith(":prazoEntrega"))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (prazoInformado != null && !prazoInformado.trim().isEmpty()) {
            prazoEntrega = LocalDate.parse(prazoInformado.trim(), DATA_BR);
        }
    }

    public List<Pedido> getPedidos() { return pedidos; }
    public Pedido getPedidoSelecionado() { return pedidoSelecionado; }
    public void setPedidoSelecionado(Pedido p) { this.pedidoSelecionado = p; }
    public StatusPedido getNovoStatus() { return novoStatus; }
    public void setNovoStatus(StatusPedido s) { this.novoStatus = s; }
    public LocalDate getPrazoEntrega() { return prazoEntrega; }
    public void setPrazoEntrega(LocalDate d) { this.prazoEntrega = d; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String o) { this.observacoes = o; }
}
