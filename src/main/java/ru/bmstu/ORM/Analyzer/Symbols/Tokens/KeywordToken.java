package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class KeywordToken extends Token<String> {
    public KeywordToken(Position start, Position follow, TokenTag tag) {
        super(tag, start, follow, tag.toString());
    }
}
