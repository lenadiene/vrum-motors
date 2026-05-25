package br.com.vrum.bean;

import java.io.Serializable;
import java.util.List;

import br.com.vrum.model.Veiculo;
import br.com.vrum.service.VeiculoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

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
    private Long veiculoDetalheId;

    private final VeiculoService service = new VeiculoService();

    @PostConstruct
    public void init() {
        destaques         = service.listarDestaques();
        lancamentos       = service.listarLancamentos();
        todosVeiculos     = service.listarDisponiveis();
        veiculosFiltrados = todosVeiculos;
    }

    public void buscar() {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            veiculosFiltrados = todosVeiculos;
            return;
        }
        String termo = termoBusca.trim();
        if (termo.length() > 100) termo = termo.substring(0, 100);
        // Remove caracteres HTML/script para evitar XSS
        termo = termo.replaceAll("[<>\"&]", "");
        termoBusca = termo;
        if (termo.isEmpty()) {
            veiculosFiltrados = todosVeiculos;
        } else {
            veiculosFiltrados = service.buscarPorNome(termo);
        }
    }

    public void abrirDetalhesAjax(AjaxBehaviorEvent event) {
        if (veiculoDetalheId != null) {
            veiculoSelecionado = service.buscarPorId(veiculoDetalheId);
        }
    }

    public String comprar(Long veiculoId) {
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("veiculoCompraId", veiculoId);
        Object usuario = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuarioLogado");
        if (usuario == null) return "/cadastro?faces-redirect=true";
        return "/pages/cliente/novo-pedido?faces-redirect=true";
    }

    public String selecionarVeiculo(Long id) {
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("veiculoSelecionadoId", id);
        return "/pages/veiculo?faces-redirect=true";
    }

    public void limparBusca() {
        termoBusca = null;
        veiculosFiltrados = todosVeiculos;
    }

    public List<Veiculo> getDestaques()           { return destaques; }
    public List<Veiculo> getLancamentos()         { return lancamentos; }
    public List<Veiculo> getTodosVeiculos()       { return todosVeiculos; }
    public List<Veiculo> getVeiculosFiltrados()   { return veiculosFiltrados; }
    public String getTermoBusca()                 { return termoBusca; }
    public void setTermoBusca(String t)           { this.termoBusca = t; }
    public Veiculo getVeiculoSelecionado()        { return veiculoSelecionado; }
    public void setVeiculoSelecionado(Veiculo v)  { this.veiculoSelecionado = v; }
    public Long getVeiculoDetalheId()             { return veiculoDetalheId; }
    public void setVeiculoDetalheId(Long id)      { this.veiculoDetalheId = id; }
}