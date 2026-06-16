package pulp;

import java.util.Objects;

public class Symbol {

    String name;
    Type declaredType;
    Type inferredType;
    Type type;
    boolean initialized;

    /**
     * The symbol is an intermittent variable node representation. The type checker uses these symbols
     * to convey the inferred type into the symbol, while the resolver produces these symbol tokens.
     *
     * A noteworthy remark is that this constructor does not take in the final type, as the typechecker fills the symbol in with
     * the final checked type.
     * @param name the lexeme of the variable, the identifier part
     * @param declaredType the parsed type declared on parse time
     * @param inferredType the intermittent token held in this node until the final type is parsed
     * @param initialized helper boolean, not strictly needed
     */
    public Symbol(String name, Type declaredType, Type inferredType, boolean initialized)
    {
        this.name = name;
        this.declaredType = declaredType;
        this.inferredType = inferredType;
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String toString()
    {
        return "Symbol : " + name + "Type of : " + type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Symbol)) return false;
        Symbol s = (Symbol) o;
        return Objects.equals(this.name, s.name)
                && this.inferredType == s.inferredType
                && this.initialized == s.initialized
                && this.declaredType == s.declaredType;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
