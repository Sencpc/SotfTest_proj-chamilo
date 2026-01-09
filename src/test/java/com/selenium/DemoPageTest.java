package com.selenium;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

public class DemoPageTest {
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

    @Test(priority = 1, description = "Masuk ke halaman Demo melalui Navbar")
    public void shouldNavigateToDemoPage() throws Exception {
        MainApp.executeTest("Navigate to Demo Page", "Masuk ke halaman Demo melalui Navbar", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            // 2. Click Demo
            WebElement demoMenu = wait
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a")));
            demoMenu.click();

            // Wait for URL to update
            wait.until(ExpectedConditions.urlContains("demo"));

            logSuccess("Clicked Demo menu and navigated to Demo page");
            Thread.sleep(3000);

            MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_Navigated.png");
        }, "DemoPageTest");
    }

    @Test(priority = 2, description = "Mengklik tombol Go to Free Campus dan kembali")
    public void shouldClickFreeCampusButton() throws Exception {
        MainApp.executeTest("Click Free Campus Button", "Mengklik tombol Go to Free Campus dan kembali", () -> {
            // Pastikan kita ada di halaman Demo
            if (!driver.getCurrentUrl().contains("demo")) {
                driver.get(CHAMILO_URL);
                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a"))).click();
            }

            String originalWindow = driver.getWindowHandle();

            // Cari tombol "Go to Free Campus"
            // Menggunakan xpath yang mencari elemen 'a' yang mengandung text "Go to Free
            // Campus"
            WebElement freeCampusButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(
                            "//a[contains(@href, 'campus.chamilo.org') and .//span[contains(text(), 'Go to Free Campus')]]")));

            // Scroll ke view agar bisa diklik
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                    freeCampusButton);
            wait.until(ExpectedConditions.elementToBeClickable(freeCampusButton));

            freeCampusButton.click();
            logSuccess("Clicked 'Go to Free Campus' button");

            // Handle new tab
            java.util.Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.size() > 1) {
                for (String handle : windowHandles) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }

                // Tunggu sebentar di halaman baru
                Thread.sleep(3000);
                logSuccess("Switched to new tab: " + driver.getCurrentUrl());

                driver.close(); // Tutup tab baru
                driver.switchTo().window(originalWindow); // Kembali ke tab awal
                logSuccess("Closed new tab and returned to Demo page");
            } else {
                // Fallback jika tidak membuka tab baru (jarang terjadi jika target="_blank")
                Thread.sleep(3000);
                driver.navigate().back();
                logSuccess("Returned to Demo page via Back");
            }

            // Verifikasi kembali ke halaman demo
            wait.until(ExpectedConditions.urlContains("demo"));
            MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_FreeCampusReturn.png");
        }, "DemoPageTest");
    }

    @Test(priority = 3, description = "Memeriksa tata bahasa pada semua elemen heading di halaman Demo")
    public void shouldCheckHeadingsGrammar() throws Exception {
        MainApp.executeTest("Check Headings Grammar", "Memeriksa tata bahasa pada semua elemen heading di halaman Demo",
                () -> {
                    // Pastikan kita ada di halaman Demo
                    if (!driver.getCurrentUrl().contains("demo")) {
                        driver.get(CHAMILO_URL);
                        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a")))
                                .click();
                    }

                    // List kata-kata informal yang dilarang
                    java.util.Map<String, String> informalWords = new java.util.HashMap<>();
                    informalWords.put("wanna", "want to");
                    informalWords.put("gonna", "going to");
                    informalWords.put("gotta", "have got to");

                    // Cari semua elemen heading h1-h6
                    List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
                    boolean foundGrammarIssue = false;

                    logSuccess("Scanning " + headings.size() + " headings for grammar police check...");

                    for (WebElement heading : headings) {
                        // Cek visibilitas agar yang tersembunyi tidak di-scan (opsional, tapi lebih
                        // akurat visual)
                        if (!heading.isDisplayed())
                            continue;

                        String text = heading.getText().toLowerCase();

                        for (java.util.Map.Entry<String, String> entry : informalWords.entrySet()) {
                            String badWord = entry.getKey();
                            String correction = entry.getValue();

                            // Cek word boundary agar tidak mendeteksi 'wanna' di dalam 'wannabe' jika tidak
                            // diinginkan,
                            // tapi untuk slang biasanya cukup contains
                            if (text.contains(badWord)) {
                                // Scroll to element to show it
                                ((JavascriptExecutor) driver)
                                        .executeScript("arguments[0].scrollIntoView({block: 'center'});", heading);

                                String alertMessage = String.format(
                                        "GRAMMAR POLICE ALERT! Found slang '%s' in heading \"%s\". The Queen demands '%s'!",
                                        badWord.toUpperCase(), heading.getText(), correction.toUpperCase());
                                // Use System.err or MainApp log if available static, or local
                                // logSuccess/logError
                                System.err.println(alertMessage);
                                foundGrammarIssue = true;
                            }
                        }
                    }

                    if (!foundGrammarIssue) {
                        logSuccess("All headings passed the grammar check. The Queen is pleased.");
                    }

                    MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_GrammarCheck.png");
                }, "DemoPageTest");
    }

    private void acceptCookiesIfPresent() {
        List<By> cookieLocators = Arrays.asList(
                By.cssSelector("a.cc-dismiss"),
                By.cssSelector("button#wt-cli-accept-btn"),
                By.xpath(
                        "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree')]"),
                By.xpath(
                        "//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree')]"));

        for (By locator : cookieLocators) {
            try {
                WebElement button = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                button.click();
                logSuccess("Cookies accepted");
                return;
            } catch (Exception ignored) {
                // different region may use another component; keep trying others
            }
        }
    }

    private void logSuccess(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] SUCCESS: %s%n", timestamp, message);

        try {
            Files.createDirectories(Path.of("cache"));
            try (FileWriter fw = new FileWriter("cache/DemoPageTest_log.txt", true)) {
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
