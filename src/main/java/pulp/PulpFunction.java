package pulp;

import java.util.List;

public class PulpFunction implements PulpCallable{

    private final Stmt.Subprogram declaration;

    public PulpFunction(Stmt.Subprogram declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for(int i = 0; i < declaration.params.size(); i++)
        {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "<fn " + declaration.name.lexeme +  ">";
    }
}
