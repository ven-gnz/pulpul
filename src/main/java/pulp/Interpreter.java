package pulp;

public class Interpreter implements Expr.Visitor<Object>{




    void interpret(Expr expression)
    {
        try
        {
            Object value = evaluate(expression);
            if (value == null) {
                System.out.println("null value :"+ value);
            }
            System.out.println(value);
            System.out.println(stringify(value));
        } catch (RuntimeError error)
        {
            Pulper.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        if(object instanceof Boolean)
        {
            if(isTruthy(object)) { return "true"; }
            else{ return "false"; }
        }


        return object.toString();
    }


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

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {

        Object left = evaluate(expr.left); // So this is supposed to be a logical expression, so maybe something that compare produces?
        Object right = evaluate(expr.right);

        switch(expr.operator.type)
        {
            case MORE -> { return null; }
            case LESS -> { return null;}
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


        if(left instanceof Double && right instanceof Double)
        {
            return;
        }
        throw new RuntimeError(type, " Cannot compare between non-numbers");
    }



    /*
        Is assign an expression or a statement ? Let's see what the book says
     */
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        return null;
    }

    @Override
    public Object visitAddExpr(Expr.Add expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if(!(left instanceof Double) || !(right instanceof Double)) throw new RuntimeError("Operands must be number type!");
        return (double)left + (double)right;
    }

    @Override
    public Object visitRemoveExpr(Expr.Remove expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if(!(left instanceof Double) || !(right instanceof Double)) throw new RuntimeError("Operands must be number type!");
        return (double)left - (double)right;
    }

    @Override
    public Object visitMultiplyExpr(Expr.Multiply expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if(!(left instanceof Double) || !(right instanceof Double)) throw new RuntimeError("Operands must be number type!");
        return (double)left * (double)right;
    }

    @Override
    public Object visitDivideExpr(Expr.Divide expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        if(!(left instanceof Double) || !(right instanceof Double)) throw new RuntimeError("Operands must be number type!");
        return (double)left / (double)right;
    }

    @Override
    public Object visitCompareExpr(Expr.Compare expr) {
        //System.out.println("Evaluating compare expression");


        Object left_eval = evaluate(expr.left);
        Object right_eval = evaluate(expr.right);



        //System.out.println("left : "+ left);

        checkNumberOperands(expr.type, left_eval, right_eval);
        System.out.println(expr.type + " expression type");
        switch(expr.type)
        {

            case GREATER -> { return (double)left_eval > (double)right_eval; }
            case LESS -> { return (double)left_eval < (double)right_eval; }
            case EQUAL -> { return(double)left_eval == (double)right_eval; }
            case NOT_EQUAL -> { return (double)left_eval != (double)right_eval; }
            case GREATER_EQUAL -> { return (double)left_eval >= (double)right_eval; }
            case LESS_EQUAL -> { return (double)left_eval <= (double)right_eval; }
        }
        //System.out.println("I did not evaluate expression type");
        return null;
    }
}
