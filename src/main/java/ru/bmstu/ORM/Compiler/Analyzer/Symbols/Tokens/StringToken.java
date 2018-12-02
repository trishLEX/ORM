package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

public class StringToken extends Token<String> {
    public StringToken(Position start, Position follow, String value) {
        super(TokenTag.STRING_CONST, start, follow, value);
    }
}
