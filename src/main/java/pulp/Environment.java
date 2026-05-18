package pulp;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Environment {



    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment()
    {
        enclosing = null;
    }

    Environment(Environment enclosee)
    {
        enclosing = enclosee;
    }

    Object get(Token name)
    {
        if (values.containsKey(name.lexeme))
        {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme +"' !");
    }

    Object getAt(int distance, String name)
    {
        return ancestor(distance).values.get(name);
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    /**
     * Worth noting, that the ancestor method always produces a variable, as long as it is only used for resolved variables.
     * @param distance the resolved distance in nested scopes
     * @return the environment which is found
     */
    Environment ancestor(int distance)
    {
        Environment environment = this;
        for(int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    void define(String name, Object value)
    {
        values.put(name, value);
    }

    void assign(Token name, Object value)
    {
        if (values.containsKey(name.lexeme))
        {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Trying to reassign undeclared variable '" + name.lexeme + "'!");
    }
}
