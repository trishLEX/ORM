package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class CommaToken extends Token<Character> {
    public CommaToken(Position start, Position follow) {
        super(TokenTag.COMMA, start, follow, ',');
    }
}
