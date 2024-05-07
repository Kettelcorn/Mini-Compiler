import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class Parser {
    private List<Token> source;
    private Token token;
    private int position;

    static class Node {
        public NodeType nt;
        public Node left, right;
        public String value;

        Node() {
            this.nt = null;
            this.left = null;
            this.right = null;
            this.value = null;
        }
        Node(NodeType node_type, Node left, Node right, String value) {
            this.nt = node_type;
            this.left = left;
            this.right = right;
            this.value = value;
        }
        public static Node make_node(NodeType nodetype, Node left, Node right) {
            return new Node(nodetype, left, right, "");
        }
        public static Node make_node(NodeType nodetype, Node left) {
            return new Node(nodetype, left, null, "");
        }
        public static Node make_leaf(NodeType nodetype, String value) {
            return new Node(nodetype, null, null, value);
        }
    }

    static class Token {
        public TokenType tokentype;
        public String value;
        public int line;
        public int pos;

        Token(TokenType token, String value, int line, int pos) {
            this.tokentype = token; this.value = value; this.line = line; this.pos = pos;
        }
        @Override
        public String toString() {
            return String.format("%5d  %5d %-15s %s", this.line, this.pos, this.tokentype, this.value);
        }
    }

    static enum TokenType {
        End_of_input(false, false, false, -1, NodeType.nd_None),
        Op_multiply(false, true, false, 13, NodeType.nd_Mul),
        Op_divide(false, true, false, 13, NodeType.nd_Div),
        Op_mod(false, true, false, 13, NodeType.nd_Mod),
        Op_add(false, true, false, 12, NodeType.nd_Add),
        Op_subtract(false, true, false, 12, NodeType.nd_Sub),
        Op_negate(false, false, true, 14, NodeType.nd_Negate),
        Op_not(false, false, true, 14, NodeType.nd_Not),
        Op_less(false, true, false, 10, NodeType.nd_Lss),
        Op_lessequal(false, true, false, 10, NodeType.nd_Leq),
        Op_greater(false, true, false, 10, NodeType.nd_Gtr),
        Op_greaterequal(false, true, false, 10, NodeType.nd_Geq),
        Op_equal(false, true, true, 9, NodeType.nd_Eql),
        Op_notequal(false, true, false, 9, NodeType.nd_Neq),
        Op_assign(false, false, false, -1, NodeType.nd_Assign),
        Op_and(false, true, false, 5, NodeType.nd_And),
        Op_or(false, true, false, 4, NodeType.nd_Or),
        Keyword_if(false, false, false, -1, NodeType.nd_If),
        Keyword_else(false, false, false, -1, NodeType.nd_None),
        Keyword_while(false, false, false, -1, NodeType.nd_While),
        Keyword_print(false, false, false, -1, NodeType.nd_None),
        Keyword_putc(false, false, false, -1, NodeType.nd_None),
        LeftParen(false, false, false, -1, NodeType.nd_None),
        RightParen(false, false, false, -1, NodeType.nd_None),
        LeftBrace(false, false, false, -1, NodeType.nd_None),
        RightBrace(false, false, false, -1, NodeType.nd_None),
        Semicolon(false, false, false, -1, NodeType.nd_None),
        Comma(false, false, false, -1, NodeType.nd_None),
        Identifier(false, false, false, -1, NodeType.nd_Ident),
        Integer(false, false, false, -1, NodeType.nd_Integer),
        String(false, false, false, -1, NodeType.nd_String);

        private final int precedence;
        private final boolean right_assoc;
        private final boolean is_binary;
        private final boolean is_unary;
        private final NodeType node_type;

        TokenType(boolean right_assoc, boolean is_binary, boolean is_unary, int precedence, NodeType node) {
            this.right_assoc = right_assoc;
            this.is_binary = is_binary;
            this.is_unary = is_unary;
            this.precedence = precedence;
            this.node_type = node;
        }
        boolean isRightAssoc() { return this.right_assoc; }
        boolean isBinary() { return this.is_binary; }
        boolean isUnary() { return this.is_unary; }
        int getPrecedence() { return this.precedence; }
        NodeType getNodeType() { return this.node_type; }
    }
    static enum NodeType {
        nd_None(""), nd_Ident("Identifier"), nd_String("String"), nd_Integer("Integer"), nd_Sequence("Sequence"), nd_If("If"),
        nd_Prtc("Prtc"), nd_Prts("Prts"), nd_Prti("Prti"), nd_While("While"),
        nd_Assign("Assign"), nd_Negate("Negate"), nd_Not("Not"), nd_Mul("Multiply"), nd_Div("Divide"), nd_Mod("Mod"), nd_Add("Add"),
        nd_Sub("Subtract"), nd_Lss("Less"), nd_Leq("LessEqual"),
        nd_Gtr("Greater"), nd_Geq("GreaterEqual"), nd_Eql("Equal"), nd_Neq("NotEqual"), nd_And("And"), nd_Or("Or");

        private final String name;

        NodeType(String name) {
            this.name = name;
        }

        @Override
        public String toString() { return this.name; }
    }

    /**
     * Prints an error message to the standard output and terminates the program.
     * @param line the line number where the error occurred.
     * @param pos the position in the line where the error occurred.
     * @param msg the error message.
     */
    static void error(int line, int pos, String msg) {
        if (line > 0 && pos > 0) {
            System.out.printf("%s in line %d, pos %d\n", msg, line, pos);
        } else {
            System.out.println(msg);
        }
        System.exit(1);
    }
    Parser(List<Token> source) {
        this.source = source;
        this.token = null;
        this.position = 0;
    }

    /**
     * Retrieves the next token from the source list.
     * @return the next token.
     */
    Token getNextToken() {
        this.token = this.source.get(this.position++);
        return this.token;
    }

    /**
     * Parses an expression based on the precedence.
     * @param p the precedence level.
     * @return the parsed node.
     */
    Node expr(int p) {
        Node node = primary();
        while (this.token.tokentype.isBinary() && this.token.tokentype.precedence > p) {
            TokenType operator = this.token.tokentype;
            getNextToken();
            Node rightNode = expr(operator.getPrecedence());
            node = Node.make_node(operator.getNodeType(), node, rightNode);
        }
        return node;
    }

    /**
     * Handles parsing of primary expressions.
     * @return the node representing the primary expression.
     */
    Node primary() {
        if (this.token.tokentype == TokenType.Integer) {
            Node node = Node.make_leaf(NodeType.nd_Integer, this.token.value);
            getNextToken();
            return node;
        } else if (this.token.tokentype == TokenType.Identifier) {
            Node node = Node.make_leaf(NodeType.nd_Ident, this.token.value);
            getNextToken();
            return node;
        } else if (this.token.tokentype == TokenType.LeftParen) {
            paren_expr();
        } else if (this.token.tokentype.isUnary() && this.token.tokentype != TokenType.Op_equal){
                NodeType unary = this.token.tokentype.getNodeType();
                getNextToken();
                return Node.make_node(unary, primary(), null);
        } else {
            error(this.token.line, this.token.pos, "Expecting primary token, cannot use " + this.token.tokentype + ".");
        }
        return null;
    }

    /**
     * Parses and handles parentheses expressions.
     * @return the node representing the expression within parentheses.
     */
    Node paren_expr() {
        expect("paren_expr", TokenType.LeftParen);
        Node node = expr(0);
        expect("paren_expr", TokenType.RightParen);
        return node;
    }

    /**
     * Ensures the next token is the expected one.
     * @param msg the message to be shown in case of an error.
     * @param s the expected token type.
     */
    void expect(String msg, TokenType s) {
        if (this.token.tokentype == s) {
            getNextToken();
            return;
        }
        error(this.token.line, this.token.pos, msg + ": Expecting '" + s + "', found: '" + this.token.tokentype + "'");
    }

    /**
     * Parses a statement.
     * @return the node representing the parsed statement.
     */
    Node stmt() {
        if (this.token.tokentype == TokenType.Identifier) {
            Node leftNode = Node.make_leaf(this.token.tokentype.getNodeType(), this.token.value);
            getNextToken();
            expect("Assign", TokenType.Op_assign);
            Node node = Node.make_node(NodeType.nd_Assign, leftNode, expr(0));
            expect("Semicolon", TokenType.Semicolon);
            return node;
        } else if (this.token.tokentype == TokenType.Keyword_while) {
            getNextToken();
            return Node.make_node(NodeType.nd_While, paren_expr(), stmt());
        } else if (this.token.tokentype == TokenType.Keyword_if) {
             getNextToken();
             Node ifNode = Node.make_node(NodeType.nd_If, null, null);
             Node parenExpr = paren_expr();
             Node ifTrue = stmt();
             if (this.token.tokentype == TokenType.Keyword_else) {
                 getNextToken();
                 Node ifFalse = stmt();
                 return Node.make_node(NodeType.nd_If, parenExpr, Node.make_node(NodeType.nd_If, ifTrue, ifFalse));
             } else {
                getNextToken();
                return Node.make_node(NodeType.nd_If, parenExpr, Node.make_node(NodeType.nd_If, ifTrue, null));
             }
        } else if (this.token.tokentype == TokenType.Keyword_print) {
            return printNode();
        } else if (this.token.tokentype == TokenType.Keyword_putc) {
            return Node.make_node(NodeType.nd_Prtc, paren_expr(), null);
        } else if (this.token.tokentype == TokenType.LeftBrace) {
            Node node = null;
            getNextToken();
            while (this.token.tokentype != TokenType.RightBrace) {
                node = Node.make_node(NodeType.nd_Sequence, node, stmt());
            }
            getNextToken();
            return node;
        } else {
            error(this.token.line, this.token.pos, "Expecting statement, found: " + this.token + ".");
        }
        return null;
    }

    /**
     * Handles the parsing of print statements.
     * @return the node representing the print sequence.
     */
    Node printNode() {
        Node node = null;
        Node temp = null;
        getNextToken();
        expect("LeftParen", TokenType.LeftParen);
        while (this.token.tokentype != TokenType.RightParen) {
            if (this.token.tokentype == TokenType.String) {
                temp = Node.make_node(NodeType.nd_Prts, Node.make_leaf(NodeType.nd_String, this.token.value));
            } else if (this.token.tokentype == TokenType.Integer) {
                temp = Node.make_node(NodeType.nd_Prti, Node.make_leaf(NodeType.nd_Integer, this.token.value));
            } else {
                temp = Node.make_node(NodeType.nd_Prtc, expr(0));
            }
            node = Node.make_node(NodeType.nd_Sequence, node, temp);
            getNextToken();
            if (this.token.tokentype == TokenType.Comma) {
                getNextToken();
            }
        }
        getNextToken();
        expect("Semicolon", TokenType.Semicolon);
        return node;
    }

    /**
     * Parses the entire input source into an AST.
     * @return the root node of the AST.
     */
    Node parse() {
        Node t = null;
        getNextToken();
        while (this.token.tokentype != TokenType.End_of_input) {
            t = Node.make_node(NodeType.nd_Sequence, t, stmt());
        }
        return t;
    }

    /**
     * Converts the AST into a string representation and outputs it to the console.
     * @param t the root node of the AST.
     * @param sb the StringBuilder to append the string representation.
     * @return the string representation of the AST.
     */
    String printAST(Node t, StringBuilder sb) {
        int i = 0;
        if (t == null) {
            sb.append(";");
            sb.append("\n");
            System.out.println(";");
        } else {
            sb.append(t.nt);
            System.out.printf("%-14s", t.nt);
            if (t.nt == NodeType.nd_Ident || t.nt == NodeType.nd_Integer || t.nt == NodeType.nd_String) {
                sb.append(" " + t.value);
                sb.append("\n");
                System.out.println(" " + t.value);
            } else {
                sb.append("\n");
                System.out.println();
                printAST(t.left, sb);
                printAST(t.right, sb);
            }

        }
        return sb.toString();
    }

    /**
     * Writes the result of AST processing to a file.
     * @param result the string representation of the AST to be written to the file.
     */
    static void outputToFile(String result) {
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/99bottles.par");
            myWriter.write(result);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The main method that sets up and runs the parser.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        if (1==1) {
            try {
                String value, token;
                String result = " ";
                StringBuilder sb = new StringBuilder();
                int line, pos;
                Token t;
                boolean found;
                List<Token> list = new ArrayList<>();
                Map<String, TokenType> str_to_tokens = new HashMap<>();

                str_to_tokens.put("End_of_input", TokenType.End_of_input);
                str_to_tokens.put("Op_multiply", TokenType.Op_multiply);
                str_to_tokens.put("Op_divide", TokenType.Op_divide);
                str_to_tokens.put("Op_mod", TokenType.Op_mod);
                str_to_tokens.put("Op_add", TokenType.Op_add);
                str_to_tokens.put("Op_subtract", TokenType.Op_subtract);
                str_to_tokens.put("Op_negate", TokenType.Op_negate);
                str_to_tokens.put("Op_not", TokenType.Op_not);
                str_to_tokens.put("Op_less", TokenType.Op_less);
                str_to_tokens.put("Op_lessequal", TokenType.Op_lessequal);
                str_to_tokens.put("Op_greater", TokenType.Op_greater);
                str_to_tokens.put("Op_greaterequal", TokenType.Op_greaterequal);
                str_to_tokens.put("Op_equal", TokenType.Op_equal);
                str_to_tokens.put("Op_notequalt", TokenType.Op_notequal);
                str_to_tokens.put("Op_assign", TokenType.Op_assign);
                str_to_tokens.put("Op_and", TokenType.Op_and);
                str_to_tokens.put("Op_or", TokenType.Op_or);
                str_to_tokens.put("Keyword_if", TokenType.Keyword_if);
                str_to_tokens.put("Keyword_else", TokenType.Keyword_else);
                str_to_tokens.put("Keyword_while", TokenType.Keyword_while);
                str_to_tokens.put("Keyword_print", TokenType.Keyword_print);
                str_to_tokens.put("Keyword_putc", TokenType.Keyword_putc);
                str_to_tokens.put("LeftParen", TokenType.LeftParen);
                str_to_tokens.put("RightParen", TokenType.RightParen);
                str_to_tokens.put("LeftBrace", TokenType.LeftBrace);
                str_to_tokens.put("RightBrace", TokenType.RightBrace);
                str_to_tokens.put("Semicolon", TokenType.Semicolon);
                str_to_tokens.put("Comma", TokenType.Comma);
                str_to_tokens.put("Identifier", TokenType.Identifier);
                str_to_tokens.put("Integer", TokenType.Integer);
                str_to_tokens.put("String", TokenType.String);

                Scanner s = new Scanner(new File("src/main/resources/99bottles.lex"));
                String source = " ";
                while (s.hasNext()) {
                    String str = s.nextLine();
                    StringTokenizer st = new StringTokenizer(str);
                    line = Integer.parseInt(st.nextToken());
                    pos = Integer.parseInt(st.nextToken());
                    token = st.nextToken();
                    value = "";
                    while (st.hasMoreTokens()) {
                        value += st.nextToken() + " ";
                    }
                    found = false;
                    if (str_to_tokens.containsKey(token)) {
                        found = true;
                        list.add(new Token(str_to_tokens.get(token), value, line, pos));
                    }
                    if (found == false) {
                        throw new Exception("Token not found: '" + token + "'");
                    }
                }
                Parser p = new Parser(list);
                result = p.printAST(p.parse(), sb);
                outputToFile(result);
            } catch (FileNotFoundException e) {
                error(-1, -1, "Exception: " + e.getMessage());
            } catch (Exception e) {
                error(-1, -1, "Exception: " + e.getMessage());
            }
        } else {
            error(-1, -1, "No args");
        }
    }
}