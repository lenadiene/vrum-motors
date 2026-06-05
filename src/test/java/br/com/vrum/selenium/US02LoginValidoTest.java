package br.com.vrum.selenium;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * US-02 - Login com credenciais validas.
 *
 * Pre-requisitos:
 * - Aplicacao rodando em http://localhost:8080/vrum-motors
 * - Usuarios padrao criados pelo DataInicializador
 * - Google Chrome instalado
 */
public class US02LoginValidoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL = BASE_URL + "/login.xhtml";
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/vrum_motors"
            + "?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    private static final String EMAIL_CLIENTE = "cliente@email.com";
    private static final String SENHA_CLIENTE = "cliente123";
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";
    private static final String EMAIL_GERENTE = "gerente.recife@vrummotors.com";
    private static final String SENHA_GERENTE = "gerente123";
    private static final String EMAIL_FABRICA = "fabrica@vrummotors.com";
    private static final String SENHA_FABRICA = "fabrica123";
    private static final String EMAIL_ADMIN = "admin@vrummotors.com";
    private static final String SENHA_ADMIN = "admin123";

    @BeforeClass
    public static void setup() throws Exception {
        garantirUsuariosPadrao();
    }

    @Before
    public void abrirNavegadorLimpo() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1366,768");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @After
    public void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void us02_clienteValidoRedirecionaParaAreaCliente() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);

        validarRedirecionamento("/pages/cliente/");
        validarSessaoIniciada("Cliente");
    }

    @Test
    public void us02_vendedorValidoRedirecionaParaAreaVendedor() {
        fazerLogin(EMAIL_VENDEDOR, SENHA_VENDEDOR);

        validarRedirecionamento("/pages/vendedor/");
        validarSessaoIniciada("Vendedor");
    }

    @Test
    public void us02_gerenteValidoRedirecionaParaAreaGerente() {
        fazerLogin(EMAIL_GERENTE, SENHA_GERENTE);

        validarRedirecionamento("/pages/gerente/");
        validarSessaoIniciada("Gerente");
    }

    @Test
    public void us02_adminFabricaValidoRedirecionaParaAreaFabrica() {
        fazerLogin(EMAIL_FABRICA, SENHA_FABRICA);

        validarRedirecionamento("/pages/fabrica/");
        validarSessaoIniciada("Fabrica");
    }

    @Test
    public void us02_adminEmpresaValidoRedirecionaParaDashboardAdministrativo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);

        validarRedirecionamento("/pages/admin/dashboard");
        validarSessaoIniciada("Administrador");
    }

    private void fazerLogin(String email, String senha) {
        driver.get(LOGIN_URL);

        WebElement campoEmail = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));

        assertTrue("Campo de e-mail deve aceitar entrada", campoEmail.isEnabled());
        assertTrue("Campo de senha deve aceitar entrada", campoSenha.isEnabled());

        campoEmail.clear();
        campoEmail.sendKeys(email);
        aguardar(300);
        campoSenha.clear();
        campoSenha.sendKeys(senha);
        aguardar(300);

        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
        aguardar(1000);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.xhtml")));
    }

    private void validarRedirecionamento(String trechoEsperado) {
        wait.until(ExpectedConditions.urlContains(trechoEsperado));
        assertTrue("Login valido deve redirecionar para " + trechoEsperado,
                driver.getCurrentUrl().contains(trechoEsperado));
        assertFalse("Login valido nao deve permanecer na tela de login",
                driver.getCurrentUrl().contains("login.xhtml"));
    }

    private void validarSessaoIniciada(String perfilEsperado) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        if (!driver.findElements(By.cssSelector(".sidebar-user-name")).isEmpty()) {
            WebElement nomeUsuario = driver.findElement(By.cssSelector(".sidebar-user-name"));
            WebElement perfilUsuario = driver.findElement(By.cssSelector(".sidebar-user-role"));

            assertFalse("Nome do usuario logado deve estar preenchido",
                    nomeUsuario.getText().trim().isEmpty());
            assertTrue("Perfil da sessao deve aparecer na sidebar",
                    normalizar(perfilUsuario.getText()).contains(normalizar(perfilEsperado)));
            assertTrue("Sessao autenticada deve expor layout interno",
                    !driver.findElements(By.cssSelector(".app-layout, .sidebar")).isEmpty());
            return;
        }

        assertTrue("Sessao autenticada deve sair da tela de login",
                driver.findElements(By.id("loginForm")).isEmpty());
        assertFalse("Pagina autenticada nao deve estar vazia",
                driver.findElement(By.tagName("body")).getText().trim().isEmpty());
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.toLowerCase()
                .replace("á", "a")
                .replace("à", "a")
                .replace("ã", "a")
                .replace("â", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c");
    }

    private void aguardar(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    private static void garantirUsuariosPadrao() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                long concessionariaId = garantirConcessionariaRecife(conn);

                long adminId = upsertUsuario(conn, "Administrador Vrum", EMAIL_ADMIN, SENHA_ADMIN,
                        "81900000001", "ADMIN_EMPRESA");
                garantirLinhaPerfil(conn, "admin_empresa", adminId, null);

                long fabricaId = upsertUsuario(conn, "Carlos Fabrica", EMAIL_FABRICA, SENHA_FABRICA,
                        "81900000002", "ADMIN_FABRICA");
                garantirLinhaPerfil(conn, "admin_fabrica", fabricaId, null);

                long gerenteId = upsertUsuario(conn, "Roberto Gerente", EMAIL_GERENTE, SENHA_GERENTE,
                        "81911110001", "GERENTE");
                garantirLinhaPerfil(conn, "gerentes", gerenteId, concessionariaId);

                long vendedorId = upsertUsuario(conn, "Ana Vendedora", EMAIL_VENDEDOR, SENHA_VENDEDOR,
                        "81922220001", "VENDEDOR");
                garantirLinhaPerfil(conn, "vendedores", vendedorId, concessionariaId);

                long clienteId = upsertUsuario(conn, "Joao Cliente", EMAIL_CLIENTE, SENHA_CLIENTE,
                        "81933330001", "CLIENTE");
                garantirCliente(conn, clienteId);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static long garantirConcessionariaRecife(Connection conn) throws Exception {
        Long existente = buscarId(conn, "SELECT id FROM concessionarias WHERE LOWER(cidade) = LOWER(?)", "Recife");
        if (existente != null) {
            return existente;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO concessionarias (ativa, cidade, endereco, estado, nome, telefone) "
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

    private static long upsertUsuario(Connection conn, String nome, String email, String senha,
            String telefone, String perfil) throws Exception {
        Long existente = buscarId(conn, "SELECT id FROM usuarios WHERE email = ?", email);
        String senhaHash = hashSenha(senha);

        if (existente != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE usuarios SET nome = ?, senha = ?, telefone = ?, perfil = ?, ativo = true "
                            + "WHERE id = ?")) {
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
                "INSERT INTO usuarios (ativo, data_criacao, email, nome, senha, telefone, perfil) "
                        + "VALUES (true, NOW(), ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, nome);
            ps.setString(3, senhaHash);
            ps.setString(4, telefone);
            ps.setString(5, perfil);
            ps.executeUpdate();
            return lerIdGerado(ps);
        }
    }

    private static void garantirLinhaPerfil(Connection conn, String tabela, long usuarioId, Long concessionariaId)
            throws Exception {
        if (buscarId(conn, "SELECT id FROM " + tabela + " WHERE id = ?", usuarioId) != null) {
            if (concessionariaId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE " + tabela + " SET concessionaria_id = ? WHERE id = ?")) {
                    ps.setLong(1, concessionariaId);
                    ps.setLong(2, usuarioId);
                    ps.executeUpdate();
                }
            }
            return;
        }

        String sql = concessionariaId == null
                ? "INSERT INTO " + tabela + " (id) VALUES (?)"
                : "INSERT INTO " + tabela + " (id, concessionaria_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            if (concessionariaId != null) {
                ps.setLong(2, concessionariaId);
            }
            ps.executeUpdate();
        }
    }

    private static void garantirCliente(Connection conn, long usuarioId) throws Exception {
        if (buscarId(conn, "SELECT id FROM clientes WHERE id = ?", usuarioId) != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE clientes SET cidade = ?, estado = ? WHERE id = ?")) {
                ps.setString(1, "Recife");
                ps.setString(2, "PE");
                ps.setLong(3, usuarioId);
                ps.executeUpdate();
            }
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO clientes (id, cidade, estado) VALUES (?, ?, ?)")) {
            ps.setLong(1, usuarioId);
            ps.setString(2, "Recife");
            ps.setString(3, "PE");
            ps.executeUpdate();
        }
    }

    private static Long buscarId(Connection conn, String sql, Object parametro) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parametro instanceof Long) {
                ps.setLong(1, (Long) parametro);
            } else {
                ps.setString(1, String.valueOf(parametro));
            }

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
            throw new IllegalStateException("Nenhum ID gerado pelo banco.");
        }
    }

    private static String hashSenha(String senha) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(senha.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String value = Integer.toHexString(0xff & b);
            if (value.length() == 1) {
                hex.append('0');
            }
            hex.append(value);
        }
        return hex.toString();
    }
}
