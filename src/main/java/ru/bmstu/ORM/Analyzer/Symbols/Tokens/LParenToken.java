package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class LParenToken extends Token<Character> {
    public LParenToken(Position start, Position follow) {
        super(TokenTag.LPAREN, start, follow, '(');
    }
}
