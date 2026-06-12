package pulp;

public record ErrorDiagnostic(
        int line,
        String lexeme,
        String message
) {}
