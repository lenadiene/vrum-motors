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
        if (veiculoSelecionado == null) {
            addErro("Nenhum veículo selecionado. Volte à página inicial, escolha um veículo e clique em Comprar.");
            return null;
        }
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
        // Nome
        String nome = cliente.getNome() == null ? "" : cliente.getNome().trim();
        if (nome.length() < 2) {
            addErro("Nome é obrigatório e deve ter ao menos 2 caracteres.");
            return false;
        }
        if (nome.length() > 100) {
            addErro("Nome não pode ultrapassar 100 caracteres.");
            return false;
        }
        // Apenas letras (incluindo acentos), espaços, hífens e apóstrofos
        if (!nome.matches("^[\\p{L}\\p{M}\\s\\-'.]+$")) {
            addErro("Nome inválido. Use apenas letras, espaços e hífens.");
            return false;
        }
        // Três ou mais letras iguais consecutivas
        if (nome.matches("(?i).*(.)\\1{2,}.*")) {
            addErro("Nome inválido. Não são permitidas três letras iguais seguidas.");
            return false;
        }

        // E-mail
        String email = cliente.getEmail() == null ? "" : cliente.getEmail().trim();
        if (email.isEmpty()) {
            addErro("E-mail é obrigatório.");
            return false;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            addErro("Informe um e-mail válido.");
            return false;
        }
        if (email.length() > 150) {
            addErro("E-mail não pode ultrapassar 150 caracteres.");
            return false;
        }

        // Telefone
        String telefone = cliente.getTelefone() == null ? "" : cliente.getTelefone().trim();
        if (telefone.isEmpty()) {
            addErro("Telefone é obrigatório.");
            return false;
        }
        if (telefone.length() > 15) {
            addErro("Telefone não pode ultrapassar 15 caracteres.");
            return false;
        }
        // Apenas dígitos, espaços, parênteses, hífens e +
        if (!telefone.matches("^[\\d\\s()\\-+.]+$")) {
            addErro("Telefone inválido. Use apenas dígitos e símbolos como (), - e +.");
            return false;
        }

        // Senha
        String senha = confirmacaoSenha == null ? "" : confirmacaoSenha.trim();
        if (senha.length() < 6) {
            addErro("Senha deve ter no mínimo 6 caracteres.");
            return false;
        }
        if (confirmacaoSenha.length() > 50) {
            addErro("Senha não pode ultrapassar 50 caracteres.");
            return false;
        }

        // Concessionária
        if (concessionariaId == null) {
            addErro("Selecione sua cidade/concessionária.");
            return false;
        }

        // CPF (obrigatório): apenas dígitos, pontos e hífens
        String cpf = cliente.getCpf() == null ? "" : cliente.getCpf().trim();
        if (cpf.isEmpty()) {
            addErro("CPF é obrigatório.");
            return false;
        }
        if (!cpf.matches("^[\\d.\\-]+$")) {
            addErro("CPF inválido. Use apenas dígitos, pontos e hífens.");
            return false;
        }
        if (cpf.length() > 14) {
            addErro("CPF não pode ultrapassar 14 caracteres.");
            return false;
        }
        if (!validarCPF(cpf)) {
            addErro("CPF inválido. Verifique os dígitos informados.");
            return false;
        }

        // Normaliza CPF e telefone para dígitos puros (comparação e armazenamento consistentes)
        String cpfDigits = cpf.replaceAll("\\D", "");
        String telDigits = telefone.replaceAll("\\D", "");

        // E-mail duplicado (apenas para novos cadastros)
        if (cliente.getId() == null && usuarioService.buscarPorEmail(email) != null) {
            addErro("E-mail já cadastrado. Faça login ou use outro e-mail.");
            return false;
        }

        // CPF duplicado (apenas para novos cadastros)
        if (cliente.getId() == null && usuarioService.cpfJaExiste(cpfDigits, null)) {
            addErro("CPF já cadastrado. Verifique os dados ou faça login.");
            return false;
        }

        // Telefone duplicado (apenas para novos cadastros)
        if (cliente.getId() == null && usuarioService.telefoneJaExiste(telDigits, null)) {
            addErro("Telefone já cadastrado. Use outro número ou faça login.");
            return false;
        }

        // Salva valores normalizados
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setTelefone(telDigits);
        cliente.setCpf(cpfDigits);

        return true;
    }

    private boolean validarCPF(String cpf) {
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11 || d.matches("(\\d)\\1{10}")) return false;
        int soma = 0, r;
        for (int i = 0; i < 9; i++) soma += (d.charAt(i) - '0') * (10 - i);
        r = soma % 11;
        if ((d.charAt(9) - '0') != (r < 2 ? 0 : 11 - r)) return false;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (d.charAt(i) - '0') * (11 - i);
        r = soma % 11;
        return (d.charAt(10) - '0') == (r < 2 ? 0 : 11 - r);
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro: " + msg, null));
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