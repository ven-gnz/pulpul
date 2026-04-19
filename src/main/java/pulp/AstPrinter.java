package pulp;

class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitIdentifierExpr(Expr.Identifier expr) {
        return expr.name;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.lexeme + wrap(expr.right.accept(this));
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return wrap(expr.left.accept(this)) +
                " " + expr.operator.lexeme + " " +
                wrap(expr.right.accept(this));
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "let " + expr.name + " be equal to " + expr.value.accept(this);
    }

    @Override
    public String visitAddExpr(Expr.Add expr) {
        return "add " + wrap(expr.left.accept(this)) +
                " to " + wrap(expr.right.accept(this));
    }

    @Override
    public String visitRemoveExpr(Expr.Remove expr) {
        return "remove " + wrap(expr.left.accept(this)) +
                " from " + wrap(expr.right.accept(this));
    }

    @Override
    public String visitMultiplyExpr(Expr.Multiply expr) {
        return "multiply " + wrap(expr.left.accept(this)) +
                " by " + wrap(expr.right.accept(this));
    }

    @Override
    public String visitDivideExpr(Expr.Divide expr) {
        return "divide " + wrap(expr.left.accept(this)) +
                " by " + wrap(expr.right.accept(this));
    }

    @Override
    public String visitCompareExpr(Expr.Compare expr) {
        return  "is "+wrap(expr.left.accept(this)) + " " +
                comparisonToString(expr.type) + " " +
                wrap(expr.right.accept(this));
    }

    private String comparisonToString(ComparisonType type) {
        return switch (type) {
            case EQUAL -> "equal to";
            case NOT_EQUAL -> "not equal to";
            case LESS -> "less than";
            case LESS_EQUAL -> "less than or equal to";
            case GREATER -> "more than";
            case GREATER_EQUAL -> "more than or equal to";
        };
    }

    private String wrap(String s) {
        return "(" + s + ")";
    }
}



