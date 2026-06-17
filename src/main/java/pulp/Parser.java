package pulp;

import java.util.*;


import static pulp.PrimitiveType.ULPPrimitive.*;
import static pulp.Pulper.error;
import static pulp.TokenType.*;
import static pulp.TokenType.TEXT;



class Parser {

    /**
     * An error token for a more meaningful debugging process.
     * The diagnostics gather information from failed expressions
     * @param token the perhaps misplaced lexeme
     * @param msg an inscription of supposed epistemic non-conformity
     * @return a noken do token
     */
    private Token errorToken(Token token, String msg)
    {
        diagnostics.add(new ErrorDiagnostic(
                token.line, token.lexeme, msg));

        return new Token(
                ERROR, "<error>",msg,token.line
        );
    }

    /**
     * ParseError is an intermittent error which does not stop the parsing, but throwing it does trigger synchronize.
     * This two tier error mechanic was introduced to gather more than one possible typo when writing the testing programs, and to gather more meaningful error messages
     * when developing the parser and the steps after it.
     */
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

    /**
     * This method would need some updating, possible nice to have feature would be to dump all the diagnostics into a log file.
     * Will not happen in course timescope.
     */
    public void printDiagnostics()
    {
        for(ErrorDiagnostic d : diagnostics) System.out.println(
                    "[line " + d.line() + "] " +
                            d.lexeme() + ": " +
                            d.message());

    }

    /**
     * Declaration is the first AST generating level, in that this method does some pre-parsing to get to statement level parsing.
     * Its relevance and naming is ambiguous, but does lead to smaller/easier to read statement level parsing.
     * @return the parsed statement AST node
     */
    private Stmt declaration()
    {
        try
        {
            if(match(LET)) return varDeclaration();
            if(match(CHECK)) return ifStatement();

            // optional repeat keyword on loops
            if(match(REPEAT)) {
                consume(UNTIL, "Expect 'until' to follow the optional keyword repeat to start loop statement");
                return whileStatement();
            }
            if(match(UNTIL)) return whileStatement();

            if(match(DESCRIBING)) return subProgram("function");
            if(match(PROGRAM)) return programDeclaration();
            return statement();
        } catch (ParseError per)
        {
            synchronize();
            return new Stmt.Error(lastToken, "invalid statement");
        }
    }

    /**
     * Declares a new variable on the heap.
     * Optionally, two original keywords are consumed in that user can omit either or both keywords to declare variables as noted in the grammar.
     * In other words, valid forms are:
     * -let type identifier be equal to
     * -let type identifier equal to
     * -let type identifier equal
     * -let type identifier be equal
     * @return a statement AST node with the value, identifier and type
     */
    private Stmt varDeclaration()
    {
        Type type = inferTypeFromTokens();
        Token name = consume(IDENTIFIER, "Except variable name here");
        if(check(BE)) consume(BE,"Expect 'be' to be next optional keyword here");
        consume(EQUAL, "Except 'equal' after be for declaration");
        if(check(TO)) consume(TO, "Except 'to' as the next optional keyword here");
        return new Stmt.Var(name, type, call());
    }

    /**
     * Due to original grammar design, the comma and 'then' keyword are included in the parser method, but are not required.
     * This time however the optional elements are back to back, so this time only three valid choices for the user to input their code:
     * Orig : check condition, then
     * new: check condition, check condition then.
     * Optionally(as denoted in the grammar), parses the else block
     * @return the if statement AST node
     */
    private Stmt ifStatement() {

        Expr condition = expression();
        if(check(COMMA)) consume(COMMA, "Except comma as the next optional token here");
        if(check(THEN)) consume(THEN, "Except then after comma as an optional keyword on if clause");
        Stmt thenBrach = statement();
        Stmt elseBranch = null;
        if (match(OTHERWISE)) elseBranch = statement();
        return new Stmt.If(condition, thenBrach, elseBranch);
    }

