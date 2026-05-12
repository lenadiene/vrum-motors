package br.com.vrum.filter;

import br.com.vrum.model.PerfilUsuario;
import br.com.vrum.model.Usuario;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/pages/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        // Não autenticado → login
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
            return;
        }

        PerfilUsuario perfil = usuario.getPerfil();

        // Controle de acesso por URL
        if (uri.contains("/pages/admin/") && perfil != PerfilUsuario.ADMIN_EMPRESA) {
            response.sendRedirect(request.getContextPath() + "/acesso-negado.xhtml");
            return;
        }
        if (uri.contains("/pages/gerente/") && perfil != PerfilUsuario.GERENTE) {
            response.sendRedirect(request.getContextPath() + "/acesso-negado.xhtml");
            return;
        }
        if (uri.contains("/pages/fabrica/") && perfil != PerfilUsuario.ADMIN_FABRICA) {
            response.sendRedirect(request.getContextPath() + "/acesso-negado.xhtml");
            return;
        }
        if (uri.contains("/pages/vendedor/") && perfil != PerfilUsuario.VENDEDOR) {
            response.sendRedirect(request.getContextPath() + "/acesso-negado.xhtml");
            return;
        }
        if (uri.contains("/pages/cliente/") && perfil != PerfilUsuario.CLIENTE) {
            response.sendRedirect(request.getContextPath() + "/acesso-negado.xhtml");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig fc) {}
    @Override public void destroy() {}
}
