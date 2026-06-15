package pulp;

import java.util.*;


import static pulp.PrimitiveType.ULPPrimitive.*;
import static pulp.Pulper.error;
import static pulp.TokenType.*;
import static pulp.TokenType.TEXT;



class Parser {

    private Token errorToken(Token token, String msg)
    {
        diagnostics.add(new ErrorDiagnostic(
                token.line, token.lexeme, msg));

        return new Token(
                ERROR, "<error>",msg,token.line
        );
    }

    private static class ParseError extends RuntimeException{
        public ParseError(String msg)
        {
            super(msg);
        }
    };

    public List<ErrorDiagnostic> diagnostics;
    private final Map<Integer, Integer> perLineErrors;
    private final Set<Integer> ignoredLines;
    private static final int MAX_ERRORS = 50;
    private static final int MAX_ERRORS_PER_LINE = 3;
    private int errors = 0;


    private final List<Token> tokens;
    private int current = 0;
    private Token lastToken;

    Parser(List<Token> tokens){
        this.tokens = tokens;
        this.diagnostics = new ArrayList<>();
        this.perLineErrors = new HashMap<>();
        this.ignoredLines = new HashSet<>();
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

    public void printDiagnostics()
    {
        for(ErrorDiagnostic d : diagnostics)
        {
            System.out.println(
                    "[line " + d.line() + "] " +
                            d.lexeme() + ": " +
                            d.message()
            );
        }
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
            return new Stmt.Error(lastToken, "invalid statement");
        }
    }

    private Stmt programDeclaration() {
        Token name = consume(IDENTIFIER, "Except program name.");
        consume(COLON, "Except : to start class body");

        List<Stmt.Subprogram> body = new ArrayList<>();
        List<Stmt> stmts = new ArrayList<>();
        while(!check(DOT) && !isAtEnd())
        {
            if(check(DESCRIBING))
            {
                body.add(subProgram("method"));
            }
            else{
                Stmt s = declaration();
                stmts.add(s);
            }
        }
        consume(DOT, "Except dot to end program body");
        return new Stmt.Program(name, body, stmts);
    }

    private Stmt.Subprogram subProgram(String kind) {

        // lol dorkaround
        if(kind == "method")
        {
            consume(DESCRIBING, "Except 'describing' to start method definition");
        }
        consume(SUBPROGRAM, "Excepted keyword 'subprogram' as the next keyword in the subprogram definition");
        consume(CALLED, "Excepted keyword 'called' as the next keyword in the subprogram definition");

        Token name = consume(IDENTIFIER, "Except "+kind+ " name");
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
        // parse for type here, then rest similarly as before
        Type type = inferTypeFromTokens();
        Token name = consume(IDENTIFIER, "Except variable name here");
        consume(BE, "Except 'be' after variable name in assignment");
        consume(EQUAL, "Except 'equal' after be for assignment");
        consume(TO, "Except 'to' after equal for assignment");
        return new Stmt.Var(name, type, expression());
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
        Expr value = expression();
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

        if(check(IDENTIFIER)) {
            Token id = peek();
            if(isConsecutiveIdentifier(id))
            {
                return errorExpr(id, "Unexpected identifier ");
            }
            id = advance();
            return new Expr.Variable(id);
        }
        if(match(STRING_LITERAL)) return parseString();
        if(match(NUMBER_LITERAL)) return parseNumberLiteral();
        if(match(MINUS)) return new Expr.Unary(previous(),primary());

        if(match(TRUE)) { return new Expr.Literal(TRUE, new PrimitiveType(TRUTH_VALUE)); }
        if(match(FALSE)) { return new Expr.Literal(FALSE, new PrimitiveType(TRUTH_VALUE)); }
        if(match(NOT)) { return new Expr.Unary(previous(), parseLogicalExpression()); }
        if(match(THIS)) { return new Expr.This(previous()); }

        Token t = peek();
        advance();
        return errorExpr(t, "unexpected token '" + t.lexeme + "', expected expression");

    }

    private boolean isConsecutiveIdentifier(Token current)
    {
        if(lastToken == null) return false;
        return lastToken.type == IDENTIFIER && current.type == IDENTIFIER;
    }

    private Expr parseNumberLiteral()
    {
        Token token = previous();
        if(token.lexeme.contains(".")) {
            return new Expr.Literal(token.literal, new PrimitiveType(REAL_NUMBER));
        }
        return new Expr.Literal(token.literal, new PrimitiveType(WHOLE_NUMBER));
    }

