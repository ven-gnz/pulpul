package pulp;

import java.util.List;


/**
 * Pretty standard debug printing tool.
 */
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
    public String visitVarStmt(Stmt.Var stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Var " + stmt.name.lexeme + "\n"));

        if (stmt.initializer != null)
        {
            indent++;
            out.append(withIndent("Initializer:\n"));
            indent++;
            out.append(expr(stmt.initializer));
            indent -= 2;
        }

        return out.toString();
    }


    @Override
    public String visitReturnStmt(Stmt.Return stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Return\n"));

        if (stmt.value != null)
        {
            indent++;
            out.append(expr(stmt.value));
            indent--;
        }

        return out.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("If\n"));

        indent++;
        out.append(withIndent("Condition:\n"));
        indent++;
        out.append(expr(stmt.condition));
        indent--;

        out.append(withIndent("Then:\n"));
        indent++;
        out.append(stmt.thenBranch.accept(this));
        indent--;

        if (stmt.elseBranch != null)
        {
            out.append(withIndent("Else:\n"));
            indent++;
            out.append(stmt.elseBranch.accept(this));
            indent--;
        }

        indent--;

        return out.toString();
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("While\n"));

        indent++;
        out.append(withIndent("Condition:\n"));
        indent++;
        out.append(expr(stmt.condition));
        indent--;

        out.append(withIndent("Body:\n"));
        indent++;
        out.append(stmt.body.accept(this));
        indent--;

        indent--;

        return out.toString();
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return withIndent("Break\n");
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
    public String visitBlockStmt(Stmt.Block stmt)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Block\n"));

        indent++;
        for (Stmt s : stmt.statements)
        {
            out.append(s.accept(this));
        }
        indent--;

        return out.toString();
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

    @Override
    public String visitErrorStmt(Stmt.Error stmt)
    {
        return withIndent("ErrorStmt(" + stmt.token + ")\n");
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
    public String visitCastExpr(Expr.Cast expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Cast\n"));

        indent++;
        out.append(withIndent("Value:\n"));
        indent++;
        out.append(expr(expr.right));
        indent--;

        out.append(withIndent("Target:\n"));
        indent++;
        out.append(withIndent(expr.targetType + "\n"));
        indent--;

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
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Unary(" + expr.operator.lexeme + ")\n"));

        indent++;
        out.append(withIndent("Operand:\n"));
        indent++;
        out.append(expr(expr.right));
        indent--;
        indent--;

        return out.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Logical(" + expr.operator.lexeme + ")\n"));

        indent++;

        out.append(withIndent("Left:\n"));
        indent++;
        out.append(expr(expr.left));
        indent--;

        out.append(withIndent("Right:\n"));
        indent++;
        out.append(expr(expr.right));
        indent--;

        indent--;

        return out.toString();
    }

    @Override
    public String visitSetExpr(Expr.Set expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Set\n"));

        indent++;

        out.append(withIndent("Object:\n"));
        indent++;
        out.append(expr(expr.object));
        indent--;

        out.append(withIndent("Field: " + expr.name.lexeme + "\n"));

        out.append(withIndent("Value:\n"));
        indent++;
        out.append(expr(expr.value));
        indent--;

        indent--;

        return out.toString();
    }

    @Override
    public String visitErrorExpr(Expr.Error expr) {

        return "Error expression:" + expr.t;
    }


    @Override
    public String visitAddExpr(Expr.Add expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Add\n"));

        indent++;

        out.append(withIndent("Left:\n"));
        indent++;
        out.append(expr(expr.left));
        indent--;

        out.append(withIndent("Right:\n"));
        indent++;
        out.append(expr(expr.right));
        indent--;

        indent--;

        return out.toString();
    }

    @Override
    public String visitRemoveExpr(Expr.Remove expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Remove\n"));

        indent++;
        ;
        out.append(withIndent("Target:\n"));
        indent++;
        // flipped as noted in the parser
        out.append(withIndent(expr(expr.right)));
        out.append("-");
        out.append(withIndent(expr(expr.left)));
        indent--;

        indent--;

        return out.toString();
    }

    @Override
    public String visitMultiplyExpr(Expr.Multiply expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Multiply( )\n"));

        indent++;

        out.append(withIndent("Left:\n"));
        indent++;
        out.append(expr(expr.left));
        indent--;

        out.append(withIndent("Right:\n"));
        indent++;
        out.append(expr(expr.right));
        indent--;

        indent--;

        return out.toString();
    }

    @Override
    public String visitDivideExpr(Expr.Divide expr)
    {
        StringBuilder out = new StringBuilder();

        out.append(withIndent("Divide(" + ")\n"));

        indent++;

        out.append(withIndent("Left:\n"));
        indent++;
        out.append(expr(expr.left));
        indent--;

        out.append(withIndent("Right:\n"));
        indent++;
        out.append(expr(expr.right));
        indent--;

        indent--;

        return out.toString();
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

