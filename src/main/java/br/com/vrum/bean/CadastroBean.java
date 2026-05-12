package br.com.vrum.bean;

import java.io.Serializable;
import java.util.List;

import br.com.vrum.model.Cliente;
import br.com.vrum.model.Concessionaria;
import br.com.vrum.model.Veiculo;
import br.com.vrum.service.ConcessionariaService;
import br.com.vrum.service.PedidoService;
import br.com.vrum.service.UsuarioService;
import br.com.vrum.service.VeiculoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named("cadastroBean")
@ViewScoped
public class CadastroBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Cliente cliente = new Cliente();
    private String confirmacaoSenha;
    private Veiculo veiculoSelecionado;
    private Long concessionariaId;
    private Concessionaria concessionariaSelecionada;
    private List<Concessionaria> concessionarias;
    private String corEscolhida;
    private int etapa = 1;

    private final UsuarioService usuarioService = new UsuarioService();
    private final PedidoService pedidoService = new PedidoService();
    private final VeiculoService veiculoService = new VeiculoService();
    private final ConcessionariaService concService = new ConcessionariaService();

    @PostConstruct
    public void init() {
        concessionarias = concService.listarAtivas();
        Long veiculoId = (Long) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("veiculoCompraId");
        if (veiculoId != null) {
            veiculoSelecionado = veiculoService.buscarPorId(veiculoId);
        }
        Object usuarioLogado = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (usuarioLogado instanceof Cliente) {
            cliente = (Cliente) usuarioLogado;
            etapa = 2;
        }
    }

    public void avancarEtapa() {
        if (etapa == 1) {
            if (!validarDadosCliente()) return;
            // Busca na lista já carregada, sem ir ao banco
            concessionariaSelecionada = concessionarias.stream()
                    .filter(c -> c.getId().equals(concessionariaId))
                    .findFirst()
                    .orElse(null);
            etapa = 2;
        }
    }

    public void voltarEtapa() {
        if (etapa > 1) etapa--;
    }

    public String finalizarPedido() {
        try {
            if (cliente.getId() == null) {
                usuarioService.salvarUsuario(cliente, confirmacaoSenha);
            }
            pedidoService.realizarPedido(
                    cliente, veiculoSelecionado, concessionariaSelecionada, corEscolhida);
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("usuarioLogado", cliente);
            etapa = 3;
            return null;
        } catch (IllegalArgumentException e) {
            addErro(e.getMessage());
            return null;
        } catch (Exception e) {
            addErro("Erro ao finalizar pedido: " + e.getMessage());
            return null;
        }
    }

    public String irParaMeusPedidos() {
        return "/pages/cliente/meus-pedidos?faces-redirect=true";
    }

    private boolean validarDadosCliente() {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            addErro("Nome é obrigatório.");
            return false;
        }
        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            addErro("E-mail é obrigatório.");
            return false;
        }
        if (concessionariaId == null) {
            addErro("Selecione sua cidade/concessionária.");
            return false;
        }
        return true;
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente c) { this.cliente = c; }

    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String s) { this.confirmacaoSenha = s; }

    public Veiculo getVeiculoSelecionado() { return veiculoSelecionado; }

    public Long getConcessionariaId() { return concessionariaId; }
    public void setConcessionariaId(Long id) { this.concessionariaId = id; }

    public Concessionaria getConcessionariaSelecionada() { return concessionariaSelecionada; }
    public void setConcessionariaSelecionada(Concessionaria c) { this.concessionariaSelecionada = c; }

    public List<Concessionaria> getConcessionarias() { return concessionarias; }

    public String getCorEscolhida() { return corEscolhida; }
    public void setCorEscolhida(String c) { this.corEscolhida = c; }

    public int getEtapa() { return etapa; }
}