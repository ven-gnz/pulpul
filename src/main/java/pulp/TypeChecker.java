package pulp;

import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import static pulp.PrimitiveType.ULPPrimitive.*;
import static pulp.TokenType.FALSE;
import static pulp.TokenType.TRUE;

public class TypeChecker implements Expr.Visitor<Type>, Stmt.Visitor<Void>{

    Map<String, Type> variables;
    List<Stmt> statements;


    public TypeChecker(List<Stmt> stmts)
    {
        this.variables = new HashMap<>();
        this.statements = stmts;
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



    Type evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    private Type lookUp(Token name)
    {
        Type type = variables.get(name.lexeme);
        if(type == null)
        {
            Pulper.error(name, "undefined variable type of " + name.lexeme);
            return null;
        }

        return type;
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
        return null;
    }

    @Override
    public Type visitLogicalExpr(Expr.Logical expr) {
        return null;
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
        return null;
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
        return lookUp(expr.name);
    }

    @Override
    public Type visitCallExpr(Expr.Call expr) {
        return null;
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
        return null;
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
            initializerType = evaluate(stmt.initializer);
        }
        //System.out.println("INIT EXPR TYPE = " + initializerType);
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
        variables.put(stmt.name.lexeme, finaltype);

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
        return null;
    }


}
