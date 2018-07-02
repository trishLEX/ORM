package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class LBracketToken extends Token<Character> {
    public LBracketToken(Position start, Position follow) {
        super(TokenTag.LBRACKET, start, follow, '[');
    }
}
