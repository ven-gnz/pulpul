package pulp;

public class Interpreter implements Expr.Visitor<Object>{


    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object)
    {
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean)object;
        return true;
    }


    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitIdentifierExpr(Expr.Identifier expr) {
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        return null;
    }


    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        return null;
    }

    @Override
    public Object visitAddExpr(Expr.Add expr) {
        return null;
    }

    @Override
    public Object visitRemoveExpr(Expr.Remove expr) {
        return null;
    }

    @Override
    public Object visitMultiplyExpr(Expr.Multiply expr) {
        return null;
    }

    @Override
    public Object visitDivideExpr(Expr.Divide expr) {
        return null;
    }

    @Override
    public Object visitCompareExpr(Expr.Compare expr) {
        return null;
    }
}
