package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Symbols.SymbolType;

public enum TokenTag implements SymbolType {
    //KEYWORDS
    AND,
    ACTION,
    AFTER,
    ALL,
    ARRAY,
    AS,
    ASC,
    AVG,
    BEFORE,
    BEGIN,
    BETWEEN,
    BIGINT,
    BOOLEAN,
    BY,
    CASCADE,
    CHAR,
    CHARACTER,
    CHECK,
    CONSTRAINT,
    CONTINUE,
    COUNT,
    CREATE,
    CROSS,
    DATE,
    DECIMAL,
    DECLARE,
    DEFAULT,
    DELETE,
    DESC,
    DISTINCT,
    DOUBLE,
    EACH,
    ELSE,
    ELSIF,
    END,
    EXCEPT,
    EXCEPTION,
    EXECUTE,
    EXISTS,
    EXIT,
    FALSE,
    FLOAT,
    FOR,
    FOREIGN,
    FROM,
    FULL,
    FUNCTION,
    GROUP,
    HAVING,
    IF,
    IN,
    INHERITS,
    INNER,
    INSERT,
    INT,
    INTEGER,
    INTERSECT,
    INTO,
    INSTEAD,
    INOUT,
    IS,
    JOIN,
    KEY,
    LANGUAGE,
    LEFT,
    LIKE,
    LOOP,
    MAX,
    MIN,
    NO,
    NOT,
    NOTICE,
    NULL,
    NUMERIC,
    OF,
    ON,
    ONLY,
    OR,
    ORDER,
    OUT,
    OUTER,
    PLPGSQL,
    PRECISION,
    PRIMARY,
    PROCEDURE,
    QUERY,
    RAISE,
    REAL,
    RECORD,
    REFERENCES,
    REPLACE,
    RESTRICT,
    RETURN,
    RETURNS,
    REVERSE,
    RIGHT,
    ROW,
    SET,
    SELECT,
    SMALLINT,
    SUM,
    TABLE,
    THEN,
    TIME,
    TIMESTAMP,
    TRIGGER,
    TRUE,
    UNION,
    UNIQUE,
    UPDATE,
    USING,
    VARCHAR,
    VALUES,
    WHERE,
    WHEN,
    WHILE,

    //SYMBOLS
    END_OF_PROGRAM,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    LESS,
    LESSEQ,
    GREATER,
    GREATEREQ,
    EQUAL,
    NOTEQUAL,
    DOT,
    COMMA,
    SEMICOLON,
    ADD,
    SUB,
    MUL,
    DIV,
    ASSIGN,
    IDENTIFIER,
    BYTE_CONST,
    SHORT_CONST,
    INT_CONST,
    LONG_CONST,
    FLOAT_CONST,
    DOUBLE_CONST,
    STRING_CONST,
    TIMESTAMP_CONST,
    DATE_CONST,
    TIME_CONST,
    DOUBLE_DOLLAR,
    DOUBLE_DOT,
}
