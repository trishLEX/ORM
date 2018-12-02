package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

public class KeywordToken extends Token<String> {
    public KeywordToken(Position start, Position follow, TokenTag tag) {
        super(tag, start, follow, tag.toString());
    }
}
