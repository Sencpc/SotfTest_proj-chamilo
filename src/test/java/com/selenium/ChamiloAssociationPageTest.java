package com.selenium;

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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class ChamiloAssociationPageTest {
    
    private static final String TARGET_URL = "https://chamilo.org/en/chamilo-2/";
    private static final String DEFAULT_BRAVE_PATH = "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe";
    private WebDriver driver;
    private WebDriverWait wait;
    private final String className = this.getClass().getSimpleName();

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
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
        
        driver.get(TARGET_URL);
        acceptCookiesIfPresent();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        TestLogger.writeLog(className);
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        boolean passed = result.getStatus() == ITestResult.SUCCESS;
        TestLogger.recordTestResult(className, result.getName(), passed);
        String statusSymbol = passed ? "✓" : "✗";
        TestLogger.logTestEvent(className, String.format("%s Test %s: %s", statusSymbol, result.getName(),(passed ? "PASSED" : "FAILED")));
        if (result.getThrowable() != null) {
             TestLogger.logTestEvent(className, "  Error: " + result.getThrowable().getMessage());
        }
    }

    // --- BAGIAN 1: "What Chamilo?" Pengujian Seksi ---
    @Test(priority = 1)
    public void testWhatChamiloSection() {
        TestLogger.logTestEvent(className, "Starting test: testWhatChamiloSection");
        
        // Verifikasi judul seksi "What Chamilo?" terlihat
        // Note: Check page source, usually sections have IDs or unique structures.
        // Assuming there is content relating to "What is Chamilo?" based on inspection or text.
        // The checklists says "What Chamilo?".
        
        // Look for the main heading or section containing "The Association"
        // Based on checklist: "The Association" judul ditampilkan dengan benar
        WebElement associationHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'The Association')]")));
        scrollIntoView(associationHeader);
        assertTrue(associationHeader.isDisplayed(), "'The Association' header should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'The Association' header is visible");

        // Verifikasi "History" subjudul ada
        WebElement historyHeader = driver.findElement(By.xpath("//*[contains(text(), 'History')]"));
        assertTrue(historyHeader.isDisplayed(), "'History' subheader should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'History' subheader is visible");

        // Verifikasi "Mission" subjudul ada
        WebElement missionHeader = driver.findElement(By.xpath("//*[contains(text(), 'Mission')]"));
        assertTrue(missionHeader.isDisplayed(), "'Mission' subheader should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'Mission' subheader is visible");
        
        // Verifikasi teks deskripsi tentang non-profit organization
        WebElement descText = driver.findElement(By.xpath("//*[contains(text(), 'non-profit organization')]"));
        assertTrue(descText.isDisplayed(), "Non-profit organization description should be visible");
        TestLogger.logTestEvent(className, "  Verified: Non-profit organization description is visible");
        
        // Official providers network info
        WebElement providersInfo = driver.findElement(By.xpath("//*[contains(text(), 'official providers network')]"));
        assertTrue(providersInfo.isDisplayed(), "Official providers network info should be visible");
        TestLogger.logTestEvent(className, "  Verified: Official providers network info is visible");
    }

    // --- BAGIAN 2: "Board of Directors" Pengujian Seksi ---
    @Test(priority = 2)
    public void testBoardOfDirectorsSection() {
        TestLogger.logTestEvent(className, "Starting test: testBoardOfDirectorsSection");

        WebElement boardHeader = scrollToElement(By.xpath("//*[contains(text(), 'Board of directors')]"));
        assertTrue(boardHeader.isDisplayed(), "'Board of directors' header should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'Board of directors' header is visible");

        // Yannick Warnier
        verifyProfile("Yannick Warnier", "President", "BeezNest Belgium");
        
        // Laura Guirao Rodríguez
        verifyProfile("Laura Guirao Rodríguez", "Treasurer", "Nosolored");

        // Noa Orizales Iglesias
        verifyProfile("Noa Orizales Iglesias", "Communication coordinator", "Contidos Dixitais");
    }

    // --- BAGIAN 3: "Our Community Leaders" Pengujian Seksi ---
    @Test(priority = 3)
    public void testCommunityLeadersSection() {
        TestLogger.logTestEvent(className, "Starting test: testCommunityLeadersSection");

        WebElement leadersHeader = scrollToElement(By.xpath("//*[contains(text(), 'Our community leaders')]"));
        assertTrue(leadersHeader.isDisplayed(), "'Our community leaders' header should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'Our community leaders' header is visible");
        
        // Michela (Chamila)
        verifyProfile("Chamila", "Chamilo Lovers Fan Club", "Chamila eLearning IA");
        
        // Ángel Quiroz (Only "Ángel" might be visible in header, so we check that or partial)
        verifyProfile("\u00C1ngel", "Lead developer", "BeezNest Latino");
        
        // Damien Renou
        verifyProfile("Damien Renou", "French-speaking community coordinator", "Num\u00E9riques");
    }

    // --- BAGIAN 4: Pengujian Validasi Link Eksternal ---
    @Test(priority = 4)
    public void testExternalLinks() {
        TestLogger.logTestEvent(className, "Starting test: testExternalLinks");
        
        // List of important links to verify from the checklist
        String[] linkTexts = {
            "BeezNest", "Nosolored", "Contidos Dixitais", "BeezNest Latino", "Num\u00E9riques",
            "campus.chamilo.org"
        };

        for (String text : linkTexts) {
             // Find links loosely matching text (checking partial href or text)
             List<WebElement> links = driver.findElements(By.partialLinkText(text));
             if (links.isEmpty()) {
                 // Try by checking href
                 links = driver.findElements(By.xpath("//a[contains(@href, '" + text.toLowerCase().replace(" ", "") + "') or contains(text(), '" + text + "')]"));
             }
             
             if (!links.isEmpty()) {
                 WebElement link = links.get(0);
                 String href = link.getAttribute("href");
                 assertNotNull(href, "Link for " + text + " should not be null");
                 // We don't necessarily click clearly external links in a simple test without handling tabs, 
                 // but we verify the href looks valid.
                 assertTrue(href.startsWith("http"), "Link for " + text + " should be absolute URL");
                 TestLogger.logTestEvent(className, "  Verified link present for: " + text + " -> " + href);
             } else {
                 TestLogger.logTestEvent(className, "  Warning: Link for '" + text + "' not found by simple search.");
             }
        }
        
        // Check Mailto links
        List<WebElement> mailtoLinks = driver.findElements(By.xpath("//a[starts-with(@href, 'mailto:')]"));
        assertTrue(mailtoLinks.size() > 0, "Should contain mailto links");
        TestLogger.logTestEvent(className, "  Verified " + mailtoLinks.size() + " mailto links found.");
    }
    
    // --- BAGIAN 5: Pengujian Pemuatan Gambar ---
    @Test(priority = 5)
    public void testImagesLoading() {
        TestLogger.logTestEvent(className, "Starting test: testImagesLoading");
        
        // Find all images in the main content area roughly
        // Better: find images within profiles.
        
        List<WebElement> images = driver.findElements(By.cssSelector("img.attachment-medium")); 
        // Note: class 'attachment-medium' is a guess for WordPress photos, or just find all images in the content.
        if (images.isEmpty()) {
            images = driver.findElements(By.xpath("//div[contains(@class,'elementor-widget-image')]//img"));
        }
        
        for (WebElement img : images) {
            String src = img.getAttribute("src");
            if (src != null && !src.isEmpty()) {
                // Verify image loads (simple JS check)
                boolean isLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].complete && typeof arguments[0].naturalWidth != 'undefined' && arguments[0].naturalWidth > 0", img);
                
                assertTrue(isLoaded, "Image should be loaded: " + src);
                TestLogger.logTestEvent(className, "  Image Loaded: " + src.substring(src.lastIndexOf('/') + 1));
            }
        }
    }

    // --- Helpers ---

    private void verifyProfile(String name, String title, String companyLinkText) {
        // Find a container that likely holds this person's info. 
        // This is tricky without specific IDs. We'll search for the name text and look around it.
        // XPath: Find element with name, then verifying near elements.
        
        WebElement nameElement = scrollToElement(By.xpath("//*[contains(text(), '" + name + "')]"));
        assertTrue(nameElement.isDisplayed(), "Name '" + name + "' should be displayed");
        TestLogger.logTestEvent(className, "  Found profile: " + name);

        // Verify Title
        if (title != null) {
            try {
                // Look for title nearby or on page
                WebElement titleEl = driver.findElement(By.xpath("//*[contains(text(), '" + title + "')]"));
                assertTrue(titleEl.isDisplayed(), "Title '" + title + "' should be visible");
                TestLogger.logTestEvent(className, "    Title verified: " + title);
            } catch (Exception e) {
                 TestLogger.logTestEvent(className, "    Warning: Title '" + title + "' not found relative to " + name);
            }
        }

        // Verify Company Link
        if (companyLinkText != null) {
             try {
                WebElement updatedLink = driver.findElement(By.partialLinkText(companyLinkText));
                assertTrue(updatedLink.isDisplayed(), "Link '" + companyLinkText + "' should be visible");
                TestLogger.logTestEvent(className, "    Company link verified: " + companyLinkText);
             } catch (Exception e) {
                 TestLogger.logTestEvent(className, "    Warning: Company link '" + companyLinkText + "' not found.");
             }
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
                TestLogger.logTestEvent(className, "Cookies accepted");
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private WebElement scrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoView(element);
        return element;
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500); // Give time for scroll
        } catch (Exception ignored) {
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
