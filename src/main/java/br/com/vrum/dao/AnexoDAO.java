package br.com.vrum.dao;

import br.com.vrum.model.Anexo;
import br.com.vrum.model.Pedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class AnexoDAO extends GenericDAO<Anexo, Long> {

    public Anexo salvarAnexo(Long pedidoId, String nomeArquivo, String caminho, String tipo, Long tamanho) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Anexo anexo = new Anexo();
            anexo.setPedido(em.getReference(Pedido.class, pedidoId));
            anexo.setNomeArquivo(nomeArquivo);
            anexo.setCaminhoArquivo(caminho);
            anexo.setTipoArquivo(tipo);
            anexo.setTamanho(tamanho);
            em.persist(anexo);
            tx.commit();
            return anexo;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao salvar anexo: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

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
