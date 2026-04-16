package pulp;

import java.util.List;


import static pulp.Pulper.error;
import static pulp.TokenType.*;

class Parser {

    private static class ParseError extends RuntimeException{};


    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Expr expression()
    {
        if(check(NOT)) return notExpression();

        
    }

    private Expr notExpression()
    {
        consume(NOT, "Except 'not'");
        Expr right = expression();
        return new Expr.Not(right);
    }





    private ComparisonType parseComparisonCriteria()
    {
        if(match(EQUAL))
        {
            consume(TO, "Except identifier");
            return ComparisonType.EQUAL;
        }


        // default case : delete
        return ComparisonType.GREATER_EQUAL;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String msg)
    {
        if(check(type)) return advance();
        throw error(peek(), msg);
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }


    private boolean check(TokenType type)
    {
        if (!isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd()
    {
        return peek().type == EOF;
    }

    private Token peek()
    {
        return tokens.get(current);
    }

    private Token previous()
    {
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String msg)
    {
        Pulper.error(token, msg);
        return new ParseError();
    }
}


enum ComparisonType
{
    EQUAL, NOT_EQUAL, LESS_EQUAL, LESS, GREATER_EQUAL, GREATER;
}