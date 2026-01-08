package com.selenium;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * TestNG suite that validates the Providers page on
 * https://chamilo.org/en/providers/
 */
public class ProvidersPageTest {

    private static final String PROVIDERS_URL = "https://chamilo.org/en/providers/";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Use Brave browser
        options.setBinary(resolveBraveBinary());
        options.addArguments(
                "--disable-notifications",
                "--start-maximized",
                "--disable-infobars",
                "--disable-extensions",
                "--disable-gpu",
                "--no-sandbox");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Test 1: Open Providers Page and verify it loads correctly
    @Test(priority = 1, description = "Open Providers page and verify the page title")
    public void shouldOpenProvidersPage() {
        driver.get(PROVIDERS_URL);
        acceptCookiesIfPresent();

        // Verify page loaded by checking the title contains relevant text
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.toLowerCase().contains("provider") || pageTitle.toLowerCase().contains("chamilo"),
                "Page title should contain 'provider' or 'chamilo'");
        logSuccess("Providers page opened successfully. Title: " + pageTitle);

        // Verify URL is correct
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("providers"), "URL should contain 'providers'");
        logSuccess("URL verified: " + currentUrl);
    }

    // Helper method to accept cookies if the cookie banner is present
    private void acceptCookiesIfPresent() {
        try {
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".cmplz-btn.cmplz-accept")));
            acceptButton.click();
            logSuccess("Cookie consent accepted");
        } catch (Exception e) {
            // Cookie banner not present or already accepted
        }
    }

    private void logSuccess(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] SUCCESS: %s%n", timestamp, message);

        try {
            Files.createDirectories(Path.of("cache"));
            try (FileWriter fw = new FileWriter("cache/ProvidersPageTest_log.txt", true)) {
                fw.write(logMessage);
            }
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    /**
     * Resolve the Brave browser binary path by checking multiple common installation locations
     */
    private String resolveBraveBinary() {
        // 1. Check environment variable first (highest priority)
        String envPath = System.getenv("BRAVE_PATH");
        if (envPath != null && Files.exists(Path.of(envPath))) {
            System.out.println("✓ Using Brave from BRAVE_PATH env variable: " + envPath);
            return envPath;
        }

        // 2. Check system property
        String sysProp = System.getProperty("brave.binary");
        if (sysProp != null && Files.exists(Path.of(sysProp))) {
            System.out.println("✓ Using Brave from system property: " + sysProp);
            return sysProp;
        }

        // 3. Search common installation paths
        String userHome = System.getProperty("user.home");
        List<String> possiblePaths = List.of(
                // Windows - User installation (most common)
                userHome + "/AppData/Local/BraveSoftware/Brave-Browser/Application/brave.exe",
                // Windows - Program Files
                "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe",
                "C:/Program Files (x86)/BraveSoftware/Brave-Browser/Application/brave.exe",
                // macOS
                "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser",
                userHome + "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser",
                // Linux - Common locations
                "/usr/bin/brave",
                "/usr/bin/brave-browser",
                "/snap/bin/brave",
                "/opt/brave.com/brave/brave",
                userHome + "/.local/bin/brave"
        );

        for (String path : possiblePaths) {
            if (Files.exists(Path.of(path))) {
                System.out.println("✓ Found Brave at: " + path);
                return path;
            }
        }

        // 4. If not found, throw helpful error
        throw new IllegalStateException(
                "Brave browser not found. Please either:\n" +
                "  1. Set BRAVE_PATH environment variable to your brave.exe location\n" +
                "  2. Set -Dbrave.binary=<path> system property\n" +
                "  3. Install Brave in a standard location\n" +
                "Searched paths: " + possiblePaths);
    }
}
