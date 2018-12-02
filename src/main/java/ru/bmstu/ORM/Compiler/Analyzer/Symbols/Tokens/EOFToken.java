package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

import static ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.TokenTag.END_OF_PROGRAM;

public class EOFToken extends Token<Character> {
    public EOFToken(Position pos) {
        super(END_OF_PROGRAM, pos, pos, (char)0xFFFFFFFF);
    }
}
