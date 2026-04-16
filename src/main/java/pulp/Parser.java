package pulp;

import java.util.List;


import static pulp.Pulper.error;
import static pulp.TokenType.*;
import pulp.ComparisonType;

class Parser {

    private static class ParseError extends RuntimeException{};


    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Expr parse()
    {
        try{
            return expression();
        } catch (ParseError pe)
        {
            return null;
        }
    }

    private Expr expression()
    {
        if(match(NOT)) return notExpression();
        if(check(LITERAL_TRUE) || check(LITERAL_FALSE)){ return booleanLiteral(); }

        Expr expr = arithmeticExpression();

        if(match(BE)) {
            return comparisonExpression(expr);
        }
        return expr;
    }

    private Expr booleanLiteral()
    {
        return new Expr.Literal(previous().literal);
    }

    private Expr arithmeticExpression()
    {

        if(match(ADD)) return addExpression();
        if(match(REMOVE)) return subtractExpression();
        if(match(MULTIPLY)) return multiplyExpression();
        if(match(DIVIDE)) return divideExpression();

        return arithmeticPrimary();
    }

    private Expr subtractExpression()
    {
        Expr left = arithmeticExpression();
        consume(FROM, "Except 'from' after left operand");
        Expr right = arithmeticExpression();
        return new Expr.Remove(left, right);
    }

    private Expr multiplyExpression()
    {
        Expr left = arithmeticExpression();
        consume(BY, "Except 'by' after left operand");
        Expr right = arithmeticExpression();
        return new Expr.Multiply(left, right);
    }

    private Expr addExpression()
    {
        Expr left  = arithmeticExpression();
        consume(TO, "Except 'to' after left operand");
        Expr right = arithmeticExpression();
        return new Expr.Add(left,right);
    }

    private Expr divideExpression()
    {
        Expr left = arithmeticExpression();
        consume(BY, "Except 'by' after left operand");
        Expr right = arithmeticExpression();
        return new Expr.Divide(left,right);
    }

    private Expr arithmeticPrimary()
    {
        if(match(NUMBER_LITERAL)) { return new Expr.Literal(previous().literal); }
        if(match(IDENTIFIER)) { return new Expr.Identifier(previous().lexeme); }
        throw error(peek(), "Except expression");
    }





    private Expr notExpression()
    {
        consume(NOT, "Except 'not'");
        Expr right = expression();
        return new Expr.Not(right);
    }

    private Expr comparisonExpression(Expr left)
    {
        ComparisonType type = parseComparisonCriteria();
        Expr right = arithmeticExpression();
        return new Expr.Compare(left, type, right);
    }





    private ComparisonType parseComparisonCriteria()
    {
        if(match(EQUAL))
        {
            consume(TO, "Except identifier");
            return ComparisonType.EQUAL;
        }


        if(match(NOT)){
            consume(EQUAL, "Except 'equal'");
            consume(TO, "Except 'to'");
            return ComparisonType.EQUAL;
        }

        if(match(LESS))
        {
            consume(THAN, "Except 'than'");
            return ComparisonType.LESS;
        }

        if(match(MORE))
        {
            consume(THAN, "Except 'than'");
            return ComparisonType.GREATER;
        }



        // default case : delete
        throw error(peek(), "Invalid comparison");
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
        if (isAtEnd()) return false;
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


