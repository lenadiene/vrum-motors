package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.model.TipoConfiguracaoVeiculo;
import br.com.vrum.service.*;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Usuários
    private List<Usuario> usuarios;
    private Usuario usuarioEdicao;
    private String senhaUsuario;
    private PerfilUsuario perfilFiltro;
    private Long concessionariaIdSelecionada;

    // Concessionárias
    private List<Concessionaria> concessionarias;
    private Concessionaria concessionariaEdicao;

    // Veículos
    private List<Veiculo> veiculos;
    private Veiculo veiculoEdicao = new Veiculo();
    private boolean mostrarFormVeiculo = false;
    private String precoTexto = "";
    private String anoTexto   = "";

    // Opções de seletores carregadas do banco
    private List<String> opcoesAnos;
    private List<String> opcoesMarcas;
    private List<String> opcoesMotores;
    private List<String> opcoesCombustiveis;
    private List<String> opcoesTransmissoes;
    private List<String> opcoesTracoes;

    // Pedidos
    private List<Pedido> pedidos;

    private final UsuarioService usuarioService = new UsuarioService();
    private final ConcessionariaService concService = new ConcessionariaService();
    private final VeiculoService veiculoService = new VeiculoService();
    private final PedidoService pedidoService = new PedidoService();
    private final ConfiguracaoVeiculoService cfgService = new ConfiguracaoVeiculoService();

    @PostConstruct
    public void init() {
        carregarTudo();
    }

    public void carregarTudo() {
        usuarios = usuarioService.listarTodos();
        concessionarias = concService.listarTodas();
        veiculos = veiculoService.listarTodos();
        pedidos = pedidoService.listarTodos();
        opcoesAnos         = cfgService.listarValores(TipoConfiguracaoVeiculo.ANO);
        opcoesMarcas       = cfgService.listarValores(TipoConfiguracaoVeiculo.MARCA);
        opcoesMotores      = cfgService.listarValores(TipoConfiguracaoVeiculo.MOTOR);
        opcoesCombustiveis = cfgService.listarValores(TipoConfiguracaoVeiculo.COMBUSTIVEL);
        opcoesTransmissoes = cfgService.listarValores(TipoConfiguracaoVeiculo.TRANSMISSAO);
        opcoesTracoes      = cfgService.listarValores(TipoConfiguracaoVeiculo.TRACAO);
    }

    // ---- USUÁRIOS ----
    public void novoUsuario() {
        usuarioEdicao = new Cliente();
        senhaUsuario = null;
        concessionariaIdSelecionada = null;
    }

    public void cancelarEdicaoUsuario() {
        usuarioEdicao = null;
        senhaUsuario = null;
        concessionariaIdSelecionada = null;
    }

    public void editarUsuario(Usuario u) {
        usuarioEdicao = u;
        concessionariaIdSelecionada = null;
        if (u instanceof Vendedor) {
            Concessionaria c = ((Vendedor) u).getConcessionaria();
            if (c != null) concessionariaIdSelecionada = c.getId();
        } else if (u instanceof Gerente) {
            Concessionaria c = ((Gerente) u).getConcessionaria();
            if (c != null) concessionariaIdSelecionada = c.getId();
        }
    }

    public void salvarUsuario() {
        FacesContext fc = FacesContext.getCurrentInstance();

        // --- 1. Sessão ---
        Usuario sessao = (Usuario) fc.getExternalContext().getSessionMap().get("usuarioLogado");
        if (sessao == null) {
            addErro("Sua sessão expirou. Realize o login novamente.");
            try {
                fc.getExternalContext().redirect(
                    fc.getExternalContext().getRequestContextPath() + "/login.xhtml");
            } catch (IOException ignored) {}
            return;
        }

        // --- 2. Permissão (checado via perfil — instanceof falha com EclipseLink JOINED) ---
        if (sessao.getPerfil() != PerfilUsuario.ADMIN_EMPRESA) {
            addErro("Você não tem permissão para executar esta ação.");
            return;
        }

        // --- 2b. Impede que o admin edite os próprios dados ---
        if (usuarioEdicao.getId() != null && usuarioEdicao.getId().equals(sessao.getId())) {
            addErro("Você não pode editar os seus próprios dados.");
            return;
        }

        // --- 3. Validação de campos (espelho server-side das regras do front) ---
        if (!validarCamposUsuario()) return;

        // --- 4. Lógica de negócio ---
        try {
            PerfilUsuario perfil = usuarioEdicao.getPerfil();
            if (perfil == null) {
                addErro("Selecione um perfil.");
                return;
            }
            if ((perfil == PerfilUsuario.VENDEDOR || perfil == PerfilUsuario.GERENTE)
                    && concessionariaIdSelecionada == null) {
                addErro("Selecione a concessionária para este perfil.");
                return;
            }

            Usuario entidade;
            if (usuarioEdicao.getId() == null) {
                entidade = construirEntidade(perfil);
                entidade.setNome(usuarioEdicao.getNome());
                entidade.setEmail(usuarioEdicao.getEmail());
                entidade.setTelefone(usuarioEdicao.getTelefone());
                entidade.setCpf(usuarioEdicao.getCpf());
            } else {
                entidade = usuarioEdicao;
            }

            if (perfil == PerfilUsuario.GERENTE && concessionariaIdSelecionada != null) {
                Long idAtual = entidade.getId();
                boolean ocupada = usuarios.stream()
                        .filter(u -> u instanceof Gerente)
                        .map(u -> (Gerente) u)
                        .filter(g -> g.getConcessionaria() != null
                                && g.getConcessionaria().getId().equals(concessionariaIdSelecionada))
                        .anyMatch(g -> !g.getId().equals(idAtual));
                if (ocupada) {
                    addErro("Esta concessionária já possui um gerente cadastrado.");
                    return;
                }
            }

            atribuirConcessionaria(entidade);
            usuarioService.salvarUsuario(entidade, senhaUsuario);
            addSucesso("Usuário salvo com sucesso!");
            usuarios = usuarioService.listarTodos();
            usuarioEdicao = null;
            concessionariaIdSelecionada = null;

        } catch (IllegalArgumentException e) {
            // Exceções de negócio: e-mail duplicado, telefone duplicado, etc.
            addErro(e.getMessage());
        } catch (PersistenceException e) {
            addErro(mapearErroPersistencia(e));
        } catch (RuntimeException e) {
            addErro(mapearErroRuntime(e));
        } catch (Exception e) {
            addErro("Não foi possível salvar os dados. Tente novamente.");
        }
    }

    private boolean validarCamposUsuario() {
        String nome = usuarioEdicao.getNome();
        if (nome == null || nome.trim().isEmpty()) {
            addErro("Informe um nome válido."); return false;
        }
        if (nome.matches(".*[0-9].*")) {
            addErro("O nome não pode conter números."); return false;
        }
        if (nome.matches(".*(.)\\1{4,}.*")) {
            addErro("O nome não pode conter caracteres repetidos em sequência."); return false;
        }
        if (nome.length() > 200) {
            addErro("O nome deve ter no máximo 200 caracteres."); return false;
        }

        String email = usuarioEdicao.getEmail();
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")) {
            addErro("Informe um e-mail válido."); return false;
        }

        String tel = usuarioEdicao.getTelefone();
        if (tel != null && !tel.isEmpty()) {
            String digits = tel.replaceAll("\\D", "");
            if (digits.length() != 11) {
                addErro("Informe um telefone válido com DDD e 9 dígitos."); return false;
            }
        }

        String cpf = usuarioEdicao.getCpf();
        if (cpf != null && !cpf.isEmpty()) {
            String digits = cpf.replaceAll("\\D", "");
            if (digits.length() != 11) {
                addErro("Informe um CPF válido com 11 dígitos."); return false;
            }
        }

        if (usuarioEdicao.getId() == null && (senhaUsuario == null || senhaUsuario.trim().isEmpty())) {
            addErro("Informe uma senha para o novo usuário."); return false;
        }
        return true;
    }

    private String mapearErroPersistencia(Throwable t) {
        String causa = getRootCauseMsg(t).toLowerCase();
        if (causa.contains("duplicate") || causa.contains("duplicado"))
            return "Registro duplicado. Verifique os dados informados.";
        if (causa.contains("timeout"))
            return "Timeout da requisição. Tente novamente mais tarde.";
        if (causa.contains("communications link") || causa.contains("connection refused"))
            return "Falha na comunicação com o banco de dados. Tente novamente.";
        return "Falha ao persistir os dados no banco. Tente novamente.";
    }

    private String mapearErroRuntime(Throwable t) {
        String causa = getRootCauseMsg(t).toLowerCase();
        if (causa.contains("duplicate") || causa.contains("duplicado"))
            return "Registro duplicado. Verifique os dados informados.";
        if (causa.contains("timeout"))
            return "Timeout da requisição. Tente novamente.";
        if (causa.contains("service unavailable") || causa.contains("connection refused"))
            return "Serviço temporariamente indisponível.";
        if (causa.contains("connection") || causa.contains("connect"))
            return "Falha na comunicação. Tente novamente.";
        return "Não foi possível salvar os dados. Tente novamente.";
    }

    private String getRootCauseMsg(Throwable t) {
        StringBuilder sb = new StringBuilder();
        while (t != null) {
            if (t.getMessage() != null) sb.append(t.getMessage()).append(' ');
            t = t.getCause();
        }
        return sb.toString();
    }

    private Usuario construirEntidade(PerfilUsuario perfil) {
        switch (perfil) {
            case ADMIN_EMPRESA: return new AdminEmpresa();
            case ADMIN_FABRICA: return new AdminFabrica();
            case GERENTE:       return new Gerente();
            case VENDEDOR:      return new Vendedor();
            default:            return new Cliente();
        }
    }

    private void atribuirConcessionaria(Usuario usuario) {
        if (concessionariaIdSelecionada == null) return;
        Concessionaria conc = concessionarias.stream()
                .filter(c -> c.getId().equals(concessionariaIdSelecionada))
                .findFirst().orElse(null);
        if (usuario instanceof Vendedor) {
            ((Vendedor) usuario).setConcessionaria(conc);
        } else if (usuario instanceof Gerente) {
            ((Gerente) usuario).setConcessionaria(conc);
        }
    }

    public void inativarUsuario(Usuario u) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Usuario sessao = (Usuario) fc.getExternalContext().getSessionMap().get("usuarioLogado");
        if (sessao != null && u.getId().equals(sessao.getId())) {
            addErro("Você não pode inativar sua própria conta.");
            return;
        }
        usuarioService.inativar(u);
        addSucesso("Usuário inativado.");
        usuarios = usuarioService.listarTodos();
    }

    public void reativarUsuario(Usuario u) {
        usuarioService.reativar(u);
        addSucesso("Usuário reativado.");
        usuarios = usuarioService.listarTodos();
    }

    // ---- CONCESSIONÁRIAS ----
    public void novaConcessionaria() {
        concessionariaEdicao = new Concessionaria();
    }

    public void cancelarEdicaoConcessionaria() {
        concessionariaEdicao = null;
    }

    public void editarConcessionaria(Concessionaria c) {
        concessionariaEdicao = c;
    }

    public void salvarConcessionaria() {
        if (!validarCamposConcessionaria()) return;
        try {
            concService.salvar(concessionariaEdicao);
            addSucesso("Concessionária salva!");
            concessionarias = concService.listarTodas();
            concessionariaEdicao = null;
        } catch (IllegalArgumentException e) {
            addErro(e.getMessage());
        } catch (PersistenceException e) {
            addErro(mapearErroPersistencia(e));
        } catch (RuntimeException e) {
            addErro(mapearErroRuntime(e));
        } catch (Exception e) {
            addErro("Não foi possível salvar os dados.");
        }
    }

    private boolean validarCamposConcessionaria() {
        String nome = concessionariaEdicao.getNome();
        if (nome == null || nome.trim().isEmpty()) {
            addErro("Informe o nome da unidade."); return false;
        }
        if (nome.length() > 300) {
            addErro("O nome deve ter no máximo 300 caracteres."); return false;
        }
        if (concessionariaEdicao.getEstado() == null || concessionariaEdicao.getEstado().trim().isEmpty()) {
            addErro("Selecione o estado (UF)."); return false;
        }
        if (concessionariaEdicao.getCidade() == null || concessionariaEdicao.getCidade().trim().isEmpty()) {
            addErro("Selecione a cidade."); return false;
        }
        String tel = concessionariaEdicao.getTelefone();
        if (tel == null || tel.isEmpty()) {
            addErro("Informe o telefone da unidade."); return false;
        }
        if (tel.replaceAll("\\D", "").length() != 11) {
            addErro("Informe um telefone válido com DDD e 9 dígitos."); return false;
        }
        String end = concessionariaEdicao.getEndereco();
        if (end == null || end.trim().isEmpty()) {
            addErro("Informe o endereço da unidade."); return false;
        }
        if (end.length() > 500) {
            addErro("O endereço deve ter no máximo 500 caracteres."); return false;
        }
        return true;
    }

    // ---- VEÍCULOS ----
    public void novoVeiculo() {
        veiculoEdicao = new Veiculo();
        precoTexto = "";
        anoTexto   = "";
        mostrarFormVeiculo = true;
    }

    public void cancelarEdicaoVeiculo() {
        veiculoEdicao = new Veiculo();
        precoTexto = "";
        anoTexto   = "";
        mostrarFormVeiculo = false;
    }

    public void editarVeiculo(Veiculo v) {
        veiculoEdicao = v;
        anoTexto   = v.getAno() != null ? v.getAno().toString() : "";
        precoTexto = v.getPreco() != null
            ? "R$ " + v.getPreco().toPlainString().replace(".", ",")
            : "";
        mostrarFormVeiculo = true;
    }

    public void salvarVeiculo() {
        // Valida marca
        if (veiculoEdicao.getMarca() == null || veiculoEdicao.getMarca().trim().isEmpty()) {
            addErro("Selecione a marca do veículo."); return;
        }

        // Validar e trim nome
        String nome = veiculoEdicao.getNome();
        if (nome != null) nome = nome.trim();
        if (nome == null || nome.isEmpty()) { addErro("Informe o nome do modelo."); return; }
        veiculoEdicao.setNome(nome);

        // Validar e trim modelo
        String modelo = veiculoEdicao.getModelo();
        if (modelo != null) modelo = modelo.trim();
        if (modelo == null || modelo.length() < 2) { addErro("O modelo deve ter no mínimo 2 caracteres."); return; }
        if (modelo.length() > 50)                  { addErro("O modelo deve ter no máximo 50 caracteres."); return; }
        veiculoEdicao.setModelo(modelo);

        // Validar ano (valores vêm do banco, select já restringe as opções)
        if (anoTexto == null || anoTexto.trim().isEmpty()) { addErro("Selecione o ano do veículo."); return; }
        try {
            veiculoEdicao.setAno(Integer.parseInt(anoTexto.trim()));
        } catch (NumberFormatException e) {
            addErro("Ano inválido."); return;
        }

        // Validar tipo
        if (veiculoEdicao.getTipo() == null) { addErro("Selecione o tipo do veículo."); return; }

        // Converter preço (máscara "R$ 150.000,00" → BigDecimal)
        if (precoTexto != null && !precoTexto.trim().isEmpty()) {
            try {
                String limpo = precoTexto
                    .replace("R$", "").replaceAll("\\s", "")
                    .replace(".", "").replace(",", ".");
                BigDecimal preco = new BigDecimal(limpo);
                if (preco.compareTo(BigDecimal.ZERO) < 0) { addErro("O preço não pode ser negativo."); return; }
                veiculoEdicao.setPreco(preco);
            } catch (NumberFormatException e) {
                addErro("Informe um preço válido."); return;
            }
        } else {
            veiculoEdicao.setPreco(null);
        }

        try {
            veiculoService.salvar(veiculoEdicao);
            addSucesso("Veículo salvo!");
            veiculos = veiculoService.listarTodos();
            veiculoEdicao = new Veiculo();
            precoTexto = "";
            anoTexto   = "";
            mostrarFormVeiculo = false;
        } catch (PersistenceException e) {
            addErro(mapearErroPersistencia(e));
        } catch (RuntimeException e) {
            addErro(mapearErroRuntime(e));
        } catch (Exception e) {
            addErro("Não foi possível salvar o veículo. Tente novamente.");
        }
    }

    public void excluirVeiculo(Veiculo v) {
        veiculoService.excluir(v);
        addSucesso("Veículo removido do catálogo.");
        veiculos = veiculoService.listarTodos();
    }

    public TipoVeiculo[] getTiposVeiculo() { return TipoVeiculo.values(); }
    public PerfilUsuario[] getPerfis()     { return PerfilUsuario.values(); }

    // ── Dashboard analytics ──────────────────────────────────────────────────

    public int getPedidosEsteMes() {
        YearMonth atual = YearMonth.now();
        return (int) pedidos.stream()
                .filter(p -> p.getDataPedido() != null
                        && YearMonth.from(p.getDataPedido()).equals(atual))
                .count();
    }

    public int getPedidosEmAberto() {
        return (int) pedidos.stream()
                .filter(p -> p.getStatus() != StatusPedido.FINALIZADO
                        && p.getStatus() != StatusPedido.CANCELADO)
                .count();
    }

    public String getTicketMedio() {
        OptionalDouble avg = pedidos.stream()
                .filter(p -> p.getVeiculo() != null && p.getVeiculo().getPreco() != null)
                .mapToDouble(p -> p.getVeiculo().getPreco().doubleValue())
                .average();
        if (!avg.isPresent()) return "R$ 0,00";
        BigDecimal val = BigDecimal.valueOf(avg.getAsDouble()).setScale(2, RoundingMode.HALF_UP);
        return "R$ " + String.format(new Locale("pt", "BR"), "%,.2f", val);
    }

    public List<Pedido> getUltimosPedidos() {
        return pedidos.stream()
                .filter(p -> p.getDataPedido() != null)
                .sorted(Comparator.comparing(Pedido::getDataPedido).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public String getStatusLabelsJson() {
        Map<StatusPedido, Long> counts = pedidos.stream()
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (StatusPedido s : StatusPedido.values()) {
            if (counts.getOrDefault(s, 0L) > 0) {
                if (!first) sb.append(',');
                sb.append('\'').append(s.getDescricao().replace("'", "\\'")).append('\'');
                first = false;
            }
        }
        return sb.append(']').toString();
    }

    public String getStatusDataJson() {
        Map<StatusPedido, Long> counts = pedidos.stream()
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (StatusPedido s : StatusPedido.values()) {
            long cnt = counts.getOrDefault(s, 0L);
            if (cnt > 0) {
                if (!first) sb.append(',');
                sb.append(cnt);
                first = false;
            }
        }
        return sb.append(']').toString();
    }

    public String getMesLabelsJson() {
        String[] nomes = {"Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"};
        YearMonth now = YearMonth.now();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 11; i >= 0; i--) {
            if (i < 11) sb.append(',');
            YearMonth ym = now.minusMonths(i);
            int y = ym.getYear() % 100;
            sb.append('\'').append(nomes[ym.getMonthValue() - 1])
              .append('/').append(y < 10 ? "0" : "").append(y).append('\'');
        }
        return sb.append(']').toString();
    }

    public String getMesDataJson() {
        YearMonth now = YearMonth.now();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 11; i >= 0; i--) {
            if (i < 11) sb.append(',');
            YearMonth ym = now.minusMonths(i);
            long cnt = pedidos.stream()
                    .filter(p -> p.getDataPedido() != null
                            && YearMonth.from(p.getDataPedido()).equals(ym))
                    .count();
            sb.append(cnt);
        }
        return sb.append(']').toString();
    }

    public String getConcLabelsJson() {
        Map<String, Long> counts = pedidos.stream()
                .filter(p -> p.getConcessionaria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getConcessionaria().getNome(), Collectors.counting()));
        StringBuilder sb = new StringBuilder("[");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .forEach(e -> sb.append(sb.length() > 1 ? "," : "")
                        .append('\'').append(e.getKey().replace("'", "\\'")).append('\''));
        return sb.append(']').toString();
    }

    public String getConcDataJson() {
        Map<String, Long> counts = pedidos.stream()
                .filter(p -> p.getConcessionaria() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getConcessionaria().getNome(), Collectors.counting()));
        StringBuilder sb = new StringBuilder("[");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .forEach(e -> sb.append(sb.length() > 1 ? "," : "").append(e.getValue()));
        return sb.append(']').toString();
    }

    public String getTipoLabelsJson() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TipoVeiculo t : TipoVeiculo.values()) counts.put(t.getDescricao(), 0L);
        pedidos.stream()
                .filter(p -> p.getVeiculo() != null && p.getVeiculo().getTipo() != null)
                .forEach(p -> {
                    String k = p.getVeiculo().getTipo().getDescricao();
                    counts.put(k, counts.get(k) + 1);
                });
        StringBuilder sb = new StringBuilder("[");
        counts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .forEach(e -> sb.append(sb.length() > 1 ? "," : "")
                        .append('\'').append(e.getKey().replace("'", "\\'")).append('\''));
        return sb.append(']').toString();
    }

    public String getTipoDataJson() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TipoVeiculo t : TipoVeiculo.values()) counts.put(t.getDescricao(), 0L);
        pedidos.stream()
                .filter(p -> p.getVeiculo() != null && p.getVeiculo().getTipo() != null)
                .forEach(p -> {
                    String k = p.getVeiculo().getTipo().getDescricao();
                    counts.put(k, counts.get(k) + 1);
                });
        StringBuilder sb = new StringBuilder("[");
        counts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .forEach(e -> sb.append(sb.length() > 1 ? "," : "").append(e.getValue()));
        return sb.append(']').toString();
    }

    public String getVeicLabelsJson() {
        Map<String, Long> counts = pedidos.stream()
                .filter(p -> p.getVeiculo() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getVeiculo().getNome() + " " + p.getVeiculo().getModelo(),
                        Collectors.counting()));
        StringBuilder sb = new StringBuilder("[");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .forEach(e -> sb.append(sb.length() > 1 ? "," : "")
                        .append('\'').append(e.getKey().replace("'", "\\'")).append('\''));
        return sb.append(']').toString();
    }

    public String getVeicDataJson() {
        Map<String, Long> counts = pedidos.stream()
                .filter(p -> p.getVeiculo() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getVeiculo().getNome() + " " + p.getVeiculo().getModelo(),
                        Collectors.counting()));
        StringBuilder sb = new StringBuilder("[");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .forEach(e -> sb.append(sb.length() > 1 ? "," : "").append(e.getValue()));
        return sb.append(']').toString();
    }
    public String getPrecoTexto()  { return precoTexto; }
    public void   setPrecoTexto(String p) { this.precoTexto = p; }
    public String getAnoTexto()    { return anoTexto; }
    public void   setAnoTexto(String a)   { this.anoTexto = a; }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    // Getters e Setters
    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario getUsuarioEdicao() { return usuarioEdicao; }
    public void setUsuarioEdicao(Usuario u) { this.usuarioEdicao = u; }
    public String getSenhaUsuario() { return senhaUsuario; }
    public void setSenhaUsuario(String s) { this.senhaUsuario = s; }
    public PerfilUsuario getPerfilFiltro() { return perfilFiltro; }
    public void setPerfilFiltro(PerfilUsuario p) { this.perfilFiltro = p; }
    public List<Concessionaria> getConcessionarias() { return concessionarias; }
    public Concessionaria getConcessionariaEdicao() { return concessionariaEdicao; }
    public void setConcessionariaEdicao(Concessionaria c) { this.concessionariaEdicao = c; }
    public List<Veiculo> getVeiculos() { return veiculos; }
    public Veiculo getVeiculoEdicao() { return veiculoEdicao; }
    public void setVeiculoEdicao(Veiculo v) { this.veiculoEdicao = v; }
    public boolean isMostrarFormVeiculo() { return mostrarFormVeiculo; }
    public List<Pedido> getPedidos() { return pedidos; }
    public Long getConcessionariaIdSelecionada() { return concessionariaIdSelecionada; }
    public void setConcessionariaIdSelecionada(Long id) { this.concessionariaIdSelecionada = id; }

    public List<String> getOpcoesAnos()         { return opcoesAnos; }
    public List<String> getOpcoesMarcas()       { return opcoesMarcas; }
    public List<String> getOpcoesMotores()      { return opcoesMotores; }
    public List<String> getOpcoesCombustiveis() { return opcoesCombustiveis; }
    public List<String> getOpcoesTransmissoes() { return opcoesTransmissoes; }
    public List<String> getOpcoesTracoes()      { return opcoesTracoes; }
}
