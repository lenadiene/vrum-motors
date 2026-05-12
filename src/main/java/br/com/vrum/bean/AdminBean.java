package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.*;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Usuários
    private List<Usuario> usuarios;
    private Usuario usuarioEdicao;
    private String senhaUsuario;
    private PerfilUsuario perfilFiltro;

    // Concessionárias
    private List<Concessionaria> concessionarias;
    private Concessionaria concessionariaEdicao = new Concessionaria();

    // Veículos
    private List<Veiculo> veiculos;
    private Veiculo veiculoEdicao = new Veiculo();

    // Pedidos
    private List<Pedido> pedidos;

    private final UsuarioService usuarioService = new UsuarioService();
    private final ConcessionariaService concService = new ConcessionariaService();
    private final VeiculoService veiculoService = new VeiculoService();
    private final PedidoService pedidoService = new PedidoService();

    @PostConstruct
    public void init() {
        carregarTudo();
    }

    public void carregarTudo() {
        usuarios = usuarioService.listarTodos();
        concessionarias = concService.listarTodas();
        veiculos = veiculoService.listarTodos();
        pedidos = pedidoService.listarTodos();
    }

    // ---- USUÁRIOS ----
    public void novoUsuario() {
        usuarioEdicao = new Cliente(); // default, pode mudar via perfil
        senhaUsuario = null;
    }

    public void editarUsuario(Usuario u) {
        usuarioEdicao = u;
    }

    public void salvarUsuario() {
        try {
            usuarioService.salvarUsuario(usuarioEdicao, senhaUsuario);
            addSucesso("Usuário salvo com sucesso!");
            usuarios = usuarioService.listarTodos();
            usuarioEdicao = null;
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void inativarUsuario(Usuario u) {
        usuarioService.inativar(u);
        addSucesso("Usuário inativado.");
        usuarios = usuarioService.listarTodos();
    }

    public void reativarUsuario(Usuario u) {
        usuarioService.reativar(u);
        addSucesso("Usuário reativado.");
        usuarios = usuarioService.listarTodos();
    }

    // ---- CONCESSIONÁRIAS ----
    public void novaConcessionaria() {
        concessionariaEdicao = new Concessionaria();
    }

    public void editarConcessionaria(Concessionaria c) {
        concessionariaEdicao = c;
    }

    public void salvarConcessionaria() {
        try {
            concService.salvar(concessionariaEdicao);
            addSucesso("Concessionária salva!");
            concessionarias = concService.listarTodas();
            concessionariaEdicao = new Concessionaria();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    // ---- VEÍCULOS ----
    public void novoVeiculo() {
        veiculoEdicao = new Veiculo();
    }

    public void editarVeiculo(Veiculo v) {
        veiculoEdicao = v;
    }

    public void salvarVeiculo() {
        try {
            veiculoService.salvar(veiculoEdicao);
            addSucesso("Veículo salvo!");
            veiculos = veiculoService.listarTodos();
            veiculoEdicao = new Veiculo();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void excluirVeiculo(Veiculo v) {
        veiculoService.excluir(v);
        addSucesso("Veículo removido do catálogo.");
        veiculos = veiculoService.listarTodos();
    }

    public TipoVeiculo[] getTiposVeiculo() { return TipoVeiculo.values(); }
    public PerfilUsuario[] getPerfis() { return PerfilUsuario.values(); }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    // Getters e Setters
    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario getUsuarioEdicao() { return usuarioEdicao; }
    public void setUsuarioEdicao(Usuario u) { this.usuarioEdicao = u; }
    public String getSenhaUsuario() { return senhaUsuario; }
    public void setSenhaUsuario(String s) { this.senhaUsuario = s; }
    public PerfilUsuario getPerfilFiltro() { return perfilFiltro; }
    public void setPerfilFiltro(PerfilUsuario p) { this.perfilFiltro = p; }
    public List<Concessionaria> getConcessionarias() { return concessionarias; }
    public Concessionaria getConcessionariaEdicao() { return concessionariaEdicao; }
    public void setConcessionariaEdicao(Concessionaria c) { this.concessionariaEdicao = c; }
    public List<Veiculo> getVeiculos() { return veiculos; }
    public Veiculo getVeiculoEdicao() { return veiculoEdicao; }
    public void setVeiculoEdicao(Veiculo v) { this.veiculoEdicao = v; }
    public List<Pedido> getPedidos() { return pedidos; }
}
