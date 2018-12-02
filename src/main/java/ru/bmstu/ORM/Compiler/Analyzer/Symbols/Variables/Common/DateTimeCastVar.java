package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.DateTimeToken;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class DateTimeCastVar extends Var {
    private String javaValue;

    public DateTimeCastVar() {
        super(VarTag.DATE_TIME_CAST);
    }

    public String getJavaValue() {
        javaValue = "";
        if (get(0).getTag() == TokenTag.DATE_CONST) {
            javaValue = String.format("new Date(new SimpleDateFormat(\"yyyy-MM-dd\").parse(\"%s\".toString()).getTime())",
                    ((DateTimeToken)get(0)).getStringValue());
        } else if (get(0).getTag() == TokenTag.TIME_CONST) {
            javaValue = String.format("new Time(new SimpleDateFormat(\"HH:mm:ss\").parse(\"%s\".toString()).getTime())",
                    ((DateTimeToken)get(0)).getStringValue());
        } else {
            javaValue = String.format("new Time(new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\").parse(\"%s\".toString()).getTime())",
                    ((DateTimeToken)get(0)).getStringValue());
        }

        return javaValue;
    }
}
