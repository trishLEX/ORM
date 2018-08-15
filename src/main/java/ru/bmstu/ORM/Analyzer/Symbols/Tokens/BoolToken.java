package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class BoolToken extends Token<Boolean> {
    public BoolToken(Position start, Position follow, Boolean value, TokenTag tag) {
        super(tag, start, follow, value);
    }
}
