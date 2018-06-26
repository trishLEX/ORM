package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class NumberToken extends Token<Number> {
    public NumberToken(Position start, Position follow, Number value) {
        super(TokenTag.NUMBER_CONST, start, follow, value);
    }
}
