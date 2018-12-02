package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

public class SpecToken extends Token<String> {
    public SpecToken(TokenTag tag, Position start, Position follow, String value) {
        super(tag, start, follow, value);
    }
}
