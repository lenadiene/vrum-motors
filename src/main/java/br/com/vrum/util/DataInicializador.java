package br.com.vrum.util;

import br.com.vrum.dao.*;
import br.com.vrum.model.*;
import br.com.vrum.service.ConfiguracaoVeiculoService;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.beans.Introspector;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Roda ao subir a aplicação e insere dados iniciais se o banco estiver vazio.
 */
@WebListener
public class DataInicializador implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Introspector.flushCaches();
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            ConcessionariaDAO concDAO = new ConcessionariaDAO();
            VeiculoDAO veiculoDAO = new VeiculoDAO();
            PedidoDAO pedidoDAO = new PedidoDAO();

            System.out.println(">>> Vrum Motors: garantindo dados de exemplo...");

            // ---- Admin Empresa ----
            if (usuarioDAO.buscarPorEmail("admin@vrummotors.com") == null) {
                AdminEmpresa admin = new AdminEmpresa();
                admin.setNome("Administrador Vrum");
                admin.setEmail("admin@vrummotors.com");
                admin.setSenha(SenhaUtil.hashSenha("admin123"));
                admin.setTelefone("81900000001");
                usuarioDAO.salvar(admin);
            }

            // ---- Admin Fábrica ----
            if (usuarioDAO.buscarPorEmail("fabrica@vrummotors.com") == null) {
                AdminFabrica fabrica = new AdminFabrica();
                fabrica.setNome("Carlos Fábrica");
                fabrica.setEmail("fabrica@vrummotors.com");
                fabrica.setSenha(SenhaUtil.hashSenha("fabrica123"));
                fabrica.setTelefone("81900000002");
                usuarioDAO.salvar(fabrica);
            }

            // ---- Concessionárias ----
            Concessionaria recife = concDAO.buscarPorCidade("Recife");
            if (recife == null) {
                recife = new Concessionaria();
                recife.setNome("Vrum Motors Recife");
                recife.setCidade("Recife");
                recife.setEstado("PE");
                recife.setEndereco("Av. Boa Viagem, 1000");
                recife.setTelefone("8132000001");
                concDAO.salvar(recife);
            }

            Concessionaria sp = concDAO.buscarPorCidade("São Paulo");
            if (sp == null) {
                sp = new Concessionaria();
                sp.setNome("Vrum Motors São Paulo");
                sp.setCidade("São Paulo");
                sp.setEstado("SP");
                sp.setEndereco("Av. Paulista, 2000");
                sp.setTelefone("1132000001");
                concDAO.salvar(sp);
            }

            // ---- Gerente Recife ----
            if (usuarioDAO.buscarPorEmail("gerente.recife@vrummotors.com") == null) {
                Gerente gerente = new Gerente();
                gerente.setNome("Roberto Gerente");
                gerente.setEmail("gerente.recife@vrummotors.com");
                gerente.setSenha(SenhaUtil.hashSenha("gerente123"));
                gerente.setTelefone("81911110001");
                gerente.setConcessionaria(recife);
                usuarioDAO.salvar(gerente);
            }

            // ---- Vendedor Recife ----
            if (usuarioDAO.buscarPorEmail("vendedor@vrummotors.com") == null) {
                Vendedor vendedor = new Vendedor();
                vendedor.setNome("Ana Vendedora");
                vendedor.setEmail("vendedor@vrummotors.com");
                vendedor.setSenha(SenhaUtil.hashSenha("vendedor123"));
                vendedor.setTelefone("81922220001");
                vendedor.setConcessionaria(recife);
                usuarioDAO.salvar(vendedor);
            }

            // ---- Cliente exemplo ----
            if (usuarioDAO.buscarPorEmail("cliente@email.com") == null) {
                Cliente cliente = new Cliente();
                cliente.setNome("João Cliente");
                cliente.setEmail("cliente@email.com");
                cliente.setSenha(SenhaUtil.hashSenha("cliente123"));
                cliente.setTelefone("81933330001");
                cliente.setCidade("Recife");
                cliente.setEstado("PE");
                usuarioDAO.salvar(cliente);
            }

            // ---- Veículos ----
            if (veiculoDAO.listarTodos().isEmpty()) {
                criarVeiculos(veiculoDAO);
            }

            // ---- Pedidos de demonstração para popular os dashboards ----
            if (pedidoDAO.listarTodos().isEmpty()) {
                criarPedidosExemplo(pedidoDAO, usuarioDAO, veiculoDAO, recife);
            }

            // ---- Configurações de seletores ----
            ConfiguracaoVeiculoService cfgService = new ConfiguracaoVeiculoService();
            if (!cfgService.existeAlgum()) {
                cfgService.popularInicial();
            }

            System.out.println(">>> Dados iniciais inseridos com sucesso!");
            System.out.println(">>> Logins: admin@vrummotors.com / admin123");
            System.out.println(">>> Logins: fabrica@vrummotors.com / fabrica123");
            System.out.println(">>> Logins: gerente.recife@vrummotors.com / gerente123");
            System.out.println(">>> Logins: vendedor@vrummotors.com / vendedor123");
            System.out.println(">>> Logins: cliente@email.com / cliente123");

        } catch (Exception e) {
            System.err.println("Erro ao inicializar dados: " + e.getMessage());
        }
    }

    private void criarVeiculos(VeiculoDAO dao) {
        // Veículo 1 - Destaque
        Veiculo v1 = new Veiculo();
        v1.setNome("Vrum GT-S");
        v1.setMarca("Vrum");
        v1.setModelo("GT-S");
        v1.setAno(2025);
        v1.setPreco(new BigDecimal("189900.00"));
        v1.setMotor("2.0 Turbo");
        v1.setPotencia("265 cv");
        v1.setTorque("380 Nm");
        v1.setTransmissao("Automático 8 marchas");
        v1.setCombustivel("Flex");
        v1.setTracao("4WD");
        v1.setConsumo("10 km/l urbano");
        v1.setVelocidadeMax("240 km/h");
        v1.setAceleracao("0-100 em 6,5s");
        v1.setDescricao("O esportivo perfeito para quem exige performance e elegância.");
        v1.setDescricaoLonga("O Vrum GT-S redefine o conceito de esportivo nacional. Com motor 2.0 Turbo de 265cv, suspensão esportiva adaptativa e interior em couro premium, ele entrega emoção em cada curva. Disponível nas versões Coupe e Fastback.");
        v1.setTipo(TipoVeiculo.DISPONIVEL);
        v1.setDestaqueHome(true);
        v1.setImagemPrincipal("gts-principal.jpg");
        dao.salvar(v1);

        // Veículo 2 - Lançamento
        Veiculo v2 = new Veiculo();
        v2.setNome("Vrum EV-X");
        v2.setMarca("Vrum");
        v2.setModelo("EV-X");
        v2.setAno(2026);
        v2.setPreco(new BigDecimal("245000.00"));
        v2.setMotor("Elétrico Dual Motor");
        v2.setPotencia("408 cv");
        v2.setTorque("660 Nm");
        v2.setTransmissao("Automático CVT");
        v2.setCombustivel("Elétrico");
        v2.setTracao("AWD");
        v2.setConsumo("4,2 km/kWh");
        v2.setVelocidadeMax("250 km/h");
        v2.setAceleracao("0-100 em 3,9s");
        v2.setDescricao("O futuro chegou. Elétrico, poderoso, silencioso.");
        v2.setDescricaoLonga("O Vrum EV-X é o SUV elétrico mais esperado do Brasil. Autonomia de 520km, carregamento rápido de 150kW e tecnologia de condução semi-autônoma de nível 2. O futuro da mobilidade na sua garagem.");
        v2.setTipo(TipoVeiculo.LANCAMENTO);
        v2.setDestaqueHome(true);
        v2.setImagemPrincipal("evx-principal.jpg");
        dao.salvar(v2);

        // Veículo 3
        Veiculo v3 = new Veiculo();
        v3.setNome("Vrum Urban");
        v3.setMarca("Vrum");
        v3.setModelo("Urban");
        v3.setAno(2025);
        v3.setPreco(new BigDecimal("98500.00"));
        v3.setMotor("1.0 Turbo");
        v3.setPotencia("130 cv");
        v3.setTorque("200 Nm");
        v3.setTransmissao("Automático 6 marchas");
        v3.setCombustivel("Flex");
        v3.setTracao("FWD");
        v3.setConsumo("13 km/l urbano");
        v3.setVelocidadeMax("185 km/h");
        v3.setAceleracao("0-100 em 9,8s");
        v3.setDescricao("Compacto e versátil. Ideal para a cidade.");
        v3.setDescricaoLonga("O Vrum Urban foi projetado para quem vive no ritmo acelerado da cidade. Compacto por fora, surpreendente por dentro. Com central multimídia 10\", câmera 360° e assistente de estacionamento.");
        v3.setTipo(TipoVeiculo.DISPONIVEL);
        v3.setDestaqueHome(false);
        v3.setImagemPrincipal("urban-principal.jpg");
        dao.salvar(v3);

        // Veículo 4 - Lançamento
        Veiculo v4 = new Veiculo();
        v4.setNome("Vrum Titan");
        v4.setMarca("Vrum");
        v4.setModelo("Titan");
        v4.setAno(2026);
        v4.setPreco(new BigDecimal("320000.00"));
        v4.setMotor("V8 5.0 Biturbo");
        v4.setPotencia("520 cv");
        v4.setTorque("700 Nm");
        v4.setTransmissao("Automático 10 marchas");
        v4.setCombustivel("Gasolina");
        v4.setTracao("4x4");
        v4.setConsumo("6 km/l urbano");
        v4.setVelocidadeMax("280 km/h");
        v4.setAceleracao("0-100 em 4,1s");
        v4.setDescricao("Uma máquina brutal. Poder e luxo sem compromisso.");
        v4.setDescricaoLonga("O Vrum Titan é a expressão máxima da engenharia automotiva nacional. V8 biturbo com 520cv, interior em couro Nappa, teto panorâmico e suspensão magnética ativa. Para quem não aceita menos que o melhor.");
        v4.setTipo(TipoVeiculo.LANCAMENTO);
        v4.setDestaqueHome(true);
        v4.setImagemPrincipal("titan-principal.jpg");
        dao.salvar(v4);

        // Veículo 5
        Veiculo v5 = new Veiculo();
        v5.setNome("Vrum Cross");
        v5.setMarca("Vrum");
        v5.setModelo("Cross");
        v5.setAno(2025);
        v5.setPreco(new BigDecimal("142900.00"));
        v5.setMotor("1.6 Turbo");
        v5.setPotencia("185 cv");
        v5.setTorque("280 Nm");
        v5.setTransmissao("CVT");
        v5.setCombustivel("Flex");
        v5.setTracao("AWD");
        v5.setConsumo("11 km/l urbano");
        v5.setVelocidadeMax("210 km/h");
        v5.setAceleracao("0-100 em 7,9s");
        v5.setDescricao("SUV aventureiro para todos os terrenos.");
        v5.setDescricaoLonga("O Vrum Cross foi construído para aventura sem abrir mão do conforto. Com 230mm de altura livre, proteção de cárter, sistema AWD inteligente e 7 lugares, ele vai onde você quiser ir.");
        v5.setTipo(TipoVeiculo.DISPONIVEL);
        v5.setDestaqueHome(true);
        v5.setImagemPrincipal("cross-principal.jpg");
        dao.salvar(v5);
    }

    private void criarPedidosExemplo(
            PedidoDAO pedidoDAO,
            UsuarioDAO usuarioDAO,
            VeiculoDAO veiculoDAO,
            Concessionaria concessionaria) {

        Usuario clienteUsuario = usuarioDAO.buscarPorEmail("cliente@email.com");
        Usuario vendedorUsuario = usuarioDAO.buscarPorEmail("vendedor@vrummotors.com");
        List<Veiculo> veiculos = veiculoDAO.listarTodos();

        if (!(clienteUsuario instanceof Cliente)
                || !(vendedorUsuario instanceof Vendedor)
                || veiculos.isEmpty()
                || concessionaria == null) {
            throw new IllegalStateException("Não foi possível preparar os pedidos de demonstração.");
        }

        Cliente cliente = (Cliente) clienteUsuario;
        Vendedor vendedor = (Vendedor) vendedorUsuario;
        LocalDateTime agora = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);

        criarPedidoExemplo(pedidoDAO, "DEMO-001", cliente, veiculos.get(0), concessionaria, vendedor,
                StatusPedido.FINALIZADO, agora.minusMonths(5), "Prata", "Financiamento", LocalDate.now().minusMonths(4));
        criarPedidoExemplo(pedidoDAO, "DEMO-002", cliente, veiculos.get(1 % veiculos.size()), concessionaria, vendedor,
                StatusPedido.CANCELADO, agora.minusMonths(4), "Branco", "Pix", null);
        criarPedidoExemplo(pedidoDAO, "DEMO-003", cliente, veiculos.get(2 % veiculos.size()), concessionaria, vendedor,
                StatusPedido.FINALIZADO, agora.minusMonths(3), "Cinza", "Financiamento", LocalDate.now().minusMonths(2));
        criarPedidoExemplo(pedidoDAO, "DEMO-004", cliente, veiculos.get(3 % veiculos.size()), concessionaria, vendedor,
                StatusPedido.PRONTO_ENTREGA, agora.minusMonths(2), "Preto", "Cartão", null);
        criarPedidoExemplo(pedidoDAO, "DEMO-005", cliente, veiculos.get(4 % veiculos.size()), concessionaria, vendedor,
                StatusPedido.ENVIADO_CIDADE, agora.minusMonths(1), "Vermelho", "Financiamento", null);
        criarPedidoExemplo(pedidoDAO, "DEMO-006", cliente, veiculos.get(0), concessionaria, vendedor,
                StatusPedido.EM_FABRICACAO, agora.minusDays(18), "Azul", "Pix", null);
        criarPedidoExemplo(pedidoDAO, "DEMO-007", cliente, veiculos.get(1 % veiculos.size()), concessionaria, vendedor,
                StatusPedido.EM_NEGOCIACAO, agora.minusDays(10), "Branco", null, null);
        criarPedidoExemplo(pedidoDAO, "DEMO-008", cliente, veiculos.get(2 % veiculos.size()), concessionaria, null,
                StatusPedido.AGUARDANDO_ATENDIMENTO, agora.minusDays(3), "Prata", null, null);

        System.out.println(">>> 8 pedidos de demonstração criados para a unidade Recife.");
    }

    private void criarPedidoExemplo(
            PedidoDAO pedidoDAO,
            String numero,
            Cliente cliente,
            Veiculo veiculo,
            Concessionaria concessionaria,
            Vendedor vendedor,
            StatusPedido status,
            LocalDateTime dataPedido,
            String cor,
            String formaPagamento,
            LocalDate dataRetirada) {

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(numero);
        pedido.setCliente(cliente);
        pedido.setVeiculo(veiculo);
        pedido.setConcessionaria(concessionaria);
        pedido.setVendedor(vendedor);
        pedido.setStatus(status);
        pedido.setDataPedido(dataPedido);
        pedido.setCorEscolhida(cor);
        pedido.setFormaPagamento(formaPagamento);
        pedido.setDataRetirada(dataRetirada);

        if (status == StatusPedido.AGUARDANDO_FABRICACAO
                || status == StatusPedido.EM_FABRICACAO
                || status == StatusPedido.ENVIADO_CIDADE
                || status == StatusPedido.PRONTO_ENTREGA) {
            pedido.setPrazoFabricacao(dataPedido.toLocalDate().plusDays(30));
        }
        if (status == StatusPedido.ENVIADO_CIDADE || status == StatusPedido.PRONTO_ENTREGA) {
            pedido.setPrazoEntrega(dataPedido.toLocalDate().plusDays(45));
        }

        pedidoDAO.salvar(pedido);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.closeFactory();
    }
}
