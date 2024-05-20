import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    /**
     * Test to see if parser output matches the expected output
     * @throws IOException
     */
    @Test
    void outputToFile() throws IOException {
        Map<String, String> outputPaths = new HashMap<>();
        outputPaths.put("src/main/resources/99bottles.par", "SolutionFiles/99bottles.par");
        outputPaths.put("src/main/resources/fizzbuzz.par", "SolutionFiles/fizzbuzz.par");
        outputPaths.put("src/main/resources/prime.par", "SolutionFiles/prime.par");

        for (Map.Entry<String, String> entry : outputPaths.entrySet()) {
            File actualFile = new File(entry.getKey());
            File expectedFile = new File(entry.getValue());
            Scanner actualScanner = new Scanner(actualFile);
            Scanner expectedScanner = new Scanner(expectedFile);
            boolean match = true;
            String errorMessage = "";
            int lineNumber = 1;
            while (actualScanner.hasNextLine() || expectedScanner.hasNextLine()) {
                String actual = actualScanner.nextLine();
                String expected = expectedScanner.nextLine();
                if (!actual.equals(expected)) {
                    match = false;
                    errorMessage = "Output: " + actual + ", Expected: " + expected + " in "
                    + entry.getKey() + " on line " + lineNumber;
                    break;
                }
                lineNumber++;
            }
            assertTrue(match, errorMessage);
        }

    }
}