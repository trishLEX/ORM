package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ColConstraintVar extends Var {
    public ColConstraintVar() {
        super(VarTag.COL_CONSTRAINT);
    }
}
