package com.selenium;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EventPageTest extends ChamiloBaseTest {

    private static final String EVENTS_PAGE_URL = "https://chamilo.org/en/eventos/";
    private static final String EVENTS_SLUG = "/eventos";

    @Override
    protected void openHome() {
        // Keep step delay consistent (2 seconds). Base implementation uses ~1s.
        driver.get(CHAMILO_URL);
        acceptCookiesIfPresent();
        delay2s();
    }

    @Test
    public void openEventsViaTopBarMenu() {
        openHome();

        navigateToEventsViaTopBar();
        acceptCookiesIfPresent();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/en"));

        delay2s();

        Assert.assertTrue(driver.getCurrentUrl().startsWith(CHAMILO_URL),
                "Expected to be on Events page, but was: " + driver.getCurrentUrl());

        clickAllReadMoreInsideList();
    }

    private void clickAllReadMoreInsideList() {
        // Structure:
        // div.column_timeline
        // └── div (timeline_items wrapper)
        // └── ul.timeline_items
        // └── li (multiple items)
        // └── div.desc
        // └── a.button (Read more)
        //
        // When all li in one column_timeline are done, move to next column_timeline.

        Set<String> clicked = new LinkedHashSet<>();
        int timelineIndex = 0;

        while (true) {
            acceptCookiesIfPresent();
            delay2s();

            // Find all column_timeline divs on the page
            List<WebElement> timelineDivs = driver.findElements(
                    By.cssSelector("div.column_timeline"));

            System.out.println("[DEBUG] Found " + timelineDivs.size() + " column_timeline divs");

            if (timelineIndex >= timelineDivs.size()) {
                // Check if we need to scroll to find more timeline divs
                if (!scrollToFindMoreTimelines(timelineDivs.size())) {
                    break; // No more timelines found after scrolling
                }
                // Re-fetch after scroll
                timelineDivs = driver.findElements(By.cssSelector("div.column_timeline"));
                if (timelineIndex >= timelineDivs.size()) {
                    break;
                }
            }

            WebElement currentTimeline = timelineDivs.get(timelineIndex);
            System.out.println("[DEBUG] Processing column_timeline #" + (timelineIndex + 1));

            // Scroll to this timeline div
            scrollToElement(currentTimeline);
            delay2s();

            // Find all li items inside this column_timeline
            List<WebElement> liItems = currentTimeline.findElements(
                    By.cssSelector("ul.timeline_items > li"));

            System.out.println("[DEBUG] Found " + liItems.size() + " li items in this timeline");

            boolean clickedInThisTimeline = false;

            for (int liIndex = 0; liIndex < liItems.size(); liIndex++) {
                // Re-fetch elements to avoid stale reference
                timelineDivs = driver.findElements(By.cssSelector("div.column_timeline"));
                if (timelineIndex >= timelineDivs.size()) {
                    break;
                }
                currentTimeline = timelineDivs.get(timelineIndex);
                liItems = currentTimeline.findElements(By.cssSelector("ul.timeline_items > li"));
                if (liIndex >= liItems.size()) {
                    break;
                }

                WebElement li = liItems.get(liIndex);

                // Find the Read more button inside this li
                List<WebElement> buttons = li.findElements(By.cssSelector(".desc a.button"));
                if (buttons.isEmpty()) {
                    continue;
                }

                WebElement button = buttons.get(0);
                String href;
                try {
                    href = button.getAttribute("href");
                } catch (Exception e) {
                    continue;
                }

                if (href == null || href.isBlank() || clicked.contains(href)) {
                    continue;
                }

                System.out.println("[DEBUG] Clicking Read more #" + (clicked.size() + 1) + ": " + href);

                // Scroll to and click
                scrollToElement(button);
                acceptCookiesIfPresent();
                delay2s();

                openReadMoreAndReturn(button);
                clicked.add(href);
                clickedInThisTimeline = true;

                delay2s();
            }

            // Done with this column_timeline, move to next
            timelineIndex++;
            System.out.println("[DEBUG] Moving to next column_timeline (index " + timelineIndex + ")");

            if (!clickedInThisTimeline && timelineIndex > 10) {
                // Safety: if we've gone through many timelines with no clicks, stop
                break;
            }
        }

        System.out.println("[DEBUG] Total Read more clicked: " + clicked.size());
        Assert.assertFalse(clicked.isEmpty(), "No 'Read more' links were clicked.");
    }

    private boolean scrollToFindMoreTimelines(int currentCount) {
        // Scroll down to find more column_timeline divs
        for (int i = 0; i < 5; i++) {
            scrollDownOneScreen();
            delay2s();

            List<WebElement> timelines = driver.findElements(
                    By.cssSelector("div.column_timeline"));
            if (timelines.size() > currentCount) {
                return true;
            }

            if (isAtBottomOfPage()) {
                return false;
            }
        }
        return false;
    }

    private void delay2s() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center',behavior:'instant'});", element);
    }

    private boolean isAtBottomOfPage() {
        long[] pos = getScrollPosition();
        long scrollY = pos[0];
        long innerH = pos[1];
        long scrollH = pos[2];
        return (scrollY + innerH) >= (scrollH - 2);
    }

    private void openReadMoreAndReturn(WebElement link) {
        int windowsBefore = driver.getWindowHandles().size();
        String beforeUrl = driver.getCurrentUrl();
        String originalWindow = driver.getWindowHandle();

        safeClick(link);
        // Banner can appear right after navigation; click it immediately if present.
        acceptCookiesIfPresent();

        // Case 1: link opens a new tab/window
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> d.getWindowHandles().size() > windowsBefore);

            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);

                    // If it is an external website, scroll to bottom first.
                    acceptCookiesIfPresent();
                    if (isExternalWebsite(driver.getCurrentUrl())) {
                        scrollToBottomWith2sDelay(20);
                    }

                    driver.close();
                    break;
                }
            }
            driver.switchTo().window(originalWindow);
            acceptCookiesIfPresent();
            ensureBackOnEventos();
            return;
        } catch (Exception ignored) {
        }

        // Case 2: navigates in the same tab
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> !d.getCurrentUrl().equals(beforeUrl));
        } catch (Exception ignored) {
        }

        acceptCookiesIfPresent();

        // If redirected to an external website, scroll to bottom, then return.
        if (isExternalWebsite(driver.getCurrentUrl())) {
            scrollToBottomWith2sDelay(20);
            driver.get(EVENTS_PAGE_URL);
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.urlContains(EVENTS_SLUG));
            acceptCookiesIfPresent();
            return;
        }

        // We always want to end up back on the Events list.
        if (!driver.getCurrentUrl().contains(EVENTS_SLUG)) {
            try {
                driver.navigate().back();
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.urlContains(EVENTS_SLUG));
            } catch (Exception ignored) {
                driver.get(EVENTS_PAGE_URL);
                new WebDriverWait(driver, Duration.ofSeconds(8))
                        .until(ExpectedConditions.urlContains(EVENTS_SLUG));
            }
            acceptCookiesIfPresent();
            // Scroll down to make sure we can see more content (2024 section)
            scrollDownOneScreen();
        }
    }

    private void ensureBackOnEventos() {
        try {
            if (!driver.getCurrentUrl().contains(EVENTS_SLUG)) {
                driver.get(EVENTS_PAGE_URL);
                new WebDriverWait(driver, Duration.ofSeconds(8))
                        .until(ExpectedConditions.urlContains(EVENTS_SLUG));
            }
        } catch (Exception ignored) {
            // ignore
        }
    }

    private boolean isExternalWebsite(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            host = host.toLowerCase();
            return !(host.equals("chamilo.org") || host.endsWith(".chamilo.org"));
        } catch (Exception ignored) {
            return false;
        }
    }

    private void scrollToBottomWith2sDelay(int maxSteps) {
        long lastScrollY = -1;
        for (int i = 0; i < maxSteps; i++) {
            acceptCookiesIfPresent();
            long[] pos = getScrollPosition();
            long scrollY = pos[0];
            long innerH = pos[1];
            long scrollH = pos[2];

            boolean atBottom = (scrollY + innerH) >= (scrollH - 2);
            if (atBottom) {
                return;
            }
            if (scrollY == lastScrollY) {
                // Not moving anymore; treat as bottom.
                return;
            }
            lastScrollY = scrollY;

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            delay2s();
        }
    }

    private long[] getScrollPosition() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "return [window.pageYOffset || document.documentElement.scrollTop || 0, " +
                            "window.innerHeight || document.documentElement.clientHeight || 0, " +
                            "Math.max(document.body.scrollHeight, document.documentElement.scrollHeight) || 0];");
            if (result instanceof List) {
                List<?> list = (List<?>) result;
                long y = ((Number) list.get(0)).longValue();
                long h = ((Number) list.get(1)).longValue();
                long sh = ((Number) list.get(2)).longValue();
                return new long[] { y, h, sh };
            }
        } catch (Exception ignored) {
            // ignore
        }
        return new long[] { 0, 0, 0 };
    }

    private void scrollDownOneScreen() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.scrollBy(0, Math.floor(window.innerHeight * 0.85));");
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void navigateToEventsViaTopBar() {
        // DOM based on screenshot:
        // nav#menu > ul#menu-main-menu-en > li#menu-item-2525 >
        // a[href='https://chamilo.org/en/eventos/']
        By[] candidates = new By[] {
                By.cssSelector("nav#menu ul#menu-main-menu-en li#menu-item-2525 > a"),
                By.cssSelector("#menu-main-menu-en #menu-item-2525 > a"),
                By.cssSelector("#menu #menu-item-2525 > a"),
                By.cssSelector("li#menu-item-2525 > a"),
                By.cssSelector("a[href='https://chamilo.org/en/eventos/']")
        };

        WebElement eventsLink = null;
        for (By locator : candidates) {
            try {
                eventsLink = new WebDriverWait(driver, Duration.ofSeconds(6))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                if (eventsLink != null) {
                    break;
                }
            } catch (Exception ignored) {
                // try next locator
            }
        }

        if (eventsLink == null) {
            throw new AssertionError("Cannot find Events link in top bar (menu-item-2525).");
        }

        safeClick(eventsLink);
        acceptCookiesIfPresent();
    }

    private void safeClick(WebElement element) {
        try {
            element.click();
        } catch (Exception ex) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception ignored) {
                throw ex;
            }
        }
    }
}
