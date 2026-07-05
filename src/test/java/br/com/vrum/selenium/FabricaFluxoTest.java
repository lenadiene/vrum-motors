package br.com.vrum.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.Duration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Fluxo Fabrica - Ordens de fabricacao e atualizacao de status.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FabricaFluxoTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    private static final String BASE_URL = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL = BASE_URL + "/login.xhtml";
    private static final String FABRICA_URL = BASE_URL + "/pages/fabrica/pedidos.xhtml";

    private static final String EMAIL_FABRICA = "fabrica@vrummotors.com";
    private static final String SENHA_FABRICA = "fabrica123";
    private static final String EMAIL_CLIENTE = "cliente@email.com";
    private static final String SENHA_CLIENTE = "cliente123";
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";

    private static final String PREFIXO_PEDIDO = "VRMFAB";
    private static final String PREFIXO_VEICULO = "Selenium Fabrica";
    private static final String PEDIDO_VISIVEL = PREFIXO_PEDIDO + "_LIST";
    private static final String PEDIDO_OPCOES = PREFIXO_PEDIDO + "_OPT";
    private static final String PEDIDO_ATUALIZAR = PREFIXO_PEDIDO + "_UPD";
    private static final String PEDIDO_FINALIZADO = PREFIXO_PEDIDO + "_DONE";
    private static final String OBSERVACAO_ATUALIZACAO = "Observacao Selenium fabrica atualizada";

    @BeforeClass
    public static void setup() throws Exception {
        prepararFixturesFabrica();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        limparFixturesFabrica();
    }

    @Before
    public void abrirNavegadorLimpo() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1366,768");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @After
    public void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void tc_FAB01_acessoDiretoSemLoginRedirecionaLogin() {
        driver.get(FABRICA_URL);

        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Area de fabrica deve exigir autenticacao",
                driver.getCurrentUrl().contains("login"));
        assertTrue("Tela de login deve ser exibida",
                driver.findElement(By.id("loginForm")).isDisplayed());
    }

    @Test
    public void tc_FAB02_adminFabricaVisualizaOrdensPermitidas() {
        fazerLoginFabrica();

        String textoPagina = driver.findElement(By.tagName("body")).getText();
        assertTrue("Pagina deve exibir titulo de ordens de fabricacao",
                normalizar(textoPagina).contains("ordens de fabricacao"));
        assertTrue("Pedido em fabricacao deve aparecer na fila",
                textoPagina.contains(PEDIDO_VISIVEL));
        assertTrue("Pedido fabricado deve aparecer na fila",
                textoPagina.contains(PEDIDO_OPCOES));
        assertFalse("Pedido finalizado nao deve aparecer na fila de fabrica",
                textoPagina.contains(PEDIDO_FINALIZADO));
    }

    @Test
    public void tc_FAB03_selecionarPedidoExibePainelComStatusPermitido() {
        fazerLoginFabrica();

        selecionarPedido(PEDIDO_OPCOES);

        WebElement painel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card-header') and contains(.,'Atualizar')]")));
        assertTrue("Painel de atualizacao deve aparecer", painel.isDisplayed());
        assertTrue("Painel deve mostrar o numero do pedido selecionado",
                driver.findElement(By.tagName("body")).getText().contains(PEDIDO_OPCOES));

        Select status = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("fabricaForm:selectStatus"))));
        assertTrue("Pedido FABRICADO deve permitir envio para cidade",
                status.getOptions().stream().anyMatch(o -> "ENVIADO_CIDADE".equals(o.getAttribute("value"))));
        assertFalse("Fabrica nao deve permitir finalizar pedido diretamente",
                status.getOptions().stream().anyMatch(o -> "FINALIZADO".equals(o.getAttribute("value"))));
    }

    @Test
    public void tc_FAB04_atualizarStatusRegistraPrazoObservacaoESucesso() throws Exception {
        fazerLoginFabrica();

        selecionarPedido(PEDIDO_ATUALIZAR);

        Select status = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("fabricaForm:selectStatus"))));
        status.selectByValue("FABRICADO");

        WebElement prazo = driver.findElement(By.id("fabricaForm:prazoEntrega"));
        prazo.clear();
        prazo.sendKeys("20/12/2026");
        js.executeScript(
                "arguments[0].value='20/12/2026';"
                        + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                prazo);
        assertEquals("20/12/2026", prazo.getAttribute("value"));

        WebElement observacoes = driver.findElement(By.id("fabricaForm:observacoes"));
        observacoes.clear();
        observacoes.sendKeys(OBSERVACAO_ATUALIZACAO);
        js.executeScript(
                "arguments[0].value=arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                observacoes, OBSERVACAO_ATUALIZACAO);
        assertEquals(OBSERVACAO_ATUALIZACAO, observacoes.getAttribute("value"));

        WebElement salvar = driver.findElement(By.id("fabricaForm:btnSalvarStatus"));
        js.executeScript("arguments[0].click();", salvar);

        WebElement sucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Atualizacao de fabricacao deve exibir sucesso", sucesso.isDisplayed());

        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            assertEquals("FABRICADO", SeleniumFixtureSupport.buscarString(conn,
                    "SELECT STATUS FROM pedidos WHERE NUMEROPEDIDO = ?", PEDIDO_ATUALIZAR));
            Date prazoBanco = SeleniumFixtureSupport.buscarDate(conn,
                    "SELECT prazo_entrega FROM pedidos WHERE NUMEROPEDIDO = ?", PEDIDO_ATUALIZAR);
            assertNotNull("Prazo de entrega deve ser persistido", prazoBanco);
            assertEquals("2026-12-20", prazoBanco.toLocalDate().toString());
            assertEquals(OBSERVACAO_ATUALIZACAO, SeleniumFixtureSupport.buscarString(conn,
                    "SELECT observacoes_fabrica FROM pedidos WHERE NUMEROPEDIDO = ?", PEDIDO_ATUALIZAR));
        }
    }

    private void fazerLoginFabrica() {
        driver.get(LOGIN_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement senha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        email.clear();
        email.sendKeys(EMAIL_FABRICA);
        senha.clear();
        senha.sendKeys(SENHA_FABRICA);
        driver.findElement(By.cssSelector("#loginForm input[type='submit'], #loginForm button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/pages/fabrica/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fabricaForm")));
    }

    private void selecionarPedido(String numeroPedido) {
        WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//*[contains(normalize-space(.),'" + numeroPedido + "')]/following::input[contains(@value,'Atualizar Status')][1]")));
        js.executeScript("arguments[0].click();", botao);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fabricaForm:selectStatus")));
    }

    private String normalizar(String texto) {
        return texto == null ? "" : java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

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

                long veiculoLista = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Lista",
                        "FAB-L", "DISPONIVEL", new BigDecimal("130000.00"));
                long veiculoOpcoes = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Opcoes",
                        "FAB-O", "DISPONIVEL", new BigDecimal("140000.00"));
                long veiculoAtualizar = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Atualizar",
                        "FAB-A", "DISPONIVEL", new BigDecimal("150000.00"));
                long veiculoFinalizado = SeleniumFixtureSupport.criarVeiculo(conn, PREFIXO_VEICULO + " Finalizado",
                        "FAB-F", "DISPONIVEL", new BigDecimal("160000.00"));

                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_VISIVEL, clienteId, veiculoLista,
                        concessionariaId, vendedorId, "EM_FABRICACAO");
                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_OPCOES, clienteId, veiculoOpcoes,
                        concessionariaId, vendedorId, "FABRICADO");
                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_ATUALIZAR, clienteId, veiculoAtualizar,
                        concessionariaId, vendedorId, "EM_FABRICACAO");
                SeleniumFixtureSupport.criarPedido(conn, PEDIDO_FINALIZADO, clienteId, veiculoFinalizado,
                        concessionariaId, vendedorId, "FINALIZADO");

                conn.commit();
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
        }
    }
}
