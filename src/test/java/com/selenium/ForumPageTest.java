package com.selenium;

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ForumPageTest {

    private static final String TEST_CLASS_NAME = "ForumPageTest";
    private static final String HOME_URL = "https://chamilo.org/en/";
    private static final String DEFAULT_BRAVE_PATH = "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe";

    private WebDriver driver;
    private WebDriverWait wait;
    private boolean forumPageLoaded = false;

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
        
        // Load home page once and accept cookies
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
        MainApp.writeTestLog(TEST_CLASS_NAME);
    }
    
    private void navigateToForum() throws InterruptedException {
        if (!forumPageLoaded) {
            WebElement forumLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//a[contains(@href, '/forum')] | //nav//a[contains(text(), 'Forum')]")));
            scrollIntoView(forumLink);
            Thread.sleep(500);
            forumLink.click();
            Thread.sleep(1500);
            forumPageLoaded = true;
        }
    }

    @Test(priority = 0, description = "Verifikasi halaman judul Forum ditampilkan dengan benar")
    public void shouldDisplayPageTitle() throws InterruptedException {
        MainApp.executeTest("Forum Page Title", "Verify forum page title displays correctly", () -> {
            navigateToForum();

            String pageTitle = driver.getTitle();
            assertTrue(pageTitle.toLowerCase().contains("forum") || pageTitle.toLowerCase().contains("chamilo"),
                    "Judul halaman harus memuat kata 'forum' atau 'chamilo'");
            logSuccess("✓ Halaman judul 'Forum' ditampilkan dengan benar di browser tab");

            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            assertTrue(heading.getText().toLowerCase().contains("forum"),
                    "Heading h1 harus memuat kata 'forum'");
            logSuccess("✓ Halaman judul terlihat di halaman (h1 tag)");
        }, 1200, TEST_CLASS_NAME);
    }

    @Test(priority = 1, description = "Halaman forum menampilkan judul utama dan intro")
    public void shouldShowTitleAndIntro() throws InterruptedException {
        MainApp.executeTest("Forum Title and Intro", "Verify forum page shows title and introduction text", () -> {
            navigateToForum();

            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            scrollIntoView(title);
            Thread.sleep(800);
            assertTrue(title.getText().toLowerCase().contains("forum"), "Judul halaman harus memuat kata Forum");
            logSuccess("✓ Judul halaman menampilkan kata 'Forum'");

            WebElement intro = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'need a hand')]")));
            scrollIntoView(intro);
            Thread.sleep(800);
            assertTrue(intro.isDisplayed(), "Subjudul Need a hand with Chamilo? harus terlihat");
            logSuccess("✓ Subjudul 'Need a hand with Chamilo?' terlihat");

            WebElement subtitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), 'Check our community forums') or contains(text(), 'find answers to your questions')]")));
            assertTrue(subtitle.isDisplayed(), "Deskripsi intro harus terlihat");
            logSuccess("✓ Deskripsi intro tentang community forums ditampilkan dengan benar");
        }, 1200, TEST_CLASS_NAME);
    }

    @Test(priority = 2, description = "Gambar forum utama dimuat dengan baik")
    public void shouldRenderForumImage() throws InterruptedException {
        MainApp.executeTest("Forum Image Rendering", "Verify forum page shows main forum image with valid dimensions", () -> {
            navigateToForum();

            List<By> imageLocators = List.of(
                    By.xpath("//img[contains(@src, 'foro') or contains(@src, 'forum')]"),
                    By.cssSelector(".avia-image-container img"),
                    By.cssSelector("article img"));

            WebElement image = findFirstDisplayed(imageLocators);
            assertNotNull(image, "Gambar forum harus ditemukan");
            logSuccess("✓ Forum image (foro_global) ditemukan");
            scrollIntoView(image);
            Thread.sleep(800);
            assertTrue(image.getSize().getHeight() > 10 && image.getSize().getWidth() > 10,
                    "Dimensi gambar forum harus masuk akal");
            logSuccess("✓ Gambar forum dimuat dengan ukuran yang masuk akal");
        }, 1200, TEST_CLASS_NAME);
    }

    @Test(priority = 3, description = "Section Global Forum menampilkan CTA ke GitHub Discussions")
    public void shouldExposeGlobalForumCTA() throws InterruptedException {
        MainApp.executeTest("Global Forum CTA", "Verify Global Forum section shows GitHub Discussions link", () -> {
            navigateToForum();

            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'global forum')]")));
            scrollIntoView(heading);
            Thread.sleep(800);
            assertTrue(heading.isDisplayed(), "Judul Global Forum harus terlihat");
            logSuccess("✓ Judul seksi 'Global Forum' terlihat");

            WebElement description = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//*[contains(text(), 'Our forum accepts messages') or contains(text(), 'Join the global discussion')]")));
            assertTrue(description.isDisplayed(), "Deskripsi Global Forum harus terlihat");
            logSuccess("✓ Deskripsi tentang forum yang menerima pesan dalam semua bahasa ditampilkan");

            WebElement cta = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//a[contains(@href, 'github.com/chamilo/chamilo-lms/discussions')]")));
            scrollIntoView(cta);
            Thread.sleep(800);
            assertTrue(cta.isDisplayed(), "CTA GitHub Discussions harus terlihat");
            logSuccess("✓ CTA 'Join the global discussion' button terlihat");
            assertTrue(cta.getAttribute("href").contains("github.com/chamilo/chamilo-lms/discussions"),
                    "Link CTA harus menuju GitHub Discussions");
            logSuccess("✓ CTA link mengarahkan ke GitHub Discussions yang benar");
        }, 1200, TEST_CLASS_NAME);
    }

    @Test(priority = 4, description = "Banner konferensi muncul dan dapat diklik")
    public void shouldDisplayConferenceBanner() throws InterruptedException {
        MainApp.executeTest("Conference Banner", "Verify conference banner displays and links to conference.chamilo.org", () -> {
            navigateToForum();

            WebElement bannerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//a[contains(@href, 'conference.chamilo.org')]")));
            scrollIntoView(bannerLink);
            Thread.sleep(800);
            assertTrue(bannerLink.isDisplayed(), "Banner konferensi harus terlihat");
            logSuccess("✓ Banner konferensi ditampilkan");
            assertTrue(bannerLink.getAttribute("href").contains("conference.chamilo.org"),
                    "Link banner harus mengarah ke conference.chamilo.org");
            logSuccess("✓ Banner mengarahkan ke conference.chamilo.org dengan benar");
        }, 1200, TEST_CLASS_NAME);
    }

    @Test(priority = 5, description = "Link eksternal utama valid")
    public void shouldHaveValidExternalLinks() throws InterruptedException {
        MainApp.executeTest("External Links Validation", "Verify all external links have valid absolute URLs", () -> {
            navigateToForum();

            List<WebElement> links = List.of(
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                            "//a[contains(@href, 'github.com/chamilo/chamilo-lms/discussions')]"))),
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                            "//a[contains(@href, 'conference.chamilo.org')]"))));

            for (WebElement link : links) {
                scrollIntoView(link);
                Thread.sleep(800);
                String href = link.getAttribute("href");
                assertNotNull(href, "Link eksternal harus memiliki href");
                logSuccess("✓ Link eksternal memiliki atribut href");
                assertFalse(href.isBlank(), "Href tidak boleh kosong");
                logSuccess("✓ Href tidak kosong");
                assertTrue(href.startsWith("http"), "Href eksternal harus absolute url");
                logSuccess("✓ Link eksternal menggunakan absolute URL");
            }
        }, 1200, TEST_CLASS_NAME);
    }

    private WebElement findFirstDisplayed(List<By> locators) {
        for (By locator : locators) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement el : elements) {
                    if (el.isDisplayed()) {
                        return el;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
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

    private void logSuccess(String message) {
        System.out.println("[ForumPageTest] " + message);
        TestLogger.logTestEvent(TEST_CLASS_NAME, message);
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
