package com.selenium;

import static org.testng.Assert.assertTrue;

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

/**
 * Simple TestNG suite that validates several critical blocks on
 * https://chamilo.org/en/ using Brave.
 */
public class HomePageTest {

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

    // 1. Slider
    @Test(priority = 1, description = "Memastikan slider berfungsi dan tombol next dapat diklik")
    public void shouldInteractWithSlider() throws Exception {
        MainApp.executeTest("Interact with Slider", "Memastikan slider berfungsi dan tombol next dapat diklik", () -> {
            driver.get(CHAMILO_URL);
            acceptCookiesIfPresent();

            // Wait for slider to be visible
            WebElement slider = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rev_slider_1_1")));
            assertTrue(slider.isDisplayed(), "Slider harus terlihat");
            logSuccess("Slider is displayed");

            // Hover over slider to make arrows visible (sometimes required for revolution
            // slider)
            new org.openqa.selenium.interactions.Actions(driver).moveToElement(slider).perform();

            // Locate and click next arrow
            // The class 'tp-rightarrow' is usually the one for next slide
            WebElement nextArrow = wait
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tp-rightarrow")));

            for (int i = 0; i < 3; i++) {
                nextArrow.click();
                logSuccess("Clicked next slide arrow (" + (i + 1) + "/3)");
                Thread.sleep(1000); // Wait for transition
            }
            MainApp.captureFullPageScreenshot(driver, "cache/HomePage_Slider.png");
        }, "HomePageTest");
    }

    @Test(priority = 2, description = "Mengklik tombol Download, Try It, dan Donate Now di dalam slider")
    public void shouldClickSliderButtons() throws Exception {
        MainApp.executeTest("Click Slider Buttons", "Mengklik tombol Download, Try It, dan Donate Now di dalam slider",
                () -> {
                    String[] buttons = { "DOWNLOAD", "TRY IT", "DONATE NOW" };

                    for (String btnText : buttons) {
                        driver.get(CHAMILO_URL);
                        acceptCookiesIfPresent();

                        // Wait for slider to load
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rev_slider_1_1")));

                        WebElement buttonToClick = null;

                        // Try to find the button by cycling through slides (max 10 attempts)
                        for (int i = 0; i < 10; i++) {
                            try {
                                // Find all candidates containing the text
                                List<WebElement> candidates = driver.findElements(By.xpath(
                                        "//div[contains(@class,'tp-caption')]//a[contains(., '" + btnText + "')]"));

                                for (WebElement candidate : candidates) {
                                    // Check if displayed and has a valid size (not a hidden clone)
                                    if (candidate.isDisplayed() && candidate.getSize().getWidth() > 0) {
                                        buttonToClick = candidate;
                                        break;
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            if (buttonToClick != null)
                                break;

                            // Click next arrow using JS to avoid hover issues
                            try {
                                WebElement nextArrow = driver.findElement(By.cssSelector(".tp-rightarrow"));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextArrow);
                                Thread.sleep(2500); // Wait for slide transition
                            } catch (Exception e) {
                                break;
                            }
                        }

                        if (buttonToClick != null) {
                            // Use JS click for reliability on slider layers
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonToClick);
                            logSuccess("Clicked slider button: " + btnText);
                            Thread.sleep(3000);
                            driver.get(CHAMILO_URL);
                        } else {
                            logSuccess("Button " + btnText + " not found in slider");
                        }
                    }
                    MainApp.captureFullPageScreenshot(driver, "cache/HomePage_SliderButtons.png");
                }, "HomePageTest");
    }

    // 2. Need Help?
    @Test(priority = 3, description = "Memastikan blok 'Need help?' menampilkan tautan bantuan utama Chamilo")
    public void shouldExposeNeedHelpResources() throws Exception {
        MainApp.executeTest("Expose Need Help Resources",
                "Memastikan blok 'Need help?' menampilkan tautan bantuan utama Chamilo", () -> {
                    driver.get(CHAMILO_URL);
                    acceptCookiesIfPresent();

                    WebElement needHelpHeading = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(By.xpath(
                                    "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'need help')]")));
                    assertTrue(needHelpHeading.isDisplayed(), "Judul Need help? harus terlihat");
                    logSuccess("Need help heading is displayed");

                    assertTrue(isLinkVisible("FAQ"), "Tautan FAQ harus tersedia");
                    logSuccess("FAQ link is visible");
                    assertTrue(isLinkVisible("Forum"), "Tautan Forum harus tersedia");
                    logSuccess("Forum link is visible");
                    assertTrue(isLinkVisible("Services"), "Tautan Services providers harus tersedia");
                    logSuccess("Services link is visible");

                    MainApp.captureFullPageScreenshot(driver, "cache/HomePage_NeedHelp.png");
                }, "HomePageTest");
    }

