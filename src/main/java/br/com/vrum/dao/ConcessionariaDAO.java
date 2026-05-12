package br.com.vrum.dao;

import br.com.vrum.model.Concessionaria;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ConcessionariaDAO extends GenericDAO<Concessionaria, Long> {

    public List<Concessionaria> listarAtivas() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT c FROM Concessionaria c WHERE c.ativa = true ORDER BY c.cidade",
                    Concessionaria.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Concessionaria buscarPorCidade(String cidade) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT c FROM Concessionaria c WHERE LOWER(c.cidade) = LOWER(:cidade)",
                    Concessionaria.class)
                    .setParameter("cidade", cidade)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
}
