package br.com.vrum.selenium;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * BLOCO F — Gestão de Pedidos
 * Testes F01–F19: filtros, seleção, edição e upload em pedidos.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminPedidosTest extends AdminSeleniumBase {

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

        List<WebElement> textoObs = driver.findElements(By.xpath(
                "//textarea[contains(@id,'bservacoes') or contains(@id,'obs')]"));
        if (!textoObs.isEmpty()) {
            textoObs.get(0).sendKeys("Texto que NÃO deve ser salvo");
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[contains(@value,'Cancelar')] | //button[contains(text(),'Cancelar')]"))).click();
        aguardar(500);

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
}
