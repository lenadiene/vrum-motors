package br.com.vrum.selenium;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

final class SeleniumFixtureSupport {

    static final String DB_URL = "jdbc:mysql://localhost:3306/vrum_motors"
            + "?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    static final String DB_USER = "root";
    static final String DB_PASS = "root";

    private SeleniumFixtureSupport() {
    }

    static Connection abrirConexao() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    static long garantirConcessionariaRecife(Connection conn) throws Exception {
        Long existente = buscarId(conn, "SELECT ID FROM concessionarias WHERE LOWER(CIDADE) = LOWER(?)", "Recife");
        if (existente != null) {
            return existente;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO concessionarias (ATIVA, CIDADE, ENDERECO, ESTADO, NOME, TELEFONE) "
                        + "VALUES (true, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Recife");
            ps.setString(2, "Av. Boa Viagem, 1000");
            ps.setString(3, "PE");
            ps.setString(4, "Vrum Motors Recife");
            ps.setString(5, "8132000001");
            ps.executeUpdate();
            return lerIdGerado(ps);
        }
    }

    static long garantirUsuario(Connection conn, String nome, String email, String senha,
            String telefone, String perfil) throws Exception {
        Long existente = buscarId(conn, "SELECT ID FROM usuarios WHERE EMAIL = ?", email);
        String senhaHash = hashSenha(senha);

        if (existente != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE usuarios SET NOME = ?, SENHA = ?, TELEFONE = ?, perfil = ?, ATIVO = true WHERE ID = ?")) {
                ps.setString(1, nome);
                ps.setString(2, senhaHash);
                ps.setString(3, telefone);
                ps.setString(4, perfil);
                ps.setLong(5, existente);
                ps.executeUpdate();
            }
            return existente;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO usuarios (NOME, EMAIL, SENHA, TELEFONE, perfil, ATIVO, data_criacao) "
                        + "VALUES (?, ?, ?, ?, ?, true, NOW())",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, senhaHash);
            ps.setString(4, telefone);
            ps.setString(5, perfil);
            ps.executeUpdate();
            return lerIdGerado(ps);
        }
    }

    static void garantirAdminFabrica(Connection conn, long usuarioId) throws Exception {
        garantirLinhaPerfil(conn, "admin_fabrica", usuarioId, null);
    }

    static void garantirVendedor(Connection conn, long usuarioId, long concessionariaId) throws Exception {
        garantirLinhaPerfil(conn, "vendedores", usuarioId, concessionariaId);
    }

    static void garantirCliente(Connection conn, long usuarioId) throws Exception {
        if (buscarId(conn, "SELECT ID FROM clientes WHERE ID = ?", usuarioId) != null) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO clientes (ID, CIDADE, ESTADO, CEP, ENDERECO) VALUES (?, ?, ?, ?, ?)")) {
            ps.setLong(1, usuarioId);
            ps.setString(2, "Recife");
            ps.setString(3, "PE");
            ps.setString(4, "50000-000");
            ps.setString(5, "Rua Selenium, 123");
            ps.executeUpdate();
        }
    }

    private static void garantirLinhaPerfil(Connection conn, String tabela, long usuarioId, Long concessionariaId)
            throws Exception {
        if (buscarId(conn, "SELECT ID FROM " + tabela + " WHERE ID = ?", usuarioId) != null) {
            if (concessionariaId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + tabela + " SET concessionaria_id = ? WHERE ID = ?")) {
                    ps.setLong(1, concessionariaId);
                    ps.setLong(2, usuarioId);
                    ps.executeUpdate();
                }
            }
            return;
        }

        if (concessionariaId == null) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tabela + " (ID) VALUES (?)")) {
                ps.setLong(1, usuarioId);
                ps.executeUpdate();
            }
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + tabela + " (ID, concessionaria_id) VALUES (?, ?)")) {
            ps.setLong(1, usuarioId);
            ps.setLong(2, concessionariaId);
            ps.executeUpdate();
        }
    }

    static long criarVeiculo(Connection conn, String nome, String modelo, String tipo, BigDecimal preco)
            throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO veiculos (NOME, MARCA, MODELO, ANO, PRECO, MOTOR, POTENCIA, TORQUE, "
                        + "TRANSMISSAO, COMBUSTIVEL, TRACAO, CONSUMO, VELOCIDADEMAX, ACELERACAO, COR, "
                        + "DESCRICAO, descricao_longa, TIPO, IMAGEMPRINCIPAL, DISPONIVEL, destaque_home) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true, true)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, "Selenium");
            ps.setString(3, modelo);
            ps.setInt(4, 2026);
            ps.setBigDecimal(5, preco);
            ps.setString(6, "2.0 Test");
            ps.setString(7, "200 cv");
            ps.setString(8, "320 Nm");
            ps.setString(9, "Automatico");
            ps.setString(10, "Flex");
            ps.setString(11, "AWD");
            ps.setString(12, "12 km/l");
            ps.setString(13, "220 km/h");
            ps.setString(14, "7.5");
            ps.setString(15, "Prata");
            ps.setString(16, "Veiculo de fixture Selenium para testes de catalogo.");
            ps.setString(17, "Descricao longa do veiculo Selenium usado em testes automatizados.");
            ps.setString(18, tipo);
            ps.setString(19, "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=800&q=80");
            ps.executeUpdate();
            return lerIdGerado(ps);
        }
    }

    static void criarPedido(Connection conn, String numero, long clienteId, long veiculoId,
            long concessionariaId, Long vendedorId, String status) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO pedidos (NUMEROPEDIDO, cliente_id, veiculo_id, concessionaria_id, vendedor_id, "
                        + "STATUS, data_pedido, data_atualizacao, prazo_fabricacao, forma_pagamento, cor_escolhida) "
                        + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), ?, ?)")) {
            ps.setString(1, numero);
            ps.setLong(2, clienteId);
            ps.setLong(3, veiculoId);
            ps.setLong(4, concessionariaId);
            if (vendedorId == null) {
                ps.setNull(5, java.sql.Types.BIGINT);
            } else {
                ps.setLong(5, vendedorId);
            }
            ps.setString(6, status);
            ps.setString(7, "Pix");
            ps.setString(8, "Prata");
            ps.executeUpdate();
        }
    }

    static void removerPedidosPorPrefixo(Connection conn, String prefixo) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pedidos WHERE NUMEROPEDIDO LIKE ?")) {
            ps.setString(1, prefixo + "%");
            ps.executeUpdate();
        }
    }

    static void removerVeiculosPorPrefixo(Connection conn, String prefixo) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM veiculos WHERE NOME LIKE ?")) {
            ps.setString(1, prefixo + "%");
            ps.executeUpdate();
        }
    }

    static String buscarString(Connection conn, String sql, Object valor) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    static Date buscarDate(Connection conn, String sql, Object valor) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDate(1) : null;
            }
        }
    }

    static Long buscarId(Connection conn, String sql, Object valor) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static long lerIdGerado(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Nao foi possivel ler o id gerado.");
    }

    private static String hashSenha(String senha) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(senha.getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
