package ru.bmstu.ORM.Analyzer.Symbols.Tokens;

import ru.bmstu.ORM.Analyzer.Service.Position;

import java.util.Date;

public class DateTimeToken extends Token<Date> {
    public DateTimeToken(Position start, Position follow, Date value) {
        super(TokenTag.DATE_TIME_CONST, start, follow, value);
    }
}
