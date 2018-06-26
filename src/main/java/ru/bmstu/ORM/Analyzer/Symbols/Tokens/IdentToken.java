package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class IdentToken extends Token<String> {
    public IdentToken(Position start, Position follow, String value) {
        super(TokenTag.IDENTIFIER, start, follow, value);
    }
}
