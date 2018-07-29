package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class TableConstraintVar extends Var {
    public TableConstraintVar() {
        super(VarTag.TABLE_CONSTRAINT);
    }
}
