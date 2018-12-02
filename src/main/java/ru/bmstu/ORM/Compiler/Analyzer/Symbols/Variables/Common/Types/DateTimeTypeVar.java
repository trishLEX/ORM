package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class DateTimeTypeVar extends Var {
    public DateTimeTypeVar() {
        super(VarTag.DATETIME_TYPE);
    }
}
