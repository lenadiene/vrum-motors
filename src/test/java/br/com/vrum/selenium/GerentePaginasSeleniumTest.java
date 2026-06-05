package br.com.vrum.selenium;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
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
 * Testes Selenium focados nas paginas do gerente:
 * vendedores.xhtml e pedidos.xhtml.
 *
 * Pre-requisitos:
 * - Aplicacao rodando em http://localhost:8080/vrum-motors
 * - Banco populado pelo DataInicializador
 * - Google Chrome instalado
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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

    @Test
    public void tc03_vendedoresCriaVendedorValidoFluxoFeliz() {
        fazerLoginGerente();
        abrirNovoVendedor();

        long ts = System.currentTimeMillis();
        String nome = "Selenium Gerente " + ts;
        String email = "selenium_gerente_" + ts + "@teste.com";
        String telefone = "819" + String.format("%08d", ts % 100000000L);

        preencherVendedor(nome, email, telefone, "senha123");

        WebElement salvar = driver.findElement(By.id("vendedorForm:btnSalvar"));
        assertFalse("Botao salvar deve ficar habilitado com vendedor valido", botaoDesabilitado(salvar));
        salvar.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + email + "')]")));
    }

    @Test
    public void tc04_vendedoresNomeMuitoLongoRespeitaMaxlength() {
        fazerLoginGerente();
        abrirNovoVendedor();

        WebElement nome = driver.findElement(By.id("vendedorForm:nome"));
        nome.sendKeys(repetir("A", 150));
        dispararInput(nome);

        assertEquals("Campo nome deve truncar em 100 caracteres", 100, nome.getAttribute("value").length());
        assertTrue("Mensagem de limite do nome deve aparecer",
                driver.findElement(By.id("nome-validation-msg")).isDisplayed());
    }

    @Test
    public void tc05_vendedoresTelefoneBloqueiaLetrasELimitaDigitos() {
        fazerLoginGerente();
        abrirNovoVendedor();

        WebElement telefone = driver.findElement(By.id("vendedorForm:telefone"));
        telefone.sendKeys("abc123456789012345");
        dispararInput(telefone);

        String valor = telefone.getAttribute("value");
        assertTrue("Telefone deve manter somente numeros", valor.matches("\\d*"));
        assertTrue("Telefone deve respeitar maxlength=11", valor.length() <= 11);
    }

    @Test
    public void tc06_vendedoresCancelarFechaFormulario() {
        fazerLoginGerente();
        abrirNovoVendedor();

        WebElement cancelar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Cancelar')] | //button[contains(text(),'Cancelar')]")));
        cancelar.click();

        wait.until(driver -> driver.findElements(By.id("vendedorForm:nome")).isEmpty());
    }

    @Test
    public void tc07_vendedoresSubmitForcadoEmailInvalidoExibeErro() {
        fazerLoginGerente();
        abrirNovoVendedor();

        preencherVendedor("Vendedor Email Invalido", "email-invalido", "81999999991", "senha123");
        WebElement salvar = driver.findElement(By.id("vendedorForm:btnSalvar"));

        js.executeScript("arguments[0].disabled=false; arguments[0].click();", salvar);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
        assertTrue("Deve permanecer na tela de vendedores",
                driver.getCurrentUrl().contains("/gerente/vendedores"));
    }

    @Test
    public void tc08_vendedoresEditarNaoExigeSenha() {
        fazerLoginGerente();
        driver.get(VENDEDORES_URL);
        wait.until(ExpectedConditions.urlContains("/gerente/vendedores"));

        List<WebElement> botoesEditar = driver.findElements(By.xpath(
                "//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')]"));
        Assume.assumeTrue("Nao ha vendedores cadastrados para testar edicao", !botoesEditar.isEmpty());

        botoesEditar.get(0).click();
        WebElement senha = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("vendedorForm:senha")));

        assertEquals("false", senha.getAttribute("data-required"));
        assertTrue("Label deve indicar que a senha pode ficar em branco",
                driver.getPageSource().contains("deixe em branco para manter"));
    }

    @Test
    public void tc09_pedidosFiltroBuscaClienteLimitaCemELimpar() {
        fazerLoginGerente();
        driver.get(PEDIDOS_URL);

        WebElement busca = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pedidoForm:buscaCliente")));
        busca.sendKeys(repetir("A", 150));
        assertEquals("Busca deve truncar em 100 caracteres", 100, busca.getAttribute("value").length());

        driver.findElement(By.id("pedidoForm:btnLimpar")).click();
        wait.until(ExpectedConditions.attributeToBe(By.id("pedidoForm:buscaCliente"), "value", ""));
    }

    @Test
    public void tc10_pedidosRegraSemVendedorStatusEmNegociacaoBloqueia() {
        fazerLoginGerente();
        abrirPrimeiroPedidoParaEdicao();

        selecionarPorValor("pedidoForm:editVendedor", "");
        selecionarPorValor("pedidoForm:editStatus", "EM_NEGOCIACAO");

        WebElement salvar = driver.findElement(By.id("pedidoForm:btnSalvarPedido"));
        assertTrue("Status em andamento sem vendedor deve bloquear salvar", botaoDesabilitado(salvar));
        assertTrue("Mensagem deve explicar exigencia de vendedor",
                driver.findElement(By.id("js-error-msg")).getText().contains("exige um Vendedor"));
    }

    @Test
    public void tc11_pedidosRegraComVendedorAguardandoBloqueia() {
        fazerLoginGerente();
        abrirPrimeiroPedidoParaEdicao();

        Select vendedor = new Select(driver.findElement(By.id("pedidoForm:editVendedor")));
        Assume.assumeTrue("Nao ha vendedor disponivel para associar ao pedido",
                vendedor.getOptions().size() > 1);
        vendedor.selectByIndex(1);
        dispararChange(driver.findElement(By.id("pedidoForm:editVendedor")));

        selecionarPorValor("pedidoForm:editStatus", "AGUARDANDO_ATENDIMENTO");

        WebElement salvar = driver.findElement(By.id("pedidoForm:btnSalvarPedido"));
        assertTrue("Pedido com vendedor nao pode ficar aguardando atendimento", botaoDesabilitado(salvar));
        assertTrue("Mensagem deve explicar conflito de vendedor/status",
                driver.findElement(By.id("js-error-msg")).getText().contains("Aguardando Atendimento"));
    }

    @Test
    public void tc12_pedidosAnexoTxtBloqueado() throws Exception {
        fazerLoginGerente();
        abrirPrimeiroPedidoParaEdicao();

        File arquivoTxt = File.createTempFile("selenium-anexo-gerente", ".txt");
        WebElement anexo = driver.findElement(By.id("pedidoForm:arquivoAnexo"));
        anexo.sendKeys(arquivoTxt.getAbsolutePath());
        dispararChange(anexo);

        assertTrue("Anexo TXT deve bloquear salvar",
                botaoDesabilitado(driver.findElement(By.id("pedidoForm:btnSalvarPedido"))));
        assertTrue("Mensagem de anexo invalido deve aparecer",
                driver.findElement(By.id("anexo-validation-msg")).isDisplayed());
    }

    @Test
    public void tc13_pedidosFormaPagamentoRespeitaMaxlength() {
        fazerLoginGerente();
        abrirPrimeiroPedidoParaEdicao();

        WebElement pagamento = driver.findElement(By.id("pedidoForm:editFormaPagamento"));
        pagamento.clear();
        pagamento.sendKeys(repetir("P", 150));
        dispararInput(pagamento);

        assertEquals("Forma de pagamento deve truncar em 100 caracteres",
                100, pagamento.getAttribute("value").length());
        assertTrue("Mensagem de limite da forma de pagamento deve aparecer",
                driver.findElement(By.id("pagamento-validation-msg")).isDisplayed());
    }

    @Test
    public void tc14_pedidosDataFormatoValidoNaoMostraErro() {
        fazerLoginGerente();
        abrirPrimeiroPedidoParaEdicao();

        WebElement prazo = driver.findElement(By.id("pedidoForm:editPrazoFabricacao"));
        prazo.clear();
        prazo.sendKeys("31/12/2026");
        dispararInput(prazo);

        assertFalse("Data valida nao deve mostrar mensagem de formato",
                driver.findElement(By.id("prazo-validation-msg")).isDisplayed());
    }

    @Test
    public void tc15_pedidosFecharEdicaoRemoveFormulario() {
        fazerLoginGerente();
        abrirPrimeiroPedidoParaEdicao();

        WebElement fechar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Fechar')] | //button[contains(text(),'Fechar')]")));
        fechar.click();

        wait.until(driver -> driver.findElements(By.id("pedidoForm:editPrazoFabricacao")).isEmpty());
    }

    @Test
    public void tc16_acessoDiretoSemLoginRedirecionaParaLogin() {
        driver.manage().deleteAllCookies();
        driver.get(VENDEDORES_URL);
        wait.until(ExpectedConditions.urlContains("login"));

        driver.manage().deleteAllCookies();
        driver.get(PEDIDOS_URL);
        wait.until(ExpectedConditions.urlContains("login"));
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

    private void abrirNovoVendedor() {
        driver.get(VENDEDORES_URL);
        wait.until(ExpectedConditions.urlContains("/gerente/vendedores"));
        WebElement novo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Novo Vendedor')] | //button[contains(text(),'Novo Vendedor')]")));
        novo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("vendedorForm:nome")));
    }

    private void preencherVendedor(String nome, String email, String telefone, String senha) {
        preencherCampo("vendedorForm:nome", nome);
        preencherCampo("vendedorForm:email", email);
        preencherCampo("vendedorForm:telefone", telefone);
        preencherCampo("vendedorForm:senha", senha);
    }

    private void preencherCampo(String id, String valor) {
        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        campo.clear();
        campo.sendKeys(valor);
        dispararInput(campo);
    }

    private void abrirPrimeiroPedidoParaEdicao() {
        driver.get(PEDIDOS_URL);
        wait.until(ExpectedConditions.urlContains("/gerente/pedidos"));
        List<WebElement> botoesEditar = driver.findElements(By.xpath(
                "//table[contains(@class,'vrum-table')]//input[contains(@value,'Editar')] | //table[contains(@class,'vrum-table')]//button[contains(text(),'Editar')]"));
        Assume.assumeTrue("Nao ha pedidos cadastrados para testar edicao", !botoesEditar.isEmpty());

        botoesEditar.get(0).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pedidoForm:editPrazoFabricacao")));
    }

    private void selecionarPorValor(String id, String valor) {
        WebElement select = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        new Select(select).selectByValue(valor);
        dispararChange(select);
    }

    private void dispararInput(WebElement element) {
        js.executeScript("arguments[0].dispatchEvent(new Event('input', {bubbles:true}))", element);
        js.executeScript("arguments[0].dispatchEvent(new Event('blur', {bubbles:true}))", element);
    }

    private void dispararChange(WebElement element) {
        js.executeScript("arguments[0].dispatchEvent(new Event('change', {bubbles:true}))", element);
    }

    private boolean botaoDesabilitado(WebElement button) {
        return Boolean.parseBoolean(button.getAttribute("disabled"));
    }

    private String repetir(String valor, int vezes) {
        return new String(new char[vezes]).replace("\0", valor);
    }
}
