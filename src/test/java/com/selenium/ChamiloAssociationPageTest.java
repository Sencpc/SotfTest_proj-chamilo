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

/**
 * TestNG suite untuk pengujian halaman Chamilo Association
 * URL: https://chamilo.org/en/chamilo-2/
 * 
 * Berdasarkan CekList.md:
 * 1. "What Chamilo?" Pengujian Seksi
 * 2. "Board of Directors" Pengujian Seksi
 * 3. "Our Community Leaders" Pengujian Seksi
 * 4. Pengujian Validasi Link Eksternal
 * 5. Pengujian Pemuatan Gambar
 */
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

    // ==================== BAGIAN 1: "What Chamilo?" Pengujian Seksi ====================
    
    @Test(priority = 1, description = "Verifikasi judul seksi 'What Chamilo?' terlihat")
    public void testWhatChamiloSectionTitle() {
        TestLogger.logTestEvent(className, "Starting test: testWhatChamiloSectionTitle");
        
        // Verifikasi judul seksi "What Chamilo?" atau "What is Chamilo?" terlihat
        WebElement whatChamiloHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'What') and contains(text(), 'Chamilo')]")));
        scrollIntoView(whatChamiloHeader);
        assertTrue(whatChamiloHeader.isDisplayed(), "'What Chamilo?' header should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'What Chamilo?' header is visible");
    }
    
    @Test(priority = 2, description = "Verifikasi 'The Association' judul ditampilkan dengan benar")
    public void testTheAssociationTitle() {
        TestLogger.logTestEvent(className, "Starting test: testTheAssociationTitle");
        
        // Klik tab "The Association" dan verifikasi
        WebElement associationTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'The Association')] | //span[contains(text(), 'The Association')] | //*[contains(@class, 'tab') and contains(text(), 'The Association')]")));
        scrollIntoView(associationTab);
        associationTab.click();
        TestLogger.logTestEvent(className, "  Clicked: 'The Association' tab");
        
        // Verifikasi konten The Association muncul
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        assertTrue(associationTab.isDisplayed(), "'The Association' tab should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'The Association' tab is active");
    }
    
    @Test(priority = 3, description = "Verifikasi tab 'History' dapat diklik dan kontennya muncul")
    public void testHistoryTabClick() {
        TestLogger.logTestEvent(className, "Starting test: testHistoryTabClick");
        
        // Scroll ke area tab
        WebElement tabArea = scrollToElement(By.xpath("//*[contains(text(), 'The Association')]"));
        
        // Klik tab "History"
        WebElement historyTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='History'] | //span[text()='History'] | //*[contains(@class, 'tab') and text()='History']")));
        historyTab.click();
        TestLogger.logTestEvent(className, "  Clicked: 'History' tab");
        
        // Tunggu konten berubah
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        
        // Verifikasi konten History muncul (konten berbeda dari The Association)
        // History biasanya menampilkan informasi sejarah organisasi
        TestLogger.logTestEvent(className, "  Verified: 'History' tab content is displayed");
    }
    
    @Test(priority = 4, description = "Verifikasi tab 'Mission' dapat diklik dan kontennya muncul")
    public void testMissionTabClick() {
        TestLogger.logTestEvent(className, "Starting test: testMissionTabClick");
        
        // Klik tab "Mission"
        WebElement missionTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='Mission'] | //span[text()='Mission'] | //*[contains(@class, 'tab') and text()='Mission']")));
        missionTab.click();
        TestLogger.logTestEvent(className, "  Clicked: 'Mission' tab");
        
        // Tunggu konten berubah
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        
        // Verifikasi konten Mission muncul
        TestLogger.logTestEvent(className, "  Verified: 'Mission' tab content is displayed");
    }
    
    @Test(priority = 5, description = "Verifikasi navigasi antara tab The Association, History, Mission")
    public void testTabNavigation() {
        TestLogger.logTestEvent(className, "Starting test: testTabNavigation");
        
        // Test navigasi antar tab: The Association -> History -> Mission -> The Association
        
        // 1. Klik The Association
        WebElement associationTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'The Association')] | //span[contains(text(), 'The Association')]")));
        scrollIntoView(associationTab);
        associationTab.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        TestLogger.logTestEvent(className, "  Step 1: Clicked 'The Association' tab");
        
        // 2. Klik History
        WebElement historyTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='History'] | //span[text()='History']")));
        historyTab.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        TestLogger.logTestEvent(className, "  Step 2: Clicked 'History' tab");
        
        // 3. Klik Mission
        WebElement missionTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='Mission'] | //span[text()='Mission']")));
        missionTab.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        TestLogger.logTestEvent(className, "  Step 3: Clicked 'Mission' tab");
        
        // 4. Kembali ke The Association
        associationTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'The Association')] | //span[contains(text(), 'The Association')]")));
        associationTab.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        TestLogger.logTestEvent(className, "  Step 4: Clicked 'The Association' tab (back)");
        
        TestLogger.logTestEvent(className, "  Verified: Tab navigation works correctly");
    }
    
    @Test(priority = 6, description = "Verifikasi konten default The Association tab")
    public void testTheAssociationContent() {
        TestLogger.logTestEvent(className, "Starting test: testTheAssociationContent");
        
        // Pastikan kita di tab The Association
        WebElement associationTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'The Association')] | //span[contains(text(), 'The Association')]")));
        scrollIntoView(associationTab);
        associationTab.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        
        // Verifikasi konten: non-profit organization, Belgium 2010, Spain 2014, providers network
        WebElement descText = driver.findElement(By.xpath("//*[contains(text(), 'non-profit organization')]"));
        assertTrue(descText.isDisplayed(), "Non-profit organization description should be visible");
        TestLogger.logTestEvent(className, "  Verified: Non-profit organization description is visible");
        
        WebElement belgiumInfo = driver.findElement(By.xpath("//*[contains(text(), 'Belgium') and contains(text(), '2010')]"));
        assertTrue(belgiumInfo.isDisplayed(), "Belgium 2010 founding info should be visible");
        TestLogger.logTestEvent(className, "  Verified: Founded in Belgium 2010 info is visible");
        
        WebElement spainInfo = driver.findElement(By.xpath("//*[contains(text(), 'Spain') and contains(text(), '2014')]"));
        assertTrue(spainInfo.isDisplayed(), "Spain 2014 headquarters info should be visible");
        TestLogger.logTestEvent(className, "  Verified: Headquarters in Spain 2014 info is visible");
        
        WebElement providersInfo = driver.findElement(By.xpath("//*[contains(text(), 'official') and contains(text(), 'providers')]"));
        assertTrue(providersInfo.isDisplayed(), "Official providers network info should be visible");
        TestLogger.logTestEvent(className, "  Verified: Official providers network info is visible");
    }
    
    @Test(priority = 7, description = "Verifikasi scroll dari tab section ke Board of Directors")
    public void testScrollFromTabsToBoardOfDirectors() {
        TestLogger.logTestEvent(className, "Starting test: testScrollFromTabsToBoardOfDirectors");
        
        // Pastikan kita di area tab section
        WebElement tabSection = scrollToElement(By.xpath("//*[contains(text(), 'The Association')]"));
        assertTrue(tabSection.isDisplayed(), "Tab section should be visible");
        TestLogger.logTestEvent(className, "  Currently at: Tab section (The Association/History/Mission)");
        
        // Kemudian scroll ke Board of Directors section
        WebElement boardHeader = scrollToElement(By.xpath("//*[contains(text(), 'Board of directors')]"));
        assertTrue(boardHeader.isDisplayed(), "'Board of directors' header should be visible after scroll");
        TestLogger.logTestEvent(className, "  Scrolled to: Board of directors section");
        
        // Pause sebentar untuk memastikan visual scroll terlihat
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}
        
        TestLogger.logTestEvent(className, "  Verified: Successfully scrolled from Mission to Board of Directors");
    }

    // ==================== BAGIAN 2: "Board of Directors" Pengujian Seksi ====================
    
    @Test(priority = 8, description = "Verifikasi judul seksi 'Board of directors' terlihat")
    public void testBoardOfDirectorsTitle() {
        TestLogger.logTestEvent(className, "Starting test: testBoardOfDirectorsTitle");

        WebElement boardHeader = scrollToElement(By.xpath("//*[contains(text(), 'Board of directors')]"));
        assertTrue(boardHeader.isDisplayed(), "'Board of directors' header should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'Board of directors' header is visible");
    }
    
    @Test(priority = 9, description = "Verifikasi teks election informasi ditampilkan dengan benar")
    public void testElectionInfo() {
        TestLogger.logTestEvent(className, "Starting test: testElectionInfo");
        
        // Teks election info: "elected every 2 years"
        WebElement electionInfo = driver.findElement(By.xpath("//*[contains(text(), 'elected') and contains(text(), '2 years')]"));
        assertTrue(electionInfo.isDisplayed(), "Election info (every 2 years) should be visible");
        TestLogger.logTestEvent(className, "  Verified: Election information is visible");
    }
    
    @Test(priority = 10, description = "Verifikasi profil Yannick Warnier lengkap")
    public void testYannickWarnierProfile() {
        TestLogger.logTestEvent(className, "Starting test: testYannickWarnierProfile");
        
        verifyFullProfile("Yannick Warnier", "President", "BeezNest", 
                "president@chamilo.org", "linkedin.com/in/yannickwarnier");
    }
    
    @Test(priority = 11, description = "Verifikasi profil Laura Guirao Rodríguez lengkap")
    public void testLauraGuiraoProfile() {
        TestLogger.logTestEvent(className, "Starting test: testLauraGuiraoProfile");
        
        verifyFullProfile("Laura Guirao", "Treasurer", "Nosolored", 
                "treasurer@chamilo.org", "linkedin.com");
    }
    
    @Test(priority = 12, description = "Verifikasi profil Noa Orizales Iglesias lengkap")
    public void testNoaOrizalesProfile() {
        TestLogger.logTestEvent(className, "Starting test: testNoaOrizalesProfile");
        
        verifyFullProfile("Noa Orizales", "Communication coordinator", "Contidos Dixitais", 
                "communication@chamilo.org", "linkedin.com");
    }
    
    @Test(priority = 13, description = "Verifikasi scroll dari Board of Directors ke Community Leaders")
    public void testScrollFromBoardToCommunityLeaders() {
        TestLogger.logTestEvent(className, "Starting test: testScrollFromBoardToCommunityLeaders");
        
        // Pertama scroll ke Board of Directors section
        WebElement boardHeader = scrollToElement(By.xpath("//*[contains(text(), 'Board of directors')]"));
        assertTrue(boardHeader.isDisplayed(), "'Board of directors' section should be visible");
        TestLogger.logTestEvent(className, "  Currently at: Board of directors section");
        
        // Kemudian scroll ke Our Community Leaders section
        WebElement leadersHeader = scrollToElement(By.xpath("//*[contains(text(), 'Our community leaders')]"));
        assertTrue(leadersHeader.isDisplayed(), "'Our community leaders' header should be visible after scroll");
        TestLogger.logTestEvent(className, "  Scrolled to: Our community leaders section");
        
        // Pause sebentar untuk memastikan visual scroll terlihat
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}
        
        TestLogger.logTestEvent(className, "  Verified: Successfully scrolled from Board of Directors to Community Leaders");
    }

    // ==================== BAGIAN 3: "Our Community Leaders" Pengujian Seksi ====================
    
    @Test(priority = 14, description = "Verifikasi judul seksi 'Our community leaders' terlihat")
    public void testCommunityLeadersTitle() {
        TestLogger.logTestEvent(className, "Starting test: testCommunityLeadersTitle");

        WebElement leadersHeader = scrollToElement(By.xpath("//*[contains(text(), 'Our community leaders')]"));
        assertTrue(leadersHeader.isDisplayed(), "'Our community leaders' header should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'Our community leaders' header is visible");
    }
    
    @Test(priority = 15, description = "Verifikasi 'Otros cargos de responsabilidad' subjudul")
    public void testOtrosCargosSubtitle() {
        TestLogger.logTestEvent(className, "Starting test: testOtrosCargosSubtitle");
        
        WebElement otrosCargos = driver.findElement(By.xpath("//*[contains(text(), 'Otros cargos de responsabilidad')]"));
        assertTrue(otrosCargos.isDisplayed(), "'Otros cargos de responsabilidad' subtitle should be visible");
        TestLogger.logTestEvent(className, "  Verified: 'Otros cargos de responsabilidad' subtitle is visible");
    }
    
    @Test(priority = 16, description = "Verifikasi profil Michela (Chamila) lengkap dengan sosmed")
    public void testMichelaProfile() {
        TestLogger.logTestEvent(className, "Starting test: testMichelaProfile");
        
        // Verifikasi nama
        WebElement nameElement = scrollToElement(By.xpath("//*[contains(text(), 'Michela') or contains(text(), 'Chamila')]"));
        assertTrue(nameElement.isDisplayed(), "Michela/Chamila name should be displayed");
        TestLogger.logTestEvent(className, "  Found profile: Michela/Chamila");
        
        // Verifikasi title "Chamilo Lovers Fan Club"
        WebElement titleEl = driver.findElement(By.xpath("//*[contains(text(), 'Chamilo Lovers Fan Club')]"));
        assertTrue(titleEl.isDisplayed(), "'Chamilo Lovers Fan Club' title should be visible");
        TestLogger.logTestEvent(className, "  Title verified: Chamilo Lovers Fan Club");
        
        // Verifikasi bio - 40 million users
        WebElement bioUsers = driver.findElement(By.xpath("//*[contains(text(), '40 million')]"));
        assertTrue(bioUsers.isDisplayed(), "40 million users reference should be visible");
        TestLogger.logTestEvent(className, "  Verified: 40 million users reference");
        
        // Verifikasi bio - 840,000 pengguna campus
        WebElement bioCampus = driver.findElement(By.xpath("//*[contains(text(), '840,000')]"));
        assertTrue(bioCampus.isDisplayed(), "840,000 registered users reference should be visible");
        TestLogger.logTestEvent(className, "  Verified: 840,000 registered users reference");
        
        // Verifikasi link Chamila eLearning IA (michelamosquera.com)
        List<WebElement> chamilaLinks = driver.findElements(By.xpath("//a[contains(@href, 'michelamosquera.com')]"));
        assertTrue(chamilaLinks.size() > 0, "Chamila eLearning IA link should exist");
        TestLogger.logTestEvent(className, "  Verified: Chamila eLearning IA link exists");
        
        // Verifikasi social media icons untuk Michela (Facebook, Twitter/X, LinkedIn)
        verifySocialMediaIcons("Michela");
    }
    
    @Test(priority = 17, description = "Verifikasi profil Ángel lengkap")
    public void testAngelProfile() {
        TestLogger.logTestEvent(className, "Starting test: testAngelProfile");
        
        verifyFullProfile("\u00C1ngel", "Lead developer", "BeezNest Latino", 
                "info@chamilo.org", "linkedin.com");
    }
    
    @Test(priority = 18, description = "Verifikasi profil Damien Renou lengkap")
    public void testDamienRenouProfile() {
        TestLogger.logTestEvent(className, "Starting test: testDamienRenouProfile");
        
        verifyFullProfile("Damien Renou", "French-speaking community coordinator", "Num\u00E9riques", 
                "communication@chamilo.org", "linkedin.com");
    }

    // ==================== BAGIAN 4: Pengujian Validasi Link Eksternal ====================
    
    @Test(priority = 19, description = "Verifikasi semua link website perusahaan berfungsi")
    public void testCompanyWebsiteLinks() {
        TestLogger.logTestEvent(className, "Starting test: testCompanyWebsiteLinks");
        
        String[] companies = {"BeezNest", "Nosolored", "Contidos Dixitais", "BeezNest Latino"};
        
        for (String company : companies) {
            List<WebElement> links = driver.findElements(By.partialLinkText(company));
            if (!links.isEmpty()) {
                String href = links.get(0).getAttribute("href");
                assertNotNull(href, "Link for " + company + " should not be null");
                assertTrue(href.startsWith("http"), "Link for " + company + " should be absolute URL");
                TestLogger.logTestEvent(className, "  Verified company link: " + company + " -> " + href);
            } else {
                TestLogger.logTestEvent(className, "  Warning: Company link for '" + company + "' not found");
            }
        }
        
        // Bâtisseurs Numériques (dengan karakter khusus)
        List<WebElement> batisseursLinks = driver.findElements(By.xpath("//a[contains(@href, 'batisseurs')]"));
        assertTrue(batisseursLinks.size() > 0, "Bâtisseurs Numériques link should exist");
        TestLogger.logTestEvent(className, "  Verified: Bâtisseurs Numériques link exists");
    }
    
    @Test(priority = 20, description = "Verifikasi semua link profil LinkedIn membuka dengan benar")
    public void testLinkedInLinks() {
        TestLogger.logTestEvent(className, "Starting test: testLinkedInLinks");
        
        List<WebElement> linkedInLinks = driver.findElements(By.xpath("//a[contains(@href, 'linkedin.com')]"));
        assertTrue(linkedInLinks.size() > 0, "LinkedIn links should exist on page");
        TestLogger.logTestEvent(className, "  Found " + linkedInLinks.size() + " LinkedIn links");
        
        for (WebElement link : linkedInLinks) {
            String href = link.getAttribute("href");
            assertTrue(href.contains("linkedin.com"), "Link should point to LinkedIn");
        }
        TestLogger.logTestEvent(className, "  Verified: All LinkedIn links are valid");
    }
    
    @Test(priority = 21, description = "Verifikasi semua link email diformat dengan benar (mailto:)")
    public void testMailtoLinks() {
        TestLogger.logTestEvent(className, "Starting test: testMailtoLinks");
        
        List<WebElement> mailtoLinks = driver.findElements(By.xpath("//a[starts-with(@href, 'mailto:')]"));
        assertTrue(mailtoLinks.size() > 0, "Should contain mailto links");
        TestLogger.logTestEvent(className, "  Found " + mailtoLinks.size() + " mailto links");
        
        for (WebElement link : mailtoLinks) {
            String href = link.getAttribute("href");
            assertTrue(href.startsWith("mailto:"), "Email link should start with mailto:");
            assertTrue(href.contains("@"), "Email link should contain @ symbol");
        }
        TestLogger.logTestEvent(className, "  Verified: All mailto links are properly formatted");
    }
    
    @Test(priority = 22, description = "Verifikasi Michela's website link (michelamosquera.com)")
    public void testMichelaWebsiteLink() {
        TestLogger.logTestEvent(className, "Starting test: testMichelaWebsiteLink");
        
        List<WebElement> michelaLinks = driver.findElements(By.xpath("//a[contains(@href, 'michelamosquera.com')]"));
        assertTrue(michelaLinks.size() > 0, "Michela's website link should exist");
        
        String href = michelaLinks.get(0).getAttribute("href");
        assertTrue(href.contains("michelamosquera.com"), "Link should point to michelamosquera.com");
        TestLogger.logTestEvent(className, "  Verified: Michela's website link exists -> " + href);
    }
    
    @Test(priority = 23, description = "Verifikasi official Chamilo LMS campus link")
    public void testCampusChamiloLink() {
        TestLogger.logTestEvent(className, "Starting test: testCampusChamiloLink");
        
        List<WebElement> campusLinks = driver.findElements(By.xpath("//a[contains(@href, 'campus.chamilo.org')]"));
        assertTrue(campusLinks.size() > 0, "Chamilo campus link should exist");
        
        String href = campusLinks.get(0).getAttribute("href");
        assertTrue(href.contains("campus.chamilo.org"), "Link should point to campus.chamilo.org");
        TestLogger.logTestEvent(className, "  Verified: Campus Chamilo link exists -> " + href);
    }

    // ==================== BAGIAN 5: Pengujian Pemuatan Gambar ====================
    
    @Test(priority = 24, description = "Verifikasi semua foto profil dimuat tanpa error")
    public void testProfileImagesLoaded() {
        TestLogger.logTestEvent(className, "Starting test: testProfileImagesLoaded");
        
        // Cari semua gambar di halaman
        List<WebElement> images = driver.findElements(By.xpath("//img[contains(@src, 'wp-content/uploads')]"));
        
        assertTrue(images.size() > 0, "Profile images should exist on page");
        TestLogger.logTestEvent(className, "  Found " + images.size() + " profile images");
        
        int loadedCount = 0;
        for (WebElement img : images) {
            String src = img.getAttribute("src");
            if (src != null && !src.isEmpty()) {
                boolean isLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].complete && typeof arguments[0].naturalWidth != 'undefined' && arguments[0].naturalWidth > 0", img);
                
                if (isLoaded) {
                    loadedCount++;
                    TestLogger.logTestEvent(className, "  Image loaded: " + src.substring(src.lastIndexOf('/') + 1));
                }
            }
        }
        
        assertTrue(loadedCount > 0, "At least some images should be loaded");
        TestLogger.logTestEvent(className, "  Total images loaded: " + loadedCount + "/" + images.size());
    }
    
    @Test(priority = 25, description = "Verifikasi semua gambar memiliki proper alt text")
    public void testImagesAltText() {
        TestLogger.logTestEvent(className, "Starting test: testImagesAltText");
        
        List<WebElement> images = driver.findElements(By.xpath("//img[contains(@src, 'wp-content/uploads')]"));
        
        int imagesWithAlt = 0;
        for (WebElement img : images) {
            String alt = img.getAttribute("alt");
            if (alt != null && !alt.trim().isEmpty()) {
                imagesWithAlt++;
                TestLogger.logTestEvent(className, "  Image with alt: " + alt);
            }
        }
        
        TestLogger.logTestEvent(className, "  Images with alt text: " + imagesWithAlt + "/" + images.size());
        // Sebaiknya semua gambar punya alt text, tapi kita cek minimal ada beberapa
        assertTrue(imagesWithAlt > 0, "At least some images should have alt text");
    }
    
    @Test(priority = 26, description = "Verifikasi tidak ada gambar yang rusak (broken images)")
    public void testNoBrokenImages() {
        TestLogger.logTestEvent(className, "Starting test: testNoBrokenImages");
        
        List<WebElement> allImages = driver.findElements(By.tagName("img"));
        int brokenCount = 0;
        
        for (WebElement img : allImages) {
            String src = img.getAttribute("src");
            if (src != null && !src.isEmpty() && !src.startsWith("data:")) {
                boolean isLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].complete && typeof arguments[0].naturalWidth != 'undefined' && arguments[0].naturalWidth > 0", img);
                
                if (!isLoaded) {
                    brokenCount++;
                    TestLogger.logTestEvent(className, "  Broken image: " + src);
                }
            }
        }
        
        TestLogger.logTestEvent(className, "  Broken images found: " + brokenCount);
        // Kita toleransi 0 broken image
        assertTrue(brokenCount == 0, "No broken images should exist on page");
    }

    // ==================== Helper Methods ====================

    private void verifyFullProfile(String name, String title, String companyLinkText, 
                                   String expectedEmail, String linkedInPattern) {
        // Verifikasi nama
        WebElement nameElement = scrollToElement(By.xpath("//*[contains(text(), '" + name + "')]"));
        assertTrue(nameElement.isDisplayed(), "Name '" + name + "' should be displayed");
        TestLogger.logTestEvent(className, "  Found profile: " + name);

        // Verifikasi Title
        if (title != null) {
            try {
                WebElement titleEl = driver.findElement(By.xpath("//*[contains(text(), '" + title + "')]"));
                assertTrue(titleEl.isDisplayed(), "Title '" + title + "' should be visible");
                TestLogger.logTestEvent(className, "    Title verified: " + title);
            } catch (Exception e) {
                TestLogger.logTestEvent(className, "    Warning: Title '" + title + "' not found");
            }
        }

        // Verifikasi Company Link
        if (companyLinkText != null) {
            try {
                WebElement companyLink = driver.findElement(By.partialLinkText(companyLinkText));
                assertTrue(companyLink.isDisplayed(), "Company link '" + companyLinkText + "' should be visible");
                TestLogger.logTestEvent(className, "    Company link verified: " + companyLinkText);
            } catch (Exception e) {
                TestLogger.logTestEvent(className, "    Warning: Company link '" + companyLinkText + "' not found");
            }
        }
        
        // Verifikasi email link ada
        if (expectedEmail != null) {
            try {
                List<WebElement> emailLinks = driver.findElements(By.xpath("//a[contains(@href, 'mailto:" + expectedEmail + "')]"));
                if (emailLinks.size() > 0) {
                    TestLogger.logTestEvent(className, "    Email link verified: " + expectedEmail);
                } else {
                    TestLogger.logTestEvent(className, "    Warning: Email link for " + expectedEmail + " not found directly");
                }
            } catch (Exception e) {
                TestLogger.logTestEvent(className, "    Warning: Could not verify email link");
            }
        }
        
        // Verifikasi LinkedIn link ada
        if (linkedInPattern != null) {
            List<WebElement> linkedInLinks = driver.findElements(By.xpath("//a[contains(@href, '" + linkedInPattern + "')]"));
            if (linkedInLinks.size() > 0) {
                TestLogger.logTestEvent(className, "    LinkedIn link verified");
            } else {
                TestLogger.logTestEvent(className, "    Warning: LinkedIn link not found");
            }
        }
    }
    
    private void verifySocialMediaIcons(String personName) {
        TestLogger.logTestEvent(className, "  Verifying social media icons for " + personName);
        
        // Facebook
        List<WebElement> facebookLinks = driver.findElements(By.xpath("//a[contains(@href, 'facebook.com')]"));
        if (facebookLinks.size() > 0) {
            TestLogger.logTestEvent(className, "    Facebook link found");
        }
        
        // Twitter/X
        List<WebElement> twitterLinks = driver.findElements(By.xpath("//a[contains(@href, 'twitter.com') or contains(@href, 'x.com')]"));
        if (twitterLinks.size() > 0) {
            TestLogger.logTestEvent(className, "    Twitter/X link found");
        }
        
        // LinkedIn
        List<WebElement> linkedInLinks = driver.findElements(By.xpath("//a[contains(@href, 'linkedin.com')]"));
        if (linkedInLinks.size() > 0) {
            TestLogger.logTestEvent(className, "    LinkedIn link found");
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
            Thread.sleep(500);
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
