package pulp;

public class RuntimeError extends RuntimeException{
    Token token;

    RuntimeError(Token tokne, String msg)
    {
        super(msg);
        this.token = tokne;
    }

    RuntimeError(ComparisonType type, String message)
    {
        super(message);
        this.token = null;
    }

    RuntimeError(String message)
    {
        super(message);
        token = null;
    }
}
