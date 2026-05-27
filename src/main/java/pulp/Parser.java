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
            if(match(DESCRIBING))
            {
                return subProgram("function");
            }
            if(match(PROGRAM))
            {
                return programDeclaration();
            }

            return statement();
        } catch (ParseError per)
        {
            synchronize();
            return null;
        }
    }

    private Stmt programDeclaration() {
        Token name = consume(IDENTIFIER, "Except program name.");
        consume(COLON, "Except : to start class body");

        List<Stmt.Subprogram> body = new ArrayList<>();
        while(!check(DOT) && !isAtEnd())
        {
            body.add(subProgram("method"));
        }
        consume(DOT, "Except dot to end class body");

        return new Stmt.Program(name, body);
    }

    private Stmt.Subprogram subProgram(String kind) {

        consume(SUBPROGRAM, "Excepted keyword 'subprogram' as the next keyword in the subprogram definition");
        consume(CALLED, "Excepted keyword 'called' as the next keyword in the subprogram definition");

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
        List<Expr> expressions = new ArrayList<>();
        expressions.add(expression());
        while(match(COMMA))
        {
            expressions.add(expression());
        }
        return new Stmt.Print(expressions);

    }

    private Expr expression()
    {

        if(check(SET))
        {
            return assignment();
        }
        if(match(IS)) {
            return parseLogicalExpression();
        }


        if (check(ADD) || check(REMOVE) || check(DIVIDE) || check(MULTIPLY)) {
            return arithmeticExpression();
        }

        return call();

    }



    private Expr primary()
    {

        if(match(IDENTIFIER)) { return new Expr.Variable(previous()); }
        if(match(STRING_LITERAL)) return parseString();
        if(match(NUMBER_LITERAL)) return new Expr.Literal(previous().literal);
        if(match(MINUS)) return new Expr.Unary(previous(),primary());

        if(match(TRUE)) { return new Expr.Literal(TRUE); }
        if(match(FALSE)) { return new Expr.Literal(FALSE); }
        if(match(NOT)) { return new Expr.Unary(previous(), parseLogicalExpression()); }

        error(tokens.get(current), "Cannot parse expression");
        return null;
    }

    private Expr parseString()
    {


        List<Expr> strings = new ArrayList<>();

        strings.add(new Expr.Literal(previous().literal));
        while(match(PLUS))
        {
            strings.add(expression());
        }
        return new Expr.Multistring(strings);


    }

    private Expr assignment()
    {

        consume(SET, "Except set to reassign variables");
        Expr target = call();
        consume(TO, "Expected 'to' as the next keyword");
        Expr value = expression();
        if (target instanceof Expr.Variable v) {
            return new Expr.Assign(v.name, value);
        }
        if (target instanceof Expr.Get g) {
            return new Expr.Set(g.object, g.name, value);
        }
        throw error(peek(), "Invalid assignment target");
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
           consume(OR, "Except boolean expression after 'or'");
           return new Expr.Logical(left, previous(), expression());
       }

        return left;

    }

    // this is logical term?
    private Expr logicalTerm()
    {
        Expr left = primary();
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
        return primary();

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



    private Expr call()
    {


        Expr expr = primary();

        while(true)
        {
            if(match(LEFT_PAREN))
            {
                expr = finishCall(expr);
            }
            else if (match(OF))
            {
                Token name = consume(IDENTIFIER, "Except identifier after 'of'");
                expr = new Expr.Get(expr,name);
            }
            else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee)
    {
        List<Expr> args = new ArrayList<>();
        if(!check(RIGHT_PAREN))
        {
            do{
                args.add(expression());
            }while(match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Except ')' to end argument list");

        return new Expr.Call(callee, paren, args);
    }

    private Expr comparisonExpression(Expr left)
    {
        System.out.println("COMPARISON EXPRESSION");
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


