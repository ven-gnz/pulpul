package pulp;

import java.util.ArrayList;
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

    List<Stmt> parse()
    {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd())
        {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration()
    {
        try
        {
            if(match(LET))
            {
               return varDeclaration();
            }

            return statement();
        } catch (ParseError per)
        {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration()
    {
        // check for globals here

        Token name = consume(IDENTIFIER, "Except variable name");

        consume(BE, "Except 'be' after variable name in assignment");
        consume(EQUAL, "Except 'equal' after be for assignment");
        consume(TO, "Except 'to' after equal for assignment");

        return new Stmt.Var(name, expression());
    }




    private Stmt statement()
    {
        if(match(DISPLAY))
        {
            return printstatement();
        }
        return expressionStatement();
    }

    private Stmt expressionStatement()
    {
        return new Stmt.Expression(expression());
    }

    private Stmt printstatement()
    {
        consume(RESULT, "Except 'result' after print command");
        Expr value = expression();
        return new Stmt.Print(value);

    }

    private Expr expression()
    {

        if(peek().type == SET)
        {
            return assignment();
        }
        if(check(IS)) {
            consume(IS, "Except logical expression or primary after 'is'");
            return parseLogicalExpression();
        }

        Expr expr = arithmeticExpression();

        return expr;

    }

    private Expr assignment()
    {

        if(match(SET))
        {
            consume(IDENTIFIER, "Except 'identifier' after set");
            Token id = previous();
            consume(TO, "Except 'to' after identifier on reassign");
            Expr value = expression();
            return new Expr.Assign(id,value);
        }
        error(tokens.get(current), "Invalid assignment target.");
        return null;
    }

    private Expr parseLogicalExpression() {


        if(check(TRUE)) {
            consume(TRUE, "Except boolean literal true after 'is'");
            return new Expr.Literal(TRUE);
        }
        if(check(FALSE))
        {
            consume(FALSE, "Except boolean literal false after 'is'");
            return new Expr.Literal(FALSE);
        }
        if(check(NOT))
        {
            consume(NOT,"Except boolean expression after 'not'");
            return new Expr.Unary(previous(), parseLogicalExpression());
        }

        Expr left = logicalTerm();
        if(check(AND))
       {
           consume(AND, "Except boolean expression after 'and'");
           return new Expr.Logical(left, previous(), expression());
       }
       if(check(OR))
       {
           consume(OR, "Except boolen expression after 'or'");
           return new Expr.Logical(left, previous(), expression());
       }

        return left;

    }

    // this is logical term?
    private Expr logicalTerm()
    {
        Expr left = arithmeticExpression();
        return comparisonExpression(left);
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
        if(match(IDENTIFIER)) { return new Expr.Variable(previous()); }

        throw error(peek(), "Except expression : cannot parse this as arithmetic or identifier");
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

    private void synchronize()
    {
        System.out.println("Synchronizing error");

        advance();

        while(!isAtEnd())
        {
            if(previous().type == DOT) return;

            switch (peek().type)
            {
               // this needs some thinking : do I want really to do the dot, as to search for the next scope?
            }
        }
    }
}


