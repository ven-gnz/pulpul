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

        Object right = evaluate(expr.right);

        switch(expr.operator.type)
        {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case NOT:
                return !isTruthy(right);
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand)
    {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be number type! ");
    }

    private void checkNumberOperands(ComparisonType type, Object left, Object right)
    {
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(type, " Operators must be numbers");
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
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.type)
        {
            case GREATER ->
                checkNumberOperands(expr.type, expr.left, expr.right);
                return (double)left > (double)right;
                break;

                //TODO
            /*
            18.04
             */


            case LESS -> break;
            case EQUAL -> break;
            case NOT_EQUAL -> break;
            case GREATER_EQUAL -> break;
            case LESS_EQUAL -> break;
        }
        return null;
    }
}
