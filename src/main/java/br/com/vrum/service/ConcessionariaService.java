package br.com.vrum.service;

import br.com.vrum.dao.ConcessionariaDAO;
import br.com.vrum.dao.VendedorDAO;
import br.com.vrum.model.Concessionaria;
import br.com.vrum.model.Vendedor;

import java.util.List;

public class ConcessionariaService {

    private final ConcessionariaDAO concDAO = new ConcessionariaDAO();
    private final VendedorDAO vendedorDAO = new VendedorDAO();

    public Concessionaria salvar(Concessionaria c) {
        if (c.getId() == null) return concDAO.salvar(c);
        return concDAO.atualizar(c);
    }

    public void inativar(Concessionaria c) {
        c.setAtiva(false);
        concDAO.atualizar(c);
    }

    public List<Concessionaria> listarTodas() { return concDAO.listarTodos(); }
    public List<Concessionaria> listarAtivas() { return concDAO.listarAtivas(); }
    public Concessionaria buscarPorId(Long id) { return concDAO.buscarPorId(id); }
    public Concessionaria buscarPorCidade(String cidade) { return concDAO.buscarPorCidade(cidade); }

    public List<Vendedor> listarVendedores(Concessionaria c) {
        return vendedorDAO.listarPorConcessionaria(c);
    }
}
