package pulp;

import java.util.List;

public class PulpFunction implements PulpCallable{

    private final Stmt.Subprogram declaration;
    private final Environment closure;

    public PulpFunction(Stmt.Subprogram declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);


        for(int i = 0; i < declaration.params.size(); i++)
        {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try
        {
            interpreter.executeBlock(declaration.body, environment);
        } catch(Return returnValue)
        {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString()
    {
        return  declaration.name.lexeme;
    }
}
