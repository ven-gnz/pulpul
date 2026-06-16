package pulp;

public class Symbol {

    String name;
    Type declaredType;
    Type inferredType;
    Type type;
    boolean initialized;

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
        return "Symbol : " + name + "Type of : " + inferredType;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Symbol)) return false;
        Symbol s = (Symbol) o;
        return this.name == s.name
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
