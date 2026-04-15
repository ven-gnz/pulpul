package pulp;
import java.util.List;

abstract class Expr{
 static class Literal extends Expr {
    Literal(Object value) {
    this.value = value;
    }

    final Object value;
  }
 static class Identifier extends Expr {
    Identifier(String name) {
    this.name = name;
    }

    final String name;
  }
 static class Assign extends Expr {
    Assign(String name, Expr value) {
    this.name = name;
    this.value = value;
    }

    final String name;
    final  Expr value;
  }
 static class Add extends Expr {
    Add(Expr left, Expr right) {
    this.left = left;
    this.right = right;
    }

    final Expr left;
    final  Expr right;
  }
}
