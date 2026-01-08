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
import java.util.Set;

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

public class FooterTest {
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

    @Test(description = "Mengklik semua link di footer dan kembali ke home")
    public void shouldClickAllFooterLinks() throws Exception {
        MainApp.executeTest("Click All Footer Links", "Mengklik semua link di footer dan kembali ke home", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            // Cari footer untuk menghitung jumlah link awal
            WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Footer")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", footer);

            List<WebElement> footerLinks = footer.findElements(By.tagName("a"));
            int linkCount = footerLinks.size();
            logSuccess("Found " + linkCount + " links in footer.");

            String originalWindow = driver.getWindowHandle();

            for (int i = 0; i < linkCount; i++) {
                // Kita harus mencari ulang elemen setiap loop karena halaman mungkin
                // direfresh/navigasi balik
                // Pastikan URL benar
                if (!driver.getCurrentUrl().contains("chamilo.org")) {
                    driver.get(CHAMILO_URL);
                }

                // Scroll ke footer lagi
                footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Footer")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", footer);

                // Ambil list lagi
                footerLinks = footer.findElements(By.tagName("a"));

                // Ambil elemen ke-i
                WebElement link = footerLinks.get(i);
                String url = link.getAttribute("href");
                String title = link.getAttribute("title");
                String linkText = !link.getText().isEmpty() ? link.getText()
                        : (title != null ? title : "Icon/Image Link");

                // Klik link
                // Gunakan JS click untuk menghindari tertutup elemen lain (misal chat widget)
                wait.until(ExpectedConditions.elementToBeClickable(link));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);

                logSuccess("Clicked footer link " + (i + 1) + "/" + linkCount + ": " + linkText + " (" + url + ")");

                // Handle tab baru vs tab yang sama
                Set<String> windowHandles = driver.getWindowHandles();
                if (windowHandles.size() > 1) {
                    // Link membuka tab baru (biasanya social media)
                    for (String handle : windowHandles) {
                        if (!handle.equals(originalWindow)) {
                            driver.switchTo().window(handle);
                            break;
                        }
                    }

                    Thread.sleep(2000); // Tunggu sebentar untuk melihat hasil
                    logSuccess("Opened in new tab: " + driver.getCurrentUrl());

                    driver.close(); // Tutup tab baru
                    driver.switchTo().window(originalWindow); // Kembali ke tab utama
                } else {
                    // Link membuka di tab yang sama
                    Thread.sleep(2000);
                    logSuccess("Opened in same tab: " + driver.getCurrentUrl());

                    // Kembali ke home
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlContains("chamilo.org"));
                }

                // Jeda sedikit sebelum iterasi berikutnya
                Thread.sleep(1000);
            }
            MainApp.captureFullPageScreenshot(driver, "cache/Footer_Links.png");
        });
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
            try (FileWriter fw = new FileWriter("cache/FooterTest_log.txt", true)) {
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
