package com.selenium;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Duration;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Simple entry point that performs a quick health ping to
 * https://chamilo.org/en/.
 */
public final class MainApp {

    private static final String TARGET_URL = "https://chamilo.org/en/";

    private MainApp() {
        // utility class
    }

    public void captureFullPageScreenshot(WebDriver driver, String fileName) {
        if (!(driver instanceof TakesScreenshot)) {
            System.err.println("Driver does not support screenshots");
            return;
        }

        TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
        File src = screenshotDriver.getScreenshotAs(OutputType.FILE);
        Path target = Path.of(fileName);

        try {
            Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved full page screenshot to: " + target.toAbsolutePath());
        } catch (IOException ioException) {
            System.err.println("Unable to save screenshot: " + ioException.getMessage());
        }
    }

    /**
     * Encapsulates test execution with logging and automatic sleep.
     * Executes a test lambda, logs results, and applies thread sleep.
     * 
     * @param testName Name of the test
     * @param description Description of what the test does
     * @param testCode The test logic as a lambda expression
     * @param sleepMs Sleep duration after test execution (in milliseconds)
     * @param testClassName The name of the test class (for logging purposes)
     */
    public static void executeTest(String testName, String description, TestExecutable testCode, long sleepMs, String testClassName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.println("\n[" + timestamp + "] -> " + testName);
        System.out.println("    Description: " + description);
        
        // Log test start with visual separator
        TestLogger.logTestEvent(testClassName, "--------------------------------------------------------------------------------");
        TestLogger.logTestEvent(testClassName, String.format("Test Name   : %s", testName));
        TestLogger.logTestEvent(testClassName, String.format("Description : %s", description));
        TestLogger.logTestEvent(testClassName, String.format("Start Time  : %s", timestamp));
        
        long startTime = System.currentTimeMillis();
        try {
            testCode.execute();
            long duration = System.currentTimeMillis() - startTime;
            String endTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            
            System.out.println("[" + timestamp + "] PASSED: " + testName);
            TestLogger.recordTestResult(testClassName, testName, true);
            TestLogger.logTestEvent(testClassName, String.format("End Time    : %s", endTimestamp));
            TestLogger.logTestEvent(testClassName, String.format("Duration    : %d ms", duration));
            TestLogger.logTestEvent(testClassName, "Result      : ✓ PASSED");
        } catch (AssertionError e) {
            long duration = System.currentTimeMillis() - startTime;
            String endTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            
            System.out.println("[" + timestamp + "] FAILED: " + testName);
            System.out.println("    Error: " + e.getMessage());
            TestLogger.recordTestResult(testClassName, testName, false);
            TestLogger.logTestEvent(testClassName, String.format("End Time    : %s", endTimestamp));
            TestLogger.logTestEvent(testClassName, String.format("Duration    : %d ms", duration));
            TestLogger.logTestEvent(testClassName, "Result      : ✗ FAILED");
            TestLogger.logTestEvent(testClassName, String.format("Error       : %s", e.getMessage()));
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String endTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            
            System.out.println("[" + timestamp + "] ERROR: " + testName);
            System.out.println("    Error: " + e.getMessage());
            TestLogger.recordTestResult(testClassName, testName, false);
            TestLogger.logTestEvent(testClassName, String.format("End Time    : %s", endTimestamp));
            TestLogger.logTestEvent(testClassName, String.format("Duration    : %d ms", duration));
            TestLogger.logTestEvent(testClassName, "Result      : ✗ ERROR");
            TestLogger.logTestEvent(testClassName, String.format("Error       : %s", e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            try {
                if (sleepMs > 0) {
                    Thread.sleep(sleepMs);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            TestLogger.logTestEvent(testClassName, "");
        }
    }

    /**
     * Convenience method that uses default sleep of 1000ms
     */
    public static void executeTest(String testName, String description, TestExecutable testCode, String testClassName) {
        executeTest(testName, description, testCode, 1000, testClassName);
    }
    
    /**
     * Convenience method with default sleep of 1000ms and no test class name
     * @deprecated Use executeTest with testClassName parameter instead
     */
    @Deprecated
    public static void executeTest(String testName, String description, TestExecutable testCode) {
        executeTest(testName, description, testCode, 1000, "Unknown");
    }
    
    /**
     * Writes the accumulated test results to a log file
     */
    public static void writeTestLog(String testClassName) {
        TestLogger.writeLog(testClassName);
    }

    /**
     * Functional interface for test execution
     */
    @FunctionalInterface
    public interface TestExecutable {
        void execute() throws Exception;
    }

    public static void main(String[] args) {
        System.out.println("Memulai pengecekan ringan ke " + TARGET_URL);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TARGET_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            System.out.printf("Status HTTP: %d%n", response.statusCode());
        } catch (IOException | InterruptedException ex) {
            System.err.println("Gagal menghubungi situs: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
