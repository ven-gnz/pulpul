package pulp;

import java.util.List;

    interface PulpCallable
    {
        int arity();
        Object call(Interpreter interpreter, List<Object> arguments);
    }

