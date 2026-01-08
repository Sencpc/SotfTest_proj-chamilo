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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * TestNG suite that validates the Providers page on
 * https://chamilo.org/en/providers/
 * 
 * Test Cases:
 * 1. Pengujian Judul Halaman
 * 2. Pengujian Header Seksi (Providers description)
 * 3. Pengujian Premium Providers Section
 * 4. Pengujian Provider Cards (BeezNest, Bâtisseurs Numériques, etc.)
 * 5. Pengujian Contact Information
 * 6. Pengujian External Links
 * 7. Pengujian Provider Certification Info
 * 8. Pengujian Footer Elements
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

    // ==================== 1. PENGUJIAN JUDUL HALAMAN ====================

    @Test(priority = 1, description = "Verifikasi halaman judul 'Providers' ditampilkan dengan benar")
    public void shouldDisplayPageTitle() {
        driver.get(PROVIDERS_URL);
        acceptCookiesIfPresent();

        // Verify page title in browser tab
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.toLowerCase().contains("provider") || pageTitle.toLowerCase().contains("chamilo"),
                "Page title should contain 'provider' or 'chamilo'");
        logSuccess("Page title verified: " + pageTitle);
    }

    @Test(priority = 2, description = "Verifikasi URL halaman providers benar")
    public void shouldHaveCorrectURL() {
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("providers"), "URL should contain 'providers'");
        logSuccess("URL verified: " + currentUrl);
    }

    @Test(priority = 3, description = "Verifikasi heading 'Providers' terlihat pada halaman")
    public void shouldDisplayProvidersHeading() {
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(), 'Providers')]")));
        assertTrue(heading.isDisplayed(), "Providers heading should be visible");
        logSuccess("Providers heading is displayed");
    }

    // ==================== 2. PENGUJIAN HEADER SEKSI ====================

    @Test(priority = 4, description = "Verifikasi deskripsi tentang official providers ditampilkan")
    public void shouldDisplayProvidersDescription() {
        WebElement description = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'official providers')]")));
        assertTrue(description.isDisplayed(), "Official providers description should be visible");
        logSuccess("Providers description is displayed");
    }

    @Test(priority = 5, description = "Verifikasi teks 'If you need professional help' ditampilkan")
    public void shouldDisplayProfessionalHelpText() {
        WebElement helpText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'professional help')]")));
        assertTrue(helpText.isDisplayed(), "Professional help text should be visible");
        logSuccess("Professional help text is displayed");
    }

    // ==================== 3. PENGUJIAN PREMIUM PROVIDERS SECTION ====================

    @Test(priority = 6, description = "Verifikasi section 'PREMIUM' terlihat")
    public void shouldDisplayPremiumSection() {
        WebElement premiumSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'PREMIUM')]")));
        assertTrue(premiumSection.isDisplayed(), "PREMIUM section should be visible");
        logSuccess("PREMIUM section is displayed");
    }

    // ==================== 4. PENGUJIAN PROVIDER CARDS ====================

    @Test(priority = 7, description = "Verifikasi BeezNest Belgium provider card ditampilkan")
    public void shouldDisplayBeezNestBelgiumCard() {
        WebElement providerCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'BeezNest Belgium')]")));
        assertTrue(providerCard.isDisplayed(), "BeezNest Belgium provider card should be visible");
        logSuccess("BeezNest Belgium provider card is displayed");
    }

    @Test(priority = 8, description = "Verifikasi Bâtisseurs Numériques provider card ditampilkan")
    public void shouldDisplayBatisseursNumeriquesCard() {
        WebElement providerCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Bâtisseurs Numériques')]")));
        assertTrue(providerCard.isDisplayed(), "Bâtisseurs Numériques provider card should be visible");
        logSuccess("Bâtisseurs Numériques provider card is displayed");
    }

    @Test(priority = 9, description = "Verifikasi Contidos Dixitais provider card ditampilkan")
    public void shouldDisplayContidosDixitaisCard() {
        WebElement providerCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Contidos Dixitais')]")));
        assertTrue(providerCard.isDisplayed(), "Contidos Dixitais provider card should be visible");
        logSuccess("Contidos Dixitais provider card is displayed");
    }

    @Test(priority = 10, description = "Verifikasi Nosolored provider card ditampilkan")
    public void shouldDisplayNosoloredCard() {
        WebElement providerCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Nosolored')]")));
        assertTrue(providerCard.isDisplayed(), "Nosolored provider card should be visible");
        logSuccess("Nosolored provider card is displayed");
    }

    @Test(priority = 11, description = "Verifikasi BeezNest Latino provider card ditampilkan")
    public void shouldDisplayBeezNestLatinoCard() {
        WebElement providerCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'BeezNest Latino')]")));
        assertTrue(providerCard.isDisplayed(), "BeezNest Latino provider card should be visible");
        logSuccess("BeezNest Latino provider card is displayed");
    }

    // ==================== 5. PENGUJIAN PROVIDER LOGOS ====================

    @Test(priority = 12, description = "Verifikasi logo provider images dimuat dengan benar")
    public void shouldDisplayProviderLogos() {
        List<WebElement> providerLogos = driver.findElements(
                By.xpath("//img[contains(@src, 'chamilo.org/wp-content/uploads')]"));
        assertFalse(providerLogos.isEmpty(), "Provider logos should be present on the page");
        
        int visibleLogos = 0;
        for (WebElement logo : providerLogos) {
            if (logo.isDisplayed()) {
                visibleLogos++;
            }
        }
        assertTrue(visibleLogos > 0, "At least one provider logo should be visible");
        logSuccess("Provider logos found: " + visibleLogos + " visible logos");
    }

    // ==================== 6. PENGUJIAN CONTACT INFORMATION ====================

    @Test(priority = 13, description = "Verifikasi informasi kontak phone numbers ada pada halaman")
    public void shouldDisplayPhoneNumbers() {
        // Check page source for phone numbers since they may be in different elements
        String pageSource = driver.getPageSource();
        
        // Check for at least one phone indicator
        boolean hasPhoneInfo = pageSource.contains("Phone") || pageSource.contains("phone");
        assertTrue(hasPhoneInfo, "Phone information should be present on the page");
        logSuccess("Phone information is present on the page");
    }

    @Test(priority = 14, description = "Verifikasi email addresses ada pada halaman")
    public void shouldDisplayEmailAddresses() {
        // Check page source for email addresses
        String pageSource = driver.getPageSource();
        
        boolean hasSalesEmail = pageSource.contains("sales@beeznest.com");
        boolean hasInfoEmail = pageSource.contains("info@");
        boolean hasProvidersEmail = pageSource.contains("providers@chamilo.org");
        
        assertTrue(hasSalesEmail || hasInfoEmail || hasProvidersEmail, 
                "At least one email address should be present on the page");
        logSuccess("Email addresses are present on the page");
    }

    // ==================== 7. PENGUJIAN EXTERNAL LINKS ====================

    @Test(priority = 15, description = "Verifikasi link BeezNest website ada dan dapat diklik")
    public void shouldHaveBeezNestWebsiteLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'beeznest.com')]")));
        assertTrue(link.isDisplayed(), "BeezNest website link should be visible");
        String href = link.getAttribute("href");
        assertTrue(href.contains("beeznest.com"), "Link should point to beeznest.com");
        logSuccess("BeezNest website link is clickable: " + href);
    }

    @Test(priority = 19, description = "Verifikasi link Bâtisseurs Numériques website ada dan dapat diklik")
    public void shouldHaveBatisseursNumeriquesWebsiteLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'batisseurs-numeriques')]")));
        assertTrue(link.isDisplayed(), "Bâtisseurs Numériques website link should be visible");
        String href = link.getAttribute("href");
        assertTrue(href.contains("batisseurs-numeriques"), "Link should point to batisseurs-numeriques");
        logSuccess("Bâtisseurs Numériques website link is clickable: " + href);
    }

    @Test(priority = 20, description = "Verifikasi link Contidos Dixitais website ada dan dapat diklik")
    public void shouldHaveContidosDixitaisWebsiteLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'contidosdixitais')]")));
        assertTrue(link.isDisplayed(), "Contidos Dixitais website link should be visible");
        String href = link.getAttribute("href");
        assertTrue(href.contains("contidosdixitais"), "Link should point to contidosdixitais");
        logSuccess("Contidos Dixitais website link is clickable: " + href);
    }

    @Test(priority = 21, description = "Verifikasi link Nosolored website ada dan dapat diklik")
    public void shouldHaveNosoloredWebsiteLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'nosolored')]")));
        assertTrue(link.isDisplayed(), "Nosolored website link should be visible");
        String href = link.getAttribute("href");
        assertTrue(href.contains("nosolored"), "Link should point to nosolored");
        logSuccess("Nosolored website link is clickable: " + href);
    }

    // ==================== 8. PENGUJIAN PROVIDER LOCATION INFO ====================

    @Test(priority = 22, description = "Verifikasi informasi lokasi Belgium ditampilkan")
    public void shouldDisplayBelgiumLocation() {
        WebElement location = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Belgium')]")));
        assertTrue(location.isDisplayed(), "Belgium location should be visible");
        logSuccess("Belgium location is displayed");
    }

    @Test(priority = 23, description = "Verifikasi informasi lokasi France ditampilkan")
    public void shouldDisplayFranceLocation() {
        WebElement location = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'France')]")));
        assertTrue(location.isDisplayed(), "France location should be visible");
        logSuccess("France location is displayed");
    }

    @Test(priority = 24, description = "Verifikasi informasi lokasi Spain ditampilkan")
    public void shouldDisplaySpainLocation() {
        WebElement location = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Spain')]")));
        assertTrue(location.isDisplayed(), "Spain location should be visible");
        logSuccess("Spain location is displayed");
    }

    @Test(priority = 25, description = "Verifikasi informasi lokasi Latin America ditampilkan")
    public void shouldDisplayLatinAmericaLocation() {
        WebElement location = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Latin America')]")));
        assertTrue(location.isDisplayed(), "Latin America location should be visible");
        logSuccess("Latin America location is displayed");
    }

    // ==================== 9. PENGUJIAN PROVIDER SERVICES ====================

    @Test(priority = 26, description = "Verifikasi layanan 'Development' ditampilkan")
    public void shouldDisplayDevelopmentService() {
        WebElement service = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Development')]")));
        assertTrue(service.isDisplayed(), "Development service should be visible");
        logSuccess("Development service is displayed");
    }

    @Test(priority = 27, description = "Verifikasi layanan 'SaaS' ditampilkan")
    public void shouldDisplaySaaSService() {
        WebElement service = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'SaaS')]")));
        assertTrue(service.isDisplayed(), "SaaS service should be visible");
        logSuccess("SaaS service is displayed");
    }

    @Test(priority = 28, description = "Verifikasi layanan 'Content' ditampilkan")
    public void shouldDisplayContentService() {
        // Check page source for Content service since it may be in different elements
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Content"), "Content service should be present on the page");
        logSuccess("Content service is displayed");
    }

    // ==================== 10. PENGUJIAN PROVIDER CERTIFICATION INFO ====================

    @Test(priority = 29, description = "Verifikasi informasi 'Provider from' year ditampilkan")
    public void shouldDisplayProviderFromYear() {
        WebElement providerYear = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Provider from')]")));
        assertTrue(providerYear.isDisplayed(), "Provider from year should be visible");
        logSuccess("Provider from year information is displayed");
    }

    @Test(priority = 30, description = "Verifikasi informasi certification process ditampilkan")
    public void shouldDisplayCertificationInfo() {
        scrollToBottom();
        WebElement certInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'certification')]")));
        assertTrue(certInfo.isDisplayed(), "Certification process info should be visible");
        logSuccess("Certification process information is displayed");
    }

    // ==================== 11. PENGUJIAN FOOTER ELEMENTS ====================

    @Test(priority = 31, description = "Verifikasi copyright text 'Chamilo © 2024' ditampilkan")
    public void shouldDisplayCopyrightText() {
        scrollToBottom();
        WebElement copyright = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Chamilo ©')]")));
        assertTrue(copyright.isDisplayed(), "Copyright text should be visible");
        logSuccess("Copyright text is displayed");
    }

    @Test(priority = 32, description = "Verifikasi link 'Aviso Legal' ada di footer")
    public void shouldHaveAvisoLegalLink() {
        scrollToBottom();
        WebElement avisoLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Aviso Legal')]")));
        assertTrue(avisoLink.isDisplayed(), "Aviso Legal link should be visible");
        logSuccess("Aviso Legal link is displayed in footer");
    }

    @Test(priority = 33, description = "Verifikasi social media icons (Facebook, Twitter, YouTube) ada di footer")
    public void shouldDisplaySocialMediaIcons() {
        scrollToBottom();
        
        // Check Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href, 'facebook.com')]")));
        assertTrue(facebookLink != null, "Facebook link should be present");
        
        // Check Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href, 'twitter.com')]")));
        assertTrue(twitterLink != null, "Twitter link should be present");
        
        // Check YouTube link
        WebElement youtubeLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@href, 'youtube.com')]")));
        assertTrue(youtubeLink != null, "YouTube link should be present");
        
        logSuccess("Social media icons (Facebook, Twitter, YouTube) are present in footer");
    }

    // ==================== 12. PENGUJIAN QUOTE REQUEST INFO ====================

    @Test(priority = 34, description = "Verifikasi informasi untuk request quote ke semua providers ditampilkan")
    public void shouldDisplayQuoteRequestInfo() {
        // Check page source for quote information
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("quote") || pageSource.contains("providers@chamilo.org"), 
                "Quote request information should be present on the page");
        logSuccess("Quote request information is displayed");
    }

    // ==================== HELPER METHODS ====================

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

    private void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        try {
            Thread.sleep(500); // Wait for scroll animation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void logSuccess(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] SUCCESS: %s%n", timestamp, message);
        System.out.println("✓ " + message);

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
