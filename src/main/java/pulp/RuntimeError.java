package pulp;

public class RuntimeError extends RuntimeException{
    Token token;

    RuntimeError(Token tokne, String msg)
    {
        super(msg);
        this.token = tokne;
    }

    RuntimeError(String message)
    {
        super(message);
        token = null;
    }
}
