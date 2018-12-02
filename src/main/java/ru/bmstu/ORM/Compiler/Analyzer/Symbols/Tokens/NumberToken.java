package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

public class NumberToken extends Token<Number> {
    public NumberToken(TokenTag tag, Position start, Position follow, Number value) {
        super(tag, start, follow, value);
    }
}
