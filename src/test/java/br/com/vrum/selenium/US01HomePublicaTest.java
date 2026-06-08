package br.com.vrum.selenium;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
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

import org.openqa.selenium.JavascriptExecutor;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * US-01 - Acesso a pagina inicial sem autenticacao.
 *
 * Pre-requisitos:
 * - Aplicacao rodando em http://localhost:8080/vrum-motors
 * - Banco populado pelo DataInicializador
 * - Google Chrome instalado
 */
public class US01HomePublicaTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080/vrum-motors";
    private static final String HOME_URL = BASE_URL + "/home.xhtml";

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1366,768");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Before
    public void limparSessaoAntesDoTeste() {
        try {
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {
        }
    }

    @After
    public void limparSessaoDepoisDoTeste() {
        try {
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void us01_homeRetornaHttp200ECarregaSemLogin() throws Exception {
        assertStatusHttpOk(HOME_URL);

        driver.get(HOME_URL);
        wait.until(ExpectedConditions.titleContains("Vrum Motors"));

        assertFalse("Home publica nao deve redirecionar visitante para login",
                driver.getCurrentUrl().contains("login"));
        assertTrue("Titulo deve conter Vrum Motors",
                driver.getTitle().contains("Vrum Motors"));
        assertTrue("Conteudo principal da home deve estar visivel",
                driver.findElement(By.cssSelector(".hero")).isDisplayed());
    }

    @Test
    public void us01_catalogoExibeNomePrecoEImagemDosVeiculos() {
        driver.get(HOME_URL);

        // Rola até o catálogo para disparar o IntersectionObserver (fade-up)
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('veiculos').scrollIntoView()");

        List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#veiculos .vehicle-card")));
        assertTrue("Catalogo deve exibir ao menos um veiculo", cards.size() > 0);

        for (WebElement card : cards) {
            WebElement nome = card.findElement(By.cssSelector(".vehicle-card-name"));
            WebElement preco = card.findElement(By.cssSelector(".vehicle-card-price"));
            WebElement imagemContainer = card.findElement(By.cssSelector(".vehicle-card-img"));
            List<WebElement> imagens = card.findElements(By.cssSelector(".vehicle-card-img img"));

            // textContent independe de opacidade (fade-up pode deixar opacity:0)
            assertFalse("Nome do veiculo nao deve estar vazio",
                    nome.getAttribute("textContent").trim().isEmpty());
            assertTrue("Preco do veiculo deve conter R$",
                    preco.getAttribute("textContent").contains("R$"));
            assertTrue("Area visual do veiculo deve estar no DOM",
                    imagemContainer.getAttribute("class").contains("vehicle-card-img"));

            if (!imagens.isEmpty()) {
                WebElement imagem = imagens.get(0);
                assertFalse("Imagem do veiculo deve ter origem definida",
                        imagem.getAttribute("src") == null || imagem.getAttribute("src").trim().isEmpty());
            } else {
                assertFalse("Area visual do veiculo deve ter conteudo quando nao houver tag img",
                        imagemContainer.getAttribute("textContent").trim().isEmpty());
            }
        }
    }

    @Test
    public void us01_campoBuscaEstaVisivelEAcessivelSemLogin() {
        driver.get(HOME_URL);

        // Rola até o catálogo: o campoBusca está dentro de .catalog-header.fade-up
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('veiculos').scrollIntoView()");

        WebElement campoBusca = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[id$='campoBusca']")));

        assertTrue("Campo de busca deve estar visivel", campoBusca.isDisplayed());
        assertTrue("Campo de busca deve estar habilitado", campoBusca.isEnabled());

        campoBusca.clear();
        campoBusca.sendKeys("Vrum");
        assertTrue("Campo de busca deve aceitar digitacao",
                "Vrum".equals(campoBusca.getAttribute("value")));
    }

    @Test
    public void us01_naoExibeBotoesOuAreasRestritasParaVisitante() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.titleContains("Vrum Motors"));

        String textoPagina = driver.findElement(By.tagName("body")).getText();
        assertFalse("Visitante nao deve ver link Minha Conta", textoPagina.contains("Minha Conta"));
        assertFalse("Visitante nao deve ver acao Sair", textoPagina.contains("Sair"));

        List<WebElement> sidebars = driver.findElements(By.cssSelector(".sidebar, .app-layout"));
        assertTrue("Home publica nao deve exibir layout interno/restrito", sidebars.isEmpty());

        List<WebElement> linksRestritos = driver.findElements(By.cssSelector(
                "a[href*='/pages/admin/'], a[href*='/pages/gerente/'], a[href*='/pages/vendedor/'], "
                        + "a[href*='/pages/fabrica/'], a[href*='/pages/cliente/']"));
        assertTrue("Visitante nao deve ver links para areas restritas", linksRestritos.isEmpty());
    }

    private void assertStatusHttpOk(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            assertTrue("Home deve retornar HTTP 200 sem autenticacao",
                    conn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } finally {
            conn.disconnect();
        }
    }
}
