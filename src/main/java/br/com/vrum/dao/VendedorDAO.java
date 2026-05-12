package br.com.vrum.dao;

import br.com.vrum.model.Vendedor;
import br.com.vrum.model.Concessionaria;
import jakarta.persistence.EntityManager;
import java.util.List;

public class VendedorDAO extends GenericDAO<Vendedor, Long> {

    public List<Vendedor> listarPorConcessionaria(Concessionaria concessionaria) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT v FROM Vendedor v WHERE v.concessionaria = :c AND v.ativo = true ORDER BY v.nome",
                    Vendedor.class)
                    .setParameter("c", concessionaria)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
