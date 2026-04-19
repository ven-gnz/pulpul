package pulp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class Pulper {

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException
    {
        if(args.length > 1)
        {
            System.out.println("Usage : Pulper [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
    }

    private static void runFile(String arg) {

    }


    private static void runPrompt() throws IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;)
        {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null) break;
            run(line);
        }
    }

    private static void run(String source)
    {
        PScanner scanner = new PScanner(source);
        List<Token> tokens = scanner.scanTokens();



        for (Token token : tokens) {
            System.out.println(token.type + " " + token.lexeme);
        }



        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if(hadError)
        {
            hadError = false;
            System.exit(65);
        }
        if(hadRuntimeError) System.exit(70);
        AstPrinter p = new AstPrinter();
        System.out.println(p.print(expression));

        interpreter.interpret(expression);
    }

    static void error(int line, String message)
    {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error)
    {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = false;
    }

    private static void report(int line, String where, String message)
    {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError=true;;
    }

    /**
     *
     * @param token error generating token on expression parsing
     * @param message additional message such as line
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}