package pulp;
import java.util.List;
import java.util.Map;
public class PulpProgram implements PulpCallable{

    final String name;
    private final Map<String, PulpFunction> methods;
    private final List<Stmt> statements;

    PulpProgram(String name, Map<String, PulpFunction> methods, List<Stmt> statements)
    {

        this.name = name;
        this.methods = methods;
        this.statements = statements;
    }

    @Override
    public String toString()
    {
        return name;
    }


    @Override
    public int arity() {
        PulpFunction initializer = findMethod("init");
        if(initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        PulpInstance instance = new PulpInstance(this);
        for (Stmt stmt : statements) {
            if (stmt instanceof Stmt.Var varStmt) {

                Object value = null;

                if (varStmt.initializer != null) {
                    value = interpreter.evaluate(varStmt.initializer);
                }

                instance.set(varStmt.name, value);
            }
        }

        PulpFunction initializer = findMethod("init");

        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    PulpFunction findMethod(String name)
    {
        if(methods.containsKey(name)){
            return methods.get(name);
        }
        return null;
    }
}
