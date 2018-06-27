package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class ArithmeticOpToken extends Token<Character> {
    public ArithmeticOpToken(TokenTag tag, Position start, Position follow, char value) {
        super(tag, start, follow, value);
    }
}