    private Expr parseString()
    {
        List<Expr> strings = new ArrayList<>();
        strings.add(new Expr.Literal(previous().literal, new PrimitiveType(PrimitiveType.ULPPrimitive.TEXT)));
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
        Type t = inferTypeFromTokens();
        if (target instanceof Expr.Variable v) {
            return new Expr.Assign(v.name, value,t);
        }
        if (target instanceof Expr.Get g) {
            return new Expr.Set(g.object, g.name, value,t);
        }
         return errorExpr(peek(), "Invalid assignment target");
    }

    /**
     * Helper method for consuming the type tokens and inferring the type to reduce redundancy
     * @return the inferred type from tokens
     */
    private Type inferTypeFromTokens()
    {

        if(match(WHOLE))
        {
            consume(NUMBER, "Excpected 'number' to complete declaration for whole number");
            return new PrimitiveType(WHOLE_NUMBER);
        }
        else if(match(REAL))
        {
            consume(NUMBER, "Excpected 'number' to complete declaration for real number");
            return new PrimitiveType(REAL_NUMBER);
        }
        else if(match(BOOLEAN)) return new PrimitiveType(PrimitiveType.ULPPrimitive.TRUTH_VALUE);

        else if(match(TEXT)) return new PrimitiveType(PrimitiveType.ULPPrimitive.TEXT);

        else {
            error(tokens.get(current), " not supported as a type");
            return null;
        }
    }

    private Expr parseLogicalExpression() {

        Expr left = logicalTerm();
        if(check(AND))
       {
           consume(AND, "Except boolean expression after 'and'");
           return new Expr.Logical(left, previous(), expression(), new PrimitiveType(TRUTH_VALUE));
       }
       if(check(OR))
       {
           consume(OR, "Except boolean expression after 'or'");
           return new Expr.Logical(left, previous(), expression(), new PrimitiveType(TRUTH_VALUE));
       }
        return left;
    }

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
        Expr left = expression();
        consume(FROM, "Except 'from' after left operand");
        Expr right = expression();
        return new Expr.Remove(left, right);
    }

    private Expr multiplyExpression()
    {
        Expr left = expression();
        consume(BY, "Except 'by' after left operand");
        Expr right = expression();
        return new Expr.Multiply(left, right);
    }

    private Expr addExpression()
    {
        Expr left  = expression();
        consume(TO, "Except 'to' after left operand");
        Expr right = expression();
        return new Expr.Add(left,right);
    }

    private Expr divideExpression()
    {
        Expr left = expression();
        consume(BY, "Except 'by' after left operand");
        Expr right = expression();
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
            else if (match(ACCESS))
            {
                Token name = consume(IDENTIFIER, "Consume error : Except identifier to access with 'access' ");
                Expr right = expr;
                expr = new Expr.Get(right, name);
            }
            else
            { break; }
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
        // default case to make java compiler happy
        return ComparisonType.EQUAL;
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
        return errorToken(peek(), msg);
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        lastToken = previous();
        return lastToken;
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



    private void reportError(Token token, String msg)
    {

        errors++;
        if(errors  >= MAX_ERRORS)
        {
            throw new RuntimeException("Too many parsing errors - aborting ");
        }
        int line = token.line;
        if(ignoredLines.contains(line)) { return ; }

        int count = perLineErrors.getOrDefault(line,0) + 1;
        perLineErrors.put(line, count);

        diagnostics.add(
                new ErrorDiagnostic(
                        line, token.lexeme, msg));
        if(count == MAX_ERRORS_PER_LINE)
        {
            ignoredLines.add(line);
            diagnostics.add(
                    new ErrorDiagnostic(line,
                            "",
                            "Too many parse errors on line "+token.line));
            throw new ParseError("Catastrophic line erro on line " + line + " - aborting statement");
        }
    }


    private Expr errorExpr(Token token, String msg)
    {
        reportError(token, msg);
        return new Expr.Error(token);
    }

    private void synchronize()
    {
        System.out.println("Synchronizing error");
        advance();
        while(!isAtEnd())
        {

            switch (peek().type)
            {
                case LET:
                case DISPLAY:
                case CHECK:
                case REPEAT:
                case DESCRIBING:
                case PROGRAM:
                    return;
            }
            advance();
        }
    }
}


