package br.com.vrum.dao;

import br.com.vrum.model.ConfiguracaoVeiculo;
import br.com.vrum.model.TipoConfiguracaoVeiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class ConfiguracaoVeiculoDAO extends GenericDAO<ConfiguracaoVeiculo, Long> {

    public List<ConfiguracaoVeiculo> listarPorTipo(TipoConfiguracaoVeiculo tipo) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT c FROM ConfiguracaoVeiculo c WHERE c.tipo = :tipo ORDER BY c.ordem, c.valor",
                    ConfiguracaoVeiculo.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void excluirPorTipo(TipoConfiguracaoVeiculo tipo) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM ConfiguracaoVeiculo c WHERE c.tipo = :tipo")
              .setParameter("tipo", tipo)
              .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao excluir configurações: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public boolean existeAlgum() {
        EntityManager em = getEM();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(c) FROM ConfiguracaoVeiculo c", Long.class)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
