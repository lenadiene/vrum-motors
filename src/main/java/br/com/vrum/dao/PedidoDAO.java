package br.com.vrum.dao;

import br.com.vrum.model.Pedido;
import br.com.vrum.model.StatusPedido;
import br.com.vrum.model.Cliente;
import br.com.vrum.model.Vendedor;
import br.com.vrum.model.Concessionaria;

import jakarta.persistence.EntityManager;
import java.util.List;

public class PedidoDAO extends GenericDAO<Pedido, Long> {

    public List<Pedido> listarPorCliente(Cliente cliente) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.cliente = :cliente ORDER BY p.dataPedido DESC",
                    Pedido.class)
                    .setParameter("cliente", cliente)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarPorConcessionaria(Concessionaria concessionaria) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.concessionaria = :c ORDER BY p.dataPedido DESC",
                    Pedido.class)
                    .setParameter("c", concessionaria)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarDisponiveisPorConcessionaria(Concessionaria concessionaria) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.concessionaria = :c AND p.vendedor IS NULL " +
                    "AND p.status = :status ORDER BY p.dataPedido ASC",
                    Pedido.class)
                    .setParameter("c", concessionaria)
                    .setParameter("status", StatusPedido.AGUARDANDO_ATENDIMENTO)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarPorVendedor(Vendedor vendedor) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.vendedor = :v ORDER BY p.dataAtualizacao DESC",
                    Pedido.class)
                    .setParameter("v", vendedor)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarPorStatus(StatusPedido status) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.status = :status ORDER BY p.dataPedido DESC",
                    Pedido.class)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarParaFabricacao() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.status IN :statuses ORDER BY p.dataPedido ASC",
                    Pedido.class)
                    .setParameter("statuses", List.of(
                            StatusPedido.EM_NEGOCIACAO,
                            StatusPedido.EM_FABRICACAO,
                            StatusPedido.FABRICADO,
                            StatusPedido.ENVIADO_CIDADE))
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Pedido buscarPorNumero(String numeroPedido) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p WHERE p.numeroPedido = :num", Pedido.class)
                    .setParameter("num", numeroPedido)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarTodosComDetalhes() {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p LEFT JOIN FETCH p.cliente LEFT JOIN FETCH p.veiculo " +
                    "LEFT JOIN FETCH p.concessionaria ORDER BY p.dataPedido DESC",
                    Pedido.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}