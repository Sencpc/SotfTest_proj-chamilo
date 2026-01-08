package com.selenium;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DownloadPageTest {

    private static final String HOME_URL = "https://chamilo.org/en/";
    private static final String DEFAULT_BRAVE_PATH = "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe";

    private WebDriver driver;
    private WebDriverWait wait;
    private boolean downloadPageLoaded = false;

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
        
        driver.get(HOME_URL);
        try {
            Thread.sleep(500);
            acceptCookiesIfPresent();
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    private void navigateToDownload() throws InterruptedException {
        if (!downloadPageLoaded) {
            WebElement downloadLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//a[contains(@href, '/download')] | //nav//a[contains(text(), 'Download')]")));
            scrollIntoView(downloadLink);
            Thread.sleep(500);
            downloadLink.click();
            Thread.sleep(1500);
            downloadPageLoaded = true;
        }
    }

    @Test(priority = 1, description = "Dokumentasi resmi memiliki link dengan href yang benar")
    public void shouldExposeDocumentationLinks() throws InterruptedException {
        MainApp.executeTest("Documentation Links", "Verify official documentation links have correct URLs", () -> {
            navigateToDownload();

            WebElement officialDoc = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText("Official documentation")));
            scrollIntoView(officialDoc);
            Thread.sleep(800);
            WebElement installGuide = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText("Installation guide")));
            scrollIntoView(installGuide);
            Thread.sleep(800);
            WebElement changelog = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText("Chamilo changelog")));
            scrollIntoView(changelog);
            Thread.sleep(800);

            assertEquals(officialDoc.getAttribute("href"), "https://docs.chamilo.org/v/1.11.x/");
            assertEquals(installGuide.getAttribute("href"), "https://11.chamilo.org/documentation/installation_guide.html");
            assertEquals(changelog.getAttribute("href"), "https://11.chamilo.org/documentation/changelog.html");
        }, 1200);
    }

    @Test(priority = 2, description = "Blok informasi versi menampilkan detail rilis dan lisensi")
    public void shouldDisplayVersionInfo() throws InterruptedException {
        MainApp.executeTest("Version Info Display", "Verify version block shows release details and license", () -> {
            navigateToDownload();

            WebElement versionHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), 'Chamilo LMS 1.11.32')]")));
            scrollIntoView(versionHeading);
            Thread.sleep(800);
            assertTrue(versionHeading.isDisplayed(), "Versi 1.11.32 harus ditampilkan");

            WebElement license = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), 'GNU/GPLv3')]")));
            scrollIntoView(license);
            Thread.sleep(800);
            assertTrue(license.isDisplayed(), "Lisensi GNU/GPLv3+ harus ditampilkan");

            WebElement phpSupport = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), 'PHP 7.4') and contains(text(), '8.3')]")));
            assertTrue(phpSupport.isDisplayed(), "Info kompatibilitas PHP harus terlihat");

            WebElement releaseDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), '2025-06-27')]")));
            assertTrue(releaseDate.isDisplayed(), "Tanggal rilis harus tampil");
        }, 1200);
    }

    @Test(priority = 3, description = "Opsi download ZIP dan TAR.GZ terlihat dan mengarah ke GitHub release")
    public void shouldExposeDownloadOptions() throws InterruptedException {
        MainApp.executeTest("Download Options", "Verify ZIP and TAR.GZ download links point to GitHub releases", () -> {
            navigateToDownload();

            WebElement zipLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//a[contains(@href, 'chamilo-1.11.32.zip')]")));
            scrollIntoView(zipLink);
            Thread.sleep(800);
            WebElement tarLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//a[contains(@href, 'chamilo-1.11.32.tar.gz')]")));
            scrollIntoView(tarLink);
            Thread.sleep(800);

            assertEquals(zipLink.getAttribute("href"),
                "https://github.com/chamilo/chamilo-lms/releases/download/v1.11.32/chamilo-1.11.32.zip");
            assertEquals(tarLink.getAttribute("href"),
                "https://github.com/chamilo/chamilo-lms/releases/download/v1.11.32/chamilo-1.11.32.tar.gz");

            WebElement compatibilityText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), 'PHP 7.4') and contains(text(), '8.3')]")));
            assertTrue(compatibilityText.isDisplayed(), "Teks kompatibilitas PHP harus berada dekat opsi unduhan");
        }, 1200);
    }

    @Test(priority = 4, description = "Ikon download ditampilkan dengan ukuran masuk akal")
    public void shouldRenderDownloadIcons() throws InterruptedException {
        MainApp.executeTest("Download Icons", "Verify download page shows icons with reasonable dimensions", () -> {
            navigateToDownload();

            List<WebElement> icons = driver.findElements(By.xpath(
                    "//img[contains(@src,'download') or contains(@src,'zip') or contains(@src,'tar') or contains(@src,'icon')]"));
            if (icons.isEmpty()) {
                icons = driver.findElements(By.cssSelector(".avia-image-container img, article img, img"));
            }
            assertFalse(icons.isEmpty(), "Setidaknya satu ikon download harus ditemukan");

            long displayed = icons.stream().filter(WebElement::isDisplayed)
                    .filter(img -> img.getSize().getHeight() > 10 && img.getSize().getWidth() > 10)
                    .count();
            assertTrue(displayed >= 2, "Minimal dua ikon download harus tampil dengan ukuran wajar");
        }, 1200);
    }

    @Test(priority = 5, description = "Banner konferensi muncul di halaman download")
    public void shouldDisplayConferenceBanner() throws InterruptedException {
        MainApp.executeTest("Conference Banner", "Verify conference banner displays on download page", () -> {
            navigateToDownload();

            WebElement bannerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//a[contains(@href, 'conference.chamilo.org')]")));
            scrollIntoView(bannerLink);
            Thread.sleep(800);
            assertTrue(bannerLink.isDisplayed(), "Banner konferensi harus terlihat");
            assertTrue(bannerLink.getAttribute("href").contains("conference.chamilo.org"));
        }, 1200);
    }

    @Test(priority = 6, description = "Konten utama memuat link eksternal valid")
    public void shouldValidateExternalLinks() throws InterruptedException {
        MainApp.executeTest("External Links Validation", "Verify all external links have valid absolute URLs", () -> {
            navigateToDownload();

            List<WebElement> links = driver.findElements(By.xpath(
                    "//a[contains(@href, 'github.com/chamilo/chamilo-lms/releases')]"));
            links.addAll(driver.findElements(By.xpath("//a[contains(@href, 'docs.chamilo.org')]")));

            assertFalse(links.isEmpty(), "Harus ada link eksternal pada halaman download");
            for (WebElement link : links) {
                scrollIntoView(link);
                Thread.sleep(800);
                String href = link.getAttribute("href");
                assertNotNull(href, "Href tidak boleh null");
                assertFalse(href.isBlank(), "Href tidak boleh kosong");
                assertTrue(href.startsWith("http"), "Href harus absolute url");
            }
        }, 1200);
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (Exception ignored) {
        }
    }

    private void acceptCookiesIfPresent() {
        List<By> cookieLocators = List.of(
                By.cssSelector("a.cc-dismiss"),
                By.cssSelector("button#wt-cli-accept-btn"),
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')]"),
                By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree')]")
        );

        for (By locator : cookieLocators) {
            try {
                WebElement button = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                button.click();
                return;
            } catch (Exception ignored) {
            }
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

        String userHome = System.getProperty("user.home");
        Path altPath = Path.of(userHome, "AppData/Local/BraveSoftware/Brave-Browser/Application/brave.exe");
        if (Files.exists(altPath)) {
            return altPath.toString();
        }

        throw new IllegalStateException("Lokasi Brave tidak ditemukan. Set properti sistem 'braveBinary'.");
    }
}
