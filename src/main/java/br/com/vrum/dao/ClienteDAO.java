package br.com.vrum.dao;

import br.com.vrum.model.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class ClienteDAO extends GenericDAO<Cliente, Long> {

    public Cliente buscarPorEmail(String email) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.email = :email", Cliente.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
