package br.com.vrum.bean;

import br.com.vrum.model.Veiculo;
import br.com.vrum.service.VeiculoService;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Named("homeBean")
@ViewScoped
public class HomeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Veiculo> destaques;
    private List<Veiculo> lancamentos;
    private List<Veiculo> todosVeiculos;
    private List<Veiculo> veiculosFiltrados;
    private String termoBusca;
    private Veiculo veiculoSelecionado;

    private final VeiculoService service = new VeiculoService();

    @PostConstruct
    public void init() {
        destaques = service.listarDestaques();
        lancamentos = service.listarLancamentos();
        todosVeiculos = service.listarDisponiveis();
        veiculosFiltrados = todosVeiculos;
    }

    public void buscar() {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            veiculosFiltrados = todosVeiculos;
        } else {
            veiculosFiltrados = service.buscarPorNome(termoBusca.trim());
        }
    }

    public String selecionarVeiculo(Long id) {
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("veiculoSelecionadoId", id);
        return "/pages/veiculo?faces-redirect=true";
    }

    public String comprar(Long veiculoId) {
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("veiculoCompraId", veiculoId);

        // Verifica se está logado como cliente
        Object usuario = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuarioLogado");
        if (usuario == null) {
            return "/cadastro?faces-redirect=true";
        }
        return "/pages/cliente/novo-pedido?faces-redirect=true";
    }

    public void limparBusca() {
        termoBusca = null;
        veiculosFiltrados = todosVeiculos;
    }

    // Getters e Setters
    public List<Veiculo> getDestaques() { return destaques; }
    public List<Veiculo> getLancamentos() { return lancamentos; }
    public List<Veiculo> getTodosVeiculos() { return todosVeiculos; }
    public List<Veiculo> getVeiculosFiltrados() { return veiculosFiltrados; }

    public String getTermoBusca() { return termoBusca; }
    public void setTermoBusca(String termoBusca) { this.termoBusca = termoBusca; }

    public Veiculo getVeiculoSelecionado() { return veiculoSelecionado; }
    public void setVeiculoSelecionado(Veiculo v) { this.veiculoSelecionado = v; }
}
