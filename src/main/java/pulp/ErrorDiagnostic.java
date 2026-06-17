package pulp;

/**
 * This is a record for parser error reporting.
 * Simple parse errors do not stop the parsing, and are gathered into here for additional scrutination.
 * @param line the line which the offending token was found on
 * @param lexeme the lexeme of the token
 * @param message additional information based on parser state
 */
public record ErrorDiagnostic(
        int line,
        String lexeme,
        String message
) {}
