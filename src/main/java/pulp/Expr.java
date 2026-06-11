package pulp;
import java.util.List;
import pulp.ComparisonType;
import pulp.Expr;

abstract class Expr{
    interface Visitor<R> {
    R visitLiteralExpr(Literal expr);
    R visitMultistringExpr(Multistring expr);
    R visitUnaryExpr(Unary expr);
    R visitLogicalExpr(Logical expr);
    R visitThisExpr(This expr);
    R visitAssignExpr(Assign expr);
    R visitAddExpr(Add expr);
    R visitRemoveExpr(Remove expr);
    R visitMultiplyExpr(Multiply expr);
    R visitDivideExpr(Divide expr);
    R visitCompareExpr(Compare expr);
    R visitVariableExpr(Variable expr);
    R visitCallExpr(Call expr);
    R visitGetExpr(Get expr);
    R visitSetExpr(Set expr);
 }
 static class Literal extends Expr {
    Literal(Object value, Type type) {
    this.value = value;
    this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitLiteralExpr(this);
    }

    final Object value;
    final Type type;
  }
 static class Multistring extends Expr {
    Multistring(List<Expr> strings) {
    this.strings = strings;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitMultistringExpr(this);
    }

    final List<Expr> strings;
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
 static class This extends Expr {
    This(Token keyword) {
    this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitThisExpr(this);
    }

    final Token keyword;
  }
 static class Assign extends Expr {
    Assign(Token name, Expr value) {
    this.name = name;
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitAssignExpr(this);
    }

    final Token name;
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
 static class Variable extends Expr {
    Variable(Token name) {
    this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitVariableExpr(this);
    }

    final Token name;
  }
 static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
    this.callee = callee;
    this.paren = paren;
    this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }
 static class Get extends Expr {
    Get(Expr object, Token name) {
    this.object = object;
    this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitGetExpr(this);
    }

    final Expr object;
    final Token name;
  }
 static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
    this.object = object;
    this.name = name;
    this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitSetExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }
    abstract <R> R accept(Visitor<R> visitor);
}
