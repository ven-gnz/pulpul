package pulp;
import java.util.List;
import java.util.Map;
public class PulpProgram implements PulpCallable{

    final String name;
    private final Map<String, PulpFunction> methods;

    PulpProgram(String name, Map<String, PulpFunction> methods)
    {

        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString()
    {
        return name;
    }


    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        PulpInstance instance = new PulpInstance(this);
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
