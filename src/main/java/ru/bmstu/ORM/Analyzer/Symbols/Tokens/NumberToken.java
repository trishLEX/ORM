package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class NumberToken extends Token<Number> {
    public NumberToken(TokenTag tag, Position start, Position follow, Number value) {
        super(tag, start, follow, value);
    }
}
