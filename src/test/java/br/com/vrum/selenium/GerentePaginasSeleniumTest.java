package br.com.vrum.selenium;

import java.time.Duration;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
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
 * Testes Selenium focados nas paginas do gerente:
 * vendedores.xhtml e pedidos.xhtml.
 *
 * Pre-requisitos:
 * - Aplicacao rodando em http://localhost:8080/vrum-motors
 * - Banco populado pelo DataInicializador
 * - Google Chrome instalado
 */
public class GerentePaginasSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;

    private static final String BASE_URL = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL = BASE_URL + "/login.xhtml";
    private static final String VENDEDORES_URL = BASE_URL + "/pages/gerente/vendedores.xhtml";
    private static final String PEDIDOS_URL = BASE_URL + "/pages/gerente/pedidos.xhtml";

    private static final String EMAIL_GERENTE = "gerente.recife@vrummotors.com";
    private static final String SENHA_GERENTE = "gerente123";

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        if (Boolean.getBoolean("selenium.headless")) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @After
    public void logout() {
        try {
            WebElement sair = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')] | //button[contains(text(),'Sair')]")));
            sair.click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.urlContains("home")));
        } catch (Exception e) {
            driver.manage().deleteAllCookies();
        }
    }

    @Test
    public void tc01_vendedoresExibeCamposComValidacoes() {
        fazerLoginGerente();
        driver.get(VENDEDORES_URL);

        wait.until(ExpectedConditions.urlContains("/gerente/vendedores"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Vendedores')]")));

        WebElement novo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Novo Vendedor')] | //button[contains(text(),'Novo Vendedor')]")));
        novo.click();

        WebElement nome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("vendedorForm:nome")));
        WebElement email = driver.findElement(By.id("vendedorForm:email"));
        WebElement telefone = driver.findElement(By.id("vendedorForm:telefone"));
        WebElement senha = driver.findElement(By.id("vendedorForm:senha"));
        WebElement salvar = driver.findElement(By.id("vendedorForm:btnSalvar"));

        assertEquals("100", nome.getAttribute("maxlength"));
        assertEquals("150", email.getAttribute("maxlength"));
        assertEquals("11", telefone.getAttribute("maxlength"));
        assertEquals("50", senha.getAttribute("maxlength"));

        nome.sendKeys("A");
        email.sendKeys("email-invalido");
        telefone.sendKeys("12345");
        senha.sendKeys("123");
        dispararInput(nome);
        dispararInput(email);
        dispararInput(telefone);
        dispararInput(senha);

        assertTrue("Botao salvar deve ficar desabilitado com dados invalidos",
                Boolean.parseBoolean(salvar.getAttribute("disabled")));
        assertTrue("Mensagem de e-mail invalido deve aparecer",
                driver.findElement(By.id("email-validation-msg")).isDisplayed());
        assertTrue("Mensagem de telefone invalido deve aparecer",
                driver.findElement(By.id("telefone-validation-msg")).isDisplayed());
    }

    @Test
    public void tc02_pedidosExibeFiltrosEValidacoesDeEdicao() {
        fazerLoginGerente();
        driver.get(PEDIDOS_URL);

        wait.until(ExpectedConditions.urlContains("/gerente/pedidos"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Pedidos da Unidade')]")));

        WebElement buscaCliente = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pedidoForm:buscaCliente")));
        assertEquals("100", buscaCliente.getAttribute("maxlength"));
        assertNotNull(driver.findElement(By.id("pedidoForm:filtroVendedor")));
        assertNotNull(driver.findElement(By.id("pedidoForm:filtroVeiculo")));
        assertNotNull(driver.findElement(By.id("pedidoForm:filtroStatus")));

        List<WebElement> botoesEditar = driver.findElements(By.xpath(
                "//table[contains(@class,'vrum-table')]//input[contains(@value,'Editar')] | //table[contains(@class,'vrum-table')]//button[contains(text(),'Editar')]"));
        Assume.assumeTrue("Nao ha pedidos cadastrados para testar edicao", !botoesEditar.isEmpty());

        botoesEditar.get(0).click();

        WebElement prazo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pedidoForm:editPrazoFabricacao")));
        WebElement pagamento = driver.findElement(By.id("pedidoForm:editFormaPagamento"));
        WebElement anexo = driver.findElement(By.id("pedidoForm:arquivoAnexo"));
        WebElement salvar = driver.findElement(By.id("pedidoForm:btnSalvarPedido"));

        assertEquals("10", prazo.getAttribute("maxlength"));
        assertEquals("100", pagamento.getAttribute("maxlength"));
        assertTrue("Anexo deve aceitar PDF", anexo.getAttribute("accept").contains(".pdf"));
        assertTrue("Anexo deve aceitar PNG", anexo.getAttribute("accept").contains(".png"));

        prazo.clear();
        prazo.sendKeys("99/99");
        dispararInput(prazo);

        assertTrue("Botao salvar deve ficar desabilitado com data fora do formato",
                Boolean.parseBoolean(salvar.getAttribute("disabled")));
        assertTrue("Mensagem de prazo invalido deve aparecer",
                driver.findElement(By.id("prazo-validation-msg")).isDisplayed());
    }

    private void fazerLoginGerente() {
        driver.get(LOGIN_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement senha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        email.clear();
        email.sendKeys(EMAIL_GERENTE);
        senha.clear();
        senha.sendKeys(SENHA_GERENTE);
        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/gerente/"));
    }

    private void dispararInput(WebElement element) {
        js.executeScript("arguments[0].dispatchEvent(new Event('input', {bubbles:true}))", element);
        js.executeScript("arguments[0].dispatchEvent(new Event('blur', {bubbles:true}))", element);
    }
}
