package br.com.vrum.bean;

import br.com.vrum.model.Veiculo;
import br.com.vrum.service.VeiculoService;

import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;

@Named("veiculoDetalheBean")
@ViewScoped
public class VeiculoDetalheBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Veiculo veiculo;
    private final VeiculoService service = new VeiculoService();

    public void init() {
        if (veiculo != null) return; // já carregado

        Object idObj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("veiculoSelecionadoId");
        if (idObj instanceof Long) {
            veiculo = service.buscarPorId((Long) idObj);
        }
    }

    public String comprar() {
        if (veiculo != null) {
            FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().put("veiculoCompraId", veiculo.getId());
        }
        Object usuario = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (usuario == null) {
            return "/cadastro?faces-redirect=true";
        }
        return "/pages/cliente/novo-pedido?faces-redirect=true";
    }

    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo v) { this.veiculo = v; }
}
