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

            WebElement demoMenu = wait
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a")));
            demoMenu.click();

            wait.until(ExpectedConditions.urlContains("demo"));

            logSuccess("Clicked Demo menu and navigated to Demo page");
            Thread.sleep(3000);

            MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_Navigated.png");
        }, "DemoPageTest");
    }

    @Test(priority = 2, description = "Mengklik tombol Go to Free Campus dan kembali")
    public void shouldClickFreeCampusButton() throws Exception {
        MainApp.executeTest("Click Free Campus Button", "Mengklik tombol Go to Free Campus dan kembali", () -> {
            if (!driver.getCurrentUrl().contains("demo")) {
                driver.get(CHAMILO_URL);
                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a"))).click();
            }

            String originalWindow = driver.getWindowHandle();

            WebElement freeCampusButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(
                            "//a[contains(@href, 'campus.chamilo.org') and .//span[contains(text(), 'Go to Free Campus')]]")));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                    freeCampusButton);
            wait.until(ExpectedConditions.elementToBeClickable(freeCampusButton));

            freeCampusButton.click();
            logSuccess("Clicked 'Go to Free Campus' button");

            java.util.Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.size() > 1) {
                for (String handle : windowHandles) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }

                Thread.sleep(3000);
                logSuccess("Switched to new tab: " + driver.getCurrentUrl());

                MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_FreeCampus_NewTab.png");

                driver.close();
                driver.switchTo().window(originalWindow);
                logSuccess("Closed new tab and returned to Demo page");
            } else {
                Thread.sleep(3000);
                MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_FreeCampus_SameTab.png");

                driver.navigate().back();
                logSuccess("Returned to Demo page via Back");
            }

            wait.until(ExpectedConditions.urlContains("demo"));
            MainApp.captureFullPageScreenshot(driver, "cache/DemoPage_FreeCampusReturn.png");
        }, "DemoPageTest");
    }

<<<<<<< HEAD
=======
    @Test(priority = 3, description = "Memeriksa tata bahasa pada semua elemen heading di halaman Demo")
    public void shouldCheckHeadingsGrammar() throws Exception {
        MainApp.executeTest("Check Headings Grammar", "Memeriksa tata bahasa pada semua elemen heading di halaman Demo",
                () -> {
                    if (!driver.getCurrentUrl().contains("demo")) {
                        driver.get(CHAMILO_URL);
                        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu-item-2897 a")))
                                .click();
                    }

                    java.util.Map<String, String> informalWords = new java.util.HashMap<>();
                    informalWords.put("wanna", "want to");
                    informalWords.put("gonna", "going to");
                    informalWords.put("gotta", "have got to");

                    List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
                    boolean foundGrammarIssue = false;

                    logSuccess("Scanning " + headings.size() + " headings for grammar police check...");

                    for (WebElement heading : headings) {
                        if (!heading.isDisplayed())
                            continue;

                        String text = heading.getText().toLowerCase();

                        for (java.util.Map.Entry<String, String> entry : informalWords.entrySet()) {
                            String badWord = entry.getKey();
                            String correction = entry.getValue();

                            if (text.contains(badWord)) {
                                ((JavascriptExecutor) driver)
                                        .executeScript("arguments[0].scrollIntoView({block: 'center'});", heading);

                                String alertMessage = String.format(
                                        "GRAMMAR POLICE ALERT! Found slang '%s' in heading \"%s\". The Queen demands '%s'!",
                                        badWord.toUpperCase(), heading.getText(), correction.toUpperCase());
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

>>>>>>> 1a543c1e8c6b2e863575d98f827f3e21990509f1
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
