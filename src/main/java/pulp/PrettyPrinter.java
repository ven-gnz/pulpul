package pulp;

import java.util.List;

public class PrettyPrinter
        implements Expr.Visitor<String>,
        Stmt.Visitor<String> {

    private int indent = 0;

    public void print(List<Stmt> statements)
    {
        for (Stmt stmt : statements)
        {
            System.out.println(stmt.accept(this));
        }
    }

    private String pad()
    {
        return "  ".repeat(indent);
    }

    private String withIndent(String text)
    {
        return pad() + text;
    }

    private String block(String header, Runnable bodyBuilder, StringBuilder out)
    {
        out.append(withIndent(header)).append("\n");

        indent++;
        bodyBuilder.run();
        indent--;

        return out.toString();
    }


    @Override
    public String visitExpressionStmt(Stmt.Expression stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("ExpressionStmt\n"));

        indent++;
        out.append(expr(stmt.expression));
        indent--;

        return out.toString();
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return "";
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return "";
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        return "";
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return "";
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("DisplayStmt\n"));

        indent++;
        for(Expr e : stmt.expressions)
        {
            out.append(e.accept(this));
        }

        indent--;

        return out.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return "";
    }

    @Override
    public String visitProgramStmt(Stmt.Program stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Program "))
                .append(stmt.name.lexeme)
                .append("\n");

        indent++;

        for (Stmt.Subprogram method : stmt.methods)
        {
            out.append(method.accept(this));
        }

        indent--;

        return out.toString();
    }

    @Override
    public String visitSubprogramStmt(Stmt.Subprogram stmt)
    {
        return withIndent("Subprogram " + stmt.name.lexeme + "\n");
    }


    private String expr(Expr expr)
    {
        return expr.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr)
    {
        return withIndent("Literal(" + expr.value + ")\n");
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr)
    {
        return withIndent("Variable(" + expr.name.lexeme + ")\n");
    }

    @Override
    public String visitThisExpr(Expr.This expr)
    {
        return withIndent("This\n");
    }

    @Override
    public String visitGetExpr(Expr.Get expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Get\n"));

        indent++;

        out.append(withIndent("Object:\n"));
        indent++;
        out.append(expr(expr.object));
        indent--;

        out.append(withIndent("Property: "))
                .append(expr.name.lexeme)
                .append("\n");

        indent--;

        return out.toString();
    }

    @Override
    public String visitCallExpr(Expr.Call expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Call\n"));

        indent++;

        out.append(withIndent("Callee:\n"));
        indent++;
        out.append(expr(expr.callee));
        indent--;

        if (!expr.arguments.isEmpty())
        {
            out.append(withIndent("Arguments:\n"));

            indent++;

            for (Expr argument : expr.arguments)
            {
                out.append(expr(argument));
            }

            indent--;
        }

        indent--;

        return out.toString();
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Assign "))
                .append(expr.name.lexeme)
                .append("\n");

        indent++;
        out.append(expr(expr.value));
        indent--;

        return out.toString();
    }


    @Override
    public String visitUnaryExpr(Expr.Unary expr)
    {
        return withIndent("Unary(" + expr.operator.lexeme + ")\n");
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr)
    {
        return withIndent("Logical(" + expr.operator.lexeme + ")\n");
    }

    @Override
    public String visitSetExpr(Expr.Set expr)
    {
        return withIndent("Set\n");
    }

    @Override
    public String visitOfExpr(Expr.Of expr)
    {
        return withIndent("OfExpr\n");
    }

    @Override
    public String visitAddExpr(Expr.Add expr)
    {
        return withIndent("Add\n");
    }

    @Override
    public String visitRemoveExpr(Expr.Remove expr)
    {
        return withIndent("Remove\n");
    }

    @Override
    public String visitMultiplyExpr(Expr.Multiply expr)
    {
        return withIndent("Multiply\n");
    }

    @Override
    public String visitDivideExpr(Expr.Divide expr)
    {
        return withIndent("Divide\n");
    }

    @Override
    public String visitCompareExpr(Expr.Compare expr)
    {
        return withIndent("Compare\n");
    }

    @Override
    public String visitMultistringExpr(Expr.Multistring expr)
    {
        return withIndent("Multistring\n");
    }
}

