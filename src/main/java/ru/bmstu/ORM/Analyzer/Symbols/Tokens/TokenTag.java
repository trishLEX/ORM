package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Symbols.SymbolType;

public enum TokenTag implements SymbolType {
    //KEYWORDS
    AND,
    ACTION,
    BETWEEN,
    BIGINT,
    BOOLEAN,
    CASCADE,
    CHAR,
    CHARACTER,
    CHECK,
    CONSTRAINT,
    CREATE,
    DATE,
    DECIMAL,
    DEFAULT,
    DELETE,
    DOUBLE,
    EXISTS,
    FALSE,
    FLOAT,
    FOREIGN,
    IF,
    INHERITS,
    INT,
    INTEGER,
    IS,
    KEY,
    LIKE,
    NO,
    NOT,
    NULL,
    NUMERIC,
    ON,
    OR,
    PRECISION,
    PRIMARY,
    REAL,
    REFERENCES,
    RESTRICT,
    SET,
    SMALLINT,
    TABLE,
    TIME,
    TIMESTAMP,
    TRUE,
    UNIQUE,
    UPDATE,
    VARCHAR,

    //SYMBOLS
    END_OF_PROGRAM,
    LPAREN,
    RPAREN,
    LESS,
    LESSEQ,
    GREATER,
    GREATEREQ,
    EQUAL,
    NOTEQUAL,
    DOT,
    COMMA,
    ADD,
    SUB,
    MUL,
    DIV,
    IDENTIFIER,
    NUMBER_CONST,
    CHARACTER_CONST,
    DATE_TIME_CONST
}
