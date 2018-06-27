package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class RParenToken extends Token<Character> {
    public RParenToken(Position start, Position follow) {
        super(TokenTag.RPAREN, start, follow, ')');
    }
}
