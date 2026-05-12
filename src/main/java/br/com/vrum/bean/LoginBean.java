package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.UsuarioService;

import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String senha;
    private Usuario usuarioLogado;

    private final UsuarioService service = new UsuarioService();

    public String login() {
        Usuario usuario = service.autenticar(email, senha);

        if (usuario == null) {
            addErro("Login inválido. Verifique e-mail e senha.");
            return null;
        }

        if (!usuario.isAtivo()) {
            addErro("Usuário inativo. Entre em contato com o administrador.");
            return null;
        }

        this.usuarioLogado = usuario;
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(true);
        session.setAttribute("usuarioLogado", usuario);

        // Redireciona conforme perfil
        switch (usuario.getPerfil()) {
            case ADMIN_EMPRESA:  return "/pages/admin/dashboard?faces-redirect=true";
            case GERENTE:        return "/pages/gerente/dashboard?faces-redirect=true";
            case ADMIN_FABRICA:  return "/pages/fabrica/pedidos?faces-redirect=true";
            case VENDEDOR:       return "/pages/vendedor/pedidos?faces-redirect=true";
            case CLIENTE:        return "/pages/cliente/meus-pedidos?faces-redirect=true";
            default:             return "/index?faces-redirect=true";
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        this.usuarioLogado = null;
        this.email = null;
        this.senha = null;
        return "/login?faces-redirect=true";
    }

    public boolean isLogado() {
        return usuarioLogado != null;
    }

    public boolean isAdmin() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.ADMIN_EMPRESA;
    }

    public boolean isGerente() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.GERENTE;
    }

    public boolean isVendedor() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.VENDEDOR;
    }

    public boolean isCliente() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.CLIENTE;
    }

    public boolean isAdminFabrica() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.ADMIN_FABRICA;
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public void setUsuarioLogado(Usuario usuarioLogado) { this.usuarioLogado = usuarioLogado; }
}
