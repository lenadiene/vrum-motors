package br.com.vrum.dao;

import java.util.List;

import br.com.vrum.model.TipoVeiculo;
import br.com.vrum.model.Veiculo;
import jakarta.persistence.EntityManager;

public class VeiculoDAO extends GenericDAO<Veiculo, Long> {

    public List<Veiculo> listarDisponiveis() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.disponivel = true ORDER BY v.nome",
                    Veiculo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Veiculo> listarPorTipo(TipoVeiculo tipo) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.tipo = br.com.vrum.model.TipoVeiculo." + tipo.name() +
                    " AND v.disponivel = true ORDER BY v.nome",
                    Veiculo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Veiculo> listarDestaques() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.destaqueHome = true AND v.disponivel = true ORDER BY v.nome",
                    Veiculo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Veiculo> listarLancamentos() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.tipo = br.com.vrum.model.TipoVeiculo.LANCAMENTO" +
                    " AND v.disponivel = true ORDER BY v.ano DESC",
                    Veiculo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Veiculo> buscarPorNome(String nome) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE LOWER(v.nome) LIKE :nome OR LOWER(v.marca) LIKE :nome ORDER BY v.nome",
                    Veiculo.class)
                    .setParameter("nome", "%" + nome.toLowerCase() + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Veiculo buscarPorId(Long id) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.id = :id", Veiculo.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
}