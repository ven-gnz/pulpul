package pulp;
import java.util.List;



import static pulp.PrimitiveType.ULPPrimitive.*;


public class TypeChecker implements Expr.Visitor<Type>, Stmt.Visitor<Void>{

    List<Stmt> statements;
    Resolver resolver;

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


    private Type promote(Type a, Type b)
    {
        if(a instanceof PrimitiveType && b instanceof PrimitiveType)
        {
            if( ((PrimitiveType) a).kind == REAL_NUMBER &&
                    ((PrimitiveType) b).kind == REAL_NUMBER)
            {
                return new PrimitiveType(REAL_NUMBER);
            }
        }
        return new PrimitiveType(WHOLE_NUMBER);
    }

    private void checkType(Stmt s)
    {
        s.accept(this);
    }


    Type typeOf(Expr expr)
    {
        return expr.accept(this);
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
        return null;
    }

    @Override
    public Type visitAddExpr(Expr.Add expr) {

        Type left = typeOf(expr.left);
        Type right = typeOf(expr.right);

        if(!isNumeric(left) || !isNumeric(right))
        {
            Pulper.error("Cannot add " + left + " and " + right);
            return new ErrorType();
        }
        System.out.println("Both add were numeric!!!!!");


        return promote(left,right);
    }

    @Override
    public Type visitRemoveExpr(Expr.Remove expr) {
        return null;
    }

    @Override
    public Type visitMultiplyExpr(Expr.Multiply expr) {
        return null;
    }

    @Override
    public Type visitDivideExpr(Expr.Divide expr) {
        return null;
    }

    @Override
    public Type visitCompareExpr(Expr.Compare expr) {
        return null;
    }

    @Override
    public Type visitVariableExpr(Expr.Variable expr) {
        Type t = resolver.getType(expr.name);
        System.out.println("RESOLVE: " + expr.name.lexeme + " -> " + t);
        if(t == null)
        {
            Pulper.error(expr.name, "Undefined variable");
            return null;
        }
        return t;
    }

    @Override
    public Type visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public Type visitCastExpr(Expr.Cast expr) {

        Type source = typeOf(expr.right);

        if(!isCastable(source, expr.targetType))
        {
            Pulper.error(null, "Invalid cast operation");
        }

        return null;
    }

    public boolean isCastable(Type from, Type to)
    {
        if(from.equals(to)) return true;

        if(isNumeric(from) && isNumeric(to)) return true;
        if(isTextual(from) || isTextual(to)) return false;
        return !isBoolean(from) && !isBoolean(to);
    }

    private boolean isNumeric(Type t)
    {
        if (!(t instanceof PrimitiveType p)) return false;
        return p.kind == WHOLE_NUMBER
                || p.kind == REAL_NUMBER;
    }

    private boolean isTextual(Type type)
    {
        return type instanceof PrimitiveType primitive &&
                primitive.kind == TEXT;
    }

    private boolean isBoolean(Type type)
    {
        return type instanceof PrimitiveType primitive && primitive.kind == TRUTH_VALUE;
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
        Pulper.error("Type error");
        return new ErrorType();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        return null;
    }

    @Override
    public Void visitProgramStmt(Stmt.Program stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {

        Type initializerType = null;
        if (stmt.initializer != null) {
            initializerType = typeOf(stmt.initializer);
        }
        Type declaredType = stmt.declaredType;

        if(declaredType != null && initializerType != null)
        {
            if(!declaredType.equals(initializerType))
            {
                //System.out.println("declaredType = " + debugType(declaredType));
                //System.out.println("initializerType = " + debugType(initializerType));
                Pulper.error(stmt.name, "Type mismatch in variable declaration");
            }
        }
        Type finaltype = initializerType;
        return null;
    }

    private String debugType(Type t) {
        if (t instanceof PrimitiveType p) {
            return "PrimitiveType(" + p.kind + ")";
        }
        return String.valueOf(t);
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitSubprogramStmt(Stmt.Subprogram stmt) {
        return null;
    }

    @Override
    public Void visitErrorStmt(Stmt.Error stmt) {
        System.out.println("Checking error type");
        return null;
    }


}
