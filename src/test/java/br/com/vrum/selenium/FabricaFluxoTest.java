package br.com.vrum.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Fluxo Fábrica — Kanban de ordens de fabricação.
 *
 * Fixture:
 *  - PEDIDO_VISIVEL   → EM_FABRICACAO  (aparece na coluna "Em Fabricação")
 *  - PEDIDO_OPCOES    → FABRICADO      (aparece na coluna "Fabricado")
 *  - PEDIDO_ATUALIZAR → AGUARDANDO_FABRICACAO (avançado em FAB04)
 *  - PEDIDO_FINALIZADO→ FINALIZADO     (não deve aparecer)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FabricaFluxoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;

    private static final String BASE_URL    = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL   = BASE_URL + "/login.xhtml";
    private static final String FABRICA_URL = BASE_URL + "/pages/fabrica/pedidos.xhtml";

    private static final String EMAIL_FABRICA  = "fabrica@vrummotors.com";
    private static final String SENHA_FABRICA  = "fabrica123";
    private static final String EMAIL_CLIENTE  = "cliente@email.com";
    private static final String SENHA_CLIENTE  = "cliente123";
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";

    private static final String PREFIXO_PEDIDO   = "VRMFAB";
    private static final String PREFIXO_VEICULO  = "Selenium Fabrica";
    private static final String PEDIDO_VISIVEL    = PREFIXO_PEDIDO + "_LIST";
    private static final String PEDIDO_OPCOES     = PREFIXO_PEDIDO + "_OPT";
    private static final String PEDIDO_ATUALIZAR  = PREFIXO_PEDIDO + "_UPD";
    private static final String PEDIDO_FINALIZADO = PREFIXO_PEDIDO + "_DONE";

    @BeforeClass
    public static void setup() throws Exception {
        prepararFixturesFabrica();

        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        opts.addArguments("--no-first-run");
        opts.addArguments("--disable-background-networking");
        opts.addArguments("--disable-features=SafeBrowsing,PasswordLeakDetection,"
                + "SafeBrowsingEnhancedProtection,PasswordManager");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", false);
        opts.setExperimentalOption("prefs", prefs);
        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js   = (JavascriptExecutor) driver;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        limparFixturesFabrica();
        if (driver != null) driver.quit();
    }

    @After
    public void logoutAposCadaTeste() {
        aguardar(1000);
        try {
            WebElement btnSair = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')] | //button[contains(text(),'Sair')]")));
            btnSair.click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.urlContains("home")));
        } catch (Exception e) {
            // Sair não encontrado — sessão já encerrada ou não estava autenticada
        } finally {
            driver.manage().deleteAllCookies();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void fazerLoginFabrica() {
        driver.get(LOGIN_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement senha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        aguardar(300);
        email.click(); email.clear(); email.sendKeys(EMAIL_FABRICA);
        aguardar(300);
        senha.click(); senha.clear(); senha.sendKeys(SENHA_FABRICA);
        aguardar(500);
        driver.findElement(By.cssSelector("#loginForm input[type='submit'], #loginForm button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/pages/fabrica/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fabricaForm")));
    }

    private void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void jsClick(WebElement element) {
        js.executeScript("arguments[0].click();", element);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
    }

    // =========================================================================
    // TESTES
    // =========================================================================

    /** FAB01 — Acesso sem login redireciona para tela de login. */
    @Test
    public void tc_FAB01_acessoDiretoSemLoginRedirecionaLogin() {
        driver.get(FABRICA_URL);
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Área de fábrica deve exigir autenticação",
                driver.getCurrentUrl().contains("login"));
        assertTrue("Tela de login deve ser exibida",
                driver.findElement(By.id("loginForm")).isDisplayed());
        System.out.println("✅ FAB01 — Acesso sem login redirecionou para login corretamente");
    }

    /** FAB02 — Admin fábrica visualiza pedidos nas colunas corretas. */
    @Test
    public void tc_FAB02_adminFabricaVisualizaOrdensPermitidas() {
        fazerLoginFabrica();

        String texto = driver.findElement(By.tagName("body")).getText();
        assertTrue("Título da página deve mencionar fabricação",
                normalizar(texto).contains("fabricacao"));
        assertTrue("PEDIDO_VISIVEL (EM_FABRICACAO) deve aparecer",
                texto.contains(PEDIDO_VISIVEL));
        assertTrue("PEDIDO_OPCOES (FABRICADO) deve aparecer",
                texto.contains(PEDIDO_OPCOES));
        assertFalse("Pedido FINALIZADO não deve aparecer",
                texto.contains(PEDIDO_FINALIZADO));
        System.out.println("✅ FAB02 — Pedidos visíveis nas colunas corretas");
    }

    /** FAB03 — Kanban exibe as 4 colunas e pedidos nas etapas corretas. */
    @Test
    public void tc_FAB03_kanbanExibeColunasEPedidosCorretos() {
        fazerLoginFabrica();

        String texto = driver.findElement(By.tagName("body")).getText();
        String norm  = normalizar(texto);

        assertTrue("Coluna Aguardando Fabricação", norm.contains("aguardando fabricacao"));
        assertTrue("Coluna Em Fabricação",         norm.contains("em fabricacao"));
        assertTrue("Coluna Fabricado",             norm.contains("fabricado"));
        assertTrue("Coluna Enviado para Cidade",   norm.contains("enviado"));

        WebElement btnFabricado = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                "//div[@data-pedido='" + PEDIDO_VISIVEL + "']//input[@type='submit'][contains(@value,'Fabricado')] | " +
                "//div[@data-pedido='" + PEDIDO_VISIVEL + "']//button[contains(.,'Fabricado')]")));
        assertTrue("Card EM_FABRICACAO deve ter botão 'Fabricado'", btnFabricado.isDisplayed());

        WebElement btnEnviar = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                "//div[@data-pedido='" + PEDIDO_OPCOES + "']//input[@type='submit'][contains(@value,'Enviar')] | " +
                "//div[@data-pedido='" + PEDIDO_OPCOES + "']//button[contains(.,'Enviar')]")));
        assertTrue("Card FABRICADO deve ter botão 'Enviar para Cidade'", btnEnviar.isDisplayed());

        List<WebElement> btnsProibidos = driver.findElements(By.xpath(
                "//*[contains(@value,'Finalizar Pedido')] | //*[contains(.,'Finalizar Pedido')]"));
        assertTrue("Fábrica não deve exibir botão Finalizar Pedido", btnsProibidos.isEmpty());
        System.out.println("✅ FAB03 — Kanban com 4 colunas e botões corretos");
    }

    /** FAB04 — Avança PEDIDO_ATUALIZAR de AGUARDANDO_FABRICACAO → EM_FABRICACAO. */
    @Test
    public void tc_FAB04_avancaAguardandoParaEmFabricacao() {
        fazerLoginFabrica();

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[@data-pedido='" + PEDIDO_ATUALIZAR + "']//input[@type='submit'][contains(@value,'Em Fabrica')] | " +
                "//div[@data-pedido='" + PEDIDO_ATUALIZAR + "']//button[contains(.,'Em Fabrica')]")));
        jsClick(btn);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Deve exibir mensagem de sucesso", msg.isDisplayed());

        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            assertEquals("EM_FABRICACAO", SeleniumFixtureSupport.buscarString(conn,
                    "SELECT STATUS FROM pedidos WHERE NUMEROPEDIDO = ?", PEDIDO_ATUALIZAR));
        } catch (Exception e) {
            throw new AssertionError("Erro ao consultar banco: " + e.getMessage(), e);
        }
        System.out.println("✅ FAB04 — Pedido avançado para EM_FABRICACAO");
    }

    /** FAB05 — Avança PEDIDO_OPCOES de FABRICADO → ENVIADO_CIDADE com prazo. */
    @Test
    public void tc_FAB05_enviaFabricadoParaCidadeComPrazo() {
        fazerLoginFabrica();

        WebElement btnEnviar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[@data-pedido='" + PEDIDO_OPCOES + "']//input[@type='submit'][contains(@value,'Enviar')] | " +
                "//div[@data-pedido='" + PEDIDO_OPCOES + "']//button[contains(.,'Enviar')]")));
        jsClick(btnEnviar);

        WebElement inputPrazo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("fabricaForm:prazoUnitario")));
        assertTrue("Painel de prazo deve aparecer", inputPrazo.isDisplayed());

        js.executeScript("arguments[0].value='20/12/2026';" +
                "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", inputPrazo);

        WebElement btnConfirmar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[@type='submit'][contains(@value,'Confirmar Envio')] | " +
                "//button[contains(.,'Confirmar Envio')]")));
        jsClick(btnConfirmar);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Deve exibir mensagem de sucesso", msg.isDisplayed());

        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            assertEquals("ENVIADO_CIDADE", SeleniumFixtureSupport.buscarString(conn,
                    "SELECT STATUS FROM pedidos WHERE NUMEROPEDIDO = ?", PEDIDO_OPCOES));
            Date prazoBanco = SeleniumFixtureSupport.buscarDate(conn,
                    "SELECT prazo_entrega FROM pedidos WHERE NUMEROPEDIDO = ?", PEDIDO_OPCOES);
            assertNotNull("Prazo de entrega deve ser persistido", prazoBanco);
            assertEquals("2026-12-20", prazoBanco.toLocalDate().toString());
        } catch (Exception e) {
            throw new AssertionError("Erro ao consultar banco: " + e.getMessage(), e);
        }
        System.out.println("✅ FAB05 — Pedido enviado para cidade com prazo 20/12/2026");
    }

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private static void prepararFixturesFabrica() throws Exception {
        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            conn.setAutoCommit(false);
            try {
                SeleniumFixtureSupport.removerPedidosPorPrefixo(conn, PREFIXO_PEDIDO);
                SeleniumFixtureSupport.removerVeiculosPorPrefixo(conn, PREFIXO_VEICULO);

                long concessionariaId = SeleniumFixtureSupport.garantirConcessionariaRecife(conn);
                long fabricaId = SeleniumFixtureSupport.garantirUsuario(conn, "Carlos Fabrica", EMAIL_FABRICA,
                        SENHA_FABRICA, "81900000002", "ADMIN_FABRICA");
                SeleniumFixtureSupport.garantirAdminFabrica(conn, fabricaId);

                long clienteId = SeleniumFixtureSupport.garantirUsuario(conn, "Joao Cliente", EMAIL_CLIENTE,
                        SENHA_CLIENTE, "81933330001", "CLIENTE");
                SeleniumFixtureSupport.garantirCliente(conn, clienteId);

                long vendedorId = SeleniumFixtureSupport.garantirUsuario(conn, "Ana Vendedora", EMAIL_VENDEDOR,
                        SENHA_VENDEDOR, "81922220001", "VENDEDOR");
                SeleniumFixtureSupport.garantirVendedor(conn, vendedorId, concessionariaId);

                long veiculoLista      = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Lista",
                        "FAB-L", "DISPONIVEL", new BigDecimal("130000.00"));
                long veiculoOpcoes     = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Opcoes",
                        "FAB-O", "DISPONIVEL", new BigDecimal("140000.00"));
                long veiculoAtualizar  = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Atualizar",
                        "FAB-A", "DISPONIVEL", new BigDecimal("150000.00"));
                long veiculoFinalizado = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Finalizado",
                        "FAB-F", "DISPONIVEL", new BigDecimal("160000.00"));

                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_VISIVEL, clienteId, veiculoLista,
                        concessionariaId, vendedorId, "EM_FABRICACAO");
                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_OPCOES, clienteId, veiculoOpcoes,
                        concessionariaId, vendedorId, "FABRICADO");
                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_ATUALIZAR, clienteId, veiculoAtualizar,
                        concessionariaId, vendedorId, "AGUARDANDO_FABRICACAO");
                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_FINALIZADO, clienteId, veiculoFinalizado,
                        concessionariaId, vendedorId, "FINALIZADO");

                conn.commit();
                System.out.println("✅ Fixtures Fábrica criadas com sucesso.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void limparFixturesFabrica() throws Exception {
        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            SeleniumFixtureSupport.removerPedidosPorPrefixo(conn, PREFIXO_PEDIDO);
            SeleniumFixtureSupport.removerVeiculosPorPrefixo(conn, PREFIXO_VEICULO);
            System.out.println("✅ Fixtures Fábrica removidas.");
        }
    }
}
