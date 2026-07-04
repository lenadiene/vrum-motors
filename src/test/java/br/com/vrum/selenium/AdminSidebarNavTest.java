package br.com.vrum.selenium;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * BLOCO G — Controles Transversais (Sidebar e Logout)
 * Testes G01–G07: navegação via sidebar, exibição do nome do admin e logout.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminSidebarNavTest extends AdminSeleniumBase {

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

        driver.get(DASHBOARD);
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue("Após logout sessão deve estar inválida", driver.getCurrentUrl().contains("login"));
        System.out.println("✅ G07 — Logout do admin: sessão invalidada e redirecionado ao login");
    }
}
