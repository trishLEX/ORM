package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class SemicolonToken extends Token<Character> {
    public SemicolonToken(Position start, Position follow) {
        super(TokenTag.SEMICOLON, start, follow, ';');
    }
}
