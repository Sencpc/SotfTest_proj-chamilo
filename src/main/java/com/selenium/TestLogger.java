package com.selenium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for logging test results to files.
 * Creates/updates log files with format: "dd/MM/yyyy - TestClassName - [passed tests : failed tests]"
 */
public class TestLogger {
    
    private static final String LOG_FOLDER = "Log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String FILE_EXTENSION = ".txt";
    
    // Stores test results per test class
    private static Map<String, TestResults> testResultsMap = new HashMap<>();

    /**
     * Appends a log line for a given test class
     */
    public static void logTestEvent(String testClassName, String line) {
        TestResults results = testResultsMap.computeIfAbsent(testClassName, k -> new TestResults());
        results.appendLog(line);
    }
    
    /**
     * Records a test result (pass/fail)
     */
    public static void recordTestResult(String testClassName, String testName, boolean passed) {
        TestResults results = testResultsMap.computeIfAbsent(testClassName, k -> new TestResults());
        if (passed) {
            results.incrementPassed(testName);
        } else {
            results.incrementFailed(testName);
        }
    }
    
    /**
     * Writes the test results to a log file
     */
    public static void writeLog(String testClassName) {
        TestResults results = testResultsMap.get(testClassName);
        if (results == null) {
            return;
        }
        
        try {
            // Get project root directory
            Path projectRoot = getProjectRoot();
            
            // Create Log folder if it doesn't exist
            Path logFolder = projectRoot.resolve(LOG_FOLDER);
            Files.createDirectories(logFolder);
            
            // Generate log file name with current date and test class name
            String fileDate = LocalDate.now().format(FILE_DATE_FORMAT);
            String currentDate = LocalDate.now().format(DATE_FORMAT);
            String logFileName = String.format("%s - %s - [%d-%d]%s", fileDate, testClassName, results.getPassed(), results.getFailed(), FILE_EXTENSION);
            Path logFilePath = logFolder.resolve(logFileName);

            // Build human-readable log content
            StringBuilder sb = new StringBuilder();
            
            // Header section
            sb.append("================================================================================\n");
            sb.append("                         TEST EXECUTION REPORT\n");
            sb.append("================================================================================\n\n");
            sb.append(String.format("Test Suite    : %s%n", testClassName));
            sb.append(String.format("Execution Date: %s%n", currentDate));
            sb.append(String.format("Total Tests   : %d%n", results.getPassed() + results.getFailed()));
            sb.append(String.format("Passed        : %d%n", results.getPassed()));
            sb.append(String.format("Failed        : %d%n", results.getFailed()));
            
            String status = results.getFailed() == 0 ? "✓ ALL TESTS PASSED" : "✗ SOME TESTS FAILED";
            sb.append(String.format("Status        : %s%n%n", status));
            
            sb.append("================================================================================\n");
            sb.append("                           DETAILED TEST RESULTS\n");
            sb.append("================================================================================\n\n");
            
            // Test details
            for (String line : results.getLogLines()) {
                sb.append(line).append(System.lineSeparator());
            }
            
            // Footer summary
            sb.append("\n================================================================================\n");
            sb.append("                              END OF REPORT\n");
            sb.append("================================================================================\n");
            sb.append(String.format("Summary: %d test(s) passed, %d test(s) failed%n", results.getPassed(), results.getFailed()));
            if (results.getFailed() == 0) {
                sb.append("Result: ✓ All tests completed successfully!\n");
            } else {
                sb.append("Result: ✗ Please review failed tests above.\n");
            }

            // Write or update the log file
            Files.write(logFilePath, sb.toString().getBytes());
            System.out.println("\n[LOG] Test results saved to: " + logFilePath.toAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Failed to write log file: " + e.getMessage());
        }
    }
    
    /**
     * Gets the project root directory by finding pom.xml
     */
    private static Path getProjectRoot() {
        Path currentPath = Paths.get("").toAbsolutePath();
        
        // Search up the directory tree for pom.xml
        while (currentPath != null) {
            Path pomFile = currentPath.resolve("pom.xml");
            if (Files.exists(pomFile)) {
                return currentPath;
            }
            currentPath = currentPath.getParent();
        }
        
        // Fallback to current working directory
        return Paths.get("").toAbsolutePath();
    }
    
    /**
     * Resets the test results for a specific test class
     */
    public static void resetResults(String testClassName) {
        testResultsMap.remove(testClassName);
    }
    
    /**
     * Inner class to store test results
     */
    private static class TestResults {
        private int passedCount = 0;
        private int failedCount = 0;
        private Map<String, Boolean> results = new HashMap<>();
        private final java.util.List<String> logLines = new java.util.ArrayList<>();
        
        void incrementPassed(String testName) {
            if (!results.containsKey(testName)) {
                results.put(testName, true);
                passedCount++;
            }
        }
        
        void incrementFailed(String testName) {
            if (!results.containsKey(testName)) {
                results.put(testName, false);
                failedCount++;
            }
        }
        
        int getPassed() {
            return passedCount;
        }
        
        int getFailed() {
            return failedCount;
        }

        void appendLog(String line) {
            logLines.add(line);
        }

        java.util.List<String> getLogLines() {
            return logLines;
        }
    }
}
