package br.com.vrum.dao;

import br.com.vrum.model.Anexo;
import br.com.vrum.model.Pedido;
import jakarta.persistence.EntityManager;
import java.util.List;

public class AnexoDAO extends GenericDAO<Anexo, Long> {

    public List<Anexo> listarPorPedido(Pedido pedido) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT a FROM Anexo a WHERE a.pedido = :pedido ORDER BY a.dataUpload DESC",
                    Anexo.class)
                    .setParameter("pedido", pedido)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
