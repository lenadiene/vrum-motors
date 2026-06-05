package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.*;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Named("gerenteBean")
@ViewScoped
public class GerenteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Gerente gerente;
    private Concessionaria concessionaria;
    private List<Vendedor> vendedores;
    private List<Pedido> pedidos;
    private Vendedor vendedorEdicao;
    private String senhaVendedor;

    private final UsuarioService usuarioService = new UsuarioService();
    private final PedidoService pedidoService = new PedidoService();
    private final ConcessionariaService concService = new ConcessionariaService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

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

    public void cancelarEdicaoVendedor() {
        vendedorEdicao = null;
        senhaVendedor = null;
    }

    public void editarVendedor(Vendedor v) {
        vendedorEdicao = v;
        senhaVendedor = null;
    }

    public void salvarVendedor() {
        if (!validarVendedor()) {
            return;
        }

        try {
            vendedorEdicao.setConcessionaria(concessionaria);
            usuarioService.salvarUsuario(vendedorEdicao, senhaVendedor);
            addSucesso("Vendedor salvo com sucesso!");
            carregarDados();
            vendedorEdicao = null;
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    private boolean validarVendedor() {
        if (gerente == null || concessionaria == null) {
            addErro("Sessão inválida. Faça login novamente.");
            return false;
        }

        if (vendedorEdicao == null) {
            addErro("Nenhum vendedor selecionado para edição.");
            return false;
        }

        String nome = vendedorEdicao.getNome() != null ? vendedorEdicao.getNome().trim() : "";
        if (nome.length() < 2) {
            addErro("Informe o nome do vendedor com pelo menos 2 caracteres.");
            return false;
        }
        if (nome.length() > 100) {
            addErro("O nome deve ter no máximo 100 caracteres.");
            return false;
        }
        vendedorEdicao.setNome(nome);

        String email = vendedorEdicao.getEmail() != null ? vendedorEdicao.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty()) {
            addErro("Informe o e-mail do vendedor.");
            return false;
        }
        if (email.length() > 150) {
            addErro("O e-mail deve ter no máximo 150 caracteres.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            addErro("Informe um e-mail válido.");
            return false;
        }
        vendedorEdicao.setEmail(email);

        String telefone = vendedorEdicao.getTelefone() != null
                ? vendedorEdicao.getTelefone().replaceAll("\\D", "")
                : "";
        if (!telefone.isEmpty() && (telefone.length() < 10 || telefone.length() > 11)) {
            addErro("Informe um telefone válido com DDD e 10 ou 11 dígitos.");
            return false;
        }
        vendedorEdicao.setTelefone(telefone.isEmpty() ? null : telefone);

        boolean novoVendedor = vendedorEdicao.getId() == null;
        String senha = senhaVendedor != null ? senhaVendedor.trim() : "";
        if (novoVendedor && senha.isEmpty()) {
            addErro("Informe a senha do novo vendedor.");
            return false;
        }
        if (!senha.isEmpty() && (senha.length() < 6 || senha.length() > 50)) {
            addErro("A senha deve ter entre 6 e 50 caracteres.");
            return false;
        }
        senhaVendedor = senha.isEmpty() ? null : senha;

        return true;
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
    public List<Pedido> getPedidosRecentes() {
        if (pedidos == null) return Collections.emptyList();
        return pedidos.subList(0, Math.min(5, pedidos.size()));
    }
    public Vendedor getVendedorEdicao() { return vendedorEdicao; }
    public void setVendedorEdicao(Vendedor v) { this.vendedorEdicao = v; }
    public String getSenhaVendedor() { return senhaVendedor; }
    public void setSenhaVendedor(String s) { this.senhaVendedor = s; }
}
