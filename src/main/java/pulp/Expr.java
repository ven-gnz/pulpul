package pulp;
import java.util.List;

abstract class Expr{
    interface Visitor<R> {
    R visitLiteralExpr(Literal expr);
    R visitIdentifierExpr(Identifier expr);
    R visitNotExpr(Not expr);
    R visitAssignExpr(Assign expr);
    R visitAddExpr(Add expr);
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
 static class Not extends Expr {
    Not(Expr expression) {
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitNotExpr(this);
    }

    final Expr expression;
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
    abstract <R> R accept(Visitor<R> visitor);
}
