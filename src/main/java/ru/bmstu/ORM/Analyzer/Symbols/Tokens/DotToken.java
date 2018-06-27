package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class DotToken extends Token<Character> {
    public DotToken(Position start, Position follow) {
        super(TokenTag.DOT, start, follow, '.');
    }
}
