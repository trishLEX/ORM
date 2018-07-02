package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class RBracketToken extends Token<Character> {
    public RBracketToken(Position start, Position follow) {
        super(TokenTag.RBRACKET, start, follow, ']');
    }
}
