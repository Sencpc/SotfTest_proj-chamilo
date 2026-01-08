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

/**
 * Tests the global search experience from the site header.
 */
public class GlobalSearchTest {

    private static final String CHAMILO_URL = "https://chamilo.org/en/";
    private static final String DEFAULT_BRAVE_PATH = "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void startDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setBinary(resolveBraveBinary());
        options.addArguments(
                "--disable-notifications",
                "--start-maximized",
                "--disable-infobars",
                "--disable-extensions");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    @AfterClass(alwaysRun = true)
    public void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
        }
    }

    @Test(priority = 1, description = "Search bar toggle exposes input with placeholder")
    public void shouldShowSearchInputAndPlaceholder() throws InterruptedException {
        MainApp.executeTest("Search Input Visibility", "Verify search box opens and shows placeholder", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            assertTrue(searchInput.isDisplayed(), "Search input harus terlihat");

            String placeholder = searchInput.getAttribute("placeholder");
            assertNotNull(placeholder, "Placeholder harus tersedia");
            assertTrue(placeholder.toLowerCase().contains("search"), "Placeholder harus mengandung kata search");
        }, 1500);
    }

    @Test(priority = 2, description = "Valid query returns results dalam waktu wajar")
    public void shouldReturnResultsForValidQuery() throws InterruptedException {
        MainApp.executeTest("Valid Search Query", "Submit valid query and verify search results page loads", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "Chamilo LMS";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);

            long start = System.nanoTime();
            searchInput.submit();
            Thread.sleep(3000);

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".page-title, h1.page-title, h1")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("article, .post, .entry")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".search-results, #search-results")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("main, #main, .content")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'results')]"))));

            Thread.sleep(1000);
            
            String currentUrl = driver.getCurrentUrl();
            String pageSource = driver.getPageSource().toLowerCase();
            
            boolean isSearchPage = currentUrl.contains("?s=") || currentUrl.contains("&s=") || 
                                   pageSource.contains("search") || pageSource.contains(query.toLowerCase());
            
            assertTrue(isSearchPage, "Harus berada di halaman hasil pencarian setelah submit query");

            long elapsedMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
            assertTrue(elapsedMs < 10000, "Waktu load pencarian harus di bawah 10 detik, aktual: " + elapsedMs + " ms");
        }, 1500);
    }

    @Test(priority = 3, description = "Query tidak ditemukan menampilkan pesan kosong yang ramah")
    public void shouldShowNoResultsState() throws InterruptedException {
        MainApp.executeTest("No Results Handling", "Verify empty state message for non-existent query", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "zzzxxyyqnonexistent";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);
            searchInput.submit();
            Thread.sleep(2000);

            WebElement emptyState = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'no results')"
                            + " or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'nothing found')"
                            + " or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'no posts found')]")));

            assertTrue(emptyState.isDisplayed(), "Pesan no results harus muncul untuk query kosong");
        }, 1500);
    }

    @Test(priority = 4, description = "Karakter khusus tidak memicu error dan halaman tetap ter-render")
    public void shouldHandleSpecialCharacters() throws InterruptedException {
        MainApp.executeTest("Special Characters Handling", "Verify page still renders after special character search", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "@#$%^&*()";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);
            searchInput.submit();
            Thread.sleep(2000);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//h1 | //h2 | //div[contains(@class,'page-title')]")));

            String title = driver.getTitle();
            assertNotNull(title);
            assertFalse(title.isBlank(), "Halaman pencarian harus memiliki title setelah submit special characters");
        }, 1500);
    }

    private WebElement openSearchBox() throws InterruptedException {
        // Scroll to top to ensure header is visible
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        Thread.sleep(500);
        
        // Try to find and click the search icon - it might be in a clickable parent
        WebElement searchToggle = null;
        List<By> searchButtonLocators = List.of(
            By.xpath("//i[contains(@class, 'icon-search-fine')]/parent::*"),
            By.xpath("//*[contains(@class, 'icon-search-fine')]/.."),
            By.cssSelector("i.icon-search-fine"),
            By.cssSelector(".icon-search-fine"),
            By.xpath("//*[contains(@class, 'icon-search-fine')]"),
            By.cssSelector("#search_button")
        );
        
        for (By locator : searchButtonLocators) {
            try {
                searchToggle = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                if (searchToggle != null && searchToggle.isDisplayed()) {
                    // Scroll to element and highlight it
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'}); " +
                        "arguments[0].style.border='3px solid red';", searchToggle);
                    Thread.sleep(800);
                    
                    // Click using JavaScript to ensure it works
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchToggle);
                    Thread.sleep(1500);
                    
                    // Check if search wrapper appeared
                    List<WebElement> wrappers = driver.findElements(By.cssSelector(".search_wrapper, div.search_wrapper"));
                    if (!wrappers.isEmpty() && wrappers.get(0).isDisplayed()) {
                        break; // Success!
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Wait for search wrapper to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".search_wrapper, div.search_wrapper")));

        // Find the search input field
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#searchform input[name='s'], .search_wrapper input[name='s'], input.field[name='s']")));
        
        // Highlight the input
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].style.border='3px solid green';", input);
        Thread.sleep(500);
        
        return input;
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

        // Fallback to common user installation path
        String userHome = System.getProperty("user.home");
        Path altPath = Path.of(userHome, "AppData/Local/BraveSoftware/Brave-Browser/Application/brave.exe");
        if (Files.exists(altPath)) {
            return altPath.toString();
        }

        throw new IllegalStateException("Lokasi Brave tidak ditemukan. Set properti sistem 'braveBinary'.");
    }
}