    @Test(priority = 4, description = "Mengklik tautan FAQ, Forum, dan Services providers di bagian Need help?")
    public void shouldNavigateNeedHelpLinks() throws Exception {
        MainApp.executeTest("Navigate Need Help Links",
                "Mengklik tautan FAQ, Forum, dan Services providers di bagian Need help?", () -> {
                    String[] links = { "FAQ", "Forum", "Services providers" };

                    for (String linkText : links) {
                        driver.get(CHAMILO_URL);
                        acceptCookiesIfPresent();

                        // Scroll to "Need help?" section
                        WebElement needHelpHeading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.xpath(
                                        "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'need help')]")));
                        scrollIntoView(needHelpHeading);

                        // Find and click the link
                        // We use XPath to find the anchor tag containing the h4 with the specific text
                        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//h4[contains(text(), '" + linkText + "')]/ancestor::a")));

                        scrollIntoView(link);
                        link.click();

                        logSuccess("Clicked 'Need help?' link: " + linkText);

                        // Wait 3 seconds
                        Thread.sleep(3000);

                        // Go back home
                        driver.get(CHAMILO_URL);
                    }
                    MainApp.captureFullPageScreenshot(driver, "cache/HomePage_NeedHelpLinks.png");
                }, "HomePageTest");
    }

    // 3. Chamilo Universe
    @Test(priority = 5, description = "Mengklik tombol Training, ChamiloTalks, Events, dan Tutorials di bagian Chamilo Universe")
    public void shouldNavigateChamiloUniverseLinks() throws Exception {
        MainApp.executeTest("Navigate Chamilo Universe Links",
                "Mengklik tombol Training, ChamiloTalks, Events, dan Tutorials di bagian Chamilo Universe", () -> {
                    String[] buttons = { "TRAINING", "CHAMILOTALKS", "EVENTS", "TUTORIALS" };

                    for (String btnText : buttons) {
                        driver.get(CHAMILO_URL);
                        acceptCookiesIfPresent();

                        // Scroll to "Chamilo universe" section
                        WebElement universeHeading = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.xpath(
                                        "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'chamilo universe')]")));
                        scrollIntoView(universeHeading);

                        // Find and click the button
                        // The buttons are <a> tags with a <span> containing the text
                        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(), '" + btnText + "')]/ancestor::a")));

                        scrollIntoView(button);
                        button.click();

                        logSuccess("Clicked 'Chamilo Universe' button: " + btnText);

                        // Wait 3 seconds
                        Thread.sleep(3000);

                        // Go back home
                        driver.get(CHAMILO_URL);
                    }
                    MainApp.captureFullPageScreenshot(driver, "cache/HomePage_Universe.png");
                }, "HomePageTest");
    }

    // 4. Do you Like Chamilo
    @Test(priority = 6, description = "Memastikan blok ajakan kontribusi tampil dan dapat diklik")
    public void shouldPromoteContributionCTA() throws Exception {
        MainApp.executeTest("Promote Contribution CTA", "Memastikan blok ajakan kontribusi tampil dan dapat diklik",
                () -> {
                    driver.get(CHAMILO_URL);
                    acceptCookiesIfPresent();

                    WebElement contributionHeading = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(By.xpath(
                                    "//h2[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'do you like chamilo')]")));
                    assertTrue(contributionHeading.isDisplayed(), "Judul Do you like Chamilo? harus terlihat");
                    logSuccess("Contribution heading is displayed");

                    scrollIntoView(contributionHeading);

                    WebElement contributeButton = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='contribute']")));
                    scrollIntoView(contributeButton);
                    wait.until(ExpectedConditions.elementToBeClickable(contributeButton));
                    assertTrue(contributeButton.isDisplayed(), "Tombol CONTRIBUTE harus terlihat");

                    contributeButton.click();
                    logSuccess("Clicked Contribute button");

                    Thread.sleep(3000);
                    driver.get(CHAMILO_URL);

                    MainApp.captureFullPageScreenshot(driver, "cache/HomePage_Contribution.png");
                }, "HomePageTest");
    }

    // 5. Acknowledgments
    @Test(priority = 7, description = "Memastikan link di section Acknowledgments dapat diklik dan kembali ke home")
    public void shouldInteractWithAcknowledgmentsLinks() throws Exception {
        MainApp.executeTest("Interact with Acknowledgments Links",
                "Memastikan link di section Acknowledgments dapat diklik dan kembali ke home", () -> {
                    driver.get(CHAMILO_URL);
                    acceptCookiesIfPresent();

                    // Cari section Acknowledgments
                    WebElement ackSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//h2[contains(text(), 'Acknowledgments')]")));
                    scrollIntoView(ackSection);
                    logSuccess("Acknowledgments section found");

                    // Cari semua link di dalam section wrapper yang sama
                    List<WebElement> links = driver.findElements(By.xpath(
                            "//h2[contains(text(), 'Acknowledgments')]/ancestor::div[contains(@class, 'section_wrapper')]//a"));

                    assertTrue(links.size() > 0, "Harus ada link di section Acknowledgments");
                    logSuccess("Found " + links.size() + " links in Acknowledgments section");

                    String originalWindow = driver.getWindowHandle();

                    for (int i = 0; i < links.size(); i++) {
                        // Refresh list element untuk menghindari StaleElementReferenceException
                        links = driver.findElements(By.xpath(
                                "//h2[contains(text(), 'Acknowledgments')]/ancestor::div[contains(@class, 'section_wrapper')]//a"));
                        WebElement link = links.get(i);
                        String url = link.getAttribute("href");

                        scrollIntoView(link);
                        wait.until(ExpectedConditions.elementToBeClickable(link));

                        // Klik link (gunakan JS untuk kestabilan)
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);

                        // Cek apakah membuka tab baru
                        Set<String> windowHandles = driver.getWindowHandles();
                        if (windowHandles.size() > 1) {
                            // Switch ke tab baru
                            for (String handle : windowHandles) {
                                if (!handle.equals(originalWindow)) {
                                    driver.switchTo().window(handle);
                                    break;
                                }
                            }
                            logSuccess("Clicked link and switched to tab: " + url);
                            Thread.sleep(2000); // Tunggu sebentar untuk visualisasi
                            driver.close(); // Tutup tab
                            driver.switchTo().window(originalWindow); // Kembali ke tab utama
                        } else {
                            // Jika tidak membuka tab baru (fallback)
                            logSuccess("Clicked link (same tab): " + url);
                            Thread.sleep(2000);
                            driver.navigate().back();
                        }

                        // Pastikan kembali ke halaman Chamilo
                        wait.until(ExpectedConditions.urlContains("chamilo.org"));
                    }
                    MainApp.captureFullPageScreenshot(driver, "cache/HomePage_Ack.png");
                }, "HomePageTest");
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

    private boolean isLinkVisible(String partialText) {
        try {
            WebElement link = wait
                    .until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(partialText)));
            return link.isDisplayed();
        } catch (Exception ex) {
            return false;
        }
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (Exception ignored) {
            // jika scroll gagal, biarkan Selenium mencoba klik langsung
        }
    }

    private void logSuccess(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] SUCCESS: %s%n", timestamp, message);

        try {
            Files.createDirectories(Path.of("cache"));
            try (FileWriter fw = new FileWriter("cache/HomePageTest_log.txt", true)) {
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
