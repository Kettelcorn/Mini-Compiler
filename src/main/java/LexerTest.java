import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void outputToFile() throws IOException {
        Map<String, String> outputPaths = new HashMap<>();
        outputPaths.put("src/main/resources/99bottles.lex", "SolutionFiles/99bottles.lex");
        outputPaths.put("src/main/resources/fizzbuzz.lex", "SolutionFiles/fizzbuzz.lex");
        outputPaths.put("src/main/resources/prime.lex", "SolutionFiles/prime.lex");

        boolean match = true;
        String errorMessage = "";
        for (Map.Entry<String, String> entry : outputPaths.entrySet()) {
            File actualFile = new File(entry.getKey());
            File expectedFile = new File(entry.getValue());
            Scanner actualScanner = new Scanner(actualFile);
            Scanner expectedScanner = new Scanner(expectedFile);
            int lineNumber = 1;
            while (actualScanner.hasNext() || expectedScanner.hasNext()) {
                String actual = actualScanner.next();
                String expected = expectedScanner.next();
                if (!actual.equals(expected)) {
                    match = false;
                    errorMessage = "Output: " + actual + ", Expected: " + expected + " in "
                            + entry.getKey() + " on line " + lineNumber;
                    break;
                }
                lineNumber++;
            }
            if (!match) {
                break;
            }
        }
        assertTrue(match, errorMessage);
    }
}