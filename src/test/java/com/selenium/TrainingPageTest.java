package com.selenium;

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

public class TrainingPageTest extends ChamiloBaseTest {

    @Test(description = "Membuka homepage Chamilo dan memastikan tombol TRAINING mengarah ke halaman training")
    public void shouldNavigateToTrainingPageFromChamiloUniverseSection() {
        openHome();

        // Scroll agar section 'Chamilo universe' terlihat dan link TRAINING bisa diklik
        // stabil.
        WebElement chamiloUniverseHeading = waitVisible(By.xpath(
                "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'chamilo universe')]"));
        scrollIntoView(chamiloUniverseHeading);

        // Klik tombol paling kiri di section 'Chamilo universe' yaitu TRAINING
        WebElement trainingLink = waitClickable(By.xpath(
                "(//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'chamilo universe')]/following::a[normalize-space()='TRAINING'])[1]"));
        scrollIntoView(trainingLink);
        trainingLink.click();

        wait.until(ExpectedConditions.urlContains("/training"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.toLowerCase().contains("/training"),
                "URL harus mengarah ke halaman training, aktual: " + currentUrl);

        // Verifikasi konten utama minimal (judul/heading mengandung kata training)
        WebElement anyTrainingHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "(//h1|//h2)[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'training')]")));
        assertTrue(anyTrainingHeading.isDisplayed(), "Heading training harus terlihat");

        // Kembali ke halaman home dengan menekan icon/logo di kiri atas navigasi
        WebElement logoTopLeft = waitClickable(By.id("logo"));
        logoTopLeft.click();

    }
}
