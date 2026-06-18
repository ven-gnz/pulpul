package pulp;
import java.util.List;



import static pulp.PrimitiveType.ULPPrimitive.*;
import static pulp.TokenType.EQUAL;


public class TypeChecker implements Expr.Visitor<Type>, Stmt.Visitor<Void>{

    List<Stmt> statements;
    Resolver resolver;
    private Stmt.Program currentProgram = null;
    private Stmt.Subprogram currentSubProgram = null;
    private int loopDepth = 0;

    public TypeChecker(List<Stmt> stmts, Resolver resolver)
    {
        this.statements = stmts;
        this.resolver = resolver;
    }

    public void check()
    {
        try
        {
            for(Stmt s : statements) {
                checkType(s);
            }
        }
        catch (RuntimeError error) { Pulper.runtimeError(error); }
    }



    private void checkType(Stmt s)
    {
        s.accept(this);
    }


    Type typeOf(Expr expr)
    {
        return expr.accept(this);
    }

    private Type promoteArithmetic(Type a, Type b)
    {
        if (a instanceof PrimitiveType pa && b instanceof PrimitiveType pb)
        {
            if (pa.kind == WHOLE_NUMBER && pb.kind == WHOLE_NUMBER)
                return new PrimitiveType(WHOLE_NUMBER);

            if (pa.kind == REAL_NUMBER || pb.kind == REAL_NUMBER)
                return new PrimitiveType(REAL_NUMBER);
        }
        return new ErrorType();
    }


    @Override
    public Type visitLiteralExpr(Expr.Literal expr) {
        return expr.type;
    }

    @Override
    public Type visitMultistringExpr(Expr.Multistring expr) {
        return new PrimitiveType(TEXT);
    }

    @Override
    public Type visitUnaryExpr(Expr.Unary expr) {
        return expr.right.accept(this);
    }

    @Override
    public Type visitLogicalExpr(Expr.Logical expr) {
        return new PrimitiveType(TRUTH_VALUE);
    }

    @Override
    public Type visitThisExpr(Expr.This expr) {
        return null;
    }

    @Override
    public Type visitAssignExpr(Expr.Assign expr) {

        Type valueType = typeOf(expr.value);

        System.out.println("Assigning to: " + expr.name.lexeme);

        Symbol sym = resolver.getSymbol(expr.name);

        System.out.println("Symbol = " + sym);

        if(sym != null)
        {
            System.out.println("Declared type = " + sym.type);
        }

        System.out.println("Value type = " + valueType);


        if(sym == null)
        {
            Pulper.typeError(expr.name, "trying to assign a non-initialized variable", currentProgram, currentSubProgram);
            return new ErrorType();
        }

        if(sym.type != null && valueType != null)
        {
            if(!isAssignable(sym.type, valueType))
            {
                Pulper.typeError(expr.name,"Cannot assign " + sym.type + " to " + valueType,currentProgram,currentSubProgram);
                return new ErrorType();
            }
        }

        resolver.updateSymbol(sym);
        return valueType;

    }

    @Override
    public Type visitAddExpr(Expr.Add expr) {

        Type left = typeOf(expr.left);
        Type right = typeOf(expr.right);

        if(!isNumeric(left) || !isNumeric(right))
        {
            Pulper.typeError(expr.keyword, "cannot add "+ left + " to " + right,currentProgram,currentSubProgram);
            return new ErrorType();
        }
        return promoteArithmetic(left,right);
    }

    @Override
    public Type visitRemoveExpr(Expr.Remove expr) {

        Type left = typeOf(expr.left);
        Type right = typeOf(expr.right);

        if(!isNumeric(left) || !isNumeric(right))
        {
            Pulper.typeError(expr.keyword, "cannot remove "+ right + " from " + left,currentProgram,currentSubProgram);
            return new ErrorType();
        }
        return promoteArithmetic(left,right);
    }

    @Override
    public Type visitMultiplyExpr(Expr.Multiply expr) {

        Type left = typeOf(expr.left);
        Type right = typeOf(expr.right);

        if(!isNumeric(left) || !isNumeric(right))
        {
            Pulper.typeError(expr.keyword, "cannot multiply "+ left + " with " + right,currentProgram,currentSubProgram);
            return new ErrorType();
        }
        return promoteArithmetic(left,right);
    }

    @Override
    public Type visitDivideExpr(Expr.Divide expr) {
        Type left = typeOf(expr.left);
        Type right = typeOf(expr.right);

        if(!isNumeric(left) || !isNumeric(right))
        {

            return new ErrorType();
        }
        return promoteArithmetic(left,right);
    }

    @Override
    public Type visitCompareExpr(Expr.Compare expr) {
        Type left = typeOf(expr.left);
        Type right = typeOf(expr.right);

        if(!isComparable(left, right))
        {
            Pulper.typeError(expr.operator, "cannot compare between "+ left + " and " + right,currentProgram,currentSubProgram);
        }
        else if(left instanceof PrimitiveType pLeft && pLeft.kind == PrimitiveType.ULPPrimitive.TEXT && expr.operator.type != EQUAL)
        {
            Pulper.typeError(expr.operator, "cannot compare equality of strings between "+ left + " and " + right,currentProgram,currentSubProgram);
        }
        return new PrimitiveType(TRUTH_VALUE);
    }

    private boolean isComparable(Type a, Type b)
    {
        if(a instanceof PrimitiveType pa && b instanceof PrimitiveType pb)
        {
            if(isNumeric(a) && isNumeric(b)) return true;
            return pa.kind == TEXT && pb.kind == TEXT;
        }
        return false;
    }

