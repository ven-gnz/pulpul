package pulp;


/*
Refactor each word to be its own keyword
 */
public enum TokenType {

    // character tokens
    COMMA, DOT, COLON, EOF,
    LESS, THAN, OR, MORE, EQUAL, NOT,

    CONSTANT, LET, SET,
    IDENTIFIER,
    INVOKE,


    DESCRIPTION, OF, SUBPROGRAM, CALLED,
    ACTING, ON, INPUTS,
    PRODUCING, OUTPUTS,
    SHOULD, THEN, OTHERWISE,
    ADD, REMOVE, MULTIPLY, DIVIDE,
    TO, FROM, WITH, BY,
    BE, IS, DISPLAY, RESULT,


    //Multi word keywords
    TYPE_STRING, TYPE_BOOLEAN, WHOLE, REAL, NUMBER,
    LITERAL_TRUE, LITERAL_FALSE, STRING_LITERAL, NUMBER_LITERAL,

    INPUT, USER,
    REPEAT, UNTIL


}
