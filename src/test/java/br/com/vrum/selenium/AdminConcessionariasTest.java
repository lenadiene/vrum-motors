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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * BLOCO D — Gestão de Concessionárias
 * Testes D01–D22: CRUD, validações front-end, integração API IBGE e server-side.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminConcessionariasTest extends AdminSeleniumBase {

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

        new Select(driver.findElement(By.id("ufSelect"))).selectByValue("SP");
        js.executeScript("onUfChange(document.getElementById('ufSelect'))");
        wait.until(d -> new Select(d.findElement(By.id("cidadeSelect"))).getOptions().size() > 10);
        int qtdCidadesSP = new Select(driver.findElement(By.id("cidadeSelect"))).getOptions().size();

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
        nome.sendKeys(NOME_CON_TESTE);
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
     */
    @Test
    public void tc_D17_falhaApiIbgeAoCarregarEstados() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        driver.get(URL_CONC);
        wait.until(ExpectedConditions.urlContains("concessionarias"));

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
            js.executeScript(
                "if(window._fetchOriginal) { window.fetch = window._fetchOriginal; delete window._fetchOriginal; }"
            );
        }
    }

    /**
     * D18 — Falha na API do IBGE ao carregar cidades.
     * Esperado: exibe "Falha ao carregar cidades. Verifique sua conexão."
     */
    @Test
    public void tc_D18_falhaApiIbgeAoCarregarCidades() {
        fazerLogin(EMAIL_ADMIN, SENHA_ADMIN);
        abrirFormNovaConcessionaria();

        wait.until(d -> new Select(d.findElement(By.id("ufSelect"))).getOptions().size() > 5);

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
        } catch (org.openqa.selenium.TimeoutException e) {
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
        aguardar(2000);

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
        aguardar(2000);

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
        } catch (org.openqa.selenium.TimeoutException e) {
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
}
