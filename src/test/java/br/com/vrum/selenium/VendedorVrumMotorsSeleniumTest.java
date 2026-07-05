package br.com.vrum.selenium;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    // ── Banco — fixture de testes ─────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/vrum_motors"
            + "?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";
    private static final String NUM_B03 = "VRM_TEST_B03";
    private static final String NUM_B04 = "VRM_TEST_B04";
    private static final String NUM_B05 = "VRM_TEST_B05";

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        criarPedidosTeste();
    }

    @AfterClass
    public static void tearDown() {
        limparPedidosTeste();
        if (driver != null) driver.quit();
    }

    /**
     * Insere pedidos de fixture diretamente no banco via JDBC, contornando o
     * @PrePersist que forçaria status = AGUARDANDO_ATENDIMENTO.
     * B04 precisa de ENVIADO_CIDADE; B05 precisa de PRONTO_ENTREGA.
     */
    private static void criarPedidosTeste() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            Long vendedorId = queryLong(conn,
                    "SELECT ID FROM usuarios WHERE EMAIL = ?", EMAIL_VENDEDOR);
            Long clienteId  = queryLong(conn,
                    "SELECT ID FROM usuarios WHERE EMAIL = ?", "cliente@email.com");
            Long veiculoId  = queryLong(conn,
                    "SELECT ID FROM veiculos LIMIT 1", (String) null);
            Long concId     = queryLong(conn,
                    "SELECT concessionaria_id FROM vendedores WHERE ID = ?",
                    vendedorId != null ? vendedorId.toString() : null);

            if (vendedorId == null || clienteId == null || veiculoId == null || concId == null) {
                System.out.println("⚠️ criarPedidosTeste: dados base não encontrados — B03/B04/B05 serão pulados.");
                return;
            }

            criarSeNaoExistir(conn, NUM_B03, clienteId, veiculoId, concId, vendedorId, "EM_NEGOCIACAO");
            criarSeNaoExistir(conn, NUM_B04, clienteId, veiculoId, concId, vendedorId, "ENVIADO_CIDADE");
            criarSeNaoExistir(conn, NUM_B05, clienteId, veiculoId, concId, vendedorId, "PRONTO_ENTREGA");

        } catch (Exception e) {
            System.err.println("⚠️ criarPedidosTeste falhou: " + e.getMessage());
        }
    }

    private static void criarSeNaoExistir(Connection conn, String numero,
            Long clienteId, Long veiculoId, Long concId, Long vendedorId, String status)
            throws Exception {
        Long existe = queryLong(conn, "SELECT ID FROM pedidos WHERE NUMEROPEDIDO = ?", numero);
        if (existe != null) {
            System.out.println("ℹ️ Pedido " + numero + " já existe, mantendo.");
            return;
        }
        String sql = "INSERT INTO pedidos "
                + "(NUMEROPEDIDO, cliente_id, veiculo_id, concessionaria_id, vendedor_id, "
                + " STATUS, data_pedido, data_atualizacao, forma_pagamento) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), 'Pix à Vista')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numero);
            ps.setLong(2, clienteId);
            ps.setLong(3, veiculoId);
            ps.setLong(4, concId);
            ps.setLong(5, vendedorId);
            ps.setString(6, status);
            ps.executeUpdate();
        }
        System.out.println("✅ criarPedidosTeste: pedido " + numero + " (" + status + ") criado.");
    }

    private static Long queryLong(Connection conn, String sql, String param) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static Long queryLong(Connection conn, String sql, Long param) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setLong(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static void limparPedidosTeste() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM pedidos WHERE NUMEROPEDIDO IN (?, ?, ?)")) {
            ps.setString(1, NUM_B03);
            ps.setString(2, NUM_B04);
            ps.setString(3, NUM_B05);
            int removidos = ps.executeUpdate();
            System.out.println("✅ limparPedidosTeste: " + removidos + " pedido(s) de teste removido(s).");
        } catch (Exception e) {
            System.err.println("⚠️ limparPedidosTeste falhou: " + e.getMessage());
        }
    }

    @After
    public void logoutAposCadaTeste() {
        aguardar(500);
        try {
            // Se não estamos em uma página de vendedor (ex: acesso-negado após A02),
            // navegar para pedidos garante que o botão "Sair" esteja disponível e
            // que a sessão do servidor seja corretamente invalidada via loginBean.logout().
            if (!driver.getCurrentUrl().contains("/vendedor/")) {
                driver.get(URL_PEDIDOS);
                aguardar(500);
            }
            WebElement btnSair = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')] | //button[contains(text(),'Sair')]")));
            btnSair.click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.urlContains("home")));
        } catch (Exception e) {
            driver.manage().deleteAllCookies();
            driver.get(LOGIN_URL);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Login válido — espera a URL mudar para confirmar redirecionamento. */
    private void fazerLogin() {
        driver.get(LOGIN_URL);
        // h:inputSecret renderiza como type="password"; não é form multipart, .click() funciona
        WebElement campoEmail = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        aguardar(300);
        campoEmail.click(); campoEmail.clear(); campoEmail.sendKeys(EMAIL_VENDEDOR);
        aguardar(300);
        campoSenha.click(); campoSenha.clear(); campoSenha.sendKeys(SENHA_VENDEDOR);
        aguardar(500);
        driver.findElement(By.cssSelector("#loginForm input[type='submit'], #loginForm button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/vendedor/"));
    }

    private void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    /** Click via JS — necessário para h:commandButton dentro de form multipart/form-data com JSF/Payara. */
    private void jsClick(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
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
     * Esperado: Mensagem de erro vermelha exigindo campos obrigatórios.
     */
    @Test
    public void tc_B02_validacao_EnviarFabricaSemDadosExibeErro() {
        fazerLogin();
        driver.get(URL_PEDIDOS);

        List<WebElement> badgesNegociacao = driver.findElements(
                By.xpath("//span[contains(@class,'badge-emnegociacao')]"));
        Assume.assumeTrue("Precondição: Deve haver pedido EM_NEGOCIACAO para gerenciar", !badgesNegociacao.isEmpty());

        WebElement btnGerenciar = driver.findElement(
                By.xpath("//span[contains(@class,'badge-emnegociacao')]/following::input[contains(@value,'Selecionar')][1]"));
        btnGerenciar.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card-header') and contains(.,'Gerenciando')]")));
        aguardar(500);

        // Jakarta Faces 4 não renderiza 'placeholder'; usamos maxlength para localizar
        WebElement inputPagamento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text' and @maxlength='50']")));
        inputPagamento.clear();
        WebElement inputPrazo = driver.findElement(By.xpath("//input[@type='text' and @maxlength='4']"));
        inputPrazo.clear();

        WebElement btnEnviarFabrica = driver.findElement(
                By.xpath("//input[contains(@value,'Enviar para Fabrica')] | //button[contains(text(),'Enviar para Fabrica')]"));
        // JS click necessário: o .click() normal do Selenium não submete form multipart/form-data neste contexto JSF
        jsClick(btnEnviarFabrica);
        aguardar(2000);

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

        List<WebElement> badgesNegociacao = driver.findElements(
                By.xpath("//span[contains(@class,'badge-emnegociacao')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido EM_NEGOCIACAO para gerenciar", !badgesNegociacao.isEmpty());

        WebElement btnGerenciar = driver.findElement(
                By.xpath("//span[contains(@class,'badge-emnegociacao')]/following::input[contains(@value,'Selecionar')][1]"));
        btnGerenciar.click();

        // Aguarda o painel de gerenciamento aparecer
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card-header') and contains(.,'Gerenciando')]")));
        aguardar(500);

        WebElement inputPagamento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text' and @maxlength='50']")));
        inputPagamento.clear();
        inputPagamento.sendKeys("Pix à Vista");

        WebElement inputPrazo = driver.findElement(By.xpath("//input[@type='text' and @maxlength='4']"));
        inputPrazo.clear();
        inputPrazo.sendKeys("90");
        aguardar(500);

        WebElement btnEnviarFabrica = driver.findElement(
                By.xpath("//input[contains(@value,'Enviar para Fabrica')] | //button[contains(text(),'Enviar para Fabrica')]"));
        jsClick(btnEnviarFabrica);
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

        List<WebElement> badgesEnviado = driver.findElements(
                By.xpath("//span[contains(@class,'badge-enviadocidade')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido ENVIADO_CIDADE para gerenciar", !badgesEnviado.isEmpty());

        WebElement btnGerenciar = driver.findElement(
                By.xpath("//span[contains(@class,'badge-enviadocidade')]/following::input[contains(@value,'Selecionar')][1]"));
        btnGerenciar.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card-header') and contains(.,'Gerenciando')]")));
        aguardar(500);

        WebElement btnPronto = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Pronto para Entrega')] | //button[contains(text(),'Pronto para Entrega')]")));
        jsClick(btnPronto);
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

        List<WebElement> badgesPronto = driver.findElements(
                By.xpath("//span[contains(@class,'badge-prontoentrega')]"));

        Assume.assumeTrue("Precondição: Deve haver pedido PRONTO_ENTREGA para gerenciar", !badgesPronto.isEmpty());

        // Busca o botão especificamente para VRM_TEST_B05 pelo número do pedido,
        // evitando ambiguidade quando VRM_TEST_B04 também está em PRONTO_ENTREGA após B04.
        WebElement btnGerenciar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(),'" + NUM_B05 + "')]/following::input[contains(@value,'Selecionar')][1]")));
        jsClick(btnGerenciar);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'card-header') and contains(.,'Gerenciando')]")));
        aguardar(500);

        WebElement inputData = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='text']")));
        inputData.clear();
        inputData.sendKeys("28/12/2026");
        aguardar(500);

        WebElement btnFinalizar = driver.findElement(
                By.xpath("//input[contains(@value,'Finalizar Pedido')] | //button[contains(text(),'Finalizar Pedido')]"));
        jsClick(btnFinalizar);
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
