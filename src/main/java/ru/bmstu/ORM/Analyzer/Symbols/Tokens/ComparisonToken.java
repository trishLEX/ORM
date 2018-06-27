package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class ComparisonToken extends Token<String> {
    public ComparisonToken(TokenTag tag, Position start, Position follow, String value) {
        super(tag, start, follow, value);
    }
}
