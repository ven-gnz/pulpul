package pulp;
import java.util.List;
import pulp.ComparisonType;
import pulp.Expr;

abstract class Expr{
    interface Visitor<R> {
    R visitLiteralExpr(Literal expr);
    R visitIdentifierExpr(Identifier expr);
    R visitUnaryExpr(Unary expr);
    R visitLogicalExpr(Logical expr);
    R visitAssignExpr(Assign expr);
    R visitAddExpr(Add expr);
    R visitRemoveExpr(Remove expr);
    R visitMultiplyExpr(Multiply expr);
    R visitDivideExpr(Divide expr);
    R visitCompareExpr(Compare expr);
 }
 static class Literal extends Expr {
    Literal(Object value) {
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
 static class Identifier extends Expr {
    Identifier(String name) {
    this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitIdentifierExpr(this);
    }

    final String name;
  }
 static class Unary extends Expr {
    Unary(Token operator, Expr right) {
    this.operator = operator;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
 static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
 static class Assign extends Expr {
    Assign(String name, Expr value) {
    this.name = name;
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitAssignExpr(this);
    }

    final String name;
    final Expr value;
  }
 static class Add extends Expr {
    Add(Expr left, Expr right) {
    this.left = left;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitAddExpr(this);
    }

    final Expr left;
    final Expr right;
  }
 static class Remove extends Expr {
    Remove(Expr left, Expr right) {
    this.left = left;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitRemoveExpr(this);
    }

    final Expr left;
    final Expr right;
  }
 static class Multiply extends Expr {
    Multiply(Expr left, Expr right) {
    this.left = left;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitMultiplyExpr(this);
    }

    final Expr left;
    final Expr right;
  }
 static class Divide extends Expr {
    Divide(Expr left, Expr right) {
    this.left = left;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitDivideExpr(this);
    }

    final Expr left;
    final Expr right;
  }
 static class Compare extends Expr {
    Compare(Expr left, ComparisonType type, Expr right) {
    this.left = left;
    this.type = type;
    this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitCompareExpr(this);
    }

    final Expr left;
    final ComparisonType type;
    final Expr right;
  }
    abstract <R> R accept(Visitor<R> visitor);
}
