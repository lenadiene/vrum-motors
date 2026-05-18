package br.com.vrum.service;

import br.com.vrum.dao.PedidoDAO;
import br.com.vrum.dao.AnexoDAO;
import br.com.vrum.model.*;

import java.time.LocalDate;
import java.util.List;

public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final AnexoDAO anexoDAO = new AnexoDAO();

    public Pedido realizarPedido(Cliente cliente, Veiculo veiculo, Concessionaria concessionaria, String cor) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setVeiculo(veiculo);
        pedido.setConcessionaria(concessionaria);
        pedido.setCorEscolhida(cor);
        pedido.setStatus(StatusPedido.AGUARDANDO_ATENDIMENTO);
        return pedidoDAO.salvar(pedido);
    }

    /**
     * Vendedor assume um pedido da fila
     */
    public void assumirPedido(Pedido pedido, Vendedor vendedor) {
        if (pedido.getVendedor() != null) {
            throw new IllegalStateException("Este pedido já foi assumido por outro vendedor.");
        }
        pedido.setVendedor(vendedor);
        pedido.setStatus(StatusPedido.EM_NEGOCIACAO);
        pedidoDAO.atualizar(pedido);
    }

    /**
     * Vendedor envia pedido para fabricação
     */
    public void enviarParaFabricacao(Pedido pedido, LocalDate prazoFabricacao, String formaPagamento) {
        validarVendedorDoPedido(pedido);
        pedido.setStatus(StatusPedido.EM_FABRICACAO);
        pedido.setPrazoFabricacao(prazoFabricacao);
        pedido.setFormaPagamento(formaPagamento);
        pedidoDAO.atualizar(pedido);
    }

    /**
     * Admin fábrica atualiza status de fabricação
     */
    public void atualizarStatusFabricacao(Pedido pedido, StatusPedido novoStatus, LocalDate prazoEntrega, String obs) {
        if (novoStatus != StatusPedido.EM_FABRICACAO &&
            novoStatus != StatusPedido.FABRICADO &&
            novoStatus != StatusPedido.ENVIADO_CIDADE) {
            throw new IllegalArgumentException("Status inválido para a fábrica: " + novoStatus);
        }
        pedido.setStatus(novoStatus);
        if (prazoEntrega != null) pedido.setPrazoEntrega(prazoEntrega);
        if (obs != null && !obs.isEmpty()) pedido.setObservacoesFabrica(obs);
        pedidoDAO.atualizar(pedido);
    }

    /**
     * Vendedor marca como pronto para entrega (veículo chegou na loja)
     */
    public void marcarProntoEntrega(Pedido pedido) {
        validarVendedorDoPedido(pedido);
        pedido.setStatus(StatusPedido.PRONTO_ENTREGA);
        pedidoDAO.atualizar(pedido);
    }

    /**
     * Vendedor finaliza o pedido com data de retirada
     */
    public void finalizarPedido(Pedido pedido, LocalDate dataRetirada) {
        validarVendedorDoPedido(pedido);
        pedido.setStatus(StatusPedido.FINALIZADO);
        pedido.setDataRetirada(dataRetirada);
        pedidoDAO.atualizar(pedido);
    }

    public void cancelarPedido(Pedido pedido) {
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoDAO.atualizar(pedido);
    }

    public void adicionarObservacao(Pedido pedido, String obs) {
        pedido.setObservacoes(obs);
        pedidoDAO.atualizar(pedido);
    }

    public Anexo adicionarAnexo(Long pedidoId, String nomeArquivo, String caminho, String tipo, Long tamanho) {
        return anexoDAO.salvarAnexo(pedidoId, nomeArquivo, caminho, tipo, tamanho);
    }

    public List<Pedido> listarPorCliente(Cliente cliente) {
        return pedidoDAO.listarPorCliente(cliente);
    }

    public List<Pedido> listarPorConcessionaria(Concessionaria c) {
        return pedidoDAO.listarPorConcessionaria(c);
    }

    public List<Pedido> listarDisponiveisPorConcessionaria(Concessionaria c) {
        return pedidoDAO.listarDisponiveisPorConcessionaria(c);
    }

    public List<Pedido> listarPorVendedor(Vendedor v) {
        return pedidoDAO.listarPorVendedor(v);
    }

    public List<Pedido> listarParaFabricacao() {
        return pedidoDAO.listarParaFabricacao();
    }

    public List<Pedido> listarTodos() {
        return pedidoDAO.listarTodosComDetalhes();
    }

    public List<Anexo> listarAnexos(Pedido pedido) {
        return anexoDAO.listarPorPedido(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoDAO.buscarPorId(id);
    }

    private void validarVendedorDoPedido(Pedido pedido) {
        if (pedido.getVendedor() == null) {
            throw new IllegalStateException("Pedido sem vendedor responsável.");
        }
    }
}
