package pulp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Files;


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

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
//> exit-code

        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
//< exit-code
//> Evaluating Expressions check-runtime-error
        if (hadRuntimeError) System.exit(70);
//< Evaluating Expressions check-runtime-error
    }
    //< run-file
//> prompt
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) { // [repl]
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
//> reset-had-error
            hadError = false;
//< reset-had-error
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
        List<Stmt> statements = parser.parse();

        if(hadError)
        {
            hadError = false;
            System.exit(65);
        }
        if(hadRuntimeError) System.exit(70);

        interpreter.interpret(statements);
    }

    static void error(int line, String message)
    {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error)
    {
        if(error.token != null)
        {
            System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        }
        hadRuntimeError = false;
    }

    private static void report(int line, String where, String message)
    {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError=true;
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