    /**
     * While statement can be initialized with an optional starting keyword repeat, or simply by the required keyword until.
     * @return the parsed while statement AST node
     */
    private Stmt whileStatement() {
        Expr condition = expression();
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    /**
     * SubPrograms in ULP are functions and methods.
     * @param kind denotes whether this is a member method or a global scope subprogram
     * @return the subprogram AST node
     */
    private Stmt.Subprogram subProgram(String kind) {


        if(kind == "method")
        {
            consume(DESCRIBING, "Except 'describing' to start method definition");
        }
        consume(SUBPROGRAM, "Excepted keyword 'subprogram' as the next keyword in the subprogram definition");
        if(check(CALLED)) consume(CALLED, "Excepted optional keyword 'called' as the next keyword in the subprogram definition");


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


    /**
     * Program is a high level item in ULP, which contains subprograms and has its own scope
     * @return the parsed program item
     */
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



    /**
     * Essentially a helper method for statement level parsing. I guess th
     * @return
     */
    private Stmt statement()
    {
        if(match(DISPLAY)) return printstatement();
        if(match(COLON)) return new Stmt.Block(block());
        if(match(BREAK)) { return new Stmt.Break(previous()); }
        if(match(RETURN)) { return returnStatement(); }
        return expressionStatement();
    }

    /**
     * Print statements also features an optional keyword as denoted in the grammar. After display, the optional result can be used(due to earlier language design).
     * Alternatively, other keywords are parsed normally, but decipted as print strings, meaning that expressions get evaluated as expected.
     * Uses the print segment method to garner a segment
     * Furthermore, additional lines of expressions can be inserted by using the comma operator.
     * @return the print statement
     */
    private Stmt printstatement()
    {
        if(check(RESULT)) consume(RESULT, "Except 'result' after print command as an optional keyword");
        List<List<Expr>> lines = new ArrayList<>();
        do {
            lines.add(printSegment());
        } while (match(COMMA));
        return new Stmt.Print(lines);
    }

    /**
     * This is convenience method, or a workaround on the language design.
     * By design, the + operator is for string concatenation. But in the primary parsing method,
     * only a string literal starting multistring will start the parsing for additional string.
     * So for generating throwaway printable strings
     * for better developer experience, this convenience method was deemed necessary for users to print
     * their multistrings that can start with any expression, and the value is then evaluated.
     * This affects only the printed result.
     * @return the parsed segment possibly containing multistring
     */
    private List<Expr> printSegment()
    {
        List<Expr> parsed = new ArrayList<>();
        do {
            parsed.add(expression());
        } while (match(PLUS));
        return parsed;
    }


    private Stmt returnStatement() {

        Token keyword = previous();
        Expr value = expression();
        return new Stmt.Return(keyword, value);
    }

    /**
     * In ULP, blocks end on the special character dot.
     * One of the few natural language leaning artefacts still present in language design. Too late to refactor away.
     * @return the block of statements pertaining to the AST node
     */
    private List <Stmt> block()
    {
        List<Stmt> statements = new ArrayList<>();
        while(!check(DOT) && !isAtEnd()) statements.add(declaration());
        consume(DOT, "Except '.' to end block");
        return statements;
    }



    private Stmt expressionStatement()
    {
        return new Stmt.Expression(expression());
    }

    /**
     * Entry point to expression-level parsing.
     * @return the parsed expression AST node
     */
    private Expr expression()
    {

        if(check(SET)) return assignment();
        if(match(IS)) return parseLogicalExpression();
        if (check(ADD) || check(REMOVE) || check(DIVIDE) || check(MULTIPLY)) return arithmeticExpression();
        return call();
    }

    private boolean startsType()
    {
        return check(WHOLE)
        || check(REAL)
        || check(TEXT)
        || check(BOOLEAN);
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
        return errorExpr(peek(), "Invalid assignment target");
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

    private Expr arithmeticExpression() {
        if(match(ADD)) return addExpression();
        if(match(REMOVE)) return subtractExpression();
        if(match(MULTIPLY)) return multiplyExpression();
        if(match(DIVIDE)) return divideExpression();
        return primary(); }

    private Expr subtractExpression() {
        Expr left = expression();
        consume(FROM, "Except 'from' after left operand");
        Expr right = expression();
        return new Expr.Remove(left, right); }

    private Expr multiplyExpression() {
        Expr left = expression();
        consume(BY, "Except 'by' after left operand");
        Expr right = expression();
        return new Expr.Multiply(left, right);}

    private Expr addExpression() {
        Expr left  = expression();
        consume(TO, "Except 'to' after left operand");
        Expr right = expression();
        return new Expr.Add(left,right); }

    private Expr divideExpression() {
        Expr left = expression();
        consume(BY, "Except 'by' after left operand");
        Expr right = expression();
        return new Expr.Divide(left,right); }

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


    /**
     * The final expression node parsing method is a bit of a catch-all sink to whatever primaries or unaries are left,due to same last minute additions such as static typing.
     * Idea is to support casting of expressions into types, and this idea came in relatively late into the development cycle.
     * @return the parsed expression
     */
    private Expr primary()
    {

        if(startsType())
        {
            Type t = inferTypeFromTokens();
            return new Expr.Cast(t, expression());
        }
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



    /**
     * Helper method for consuming the type tokens and inferring the type to reduce redundancy
     * @return the inferred type from tokens
     */
    private Type inferTypeFromTokens()
    {

        if(match(WHOLE))
        {
            if(check(NUMBER)) consume(NUMBER, "Excpected 'number' to complete declaration for whole number");

            return new PrimitiveType(WHOLE_NUMBER);
        }
        else if(match(REAL))
        {
            if(check(NUMBER)) consume(NUMBER, "Excpected 'number' to complete declaration for whole number");
            return new PrimitiveType(REAL_NUMBER);
        }
        else if(match(BOOLEAN)) return new PrimitiveType(PrimitiveType.ULPPrimitive.TRUTH_VALUE);

        else if(match(TEXT)) return new PrimitiveType(PrimitiveType.ULPPrimitive.TEXT);

        else if(match(IDENTIFIER)) return new NamedType(previous().lexeme);

        else {
            error(tokens.get(current), " not supported as a type");
            return null;
        }
    }



    private Expr logicalTerm() {
        Expr left = primary();
        if(check(EQUAL) || check(NOT) || check(LESS) || check(MORE)) return comparisonExpression(left);
        return left;
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
        ComparisonType cType = parseComparisonCriteria();
        if(cType == null)
        {
            Token bad = tokens.get(current);
            error(bad, "Cannot parse comparison token");
            return new Expr.Error(bad);
        }
        Expr right = arithmeticExpression();
        return new Expr.Compare(left, cType, right);
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
            else return ComparisonType.LESS;
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
            else return ComparisonType.GREATER;
        }
        return null;
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
            throw new ParseError("Catastrophic line error on line " + line + " - aborting statement");
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


