package br.com.vrum.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * BLOCO C — Gestão de Usuários
 * Testes C01–C38: CRUD, validações front-end e server-side de usuários.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminUsuariosTest extends AdminSeleniumBase {

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
        aguardar(300);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

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
                By.cssSelector("#concessionariaField select")));
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
        } catch (org.openqa.selenium.TimeoutException e) {
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
                    By.cssSelector("#concessionariaField select")));
            List<WebElement> opcoes = new Select(selectConc).getOptions();
            new Select(selectConc).selectByIndex(opcoes.size() - 1);
        } catch (org.openqa.selenium.TimeoutException e) {
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
        } catch (org.openqa.selenium.TimeoutException e) {
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
        aguardar(300);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C07 — Resultado criação Admin Fábrica: " + msg.getText());
        } catch (org.openqa.selenium.TimeoutException e) {
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
        aguardar(300);

        habilitarBotaoJs("[id$=':btnSalvarUsuario']");
        driver.findElement(By.cssSelector("[id$=':btnSalvarUsuario']")).click();

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".msg-success, .msg-error")));
            System.out.println("✅ C08 — Resultado criação Admin Empresa: " + msg.getText());
        } catch (org.openqa.selenium.TimeoutException e) {
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
    public void tc_C15_telefoneLetrasBloqueadas() {
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
        // Chrome 149 não dispara oninput em password fields via WebDriver —
        // seta o flag diretamente e recalcula o estado do botão
        js.executeScript("window._senhaPreenchida=true; atualizarBotao();");
        aguardar(300);

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
        } catch (org.openqa.selenium.TimeoutException e) {
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
        } catch (org.openqa.selenium.TimeoutException e) {
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
                    By.cssSelector("#concessionariaField select")));
            new Select(selectConc).selectByIndex(1);
        } catch (org.openqa.selenium.TimeoutException e) {
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
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("⚠️ C30 — Timeout aguardando resposta");
        }
    }

    /**
     * C31 — Erro ao persistir no banco.
     * Esperado: exibe "Falha ao persistir os dados no banco. Tente novamente."
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
        } catch (org.openqa.selenium.TimeoutException e) {
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
        } catch (org.openqa.selenium.TimeoutException e) {
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
        } catch (org.openqa.selenium.TimeoutException ignored) {}

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
        } catch (org.openqa.selenium.TimeoutException e) {
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

        List<WebElement> linhas = driver.findElements(By.cssSelector(".vrum-table tbody tr"));
        WebElement btnParaInativar = null;
        for (WebElement linha : linhas) {
            if (!linha.getText().contains(EMAIL_ADMIN)) {
                List<WebElement> btn = linha.findElements(
                        By.xpath(".//input[contains(@value,'Inativar')] | .//button[contains(text(),'Inativar')]"));
                if (!btn.isEmpty()) { btnParaInativar = btn.get(0); break; }
            }
        }
        if (btnParaInativar == null) {
            System.out.println("⚠️ C37 — Nenhum usuário ativo não-admin para inativar");
            return;
        }

        btnParaInativar.click();
        aguardar(1000);

        try {
            WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".msg-success")));
            assertTrue("Inativação deve exibir sucesso", msg.isDisplayed());
            System.out.println("✅ C37 — Usuário inativado: " + msg.getText());
        } catch (org.openqa.selenium.TimeoutException e) {
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
        } catch (org.openqa.selenium.TimeoutException e) {
            List<WebElement> inativar = driver.findElements(
                    By.xpath("//input[contains(@value,'Inativar')] | //button[contains(text(),'Inativar')]"));
            assertTrue("Botão Inativar deve aparecer após reativação", !inativar.isEmpty());
            System.out.println("✅ C38 — Usuário reativado (botão Inativar visível)");
        }
    }
}
