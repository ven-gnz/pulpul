package pulp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pulp.TokenType.*;

public class PScanner {


    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("constant", CONSTANT);
        keywords.put("let", LET);
        keywords.put("be", BE);
        keywords.put("set", SET);
        keywords.put("not", NOT);
        keywords.put("and", AND);

        keywords.put("invoke", INVOKE);
        keywords.put("with", WITH);
        keywords.put("should", SHOULD);
        keywords.put("then", THEN);
        keywords.put("otherwise", OTHERWISE);

        keywords.put("add", ADD);
        keywords.put("to", TO);
        keywords.put("remove", REMOVE);
        keywords.put("from", FROM);
        keywords.put("divide", DIVIDE);
        keywords.put("by", BY);
        keywords.put("multiply", MULTIPLY);

        keywords.put("equal", EQUAL);
        keywords.put("or", OR);
        keywords.put("more", MORE);
        keywords.put("less", LESS);
        keywords.put("is", IS);
        keywords.put("than", THAN);

        keywords.put("display", DISPLAY);
        keywords.put("result", RESULT);

        keywords.put("input", INPUT);
        keywords.put("user", USER);
        keywords.put("repeat", REPEAT);
        keywords.put("until", UNTIL);

        keywords.put("description", DESCRIPTION);
        keywords.put("of", OF);
        keywords.put("subprogram", SUBPROGRAM);
        keywords.put("called", CALLED);

        keywords.put("acting", ACTING);
        keywords.put("on", ON);
        keywords.put("inputs", INPUTS);
        keywords.put("producing", PRODUCING);
        keywords.put("outputs", OUTPUTS);

        keywords.put("boolean", TYPE_BOOLEAN);
        keywords.put("string", TYPE_STRING);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);

        keywords.put("number", NUMBER);
        keywords.put("real", REAL);
        keywords.put("whole", WHOLE);



    }

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

    private void identifier()
    {
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current).toLowerCase();
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }


    private boolean isAlpha(char c)
    {
        return  (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_';
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private void scanToken() {

        char c = advance();
        switch(c)
        {
            case ',': addToken(TokenType.COMMA); break;
            case ':': addToken(TokenType.COLON); break;
            case '.':addToken(TokenType.DOT); break;
            case '-':addToken(TokenType.MINUS); break;
            case '"':string(); break;

            default:
                if(isDigit(c))
                {
                    number();
                }
                else if(isAlpha(c))
                {
                    identifier();
                }
        }
    }

    private char peekNext()
    {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number()
    {
        while(isDigit(peek())) { advance(); }

        if(peek() == '.' && isDigit(peekNext()))
        {
            advance();
            while(isDigit(peek())) advance();
        }
            addToken(NUMBER_LITERAL, Double.parseDouble(source.substring(start, current)));
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
