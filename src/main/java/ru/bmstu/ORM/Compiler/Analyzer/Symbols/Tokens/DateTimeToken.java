package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;

import java.util.Date;

public class DateTimeToken extends Token<Date> {
    public DateTimeToken(TokenTag tag, Position start, Position follow, Date value) {
        super(tag, start, follow, value);
    }
}
