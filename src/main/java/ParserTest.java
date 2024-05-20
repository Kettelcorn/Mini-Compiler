import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
                if (!match) {
                    break;
                }
            }
            assertTrue(match, errorMessage);
        }
    }

    /**
     * Test to see if the constructor works as expected
     */
    @Test
    void contstuctorTest() {
        List<Parser.Token> tokens = new ArrayList<>();
        tokens.add(new Parser.Token(Parser.TokenType.String, "Hello", 1, 1));
        tokens.add(new Parser.Token(Parser.TokenType.Integer, "5", 1, 5));
        tokens.add(new Parser.Token(Parser.TokenType.String, "There", 1, 6));
        Parser parser = new Parser(tokens);

        assertEquals(tokens.get(0), parser.getNextToken());
        assertEquals(tokens.get(1), parser.getNextToken());
        assertEquals(tokens.get(2), parser.getNextToken());
    }

    /**
     * Test to see if the parser can parse a simple program
     */
    @Test
    void parseTest() {
        List<Parser.Token> tokens = new ArrayList<>();
        tokens.add(new Parser.Token(Parser.TokenType.Identifier, "Hello", 1, 1));
        tokens.add(new Parser.Token(Parser.TokenType.Op_assign, null, 1, 5));
        tokens.add(new Parser.Token(Parser.TokenType.Integer, "5", 1, 7));
        tokens.add(new Parser.Token(Parser.TokenType.Semicolon, null, 1, 8));
        tokens.add(new Parser.Token(Parser.TokenType.End_of_input, null, 2, 1));
        Parser parser = new Parser(tokens);

        Parser.Node node = new Parser.Node(Parser.NodeType.nd_Ident, null, null, "Hello");
        Parser.Node node2 = new Parser.Node(Parser.NodeType.nd_Integer, null, null, "5");
        Parser.Node node3 = new Parser.Node(Parser.NodeType.nd_Assign, node, node2, null);
        Parser.Node node4 = new Parser.Node(Parser.NodeType.nd_Sequence, null, node3, null);
        Parser.Node output = parser.parse();

        assertEquals(node4.nt, output.nt);
        assertEquals(node4.right.nt, output.right.nt);
        assertEquals(node4.right.left.nt, output.right.left.nt);
        assertEquals(node4.right.left.value, output.right.left.value);
        assertEquals(node4.right.right.nt, output.right.right.nt);
        assertEquals(node4.right.right.value, output.right.right.value);
    }
}