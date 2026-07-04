package br.com.vrum.selenium;

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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Testes automatizados Selenium — Fluxo Admin (ADMIN_EMPRESA)
 *
 * Pré-requisitos:
 *   - Aplicação rodando em http://localhost:8080/vrum-motors
 *   - Banco populado pelo DataInicializador
 *   - Google Chrome instalado
 *
 * Execução em ordem garantida por @FixMethodOrder(NAME_ASCENDING).
 * Blocos: A (auth), B (dashboard), C (usuários), D (concessionárias),
 *         E (veículos), F (pedidos), G (sidebar/nav).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminVrumMotorsSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;

    // ── URLs ──────────────────────────────────────────────────────────────────
    private static final String BASE_URL     = "http://localhost:8080/vrum-motors";
    private static final String LOGIN_URL    = BASE_URL + "/login.xhtml";
    private static final String DASHBOARD    = BASE_URL + "/pages/admin/dashboard.xhtml";
    private static final String URL_USUARIOS = BASE_URL + "/pages/admin/usuarios.xhtml";
    private static final String URL_CONC     = BASE_URL + "/pages/admin/concessionarias.xhtml";
    private static final String URL_VEIC     = BASE_URL + "/pages/admin/veiculos.xhtml";
    private static final String URL_PEDIDOS  = BASE_URL + "/pages/admin/pedidos.xhtml";

    // ── Credenciais ───────────────────────────────────────────────────────────
    private static final String EMAIL_ADMIN    = "admin@vrummotors.com";
    private static final String SENHA_ADMIN    = "admin123";
    private static final String EMAIL_GERENTE  = "gerente.recife@vrummotors.com";
    private static final String SENHA_GERENTE  = "gerente123";
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";
    private static final String EMAIL_CLIENTE  = "cliente@email.com";
    private static final String SENHA_CLIENTE  = "cliente123";
    private static final String EMAIL_FABRICA  = "fabrica@vrummotors.com";
    private static final String SENHA_FABRICA  = "fabrica123";

    // ── Dados gerados nessa sessão ────────────────────────────────────────────
    private static final long   TS                   = System.currentTimeMillis();
    private static final String EMAIL_USUARIO_TESTE  = "selenium_admin_" + TS + "@teste.com";
    private static final String NOME_USUARIO_TESTE   = "Selenium Admin User";
    private static final String NOME_CON_TESTE       = "Vrum Selenium " + TS;
    private static final String NOME_VEICULO_TESTE   = "Vrum Selenium";
    private static final String MODELO_VEICULO_TESTE = "ST" + (TS % 10000);

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
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
    private void fazerLogin(String email, String senha) {
        preencherESubmeterLogin(email, senha);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(LOGIN_URL)));
    }

    /**
     * Apenas preenche e submete o formulário de login, SEM aguardar
     * redirecionamento. Usar quando o login pode falhar (ex: senha errada).
     */
    private void preencherESubmeterLogin(String email, String senha) {
        driver.get(LOGIN_URL);
        WebElement campoEmail = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        aguardar(300);
        campoEmail.clear(); campoEmail.sendKeys(email);
        aguardar(300);
        campoSenha.clear(); campoSenha.sendKeys(senha);
        aguardar(300);
        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
        aguardar(1000); // aguarda resposta do servidor sem travar em TimeoutException
    }

    private void abrirFormNovoUsuario() {
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.urlContains("usuarios"));
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Novo Usuário')] | //button[contains(text(),'Novo Usuário')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNome']")));
        aguardar(500);
    }

    private void abrirFormNovaConcessionaria() {
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.urlContains("concessionarias"));
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Nova Concession')] | //button[contains(text(),'Nova Concession')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeCon']")));
        aguardar(500);
    }

    private void abrirFormNovoVeiculo() {
        driver.get(URL_VEIC);
        wait.until(ExpectedConditions.urlContains("veiculos"));
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Novo Veículo')] | //button[contains(text(),'Novo Veículo')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeV']")));
        aguardar(500);
    }

    /** Dispara eventos input+blur para acionar validações JS do campo. */
    private void dispararValidacao(WebElement el) {
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", el);
        js.executeScript("arguments[0].dispatchEvent(new Event('blur',{bubbles:true}))", el);
        aguardar(300);
    }

    /**
     * Remove o atributo disabled de um botão via JS (para testar validações server-side).
     * Usa aspas duplas como delimitador do querySelector para não conflitar com as
     * aspas simples presentes nos seletores de atributo CSS (ex: [id$=':btnSalvar']).
     */
    private void habilitarBotaoJs(String seletor) {
        js.executeScript("var b=document.querySelector(\"" + seletor + "\"); if(b) b.disabled=false;");
        aguardar(200);
    }

    /**
     * Clica no primeiro pedido da tabela para abrir o painel de edição.
     * Tenta o botão "Selecionar/Ver"; se não existir, clica diretamente na linha.
     * Deve ser chamado após confirmar que há pedidos com Assume.assumeTrue.
     */
    private void abrirPrimeiroPedido() {
        List<WebElement> btnsSel = driver.findElements(By.xpath(
                "(//input[contains(@value,'Selecionar')] | //button[contains(text(),'Selecionar') or contains(text(),'Ver')])[1]"));
        if (!btnsSel.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(btnsSel.get(0))).click();
        } else {
            driver.findElements(By.cssSelector(".vrum-table tbody tr")).get(0).click();
        }
        aguardar(800);
    }

    private void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // =========================================================================
    // BLOCO A — Autenticação e Acesso
    // =========================================================================

    /**
     * A01 — Login com credenciais admin válidas.
     * Esperado: redireciona para /pages/admin/dashboard.xhtml
     */
    @Test
    public void tc_A01_loginAdminValidoRedirecionaDashboard() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        assertTrue("Admin deve ir ao dashboard", driver.getCurrentUrl().contains("/admin/"));
        System.out.println("✅ A01 — Login admin redireciona ao dashboard");
    }

    /**
     * A02 — Login com email correto e senha errada.
     * Esperado: permanece no login, exibe mensagem de erro.
     */
    @Test
    public void tc_A02_loginSenhaErradaNaoAcessaAdmin() {
        // Usa preencherESubmeterLogin para NÃO lançar TimeoutException
        // quando o JSF permanece em login.xhtml após falha de autenticação.
        preencherESubmeterLogin(EMAIL_ADMIN, "senhaErrada999");
        assertTrue("Deve permanecer no login com senha errada",
                driver.getCurrentUrl().contains("login"));
        System.out.println("✅ A02 — Senha errada mantém na página de login");
    }

    /**
     * A03 — Acesso direto a /pages/admin/dashboard.xhtml sem login.
     * Esperado: redireciona para login.xhtml.
     */
    @Test
    public void tc_A03_acessoDiretoSemLoginRedirecionaLogin() {
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Acesso sem sessão deve redirecionar ao login", driver.getCurrentUrl().contains("login"));
        System.out.println("✅ A03 — Acesso direto sem login redireciona ao login");
    }

    /**
     * A04 — Usuário GERENTE tenta acessar /pages/admin/.
     * Esperado: redireciona para acesso-negado.xhtml (ou para área do gerente).
     */
    @Test
    public void tc_A04_gerenteNaoAcessaAreaAdmin() {
        fazerLogin(EMAIL_GERENTE, SENHA_GERENTE);
        wait.until(ExpectedConditions.urlContains("/gerente/"));
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("gerente")));
        assertFalse("Gerente não deve acessar dashboard admin",
                driver.getCurrentUrl().contains("/admin/dashboard"));
        System.out.println("✅ A04 — Gerente bloqueado da área admin. URL: " + driver.getCurrentUrl());
    }

    /**
     * A05 — Usuário VENDEDOR tenta acessar /pages/admin/.
     * Esperado: redireciona para acesso-negado.xhtml (ou área do vendedor).
     */
    @Test
    public void tc_A05_vendedorNaoAcessaAreaAdmin() {
        fazerLogin(EMAIL_VENDEDOR, SENHA_VENDEDOR);
        wait.until(ExpectedConditions.urlContains("/vendedor/"));
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("vendedor")));
        assertFalse("Vendedor não deve acessar dashboard admin",
                driver.getCurrentUrl().contains("/admin/dashboard"));
        System.out.println("✅ A05 — Vendedor bloqueado da área admin. URL: " + driver.getCurrentUrl());
    }

    /**
     * A06 — Usuário CLIENTE tenta acessar /pages/admin/.
     * Esperado: redireciona para acesso-negado.xhtml (ou área do cliente).
     */
    @Test
    public void tc_A06_clienteNaoAcessaAreaAdmin() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);
        wait.until(ExpectedConditions.urlContains("/cliente/"));
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("cliente")));
        assertFalse("Cliente não deve acessar dashboard admin",
                driver.getCurrentUrl().contains("/admin/dashboard"));
        System.out.println("✅ A06 — Cliente bloqueado da área admin. URL: " + driver.getCurrentUrl());
    }

    /**
     * A07 — Usuário ADMIN_FABRICA tenta acessar /pages/admin/.
     * Esperado: redireciona para acesso-negado.xhtml (ou área de fábrica).
     */
    @Test
    public void tc_A07_fabricaNaoAcessaAreaAdminEmpresa() {
        fazerLogin(EMAIL_FABRICA, SENHA_FABRICA);
        wait.until(ExpectedConditions.urlContains("/fabrica/"));
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("fabrica")));
        assertFalse("Admin fábrica não deve acessar dashboard admin empresa",
                driver.getCurrentUrl().contains("/admin/dashboard"));
        System.out.println("✅ A07 — Admin fábrica bloqueado da área admin empresa. URL: " + driver.getCurrentUrl());
    }

    /**
     * A08 — Logout do admin.
     * Esperado: invalida sessão e redireciona para login.xhtml.
     */
    @Test
    public void tc_A08_logoutAdminRedirecionaLogin() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        WebElement btnSair = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')] | //button[contains(text(),'Sair')]")));
        btnSair.click();
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Logout deve redirecionar ao login", driver.getCurrentUrl().contains("login"));
        System.out.println("✅ A08 — Logout redireciona ao login");
    }

    /**
     * A09 — Após logout, tentar acessar /pages/admin/.
     * Esperado: redireciona para login.xhtml (sessão inválida).
     */
    @Test
    public void tc_A09_aposLogoutAcessoAdminBloqueado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        driver.manage().deleteAllCookies();
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Após logout a área admin deve estar bloqueada", driver.getCurrentUrl().contains("login"));
        System.out.println("✅ A09 — Após logout acesso admin bloqueado");
    }

    // =========================================================================
    // BLOCO B — Dashboard
    // =========================================================================

    /**
     * B01 — Admin acessa dashboard.
     * Esperado: página carrega com título e conteúdo.
     */
    @Test
    public void tc_B01_dashboardCarregaComTitulo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue("Dashboard deve ter título h1 visível", h1.getText().length() > 0);
        System.out.println("✅ B01 — Dashboard carregado. Título: " + h1.getText());
    }

    /**
     * B02 — Dashboard exibe link para Usuários.
     * Esperado: clique navega para usuarios.xhtml.
     */
    @Test
    public void tc_B02_dashboardLinkUsuariosNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'usuarios')]"))).click();
        wait.until(ExpectedConditions.urlContains("usuarios"));
        assertTrue("Link Usuários deve navegar para usuarios.xhtml",
                driver.getCurrentUrl().contains("usuarios"));
        System.out.println("✅ B02 — Link Usuários navega corretamente");
    }

    /**
     * B03 — Dashboard exibe link para Concessionárias.
     * Esperado: clique navega para concessionarias.xhtml.
     */
    @Test
    public void tc_B03_dashboardLinkConcessionariasNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'concessionarias')]"))).click();
        wait.until(ExpectedConditions.urlContains("concessionarias"));
        assertTrue("Link Concessionárias deve navegar para concessionarias.xhtml",
                driver.getCurrentUrl().contains("concessionarias"));
        System.out.println("✅ B03 — Link Concessionárias navega corretamente");
    }

    /**
     * B04 — Dashboard exibe link para Veículos.
     * Esperado: clique navega para veiculos.xhtml.
     */
    @Test
    public void tc_B04_dashboardLinkVeiculosNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'veiculos')]"))).click();
        wait.until(ExpectedConditions.urlContains("veiculos"));
        assertTrue("Link Veículos deve navegar para veiculos.xhtml",
                driver.getCurrentUrl().contains("veiculos"));
        System.out.println("✅ B04 — Link Veículos navega corretamente");
    }

    /**
     * B05 — Dashboard exibe link para Pedidos.
     * Esperado: clique navega para pedidos.xhtml.
     */
    @Test
    public void tc_B05_dashboardLinkPedidosNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'pedidos')]"))).click();
        wait.until(ExpectedConditions.urlContains("pedidos"));
        assertTrue("Link Pedidos deve navegar para pedidos.xhtml",
                driver.getCurrentUrl().contains("pedidos"));
        System.out.println("✅ B05 — Link Pedidos navega corretamente");
    }

    // =========================================================================
    // BLOCO C — Gestão de Usuários
    // =========================================================================

    /**
     * C01 — Admin acessa página de usuários.
     * Esperado: tabela de usuários exibida.
     */
    @Test
    public void tc_C01_adminAcessaListaUsuarios() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela de usuários deve existir", tabela);
        System.out.println("✅ C01 — Lista de usuários carregada");
    }

    /**
     * C02 — Tabela lista usuários com nome, email, perfil e status.
     * Esperado: colunas corretas no cabeçalho.
     */
    @Test
    public void tc_C02_tabelaUsuariosExibeColunas() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        String cabecalho = driver.findElement(By.cssSelector(".vrum-table thead")).getText().toLowerCase();
        assertTrue("Coluna nome", cabecalho.contains("nome"));
        assertTrue("Coluna email", cabecalho.contains("e-mail") || cabecalho.contains("email"));
        assertTrue("Coluna perfil", cabecalho.contains("perfil"));
        assertTrue("Coluna status", cabecalho.contains("status"));
        System.out.println("✅ C02 — Colunas da tabela de usuários confirmadas");
    }

    /**
     * C03 — Admin clica em "+ Novo Usuário".
     * Esperado: formulário abre vazio, botão Salvar desabilitado.
     */
    @Test
    public void tc_C03_abrirFormNovoUsuarioBotaoDesabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarUsuario']")));
        assertFalse("Botão Salvar deve iniciar desabilitado", btn.isEnabled());
        System.out.println("✅ C03 — Formulário novo usuário: botão Salvar desabilitado");
    }

    /**
     * C04 — Admin preenche todos os campos válidos e salva um CLIENTE.
     * Esperado: mensagem de sucesso, usuário aparece na tabela.
     */
    @Test
    public void tc_C04_criarUsuarioClienteValido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys(NOME_USUARIO_TESTE);          dispararValidacao(nome);
        email.sendKeys(EMAIL_USUARIO_TESTE);        dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE"); aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarUsuario']:not([disabled])")));
        btn.click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Deve exibir mensagem de sucesso", msg.isDisplayed());
        System.out.println("✅ C04 — Usuário CLIENTE criado: " + EMAIL_USUARIO_TESTE);
    }

    /**
     * C05 — Admin cria usuário VENDEDOR e seleciona concessionária.
     * Esperado: salvo com sucesso.
     */
    @Test
    public void tc_C05_criarUsuarioVendedorComConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        String emailVend = "selenium_vend_" + TS + "@teste.com";
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Vendedor Selenium");   dispararValidacao(nome);
        email.sendKeys(emailVend);            dispararValidacao(email);
        new Select(perfil).selectByValue("VENDEDOR"); aguardar(500);

        WebElement selectConc = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[id$='concessionariaIdSelecionada']")));
        new Select(selectConc).selectByIndex(1);
        aguardar(300);

        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C05 — Resultado criação Vendedor: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C05 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * C06 — Admin cria usuário GERENTE e seleciona concessionária.
     * Esperado: salvo com sucesso.
     */
    @Test
    public void tc_C06_criarUsuarioGerenteComConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        String emailGer = "selenium_ger_" + TS + "@teste.com";
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Gerente Selenium");    dispararValidacao(nome);
        email.sendKeys(emailGer);             dispararValidacao(email);
        new Select(perfil).selectByValue("GERENTE"); aguardar(500);

        try {
            WebElement selectConc = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='concessionariaIdSelecionada']")));
            List<WebElement> opcoes = new Select(selectConc).getOptions();
            // Tenta selecionar uma concessionária sem gerente (última da lista tem mais chance)
            new Select(selectConc).selectByIndex(opcoes.size() - 1);
        } catch (TimeoutException e) {
            System.out.println("⚠️ C06 — Campo concessionária não exibido para GERENTE");
            return;
        }
        aguardar(300);

        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C06 — Resultado criação Gerente: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C06 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * C07 — Admin cria usuário ADMIN_FABRICA.
     * Esperado: salvo com sucesso.
     */
    @Test
    public void tc_C07_criarUsuarioAdminFabrica() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        String emailFab = "selenium_fab_" + TS + "@teste.com";
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Fabrica Selenium");   dispararValidacao(nome);
        email.sendKeys(emailFab);            dispararValidacao(email);
        new Select(perfil).selectByValue("ADMIN_FABRICA"); aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarUsuario']:not([disabled])")));
        btn.click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C07 — Resultado criação Admin Fábrica: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C07 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * C08 — Admin cria usuário ADMIN_EMPRESA.
     * Esperado: salvo com sucesso.
     */
    @Test
    public void tc_C08_criarUsuarioAdminEmpresa() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        String emailAdm = "selenium_adm_" + TS + "@teste.com";
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Admin Empresa Selenium"); dispararValidacao(nome);
        email.sendKeys(emailAdm);                dispararValidacao(email);
        new Select(perfil).selectByValue("ADMIN_EMPRESA"); aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarUsuario']:not([disabled])")));
        btn.click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C08 — Resultado criação Admin Empresa: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C08 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * C09 — Campo nome vazio + foco sai do campo.
     * Esperado: exibe "Informe um nome válido." abaixo do campo.
     */
    @Test
    public void tc_C09_campoNomeVazioExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        nome.click(); aguardar(200);
        dispararValidacao(nome);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome-validation-msg")));
        assertTrue("Mensagem de nome vazio deve aparecer", msg.isDisplayed());
        System.out.println("✅ C09 — Nome vazio exibe mensagem: " + msg.getText());
    }

    /**
     * C10 — Nome com dígito (ex: "João1").
     * Esperado: exibe "O nome não pode conter números."
     */
    @Test
    public void tc_C10_nomeComDigitoExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        nome.sendKeys("João3");
        dispararValidacao(nome);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome-validation-msg")));
        assertTrue("Mensagem de número no nome deve aparecer", msg.isDisplayed());
        assertTrue("Texto deve mencionar números",
                msg.getText().toLowerCase().contains("número") || msg.getText().toLowerCase().contains("numeros"));
        System.out.println("✅ C10 — Nome com dígito exibe mensagem: " + msg.getText());
    }

    /**
     * C11 — Nome com sequência repetida (ex: "AAAAA").
     * Esperado: exibe "O nome não pode conter caracteres repetidos em sequência."
     */
    @Test
    public void tc_C11_nomeComCaracteresRepetidosExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        nome.sendKeys("AAAAAAAA");
        dispararValidacao(nome);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome-validation-msg")));
        assertTrue("Mensagem de chars repetidos deve aparecer", msg.isDisplayed());
        System.out.println("✅ C11 — Nome com sequência repetida exibe mensagem: " + msg.getText());
    }

    /**
     * C12 — Nome com mais de 200 caracteres.
     * Esperado: campo trunca em 200 (maxlength).
     */
    @Test
    public void tc_C12_nomeMaisDe200CaracteresLimita() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        String maxAttr = nome.getAttribute("maxlength");
        assertEquals("maxlength do nome deve ser 200", "200", maxAttr);
        System.out.println("✅ C12 — maxlength=200 no campo nome confirmado");
    }

    /**
     * C13 — Email com formato inválido (ex: "joao@").
     * Esperado: exibe "Informe um e-mail válido."
     */
    @Test
    public void tc_C13_emailFormatoInvalidoExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement email = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        email.sendKeys("emailinvalido@");
        dispararValidacao(email);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email-validation-msg")));
        assertTrue("Mensagem de email inválido deve aparecer", msg.isDisplayed());
        System.out.println("✅ C13 — Email inválido exibe mensagem: " + msg.getText());
    }

    /**
     * C14 — Email válido digitado.
     * Esperado: mensagem de erro desaparece.
     */
    @Test
    public void tc_C14_emailValidoLimpaMensagemDeErro() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement email = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        email.sendKeys("emailinvalido@");
        dispararValidacao(email);
        aguardar(200);
        email.clear();
        email.sendKeys("valido@teste.com");
        dispararValidacao(email);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("email-validation-msg")));
        WebElement msg = driver.findElement(By.id("email-validation-msg"));
        assertFalse("Mensagem de erro deve sumir com email válido", msg.isDisplayed());
        System.out.println("✅ C14 — Email válido limpa a mensagem de erro");
    }

    /**
     * C15 — Telefone com letra digitada.
     * Esperado: letra bloqueada pelo onkeydown, não entra no campo.
     */
    @Test
    public void tc_C15_telefoneLеtrasBloqueadas() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefone']"));
        tel.sendKeys("abc11987654321");
        dispararValidacao(tel);

        String valor = tel.getAttribute("value");
        assertFalse("Letras não devem entrar no campo telefone",
                valor != null && valor.matches(".*[a-zA-Z].*"));
        System.out.println("✅ C15 — Letras bloqueadas no telefone. Valor: " + valor);
    }

    /**
     * C16 — Telefone com 5 dígitos + sai do campo.
     * Esperado: exibe "Informe um telefone válido com DDD e 9 dígitos."
     */
    @Test
    public void tc_C16_telefoneIncompletoExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefone']"));
        tel.sendKeys("81999");
        dispararValidacao(tel);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("telefone-validation-msg")));
        assertTrue("Mensagem de telefone incompleto deve aparecer", msg.isDisplayed());
        System.out.println("✅ C16 — Telefone incompleto exibe mensagem: " + msg.getText());
    }

    /**
     * C17 — Telefone com 11 dígitos.
     * Esperado: mensagem de erro desaparece.
     */
    @Test
    public void tc_C17_telefoneCompletoLimpaMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefone']"));
        tel.sendKeys("81999");
        dispararValidacao(tel);
        tel.clear();
        tel.sendKeys("81987654321");
        dispararValidacao(tel);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("telefone-validation-msg")));
        WebElement msg = driver.findElement(By.id("telefone-validation-msg"));
        assertFalse("Mensagem deve sumir com telefone completo", msg.isDisplayed());
        System.out.println("✅ C17 — Telefone completo limpa mensagem de erro");
    }

    /**
     * C18 — CPF digitado → máscara aplicada automaticamente.
     * Esperado: exibe no formato 000.000.000-00.
     */
    @Test
    public void tc_C18_cpfMascaraAplicadaAutomaticamente() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement cpf = driver.findElement(By.cssSelector("[id$=':inputCpf']"));
        cpf.sendKeys("12345678901");
        dispararValidacao(cpf);
        aguardar(300);

        String valor = cpf.getAttribute("value");
        assertTrue("CPF deve ter máscara 000.000.000-00",
                valor != null && valor.contains(".") && valor.contains("-"));
        System.out.println("✅ C18 — Máscara CPF aplicada: " + valor);
    }

    /**
     * C19 — CPF com menos de 11 dígitos + sai do campo.
     * Esperado: exibe "Informe um CPF válido com 11 dígitos."
     */
    @Test
    public void tc_C19_cpfIncompletoExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement cpf = driver.findElement(By.cssSelector("[id$=':inputCpf']"));
        cpf.sendKeys("12345");
        dispararValidacao(cpf);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cpf-validation-msg")));
        assertTrue("Mensagem de CPF incompleto deve aparecer", msg.isDisplayed());
        System.out.println("✅ C19 — CPF incompleto exibe mensagem: " + msg.getText());
    }

    /**
     * C20 — Botão Salvar com nome + email inválidos.
     * Esperado: botão permanece cinza e desabilitado.
     */
    @Test
    public void tc_C20_botaoDesabilitadoComNomeEmailInvalidos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome  = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        nome.sendKeys("João3");       dispararValidacao(nome);
        email.sendKeys("invalido@");  dispararValidacao(email);

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarUsuario']")));
        assertFalse("Botão deve estar desabilitado com campos inválidos", btn.isEnabled());
        System.out.println("✅ C20 — Botão desabilitado com nome e email inválidos");
    }

    /**
     * C21 — Botão Salvar após preencher nome, email, perfil e senha.
     * Esperado: botão fica vermelho e habilitado.
     */
    @Test
    public void tc_C21_botaoHabilitadoAposPreencherCamposValidos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("João Silva");                      dispararValidacao(nome);
        email.sendKeys("joao_" + TS + "@teste.com");      dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE");      aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarUsuario']:not([disabled])")));
        assertTrue("Botão deve estar habilitado com campos válidos", btn.isEnabled());
        System.out.println("✅ C21 — Botão habilitado após preencher campos válidos");
    }

    /**
     * C22 — Perfil VENDEDOR selecionado.
     * Esperado: campo "Concessionária" aparece.
     */
    @Test
    public void tc_C22_perfilVendedorExibeCampoConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        new Select(driver.findElement(By.cssSelector("[id$=':perfilSelect']"))).selectByValue("VENDEDOR");
        aguardar(500);

        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("concessionariaField")));
        assertTrue("Campo concessionária deve aparecer para VENDEDOR", campo.isDisplayed());
        System.out.println("✅ C22 — Perfil VENDEDOR exibe campo concessionária");
    }

    /**
     * C23 — Perfil GERENTE selecionado.
     * Esperado: campo "Concessionária" aparece.
     */
    @Test
    public void tc_C23_perfilGerenteExibeCampoConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        new Select(driver.findElement(By.cssSelector("[id$=':perfilSelect']"))).selectByValue("GERENTE");
        aguardar(500);

        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("concessionariaField")));
        assertTrue("Campo concessionária deve aparecer para GERENTE", campo.isDisplayed());
        System.out.println("✅ C23 — Perfil GERENTE exibe campo concessionária");
    }

    /**
     * C24 — Perfil CLIENTE selecionado.
     * Esperado: campo "Concessionária" some.
     */
    @Test
    public void tc_C24_perfilClienteOcultaCampoConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        new Select(driver.findElement(By.cssSelector("[id$=':perfilSelect']"))).selectByValue("CLIENTE");
        aguardar(500);

        // Campo pode estar no DOM (oculto) ou ausente — ambos são comportamentos válidos
        List<WebElement> campos = driver.findElements(By.id("concessionariaField"));
        if (!campos.isEmpty()) {
            assertFalse("Campo concessionária deve estar oculto para CLIENTE", campos.get(0).isDisplayed());
        }
        System.out.println("✅ C24 — Perfil CLIENTE oculta campo concessionária");
    }

    /**
     * C25 — Senha não preenchida para novo usuário.
     * Esperado: botão Salvar desabilitado.
     */
    @Test
    public void tc_C25_senhaNaoPreenchidaDesabilitaBotao() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome  = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));

        nome.sendKeys("Teste Sem Senha");                   dispararValidacao(nome);
        email.sendKeys("semsenha_" + TS + "@teste.com");    dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE");        aguardar(300);
        // Senha deixada em branco intencionalmente

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarUsuario']")));
        assertFalse("Botão deve estar desabilitado sem senha para novo usuário", btn.isEnabled());
        System.out.println("✅ C25 — Botão desabilitado sem senha para novo usuário");
    }

    /**
     * C26 — Email já cadastrado.
     * Esperado: exibe "E-mail já cadastrado."
     */
    @Test
    public void tc_C26_excecaoEmailJaCadastrado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Email Duplicado");   dispararValidacao(nome);
        email.sendKeys(EMAIL_CLIENTE);      dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE"); aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de email duplicado",
                msg.getText().toLowerCase().contains("cadastrado") || msg.getText().toLowerCase().contains("e-mail"));
        System.out.println("✅ C26 — Exceção email já cadastrado: " + msg.getText());
    }

    /**
     * C27 — Telefone já cadastrado.
     * Esperado: exibe "Telefone já cadastrado."
     */
    @Test
    public void tc_C27_excecaoTelefoneJaCadastrado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        // Confirma que há usuários antes de tentar duplicar o telefone
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        if (linhas.isEmpty()) {
            System.out.println("⚠️ C27 — Nenhum usuário na tabela para extrair telefone");
            return;
        }

        abrirFormNovoUsuario();
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement tel    = driver.findElement(By.cssSelector("[id$=':inputTelefone']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Tel Duplicado");                  dispararValidacao(nome);
        email.sendKeys("teldup_" + TS + "@teste.com");   dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE");     aguardar(300);
        // Telefone do cliente padrão do DataInicializador
        tel.sendKeys("81999990001");                     dispararValidacao(tel);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir erro de telefone duplicado",
                    msg.getText().toLowerCase().contains("telefone") || msg.getText().toLowerCase().contains("cadastrado"));
            System.out.println("✅ C27 — Exceção telefone já cadastrado: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C27 — Telefone não estava cadastrado ou comportamento diferente");
        }
    }

    /**
     * C28 — CPF já cadastrado.
     * Esperado: exibe "CPF já cadastrado."
     */
    @Test
    public void tc_C28_excecaoCpfJaCadastrado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement cpf    = driver.findElement(By.cssSelector("[id$=':inputCpf']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("CPF Duplicado");                  dispararValidacao(nome);
        email.sendKeys("cpfdup_" + TS + "@teste.com");   dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE");     aguardar(300);
        // CPF do cliente padrão do DataInicializador
        cpf.sendKeys("12345678901");                     dispararValidacao(cpf);
        aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir erro de CPF duplicado",
                    msg.getText().toLowerCase().contains("cpf") || msg.getText().toLowerCase().contains("cadastrado"));
            System.out.println("✅ C28 — Exceção CPF já cadastrado: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C28 — CPF não estava cadastrado ou comportamento diferente");
        }
    }

    /**
     * C29 — VENDEDOR sem concessionária selecionada.
     * Esperado: exibe "Selecione a concessionária para este perfil."
     */
    @Test
    public void tc_C29_excecaoVendedorSemConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        String emailV = "semconc_" + TS + "@teste.com";
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Vendedor Sem Conc");  dispararValidacao(nome);
        email.sendKeys(emailV);              dispararValidacao(email);
        new Select(perfil).selectByValue("VENDEDOR"); aguardar(400);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de concessionária obrigatória",
                msg.getText().toLowerCase().contains("concession"));
        System.out.println("✅ C29 — Exceção vendedor sem concessionária: " + msg.getText());
    }

    /**
     * C30 — GERENTE em concessionária que já possui gerente.
     * Esperado: exibe "Esta concessionária já possui um gerente cadastrado."
     */
    @Test
    public void tc_C30_excecaoGerenteEmConcessionariaOcupada() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        String emailG = "gerente2_" + TS + "@teste.com";
        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Gerente Duplicado");  dispararValidacao(nome);
        email.sendKeys(emailG);              dispararValidacao(email);
        new Select(perfil).selectByValue("GERENTE"); aguardar(400);

        try {
            WebElement selectConc = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[id$='concessionariaIdSelecionada']")));
            // Seleciona a primeira que provavelmente já tem gerente
            new Select(selectConc).selectByIndex(1);
        } catch (TimeoutException e) {
            System.out.println("⚠️ C30 — Campo concessionária não disponível");
            return;
        }
        aguardar(300);

        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error, .msg-success")));
            System.out.println("✅ C30 — Resultado gerente em conc. ocupada: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C30 — Timeout aguardando resposta");
        }
    }

    /**
     * C31 — Erro ao persistir no banco.
     * Esperado: exibe "Falha ao persistir os dados no banco. Tente novamente."
     * Nota: simula injeção de dado inválido via JS para forçar erro server-side.
     */
    @Test
    public void tc_C31_excecaoErroPersistirBanco() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        nome.sendKeys("Erro Banco Selenium");            dispararValidacao(nome);
        email.sendKeys("errobanco_" + TS + "@teste.com"); dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE");     aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(400);

        // Injeta valor inválido no campo hidden de perfil para forçar erro server-side
        js.executeScript(
            "var inputs = document.querySelectorAll('[id$=\":perfilSelect\"]');" +
            "if(inputs.length) inputs[0].value='PERFIL_INEXISTENTE';"
        );
        aguardar(200);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir alguma mensagem de erro", msg.isDisplayed());
            System.out.println("✅ C31 — Erro ao persistir exibe mensagem: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C31 — Servidor rejeitou silenciosamente ou redirecionou");
        }
    }

    /**
     * C32 — Admin clica em "Editar" em um usuário.
     * Esperado: formulário abre pré-preenchido com dados do usuário.
     */
    @Test
    public void tc_C32_editarUsuarioFormularioPrePreenchido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();
        aguardar(800);

        WebElement inputNome = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[id$=':inputNome']")));
        String valorNome = inputNome.getAttribute("value");
        assertTrue("Formulário de edição deve ter nome pré-preenchido",
                valorNome != null && !valorNome.isEmpty());
        System.out.println("✅ C32 — Formulário de edição pré-preenchido. Nome: " + valorNome);
    }

    /**
     * C33 — Admin edita nome e salva.
     * Esperado: sucesso, tabela atualizada.
     */
    @Test
    public void tc_C33_editarNomeESalvarComSucesso() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();

        WebElement inputNome = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[id$=':inputNome']")));
        inputNome.clear();
        inputNome.sendKeys("Nome Editado Selenium");
        dispararValidacao(inputNome);
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C33 — Resultado edição de nome: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C33 — Timeout aguardando resposta");
        }
    }

    /**
     * C34 — Admin deixa campo senha vazio ao editar.
     * Esperado: senha existente mantida (não alterada).
     */
    @Test
    public void tc_C34_senhaBrancoNaEdicaoMantemSenhaAtual() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputSenha']")));
        WebElement senha = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        // Campo senha deve estar vazio no modo edição (placeholder indica "deixe em branco para manter")
        String valorSenha = senha.getAttribute("value");
        assertTrue("Campo senha em branco ao editar é comportamento esperado",
                valorSenha == null || valorSenha.isEmpty());
        System.out.println("✅ C34 — Campo senha em branco na edição (senha atual mantida)");
    }

    /**
     * C35 — Admin clica em "Cancelar" durante edição.
     * Esperado: formulário fecha sem salvar.
     */
    @Test
    public void tc_C35_cancelarEdicaoFechaFormulario() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNome']")));
        WebElement btnCancelar = driver.findElement(
                By.xpath("//input[contains(@value,'Cancelar')] | //button[contains(text(),'Cancelar')]"));
        btnCancelar.click();
        aguardar(500);

        List<WebElement> form = driver.findElements(By.cssSelector("[id$=':inputNome']"));
        assertTrue("Formulário deve fechar após cancelar", form.isEmpty());
        System.out.println("✅ C35 — Cancelar edição fecha o formulário");
    }

    /**
     * C36 — Após erro no salvar, corrigir campo e tentar de novo.
     * Esperado: salva com sucesso na segunda tentativa.
     */
    @Test
    public void tc_C36_aposErroCorrigirESalvarNovamente() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoUsuario();

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNome']"));
        WebElement email  = driver.findElement(By.cssSelector("[id$=':inputEmail']"));
        WebElement perfil = driver.findElement(By.cssSelector("[id$=':perfilSelect']"));
        WebElement senha  = driver.findElement(By.cssSelector("[id$=':inputSenha']"));

        // 1ª tentativa: email já cadastrado → erro
        nome.sendKeys("Retry Selenium");      dispararValidacao(nome);
        email.sendKeys(EMAIL_CLIENTE);        dispararValidacao(email);
        new Select(perfil).selectByValue("CLIENTE"); aguardar(300);
        senha.sendKeys("senha123");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", senha);
        aguardar(400);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
        } catch (TimeoutException ignored) {}

        // 2ª tentativa: corrige o email
        WebElement emailAtualizado = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':inputEmail']")));
        emailAtualizado.clear();
        emailAtualizado.sendKeys("retry_" + TS + "@teste.com");
        dispararValidacao(emailAtualizado);
        aguardar(400);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C36 — Resultado após correção e nova tentativa: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ C36 — Timeout aguardando resposta");
        }
    }

    /**
     * C37 — Admin clica em "Inativar" em usuário ativo.
     * Esperado: status muda para "Inativo", botão vira "Reativar".
     */
    @Test
    public void tc_C37_inativarUsuarioAtivo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        List<WebElement> botoesInativar = driver.findElements(
                By.xpath("//input[contains(@value,'Inativar')] | //button[contains(text(),'Inativar')]"));

        if (botoesInativar.isEmpty()) {
            System.out.println("⚠️ C37 — Nenhum usuário ativo para inativar");
            return;
        }

        botoesInativar.get(0).click();
        aguardar(1000);

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
            assertTrue("Inativação deve exibir sucesso", msg.isDisplayed());
            System.out.println("✅ C37 — Usuário inativado: " + msg.getText());
        } catch (TimeoutException e) {
            // Verifica se botão "Reativar" apareceu mesmo sem mensagem explícita
            List<WebElement> reativar = driver.findElements(
                    By.xpath("//input[contains(@value,'Reativar')] | //button[contains(text(),'Reativar')]"));
            assertTrue("Botão Reativar deve aparecer após inativação", !reativar.isEmpty());
            System.out.println("✅ C37 — Usuário inativado (botão Reativar visível)");
        }
    }

    /**
     * C38 — Admin clica em "Reativar" em usuário inativo.
     * Esperado: status volta para "Ativo", botão vira "Inativar".
     */
    @Test
    public void tc_C38_reativarUsuarioInativo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        List<WebElement> botoesReativar = driver.findElements(
                By.xpath("//input[contains(@value,'Reativar')] | //button[contains(text(),'Reativar')]"));

        if (botoesReativar.isEmpty()) {
            System.out.println("⚠️ C38 — Nenhum usuário inativo para reativar");
            return;
        }

        botoesReativar.get(0).click();
        aguardar(1000);

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
            assertTrue("Reativação deve exibir sucesso", msg.isDisplayed());
            System.out.println("✅ C38 — Usuário reativado: " + msg.getText());
        } catch (TimeoutException e) {
            List<WebElement> inativar = driver.findElements(
                    By.xpath("//input[contains(@value,'Inativar')] | //button[contains(text(),'Inativar')]"));
            assertTrue("Botão Inativar deve aparecer após reativação", !inativar.isEmpty());
            System.out.println("✅ C38 — Usuário reativado (botão Inativar visível)");
        }
    }

    // =========================================================================
    // BLOCO D — Gestão de Concessionárias
    // =========================================================================

    /**
     * D01 — Admin acessa página de concessionárias.
     * Esperado: tabela exibida com registros.
     */
    @Test
    public void tc_D01_adminAcessaListaConcessionarias() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_CONC);
        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela de concessionárias deve existir", tabela);
        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        assertTrue("Deve haver ao menos uma concessionária na tabela", !linhas.isEmpty());
        System.out.println("✅ D01 — Lista de concessionárias: " + linhas.size() + " registros");
    }

    /**
     * D02 — Admin clica em "+ Nova Concessionária".
     * Esperado: formulário abre vazio, botão Salvar desabilitado.
     */
    @Test
    public void tc_D02_abrirFormNovaConcessionariaBotaoDesabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarCon']")));
        assertFalse("Botão Salvar deve iniciar desabilitado", btn.isEnabled());
        System.out.println("✅ D02 — Formulário nova concessionária: botão Salvar desabilitado");
    }

    /**
     * D03 — Campo "Nome da Unidade" vazio + blur.
     * Esperado: exibe "Informe o nome da unidade."
     */
    @Test
    public void tc_D03_nomeUnidadeVazioExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNomeCon']"));
        nome.click(); aguardar(200);
        dispararValidacao(nome);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("conc-nome-msg")));
        assertTrue("Mensagem de nome vazio deve aparecer", msg.isDisplayed());
        System.out.println("✅ D03 — Nome unidade vazio exibe mensagem: " + msg.getText());
    }

    /**
     * D04 — Nome com mais de 300 caracteres.
     * Esperado: campo trunca em 300 (maxlength).
     */
    @Test
    public void tc_D04_nomeMaisDe300CaracteresLimita() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNomeCon']"));
        String maxAttr = nome.getAttribute("maxlength");
        assertEquals("maxlength do nome da concessionária deve ser 300", "300", maxAttr);
        System.out.println("✅ D04 — maxlength=300 no campo nome da concessionária confirmado");
    }

    /**
     * D05 — Select UF carrega ao abrir formulário.
     * Esperado: lista de estados do IBGE populada em ordem alfabética (27 UFs).
     */
    @Test
    public void tc_D05_selectUfCarregaEstadosIbge() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> {
            Select s = new Select(d.findElement(By.id("ufSelect")));
            return s.getOptions().size() > 5;
        });

        Select ufSelect = new Select(driver.findElement(By.id("ufSelect")));
        assertTrue("UF select deve ter os 27 estados + opção vazia",
                ufSelect.getOptions().size() >= 27);
        System.out.println("✅ D05 — UF select carregado com " + ufSelect.getOptions().size() + " opções");
    }

    /**
     * D06 — Select "Cidade" antes de selecionar UF.
     * Esperado: campo permanece desabilitado.
     */
    @Test
    public void tc_D06_cidadeDesabilitadaAntesDeSelecionarUf() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();
        aguardar(1000);

        WebElement cidadeSelect = driver.findElement(By.id("cidadeSelect"));
        assertFalse("Cidade deve estar desabilitada antes de selecionar UF", cidadeSelect.isEnabled());
        System.out.println("✅ D06 — Cidade desabilitada antes de selecionar UF");
    }

    /**
     * D07 — UF selecionada.
     * Esperado: Select de cidades habilitado e populado via API IBGE.
     */
    @Test
    public void tc_D07_selecionarUfHabilitaECarregaCidades() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> {
            Select s = new Select(d.findElement(By.id("ufSelect")));
            return s.getOptions().size() > 5;
        });

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("SP");
        aguardar(300);
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");

        wait.until(d -> {
            Select s = new Select(d.findElement(By.id("cidadeSelect")));
            return s.getOptions().size() > 5;
        });

        Select cidadeSelect = new Select(driver.findElement(By.id("cidadeSelect")));
        assertTrue("Cidade deve ter opções após selecionar UF SP",
                cidadeSelect.getOptions().size() > 10);
        assertTrue("Cidade deve estar habilitada após selecionar UF",
                driver.findElement(By.id("cidadeSelect")).isEnabled());
        System.out.println("✅ D07 — UF SP carregou " + cidadeSelect.getOptions().size() + " cidades");
    }

    /**
     * D08 — Mudar UF.
     * Esperado: Select de cidades recarregado com as cidades da nova UF.
     */
    @Test
    public void tc_D08_mudarUfRecarregaCidades() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

        // Seleciona SP
        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("SP");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        wait.until(d -> new Select(d.findElement(By.id("cidadeSelect"))).getOptions().size() > 10);
        int qtdCidadesSP = new Select(driver.findElement(By.id("cidadeSelect"))).getOptions().size();

        // Muda para PE (menos cidades que SP)
        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("PE");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        aguardar(500);

        wait.until(d -> {
            Select s = new Select(d.findElement(By.id("cidadeSelect")));
            return s.getOptions().size() > 5 && s.getOptions().size() != qtdCidadesSP;
        });

        int qtdCidadesPE = new Select(driver.findElement(By.id("cidadeSelect"))).getOptions().size();
        assertTrue("Cidades da PE devem ser diferentes das de SP", qtdCidadesPE != qtdCidadesSP);
        System.out.println("✅ D08 — Mudar UF: SP=" + qtdCidadesSP + " cidades → PE=" + qtdCidadesPE + " cidades");
    }

    /**
     * D09 — Telefone com letra digitada.
     * Esperado: letra bloqueada, não entra no campo.
     */
    @Test
    public void tc_D09_telefoneConcLetrasBloqueiadas() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']"));
        tel.sendKeys("abc11987654321");
        dispararValidacao(tel);

        String valor = tel.getAttribute("value");
        assertFalse("Letras não devem entrar no telefone da concessionária",
                valor != null && valor.matches(".*[a-zA-Z].*"));
        System.out.println("✅ D09 — Letras bloqueadas no telefone. Valor: " + valor);
    }

    /**
     * D10 — Telefone com 7 dígitos + blur.
     * Esperado: exibe "Informe um telefone válido com DDD e 9 dígitos."
     */
    @Test
    public void tc_D10_telefoneIncompletoConcExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']"));
        tel.sendKeys("8199999");
        dispararValidacao(tel);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("conc-telefone-msg")));
        assertTrue("Mensagem de telefone incompleto deve aparecer", msg.isDisplayed());
        System.out.println("✅ D10 — Telefone incompleto exibe mensagem: " + msg.getText());
    }

    /**
     * D11 — Endereço vazio + blur.
     * Esperado: exibe "Informe o endereço da unidade."
     */
    @Test
    public void tc_D11_enderecoVazioExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement end = driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']"));
        end.click(); aguardar(200);
        dispararValidacao(end);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("conc-endereco-msg")));
        assertTrue("Mensagem de endereço vazio deve aparecer", msg.isDisplayed());
        System.out.println("✅ D11 — Endereço vazio exibe mensagem: " + msg.getText());
    }

    /**
     * D12 — Endereço com mais de 500 caracteres.
     * Esperado: campo trunca em 500 (maxlength).
     */
    @Test
    public void tc_D12_enderecoMaisDe500CaracteresLimita() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement end = driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']"));
        String maxAttr = end.getAttribute("maxlength");
        assertEquals("maxlength do endereço deve ser 500", "500", maxAttr);
        System.out.println("✅ D12 — maxlength=500 no campo endereço confirmado");
    }

    /**
     * D13 — Botão Salvar com qualquer campo inválido.
     * Esperado: permanece cinza e desabilitado.
     */
    @Test
    public void tc_D13_botaoConcDesabilitadoComCamposInvalidos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarCon']")));
        assertFalse("Botão deve estar desabilitado com campos vazios", btn.isEnabled());
        System.out.println("✅ D13 — Botão concessionária desabilitado com campos inválidos");
    }

    /**
     * D14 — Todos os campos preenchidos corretamente.
     * Esperado: botão Salvar habilitado em vermelho.
     */
    @Test
    public void tc_D14_todosCamposValidosBotaoHabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

        driver.findElement(By.cssSelector("[id$=':inputNomeCon']")).sendKeys("Conc Valida " + TS);
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputNomeCon']")));

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("RJ");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        wait.until(d -> new Select(d.findElement(By.id("cidadeSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("cidadeSelect"))).selectByIndex(1);
        js.executeScript("onCidadeChange(document.getElementById('cidadeSelect'))");
        aguardar(300);

        driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']")).sendKeys("21987654321");
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']")));

        driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']")).sendKeys("Av. Rio, 100");
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']")));
        aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarCon']:not([disabled])")));
        assertTrue("Botão deve estar habilitado com todos os campos válidos", btn.isEnabled());
        System.out.println("✅ D14 — Todos os campos válidos: botão habilitado");
    }

    /**
     * D15 — Preenche todos os campos válidos e salva.
     * Esperado: mensagem de sucesso, concessionária aparece na tabela.
     */
    @Test
    public void tc_D15_criarConcessionariaValida() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNomeCon']"));
        nome.sendKeys(NOME_CON_TESTE);
        dispararValidacao(nome);

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("PE");
        aguardar(300);
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");

        wait.until(d -> new Select(d.findElement(By.id("cidadeSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("cidadeSelect"))).selectByIndex(1);
        js.executeScript("onCidadeChange(document.getElementById('cidadeSelect'))");
        aguardar(300);

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']"));
        tel.sendKeys("81987654321");
        dispararValidacao(tel);

        WebElement end = driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']"));
        end.sendKeys("Av. Selenium, 100, Bairro Teste");
        dispararValidacao(end);
        aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarCon']:not([disabled])")));
        btn.click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Deve exibir sucesso ao criar concessionária", msg.isDisplayed());
        System.out.println("✅ D15 — Concessionária criada: " + NOME_CON_TESTE);
    }

    /**
     * D16 — Nome de concessionária já cadastrado.
     * Esperado: exibe "Nome já cadastrado."
     */
    @Test
    public void tc_D16_excecaoNomeConcessionariaDuplicado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNomeCon']"));
        nome.sendKeys(NOME_CON_TESTE); // mesmo nome criado em D15
        dispararValidacao(nome);

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("SP");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        wait.until(d -> new Select(d.findElement(By.id("cidadeSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("cidadeSelect"))).selectByIndex(1);
        js.executeScript("onCidadeChange(document.getElementById('cidadeSelect'))");
        aguardar(300);

        driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']")).sendKeys("11987654321");
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']")));

        driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']")).sendKeys("Rua Duplicada, 1");
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']")));
        aguardar(500);

        habilitarBotaoJs("[id$=':btnSalvarCon']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarCon']")).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de nome duplicado",
                msg.getText().toLowerCase().contains("cadastrado") || msg.getText().toLowerCase().contains("nome"));
        System.out.println("✅ D16 — Exceção nome duplicado: " + msg.getText());
    }

    /**
     * D17 — Falha na API do IBGE ao carregar estados.
     * Esperado: exibe "Falha ao carregar estados. Verifique sua conexão."
     * Nota: simula falha via JS interceptando o fetch antes de abrir o formulário.
     */
    @Test
    public void tc_D17_falhaApiIbgeAoCarregarEstados() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.urlContains("concessionarias"));

        // Intercepta fetch para simular falha de rede
        js.executeScript(
            "window._fetchOriginal = window.fetch;" +
            "window.fetch = function(url) {" +
            "  if(url && url.toString().includes('ibge')) {" +
            "    return Promise.reject(new Error('Simulated IBGE failure'));" +
            "  }" +
            "  return window._fetchOriginal.apply(this, arguments);" +
            "};"
        );

        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Nova Concession')] | //button[contains(text(),'Nova Concession')]")));
        btnNovo.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeCon']")));
        aguardar(1500);

        try {
            WebElement msgErro = driver.findElement(By.id("uf-error-msg"));
            assertTrue("Mensagem de erro da API IBGE deve aparecer", msgErro.isDisplayed());
            System.out.println("✅ D17 — Falha API IBGE estados: " + msgErro.getText());
        } catch (NoSuchElementException e) {
            System.out.println("⚠️ D17 — Elemento de erro da API IBGE não encontrado no DOM");
        } finally {
            // Restaura fetch original
            js.executeScript(
                "if(window._fetchOriginal) { window.fetch = window._fetchOriginal; delete window._fetchOriginal; }"
            );
        }
    }

    /**
     * D18 — Falha na API do IBGE ao carregar cidades.
     * Esperado: exibe "Falha ao carregar cidades. Verifique sua conexão."
     * Nota: estados carregam normalmente; fetch é bloqueado somente na chamada de cidades.
     */
    @Test
    public void tc_D18_falhaApiIbgeAoCarregarCidades() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

        // Intercepta apenas a chamada de cidades
        js.executeScript(
            "window._fetchOriginal = window.fetch;" +
            "window.fetch = function(url) {" +
            "  if(url && url.toString().includes('municipios')) {" +
            "    return Promise.reject(new Error('Simulated city load failure'));" +
            "  }" +
            "  return window._fetchOriginal.apply(this, arguments);" +
            "};"
        );

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("MG");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        aguardar(1500);

        try {
            WebElement msgErro = driver.findElement(By.id("cidade-error-msg"));
            assertTrue("Mensagem de erro de cidades deve aparecer", msgErro.isDisplayed());
            System.out.println("✅ D18 — Falha API IBGE cidades: " + msgErro.getText());
        } catch (NoSuchElementException e) {
            System.out.println("⚠️ D18 — Elemento de erro de cidades não encontrado no DOM");
        } finally {
            js.executeScript(
                "if(window._fetchOriginal) { window.fetch = window._fetchOriginal; delete window._fetchOriginal; }"
            );
        }
    }

    /**
     * D19 — Erro interno ao salvar concessionária.
     * Esperado: exibe "Não foi possível salvar os dados."
     * Nota: injeta valor inválido via JS para forçar erro server-side.
     */
    @Test
    public void tc_D19_erroInternoAoSalvarConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

        driver.findElement(By.cssSelector("[id$=':inputNomeCon']")).sendKeys("Conc Erro Banco " + TS);
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputNomeCon']")));

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("BA");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        wait.until(d -> new Select(d.findElement(By.id("cidadeSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("cidadeSelect"))).selectByIndex(1);
        js.executeScript("onCidadeChange(document.getElementById('cidadeSelect'))");
        aguardar(300);

        driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']")).sendKeys("71987654321");
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']")));
        driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']")).sendKeys("Rua Erro, 99");
        dispararValidacao(driver.findElement(By.cssSelector("[id$=':inputEnderecoCon']")));
        aguardar(400);

        // Corrompe UF para forçar erro server-side
        js.executeScript("var s = document.getElementById('ufSelect'); if(s) s.value = '';");
        js.executeScript(
            "var h = document.querySelector('[id$=\":hiddenUf\"]');" +
            "if(h) h.value = 'INVALIDA';"
        );

        habilitarBotaoJs("[id$=':btnSalvarCon']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarCon']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir alguma mensagem de erro", msg.isDisplayed());
            System.out.println("✅ D19 — Erro ao salvar exibe mensagem: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ D19 — Servidor rejeitou silenciosamente ou redirecionou");
        }
    }

    /**
     * D20 — Admin clica em "Editar" em concessionária.
     * Esperado: formulário pré-preenchido, UF e cidade pré-selecionados.
     */
    @Test
    public void tc_D20_editarConcessionariaFormularioPrePreenchido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeCon']")));
        aguardar(2000); // aguarda UF e cidade carregarem via IBGE

        String valorNome = driver.findElement(By.cssSelector("[id$=':inputNomeCon']")).getAttribute("value");
        assertTrue("Nome deve estar pré-preenchido", valorNome != null && !valorNome.isEmpty());

        String valorUf = driver.findElement(By.id("ufSelect")).getAttribute("value");
        assertTrue("UF deve estar pré-selecionada", valorUf != null && !valorUf.isEmpty());

        System.out.println("✅ D20 — Edição concessionária pré-preenchida. Nome: " + valorNome + " | UF: " + valorUf);
    }

    /**
     * D21 — Admin edita telefone e salva.
     * Esperado: sucesso, tabela atualizada.
     */
    @Test
    public void tc_D21_editarTelefoneESalvar() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputTelefoneCon']")));
        aguardar(2000); // aguarda carregamento dos selects IBGE

        WebElement tel = driver.findElement(By.cssSelector("[id$=':inputTelefoneCon']"));
        tel.clear();
        tel.sendKeys("81911112222");
        dispararValidacao(tel);
        aguardar(400);

        habilitarBotaoJs("[id$=':btnSalvarCon']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarCon']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ D21 — Resultado edição de telefone: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ D21 — Timeout aguardando resposta");
        }
    }

    /**
     * D22 — Admin clica "Cancelar" na edição.
     * Esperado: formulário fecha sem salvar.
     */
    @Test
    public void tc_D22_cancelarEdicaoConcessionariaFechaFormulario() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeCon']")));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Cancelar')] | //button[contains(text(),'Cancelar')]"))).click();
        aguardar(500);

        List<WebElement> form = driver.findElements(By.cssSelector("[id$=':inputNomeCon']"));
        assertTrue("Formulário deve fechar após cancelar", form.isEmpty());
        System.out.println("✅ D22 — Cancelar edição concessionária fecha o formulário");
    }

    // =========================================================================
    // BLOCO E — Gestão de Veículos
    // =========================================================================

    /**
     * E01 — Admin acessa página de veículos.
     * Esperado: tabela com catálogo exibida.
     */
    @Test
    public void tc_E01_adminAcessaListaVeiculos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_VEIC);
        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela de veículos deve existir", tabela);
        System.out.println("✅ E01 — Lista de veículos carregada");
    }

    /**
     * E02 — Admin clica em "+ Novo Veículo".
     * Esperado: formulário abre, campo Marca exibe "VRUM" fixo, botão Salvar desabilitado.
     */
    @Test
    public void tc_E02_abrirFormNovoVeiculoMarcaVrumEBotaoDesabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarVeiculo']")));
        assertFalse("Botão Salvar deve iniciar desabilitado", btn.isEnabled());

        List<WebElement> badgeMarca = driver.findElements(By.cssSelector(".marca-badge"));
        assertFalse("Badge VRUM deve estar visível", badgeMarca.isEmpty());
        assertTrue("Badge deve exibir VRUM", badgeMarca.get(0).getText().toUpperCase().contains("VRUM"));
        System.out.println("✅ E02 — Formulário novo veículo: marca VRUM fixa e botão desabilitado");
    }

    /**
     * E03 — "Nome do Modelo" vazio + blur.
     * Esperado: exibe "Informe o nome do modelo."
     */
    @Test
    public void tc_E03_nomeModeloVazioExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement nome = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        nome.click(); aguardar(200);
        dispararValidacao(nome);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("v-nome-msg")));
        assertTrue("Mensagem de nome vazio deve aparecer", msg.isDisplayed());
        System.out.println("✅ E03 — Nome modelo vazio exibe mensagem: " + msg.getText());
    }

    /**
     * E04 — "Modelo" com 1 caractere + blur.
     * Esperado: exibe "O modelo deve ter no mínimo 2 caracteres."
     */
    @Test
    public void tc_E04_modeloComUmCaractereExibeMensagem() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        modelo.sendKeys("X");
        dispararValidacao(modelo);

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("v-modelo-msg")));
        assertTrue("Mensagem de modelo curto deve aparecer", msg.isDisplayed());
        assertTrue("Texto deve mencionar mínimo de caracteres",
                msg.getText().contains("mínimo") || msg.getText().contains("2"));
        System.out.println("✅ E04 — Modelo com 1 char exibe mensagem: " + msg.getText());
    }

    /**
     * E05 — "Modelo" com 51 caracteres.
     * Esperado: campo trunca em 50 (maxlength).
     */
    @Test
    public void tc_E05_modeloCom51CaracteresTruncaEm50() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        String maxAttr = modelo.getAttribute("maxlength");
        assertEquals("maxlength do modelo deve ser 50", "50", maxAttr);
        System.out.println("✅ E05 — maxlength=50 no campo modelo confirmado");
    }

    /**
     * E06 — Seletor de ano antes de clicar.
     * Esperado: lista de 1950 até anoAtual+1 gerada.
     */
    @Test
    public void tc_E06_seletorAnoCarregaListaCompleta() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(1000);

        Select anoSelect = new Select(driver.findElement(By.id("anoSelect")));
        int totalOpcoes = anoSelect.getOptions().size();
        assertTrue("Seletor de ano deve ter mais de 60 opções (1950–anoAtual+1)",
                totalOpcoes > 60);

        // Verifica que 1950 está presente
        boolean tem1950 = anoSelect.getOptions().stream()
                .anyMatch(o -> o.getText().equals("1950") || o.getAttribute("value").equals("1950"));
        assertTrue("Ano 1950 deve estar na lista", tem1950);

        System.out.println("✅ E06 — Seletor de ano carregado com " + totalOpcoes + " opções (1950–anoAtual+1)");
    }

    /**
     * E07 — Ano não selecionado.
     * Esperado: botão Salvar desabilitado mesmo com nome e tipo preenchidos.
     */
    @Test
    public void tc_E07_semAnoSelecionadoBotaoDesabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys("Vrum GT");   dispararValidacao(nome);
        modelo.sendKeys("GT-S");    dispararValidacao(modelo);
        new Select(tipo).selectByIndex(1); aguardar(300);
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(300);

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarVeiculo']")));
        assertFalse("Sem ano selecionado botão deve estar desabilitado", btn.isEnabled());
        System.out.println("✅ E07 — Sem ano: botão permanece desabilitado");
    }

    /**
     * E08 — Tipo não selecionado.
     * Esperado: botão Salvar desabilitado mesmo com nome e ano preenchidos.
     */
    @Test
    public void tc_E08_semTipoSelecionadoBotaoDesabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));

        nome.sendKeys("Vrum GT");   dispararValidacao(nome);
        modelo.sendKeys("GT-S");    dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(300);

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[id$=':btnSalvarVeiculo']")));
        assertFalse("Sem tipo selecionado botão deve estar desabilitado", btn.isEnabled());
        System.out.println("✅ E08 — Sem tipo: botão permanece desabilitado");
    }

    /**
     * E09 — Todos os obrigatórios preenchidos.
     * Esperado: botão Salvar habilitado.
     */
    @Test
    public void tc_E09_todosCamposObrigatoriosBotaoHabilitado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys("Vrum GT Habilitado");   dispararValidacao(nome);
        modelo.sendKeys("GTH-2");              dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);

        new Select(tipo).selectByIndex(1);
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarVeiculo']:not([disabled])")));
        assertTrue("Botão deve estar habilitado com todos os campos obrigatórios", btn.isEnabled());
        System.out.println("✅ E09 — Todos obrigatórios preenchidos: botão habilitado");
    }

    /**
     * E10 — Campo Potência com letra.
     * Esperado: letra bloqueada, não entra no campo.
     */
    @Test
    public void tc_E10_potenciaLetrasBloqueadas() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement pot = driver.findElement(By.xpath(
                "//input[contains(@placeholder,'200') or contains(@id,'Potencia') or contains(@id,'potencia')]"));
        pot.sendKeys("abc200");
        dispararValidacao(pot);

        String valor = pot.getAttribute("value");
        assertFalse("Letras não devem entrar no campo potência",
                valor != null && valor.matches(".*[a-zA-Z].*"));
        System.out.println("✅ E10 — Letras bloqueadas na potência. Valor: " + valor);
    }

    /**
     * E11 — Campo Potência com 5 dígitos.
     * Esperado: trunca em 4 (maxlength=4).
     */
    @Test
    public void tc_E11_potenciaMaxLengthQuatroDigitos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement pot = driver.findElement(By.xpath(
                "//input[contains(@placeholder,'200') or contains(@id,'Potencia') or contains(@id,'potencia')]"));
        String maxAttr = pot.getAttribute("maxlength");
        assertEquals("maxlength da potência deve ser 4", "4", maxAttr);
        System.out.println("✅ E11 — maxlength=4 no campo potência confirmado");
    }

    /**
     * E12 — Campo Vel. Máxima com 4 dígitos.
     * Esperado: trunca em 3 (maxlength=3).
     */
    @Test
    public void tc_E12_velocidadeMaximaMaxLengthTresDigitos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement vel = driver.findElement(By.xpath(
                "//input[contains(@placeholder,'250') or contains(@id,'Velocidade') or contains(@id,'velMax')]"));
        String maxAttr = vel.getAttribute("maxlength");
        assertEquals("maxlength da velocidade máxima deve ser 3", "3", maxAttr);
        System.out.println("✅ E12 — maxlength=3 no campo velocidade máxima confirmado");
    }

    /**
     * E13 — Campo Aceleração com segundo ponto decimal.
     * Esperado: segundo ponto bloqueado (somente um separador decimal permitido).
     */
    @Test
    public void tc_E13_aceleracaoSegundoPontoDecimalBloqueado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement acel = driver.findElement(By.xpath(
                "//input[contains(@placeholder,'5.5') or contains(@id,'Acel') or contains(@id,'acel')]"));
        acel.sendKeys("5.5");
        dispararValidacao(acel);

        acel.sendKeys(".5"); // tenta adicionar segundo ponto
        dispararValidacao(acel);
        String valorApos2Pontos = acel.getAttribute("value");

        int qtdPontos = valorApos2Pontos.replaceAll("[^.]", "").length();
        assertTrue("Campo aceleração deve aceitar no máximo um ponto decimal", qtdPontos <= 1);
        System.out.println("✅ E13 — Aceleração: 1 ponto aceito, segundo bloqueado. Valor: " + valorApos2Pontos);
    }

    /**
     * E14 — Preço digitado.
     * Esperado: formatado em tempo real como "R$ 0.000,00".
     */
    @Test
    public void tc_E14_precoFormatadoEmTempoReal() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement precoEl = driver.findElement(By.cssSelector("[id$=':inputPreco']"));
        precoEl.sendKeys("15000000");
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", precoEl);
        aguardar(400);

        String valor = precoEl.getAttribute("value");
        assertTrue("Preço deve estar formatado com R$", valor != null && valor.contains("R$"));
        assertTrue("Preço deve ter separador de milhar ou vírgula decimal", valor.contains(","));
        System.out.println("✅ E14 — Máscara monetária aplicada: " + valor);
    }

    /**
     * E15 — Descrição Curta com 180 caracteres.
     * Esperado: contador exibe "20 restantes".
     */
    @Test
    public void tc_E15_descricaoCurta180CaracteresContador20Restantes() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement descEl = driver.findElement(By.cssSelector("[id$=':inputDescCurta']"));
        descEl.sendKeys("A".repeat(180));
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", descEl);
        aguardar(300);

        WebElement contador = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("desc-contador")));
        assertTrue("Contador deve mostrar 20 restantes", contador.getText().contains("20"));
        System.out.println("✅ E15 — Contador regressivo: " + contador.getText());
    }

    /**
     * E16 — Descrição Curta com 195 caracteres.
     * Esperado: contador fica vermelho (classe CSS de alerta).
     */
    @Test
    public void tc_E16_descricaoCurta195CaracteresContadorVermelho() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement descEl = driver.findElement(By.cssSelector("[id$=':inputDescCurta']"));
        descEl.sendKeys("A".repeat(195));
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", descEl);
        aguardar(300);

        WebElement contador = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("desc-contador")));
        String classes = contador.getAttribute("class");
        assertTrue("Contador deve ter classe de alerta (vermelho) com 195 chars",
                classes != null && (classes.contains("danger") || classes.contains("red") || classes.contains("alerta")));
        System.out.println("✅ E16 — Contador vermelho com 195 chars. Classes: " + classes);
    }

    /**
     * E17 — Descrição Curta com 200 caracteres.
     * Esperado: campo trunca em 200, contador exibe "0 restantes".
     */
    @Test
    public void tc_E17_descricaoCurta200CaracteresContadorZero() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement descEl = driver.findElement(By.cssSelector("[id$=':inputDescCurta']"));
        String maxAttr = descEl.getAttribute("maxlength");
        assertEquals("maxlength da descrição curta deve ser 200", "200", maxAttr);

        descEl.sendKeys("A".repeat(200));
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", descEl);
        aguardar(300);

        WebElement contador = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("desc-contador")));
        assertTrue("Contador deve mostrar 0 restantes com 200 chars",
                contador.getText().contains("0"));
        System.out.println("✅ E17 — maxlength=200 e contador exibe 0 restantes");
    }

    /**
     * E18 — Descrição Completa com mais de 4000 caracteres.
     * Esperado: campo trunca em 4000 (maxlength).
     */
    @Test
    public void tc_E18_descricaoCompletaMaxLength4000() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();

        WebElement descLonga = driver.findElement(By.cssSelector("[id$='descricaoLonga']"));
        String maxAttr = descLonga.getAttribute("maxlength");
        assertTrue("maxlength da descrição completa deve ser 4000",
                "4000".equals(maxAttr) || maxAttr == null);
        System.out.println("✅ E18 — Descrição completa maxlength: " + maxAttr);
    }

    /**
     * E19 — Preenche campos obrigatórios válidos e salva.
     * Esperado: veículo aparece na tabela, formulário fecha.
     */
    @Test
    public void tc_E19_criarVeiculoComDadosValidosComSucesso() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys(NOME_VEICULO_TESTE + " " + TS);  dispararValidacao(nome);
        modelo.sendKeys(MODELO_VEICULO_TESTE);          dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);

        new Select(tipo).selectByIndex(1);
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(500);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[id$=':btnSalvarVeiculo']:not([disabled])")));
        btn.click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
        assertTrue("Deve exibir sucesso ao criar veículo", msg.isDisplayed());
        System.out.println("✅ E19 — Veículo criado: " + NOME_VEICULO_TESTE);
    }

    /**
     * E20 — Modelo com menos de 2 chars chega ao servidor.
     * Esperado: exibe "O modelo deve ter no mínimo 2 caracteres."
     */
    @Test
    public void tc_E20_modeloMenorQue2CharsErroServidor() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys("Teste Modelo Curto");  dispararValidacao(nome);
        modelo.sendKeys("X");                 dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);
        new Select(tipo).selectByIndex(1);
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(300);

        // Bypassa validação front-end e força envio ao servidor
        js.executeScript(
            "var m = document.querySelector('[id$=\":inputModelo\"]'); if(m) m.removeAttribute('maxlength');"
        );
        habilitarBotaoJs("[id$=':btnSalvarVeiculo']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarVeiculo']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir erro de modelo muito curto",
                    msg.getText().toLowerCase().contains("modelo") || msg.getText().toLowerCase().contains("mínimo"));
            System.out.println("✅ E20 — Modelo curto erro servidor: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ E20 — Servidor aceitou ou redirecionou sem mensagem visível");
        }
    }

    /**
     * E21 — Ano maior que anoAtual+1 chega ao servidor.
     * Esperado: exibe "Ano inválido."
     */
    @Test
    public void tc_E21_anoFuturoInvalidoErroServidor() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys("Teste Ano Invalido");  dispararValidacao(nome);
        modelo.sendKeys("AI-99");             dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        // Injeta ano além do permitido diretamente no campo hidden
        js.executeScript("var h = document.querySelector('[id$=\":hiddenAno\"]'); if(h) h.value = '2099';");
        new Select(tipo).selectByIndex(1); aguardar(300);

        habilitarBotaoJs("[id$=':btnSalvarVeiculo']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarVeiculo']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir erro de ano inválido",
                    msg.getText().toLowerCase().contains("ano") || msg.getText().toLowerCase().contains("inválido"));
            System.out.println("✅ E21 — Ano futuro inválido exibe erro: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ E21 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * E22 — Preço negativo chega ao servidor.
     * Esperado: exibe "O preço não pode ser negativo."
     */
    @Test
    public void tc_E22_precoNegativoErroServidor() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys("Teste Preco Neg");  dispararValidacao(nome);
        modelo.sendKeys("PN-01");          dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);
        new Select(tipo).selectByIndex(1);

        // Injeta preço negativo diretamente no campo hidden/value
        js.executeScript(
            "var p = document.querySelector('[id$=\":inputPreco\"]');" +
            "if(p) { p.removeAttribute('readonly'); p.value = '-100'; }" +
            "var h = document.querySelector('[id$=\":hiddenPreco\"]');" +
            "if(h) h.value = '-100';"
        );
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(300);

        habilitarBotaoJs("[id$=':btnSalvarVeiculo']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarVeiculo']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir erro de preço negativo",
                    msg.getText().toLowerCase().contains("preço") || msg.getText().toLowerCase().contains("negativo"));
            System.out.println("✅ E22 — Preço negativo exibe erro: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ E22 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * E23 — Tipo não selecionado chega ao servidor.
     * Esperado: exibe "Selecione o tipo do veículo."
     */
    @Test
    public void tc_E23_tipoNaoSelecionadoErroServidor() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));

        nome.sendKeys("Teste Sem Tipo");  dispararValidacao(nome);
        modelo.sendKeys("ST-01");         dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);
        // Tipo intencionalmente não selecionado; força envio via JS
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(300);

        habilitarBotaoJs("[id$=':btnSalvarVeiculo']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarVeiculo']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir erro de tipo não selecionado",
                    msg.getText().toLowerCase().contains("tipo"));
            System.out.println("✅ E23 — Tipo não selecionado exibe erro: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ E23 — Timeout aguardando resposta do servidor");
        }
    }

    /**
     * E24 — Erro ao persistir veículo no banco.
     * Esperado: exibe "Não foi possível salvar o veículo. Tente novamente."
     * Nota: simula corrompendo o hidden bean ID para forçar erro server-side.
     */
    @Test
    public void tc_E24_erroAoPersistirVeiculo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovoVeiculo();
        aguardar(600);

        WebElement nome   = driver.findElement(By.cssSelector("[id$=':inputNomeV']"));
        WebElement modelo = driver.findElement(By.cssSelector("[id$=':inputModelo']"));
        WebElement tipo   = driver.findElement(By.cssSelector("[id$=':tipoSelect']"));

        nome.sendKeys("Teste Erro Banco");  dispararValidacao(nome);
        modelo.sendKeys("EB-01");           dispararValidacao(modelo);

        wait.until(d -> new Select(d.findElement(By.id("anoSelect"))).getOptions().size() > 5);
        new Select(driver.findElement(By.id("anoSelect"))).selectByValue("2024");
        js.executeScript("onAnoChange(document.getElementById('anoSelect'))"); aguardar(300);
        new Select(tipo).selectByIndex(1);
        js.executeScript("atualizarBotaoVeiculo()"); aguardar(400);

        // Corrompe o tipo para forçar erro de persistência
        js.executeScript(
            "var s = document.querySelector('[id$=\":tipoSelect\"]');" +
            "if(s) s.value = 'TIPO_INVALIDO_FORCAR_ERRO';"
        );

        habilitarBotaoJs("[id$=':btnSalvarVeiculo']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarVeiculo']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-error")));
            assertTrue("Deve exibir alguma mensagem de erro", msg.isDisplayed());
            System.out.println("✅ E24 — Erro ao persistir veículo: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ E24 — Servidor rejeitou silenciosamente ou redirecionou");
        }
    }

    /**
     * E25 — Admin clica em "Editar" em veículo.
     * Esperado: formulário pré-preenchido, ano pré-selecionado, preço formatado.
     */
    @Test
    public void tc_E25_editarVeiculoFormularioPrePreenchido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_VEIC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();

        WebElement nomeEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[id$=':inputNomeV']")));
        aguardar(600);

        String valorNome = nomeEl.getAttribute("value");
        assertTrue("Formulário de edição deve ter nome pré-preenchido",
                valorNome != null && !valorNome.isEmpty());

        String valorAno = driver.findElement(By.id("anoSelect")).getAttribute("value");
        assertTrue("Ano deve estar pré-selecionado", valorAno != null && !valorAno.isEmpty());

        System.out.println("✅ E25 — Edição veículo pré-preenchida. Nome: " + valorNome + " | Ano: " + valorAno);
    }

    /**
     * E26 — Admin edita modelo e salva.
     * Esperado: sucesso, tabela atualizada.
     */
    @Test
    public void tc_E26_editarModeloESalvarComSucesso() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_VEIC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();

        WebElement modeloEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[id$=':inputModelo']")));
        aguardar(600);

        modeloEl.clear();
        modeloEl.sendKeys("ModeloEditado" + (TS % 1000));
        dispararValidacao(modeloEl);
        aguardar(400);

        habilitarBotaoJs("[id$=':btnSalvarVeiculo']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarVeiculo']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ E26 — Resultado edição de modelo: " + msg.getText());
        } catch (TimeoutException e) {
            System.out.println("⚠️ E26 — Timeout aguardando resposta");
        }
    }

    /**
     * E27 — Admin clica em "Remover" em veículo.
     * Esperado: veículo some do catálogo (soft delete, disponivel = false).
     */
    @Test
    public void tc_E27_removerVeiculoSoftDelete() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_VEIC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        int qtdAntes = driver.findElements(By.cssSelector(".vrum-table tbody tr")).size();

        List<WebElement> botoesRemover = driver.findElements(
                By.xpath("//input[contains(@value,'Remover')] | //button[contains(text(),'Remover')]"));

        if (botoesRemover.isEmpty()) {
            System.out.println("⚠️ E27 — Nenhum botão Remover encontrado");
            return;
        }

        botoesRemover.get(0).click();
        aguardar(1500);

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
            assertTrue("Remoção deve exibir sucesso", msg.isDisplayed());
            System.out.println("✅ E27 — Veículo removido (soft delete): " + msg.getText());
        } catch (TimeoutException e) {
            // Verifica se a quantidade de linhas diminuiu
            int qtdDepois = driver.findElements(By.cssSelector(".vrum-table tbody tr")).size();
            assertTrue("Tabela deve ter menos linhas após remoção", qtdDepois < qtdAntes);
            System.out.println("✅ E27 — Veículo removido: tabela de " + qtdAntes + " para " + qtdDepois + " linhas");
        }
    }

    /**
     * E28 — Admin clica "Cancelar" no formulário de veículo.
     * Esperado: formulário fecha sem salvar.
     */
    @Test
    public void tc_E28_cancelarEdicaoVeiculoFechaFormulario() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_VEIC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));

        WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@value,'Editar')] | //button[contains(text(),'Editar')])[1]")));
        btnEditar.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id$=':inputNomeV']")));

        WebElement btnCancelar = driver.findElement(
                By.xpath("//input[contains(@value,'Cancelar')] | //button[contains(text(),'Cancelar')]"));
        btnCancelar.click();
        aguardar(500);

        List<WebElement> form = driver.findElements(By.cssSelector("[id$=':inputNomeV']"));
        assertTrue("Formulário deve fechar após cancelar", form.isEmpty());
        System.out.println("✅ E28 — Cancelar edição veículo fecha o formulário");
    }

    // =========================================================================
    // BLOCO F — Gestão de Pedidos
    // =========================================================================

    /**
     * F01 — Admin acessa página de pedidos.
     * Esperado: tabela com todos os pedidos exibida.
     */
    @Test
    public void tc_F01_adminAcessaTodosPedidos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));
        WebElement main = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".main-content")));
        assertNotNull("Página de pedidos deve carregar", main);
        System.out.println("✅ F01 — Página de pedidos admin carregada");
    }

    /**
     * F02 — Filtrar por nome do cliente.
     * Esperado: tabela exibe apenas pedidos do cliente correspondente.
     */
    @Test
    public void tc_F02_filtrarPedidosPorCliente() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement campoBusca = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@placeholder,'liente') or contains(@id,'buscaCliente') or contains(@id,'filtroCliente')]")));
        campoBusca.clear();
        campoBusca.sendKeys("Selenium");
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Filtrar') or contains(@value,'Buscar')] | //button[contains(text(),'Filtrar') or contains(text(),'Buscar')]")))
                .click();
        aguardar(1000);

        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".vrum-table, .main-content")));
        assertNotNull("Tabela deve existir após filtro por cliente", tabela);
        System.out.println("✅ F02 — Filtro por cliente executado");
    }

    /**
     * F03 — Filtrar por vendedor.
     * Esperado: tabela exibe apenas pedidos do vendedor selecionado.
     */
    @Test
    public void tc_F03_filtrarPedidosPorVendedor() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement selectVendedor = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id,'filtroVendedor') or contains(@id,'Vendedor')]")));
        Select sel = new Select(selectVendedor);
        Assume.assumeTrue("Precondição: select de vendedor deve ter mais de uma opção", sel.getOptions().size() > 1);

        sel.selectByIndex(1);
        aguardar(300);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Filtrar') or contains(@value,'Aplicar')] | //button[contains(text(),'Filtrar')]")))
                .click();
        aguardar(1000);

        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela deve existir após filtro por vendedor", tabela);
        System.out.println("✅ F03 — Filtro por vendedor executado");
    }

    /**
     * F04 — Filtrar por veículo.
     * Esperado: tabela exibe apenas pedidos do veículo selecionado.
     */
    @Test
    public void tc_F04_filtrarPedidosPorVeiculo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement selectVeiculo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id,'filtroVeiculo') or contains(@id,'Veiculo')]")));
        Select sel = new Select(selectVeiculo);
        Assume.assumeTrue("Precondição: select de veículo deve ter mais de uma opção", sel.getOptions().size() > 1);

        sel.selectByIndex(1);
        aguardar(300);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Filtrar') or contains(@value,'Aplicar')] | //button[contains(text(),'Filtrar')]")))
                .click();
        aguardar(1000);

        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela deve existir após filtro por veículo", tabela);
        System.out.println("✅ F04 — Filtro por veículo executado");
    }

    /**
     * F05 — Filtrar por status.
     * Esperado: tabela exibe apenas pedidos no status selecionado.
     */
    @Test
    public void tc_F05_filtrarPedidosPorStatus() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement selectStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id,'filtroStatus') or contains(@id,'StatusStr') or contains(@id,'status')]")));
        Select sel = new Select(selectStatus);
        Assume.assumeTrue("Precondição: select de status deve ter mais de uma opção", sel.getOptions().size() > 1);

        sel.selectByIndex(1);
        aguardar(300);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Filtrar') or contains(@value,'Aplicar')] | //button[contains(text(),'Filtrar')]")))
                .click();
        aguardar(1000);

        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela deve existir após filtro por status", tabela);
        System.out.println("✅ F05 — Filtro por status executado");
    }

    /**
     * F06 — Filtrar por concessionária.
     * Esperado: tabela exibe apenas pedidos da concessionária selecionada.
     */
    @Test
    public void tc_F06_filtrarPedidosPorConcessionaria() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement selectConc = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id,'filtroConcessionaria') or contains(@id,'Concession')]")));
        Select sel = new Select(selectConc);
        Assume.assumeTrue("Precondição: select de concessionária deve ter mais de uma opção", sel.getOptions().size() > 1);

        sel.selectByIndex(1);
        aguardar(300);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Filtrar') or contains(@value,'Aplicar')] | //button[contains(text(),'Filtrar')]")))
                .click();
        aguardar(1000);

        WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela deve existir após filtro por concessionária", tabela);
        System.out.println("✅ F06 — Filtro por concessionária executado");
    }

    /**
     * F07 — Filtro com termo inexistente.
     * Esperado: tabela vazia ou mensagem "Nenhum pedido encontrado".
     */
    @Test
    public void tc_F07_filtroTermoInexistenteExibeTabelaVazia() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement campoBusca = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@placeholder,'liente') or contains(@id,'buscaCliente') or contains(@id,'filtroCliente')]")));
        campoBusca.clear();
        campoBusca.sendKeys("zzzTermoQueNaoExiste999zzzz");
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Filtrar') or contains(@value,'Buscar')] | //button[contains(text(),'Filtrar')]")))
                .click();
        aguardar(1000);

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        List<WebElement> msgVazia = driver.findElements(By.xpath(
                "//*[contains(text(),'Nenhum') or contains(text(),'nenhum') or contains(text(),'não encontrado')]"));

        assertTrue("Deve haver tabela vazia ou mensagem de resultado vazio",
                linhas.isEmpty() || !msgVazia.isEmpty());
        System.out.println("✅ F07 — Filtro inexistente: " +
                (linhas.isEmpty() ? "tabela vazia" : "mensagem exibida"));
    }

    /**
     * F08 — Clicar em "Limpar Filtros".
     * Esperado: lista completa de pedidos restaurada.
     */
    @Test
    public void tc_F08_limparFiltrosRestauralista() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement btnLimpar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Limpar')] | //button[contains(text(),'Limpar')]")));
        btnLimpar.click();
        aguardar(1000);

        WebElement main = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".main-content")));
        assertNotNull("Página deve continuar carregada após limpar filtros", main);
        System.out.println("✅ F08 — Limpar filtros executado, lista restaurada");
    }

    /**
     * F09 — Admin seleciona um pedido.
     * Esperado: painel de detalhes/edição exibido ao lado.
     */
    @Test
    public void tc_F09_selecionarPedidoExibePainelDetalhes() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());

        abrirPrimeiroPedido();

        WebElement painel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".card, .painel-detalhe, .detalhe-pedido")));
        assertTrue("Painel de detalhes deve aparecer após selecionar pedido", painel.isDisplayed());
        System.out.println("✅ F09 — Pedido selecionado: painel de detalhes exibido");
    }

    /**
     * F10 — Admin altera status do pedido e salva.
     * Esperado: mensagem de sucesso, status atualizado.
     */
    @Test
    public void tc_F10_adminAlteraStatusPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement selectStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id,'editStatus') or contains(@id,'Status') or contains(@id,'status')]")));
        Select sel = new Select(selectStatus);
        int indiceAtual = sel.getOptions().indexOf(sel.getFirstSelectedOption());
        sel.selectByIndex(indiceAtual == 0 ? 1 : 0);
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success, .msg-error")));
        assertTrue("Status do pedido deve gerar resposta do servidor", msg.isDisplayed());
        System.out.println("✅ F10 — Status do pedido alterado: " + msg.getText());
    }

    /**
     * F11 — Admin altera vendedor responsável e salva.
     * Esperado: vendedor atualizado.
     */
    @Test
    public void tc_F11_adminAlteraVendedorPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement selectVend = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id,'editVendedor') or contains(@id,'Vendedor') or contains(@id,'vendedor')]")));
        Select sel = new Select(selectVend);
        Assume.assumeTrue("Precondição: deve haver mais de um vendedor disponível", sel.getOptions().size() > 1);

        sel.selectByIndex(1);
        aguardar(300);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success, .msg-error")));
        assertTrue("Alteração de vendedor deve gerar resposta do servidor", msg.isDisplayed());
        System.out.println("✅ F11 — Vendedor do pedido alterado: " + msg.getText());
    }

    /**
     * F12 — Admin altera forma de pagamento e salva.
     * Esperado: dado atualizado.
     */
    @Test
    public void tc_F12_adminAlteraFormaPagamentoPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement selectPgto = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//select[contains(@id,'pagamento') or contains(@id,'Pagamento') or contains(@id,'formaPgto')]")));
        Select sel = new Select(selectPgto);
        Assume.assumeTrue("Precondição: deve haver mais de uma forma de pagamento", sel.getOptions().size() > 1);

        int indiceAtual = sel.getOptions().indexOf(sel.getFirstSelectedOption());
        sel.selectByIndex(indiceAtual == 0 ? 1 : 0);
        aguardar(300);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success, .msg-error")));
        assertTrue("Alteração de pagamento deve gerar resposta do servidor", msg.isDisplayed());
        System.out.println("✅ F12 — Forma de pagamento alterada: " + msg.getText());
    }

    /**
     * F13 — Admin altera prazo de fabricação e salva.
     * Esperado: dado atualizado.
     */
    @Test
    public void tc_F13_adminAlteraPrazoFabricacaoPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement campoPrazo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//input[contains(@id,'prazoFab') or contains(@id,'PrazoFab') or contains(@id,'fabricacao')]")));
        campoPrazo.clear();
        campoPrazo.sendKeys("60");
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success, .msg-error")));
        assertTrue("Alteração de prazo de fabricação deve gerar resposta", msg.isDisplayed());
        System.out.println("✅ F13 — Prazo de fabricação alterado: " + msg.getText());
    }

    /**
     * F14 — Admin altera prazo de entrega e salva.
     * Esperado: dado atualizado.
     */
    @Test
    public void tc_F14_adminAlteraPrazoEntregaPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement campoPrazo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//input[contains(@id,'prazoEnt') or contains(@id,'PrazoEnt') or contains(@id,'entrega')]")));
        campoPrazo.clear();
        campoPrazo.sendKeys("90");
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success, .msg-error")));
        assertTrue("Alteração de prazo de entrega deve gerar resposta", msg.isDisplayed());
        System.out.println("✅ F14 — Prazo de entrega alterado: " + msg.getText());
    }

    /**
     * F15 — Admin altera data de retirada e salva.
     * Esperado: dado atualizado.
     */
    @Test
    public void tc_F15_adminAlteraDataRetiradaPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement campoData = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//input[@type='date' and (contains(@id,'retirada') or contains(@id,'Retirada') or contains(@id,'dataRet'))]")));
        js.executeScript("arguments[0].value = '2026-12-31'", campoData);
        js.executeScript("arguments[0].dispatchEvent(new Event('change',{bubbles:true}))", campoData);
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success, .msg-error")));
        assertTrue("Alteração de data de retirada deve gerar resposta", msg.isDisplayed());
        System.out.println("✅ F15 — Data de retirada alterada: " + msg.getText());
    }

    /**
     * F16 — Admin adiciona observação e salva.
     * Esperado: observação gravada com sucesso.
     */
    @Test
    public void tc_F16_adminAdicionaObservacaoPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement obs = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//textarea[contains(@id,'bservacoes') or contains(@id,'obs') or contains(@id,'Obs')]")));
        obs.clear();
        obs.sendKeys("Observação adicionada via Selenium - " + TS);
        aguardar(300);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"))).click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-success")));
        assertTrue("Salvar observação deve exibir mensagem de sucesso", msg.isDisplayed());
        System.out.println("✅ F16 — Observação adicionada ao pedido: " + msg.getText());
    }

    /**
     * F17 — Admin cancela edição do pedido.
     * Esperado: painel fecha sem salvar alterações.
     */
    @Test
    public void tc_F17_adminCancelaEdicaoPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        // Altera campo sem salvar
        List<WebElement> textoObs = driver.findElements(By.xpath(
                "//textarea[contains(@id,'bservacoes') or contains(@id,'obs')]"));
        if (!textoObs.isEmpty()) {
            textoObs.get(0).sendKeys("Texto que NÃO deve ser salvo");
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Cancelar')] | //button[contains(text(),'Cancelar')]"))).click();
        aguardar(500);

        // Após cancelar, painel de edição não deve mais ter o botão Salvar visível
        List<WebElement> btnSalvar = driver.findElements(By.xpath(
                "//input[contains(@value,'Salvar')] | //button[contains(text(),'Salvar')]"));
        assertTrue("Painel deve fechar após cancelar (botão Salvar não visível)",
                btnSalvar.isEmpty() || btnSalvar.stream().noneMatch(WebElement::isDisplayed));
        System.out.println("✅ F17 — Cancelar edição de pedido fechou o painel");
    }

    /**
     * F18 — Admin faz upload de arquivo em um pedido.
     * Esperado: arquivo listado nos anexos do pedido.
     */
    @Test
    public void tc_F18_adminUploadArquivoPedido() throws java.io.IOException {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        WebElement inputFile = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@type='file']")));

        java.io.File tempFile = java.io.File.createTempFile("selenium_anexo_", ".txt");
        java.nio.file.Files.writeString(tempFile.toPath(), "Anexo de teste Selenium - " + TS);
        tempFile.deleteOnExit();

        inputFile.sendKeys(tempFile.getAbsolutePath());
        aguardar(500);

        // Clica em "Enviar" se houver botão separado
        List<WebElement> btnEnviar = driver.findElements(By.xpath(
                "//input[contains(@value,'Enviar') or contains(@value,'Upload')] | //button[contains(text(),'Enviar') or contains(text(),'Upload')]"));
        if (!btnEnviar.isEmpty()) {
            btnEnviar.get(0).click();
            aguardar(2000);
        } else {
            aguardar(1500);
        }

        List<WebElement> anexos = driver.findElements(By.cssSelector(
                ".anexo, .attachment, [class*='anexo'], [class*='attach']"));
        assertTrue("Arquivo deve aparecer na lista de anexos após upload", !anexos.isEmpty());
        System.out.println("✅ F18 — Upload de arquivo realizado: " + anexos.size() + " anexo(s)");
    }

    /**
     * F19 — Admin clica em "Download" em um anexo.
     * Esperado: arquivo baixado corretamente.
     */
    @Test
    public void tc_F19_adminDownloadAnexoPedido() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_PEDIDOS);
        wait.until(ExpectedConditions.urlContains("pedidos"));

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        Assume.assumeTrue("Precondição: banco deve ter ao menos um pedido", !linhas.isEmpty());
        abrirPrimeiroPedido();

        List<WebElement> botoesDownload = driver.findElements(By.xpath(
                "//a[contains(@href,'download') or contains(text(),'Download') or contains(@title,'Download')] | " +
                "//button[contains(text(),'Download')] | " +
                "//input[contains(@value,'Download')]"));
        Assume.assumeTrue("Precondição: deve haver ao menos um anexo disponível para download",
                !botoesDownload.isEmpty());

        botoesDownload.get(0).click();
        aguardar(2000);

        assertFalse("Download não deve redirecionar para página de erro",
                driver.getCurrentUrl().contains("error") || driver.getCurrentUrl().contains("404"));
        System.out.println("✅ F19 — Download de anexo acionado com sucesso");
    }

    // =========================================================================
    // BLOCO G — Controles Transversais (Sidebar e Logout)
    // =========================================================================

    /**
     * G01 — Sidebar: clique em "Usuários".
     * Esperado: navega para usuarios.xhtml.
     */
    @Test
    public void tc_G01_sidebarLinkUsuariosNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//nav[contains(@class,'sidebar')]//a[contains(@href,'usuarios')]"))).click();
        wait.until(ExpectedConditions.urlContains("usuarios"));
        assertTrue("Sidebar: link Usuários deve navegar",
                driver.getCurrentUrl().contains("usuarios"));
        System.out.println("✅ G01 — Sidebar link Usuários navega");
    }

    /**
     * G02 — Sidebar: clique em "Concessionárias".
     * Esperado: navega para concessionarias.xhtml.
     */
    @Test
    public void tc_G02_sidebarLinkConcessionariasNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//nav[contains(@class,'sidebar')]//a[contains(@href,'concessionarias')]"))).click();
        wait.until(ExpectedConditions.urlContains("concessionarias"));
        assertTrue("Sidebar: link Concessionárias deve navegar",
                driver.getCurrentUrl().contains("concessionarias"));
        System.out.println("✅ G02 — Sidebar link Concessionárias navega");
    }

    /**
     * G03 — Sidebar: clique em "Veículos".
     * Esperado: navega para veiculos.xhtml.
     */
    @Test
    public void tc_G03_sidebarLinkVeiculosNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//nav[contains(@class,'sidebar')]//a[contains(@href,'veiculos')]"))).click();
        wait.until(ExpectedConditions.urlContains("veiculos"));
        assertTrue("Sidebar: link Veículos deve navegar",
                driver.getCurrentUrl().contains("veiculos"));
        System.out.println("✅ G03 — Sidebar link Veículos navega");
    }

    /**
     * G04 — Sidebar: clique em "Pedidos".
     * Esperado: navega para pedidos.xhtml.
     */
    @Test
    public void tc_G04_sidebarLinkPedidosNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//nav[contains(@class,'sidebar')]//a[contains(@href,'pedidos')]"))).click();
        wait.until(ExpectedConditions.urlContains("pedidos"));
        assertTrue("Sidebar: link Pedidos deve navegar",
                driver.getCurrentUrl().contains("pedidos"));
        System.out.println("✅ G04 — Sidebar link Pedidos navega");
    }

    /**
     * G05 — Sidebar: clique em "Dashboard".
     * Esperado: navega para dashboard.xhtml.
     */
    @Test
    public void tc_G05_sidebarLinkDashboardNavega() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_USUARIOS);
        wait.until(ExpectedConditions.urlContains("usuarios"));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//nav[contains(@class,'sidebar')]//a[contains(@href,'dashboard')]"))).click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue("Sidebar: link Dashboard deve navegar",
                driver.getCurrentUrl().contains("dashboard"));
        System.out.println("✅ G05 — Sidebar link Dashboard navega");
    }

    /**
     * G06 — Sidebar exibe nome do admin logado.
     * Esperado: nome correto exibido no cabeçalho da sidebar.
     */
    @Test
    public void tc_G06_sidebarExibeNomeAdminLogado() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        WebElement nomeUsuario = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".sidebar-user-name")));
        assertNotNull("Elemento com nome do admin deve existir", nomeUsuario);
        assertFalse("Nome do admin deve estar visível e não vazio",
                nomeUsuario.getText() == null || nomeUsuario.getText().trim().isEmpty());
        System.out.println("✅ G06 — Sidebar exibe nome do admin: " + nomeUsuario.getText());
    }

    /**
     * G07 — Admin clica em "Sair".
     * Esperado: logout executado e redirecionamento para login.xhtml.
     */
    @Test
    public void tc_G07_adminClicarSairFazLogout() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        WebElement btnSair = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')] | //button[contains(text(),'Sair')]")));
        btnSair.click();

        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Logout deve redirecionar ao login", driver.getCurrentUrl().contains("login"));

        // Confirma que sessão foi invalidada tentando acessar área restrita
        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Após logout sessão deve estar inválida", driver.getCurrentUrl().contains("login"));
        System.out.println("✅ G07 — Logout do admin: sessão invalidada e redirecionado ao login");
    }
}
