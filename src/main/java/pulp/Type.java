package pulp;


abstract class Type
{ }

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
}


class NamedType extends Type
{
    String name;
}