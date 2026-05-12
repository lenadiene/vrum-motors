package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.PedidoService;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Named("clientePedidoBean")
@ViewScoped
public class ClientePedidoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Cliente cliente;
    private List<Pedido> pedidos;
    private Pedido pedidoDetalhes;

    private final PedidoService service = new PedidoService();

    @PostConstruct
    public void init() {
        Object obj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (obj instanceof Cliente) {
            cliente = (Cliente) obj;
            pedidos = service.listarPorCliente(cliente);
        }
    }

    public void verDetalhes(Pedido p) {
        this.pedidoDetalhes = service.buscarPorId(p.getId());
    }

    public String getCorBadgeStatus(StatusPedido status) {
        switch (status) {
            case AGUARDANDO_ATENDIMENTO: return "badge-secondary";
            case EM_NEGOCIACAO:          return "badge-warning";
            case EM_FABRICACAO:          return "badge-info";
            case FABRICADO:              return "badge-primary";
            case ENVIADO_CIDADE:         return "badge-info";
            case PRONTO_ENTREGA:         return "badge-success";
            case FINALIZADO:             return "badge-dark";
            case CANCELADO:              return "badge-danger";
            default:                     return "badge-secondary";
        }
    }

    public int getProgressoPedido(StatusPedido status) {
        switch (status) {
            case AGUARDANDO_ATENDIMENTO: return 10;
            case EM_NEGOCIACAO:          return 25;
            case EM_FABRICACAO:          return 45;
            case FABRICADO:              return 65;
            case ENVIADO_CIDADE:         return 80;
            case PRONTO_ENTREGA:         return 90;
            case FINALIZADO:             return 100;
            default:                     return 0;
        }
    }

    public List<Pedido> getPedidos() { return pedidos; }
    public Pedido getPedidoDetalhes() { return pedidoDetalhes; }
    public Cliente getCliente() { return cliente; }
}
