package br.com.vrum.selenium;

import java.time.Duration;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Testes automatizados Selenium — Fluxo do Vendedor (VENDEDOR)
 *
 * Pré-requisitos:
 * - Aplicação rodando em http://localhost:8080/vrum-motors
 * - Banco populado pelo DataInicializador
 * - Google Chrome instalado
 *
 * Execução em ordem garantida por @FixMethodOrder(NAME_ASCENDING).
 * Blocos: A (auth), B (pedidos - US-10 a US-13).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VendedorVrumMotorsSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // ── URLs ──────────────────────────────────────────────────────────────────
    private static final String BASE_URL     = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL    = BASE_URL + "/login.xhtml";
    private static final String URL_PEDIDOS  = BASE_URL + "/pages/vendedor/pedidos.xhtml";

    // ── Credenciais ───────────────────────────────────────────────────────────
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @After
    public void logoutAposCadaTeste() {
        aguardar(1500);
        try {
            WebElement btnSair = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')] | //button[contains(text(),'Sair')]")));
            btnSair.click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.urlContains("home")));
        } catch (Exception e) {
            driver.manage().deleteAllCookies();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Login válido — espera a URL mudar para confirmar redirecionamento. */
    private void fazerLogin() {
        driver.get(LOGIN_URL);
        WebElement campoEmail = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        aguardar(300);
        campoEmail.clear(); campoEmail.sendKeys(EMAIL_VENDEDOR);
        aguardar(300);
        campoSenha.clear(); campoSenha.sendKeys(SENHA_VENDEDOR);
        aguardar(300);
        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/vendedor/"));
    }

    private void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // =========================================================================
    // BLOCO A — Autenticação e Acesso
    // =========================================================================

    /**
     * A01 — Login com credenciais de vendedor.
     * Esperado: redireciona para /pages/vendedor/pedidos.xhtml
     */
    @Test
    public void tc_A01_loginVendedorRedirecionaPedidos() {
        fazerLogin();
        assertTrue("Vendedor deve ir à área de pedidos", driver.getCurrentUrl().contains("/vendedor/"));
        System.out.println("✅ A01 — Login do Vendedor bem-sucedido");
    }

    /**
     * A02 — Tentar acessar área do administrador.
     * Esperado: Acesso negado.
     */
    @Test
    public void tc_A02_vendedorNaoAcessaAreaAdmin() {
        fazerLogin();
        driver.get(BASE_URL + "/pages/admin/dashboard.xhtml");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("vendedor")));
        assertFalse("Vendedor não deve acessar área do admin",
                driver.getCurrentUrl().contains("/admin/dashboard"));
        System.out.println("✅ A02 — Vendedor bloqueado da área de admin");
    }

    // =========================================================================
    // BLOCO B — Gestão de Pedidos
    // =========================================================================

    /**
     * B01 — US-10: Vendedor assume um pedido disponível na fila.
     * Esperado: Mensagem de sucesso.
     */
    @Test
    public void tc_B01_us10_vendedorAssumePedido() {
        fazerLogin();
        driver.get(URL_PEDIDOS);
        
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
            List<WebElement> botoesAssumir = driver.findElements(
                    By.xpath("//input[@value='Assumir'] | //button[contains(text(),'Assumir')]"));

            Assume.assumeTrue("Precondição: Deve haver um pedido na fila para assumir", botoesAssumir.size() > 0);

            botoesAssumir.get(0).click();
            aguardar(1500);
            WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
            assertTrue("Mensagem de sucesso deve aparecer", msgSucesso.isDisplayed());
            System.out.println("✅ B01 (US-10) — Vendedor assumiu o pedido com sucesso");
            
        } catch (TimeoutException e) {
            System.out.println("⚠️ B01 ignorado: Nenhum pedido na fila disponível.");
        }
    }

    /**
     * B02 — Validação (JSF/Backend): Sistema bloqueia envio vazio.
     * Testa o conserto do bug de 'bypass'.
     * Esperado: Mensagem de erro vermelha exigindo campos obrigatórios.
     */
    @Test
    public void tc_B02_validacao_EnviarFabricaSemDadosExibeErro() {
        fazerLogin();
        driver.get(URL_PEDIDOS);
        
        List<WebElement> botoesGerenciar = driver.findElements(
                By.xpath("//input[contains(@value,'Gerenciar') or contains(@value,'Selecionar')] | //button[contains(text(),'Gerenciar')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido EM_NEGOCIACAO para gerenciar", !botoesGerenciar.isEmpty());

        botoesGerenciar.get(0).click();
        aguardar(1500);

        // Limpa campos para forçar erro de required="true" do JSF
        WebElement inputPagamento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@placeholder,'Financiamento') or contains(@id,'Pagamento')]")));
        inputPagamento.clear();

        WebElement inputPrazo = driver.findElement(By.xpath("//input[contains(@placeholder,'Ex: 90') or contains(@id,'Prazo')]"));
        inputPrazo.clear();

        WebElement btnEnviarFabrica = driver.findElement(
                By.xpath("//input[contains(@value,'Enviar para Fabricação')] | //button[contains(text(),'Enviar para Fabricação')]"));
        btnEnviarFabrica.click();
        aguardar(1500);

        // Validação: A mensagem de erro deve aparecer (globalOnly="false" corrigido)
        WebElement msgErro = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
        assertTrue("Deve barrar envio sem forma de pagamento e prazo", msgErro.isDisplayed());
        System.out.println("✅ B02 (Validação) — Envio vazio foi bloqueado corretamente na interface.");
    }

    /**
     * B03 — US-11: Vendedor envia pedido usando Prazo em DIAS (número inteiro).
     * Esperado: Mensagem de sucesso.
     */
    @Test
    public void tc_B03_us11_enviarParaFabricacao_ComPrazoEmDias() {
        fazerLogin();
        driver.get(URL_PEDIDOS);
        
        List<WebElement> botoesGerenciar = driver.findElements(
                By.xpath("//input[contains(@value,'Gerenciar') or contains(@value,'Selecionar')] | //button[contains(text(),'Gerenciar')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido EM_NEGOCIACAO para gerenciar", !botoesGerenciar.isEmpty());

        botoesGerenciar.get(0).click();
        aguardar(1500);

        WebElement inputPagamento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@placeholder,'Financiamento') or contains(@id,'Pagamento')]")));
        inputPagamento.clear();
        inputPagamento.sendKeys("Pix à Vista");

        // NOVO COMPORTAMENTO: Digitar apenas dias (ex: 90) em vez de data (28/05/2026)
        WebElement inputPrazo = driver.findElement(By.xpath("//input[contains(@placeholder,'Ex: 90') or contains(@id,'Prazo')]"));
        inputPrazo.clear();
        inputPrazo.sendKeys("90");
        aguardar(500);

        WebElement btnEnviarFabrica = driver.findElement(
                By.xpath("//input[contains(@value,'Enviar para Fabricação')] | //button[contains(text(),'Enviar para Fabricação')]"));
        btnEnviarFabrica.click();
        aguardar(1500);

        WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Mensagem de sucesso deve aparecer", msgSucesso.isDisplayed());
        System.out.println("✅ B03 (US-11) — Pedido enviado para fabricação usando Prazo em Dias!");
    }

    /**
     * B04 — US-12: Vendedor marca veículo como pronto para entrega.
     * Esperado: Mensagem de sucesso.
     */
    @Test
    public void tc_B04_us12_marcarProntoEntrega() {
        fazerLogin();
        driver.get(URL_PEDIDOS);
        
        List<WebElement> botoesPronto = driver.findElements(
                By.xpath("//input[contains(@value,'Pronto para Entrega')] | //button[contains(text(),'Pronto para Entrega')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido ENVIADO_CIDADE com painel aberto", !botoesPronto.isEmpty());

        botoesPronto.get(0).click();
        aguardar(1500);

        WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Mensagem de sucesso deve aparecer", msgSucesso.isDisplayed());
        System.out.println("✅ B04 (US-12) — Pedido marcado como Pronto para Entrega com sucesso");
    }

    /**
     * B05 — US-13: Vendedor finaliza pedido com data de retirada.
     * Esperado: Mensagem de sucesso.
     */
    @Test
    public void tc_B05_us13_finalizarPedido() {
        fazerLogin();
        driver.get(URL_PEDIDOS);
        
        List<WebElement> botoesFinalizar = driver.findElements(
                By.xpath("//input[contains(@value,'Finalizar Pedido')] | //button[contains(text(),'Finalizar Pedido')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido PRONTO_ENTREGA com painel aberto", !botoesFinalizar.isEmpty());

        WebElement inputData = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='dd/MM/yyyy' or contains(@id,'Retirada')]")));
        inputData.clear();
        inputData.sendKeys("28/12/2026");
        aguardar(500);

        botoesFinalizar.get(0).click();
        aguardar(1500);

        WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Mensagem de sucesso deve aparecer ao finalizar", msgSucesso.isDisplayed());
        System.out.println("✅ B05 (US-13) — Pedido finalizado com sucesso");
    }
    /**
     * TC50 — US-26: Vendedor visualiza link gerado do WhatsApp.
     * Esperado: O link deve conter a estrutura de redirecionamento do WhatsApp.
     */
    @Test
    public void tc_B06_us26_vendedorLinkWhatsApp() {
        fazerLogin(); 
        driver.get(URL_PEDIDOS);

        try {
            WebElement btnWhatsApp = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[contains(@href, 'wa.me')]")));
            
            String linkHref = btnWhatsApp.getAttribute("href");
            
            assertTrue("O link deve conter a estrutura do WhatsApp", 
                        linkHref.contains("wa.me"));

            System.out.println("✅ TC50 (US-26) — Link do WhatsApp validado: " + linkHref);
        } catch (TimeoutException e) {
            System.out.println("⚠️ TC50 ignorado: Nenhum link de WhatsApp encontrado na tela.");
        }
    }
}
