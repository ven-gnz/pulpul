package pulp;


import java.util.HashMap;
import java.util.Map;

public class PulpInstance {

    private PulpProgram program;
    private final Map<String, Object> fields = new HashMap<>();

    PulpInstance(PulpProgram program)
    {
        this.program = program;
    }

    Object get(Token name)
    {
        if(fields.containsKey(name.lexeme))
        {
            return fields.get(name.lexeme);
        }

        PulpFunction function = program.findMethod(name.lexeme);
        if(function != null) return function.bind(this);
        throw new RuntimeError(name, " undefined property '" + name.lexeme +" '!");
    }



    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString()
    {
        return program.name + " instance";
    }
}