    @Override
    public Type visitVariableExpr(Expr.Variable expr) {

        Symbol s = resolver.getSymbol(expr.name);
        if(s == null)
        {
            Pulper.error(expr.name, "TypeChecker : Undefined variable");
            return new ErrorType();
        }
        if(!s.initialized)
        {
            Pulper.error(expr.name, "TypeChecker: Undefined variable");
            return new ErrorType();
        }

        if (s.type == null) {
            Pulper.error(expr.name, "TypeChecker: Variable used before type is known");
            return new ErrorType();
        }

        return s.type;
    }

    @Override
    public Type visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public Type visitCastExpr(Expr.Cast expr) {
        Type source = typeOf(expr.right);
        if(source == null)
        {
            Pulper.error("TypeChecker: cast expression should not be cast onto null ");
            return new ErrorType();
        }
        if(!isCastable(source, expr.targetType))
        {
            Pulper.error("TypeChecker: Invalid cast operation between" + source + " and " + expr.targetType);
        }
        return expr.targetType;
    }

    /**
     * Pulp supports currently only conversions between numeric types. This provides the minimal strict rule
     * between what types of primitives are castable : cast a real to an int or an int to a real when you need floating point.
     * Would be nice to have a double parsing from string
     * etc. but as of now the whole typechecker exists as a proof of concept for grading.
     *
     * @param from the original type of the right hand expr
     * @param to the left hand type which the cast should result in
     * @return success of cast
     */
    public boolean isCastable(Type from, Type to)
    {
        if(from.equals(to)) return true;
        return isNumeric(from) && isNumeric(to);
    }

    private boolean isNumeric(Type t)
    {
        if (!(t instanceof PrimitiveType p)) return false;
        return p.kind == WHOLE_NUMBER
                || p.kind == REAL_NUMBER;
    }


    @Override
    public Type visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public Type visitSetExpr(Expr.Set expr) {
        return null;
    }

    @Override
    public Type visitErrorExpr(Expr.Error expr) {
        Pulper.error("Type error :" + expr.t);
        return new ErrorType();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        return null;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program stmt) {

        currentProgram = stmt;
        for(Stmt s : stmt.statements)
        {
            checkType(s);
        }

        for(Stmt.Subprogram m : stmt.methods)
        {
            checkType(m);
        }
        currentProgram = null;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {

        for(List<Expr> lines : stmt.expressions)
        {
            for(Expr e : lines)
            {
                typeOf(e);
            }
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {

        Symbol sym = resolver.getSymbol(stmt.name);

        Type declaredType = stmt.declaredType;
        Type inferredType = null;

        if(stmt.initializer != null)
        {
            inferredType = typeOf(stmt.initializer);
        }

        sym.declaredType = declaredType;
        sym.inferredType = inferredType;

        if(declaredType != null && inferredType != null)
        {
            if(!isAssignable(declaredType, inferredType)) {
                Pulper.typeError(stmt.name,"Cannot assign " + inferredType + " to " + declaredType,currentProgram,currentSubProgram);
                sym.type = new ErrorType();
            } else {
                sym.type = declaredType; }
        }

        else if (declaredType != null)
        {
            sym.inferredType = declaredType;
        }

        else if (inferredType != null)
        {
            sym.type = inferredType;
        }
        else {
            sym.type = new ErrorType();
            Pulper.error(stmt.name, " Cannot infer type of of variable");
        }
        if(!resolver.updateSymbol(sym))
        {
            Pulper.error("Error : cannot update symbols type");
        }
        return null;



    }

    private boolean isAssignable(Type to, Type from) {
        if (to instanceof PrimitiveType a && from instanceof PrimitiveType b) {

            if (a.kind == b.kind) return true;
            if (isNumeric(a) && isNumeric(b)) return true;

            return false;
        }

        return false;
    }


    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {

        Type t = null;
        if(stmt.value != null)
        {
            t = typeOf(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {

        Type cond = typeOf(stmt.condition);

        if (!isBoolean(cond)) {
            Pulper.error("If condition must be boolean");
        }

        checkType(stmt.thenBranch);

        if (stmt.elseBranch != null) {
            checkType(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {

        Type cond = typeOf(stmt.condition);

        if (!isBoolean(cond)) {
            Pulper.error("While condition must be boolean");
        }

        loopDepth++;
        checkType(stmt.body);
        loopDepth--;

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    private boolean isBoolean(Type type)
    {
        return type instanceof PrimitiveType p
                && p.kind == TRUTH_VALUE;
    }

    @Override
    public Void visitSubprogramStmt(Stmt.Subprogram stmt) {
        currentSubProgram = stmt;

        for(Parameter p : stmt.params)
        {
            Symbol s = resolver.getSymbol(p.name);
            if(s != null)
            {
                s.type = p.type;
                resolver.updateSymbol(s);
            }
        }
        for(Stmt s : stmt.body)
        {
            checkType(s);
        }
        currentSubProgram = null;
        return null;
    }

    /**
     *
     * @param stmt the input statement
     * @return nothing, handled at runtime
     */
    @Override
    public Void visitInputStmt(Stmt.Input stmt) {

        return null;
    }

    @Override
    public Void visitErrorStmt(Stmt.Error stmt) {
        Pulper.error(stmt.token, " Cannot check expression type!");
        return null;
    }


}
