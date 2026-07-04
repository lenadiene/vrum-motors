package br.com.vrum.selenium;

import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * BLOCO B — Dashboard
 * Testes B01–B05: carregamento do dashboard e navegação via links.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminDashboardTest extends AdminSeleniumBase {

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
}
