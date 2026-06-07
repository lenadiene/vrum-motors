package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.ConcessionariaService;
import br.com.vrum.service.PedidoService;
import br.com.vrum.service.VeiculoService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("novoPedidoBean")
@ViewScoped
public class NovoPedidoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Cliente cliente;
    private List<Veiculo> veiculos;
    private List<Concessionaria> concessionarias;

    private Veiculo veiculoSelecionado;
    private Long concessionariaId;
    private Concessionaria concessionariaSelecionada;
    private String corEscolhida;

    private final PedidoService pedidoService = new PedidoService();
    private final VeiculoService veiculoService = new VeiculoService();
    private final ConcessionariaService concService = new ConcessionariaService();

    @PostConstruct
    public void init() {
        Object obj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (obj instanceof Cliente) {
            cliente = (Cliente) obj;
        }
        veiculos = veiculoService.listarDisponiveis();
        concessionarias = concService.listarAtivas();
        Long veiculoId = (Long) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("veiculoCompraId");
        if (veiculoId != null) {
            veiculoSelecionado = veiculoService.buscarPorId(veiculoId);
        }
    }

    public void selecionarVeiculo(Veiculo v) {
        this.veiculoSelecionado = v;
        this.concessionariaId = null;
        this.concessionariaSelecionada = null;
        this.corEscolhida = null;
    }

    public String confirmarPedido() {
        if (veiculoSelecionado == null) {
            addErro("Selecione um veículo.");
            return null;
        }
        if (concessionariaId == null) {
            addErro("Selecione a concessionária.");
            return null;
        }
        if (corEscolhida == null || corEscolhida.trim().isEmpty()) {
            addErro("Informe a cor desejada.");
            return null;
        }
        concessionariaSelecionada = concessionarias.stream()
                .filter(c -> c.getId().equals(concessionariaId))
                .findFirst().orElse(null);
        if (concessionariaSelecionada == null) {
            addErro("Concessionária inválida.");
            return null;
        }
        try {
            pedidoService.realizarPedido(cliente, veiculoSelecionado, concessionariaSelecionada, corEscolhida.trim());
            return "/pages/cliente/meus-pedidos?faces-redirect=true";
        } catch (Exception e) {
            addErro("Erro ao realizar pedido: " + e.getMessage());
            return null;
        }
    }

    public void cancelarSelecao() {
        veiculoSelecionado = null;
        concessionariaId = null;
        concessionariaSelecionada = null;
        corEscolhida = null;
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    public Cliente getCliente() { return cliente; }
    public List<Veiculo> getVeiculos() { return veiculos; }
    public List<Concessionaria> getConcessionarias() { return concessionarias; }
    public Veiculo getVeiculoSelecionado() { return veiculoSelecionado; }
    public Long getConcessionariaId() { return concessionariaId; }
    public void setConcessionariaId(Long id) { this.concessionariaId = id; }
    public String getCorEscolhida() { return corEscolhida; }
    public void setCorEscolhida(String c) { this.corEscolhida = c; }
}
