package br.com.vrum.selenium;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.Normalizer;
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
 * US-03 - Feedback de login invalido.
 *
 * Pre-requisitos:
 * - Aplicacao rodando em http://localhost:8080/vrum-motors
 * - Banco MySQL acessivel com as credenciais locais padrao
 * - Google Chrome instalado
 */
public class US03LoginInvalidoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL = BASE_URL + "/login.xhtml";
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/vrum_motors"
            + "?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    private static final String EMAIL_VALIDO = "cliente@email.com";
    private static final String SENHA_VALIDA = "cliente123";

    @BeforeClass
    public static void setup() throws Exception {
        garantirClientePadrao();
    }

    @Before
    public void abrirNavegadorLimpo() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
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
    public void us03_emailInexistenteExibeErroEPermaneceNoLogin() {
        String emailInexistente = "nao-existe-us03@vrummotors.com";
        String senhaInformada = "senhaerrada";

        submeterLogin(emailInexistente, senhaInformada);

        validarPermaneceNoLogin();
        validarMensagemErroGenerica(emailInexistente, senhaInformada);
    }

    @Test
    public void us03_senhaIncorretaExibeErroEPermaneceNoLogin() {
        String senhaIncorreta = "senha-incorreta-us03";

        submeterLogin(EMAIL_VALIDO, senhaIncorreta);

        validarPermaneceNoLogin();
        validarMensagemErroGenerica(EMAIL_VALIDO, senhaIncorreta);
    }

    private void submeterLogin(String email, String senha) {
        driver.get(LOGIN_URL);

        WebElement campoEmail = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));

        campoEmail.clear();
        campoEmail.sendKeys(email);
        campoSenha.clear();
        campoSenha.sendKeys(senha);

        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
    }

    private void validarPermaneceNoLogin() {
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Falha de autenticacao deve manter URL na tela de login",
                driver.getCurrentUrl().contains("login"));
        assertFalse("Falha de autenticacao nao deve redirecionar para area interna",
                driver.getCurrentUrl().contains("/pages/"));
    }

    private void validarMensagemErroGenerica(String emailInformado, String senhaInformada) {
        WebElement mensagem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-error, .ui-messages-error, .messages")));

        String textoErro = mensagem.getText().trim();
        String textoNormalizado = normalizar(textoErro);

        assertFalse("Mensagem de erro deve estar preenchida", textoErro.isEmpty());
        assertTrue("Mensagem deve indicar feedback de erro",
                textoNormalizado.contains("login")
                        || textoNormalizado.contains("credenciais")
                        || textoNormalizado.contains("senha")
                        || textoNormalizado.contains("invalido")
                        || textoNormalizado.contains("erro"));

        assertFalse("Mensagem nao deve revelar o e-mail informado",
                textoErro.contains(emailInformado));
        assertFalse("Mensagem nao deve revelar a senha informada",
                textoErro.contains(senhaInformada));
        assertFalse("Mensagem nao deve revelar detalhes de banco",
                textoNormalizado.contains("select")
                        || textoNormalizado.contains("where")
                        || textoNormalizado.contains("usuarios")
                        || textoNormalizado.contains("hash"));
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private static void garantirClientePadrao() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                long clienteId = upsertUsuario(conn, "Joao Cliente", EMAIL_VALIDO, SENHA_VALIDA,
                        "81933330001", "CLIENTE");
                garantirCliente(conn, clienteId);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
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
                "INSERT INTO usuarios (nome, email, senha, telefone, perfil, ativo) VALUES (?, ?, ?, ?, ?, true)",
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

    private static void garantirCliente(Connection conn, long usuarioId) throws Exception {
        if (buscarId(conn, "SELECT id FROM clientes WHERE id = ?", usuarioId) != null) {
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO clientes (id, cpf, dataNascimento, endereco) VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, usuarioId);
            ps.setString(2, "00011122233");
            ps.setDate(3, java.sql.Date.valueOf("1990-01-01"));
            ps.setString(4, "Rua do Cliente, 123");
            ps.executeUpdate();
        }
    }

    private static Long buscarId(Connection conn, String sql, Object valor) throws Exception {
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
