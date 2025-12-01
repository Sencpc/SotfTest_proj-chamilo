package com.selenium;

import static org.testng.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Simple TestNG suite that validates several critical blocks on https://chamilo.org/en/ using Brave.
 */
public class HomePageTest {

    private static final String CHAMILO_URL = "https://chamilo.org/en/";
    private static final String DEFAULT_BRAVE_PATH = "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setBinary(resolveBraveBinary());
        options.addArguments(
                "--disable-notifications",
                "--start-maximized",
                "--disable-infobars",
                "--disable-extensions");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(description = "Memastikan blok 'Need help?' menampilkan tautan bantuan utama Chamilo")
    public void shouldExposeNeedHelpResources() {
        driver.get(CHAMILO_URL);
        acceptCookiesIfPresent();

        WebElement needHelpHeading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'need help')]")));
        assertTrue(needHelpHeading.isDisplayed(), "Judul Need help? harus terlihat");

        assertTrue(isLinkVisible("FAQ"), "Tautan FAQ harus tersedia");
        assertTrue(isLinkVisible("Forum"), "Tautan Forum harus tersedia");
        assertTrue(isLinkVisible("Services"), "Tautan Services providers harus tersedia");
    }

    @Test(description = "Memastikan blok ajakan kontribusi tampil dan dapat diklik")
    public void shouldPromoteContributionCTA() {
        driver.get(CHAMILO_URL);
        acceptCookiesIfPresent();

        WebElement contributionHeading = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'do you like chamilo')]")));
        assertTrue(contributionHeading.isDisplayed(), "Judul Do you like Chamilo? harus terlihat");

        WebElement contributeButton = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'contribute')]")));
        scrollIntoView(contributeButton);
        wait.until(ExpectedConditions.elementToBeClickable(contributeButton));
        assertTrue(contributeButton.isDisplayed(), "Tombol CONTRIBUTE harus terlihat");
    }

    private void acceptCookiesIfPresent() {
        List<By> cookieLocators = List.of(
                By.cssSelector("a.cc-dismiss"),
                By.cssSelector("button#wt-cli-accept-btn"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree')]")
        );

        for (By locator : cookieLocators) {
            try {
                WebElement button = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                button.click();
                return;
            } catch (Exception ignored) {
                // different region may use another component; keep trying others
            }
        }
    }

    private boolean isLinkVisible(String partialText) {
        try {
            WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(partialText)));
            return link.isDisplayed();
        } catch (Exception ex) {
            return false;
        }
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (Exception ignored) {
            // jika scroll gagal, biarkan Selenium mencoba klik langsung
        }
    }

    private String resolveBraveBinary() {
        String customBinary = System.getProperty("braveBinary");
        if (customBinary != null && !customBinary.isBlank()) {
            return customBinary;
        }

        if (Files.exists(Path.of(DEFAULT_BRAVE_PATH))) {
            return DEFAULT_BRAVE_PATH;
        }

        throw new IllegalStateException("Lokasi Brave tidak ditemukan. Set properti sistem 'braveBinary'.");
    }
}
