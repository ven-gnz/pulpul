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
            if(match(CHECK))
            {
                return ifStatement();
            }
            if(match(REPEAT))
            {
                return whileStatement();
            }
            if(match(DESCRIPTION))
            {
                return subProgram("function");
            }

            return statement();
        } catch (ParseError per)
        {
            synchronize();
            return null;
        }
    }

    private Stmt subProgram(String kind) {

        consume(OF, "Except keyword 'of' as the next keyword in the subprogram definition");
        consume(SUBPROGRAM, "Excepted keyword 'subprogram' as the next keyword in the subprogram definition");
        consume(CALLED, "Excepted keyword 'called' as the next keyword in the subprogram definition");

        //TODO : 06.05.2026
        Token name = consume(IDENTIFIER, "Except "+kind+ " name");
        // <subprogram_definition> ::= "Description" "of" "subprogram" "called" <subprogram_name> "acting" "on" "inputs" <input_list> "producing" "outputs" <subprogram_output> <block>
        consume(ACTING, "Expected keyword 'acting' as the next keyword in the subprogram definition");
        consume(ON, " Expected keyword 'of' as the next keyword in the subprogram definition");
        consume(INPUTS, "Excepted keyword 'inputs' as the next keyword in the subprogram definition");

        List<Token> parameters = new ArrayList<>();
        if(!check(PRODUCING))
        {
            do {
                if (parameters.size() >= 127)
                {
                    error(peek(), "Cannot exceed 127 parameters");
                }
                parameters.add(consume(IDENTIFIER, "Except parameter name."));
            }while(match(COMMA));
        }
        consume(PRODUCING, "Expect keyword producing to end input argument list on function defition");
        consume(OUTPUTS, "Expected keyword 'outputs' to begin list of function return values");
        consume(COLON, "");
        List<Stmt> body = block();
        return new Stmt.Subprogram(name, parameters, body);

    }

    private Stmt whileStatement() {
        consume(UNTIL, "Except 'until' after repeat to start loop");
        Expr condition = expression();
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {

        Expr condition = expression();
        consume(COMMA, "Except comma after boolean expression on if clause");
        consume(THEN, "Except then after comma on if clause");
        Stmt thenBrach = statement();
        //this parses the block until the dot
        Stmt elseBranch = null;
        if (match(OTHERWISE))
        {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBrach, elseBranch);

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
        if(match(COLON))
        {
            return new Stmt.Block(block());
        }
        if(match(BREAK)) { return new Stmt.Break(previous()); }
        if(match(RETURN)) { return returnStatement(); }
        return expressionStatement();
    }

    private Stmt returnStatement() {

        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON))
        {
            value = expression();
        }
        consume(SEMICOLON, "Except ';' after return value");
        return new Stmt.Return(keyword, value);
    }

    private List <Stmt> block()
    {
        List<Stmt> statements = new ArrayList<>();

        while(!check(DOT) && !isAtEnd())
        {
            statements.add(declaration());
        }
        consume(DOT, "Except '.' to end block");
        return statements;
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

        //PARSE : primary, unary, arithmetic, literals
        if (check(ADD) || check(REMOVE) || check(DIVIDE) || check(MULTIPLY)) {
            return arithmeticExpression();
        }

        return primary();

    }

    private Expr primary()
    {

        if(match(IDENTIFIER)) { return new Expr.Variable(previous()); }
        if(match(STRING_LITERAL)) return new Expr.Literal(previous().literal);
        if(check(NUMBER_LITERAL) || check(MINUS)) return arithmeticPrimary();

        if(match(TRUE)) { return new Expr.Literal(TRUE); }
        if(match(FALSE)) { return new Expr.Literal(FALSE); }
        if(match(NOT)) { return new Expr.Unary(previous(), parseLogicalExpression()); }

        if(match(LEFT_PAREN)) return call();
        throw error(peek(), "Except expression : cannot parse this as arithmetic or identifier");
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
        Expr left = expression();
        if(check(EQUAL) || check(NOT) || check(LESS) || check(MORE))
        {
            return comparisonExpression(left);
        }
        return left;
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
        return new Expr.Literal(previous().literal);

    }

    private Expr call()
    {


        Expr callee = new Expr.Literal(consume(IDENTIFIER, "Except method name")); // get name parsed
        consume(WITH, "Except with to list arguments for subprogram call");

        // parse arguments until semicolon
        List<Expr> arguments = new ArrayList<>();

        while(!check(SEMICOLON))
        {
            arguments.add(expression());
            if(!check(COMMA)) { break; }
            consume(COMMA, "Expected 'comma' to continue argument list for invokation ");
        }

        Token sc = consume(SEMICOLON, "Except semicolon to end argument list on invokation");

        return new Expr.Call(callee, sc, arguments);
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


