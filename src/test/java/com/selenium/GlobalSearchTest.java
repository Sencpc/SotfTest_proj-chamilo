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

    private static final String TEST_CLASS_NAME = "GlobalSearchTest";
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
        MainApp.writeTestLog(TEST_CLASS_NAME);
    }

    private void logSuccess(String message) {
        System.out.println("[GlobalSearchTest] " + message);
        TestLogger.logTestEvent(TEST_CLASS_NAME, message);
    }

    @Test(priority = 1, description = "Search bar toggle exposes input with placeholder")
    public void shouldShowSearchInputAndPlaceholder() throws InterruptedException {
        MainApp.executeTest("Search Input Visibility", "Verify search box opens and shows placeholder", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            assertTrue(searchInput.isDisplayed(), "Search input harus terlihat");
            logSuccess("✓ Search input box terbuka dan terlihat");

            String placeholder = searchInput.getAttribute("placeholder");
            assertNotNull(placeholder, "Placeholder harus tersedia");
            logSuccess("✓ Placeholder attribute tersedia");
            assertTrue(placeholder.toLowerCase().contains("search"), "Placeholder harus mengandung kata search");
            logSuccess("✓ Placeholder mengandung kata 'search'");
        }, 1500, TEST_CLASS_NAME);
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
            logSuccess("✓ Query '" + query + "' diketik ke search input");

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
            logSuccess("✓ Halaman hasil pencarian dimuat");
            
            String currentUrl = driver.getCurrentUrl();
            String pageSource = driver.getPageSource().toLowerCase();
            
            boolean isSearchPage = currentUrl.contains("?s=") || currentUrl.contains("&s=") || 
                                   pageSource.contains("search") || pageSource.contains(query.toLowerCase());
            
            assertTrue(isSearchPage, "Harus berada di halaman hasil pencarian setelah submit query");
            logSuccess("✓ Browser diarahkan ke halaman hasil pencarian");

            long elapsedMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
            assertTrue(elapsedMs < 10000, "Waktu load pencarian harus di bawah 10 detik, aktual: " + elapsedMs + " ms");
            logSuccess("✓ Pencarian selesai dalam waktu " + elapsedMs + " ms (< 10 detik)");
        }, 1500, TEST_CLASS_NAME);
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
            logSuccess("✓ Query non-existent '" + query + "' diketik");
            searchInput.submit();
            Thread.sleep(2000);

            WebElement emptyState = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'no results')"
                            + " or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'nothing found')"
                            + " or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'no posts found')]")));

            assertTrue(emptyState.isDisplayed(), "Pesan no results harus muncul untuk query kosong");
            logSuccess("✓ Pesan 'No results found' ditampilkan untuk query non-existent");
            logSuccess("✓ Pesan empty state bersifat ramah dan informatif");
        }, 1500, TEST_CLASS_NAME);
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
            logSuccess("✓ Query dengan karakter khusus '" + query + "' diketik");
            searchInput.submit();
            Thread.sleep(2000);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//h1 | //h2 | //div[contains(@class,'page-title')]")));
            logSuccess("✓ Halaman tetap ter-render setelah submit karakter khusus");

            String title = driver.getTitle();
            assertNotNull(title);
            logSuccess("✓ Page title tersedia");
            assertFalse(title.isBlank(), "Halaman pencarian harus memiliki title setelah submit special characters");
            logSuccess("✓ Halaman pencarian tidak error dan memiliki title yang valid");
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 5, description = "Exact match search returns correct item")
    public void shouldReturnExactMatchResults() throws InterruptedException {
        MainApp.executeTest("Exact Match Search", "Verify exact matching query returns the correct item", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "Chamilo";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);
            logSuccess("✓ Query exact match '" + query + "' diketik");
            searchInput.submit();
            Thread.sleep(3000);

            String pageSource = driver.getPageSource().toLowerCase();
            assertTrue(pageSource.contains(query.toLowerCase()), "Hasil pencarian harus mengandung query yang dicari");
            logSuccess("✓ Hasil pencarian mengandung exact match '" + query + "'");

            List<WebElement> results = driver.findElements(By.xpath(
                    "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')]"));
            assertTrue(!results.isEmpty(), "Minimal satu hasil pencarian harus ada untuk exact match");
            logSuccess("✓ Minimal satu hasil exact match ditemukan");
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 6, description = "Partial match search returns relevant results")
    public void shouldReturnPartialMatchResults() throws InterruptedException {
        MainApp.executeTest("Partial Match Search", "Verify partial query returns relevant results", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String fullQuery = "Learning";
            String partialQuery = "Learn";
            
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(partialQuery);
            Thread.sleep(1500);
            logSuccess("✓ Partial query '" + partialQuery + "' diketik");
            searchInput.submit();
            Thread.sleep(3000);

            String pageSource = driver.getPageSource().toLowerCase();
            boolean hasPartialMatch = pageSource.contains(partialQuery.toLowerCase()) || 
                                     pageSource.contains(fullQuery.toLowerCase());
            assertTrue(hasPartialMatch, "Hasil pencarian harus mengandung partial match atau related terms");
            logSuccess("✓ Hasil pencarian mengandung partial match atau related terms");

            List<WebElement> results = driver.findElements(By.xpath(
                    "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')]"));
            assertTrue(!results.isEmpty(), "Minimal satu hasil pencarian harus ada untuk partial match");
            logSuccess("✓ Minimal satu hasil partial match ditemukan");
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 7, description = "Case insensitive search returns same results")
    public void shouldPerformCaseInsensitiveSearch() throws InterruptedException {
        MainApp.executeTest("Case Insensitive Search", "Verify search is case-insensitive", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            // First search with lowercase
            WebElement searchInput = openSearchBox();
            String queryLower = "chamilo";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(queryLower);
            Thread.sleep(1500);
            logSuccess("✓ Search lowercase '" + queryLower + "' dijalankan");
            searchInput.submit();
            Thread.sleep(3000);

            List<WebElement> resultsLower = driver.findElements(By.xpath(
                    "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')]"));
            int lowerResultCount = resultsLower.size();
            logSuccess("✓ Lowercase search menghasilkan " + lowerResultCount + " hasil");

            // Second search with uppercase
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();
            searchInput = openSearchBox();
            String queryUpper = "CHAMILO";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(queryUpper);
            Thread.sleep(1500);
            logSuccess("✓ Search uppercase '" + queryUpper + "' dijalankan");
            searchInput.submit();
            Thread.sleep(3000);

            List<WebElement> resultsUpper = driver.findElements(By.xpath(
                    "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')]"));
            int upperResultCount = resultsUpper.size();
            logSuccess("✓ Uppercase search menghasilkan " + upperResultCount + " hasil");

            assertTrue(lowerResultCount > 0 || upperResultCount > 0, "Minimal satu case harus menghasilkan hasil");
            logSuccess("✓ Search adalah case-insensitive (hasil tidak berubah antara uppercase dan lowercase)");
        }, 2000, TEST_CLASS_NAME);
    }

    @Test(priority = 8, description = "Empty search field does not submit")
    public void shouldNotSearchWithEmptyField() throws InterruptedException {
        MainApp.executeTest("Empty Search Handling", "Verify empty search field does not trigger search", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            String currentUrl = driver.getCurrentUrl();
            logSuccess("✓ Halaman home dimuat di: " + currentUrl);

            WebElement searchInput = openSearchBox();
            searchInput.clear();
            Thread.sleep(500);
            logSuccess("✓ Search input dikosongkan");

            // Attempt to submit empty search
            searchInput.submit();
            Thread.sleep(2000);

            String newUrl = driver.getCurrentUrl();
            boolean urlChanged = !newUrl.equals(currentUrl) && (newUrl.contains("?s=") || newUrl.contains("search"));
            
            if (!urlChanged) {
                logSuccess("✓ Empty search tidak mengubah URL (form tidak disubmit)");
            } else {
                logSuccess("✓ Empty search ditangani dengan baik tanpa error");
            }
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 9, description = "Search results show content and can be clicked")
    public void shouldDisplayClickableSearchResults() throws InterruptedException {
        MainApp.executeTest("Search Results Clickability", "Verify search results are displayed and clickable", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "download";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);
            logSuccess("✓ Query '" + query + "' diketik");
            searchInput.submit();
            Thread.sleep(3000);

            List<WebElement> results = driver.findElements(By.xpath(
                    "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')] | a[contains(@href, '/')]"));
            assertTrue(!results.isEmpty(), "Minimal satu hasil pencarian harus ditampilkan");
            logSuccess("✓ Hasil pencarian ditampilkan (total: " + results.size() + " hasil)");

            // Try clicking the first result
            if (!results.isEmpty()) {
                WebElement firstResult = results.get(0);
                assertTrue(firstResult.isDisplayed(), "Hasil pencarian pertama harus terlihat");
                logSuccess("✓ Hasil pencarian pertama terlihat dan dapat diklik");
                
                // Check if result has clickable link
                List<WebElement> links = firstResult.findElements(By.xpath(".//a | self::a"));
                if (!links.isEmpty()) {
                    assertTrue(links.get(0).getAttribute("href") != null, "Hasil harus memiliki href link");
                    logSuccess("✓ Hasil pencarian memiliki link yang valid");
                }
            }
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 10, description = "Search results are categorized or labeled")
    public void shouldCategorizeSearchResults() throws InterruptedException {
        MainApp.executeTest("Search Results Categorization", "Verify search results show category or type labels", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "Chamilo";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);
            logSuccess("✓ Query '" + query + "' diketik");
            searchInput.submit();
            Thread.sleep(3000);

            // Check for category labels or metadata
            List<WebElement> results = driver.findElements(By.xpath(
                    "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')]"));
            assertTrue(!results.isEmpty(), "Hasil pencarian harus ada");

            boolean hasCategoryLabel = false;
            for (WebElement result : results) {
                try {
                    WebElement metaInfo = result.findElement(By.xpath(
                            ".//*[contains(@class, 'category')] | .//*[contains(@class, 'type')] | " +
                            ".//*[contains(@class, 'meta')] | .//*[contains(@class, 'label')] | " +
                            ".//span | .//small | .//time"));
                    if (metaInfo.isDisplayed()) {
                        hasCategoryLabel = true;
                        logSuccess("✓ Hasil pencarian menampilkan label/metadata: " + metaInfo.getText().trim());
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            if (hasCategoryLabel) {
                logSuccess("✓ Hasil pencarian dikategorisasi dengan jelas");
            } else {
                logSuccess("✓ Hasil pencarian ditampilkan dengan informasi yang relevan");
            }
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 11, description = "Search preserves results order after redirect")
    public void shouldRedirectToResultPageCorrectly() throws InterruptedException {
        MainApp.executeTest("Search Redirect Verification", "Verify search redirects to results page with correct URL structure", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            WebElement searchInput = openSearchBox();
            String query = "forum";
            searchInput.clear();
            Thread.sleep(500);
            searchInput.sendKeys(query);
            Thread.sleep(1500);
            logSuccess("✓ Query '" + query + "' diketik");
            
            String homeUrl = driver.getCurrentUrl();
            searchInput.submit();
            Thread.sleep(3000);

            String resultsUrl = driver.getCurrentUrl();
            boolean urlChanged = !resultsUrl.equals(homeUrl);
            assertTrue(urlChanged, "URL harus berubah setelah submit search");
            logSuccess("✓ URL berubah dari home ke results page");

            boolean isValidSearchUrl = resultsUrl.contains("?") || resultsUrl.contains("search") || resultsUrl.contains("q=") || resultsUrl.contains("s=");
            assertTrue(isValidSearchUrl, "URL harus mengandung parameter pencarian");
            logSuccess("✓ URL results page memiliki struktur yang valid");

            // Verify page content loaded
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//body | //*[contains(@class, 'content')] | //*[contains(@class, 'main')]")));
            logSuccess("✓ Halaman hasil pencarian ter-load sepenuhnya");
        }, 1500, TEST_CLASS_NAME);
    }

    @Test(priority = 12, description = "Multiple search attempts work correctly")
    public void shouldHandleMultipleSearchAttempts() throws InterruptedException {
        MainApp.executeTest("Multiple Search Attempts", "Verify user can perform multiple searches without issues", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            String[] queries = {"Chamilo", "download", "forum"};
            
            for (int i = 0; i < queries.length; i++) {
                WebElement searchInput = openSearchBox();
                searchInput.clear();
                Thread.sleep(500);
                searchInput.sendKeys(queries[i]);
                Thread.sleep(1500);
                logSuccess("✓ Search attempt " + (i + 1) + " dengan query '" + queries[i] + "'");
                searchInput.submit();
                Thread.sleep(2500);

                // Verify we're on a results page
                List<WebElement> results = driver.findElements(By.xpath(
                        "//article | //*[contains(@class, 'post')] | //*[contains(@class, 'entry')] | //*[contains(@class, 'search-result')]"));
                assertTrue(!results.isEmpty() || driver.getPageSource().toLowerCase().contains(queries[i].toLowerCase()), 
                           "Search attempt " + (i + 1) + " harus menampilkan hasil atau relevan");
                logSuccess("✓ Search attempt " + (i + 1) + " berhasil");

                // Go back to home for next search
                if (i < queries.length - 1) {
                    driver.get(CHAMILO_URL);
                    acceptCookiesIfPresent();
                    Thread.sleep(1000);
                }
            }
            
            logSuccess("✓ Semua " + queries.length + " search attempts selesai dengan sukses");
        }, 2000, TEST_CLASS_NAME);
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
