package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ColumnDefVar extends Var {
    public ColumnDefVar() {
        super(VarTag.COLUMN_DEF);
    }
}
