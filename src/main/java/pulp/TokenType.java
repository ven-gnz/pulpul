package pulp;


/*
Refactor each word to be its own keyword
 */
public enum TokenType {

    // character tokens
    COMMA, DOT, COLON, SEMICOLON, EOF,
    LESS, THAN, OR, MORE, EQUAL, NOT, MINUS,

    GLOBAL, LET, SET,
    IDENTIFIER,
    LEFT_PAREN, RIGHT_PAREN,
    BREAK,
    RETURN,
    PLUS,


    DESCRIPTION, OF, SUBPROGRAM, CALLED,
    ACTING, ON, INPUTS,
    PRODUCING, OUTPUTS,
    CHECK, THEN, OTHERWISE,
    ADD, REMOVE, MULTIPLY, DIVIDE,
    TO, FROM, WITH, BY,
    BE, IS, DISPLAY, RESULT,
    AND,


    //Multi word keywords
    TYPE_STRING, TYPE_BOOLEAN, WHOLE, REAL, NUMBER,
    TRUE, FALSE, STRING_LITERAL, NUMBER_LITERAL,

    INPUT, USER,
    REPEAT, UNTIL


}
