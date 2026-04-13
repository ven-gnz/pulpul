package org.pulp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class Pulper {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException
    {
        if(args.length > 1)
        {
            System.out.println("Usage : Pupler [script]");
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

        for(Token token : tokens)
        {
            System.out.println(token);
        }
        if(hadError)
        {
            hadError = false;
            System.exit(65);
        }
    }

    static void error(int line, String message)
    {
        report(line, "", message);
    }

    private static void report(int line, String where, String message)
    {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError=true;;
    }
}