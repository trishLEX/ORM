package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

public class AssignOpToken extends Token<String> {
    public AssignOpToken(Position start, Position follow) {
        super(TokenTag.ASSIGN, start, follow, ":=");
    }
}
