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
    private Long concessionariaIdSelecionada;

    // Concessionárias
    private List<Concessionaria> concessionarias;
    private Concessionaria concessionariaEdicao;

    // Veículos
    private List<Veiculo> veiculos;
    private Veiculo veiculoEdicao = new Veiculo();
    private boolean mostrarFormVeiculo = false;

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
        usuarioEdicao = new Cliente();
        senhaUsuario = null;
        concessionariaIdSelecionada = null;
    }

    public void cancelarEdicaoUsuario() {
        usuarioEdicao = null;
        senhaUsuario = null;
        concessionariaIdSelecionada = null;
    }

    public void editarUsuario(Usuario u) {
        usuarioEdicao = u;
        concessionariaIdSelecionada = null;
        if (u instanceof Vendedor) {
            Concessionaria c = ((Vendedor) u).getConcessionaria();
            if (c != null) concessionariaIdSelecionada = c.getId();
        } else if (u instanceof Gerente) {
            Concessionaria c = ((Gerente) u).getConcessionaria();
            if (c != null) concessionariaIdSelecionada = c.getId();
        }
    }

    public void salvarUsuario() {
        try {
            PerfilUsuario perfil = usuarioEdicao.getPerfil();
            if (perfil == null) {
                addErro("Selecione um perfil.");
                return;
            }
            if ((perfil == PerfilUsuario.VENDEDOR || perfil == PerfilUsuario.GERENTE)
                    && concessionariaIdSelecionada == null) {
                addErro("Selecione a concessionária para este perfil.");
                return;
            }

            Usuario entidade;
            if (usuarioEdicao.getId() == null) {
                entidade = construirEntidade(perfil);
                entidade.setNome(usuarioEdicao.getNome());
                entidade.setEmail(usuarioEdicao.getEmail());
                entidade.setTelefone(usuarioEdicao.getTelefone());
                entidade.setCpf(usuarioEdicao.getCpf());
            } else {
                entidade = usuarioEdicao;
            }
            atribuirConcessionaria(entidade);
            usuarioService.salvarUsuario(entidade, senhaUsuario);
            addSucesso("Usuário salvo com sucesso!");
            usuarios = usuarioService.listarTodos();
            usuarioEdicao = null;
            concessionariaIdSelecionada = null;
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    private Usuario construirEntidade(PerfilUsuario perfil) {
        switch (perfil) {
            case ADMIN_EMPRESA: return new AdminEmpresa();
            case ADMIN_FABRICA: return new AdminFabrica();
            case GERENTE:       return new Gerente();
            case VENDEDOR:      return new Vendedor();
            default:            return new Cliente();
        }
    }

    private void atribuirConcessionaria(Usuario usuario) {
        if (concessionariaIdSelecionada == null) return;
        Concessionaria conc = concessionarias.stream()
                .filter(c -> c.getId().equals(concessionariaIdSelecionada))
                .findFirst().orElse(null);
        if (usuario instanceof Vendedor) {
            ((Vendedor) usuario).setConcessionaria(conc);
        } else if (usuario instanceof Gerente) {
            ((Gerente) usuario).setConcessionaria(conc);
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

    public void cancelarEdicaoConcessionaria() {
        concessionariaEdicao = null;
    }

    public void editarConcessionaria(Concessionaria c) {
        concessionariaEdicao = c;
    }

    public void salvarConcessionaria() {
        try {
            concService.salvar(concessionariaEdicao);
            addSucesso("Concessionária salva!");
            concessionarias = concService.listarTodas();
            concessionariaEdicao = null;
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    // ---- VEÍCULOS ----
    public void novoVeiculo() {
        veiculoEdicao = new Veiculo();
        mostrarFormVeiculo = true;
    }

    public void cancelarEdicaoVeiculo() {
        veiculoEdicao = new Veiculo();
        mostrarFormVeiculo = false;
    }

    public void editarVeiculo(Veiculo v) {
        veiculoEdicao = v;
        mostrarFormVeiculo = true;
    }

    public void salvarVeiculo() {
        try {
            veiculoService.salvar(veiculoEdicao);
            addSucesso("Veículo salvo!");
            veiculos = veiculoService.listarTodos();
            veiculoEdicao = new Veiculo();
            mostrarFormVeiculo = false;
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
    public boolean isMostrarFormVeiculo() { return mostrarFormVeiculo; }
    public List<Pedido> getPedidos() { return pedidos; }
    public Long getConcessionariaIdSelecionada() { return concessionariaIdSelecionada; }
    public void setConcessionariaIdSelecionada(Long id) { this.concessionariaIdSelecionada = id; }
}
