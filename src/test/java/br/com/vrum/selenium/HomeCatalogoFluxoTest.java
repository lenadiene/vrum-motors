package br.com.vrum.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Duration;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
 * Fluxo Home - Catalogo, filtros e modal de detalhes.
 *
 * Complementa US01HomePublicaTest sem alterar a cobertura ja existente.
 */
public class HomeCatalogoFluxoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;

    private static final String BASE_URL = "http://localhost:8080/vrum-motors";
    private static final String HOME_URL = BASE_URL + "/home.xhtml";
    private static final String PREFIXO_VEICULO = "Selenium Home";
    private static final String VEICULO_BUSCA = PREFIXO_VEICULO + " Alpha";
    private static final String VEICULO_LANCAMENTO = PREFIXO_VEICULO + " Lancamento";

    @BeforeClass
    public static void setup() throws Exception {
        prepararFixturesHome();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1366,768");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (driver != null) {
            driver.quit();
        }
        limparFixturesHome();
    }

    @Before
    public void abrirHomeLimpa() {
        driver.manage().deleteAllCookies();
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.titleContains("Vrum Motors"));
        rolarParaCatalogo();
    }

    @After
    public void limparSessao() {
        driver.manage().deleteAllCookies();
    }

    @Test
    public void tc_HOME01_buscaPorNomeFiltraCatalogo() {
        buscarNoCatalogo(VEICULO_BUSCA);

        List<WebElement> cards = cardsCatalogo();
        assertEquals("Busca pelo nome unico deve retornar somente o veiculo fixture", 1, cards.size());
        assertTrue("Resultado deve exibir o veiculo buscado",
                texto(cards.get(0)).contains(VEICULO_BUSCA));
        assertFalse("Resultado filtrado nao deve exibir outro fixture",
                texto(cards.get(0)).contains(VEICULO_LANCAMENTO));
    }

    @Test
    public void tc_HOME02_buscaInexistenteExibeEstadoVazio() {
        buscarNoCatalogo("ZZZ_HOME_SEM_RESULTADO_987654");

        assertTrue("Busca inexistente nao deve exibir cards",
                driver.findElements(By.cssSelector(".catalog-section .vehicle-card")).isEmpty());
        assertTrue("Busca inexistente deve exibir mensagem de vazio",
                normalizar(driver.findElement(By.cssSelector(".catalog-section")).getText())
                        .contains("nenhum veiculo encontrado"));
    }

    @Test
    public void tc_HOME03_limparBuscaRestauraCatalogo() {
        buscarNoCatalogo("ZZZ_HOME_SEM_RESULTADO_987654");
        assertTrue("Pre-condicao: busca inexistente deve zerar cards",
                driver.findElements(By.cssSelector(".catalog-section .vehicle-card")).isEmpty());

        clicarBotaoCatalogo("Limpar");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".catalog-section .vehicle-card")));
        assertTrue("Limpar busca deve restaurar cards do catalogo", cardsCatalogo().size() > 0);
        assertTrue("Campo de busca deve voltar vazio",
                campoBusca().getAttribute("value") == null || campoBusca().getAttribute("value").isEmpty());
    }

    @Test
    public void tc_HOME04_detalhesDoVeiculoAbremModalComDados() {
        buscarNoCatalogo(VEICULO_BUSCA);
        WebElement card = cardsCatalogo().get(0);
        WebElement detalhes = card.findElement(By.xpath(".//button[contains(normalize-space(.),'Detalhes')]"));

        js.executeScript("arguments[0].click();", detalhes);

        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("modalVeiculo")));
        wait.until(d -> modal.getAttribute("class").contains("open"));

        assertEquals("Modal deve exibir o nome do veiculo selecionado",
                VEICULO_BUSCA, driver.findElement(By.id("modal-nome")).getText().trim());
        assertTrue("Modal deve exibir preco formatado",
                driver.findElement(By.id("modal-preco")).getText().contains("R$"));
        assertTrue("Modal deve exibir especificacoes do veiculo",
                normalizar(driver.findElement(By.id("modal-specs")).getText()).contains("selenium"));
    }

    private void buscarNoCatalogo(String termo) {
        WebElement campo = campoBusca();
        campo.clear();
        campo.sendKeys(termo);
        clicarBotaoCatalogo("Buscar");
        rolarParaCatalogo();
        aguardar(700);
    }

    private WebElement campoBusca() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[id$='campoBusca']")));
    }

    private void clicarBotaoCatalogo(String texto) {
        WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='" + texto + "'] | //button[normalize-space(.)='" + texto + "']")));
        js.executeScript("arguments[0].click();", botao);
    }

    private List<WebElement> cardsCatalogo() {
        return driver.findElements(By.cssSelector(".catalog-section .vehicle-card"));
    }

    private void rolarParaCatalogo() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#veiculos, .catalog-section")));
        js.executeScript("var el=document.querySelector('#veiculos, .catalog-section'); if(el) el.scrollIntoView();");
    }

    private String texto(WebElement element) {
        String textContent = element.getAttribute("textContent");
        return textContent == null ? "" : textContent.trim();
    }

    private String normalizar(String texto) {
        return texto == null ? "" : java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private void aguardar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private static void prepararFixturesHome() throws Exception {
        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            conn.setAutoCommit(false);
            try {
                SeleniumFixtureSupport.removerPedidosPorPrefixo(conn, "VRM_HOME_TEST");
                SeleniumFixtureSupport.removerVeiculosPorPrefixo(conn, PREFIXO_VEICULO);
                SeleniumFixtureSupport.criarVeiculo(conn, VEICULO_BUSCA, "HOM-A1",
                        "DISPONIVEL", new BigDecimal("111111.00"));
                SeleniumFixtureSupport.criarVeiculo(conn, VEICULO_LANCAMENTO, "HOM-L1",
                        "LANCAMENTO", new BigDecimal("222222.00"));
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static void limparFixturesHome() throws Exception {
        try (Connection conn = SeleniumFixtureSupport.abrirConexao()) {
            SeleniumFixtureSupport.removerPedidosPorPrefixo(conn, "VRM_HOME_TEST");
            SeleniumFixtureSupport.removerVeiculosPorPrefixo(conn, PREFIXO_VEICULO);
        }
    }
}
