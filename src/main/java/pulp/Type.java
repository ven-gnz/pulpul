package pulp;


abstract class Type
{

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}

class PrimitiveType extends Type{

    final ULPPrimitive kind;

    enum ULPPrimitive
    {

        WHOLE_NUMBER,
        REAL_NUMBER,
        TEXT,
        TRUTH_VALUE
    }

    PrimitiveType(ULPPrimitive kind)
    {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof PrimitiveType other)) return false;
        return this.kind == other.kind;
    }

    @Override
    public int hashCode()
    {
        return kind.hashCode();
    }

    @Override
    public String toString() {
        return "PrimitiveType{" +
                "kind=" + kind +
                '}';
    }
}


class NamedType extends Type
{
    String name;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof NamedType other)) return false;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}