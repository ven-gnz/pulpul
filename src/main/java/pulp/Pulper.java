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


        for (Token token : tokens) System.out.println(token.type + " " + token.lexeme);


        Parser parser = new Parser(tokens);
        List<Stmt> statements = new ArrayList<>();
        try {
           statements = parser.parse();
        }catch (RuntimeException parserRunTimeException)
        {
            printParseErrors(parser.diagnostics);
        }
        printParseErrors(parser.diagnostics);


        if(hadError)
        {
            System.exit(60);
        }

        PrettyPrinter prettyPrinter = new PrettyPrinter();
        prettyPrinter.print(statements);

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if(hadError)
        {
            System.exit(61);
        }


        TypeChecker typeChecker = new TypeChecker(statements, resolver);
        typeChecker.check();
        if(hadError)
        {
            hadError = false;
            System.exit(65);
        }
        interpreter.setResolver(resolver);
        interpreter.interpret(statements);
        if(hadRuntimeError) System.exit(70);

    }

    private static void printParseErrors(List<ErrorDiagnostic> diagnostics)
    {
        if(diagnostics == null || diagnostics.isEmpty()) return;
        for(ErrorDiagnostic d : diagnostics)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Parser error -m ");
            sb.append(d.message());

            sb.append("at'").append(d.lexeme()).append("'");

            sb.append("[line ").append(d.line()).append("]");
            System.err.println(sb.toString());
        }
        hadError = true;
    }

    static void error(int line, String message)
    {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error)
    {
        String location = "";
        if(error.token != null)
        {
            location = "[line " + error.token.line + "] ";
        }
        System.err.println("Runtime error: " + error.getMessage() + location);
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message)
    {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError=true;
    }

    public static void typeError(Token token, String message, Stmt.Program program, Stmt.Subprogram subprogram)
    {
        System.err.println(formatTypeError(token,message,program,subprogram));
        hadError = true;
    }

    private static String formatTypeError(
            Token token,
            String message,
            Stmt.Program program,
            Stmt.Subprogram subprogram)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("TypeError: ");
        if (program != null) {
            sb.append(" program(")
                    .append(program.name.lexeme).append(")");
        }

        if (subprogram != null) {
            sb.append(" in subprogram(")
                    .append(subprogram.name.lexeme).append(") -");
        }
        sb.append("\n");
        sb.append(message);

        if (token != null) {
            sb.append(" [line ").append(token.line).append("]");
        }

        return sb.toString();
    }

    /**
     *
     * @param token error generating token on expression parsing
     * @param message additional message
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void error(String message)
    {
        System.err.println(message);
    }
}