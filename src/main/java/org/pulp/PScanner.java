package org.pulp;

import java.util.ArrayList;
import java.util.List;

public class PScanner {


    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public PScanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens()
    {
        while (!isAtEnd())
        {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "",null, line));
        return tokens;
    }

    private char advance()
    {
        return source.charAt(current++);
    }

    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = source.substring(start,current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char peek()
    {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void scanToken() {

        char c = advance();
        switch(c)
        {
            case ',': addToken(TokenType.COMMA); break;
            case ':': addToken(TokenType.COLON); break;
            case '.':addToken(TokenType.DOT); break;
            case '"':string(); break;
        }
    }

    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number()
    {
        while()
    }

    private void string()
    {
        while(peek() != '"' && !isAtEnd())
        {
            if(peek() == '\n') { line++; }
            advance();
        }

        if(isAtEnd())
        {
            Pulper.error(line, "Unterminating string literal !");
            return;
        }
        //closing quotation mark
        advance();
        String value = source.substring(start+1, current-1);
        addToken(TokenType.STRING_LITERAL, value);
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }
}
