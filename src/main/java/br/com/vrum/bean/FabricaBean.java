package br.com.vrum.bean;

import br.com.vrum.model.Pedido;
import br.com.vrum.model.StatusPedido;
import br.com.vrum.service.PedidoService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Named("fabricaBean")
@ViewScoped
public class FabricaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<Pedido> todos;

    // Estado de seleção em lote (IDs separados por vírgula, preenchidos via JS)
    private String idsSelecionadosHidden = "";

    // Estado do painel de prazo unitário
    private Long pedidoParaAvancarId;
    private String prazoEntregaUnitarioTexto;

    // Estado do painel de prazo em lote
    private boolean mostrarConfirmacaoLote;
    private String prazoEntregaLoteTexto;

    private final PedidoService service = new PedidoService();

    @PostConstruct
    public void init() {
        todos = service.listarParaFabricacao();
    }

    // ── Listas por coluna ────────────────────────────────────────────────────

    public List<Pedido> getPedidosAguardando() {
        return filtrar(StatusPedido.AGUARDANDO_FABRICACAO);
    }

    public List<Pedido> getPedidosEmFabricacao() {
        return filtrar(StatusPedido.EM_FABRICACAO);
    }

    public List<Pedido> getPedidosFabricados() {
        return filtrar(StatusPedido.FABRICADO);
    }

    public List<Pedido> getPedidosEnviados() {
        return filtrar(StatusPedido.ENVIADO_CIDADE);
    }

    private List<Pedido> filtrar(StatusPedido s) {
        return todos.stream().filter(p -> p.getStatus() == s).collect(Collectors.toList());
    }

    // ── Ações unitárias ──────────────────────────────────────────────────────

    /** AGUARDANDO_FABRICACAO → EM_FABRICACAO  e  EM_FABRICACAO → FABRICADO */
    public void avancarUnitario(Pedido p) {
        try {
            StatusPedido proximo = proximoStatus(p.getStatus());
            service.atualizarStatusFabricacao(p, proximo, null, null);
            recarregar();
            addSucesso("Status atualizado para: " + proximo.getDescricao());
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    /** Abre painel de prazo para envio unitário FABRICADO → ENVIADO_CIDADE */
    public void prepararEnvioUnitario(Pedido p) {
        this.pedidoParaAvancarId = p.getId();
        this.prazoEntregaUnitarioTexto = "";
    }

    public void confirmarEnvioUnitario() {
        LocalDate prazo = parsePrazo(prazoEntregaUnitarioTexto);
        if (prazo == null) {
            addErro("Informe o prazo de entrega no formato dd/MM/yyyy.");
            return;
        }
        try {
            Pedido p = service.buscarPorId(pedidoParaAvancarId);
            service.atualizarStatusFabricacao(p, StatusPedido.ENVIADO_CIDADE, prazo, null);
            pedidoParaAvancarId = null;
            prazoEntregaUnitarioTexto = "";
            recarregar();
            addSucesso("Veículo enviado para a cidade com sucesso!");
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    // ── Ações em lote ────────────────────────────────────────────────────────

    /** Batch: AGUARDANDO_FABRICACAO → EM_FABRICACAO */
    public void avancarLoteAguardando() {
        avancarLote(StatusPedido.AGUARDANDO_FABRICACAO, StatusPedido.EM_FABRICACAO);
    }

    /** Batch: EM_FABRICACAO → FABRICADO */
    public void avancarLoteEmFabricacao() {
        avancarLote(StatusPedido.EM_FABRICACAO, StatusPedido.FABRICADO);
    }

    private void avancarLote(StatusPedido de, StatusPedido para) {
        List<Long> ids = parseIds();
        if (ids.isEmpty()) {
            addErro("Selecione ao menos um pedido.");
            return;
        }
        try {
            int cont = 0;
            for (Long id : ids) {
                Pedido p = service.buscarPorId(id);
                if (p != null && p.getStatus() == de) {
                    service.atualizarStatusFabricacao(p, para, null, null);
                    cont++;
                }
            }
            recarregar();
            addSucesso(cont + " pedido(s) avançados para: " + para.getDescricao());
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    /** Abre painel de prazo para envio em lote FABRICADO → ENVIADO_CIDADE */
    public void prepararEnvioLote() {
        if (parseIds().isEmpty()) {
            addErro("Selecione ao menos um pedido para enviar.");
            return;
        }
        mostrarConfirmacaoLote = true;
        prazoEntregaLoteTexto = "";
    }

    public void confirmarEnvioLote() {
        LocalDate prazo = parsePrazo(prazoEntregaLoteTexto);
        if (prazo == null) {
            addErro("Informe o prazo de entrega no formato dd/MM/yyyy.");
            return;
        }
        List<Long> ids = parseIds();
        try {
            int cont = 0;
            for (Long id : ids) {
                Pedido p = service.buscarPorId(id);
                if (p != null && p.getStatus() == StatusPedido.FABRICADO) {
                    service.atualizarStatusFabricacao(p, StatusPedido.ENVIADO_CIDADE, prazo, null);
                    cont++;
                }
            }
            mostrarConfirmacaoLote = false;
            idsSelecionadosHidden = "";
            prazoEntregaLoteTexto = "";
            recarregar();
            addSucesso(cont + " pedido(s) enviados para a cidade!");
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void cancelarAcao() {
        pedidoParaAvancarId = null;
        mostrarConfirmacaoLote = false;
        prazoEntregaUnitarioTexto = "";
        prazoEntregaLoteTexto = "";
        idsSelecionadosHidden = "";
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void recarregar() {
        todos = service.listarParaFabricacao();
        idsSelecionadosHidden = "";
    }

    private List<Long> parseIds() {
        if (idsSelecionadosHidden == null || idsSelecionadosHidden.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(idsSelecionadosHidden.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private LocalDate parsePrazo(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(texto.trim(), DATA_BR);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private StatusPedido proximoStatus(StatusPedido atual) {
        switch (atual) {
            case AGUARDANDO_FABRICACAO: return StatusPedido.EM_FABRICACAO;
            case EM_FABRICACAO:         return StatusPedido.FABRICADO;
            default: throw new IllegalStateException("Status sem transição direta: " + atual);
        }
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public boolean isMostrarPrazoUnitario()    { return pedidoParaAvancarId != null; }
    public boolean isMostrarConfirmacaoLote()  { return mostrarConfirmacaoLote; }

    public String getIdsSelecionadosHidden()               { return idsSelecionadosHidden; }
    public void   setIdsSelecionadosHidden(String v)       { this.idsSelecionadosHidden = v; }
    public String getPrazoEntregaUnitarioTexto()           { return prazoEntregaUnitarioTexto; }
    public void   setPrazoEntregaUnitarioTexto(String v)   { this.prazoEntregaUnitarioTexto = v; }
    public String getPrazoEntregaLoteTexto()               { return prazoEntregaLoteTexto; }
    public void   setPrazoEntregaLoteTexto(String v)       { this.prazoEntregaLoteTexto = v; }
}
