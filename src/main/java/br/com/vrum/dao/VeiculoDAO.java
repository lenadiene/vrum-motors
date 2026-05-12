package br.com.vrum.dao;

import br.com.vrum.model.Veiculo;
import br.com.vrum.model.TipoVeiculo;

import jakarta.persistence.EntityManager;
import java.util.List;

public class VeiculoDAO extends GenericDAO<Veiculo, Long> {

    public List<Veiculo> listarDisponiveis() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.disponivel = true ORDER BY v.nome", Veiculo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Veiculo> listarPorTipo(TipoVeiculo tipo) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Veiculo v WHERE v.tipo = :tipo AND v.disponivel = true ORDER BY v.nome",
                    Veiculo.class)
                    .setParameter("tipo", tipo)
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
        // Usando query nativa SQL em vez de JPQL
        return em.createNativeQuery(
                "SELECT * FROM veiculos v WHERE v.tipo = 'LANCAMENTO' AND v.disponivel = 1 ORDER BY v.ano DESC",
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
}