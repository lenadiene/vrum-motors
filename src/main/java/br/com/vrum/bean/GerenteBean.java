package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.*;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Named("gerenteBean")
@ViewScoped
public class GerenteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Gerente gerente;
    private Concessionaria concessionaria;
    private List<Vendedor> vendedores;
    private List<Pedido> pedidos;
    private Vendedor vendedorEdicao = new Vendedor();
    private String senhaVendedor;

    private final UsuarioService usuarioService = new UsuarioService();
    private final PedidoService pedidoService = new PedidoService();
    private final ConcessionariaService concService = new ConcessionariaService();

    @PostConstruct
    public void init() {
        Object obj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (obj instanceof Gerente) {
            gerente = (Gerente) obj;
            concessionaria = gerente.getConcessionaria();
            carregarDados();
        }
    }

    public void carregarDados() {
        vendedores = concService.listarVendedores(concessionaria);
        pedidos = pedidoService.listarPorConcessionaria(concessionaria);
    }

    public void novoVendedor() {
        vendedorEdicao = new Vendedor();
        vendedorEdicao.setConcessionaria(concessionaria);
        senhaVendedor = null;
    }

    public void editarVendedor(Vendedor v) {
        vendedorEdicao = v;
    }

    public void salvarVendedor() {
        try {
            vendedorEdicao.setConcessionaria(concessionaria);
            usuarioService.salvarUsuario(vendedorEdicao, senhaVendedor);
            addSucesso("Vendedor salvo com sucesso!");
            carregarDados();
            vendedorEdicao = new Vendedor();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void inativarVendedor(Vendedor v) {
        usuarioService.inativar(v);
        addSucesso("Vendedor inativado.");
        carregarDados();
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    public Gerente getGerente() { return gerente; }
    public Concessionaria getConcessionaria() { return concessionaria; }
    public List<Vendedor> getVendedores() { return vendedores; }
    public List<Pedido> getPedidos() { return pedidos; }
    public Vendedor getVendedorEdicao() { return vendedorEdicao; }
    public void setVendedorEdicao(Vendedor v) { this.vendedorEdicao = v; }
    public String getSenhaVendedor() { return senhaVendedor; }
    public void setSenhaVendedor(String s) { this.senhaVendedor = s; }
}
