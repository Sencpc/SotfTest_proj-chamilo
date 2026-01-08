package com.selenium;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
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

public class NavbarTest {

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
        driver.get(CHAMILO_URL);
    }

    @Test
    public void testNavbarNavigation() throws InterruptedException {
        // 1. Click Chamilo
        WebElement chamiloMenu = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-409 a")));
        chamiloMenu.click();
        logSuccess("Clicked Chamilo menu");

        // Wait 3 seconds then go back home
        Thread.sleep(3000);
        driver.get(CHAMILO_URL);

        // 2. Click Demo
        WebElement demoMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a")));
        demoMenu.click();
        logSuccess("Clicked Demo menu");

        // Go back home
        driver.get(CHAMILO_URL);

        // 3. Click Forum
        WebElement forumMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2898 a")));
        forumMenu.click();
        logSuccess("Clicked Forum menu");

        // Go back home
        driver.get(CHAMILO_URL);

        // 4. Click Download
        WebElement downloadMenu = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2903 a")));
        downloadMenu.click();
        logSuccess("Clicked Download menu");

        // Go back home
        driver.get(CHAMILO_URL);

        // 5. Click Events
        WebElement eventsMenu = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2525 a")));
        eventsMenu.click();
        logSuccess("Clicked Events menu");

        // Go back home
        driver.get(CHAMILO_URL);

        // 6. Click Contact
        WebElement contactMenu = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-411 a")));
        contactMenu.click();
        logSuccess("Clicked Contact menu");

        // Go back home
        driver.get(CHAMILO_URL);

        // 7. Search for "Education"
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("search_button")));
        searchButton.click();

        WebElement searchInput = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#searchform input[name='s']")));
        searchInput.sendKeys("Education");
        searchInput.submit();
        logSuccess("Performed search for 'Education'");

        // Go back home
        driver.get(CHAMILO_URL);

        // 9. Click Logo
        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.id("logo")));
        logo.click();
        logSuccess("Clicked Logo");
    }

    @AfterClass
    public void tearDown() throws InterruptedException {
        if (driver != null) {
            Thread.sleep(5000);
            driver.quit();
        }
    }

    private void logSuccess(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] SUCCESS: %s%n", timestamp, message);

        try {
            Files.createDirectories(Path.of("cache"));
            try (FileWriter fw = new FileWriter("cache/NavbarTest_log.txt", true)) {
                fw.write(logMessage);
            }
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
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
