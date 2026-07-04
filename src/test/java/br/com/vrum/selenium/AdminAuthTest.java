package br.com.vrum.selenium;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * BLOCO A — Autenticação e Acesso
 * Testes A01–A09: login, logout, controle de acesso por perfil.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminAuthTest extends AdminSeleniumBase {

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
}
