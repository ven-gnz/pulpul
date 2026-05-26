package pulp;
import java.util.List;
import java.util.Map;
public class PulpProgram implements PulpCallable{

    final String name;

    PulpProgram(String name)
    {
        this.name = name;
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
}
