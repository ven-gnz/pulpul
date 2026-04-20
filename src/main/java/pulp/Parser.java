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
        if(check(TRUE) || check(FALSE)) { return booleanLiteral(); }

        // parse unaries here ?

        if(match(IS, TRUE, FALSE)) {
            return logicalTerm();
        }
        if(match(AND,OR))
        {
            return parseLogicalExpression();
        }

        Expr expr = arithmeticExpression();

        return expr;

    }

    private Expr parseLogicalExpression() {

        return null;
    }

    // this is logical term?
    private Expr logicalTerm()
    {

        // is comparisontype, it consumes than, then parse right expression
        System.out.println("logical term parsed");
        Expr left = arithmeticExpression();
        Expr comparison = comparisonExpression(left);
        return comparison;

    }

    private Expr booleanLiteral()
    {
        if(check(TRUE)) {
            consume(TRUE, "Except truth value true");
            return new Expr.Literal(TRUE);
        }
        if(check(FALSE))
        {
            consume(FALSE, "Except truth value false");
            return new Expr.Literal(FALSE);
        }
        throw error(tokens.get(current), " cannot parse boolean literal");
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
        if(match(MINUS)) {
            Token operator = previous();
            Expr right = arithmeticPrimary();
            return new Expr.Unary(operator, right);
        }
        if(match(NUMBER_LITERAL)) { return new Expr.Literal(previous().literal); }
        if(match(IDENTIFIER)) { return new Expr.Identifier(previous().lexeme); }
        throw error(peek(), "Except expression");
    }





    private Expr unary()
    {

        System.out.println("Enter unary");
        if(match(MINUS, NOT))
        {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        // Do I need this?
        return arithmeticPrimary();
    }

    private Expr comparisonExpression(Expr left)
    {
        ComparisonType type = parseComparisonCriteria();
        Expr right = arithmeticExpression();
        //System.out.println("type :" + type.toString());
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
            return ComparisonType.NOT_EQUAL;
        }
        // add less than or equal to and more than or equal to cases
        if(match(LESS))
        {
            consume(THAN, "Except 'than' after less");
            if(match(OR)){
                consume(EQUAL, "Except 'equal' after or");
                consume(TO, "Except 'to' after equal");
                return ComparisonType.LESS_EQUAL;
            }
            else {
                return ComparisonType.LESS;
            }
        }

        if(match(MORE))
        {
            consume(THAN, "Except 'than'");
            if(match(OR))
            {
                consume(EQUAL, "Except 'equal' after or");
                consume(TO, "Except 'to' after equal");
                return ComparisonType.GREATER_EQUAL;
            }
            else { return ComparisonType.GREATER; }

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


