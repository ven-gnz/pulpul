package pulp;

/**
 * Pretty self-explanatory class for sticking the type into the token for typing signatures on methods.
 */
public class Parameter {
    final Type type;
    final Token name;

    Parameter(Type type, Token name)
    {
        this.type = type;
        this.name = name;
    }
}
