package pulp;

/**
 * This is a record for parser error reporting.
 * Simple parse errors do not stop the parsing, but do produce bad AST nodes most of the time.
 * Having three or more parse errors per line triggers synchronize.
 * These are just intermittent targets for the parser to dump minor errors and then prints them on request for better development experience.
 * @param line the line which the offending token was found on
 * @param lexeme the lexeme of the token
 * @param message additional information based on parser state
 */
public record ErrorDiagnostic(
        int line,
        String lexeme,
        String message
) {}
