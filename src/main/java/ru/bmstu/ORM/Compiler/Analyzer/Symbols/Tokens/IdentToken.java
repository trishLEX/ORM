package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

public class IdentToken extends Token<String> {
    public IdentToken(Position start, Position follow, String value) {
        super(TokenTag.IDENTIFIER, start, follow, value);
    }
}
