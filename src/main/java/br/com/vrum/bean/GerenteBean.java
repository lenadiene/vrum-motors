package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.*;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.Serializable;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
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
        return pedidosSeguros().stream()
                .filter(p -> p.getDataPedido() != null)
                .sorted(Comparator.comparing(Pedido::getDataPedido).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // Indicadores do dashboard da unidade
    public int getPedidosEsteMes() {
        YearMonth atual = YearMonth.now();
        return (int) pedidosSeguros().stream()
                .filter(p -> p.getDataPedido() != null && YearMonth.from(p.getDataPedido()).equals(atual))
                .count();
    }

    public int getPedidosEmAberto() {
        return (int) pedidosSeguros().stream()
                .filter(p -> p.getStatus() != StatusPedido.FINALIZADO
                        && p.getStatus() != StatusPedido.CANCELADO)
                .count();
    }

    public int getPedidosSemVendedor() {
        return (int) pedidosSeguros().stream()
                .filter(p -> p.getVendedor() == null)
                .count();
    }

    public int getPedidosComVendedor() {
        return (int) pedidosSeguros().stream()
                .filter(p -> p.getVendedor() != null)
                .count();
    }

    public int getPedidosConcluidos() {
        return (int) pedidosSeguros().stream()
                .filter(p -> p.getStatus() == StatusPedido.FINALIZADO)
                .count();
    }

    public int getVendedoresAtivos() {
        return (int) vendedoresSeguros().stream().filter(Vendedor::isAtivo).count();
    }

    public StatusPedido[] getStatusPedidoValues() {
        return StatusPedido.values();
    }

    public int quantidadeStatus(StatusPedido status) {
        return (int) pedidosSeguros().stream().filter(p -> p.getStatus() == status).count();
    }

    public String getTicketMedio() {
        OptionalDouble media = pedidosSeguros().stream()
                .filter(p -> p.getVeiculo() != null && p.getVeiculo().getPreco() != null)
                .mapToDouble(p -> p.getVeiculo().getPreco().doubleValue())
                .average();
        if (!media.isPresent()) return "R$ 0,00";
        BigDecimal valor = BigDecimal.valueOf(media.getAsDouble()).setScale(2, RoundingMode.HALF_UP);
        return "R$ " + String.format(new Locale("pt", "BR"), "%,.2f", valor);
    }

    public String getMesLabelsJson() {
        String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        YearMonth atual = YearMonth.now();
        List<String> labels = new java.util.ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth mes = atual.minusMonths(i);
            labels.add(meses[mes.getMonthValue() - 1] + "/" + String.format("%02d", mes.getYear() % 100));
        }
        return paraJsonTextos(labels);
    }

    public String getMesDataJson() {
        YearMonth atual = YearMonth.now();
        List<Long> dados = new java.util.ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth mes = atual.minusMonths(i);
            dados.add(pedidosSeguros().stream()
                    .filter(p -> p.getDataPedido() != null && YearMonth.from(p.getDataPedido()).equals(mes))
                    .count());
        }
        return paraJsonNumeros(dados);
    }

    public String getStatusLabelsJson() {
        Map<StatusPedido, Long> contagem = pedidosSeguros().stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));
        List<String> labels = new java.util.ArrayList<>();
        for (StatusPedido status : StatusPedido.values()) {
            if (contagem.getOrDefault(status, 0L) > 0) labels.add(status.getDescricao());
        }
        return paraJsonTextos(labels);
    }

    public String getStatusDataJson() {
        Map<StatusPedido, Long> contagem = pedidosSeguros().stream()
                .filter(p -> p.getStatus() != null)
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));
        List<Long> dados = new java.util.ArrayList<>();
        for (StatusPedido status : StatusPedido.values()) {
            long quantidade = contagem.getOrDefault(status, 0L);
            if (quantidade > 0) dados.add(quantidade);
        }
        return paraJsonNumeros(dados);
    }

    public String getVendedorLabelsJson() {
        return paraJsonTextos(new java.util.ArrayList<>(pedidosPorVendedor().keySet()));
    }

    public String getVendedorDataJson() {
        return paraJsonNumeros(new java.util.ArrayList<>(pedidosPorVendedor().values()));
    }

    public String getTipoLabelsJson() {
        return paraJsonTextos(new java.util.ArrayList<>(pedidosPorTipo().keySet()));
    }

    public String getTipoDataJson() {
        return paraJsonNumeros(new java.util.ArrayList<>(pedidosPorTipo().values()));
    }

    public String getVeiculoLabelsJson() {
        return paraJsonTextos(new java.util.ArrayList<>(pedidosPorVeiculo().keySet()));
    }

    public String getVeiculoDataJson() {
        return paraJsonNumeros(new java.util.ArrayList<>(pedidosPorVeiculo().values()));
    }

    public String classeStatus(StatusPedido status) {
        if (status == null) return "badge-aguardando";
        switch (status) {
            case AGUARDANDO_ATENDIMENTO: return "badge-aguardando";
            case EM_NEGOCIACAO:
            case AGUARDANDO_FABRICACAO: return "badge-negociacao";
            case EM_FABRICACAO: return "badge-fabricacao";
            case FABRICADO: return "badge-fabricado";
            case ENVIADO_CIDADE: return "badge-enviado";
            case PRONTO_ENTREGA: return "badge-pronto";
            case FINALIZADO: return "badge-finalizado";
            case CANCELADO: return "badge-cancelado";
            default: return "badge-aguardando";
        }
    }

    private List<Pedido> pedidosSeguros() {
        return pedidos == null ? Collections.emptyList() : pedidos;
    }

    private List<Vendedor> vendedoresSeguros() {
        return vendedores == null ? Collections.emptyList() : vendedores;
    }

    private Map<String, Long> pedidosPorVendedor() {
        Map<String, Long> contagem = new LinkedHashMap<>();
        pedidosSeguros().stream()
                .filter(p -> p.getVendedor() != null)
                .collect(Collectors.groupingBy(p -> p.getVendedor().getNome(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> contagem.put(e.getKey(), e.getValue()));
        return contagem;
    }

    private Map<String, Long> pedidosPorTipo() {
        Map<String, Long> contagem = new LinkedHashMap<>();
        pedidosSeguros().stream()
                .filter(p -> p.getVeiculo() != null && p.getVeiculo().getTipo() != null)
                .collect(Collectors.groupingBy(p -> p.getVeiculo().getTipo().getDescricao(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> contagem.put(e.getKey(), e.getValue()));
        return contagem;
    }

    private Map<String, Long> pedidosPorVeiculo() {
        Map<String, Long> contagem = new LinkedHashMap<>();
        pedidosSeguros().stream()
                .filter(p -> p.getVeiculo() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getVeiculo().getNome() + " " + p.getVeiculo().getModelo(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .forEach(e -> contagem.put(e.getKey(), e.getValue()));
        return contagem;
    }

    private String paraJsonTextos(List<String> valores) {
        return valores.stream().map(this::escaparJson).collect(Collectors.joining(",", "[", "]"));
    }

    private String paraJsonNumeros(List<Long> valores) {
        return valores.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }

    private String escaparJson(String valor) {
        if (valor == null) return "null";
        return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("<", "\\u003c").replace(">", "\\u003e")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
    public Vendedor getVendedorEdicao() { return vendedorEdicao; }
    public void setVendedorEdicao(Vendedor v) { this.vendedorEdicao = v; }
    public String getSenhaVendedor() { return senhaVendedor; }
    public void setSenhaVendedor(String s) { this.senhaVendedor = s; }
}
