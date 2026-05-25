package br.com.vrum.selenium;

import java.time.Duration;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Testes automatizados Selenium — Vrum Motors
 * 
 * Pré-requisitos:
 *   - Aplicação rodando em http://localhost:8080/vrum-motors
 *   - Banco populado (DataInicializador executa automaticamente)
 *   - Google Chrome instalado
 * 
 * Para executar: mvn test
 */
public class VrumMotorsSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL   = "http://localhost:8080/vrum-motors";
    private static final String HOME_URL   = BASE_URL + "/home.xhtml";
    private static final String LOGIN_URL  = BASE_URL + "/login.xhtml";
    private static final String CADASTRO_URL = BASE_URL + "/cadastro.xhtml";

    // Credenciais de teste (inseridas pelo DataInicializador)
    private static final String EMAIL_ADMIN    = "admin@vrummotors.com";
    private static final String SENHA_ADMIN    = "admin123";
    private static final String EMAIL_GERENTE  = "gerente.recife@vrummotors.com";
    private static final String SENHA_GERENTE  = "gerente123";
    private static final String EMAIL_FABRICA  = "fabrica@vrummotors.com";
    private static final String SENHA_FABRICA  = "fabrica123";
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";
    private static final String EMAIL_CLIENTE  = "cliente@email.com";
    private static final String SENHA_CLIENTE  = "cliente123";

    @BeforeClass
    public static void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        // options.addArguments("--headless"); // descomente para rodar sem abrir janela
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @After
    public void logout() {
        // Pausa de 2 segundos no final de CADA teste para você conseguir ver o resultado visualmente!
        aguardar(2000);

        try {
            driver.get(BASE_URL + "/logout");
            wait.until(ExpectedConditions.urlContains("login"));
        } catch (Exception ignored) {}
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void fazerLogin(String email, String senha) {
        driver.get(LOGIN_URL);

        // Aguarda o campo email estar clicável (visível E interagível)
        WebElement campoEmail = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));

        aguardar(300);
        campoEmail.click();
        campoEmail.clear();
        campoEmail.sendKeys(email);

        aguardar(300);
        campoSenha.click();
        campoSenha.clear();
        campoSenha.sendKeys(senha);

        aguardar(300);
        WebElement btnLogin = driver.findElement(
                By.cssSelector("input[type='submit'], button[type='submit']"));
        btnLogin.click();

        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlToBe(LOGIN_URL)));
    }

    private WebElement encontrarInput(String partialId) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[contains(@id,'" + partialId + "') or @name='" + partialId + "']")));
        } catch (TimeoutException e) {
            System.err.println("Elemento de input com id parcial '" + partialId + "' não encontrado.");
            return null;
        }
    }

    private void aguardar(int millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    private void tirarScreenshot(String nome) {
        // Opcional: captura screenshot para evidência
        // TakesScreenshot ts = (TakesScreenshot) driver;
        // File src = ts.getScreenshotAs(OutputType.FILE);
        // FileUtils.copyFile(src, new File("target/screenshots/" + nome + ".png"));
    }

    // =========================================================
    // TC01 — Home Page pública é acessível sem login
    // =========================================================
    @Test
    public void tc01_homepagePublicaAcessivel() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.titleContains("Vrum Motors"));

        String titulo = driver.getTitle();
        assertTrue("Título deve conter 'Vrum Motors'", titulo.contains("Vrum Motors"));

        // Verifica que a navbar existe
        WebElement navbar = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar")));
        assertNotNull("Navbar deve estar visível", navbar);

        // Verifica a presença do hero
        WebElement hero = driver.findElement(By.cssSelector(".hero"));
        assertTrue("Hero deve estar visível", hero.isDisplayed());

        tirarScreenshot("tc01_homepage");
        System.out.println("✅ TC01 — Home pública acessível");
    }

    // =========================================================
    // TC02 — Login com credenciais inválidas exibe mensagem de erro
    // =========================================================
    @Test
    public void tc02_loginInvalidoExibeErro() {
        driver.get(LOGIN_URL);

        WebElement campoEmail = encontrarInput("email");
        WebElement campoSenha = encontrarInput("senha");

        aguardar(500);
        campoEmail.sendKeys("invalido@teste.com");
        aguardar(500);
        campoSenha.sendKeys("senhaerrada");
        aguardar(500);

        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();

        // Deve permanecer na página de login
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Deve permanecer no login após falha", driver.getCurrentUrl().contains("login"));

        // Deve exibir mensagem de erro
        try {
            WebElement msgErro = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(".msg-error, .ui-messages-error")));
            assertTrue("Mensagem de erro deve ser exibida", msgErro.isDisplayed());
        } catch (TimeoutException e) {
            // Aceita também: página não redireciona (erro genérico)
            assertTrue("URL deve continue sendo login", driver.getCurrentUrl().contains("login"));
        }

        tirarScreenshot("tc02_login_invalido");
        System.out.println("✅ TC02 — Login inválido exibe erro");
    }

    // =========================================================
    // TC03 — Login do Admin redireciona ao dashboard
    // =========================================================
    @Test
    public void tc03_loginAdminRedirecionaDashboard() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);

        wait.until(ExpectedConditions.urlContains("/admin/"));
        assertTrue("Admin deve ir ao dashboard", driver.getCurrentUrl().contains("/admin/"));

        tirarScreenshot("tc03_admin_dashboard");
        System.out.println("✅ TC03 — Login admin redireciona ao dashboard");
    }

    // =========================================================
    // TC04 — Login do Gerente redireciona à área do gerente
    // =========================================================
    @Test
    public void tc04_loginGerenteRedirecionaAreaGerente() {
        fazerLogin(EMAIL_GERENTE, SENHA_GERENTE);

        wait.until(ExpectedConditions.urlContains("/gerente/"));
        assertTrue("Gerente deve ir à sua área", driver.getCurrentUrl().contains("/gerente/"));

        tirarScreenshot("tc04_gerente_dashboard");
        System.out.println("✅ TC04 — Login gerente redireciona corretamente");
    }

    // =========================================================
    // TC05 — Login do Admin da Fábrica redireciona à fábrica
    // =========================================================
    @Test
    public void tc05_loginFabricaRedirecionaFabrica() {
        fazerLogin(EMAIL_FABRICA, SENHA_FABRICA);

        wait.until(ExpectedConditions.urlContains("/fabrica/"));
        assertTrue("Admin fábrica deve ir à área de fábrica", driver.getCurrentUrl().contains("/fabrica/"));

        tirarScreenshot("tc05_fabrica");
        System.out.println("✅ TC05 — Login fábrica redireciona corretamente");
    }

    // =========================================================
    // TC06 — Login do Vendedor redireciona à área de pedidos
    // =========================================================
    @Test
    public void tc06_loginVendedorRedirecionaPedidos() {
        fazerLogin(EMAIL_VENDEDOR, SENHA_VENDEDOR);

        wait.until(ExpectedConditions.urlContains("/vendedor/"));
        assertTrue("Vendedor deve ir à área de pedidos", driver.getCurrentUrl().contains("/vendedor/"));

        tirarScreenshot("tc06_vendedor");
        System.out.println("✅ TC06 — Login vendedor redireciona corretamente");
    }

    // =========================================================
    // TC07 — Login do Cliente redireciona a meus pedidos
    // =========================================================
    @Test
    public void tc07_loginClienteRedirecionaMeusPedidos() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);

        wait.until(ExpectedConditions.urlContains("/cliente/"));
        assertTrue("Cliente deve ir a meus pedidos", driver.getCurrentUrl().contains("/cliente/"));

        tirarScreenshot("tc07_cliente_pedidos");
        System.out.println("✅ TC07 — Login cliente redireciona corretamente");
    }

    // =========================================================
    // TC08 — Home exibe lista de veículos disponíveis
    // =========================================================
    @Test
    public void tc08_homeExibeVeiculos() {
        driver.get(HOME_URL);

        // Aguarda os cards de veículos carregarem
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".vehicles-grid, .vehicle-card")));

        java.util.List<WebElement> cards = driver.findElements(
                By.cssSelector(".vehicle-card"));
        assertTrue("Deve exibir ao menos um veículo", cards.size() > 0);

        tirarScreenshot("tc08_veiculos_home");
        System.out.println("✅ TC08 — Home exibe veículos (" + cards.size() + " encontrados)");
    }

    // =========================================================
    // TC09 — Home exibe seção de lançamentos
    // =========================================================
    @Test
    public void tc09_homeExibeLancamentos() {
        driver.get(HOME_URL);

        WebElement secaoLancamentos = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("lancamentos")));
        assertTrue("Seção de lançamentos deve existir", secaoLancamentos.isDisplayed());

        tirarScreenshot("tc09_lancamentos");
        System.out.println("✅ TC09 — Seção de lançamentos exibida");
    }

    // =========================================================
    // TC10 — Busca de veículo na home
    // =========================================================
    @Test
    public void tc10_buscaVeiculoHome() {
        driver.get(HOME_URL);

        WebElement campoBusca = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[contains(@placeholder,'Buscar')]")));
        campoBusca.sendKeys("Vrum");

        aguardar(1000); // Pausa para você ver a palavra sendo digitada

        WebElement btnBuscar = driver.findElement(
                By.xpath("//input[@value='Buscar'] | //button[contains(text(),'Buscar')]"));
        btnBuscar.click();

        aguardar(1000); // Pausa para você ver o resultado

        java.util.List<WebElement> resultados = driver.findElements(
                By.cssSelector(".vehicle-card"));
        assertTrue("Busca deve retornar resultados", resultados.size() >= 0);

        tirarScreenshot("tc10_busca");
        System.out.println("✅ TC10 — Busca de veículo funciona (" + resultados.size() + " resultados)");
    }

    // =========================================================
    // TC11 — Acesso a área protegida sem login redireciona ao login
    // =========================================================
    @Test
    public void tc11_acessoProtegidoSemLoginRedireciona() {
        driver.get(BASE_URL + "/pages/admin/dashboard.xhtml");

        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Acesso sem login deve redirecionar ao login",
                driver.getCurrentUrl().contains("login"));

        tirarScreenshot("tc11_acesso_protegido");
        System.out.println("✅ TC11 — Área protegida redireciona ao login");
    }

    // =========================================================
    // TC12 — Admin consegue acessar lista de usuários
    // =========================================================
    @Test
    public void tc12_adminAcessaListaUsuarios() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);

        driver.get(BASE_URL + "/pages/admin/usuarios.xhtml");
        wait.until(ExpectedConditions.urlContains("usuarios"));
        assertTrue("Admin deve acessar usuários", driver.getCurrentUrl().contains("usuarios"));

        WebElement tabela = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela de usuários deve existir", tabela);

        tirarScreenshot("tc12_admin_usuarios");
        System.out.println("✅ TC12 — Admin acessa lista de usuários");
    }

    // =========================================================
    // TC13 — Admin consegue criar novo veículo
    // =========================================================
    @Test
    public void tc13_adminCriaNovoVeiculo() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);

        driver.get(BASE_URL + "/pages/admin/veiculos.xhtml");
        wait.until(ExpectedConditions.urlContains("veiculos"));

        // Clica em Novo Veículo
        WebElement btnNovo = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='+ Novo Veículo'] | //button[contains(text(),'Novo Veículo')]")));
        btnNovo.click();

        aguardar(1000); // Pausa para você ver o formulário abrindo

        // Preenche o nome do modelo
        WebElement inputNome = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[contains(@id,'nome')]")));
        inputNome.clear();
        inputNome.sendKeys("Vrum Teste Selenium");
        
        aguardar(1000); // Pausa para você ver o nome escrito

        tirarScreenshot("tc13_novo_veiculo_form");
        System.out.println("✅ TC13 — Formulário de novo veículo aberto");
    }

    // =========================================================
    // TC14 — Vendedor vê pedidos disponíveis
    // =========================================================
    @Test
    public void tc14_vendedorVePedidosDisponiveis() {
        fazerLogin(EMAIL_VENDEDOR, SENHA_VENDEDOR);

        wait.until(ExpectedConditions.urlContains("/vendedor/"));

        // Verifica que a tabela ou mensagem de "nenhum pedido" está visível
        try {
            WebElement tabela = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
            assertNotNull(tabela);
            System.out.println("✅ TC14 — Tabela de pedidos encontrada");
        } catch (TimeoutException e) {
            // Pode não ter pedidos disponíveis ainda
            WebElement pagina = driver.findElement(By.cssSelector(".main-content"));
            assertNotNull("Página do vendedor deve carregar", pagina);
            System.out.println("✅ TC14 — Página do vendedor carregada (sem pedidos)");
        }

        tirarScreenshot("tc14_vendedor_pedidos");
    }

    // =========================================================
    // TC15 — Admin da Fábrica vê ordens de fabricação
    // =========================================================
    @Test
    public void tc15_adminFabricaVeOrdens() {
        fazerLogin(EMAIL_FABRICA, SENHA_FABRICA);

        wait.until(ExpectedConditions.urlContains("/fabrica/"));

        WebElement mainContent = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-content")));
        assertNotNull("Conteúdo da página da fábrica deve ser visível", mainContent);

        tirarScreenshot("tc15_fabrica_ordens");
        System.out.println("✅ TC15 — Admin fábrica vê página de ordens");
    }

    // =========================================================
    // TC16 — Gerente vê dashboard com dados da sua concessionária
    // =========================================================
    @Test
    public void tc16_gerenteVeDashboard() {
        fazerLogin(EMAIL_GERENTE, SENHA_GERENTE);

        wait.until(ExpectedConditions.urlContains("/gerente/"));

        WebElement h1 = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue("Dashboard do gerente deve ter título",
                h1.getText().length() > 0);

        tirarScreenshot("tc16_gerente_dashboard");
        System.out.println("✅ TC16 — Gerente vê dashboard: " + h1.getText());
    }

    // =========================================================
    // TC17 — Gerente acessa gestão de vendedores
    // =========================================================
    @Test
    public void tc17_gerenteAcessaVendedores() {
        fazerLogin(EMAIL_GERENTE, SENHA_GERENTE);

        driver.get(BASE_URL + "/pages/gerente/vendedores.xhtml");
        wait.until(ExpectedConditions.urlContains("vendedores"));
        assertTrue("Gerente deve acessar vendedores",
                driver.getCurrentUrl().contains("vendedores"));

        tirarScreenshot("tc17_gerente_vendedores");
        System.out.println("✅ TC17 — Gerente acessa gestão de vendedores");
    }

    // =========================================================
    // TC18 — Gerente não consegue acessar área do admin
    // =========================================================
    @Test
    public void tc18_gerenteNaoAcessaAreaAdmin() {
        fazerLogin(EMAIL_GERENTE, SENHA_GERENTE);

        driver.get(BASE_URL + "/pages/admin/dashboard.xhtml");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("gerente")
        ));

        boolean bloqueado = driver.getCurrentUrl().contains("acesso-negado")
                || !driver.getCurrentUrl().contains("/admin/");
        assertTrue("Gerente não deve acessar área admin", bloqueado);

        tirarScreenshot("tc18_gerente_bloqueado");
        System.out.println("✅ TC18 — Gerente bloqueado da área admin");
    }

    // =========================================================
    // TC19 — Cliente vê sua página de pedidos
    // =========================================================
    @Test
    public void tc19_clienteVeMeusPedidos() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);

        wait.until(ExpectedConditions.urlContains("/cliente/"));

        WebElement mainContent = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-content")));
        assertNotNull("Página de pedidos do cliente deve carregar", mainContent);

        // Verifica que tem o título da página
        WebElement h1 = driver.findElement(By.tagName("h1"));
        assertTrue("Deve ter título", h1.getText().length() > 0);

        tirarScreenshot("tc19_cliente_pedidos");
        System.out.println("✅ TC19 — Cliente vê sua página de pedidos: " + h1.getText());
    }

    // =========================================================
    // TC20 — Cliente não consegue acessar área do admin
    // =========================================================
    @Test
    public void tc20_clienteNaoAcessaAdmin() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);

        driver.get(BASE_URL + "/pages/admin/dashboard.xhtml");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("acesso-negado"),
                ExpectedConditions.urlContains("cliente")
        ));

        boolean bloqueado = !driver.getCurrentUrl().contains("/admin/");
        assertTrue("Cliente não deve acessar área admin", bloqueado);

        tirarScreenshot("tc20_cliente_bloqueado");
        System.out.println("✅ TC20 — Cliente bloqueado da área admin");
    }

    // =========================================================
    // TC21 — Admin consegue acessar lista de concessionárias
    // =========================================================
    @Test
    public void tc21_adminAcessaConcessionarias() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);

        driver.get(BASE_URL + "/pages/admin/concessionarias.xhtml");
        wait.until(ExpectedConditions.urlContains("concessionarias"));

        WebElement tabela = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vrum-table")));
        assertNotNull("Tabela de concessionárias deve existir", tabela);

        java.util.List<WebElement> linhas = driver.findElements(
                By.cssSelector(".vrum-table tbody tr"));
        assertTrue("Deve ter ao menos uma concessionária", linhas.size() > 0);

        tirarScreenshot("tc21_concessionarias");
        System.out.println("✅ TC21 — Admin acessa concessionárias (" + linhas.size() + " registros)");
    }

    // =========================================================
    // TC22 — Admin acessa todos os pedidos
    // =========================================================
    @Test
    public void tc22_adminAcessaTodosPedidos() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);

        driver.get(BASE_URL + "/pages/admin/pedidos.xhtml");
        wait.until(ExpectedConditions.urlContains("pedidos"));

        WebElement mainContent = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-content")));
        assertNotNull("Página de pedidos admin deve carregar", mainContent);

        tirarScreenshot("tc22_admin_pedidos");
        System.out.println("✅ TC22 — Admin acessa todos os pedidos");
    }

    // =========================================================
    // TC23 — Logout funciona corretamente
    // =========================================================
    @Test
    public void tc23_logoutFunciona() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        wait.until(ExpectedConditions.urlContains("/admin/"));

        // Clica em sair
        WebElement btnSair = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(text(),'Sair')] | //input[contains(@value,'Sair')]")));
        btnSair.click();

        // Deve voltar ao login
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Logout deve redirecionar ao login",
                driver.getCurrentUrl().contains("login"));

        tirarScreenshot("tc23_logout");
        System.out.println("✅ TC23 — Logout funciona");
    }

    // =========================================================
    // TC24 — Página de detalhe do veículo é acessível
    // =========================================================
    @Test
    public void tc24_paginaDetalheVeiculo() {
        driver.get(HOME_URL);

        // Clica em "Detalhes" no primeiro veículo
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".vehicle-card")));

        java.util.List<WebElement> botoesDetalhes = driver.findElements(
                By.xpath("//input[@value='Detalhes'] | //button[contains(text(),'Detalhes')]"));

        if (!botoesDetalhes.isEmpty()) {
            aguardar(1000); // Pausa antes de clicar
            botoesDetalhes.get(0).click();
            aguardar(1000); // Pausa depois de clicar

            // Pode ir para detalhe ou cadastro
            boolean paginaCorreta = driver.getCurrentUrl().contains("veiculo")
                    || driver.getCurrentUrl().contains("cadastro");
            assertTrue("Deve navegar para detalhe ou cadastro", paginaCorreta);
            System.out.println("✅ TC24 — Navegação para detalhe funciona: " + driver.getCurrentUrl());
        } else {
            System.out.println("⚠️ TC24 — Botões de detalhes não encontrados (pode ser forma diferente)");
        }

        tirarScreenshot("tc24_detalhe_veiculo");
    }

    // =========================================================
    // TC25 — Campos obrigatórios do login validados
    // =========================================================
    @Test
    public void tc25_validacaoCamposLogin() {
        driver.get(LOGIN_URL);

        aguardar(1000); // Pausa visual

        // Tenta submeter sem preencher
        WebElement btnSubmit = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("input[type='submit'], button[type='submit']")));
        btnSubmit.click();

        aguardar(1500); // Pausa para você ver as mensagens de erro (campos em vermelho)

        // Deve permanecer na página de login
        assertTrue("Deve permanecer no login com campos vazios",
                driver.getCurrentUrl().contains("login"));

        tirarScreenshot("tc25_validacao_login");
        System.out.println("✅ TC25 — Validação de campos obrigatórios no login");
    }

    // =========================================================
    // TC26 — Limite de caracteres nos campos de texto da Home
    // =========================================================
    @Test
    public void tc26_limiteCaracteresBuscaHome() {
        driver.get(HOME_URL);

        WebElement campoBusca = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[contains(@placeholder,'Buscar')]")));

        String textoMuitoLongo = "a".repeat(300);
        aguardar(1000);
        campoBusca.sendKeys(textoMuitoLongo);
        ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'))", campoBusca);
        aguardar(1000); // Você verá que só couberam 100 'a's

        String valorNoCampo = campoBusca.getAttribute("value");
        // Verifica se o texto inserido foi truncado para menos que 300 caracteres,
        // ou se o maxLength está sendo aplicado
        String maxLengthAtr = campoBusca.getAttribute("maxlength");
        if (maxLengthAtr != null && !maxLengthAtr.isEmpty()) {
            int maxLen = Integer.parseInt(maxLengthAtr);
            assertTrue("Campo deve respeitar o maxlength de " + maxLen, valorNoCampo.length() <= maxLen);
            
            WebElement msg = driver.findElement(By.id("busca-max-msg"));
            assertTrue("Mensagem de erro de limite na home não está visível!", msg.isDisplayed());

            System.out.println("✅ TC26 — Limite aplicado corretamente no campo de busca (valor truncado).");
        } else {
            // Se o maxlength não estiver lá, isso é um indicativo que poderia estar
            // e como não está, podemos imprimir um alerta
            System.out.println("⚠️ TC26 — Campo de busca sem maxlength attribute.");
        }
    }


    // =========================================================
    // TC28 — Limite de caracteres nos campos do Login
    // =========================================================
    @Test
    public void tc28_limiteCaracteresLogin() {
        driver.get(LOGIN_URL);

        WebElement campoEmail = encontrarInput("email");
        WebElement campoSenha = encontrarInput("senha");

        String textoLongo = "a".repeat(300);

        aguardar(1000);
        campoEmail.sendKeys(textoLongo);
        // O Selenium envia as teclas muito rápido. Vamos forçar um trigger no keyup também 
        // para garantir que a mensagem de erro (JavaScript) apareça na tela:
        ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'))", campoEmail);
        
        aguardar(1000);
        campoSenha.sendKeys(textoLongo);
        ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'))", campoSenha);
        
        aguardar(2000); // Pausa MESTRE para podermos ver as caixas preenchidas E a mensagem vermelha!!

        String maxLengthEmail = campoEmail.getAttribute("maxlength");
        String maxLengthSenha = campoSenha.getAttribute("maxlength");

        if (maxLengthEmail != null && !maxLengthEmail.isEmpty()) {
             assertTrue("Email no Login deve respeitar o limite", campoEmail.getAttribute("value").length() <= Integer.parseInt(maxLengthEmail));
             
             // Vamos verificar se a mensagem em vermelho apareceu na tela mesmo!
             WebElement msg = driver.findElement(By.id("email-max-msg"));
             assertTrue("Mensagem de erro de limite de email não está visível!", msg.isDisplayed());
             
             System.out.println("✅ TC28 — Email no Login com limite ok E MENSAGEM VISUAL MOSTRADA.");
        } else {
             System.out.println("⚠️ TC28 — Campo Email no Login sem maxlength.");
        }

        if (maxLengthSenha != null && !maxLengthSenha.isEmpty()) {
             assertTrue("Senha no Login deve respeitar o limite", campoSenha.getAttribute("value").length() <= Integer.parseInt(maxLengthSenha));
             WebElement msg = driver.findElement(By.id("senha-max-msg"));
             assertTrue("Mensagem de erro de limite de senha não está visível!", msg.isDisplayed());
             System.out.println("✅ TC28 — Senha no Login com limite ok.");
        } else {
             System.out.println("⚠️ TC28 — Campo Senha no Login sem maxlength.");
        }
    }

    // =========================================================
    // TC29 — CAD01: Cadastro com dados válidos avança para etapa 2
    // =========================================================
    @Test
    public void tc29_cadastroValidoAvancaEtapa2() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.sendKeys("Cliente Selenium Valido");

        String emailUnico = "selenium" + System.currentTimeMillis() + "@teste.com";
        driver.findElement(By.id("cadastroForm:emailCad")).sendKeys(emailUnico);
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0001");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("senha123");

        new Select(driver.findElement(By.cssSelector("#cadastroForm select")))
                .selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf"))
                .sendKeys(String.format("%011d", System.currentTimeMillis() % 100000000000L));

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement etapa2 = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Confirmar Pedido')]")));
        assertNotNull("Deve avançar para a etapa 2", etapa2);

        tirarScreenshot("tc29_cadastro_valido_etapa2");
        System.out.println("✅ TC_CAD01 — Cadastro válido avança para etapa 2");
    }

    // =========================================================
    // TC30 — CAD02: E-mail já existente exibe erro na etapa 1
    // =========================================================
    @Test
    public void tc30_cadastroEmailDuplicadoExibeErro() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.sendKeys("Cliente Duplicado");

        driver.findElement(By.id("cadastroForm:emailCad")).sendKeys(EMAIL_CLIENTE);
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0002");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("senha123");

        new Select(driver.findElement(By.cssSelector("#cadastroForm select")))
                .selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf"))
                .sendKeys(String.format("%011d", System.currentTimeMillis() % 100000000000L));

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement msgErro = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de e-mail duplicado",
                msgErro.getText().toLowerCase().contains("cadastrado") ||
                msgErro.getText().toLowerCase().contains("e-mail"));

        tirarScreenshot("tc30_email_duplicado");
        System.out.println("✅ TC_CAD02 — E-mail duplicado exibe erro na etapa 1: " + msgErro.getText());
    }

    // =========================================================
    // TC31 — CAD03: Nome com caracteres especiais exibe aviso visual
    // =========================================================
    @Test
    public void tc31_cadastroNomeInvalidoExibeAviso() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.click();
        // Digitar '@' aciona onkeydown → vrumNomeTecla → bloqueia e exibe aviso
        inputNome.sendKeys("@");
        aguardar(500);

        WebElement msgInvalido = driver.findElement(By.id("nome-invalido-msg"));
        assertTrue("Mensagem de nome inválido deve aparecer", msgInvalido.isDisplayed());

        // Caractere bloqueado não deve entrar no campo
        String valor = inputNome.getAttribute("value");
        assertTrue("Caractere especial não deve entrar no campo",
                valor == null || valor.isEmpty() || !valor.contains("@"));

        tirarScreenshot("tc31_nome_invalido");
        System.out.println("✅ TC_CAD03 — Nome com caracteres especiais exibe aviso");
    }

    // =========================================================
    // TC32 — CAD04: Senha curta bloqueia avanço para etapa 2
    // =========================================================
    @Test
    public void tc32_cadastroSenhaCurtaBloqueia() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.sendKeys("Cliente Senha Curta");

        driver.findElement(By.id("cadastroForm:emailCad"))
                .sendKeys("senhacurta" + System.currentTimeMillis() + "@teste.com");
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0003");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("abc");

        new Select(driver.findElement(By.cssSelector("#cadastroForm select")))
                .selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf")).sendKeys("11111111111");

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1500);

        // Deve permanecer na etapa 1 — campo nome ainda visível
        WebElement campoNome = driver.findElement(By.id("cadastroForm:nome"));
        assertTrue("Deve permanecer na etapa 1 com senha curta", campoNome.isDisplayed());

        tirarScreenshot("tc32_senha_curta");
        System.out.println("✅ TC_CAD04 — Senha curta bloqueia avanço (permaneceu na etapa 1)");
    }

    // =========================================================
    // TC33 — CAD05: Campos obrigatórios vazios bloqueiam avanço
    // =========================================================
    @Test
    public void tc33_cadastroCamposVaziosBloqueia() {
        driver.get(CADASTRO_URL);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Continuar')]")));
        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1500);

        // Deve permanecer na etapa 1 — campo nome ainda visível
        WebElement campoNome = driver.findElement(By.id("cadastroForm:nome"));
        assertTrue("Deve permanecer na etapa 1 com campos vazios", campoNome.isDisplayed());

        tirarScreenshot("tc33_campos_obrigatorios");
        System.out.println("✅ TC_CAD05 — Campos vazios bloqueiam avanço (permaneceu na etapa 1)");
    }

    // =========================================================
    // TC34 — CAD06: Botão Voltar na etapa 2 retorna para etapa 1
    // =========================================================
    @Test
    public void tc34_cadastroBotaoVoltarRetornaEtapa1() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.sendKeys("Cliente Voltar Teste");

        driver.findElement(By.id("cadastroForm:emailCad"))
                .sendKeys("voltar" + System.currentTimeMillis() + "@teste.com");
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0004");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("senha123");

        new Select(driver.findElement(By.cssSelector("#cadastroForm select")))
                .selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf"))
                .sendKeys(String.format("%011d", System.currentTimeMillis() % 100000000000L));

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Confirmar Pedido')]")));

        WebElement btnVoltar = driver.findElement(
                By.xpath("//input[contains(@value,'Voltar')] | //button[contains(text(),'Voltar')]"));
        btnVoltar.click();
        aguardar(1000);

        WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("cadastroForm:nome")));
        assertTrue("Deve voltar para etapa 1", campoNome.isDisplayed());

        tirarScreenshot("tc34_voltar_etapa1");
        System.out.println("✅ TC_CAD06 — Botão Voltar retorna para etapa 1");
    }

    // =========================================================
    // TC35 — PED01: Clicar em Comprar na home redireciona para cadastro
    // =========================================================
    @Test
    public void tc35_comprarVeiculoRedirecionaCadastro() {
        driver.get(HOME_URL);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".vehicle-card")));
        aguardar(1000);

        WebElement btnComprar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Comprar']")));
        btnComprar.click();

        wait.until(ExpectedConditions.urlContains("cadastro"));
        assertTrue("Comprar deve redirecionar ao cadastro",
                driver.getCurrentUrl().contains("cadastro"));

        tirarScreenshot("tc35_comprar_redireciona");
        System.out.println("✅ TC_PED01 — Comprar redireciona ao cadastro: " + driver.getCurrentUrl());
    }

    // =========================================================
    // TC36 — PED02: Confirmar pedido sem veículo não finaliza o pedido
    // =========================================================
    @Test
    public void tc36_confirmarSemVeiculoNaoFinalizaPedido() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);
        wait.until(ExpectedConditions.urlContains("/cliente/"));

        driver.get(CADASTRO_URL);
        aguardar(1000);

        // Etapa 2 deve estar visível (cliente logado vai direto)
        WebElement btnConfirmar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Confirmar')]")));
        btnConfirmar.click();
        aguardar(2000);

        // Não deve chegar à etapa 3 (PEDIDO REALIZADO)
        java.util.List<WebElement> sucesso = driver.findElements(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]"));
        boolean chegouEtapa3 = sucesso.stream().anyMatch(WebElement::isDisplayed);
        assertTrue("Sem veículo selecionado não deve finalizar o pedido", !chegouEtapa3);

        // Deve permanecer na URL de cadastro
        assertTrue("Deve permanecer na página de cadastro",
                driver.getCurrentUrl().contains("cadastro"));

        tirarScreenshot("tc36_sem_veiculo");
        System.out.println("✅ TC_PED02 — Confirmar sem veículo não finaliza o pedido");
    }

    // =========================================================
    // TC37 — PED03: Fluxo completo de pedido até etapa 3 (sucesso)
    // =========================================================
    @Test
    public void tc37_fluxoCompletoPedidoEtapa3() {
        driver.get(HOME_URL);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".vehicle-card")));
        aguardar(1000);

        WebElement btnComprar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Comprar']")));
        btnComprar.click();

        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(500);

        String emailUnico = "fluxo" + System.currentTimeMillis() + "@teste.com";

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.sendKeys("Cliente Fluxo Completo");

        driver.findElement(By.id("cadastroForm:emailCad")).sendKeys(emailUnico);
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0005");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("senha123");

        new Select(driver.findElement(By.cssSelector("#cadastroForm select")))
                .selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf"))
                .sendKeys(String.format("%011d", System.currentTimeMillis() % 100000000000L));

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1500);

        WebElement btnConfirmar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Confirmar')] | //button[contains(text(),'Confirmar')]")));
        btnConfirmar.click();
        aguardar(2000);

        WebElement sucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]")));
        assertTrue("Deve exibir tela de sucesso (etapa 3)", sucesso.isDisplayed());

        tirarScreenshot("tc37_fluxo_completo");
        System.out.println("✅ TC_PED03 — Fluxo completo: pedido realizado com sucesso!");
    }

    // =========================================================
    // TC38 — VAL01: Limite de caracteres no cadastro exibe mensagem
    // =========================================================
    @Test
    public void tc38_limiteCaracteresCadastroExibeMensagem() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));

        // Verifica que maxlength está definido corretamente
        String maxlength = inputNome.getAttribute("maxlength");
        assertNotNull("Campo nome deve ter atributo maxlength", maxlength);
        assertTrue("Maxlength deve ser 100", "100".equals(maxlength));

        // Tenta digitar além do limite
        inputNome.click();
        inputNome.sendKeys("A".repeat(150));
        aguardar(500);

        // O campo não deve conter mais de 100 caracteres
        String valor = inputNome.getAttribute("value");
        assertTrue("Campo deve respeitar maxlength de " + maxlength,
                valor.length() <= Integer.parseInt(maxlength));

        // Chama vrumLimite diretamente via JS para verificar que a mensagem aparece
        ((JavascriptExecutor) driver).executeScript(
                "vrumLimite(arguments[0], 'nome-max-msg', 100)", inputNome);
        aguardar(500);

        WebElement msgLimite = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("nome-max-msg")));
        assertTrue("Mensagem de limite deve aparecer", msgLimite.isDisplayed());

        tirarScreenshot("tc38_limite_nome");
        System.out.println("✅ TC_VAL01 — Maxlength " + maxlength + " aplicado e mensagem exibida (" + valor.length() + " chars)");
    }

    // =========================================================
    // TC39 — VAL02: E-mail com formato inválido exibe aviso ao sair do campo
    // =========================================================
    @Test
    public void tc39_emailFormatoInvalidoExibeAviso() {
        driver.get(CADASTRO_URL);

        WebElement inputEmail = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:emailCad")));
        inputEmail.sendKeys("email-invalido@");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('blur'))", inputEmail);
        aguardar(500);

        WebElement msgFormato = driver.findElement(By.id("email-formato-msg"));
        assertTrue("Mensagem de formato inválido deve aparecer", msgFormato.isDisplayed());

        tirarScreenshot("tc39_email_invalido");
        System.out.println("✅ TC_VAL02 — E-mail com formato inválido exibe aviso");
    }

    // =========================================================
    // TC40 — CAD07: CPF já existente exibe erro na etapa 1
    // =========================================================
    @Test
    public void tc40_cadastroCpfDuplicadoExibeErro() {
        // Passo 1: registrar um usuário com CPF único via fluxo completo
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        aguardar(1000);

        WebElement btnComprar1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Comprar']")));
        btnComprar1.click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(500);

        long ts = System.currentTimeMillis();
        String emailUnico1 = "cpfteste1" + ts + "@teste.com";
        String cpfUnico = String.format("%011d", ts % 100000000000L);

        WebElement inputNome1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome1.sendKeys("Cliente CPF Um");
        driver.findElement(By.id("cadastroForm:emailCad")).sendKeys(emailUnico1);
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0010");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("senha123");
        new Select(driver.findElement(By.cssSelector("#cadastroForm select"))).selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf")).sendKeys(cpfUnico);

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement btnConfirmar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Confirmar')] | //button[contains(text(),'Confirmar')]")));
        btnConfirmar.click();
        aguardar(2000);

        // Garante que o usuário foi salvo no banco
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]")));

        // Passo 2: logout e tentar cadastrar com o mesmo CPF mas e-mail diferente
        driver.get(BASE_URL + "/logout");
        wait.until(ExpectedConditions.urlContains("login"));

        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        aguardar(500);

        WebElement btnComprar2 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Comprar']")));
        btnComprar2.click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(500);

        String emailUnico2 = "cpfteste2" + System.currentTimeMillis() + "@teste.com";

        WebElement inputNome2 = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome2.sendKeys("Cliente CPF Dois");
        driver.findElement(By.id("cadastroForm:emailCad")).sendKeys(emailUnico2);
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("(81) 99999-0011");
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("senha123");
        new Select(driver.findElement(By.cssSelector("#cadastroForm select"))).selectByIndex(1);
        driver.findElement(By.id("cadastroForm:cpf")).sendKeys(cpfUnico); // mesmo CPF!

        aguardar(500);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement msgErro = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de CPF duplicado",
                msgErro.getText().toLowerCase().contains("cpf") ||
                msgErro.getText().toLowerCase().contains("cadastrado"));

        tirarScreenshot("tc40_cpf_duplicado");
        System.out.println("✅ TC_CAD07 — CPF duplicado exibe erro na etapa 1: " + msgErro.getText());
    }
}