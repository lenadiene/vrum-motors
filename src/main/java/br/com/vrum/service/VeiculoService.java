package br.com.vrum.service;

import java.util.List;

import br.com.vrum.dao.VeiculoDAO;
import br.com.vrum.model.TipoVeiculo;
import br.com.vrum.model.Veiculo;
import java.math.BigDecimal;

public class VeiculoService {

    private final VeiculoDAO veiculoDAO = new VeiculoDAO();

    public Veiculo salvar(Veiculo veiculo) {
        if (veiculo.getId() == null) return veiculoDAO.salvar(veiculo);
        return veiculoDAO.atualizar(veiculo);
    }

    public void excluir(Veiculo veiculo) {
        veiculo.setDisponivel(false);
        veiculoDAO.atualizar(veiculo);
    }

    public List<Veiculo> buscarComFiltros(
            String termo,
            TipoVeiculo tipo,
            BigDecimal precoMinimo,
            BigDecimal precoMaximo) {

        return veiculoDAO.buscarComFiltros(
                termo,
                tipo,
                precoMinimo,
                precoMaximo
        );
    }


    public List<Veiculo> listarTodos()                    { return veiculoDAO.listarTodos(); }
    public List<Veiculo> listarDisponiveis()              { return veiculoDAO.listarDisponiveis(); }
    public List<Veiculo> listarDestaques()                { return veiculoDAO.listarDestaques(); }
    public List<Veiculo> listarLancamentos()              { return veiculoDAO.listarLancamentos(); }
    public List<Veiculo> listarPorTipo(TipoVeiculo tipo)  { return veiculoDAO.listarPorTipo(tipo); }
    public List<Veiculo> buscarPorNome(String nome)       { return veiculoDAO.buscarPorNome(nome); }
    public Veiculo buscarPorId(Long id)                   { return veiculoDAO.buscarPorId(id); }
}