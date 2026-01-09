package com.selenium;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

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

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class ChamiloBaseTest {

    protected static final String CHAMILO_URL = "https://chamilo.org/en/";

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeClass(alwaysRun = true)
    public void setUpBase() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        String customBinary = System.getProperty("chromeBinary");
        if (customBinary != null && !customBinary.isBlank()) {
            options.setBinary(customBinary);
        }
        options.addArguments(
                "--disable-notifications",
                "--start-maximized",
                "--disable-infobars",
                "--disable-extensions");

        driver = new ChromeDriver(options);
        // Jangan terlalu besar: kalau elemen tidak ketemu, test akan terasa “hang” lama
        // sekali.
        wait = new WebDriverWait(driver, Duration.ofSeconds(1));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownBase() {
        waitUntilStopped();

        if (driver != null) {
            driver.quit();
        }
    }

    protected void openHome() {
        driver.get(CHAMILO_URL);
        acceptCookiesIfPresent();
        pause1s();
    }

    protected void pause1s() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                    element);
        } catch (Exception ignored) {
            // ignore
        }
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitClickable(By locator, Duration timeout) {
        try {
            return new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            throw e;
        }
    }

    protected void safeClick(By locator) {
        safeClick(locator, Duration.ofSeconds(8));
    }

    protected void safeClick(By locator, Duration timeout) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = waitClickable(locator, timeout);
                scrollIntoView(el);
                try {
                    el.click();
                } catch (Exception clickEx) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    } catch (Exception jsEx) {
                        throw clickEx;
                    }
                }
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException stale) {
                attempts++;
            } catch (Exception e) {
                attempts++;
                if (attempts >= 3) {
                    throw e;
                }
                pause1s();
            }
        }
    }

    protected void acceptCookiesIfPresent() {
        // Keep this method non-blocking: do NOT introduce multi-second waits.
        // The banner can appear at the bottom (vh100% overlay). We click it as
        // soon as it is present, otherwise return immediately.
        List<By> cookieLocators = List.of(
                // Common cookie plugins
                By.cssSelector("button#wt-cli-accept-btn"),
                By.cssSelector("a.cc-dismiss"),

                // Explicit text matches (English/Spanish)
                By.xpath(
                        "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'i agree')]"),
                By.xpath(
                        "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'agree')]"),
                By.xpath(
                        "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'estoy de acuerdo')]"),
                By.xpath(
                        "//a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'estoy de acuerdo')]"),

                // Generic accept
                By.cssSelector("button[aria-label*='Accept']"),
                By.xpath(
                        "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')]"));

        // Fast path: immediate DOM checks (no waits)
        if (tryClickAnyPresent(cookieLocators)) {
            return;
        }

        // Very short poll: some banners render a moment after navigation.
        long end = System.currentTimeMillis() + 250;
        while (System.currentTimeMillis() < end) {
            if (tryClickAnyPresent(cookieLocators)) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    protected void openEventsFromNavbar() {
        acceptCookiesIfPresent();

        By[] candidates = new By[] {
                By.cssSelector("#menu-item-2525 a"),
                By.cssSelector("nav#menu ul#menu-main-menu-en li#menu-item-2525 > a"),
                By.cssSelector("a[href='https://chamilo.org/en/eventos/']"),
                // Tolerant text fallback (handles 'Events'/'Eventos')
                By.xpath("//nav//a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'event')]")
        };

        Exception last = null;
        for (By locator : candidates) {
            try {
                safeClick(locator, Duration.ofSeconds(10));
                acceptCookiesIfPresent();
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.or(
                                ExpectedConditions.urlContains("/eventos"),
                                ExpectedConditions.urlContains("/en")));
                return;
            } catch (Exception e) {
                last = e;
            }
        }
        if (last != null) {
            throw new RuntimeException("Failed to open Events from navbar using known locators", last);
        } else {
            throw new RuntimeException("Failed to open Events from navbar: no candidates tried");
        }
    }

    private boolean tryClickAnyPresent(List<By> locators) {
        for (By locator : locators) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement el : elements) {
                    if (el == null) {
                        continue;
                    }
                    if (!el.isDisplayed()) {
                        continue;
                    }
                    try {
                        el.click();
                    } catch (Exception clickEx) {
                        try {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                        } catch (Exception ignored) {
                            continue;
                        }
                    }
                    return true;
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        return false;
    }

    protected void waitUntilStopped() {
        // Default true sesuai permintaan: test menunggu sampai dimatikan
        boolean keepOpen = Boolean.parseBoolean(System.getProperty("keepOpen", "true"));
        if (!keepOpen) {
            return;
        }

        System.out.println("\nTesting selesai untuk class: " + getClass().getSimpleName());
        System.out
                .println("Browser dibiarkan terbuka. Tutup browser (X) ATAU tekan ENTER di terminal untuk menutup...");

        AtomicBoolean shouldClose = new AtomicBoolean(false);

        Thread stdinWaiter = new Thread(() -> {
            try {
                // Jangan ditutup, agar System.in tetap bisa dipakai test lain
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
                shouldClose.set(true);
                scanner.close();
            } catch (Exception ignored) {
                // ignore
            }
        }, "stdin-waiter");
        stdinWaiter.setDaemon(true);
        stdinWaiter.start();

        while (!shouldClose.get()) {
            try {
                if (driver == null) {
                    break;
                }
                if (driver.getWindowHandles().isEmpty()) {
                    break;
                }
            } catch (Exception ignored) {
                // jika browser sudah ditutup, driver biasanya akan melempar exception
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
