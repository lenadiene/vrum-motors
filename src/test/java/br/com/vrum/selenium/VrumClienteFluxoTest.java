package br.com.vrum.selenium;

import java.time.Duration;
import java.util.List;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.*;

/**
 * Testes de fluxo do cliente — Vrum Motors
 *
 * Cobre: acesso público, cadastro, validações, pedidos e segurança de sessão.
 *
 * Pré-requisitos:
 *   - Aplicação rodando em http://localhost:8080/vrum-motors
 *   - Banco populado pelo DataInicializador
 *   - Google Chrome instalado
 *
 * Execução: mvn test
 */
public class VrumClienteFluxoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL     = "http://localhost:8080/vrum-motors";
    private static final String HOME_URL     = BASE_URL + "/home.xhtml";
    private static final String LOGIN_URL    = BASE_URL + "/login.xhtml";
    private static final String CADASTRO_URL = BASE_URL + "/cadastro.xhtml";
    private static final String PEDIDOS_URL  = BASE_URL + "/pages/cliente/meus-pedidos.xhtml";

    private static final String EMAIL_CLIENTE  = "cliente@email.com";
    private static final String SENHA_CLIENTE  = "cliente123";
    private static final String EMAIL_ADMIN    = "admin@vrummotors.com";
    private static final String SENHA_ADMIN    = "admin123";
    private static final String EMAIL_GERENTE  = "gerente.recife@vrummotors.com";
    private static final String SENHA_GERENTE  = "gerente123";
    private static final String EMAIL_VENDEDOR = "vendedor@vrummotors.com";
    private static final String SENHA_VENDEDOR = "vendedor123";
    private static final String EMAIL_FABRICA  = "fabrica@vrummotors.com";
    private static final String SENHA_FABRICA  = "fabrica123";

    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @Before
    public void limparSessao() {
        // Limpa cookies para forçar nova sessão no browser
        try { driver.manage().deleteAllCookies(); } catch (Exception ignored) {}
        // Navega para uma URL estática antes do logout para criar um cookie limpo
        try {
            driver.get(BASE_URL + "/login.xhtml");
            aguardar(300);
            driver.get(BASE_URL + "/logout");
            aguardar(500);
        } catch (Exception ignored) {}
    }

    @After
    public void logout() {
        aguardar(1500);
        try {
            driver.get(BASE_URL + "/logout");
            wait.until(ExpectedConditions.urlContains("login"));
        } catch (Exception ignored) {}
    }

    // =========================================================
    // GRUPO 1 — ACESSO PÚBLICO
    // =========================================================

    /** CF01 — Tela de cadastro é pública (sem login). */
    @Test
    public void cf01_telaCadastroPublica() {
        driver.get(CADASTRO_URL);
        aguardar(800);

        assertFalse("Cadastro não deve redirecionar ao login",
                driver.getCurrentUrl().contains("login"));

        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("cadastroForm")));
        assertNotNull("Formulário de cadastro deve existir", form);

        WebElement inputNome = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("cadastroForm:nome")));
        assertTrue("Campo nome deve estar visível", inputNome.isDisplayed());

        System.out.println("✅ CF01 — Tela de cadastro é pública");
    }

    /** CF02 — Todos os botões "Comprar" na home levam ao cadastro. */
    @Test
    public void cf02_todosComprarLevaoCadastro() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));

        int total = driver.findElements(By.xpath("//input[@value='Comprar']")).size();
        assertTrue("Deve haver ao menos um botão Comprar", total > 0);

        for (int i = 0; i < total; i++) {
            driver.get(HOME_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
            driver.navigate().refresh();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
            aguardar(500);

            List<WebElement> botoes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath("//input[@value='Comprar']")));
            botoes.get(i).click();
            wait.until(ExpectedConditions.urlContains("cadastro"));

            assertTrue("Botão Comprar #" + (i + 1) + " deve levar ao cadastro",
                    driver.getCurrentUrl().contains("cadastro"));
            System.out.println("   ✔ Botão #" + (i + 1) + " → " + driver.getCurrentUrl());
        }

        System.out.println("✅ CF02 — Todos os " + total + " botões Comprar levam ao cadastro");
    }

    // =========================================================
    // GRUPO 2 — VALIDAÇÕES DO FORMULÁRIO DE CADASTRO
    // =========================================================

    /** CF03 — Campos obrigatórios vazios bloqueiam o avanço. */
    @Test
    public void cf03_camposVaziosBloqueiamAvanco() {
        driver.get(CADASTRO_URL);
        aguardar(500);

        // Botão deve estar desabilitado (cinza) com campos vazios
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("cadastroForm:btnContinuar")));
        assertTrue("Botão Continuar deve estar desabilitado com campos vazios",
                btn.getAttribute("disabled") != null || !btn.isEnabled());

        System.out.println("✅ CF03 — Campos vazios mantêm botão desabilitado");
    }

    /** CF04 — Caractere especial no nome é bloqueado e exibe aviso. */
    @Test
    public void cf04_nomeCaractereEspecialBloqueado() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.click();
        // Dispara keydown via JS para garantir que o handler de bloqueio executa
        ((JavascriptExecutor) driver).executeScript(
                "var e = new KeyboardEvent('keydown', {key:'@', bubbles:true, cancelable:true});" +
                "arguments[0].dispatchEvent(e);", inputNome);
        aguardar(600);

        WebElement aviso = driver.findElement(By.id("nome-invalido-msg"));
        assertTrue("Aviso de nome inválido deve aparecer", aviso.isDisplayed());

        String valor = inputNome.getAttribute("value");
        assertFalse("Caractere '@' não deve entrar no campo",
                valor != null && valor.contains("@"));

        System.out.println("✅ CF04 — Caractere especial bloqueado no nome");
    }

    /** CF05 — Três letras iguais consecutivas são bloqueadas no nome. */
    @Test
    public void cf05_nomeTresLetrasIguaisBloqueia() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.click();
        inputNome.sendKeys("aaa");
        aguardar(400);

        // A terceira letra igual não deve entrar — campo deve ter no máximo "aa"
        String valor = inputNome.getAttribute("value");
        assertFalse("Três letras iguais não devem entrar no campo",
                valor != null && valor.contains("aaa"));

        System.out.println("✅ CF05 — Três letras iguais consecutivas bloqueadas");
    }

    /** CF06 — E-mail com formato inválido exibe aviso ao sair do campo. */
    @Test
    public void cf06_emailFormatoInvalidoExibeAviso() {
        driver.get(CADASTRO_URL);

        WebElement inputEmail = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:emailCad")));
        inputEmail.sendKeys("invalido@");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('blur'))", inputEmail);
        aguardar(400);

        WebElement aviso = driver.findElement(By.id("email-formato-msg"));
        assertTrue("Aviso de e-mail inválido deve aparecer", aviso.isDisplayed());

        System.out.println("✅ CF06 — E-mail inválido exibe aviso");
    }

    /** CF07 — CPF inválido exibe aviso ao sair do campo. */
    @Test
    public void cf07_cpfInvalidoExibeAviso() {
        driver.get(CADASTRO_URL);

        WebElement inputCpf = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:cpf")));
        inputCpf.click();
        inputCpf.sendKeys("12345678900"); // CPF com dígitos verificadores errados
        ((JavascriptExecutor) driver).executeScript(
                "if(typeof vrumCPFMascara==='function') vrumCPFMascara(arguments[0]);", inputCpf);
        aguardar(400);
        ((JavascriptExecutor) driver).executeScript(
                "if(typeof vrumCPFBlur==='function') vrumCPFBlur(arguments[0]);", inputCpf);
        aguardar(600);

        WebElement aviso = driver.findElement(By.id("cpf-formato-msg"));
        assertTrue("Aviso de CPF inválido deve aparecer", aviso.isDisplayed());

        System.out.println("✅ CF07 — CPF inválido exibe aviso");
    }

    /** CF08 — Máscara do telefone formata automaticamente ao digitar. */
    @Test
    public void cf08_mascaraTelefoneFormata() {
        driver.get(CADASTRO_URL);

        WebElement inputTel = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:telefone")));
        inputTel.click();
        inputTel.sendKeys("81987654321");
        ((JavascriptExecutor) driver).executeScript(
                "if(typeof vrumTelefoneMascara==='function') vrumTelefoneMascara(arguments[0]);", inputTel);
        aguardar(600);

        String valor = inputTel.getAttribute("value");
        assertTrue("Telefone deve estar formatado com máscara",
                valor.contains("(") && valor.contains(")") && valor.contains("-"));
        assertEquals("Formato esperado: (81) 98765-4321", "(81) 98765-4321", valor);

        System.out.println("✅ CF08 — Máscara do telefone aplicada: " + valor);
    }

    /** CF09 — Máscara do CPF formata automaticamente ao digitar. */
    @Test
    public void cf09_mascaraCPFFormata() {
        driver.get(CADASTRO_URL);

        WebElement inputCpf = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:cpf")));
        inputCpf.click();
        inputCpf.sendKeys("52998224725");
        ((JavascriptExecutor) driver).executeScript(
                "if(typeof vrumCPFMascara==='function') vrumCPFMascara(arguments[0]);", inputCpf);
        aguardar(600);

        String valor = inputCpf.getAttribute("value");
        assertTrue("CPF deve estar formatado com máscara",
                valor.contains(".") && valor.contains("-"));
        assertEquals("Formato esperado: 529.982.247-25", "529.982.247-25", valor);

        System.out.println("✅ CF09 — Máscara do CPF aplicada: " + valor);
    }

    /** CF10 — Senha curta mantém botão desabilitado. */
    @Test
    public void cf10_senhaCurtaMantemBotaoDesabilitado() {
        driver.get(CADASTRO_URL);

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.sendKeys("Cliente Teste");
        driver.findElement(By.id("cadastroForm:emailCad"))
                .sendKeys("teste" + System.currentTimeMillis() + "@teste.com");
        driver.findElement(By.id("cadastroForm:telefone")).sendKeys("81987654321");
        driver.findElement(By.id("cadastroForm:cpf")).sendKeys("52998224725");
        new Select(driver.findElement(By.cssSelector("#cadastroForm select"))).selectByIndex(1);
        driver.findElement(By.id("cadastroForm:senha")).sendKeys("abc"); // curta
        aguardar(500);

        WebElement btn = driver.findElement(By.id("cadastroForm:btnContinuar"));
        assertTrue("Botão deve permanecer desabilitado com senha curta",
                btn.getAttribute("disabled") != null || !btn.isEnabled());

        System.out.println("✅ CF10 — Senha curta mantém botão desabilitado");
    }

    // =========================================================
    // GRUPO 3 — UNICIDADE DE DADOS
    // =========================================================

    /** CF11 — E-mail já cadastrado exibe erro no servidor. */
    @Test
    public void cf11_emailDuplicadoExibeErro() {
        driver.get(CADASTRO_URL);

        preencherEtapa1("Cliente Email Dup", EMAIL_CLIENTE, gerarTelefone(),
                "senha123", gerarCPFValido());

        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement erro = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de e-mail duplicado",
                erro.getText().toLowerCase().contains("e-mail") ||
                erro.getText().toLowerCase().contains("cadastrado"));

        System.out.println("✅ CF11 — E-mail duplicado exibe erro: " + erro.getText());
    }

    /** CF12 — CPF já cadastrado exibe erro no servidor. */
    @Test
    public void cf12_cpfDuplicadoExibeErro() {
        // Passo 1: registra com CPF único
        String cpf = gerarCPFValido();
        realizarCadastroCompleto("Cliente CPF Orig", gerarTelefone(), cpf);

        driver.get(BASE_URL + "/logout");
        wait.until(ExpectedConditions.urlContains("login"));

        // Passo 2: tenta cadastrar com mesmo CPF
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1("Cliente CPF Dup", "cpfdup" + System.currentTimeMillis() + "@teste.com",
                gerarTelefone(), "senha123", cpf);
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement erro = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de CPF duplicado",
                erro.getText().toLowerCase().contains("cpf") ||
                erro.getText().toLowerCase().contains("cadastrado"));

        System.out.println("✅ CF12 — CPF duplicado exibe erro: " + erro.getText());
    }

    /** CF13 — Telefone já cadastrado exibe erro no servidor. */
    @Test
    public void cf13_telefoneDuplicadoExibeErro() {
        String telefone = gerarTelefone();

        // Passo 1: registra com telefone único
        realizarCadastroCompleto("Cliente Tel Orig", telefone, gerarCPFValido());

        driver.get(BASE_URL + "/logout");
        wait.until(ExpectedConditions.urlContains("login"));

        // Passo 2: tenta cadastrar com mesmo telefone
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1("Cliente Tel Dup", "teldup" + System.currentTimeMillis() + "@teste.com",
                telefone, "senha123", gerarCPFValido());
        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement erro = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".msg-error")));
        assertTrue("Deve exibir erro de telefone duplicado",
                erro.getText().toLowerCase().contains("telefone") ||
                erro.getText().toLowerCase().contains("cadastrado"));

        System.out.println("✅ CF13 — Telefone duplicado exibe erro: " + erro.getText());
    }

    // =========================================================
    // GRUPO 4 — FLUXO COMPLETO DE CADASTRO E PEDIDO
    // =========================================================

    /** CF14 — Dados válidos avançam para etapa 2 (confirmar). */
    @Test
    public void cf14_dadosValidosAvancamEtapa2() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1("Cliente Valido Etapa2", "valido" + System.currentTimeMillis() + "@teste.com",
                gerarTelefone(), "senha123", gerarCPFValido());

        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        WebElement etapa2 = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Confirmar Pedido')]")));
        assertNotNull("Deve avançar para etapa 2", etapa2);

        System.out.println("✅ CF14 — Dados válidos avançam para etapa 2");
    }

    /** CF15 — Botão Voltar na etapa 2 retorna para etapa 1. */
    @Test
    public void cf15_botaoVoltarRetornaEtapa1() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1("Cliente Voltar", "voltar" + System.currentTimeMillis() + "@teste.com",
                gerarTelefone(), "senha123", gerarCPFValido());

        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1000);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(text(),'Confirmar Pedido')]")));

        WebElement btnVoltar = driver.findElement(
                By.xpath("//input[contains(@value,'Voltar')] | //button[contains(text(),'Voltar')]"));
        btnVoltar.click();
        aguardar(800);

        WebElement campoNome = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("cadastroForm:nome")));
        assertTrue("Deve voltar para etapa 1", campoNome.isDisplayed());

        System.out.println("✅ CF15 — Botão Voltar retorna para etapa 1");
    }

    /** CF16 — Cor hex é sincronizada na tela de novo pedido (cliente logado). */
    @Test
    public void cf16_corHexSincronizadaNaEtapa2() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);
        wait.until(ExpectedConditions.urlContains("/cliente/"));

        // Cliente logado → Comprar redireciona para novo-pedido.xhtml
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        aguardar(400);
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("novo-pedido"),
                ExpectedConditions.urlContains("cadastro")));
        aguardar(800);

        // Seleciona um veículo na tela de novo-pedido para exibir o painel com o color picker
        try {
            WebElement btnSelecionar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[contains(@value,'Selecionar')] | //button[contains(text(),'Selecionar')]")));
            btnSelecionar.click();
            aguardar(800);
        } catch (Exception ignored) {
            // Se já veio com veículo selecionado (via cadastro), o picker já está visível
        }

        // Picker pode estar em novo-pedido (corPickerNP) ou em cadastro (corPicker)
        WebElement colorPicker = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[@id='corPickerNP'] | //*[@id='corPicker']")));
        assertNotNull("Seletor de cor deve existir", colorPicker);

        String corEscolhida = "#D62828";
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; " +
                "arguments[0].dispatchEvent(new Event('input'));",
                colorPicker, corEscolhida);
        aguardar(400);

        WebElement hexInput = driver.findElement(
                By.xpath("//*[contains(@id,'corHexNP')] | //*[contains(@id,'corHex')]"));
        assertEquals("Campo hex deve refletir a cor escolhida",
                corEscolhida, hexInput.getAttribute("value"));

        System.out.println("✅ CF16 — Cor hex sincronizada: " + hexInput.getAttribute("value"));
    }

    /** CF17 — Fluxo completo: cadastro + pedido finalizado (etapa 3). */
    @Test
    public void cf17_fluxoCompletoPedidoFinalizado() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1("Cliente Fluxo Completo", "fluxo" + System.currentTimeMillis() + "@teste.com",
                gerarTelefone(), "senha123", gerarCPFValido());

        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1500);

        WebElement btnConfirmar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Confirmar')] | //button[contains(text(),'Confirmar')]")));
        btnConfirmar.click();
        aguardar(2000);

        WebElement sucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]")));
        assertTrue("Deve exibir tela de sucesso (etapa 3)", sucesso.isDisplayed());

        System.out.println("✅ CF17 — Fluxo completo finalizado com sucesso (etapa 3)");
    }

    /** CF18 — Pedido recém-criado aparece em meus-pedidos. */
    @Test
    public void cf18_pedidoNovoApareceEmMeusPedidos() {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1("Cliente Pedido Novo", "pedido" + System.currentTimeMillis() + "@teste.com",
                gerarTelefone(), "senha123", gerarCPFValido());

        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1500);

        driver.findElement(By.xpath(
                "//input[contains(@value,'Confirmar')] | //button[contains(text(),'Confirmar')]")).click();
        aguardar(2000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]")));

        // Navega para meus-pedidos via botão Acompanhar
        WebElement btnAcompanhar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Acompanhar')] | //button[contains(text(),'Acompanhar')]")));
        btnAcompanhar.click();
        wait.until(ExpectedConditions.urlContains("meus-pedidos"));
        aguardar(1000);

        WebElement conteudo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".main-content")));
        assertFalse("Meus pedidos não deve estar vazio após pedido",
                conteudo.getText().trim().isEmpty());

        System.out.println("✅ CF18 — Pedido novo aparece em meus-pedidos");
    }

    // =========================================================
    // GRUPO 5 — LOGIN DO CLIENTE
    // =========================================================

    /** CF19 — Login do cliente redireciona para meus-pedidos. */
    @Test
    public void cf19_loginClienteRedirecionaMeusPedidos() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);

        wait.until(ExpectedConditions.urlContains("/cliente/"));
        assertTrue("Cliente deve ir para área /cliente/",
                driver.getCurrentUrl().contains("/cliente/"));

        System.out.println("✅ CF19 — Login do cliente redireciona corretamente: " + driver.getCurrentUrl());
    }

    /** CF20 — Confirmar pedido sem veículo selecionado não finaliza. */
    @Test
    public void cf20_confirmarSemVeiculoNaoFinaliza() {
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);
        wait.until(ExpectedConditions.urlContains("/cliente/"));

        driver.get(CADASTRO_URL);
        aguardar(800);

        // Cliente logado vai para etapa 2, mas sem veículo selecionado
        WebElement btnConfirmar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Confirmar')]")));
        btnConfirmar.click();
        aguardar(2000);

        List<WebElement> etapa3 = driver.findElements(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]"));
        boolean chegouEtapa3 = etapa3.stream().anyMatch(WebElement::isDisplayed);
        assertFalse("Sem veículo não deve finalizar pedido", chegouEtapa3);

        System.out.println("✅ CF20 — Confirmação sem veículo não finaliza pedido");
    }

    // =========================================================
    // GRUPO 6 — SEGURANÇA DE SESSÃO
    // =========================================================

    /** CF21 — Admin, Gerente, Vendedor e Fábrica não acessam meus-pedidos. */
    @Test
    public void cf21_naoClientesNaoAcessamMeusPedidos() {
        String[][] perfis = {
            {EMAIL_ADMIN,    SENHA_ADMIN,    "Admin"},
            {EMAIL_GERENTE,  SENHA_GERENTE,  "Gerente"},
            {EMAIL_VENDEDOR, SENHA_VENDEDOR, "Vendedor"},
            {EMAIL_FABRICA,  SENHA_FABRICA,  "Fabrica"}
        };

        for (String[] p : perfis) {
            aguardar(1000);
            fazerLogin(p[0], p[1]);
            driver.get(PEDIDOS_URL);
            aguardar(1000);

            assertFalse("Perfil '" + p[2] + "' não deve acessar meus-pedidos",
                    driver.getCurrentUrl().contains("meus-pedidos"));
            System.out.println("   ✔ " + p[2] + " bloqueado → " + driver.getCurrentUrl());

            driver.get(BASE_URL + "/logout");
            wait.until(ExpectedConditions.urlContains("login"));
            aguardar(1000);
        }

        System.out.println("✅ CF21 — Todos os perfis não-cliente bloqueados de meus-pedidos");
    }

    /** CF22 — Sessão isolada: cliente B não acessa dados do cliente A. */
    @Test
    public void cf22_sessaoIsoladaEntreClientes() {
        // Login do cliente A e captura do nome exibido
        fazerLogin(EMAIL_CLIENTE, SENHA_CLIENTE);
        wait.until(ExpectedConditions.urlContains("/cliente/"));
        String nomeClienteA = "João Cliente"; // nome do cliente padrão do DataInicializador

        driver.get(BASE_URL + "/logout");
        wait.until(ExpectedConditions.urlContains("login"));

        // Registra cliente B
        realizarCadastroCompleto("Cliente B Isolamento", gerarTelefone(), gerarCPFValido());

        // Cliente B navega para meus-pedidos e não deve ver dados do cliente A
        driver.get(PEDIDOS_URL);
        wait.until(ExpectedConditions.urlContains("meus-pedidos"));
        aguardar(800);

        WebElement conteudo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".main-content")));
        assertFalse("Cliente B não deve ver nome do cliente A na sua página",
                conteudo.getText().contains(nomeClienteA));

        assertTrue("Cliente B deve estar em meus-pedidos",
                driver.getCurrentUrl().contains("meus-pedidos"));

        System.out.println("✅ CF22 — Sessão isolada: cliente B não vê dados do cliente A");
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void fazerLogin(String email, String senha) {
        driver.get(LOGIN_URL);
        // Reload para garantir ViewState JSF fresco
        driver.navigate().refresh();
        WebElement campoEmail = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("loginForm:email")));
        WebElement campoSenha = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("loginForm:senha")));
        aguardar(300);
        campoEmail.click(); campoEmail.clear(); campoEmail.sendKeys(email);
        aguardar(300);
        campoSenha.click(); campoSenha.clear(); campoSenha.sendKeys(senha);
        aguardar(300);
        driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(LOGIN_URL)));
    }

    /** Preenche todos os campos da etapa 1 do cadastro. */
    private void preencherEtapa1(String nome, String email, String telefone,
                                  String senha, String cpf) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement inputNome = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("cadastroForm:nome")));
        inputNome.click();
        inputNome.sendKeys(nome);
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", inputNome);

        WebElement inputEmail = driver.findElement(By.id("cadastroForm:emailCad"));
        inputEmail.sendKeys(email);
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", inputEmail);

        // Telefone: envia só dígitos, a máscara formata
        WebElement inputTel = driver.findElement(By.id("cadastroForm:telefone"));
        inputTel.click();
        inputTel.sendKeys(telefone.replaceAll("\\D", ""));
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", inputTel);

        // CPF: envia só dígitos, a máscara formata
        WebElement inputCpf = driver.findElement(By.id("cadastroForm:cpf"));
        inputCpf.click();
        inputCpf.sendKeys(cpf.replaceAll("\\D", ""));
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", inputCpf);

        WebElement inputSenha = driver.findElement(By.id("cadastroForm:senha"));
        inputSenha.sendKeys(senha);
        js.executeScript("arguments[0].dispatchEvent(new Event('input',{bubbles:true}))", inputSenha);

        new Select(driver.findElement(By.cssSelector("#cadastroForm select"))).selectByIndex(1);
        aguardar(500);

        // Dispara vrumVerificarFormulario explicitamente para garantir habilitação do botão
        js.executeScript("if(typeof vrumVerificarFormulario === 'function') vrumVerificarFormulario();");
        aguardar(300);
    }

    /** Fluxo completo: home → Comprar → etapa 1 → etapa 2 → etapa 3. */
    private void realizarCadastroCompleto(String nome, String telefone, String cpf) {
        driver.get(HOME_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vehicle-card")));
        aguardar(400);
        driver.findElement(By.xpath("//input[@value='Comprar']")).click();
        wait.until(ExpectedConditions.urlContains("cadastro"));
        aguardar(400);

        preencherEtapa1(nome, nome.toLowerCase().replaceAll("\\s+", "") +
                        System.currentTimeMillis() + "@teste.com",
                        telefone, "senha123", cpf);

        driver.findElement(By.xpath("//input[contains(@value,'Continuar')]")).click();
        aguardar(1500);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@value,'Confirmar')] | //button[contains(text(),'Confirmar')]"))).click();
        aguardar(2000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'PEDIDO REALIZADO')]")));
    }

    /** Gera CPF matematicamente válido (dígitos verificadores corretos). */
    private String gerarCPFValido() {
        java.util.Random rand = new java.util.Random(System.nanoTime());
        int[] d = new int[11];
        do {
            for (int i = 0; i < 9; i++) d[i] = rand.nextInt(10);
        } while (todosIguais(d));

        int soma = 0;
        for (int i = 0; i < 9; i++) soma += d[i] * (10 - i);
        int r = soma % 11;
        d[9] = r < 2 ? 0 : 11 - r;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += d[i] * (11 - i);
        r = soma % 11;
        d[10] = r < 2 ? 0 : 11 - r;

        return String.format("%d%d%d%d%d%d%d%d%d%d%d",
                d[0],d[1],d[2],d[3],d[4],d[5],d[6],d[7],d[8],d[9],d[10]);
    }

    private boolean todosIguais(int[] d) {
        for (int i = 1; i < 9; i++) if (d[i] != d[0]) return false;
        return true;
    }

    /** Gera telefone único no formato que a máscara espera (só dígitos). */
    private String gerarTelefone() {
        long n = System.nanoTime() % 100000000L;
        return String.format("81 9%04d%04d", n / 10000, n % 10000);
    }

    private void aguardar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
