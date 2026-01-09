package com.selenium;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

public class ContributePageTest extends ChamiloBaseTest {

    @Test(description = "Scroll ke bagian 'Do you like Chamilo?', klik tombol CONTRIBUTE, lalu kembali ke home via logo")
    public void shouldNavigateToContributeAndBackHomeViaLogo() {
        openHome();

        // Bagian ini berada di bawah section Training (Chamilo universe)
        WebElement likeChamiloHeading = waitVisible(By.xpath(
                "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'do you like chamilo')]"));
        scrollIntoView(likeChamiloHeading);

        // Tombol CONTRIBUTE di DOM berupa <a> yang berisi <span
        // class='button_label'>CONTRIBUTE</span>
        WebElement contributeSpan = waitClickable(By.xpath(
                "(//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'do you like chamilo')]/following::a[contains(@href,'/contribute')])[1]//span[contains(@class,'button_label') and normalize-space()='CONTRIBUTE']"));
        scrollIntoView(contributeSpan);
        try {
            contributeSpan.click();
        } catch (Exception ignored) {
            // fallback kalau klik di span terhalang: klik elemen <a> parent
            WebElement contributeLink = contributeSpan.findElement(By.xpath("./ancestor::a[1]"));
            scrollIntoView(contributeLink);
            contributeLink.click();
        }

        wait.until(ExpectedConditions.urlContains("/contribute"));
        String contributeUrl = driver.getCurrentUrl();
        assertTrue(contributeUrl.toLowerCase().contains("/contribute"),
                "URL harus mengarah ke halaman contribute, aktual: " + contributeUrl);

        // Sebelum menekan logo, scroll perlahan ke bawah
        slowScrollDown(10, 250, 1000);

        // Kembali ke halaman home dengan menekan icon/logo di kiri atas navigasi
        WebElement logoTopLeft = waitClickable(By.id("logo"));
        logoTopLeft.click();

        String homeUrl = driver.getCurrentUrl();
        assertTrue(homeUrl.contains("/en"), "Harus kembali ke homepage EN, aktual: " + homeUrl);
        assertTrue(!homeUrl.toLowerCase().contains("/contribute"),
                "URL home tidak boleh masih di contribute, aktual: " + homeUrl);
    }

    private void slowScrollDown(int steps, int pixelsPerStep, long delayMs) {
        try {
            for (int i = 0; i < steps; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", pixelsPerStep);
                Thread.sleep(delayMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // ignore
        }
    }
}
