package br.com.vrum.selenium;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
 * Classe base para todos os testes Selenium do painel Admin.
 * Fornece: driver, wait, js, constantes, ciclo de vida e helpers comuns.
 */
public abstract class AdminSeleniumBase {

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static JavascriptExecutor js;

    // ── URLs ──────────────────────────────────────────────────────────────────
    protected static final String BASE_URL     = "http://localhost:8080/vrum-motors";
    protected static final String LOGIN_URL    = BASE_URL + "/login.xhtml";
    protected static final String DASHBOARD    = BASE_URL + "/pages/admin/dashboard.xhtml";
    protected static final String URL_USUARIOS = BASE_URL + "/pages/admin/usuarios.xhtml";
    protected static final String URL_CONC     = BASE_URL + "/pages/admin/concessionarias.xhtml";
    protected static final String URL_VEIC     = BASE_URL + "/pages/admin/veiculos.xhtml";
    protected static final String URL_PEDIDOS  = BASE_URL + "/pages/admin/pedidos.xhtml";

    // ── Credenciais ───────────────────────────────────────────────────────────
    protected static final String EMAIL_ADMIN    = "admin@vrummotors.com";
    protected static final String SENHA_ADMIN    = "admin123";
    protected static final String EMAIL_GERENTE  = "gerente.recife@vrummotors.com";
    protected static final String SENHA_GERENTE  = "gerente123";
    protected static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    protected static final String SENHA_VENDEDOR = "vendedor123";
    protected static final String EMAIL_CLIENTE  = "cliente@email.com";
    protected static final String SENHA_CLIENTE  = "cliente123";
    protected static final String EMAIL_FABRICA  = "fabrica@vrummotors.com";
    protected static final String SENHA_FABRICA  = "fabrica123";

    // ── Dados gerados nessa sessão ────────────────────────────────────────────
    protected static final long   TS                   = System.currentTimeMillis();
    protected static final String EMAIL_USUARIO_TESTE  = "selenium_admin_" + TS + "@teste.com";
    protected static final String NOME_USUARIO_TESTE   = "Selenium Admin User";
    protected static final String NOME_CON_TESTE       = "Vrum Selenium " + TS;
    protected static final String NOME_VEICULO_TESTE   = "Vrum Selenium";
    protected static final String MODELO_VEICULO_TESTE = "ST" + (TS % 10000);

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        opts.addArguments("--no-first-run");
        opts.addArguments("--disable-background-networking");
        // Desabilita completamente o Safe Browsing / gerenciador de senhas do Chrome
        // para evitar diálogos de "Salvar senha" e "Mude sua senha" durante os testes
        opts.addArguments("--disable-features=SafeBrowsing,PasswordLeakDetection,"
                + "SafeBrowsingEnhancedProtection,PasswordManager");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", false);
        prefs.put("safebrowsing_without_cookies_enabled", false);
        opts.setExperimentalOption("prefs", prefs);
        opts.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        driver = new ChromeDriver(opts);
        js     = (JavascriptExecutor) driver;
        wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
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
            // ignored — finally garante limpeza
        } finally {
            driver.manage().deleteAllCookies();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Login válido — espera a URL mudar para confirmar redirecionamento. */
    protected void fazerLogin(String email, String senha) {
        preencherESubmeterLogin(email, senha);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(LOGIN_URL)));
    }

    /**
     * Apenas preenche e submete o formulário de login, SEM aguardar
     * redirecionamento. Usar quando o login pode falhar (ex: senha errada).
     */
    protected void preencherESubmeterLogin(String email, String senha) {
        driver.get(LOGIN_URL);
        WebElement campoEmail = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        aguardar(300);
        campoEmail.clear(); campoEmail.sendKeys(email);
        aguardar(300);
        campoSenha.clear(); campoSenha.sendKeys(senha);
        aguardar(300);
        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
        aguardar(1000);
    }

    protected void abrirFormNovoUsuario() {
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.urlContains("usuarios"));
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Novo Usuário')] | //button[contains(text(),'Novo Usuário')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNome']")));
        aguardar(500);
    }

    protected void abrirFormNovaConcessionaria() {
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.urlContains("concessionarias"));
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Nova Concession')] | //button[contains(text(),'Nova Concession')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeCon']")));
        aguardar(500);
    }

    protected void abrirFormNovoVeiculo() {
        driver.get(URL_VEIC);
        wait.until(ExpectedConditions.urlContains("veiculos"));
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Novo Veículo')] | //button[contains(text(),'Novo Veículo')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeV']")));
        aguardar(500);
    }

    /** Dispara eventos input+blur para acionar validações JS do campo. */
    protected void dispararValidacao(WebElement el) {
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", el);
        js.executeScript("arguments[0].dispatchEvent(new Event('blur',{bubbles:true}))", el);
        aguardar(300);
    }

    /**
     * Remove o atributo disabled de um botão via JS (para testar validações server-side).
     * Usa aspas duplas como delimitador do querySelector para não conflitar com as
     * aspas simples presentes nos seletores de atributo CSS (ex: [id$=':btnSalvar']).
     */
    protected void habilitarBotaoJs(String seletor) {
        js.executeScript("var b=document.querySelector(\"" + seletor + "\"); if(b) b.disabled=false;");
        aguardar(200);
    }

    /**
     * Clica no primeiro pedido da tabela para abrir o painel de edição.
     * Tenta o botão "Selecionar/Ver"; se não existir, clica diretamente na linha.
     */
    protected void abrirPrimeiroPedido() {
        List<WebElement> btnsSel = driver.findElements(By.xpath(
                "(//input[contains(@value,'Selecionar') or contains(@value,'Editar')] | " +
                "//button[contains(text(),'Selecionar') or contains(text(),'Ver') or contains(text(),'Editar')])[1]"));
        if (!btnsSel.isEmpty()) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnsSel.get(0)));
            js.executeScript("arguments[0].click();", btn);
        } else {
            List<WebElement> rows = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
            if (!rows.isEmpty()) rows.get(0).click();
        }
        aguardar(1000);
    }

    protected void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
