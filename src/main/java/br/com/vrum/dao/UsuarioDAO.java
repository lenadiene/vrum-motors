package br.com.vrum.dao;

import br.com.vrum.model.Usuario;
import br.com.vrum.model.PerfilUsuario;
import br.com.vrum.util.SenhaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class UsuarioDAO extends GenericDAO<Usuario, Long> {

    public Usuario buscarPorEmail(String email) {
        EntityManager em = getEM();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Usuario autenticar(String email, String senha) {
        EntityManager em = getEM();
        try {
            String senhaHash = SenhaUtil.hashSenha(senha);
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email AND u.senha = :senha AND u.ativo = true",
                    Usuario.class)
                    .setParameter("email", email)
                    .setParameter("senha", senhaHash)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarPorPerfil(PerfilUsuario perfil) {
        EntityManager em = getEM();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.perfil = :perfil ORDER BY u.nome", Usuario.class)
                    .setParameter("perfil", perfil)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean emailJaExiste(String email, Long idExcluir) {
        EntityManager em = getEM();
        try {
            String jpql = idExcluir == null
                    ? "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email"
                    : "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email AND u.id <> :id";
            var query = em.createQuery(jpql, Long.class).setParameter("email", email);
            if (idExcluir != null) query.setParameter("id", idExcluir);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}
