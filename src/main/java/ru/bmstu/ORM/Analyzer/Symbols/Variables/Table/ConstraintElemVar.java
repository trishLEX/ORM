package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ConstraintElemVar extends Var {
    public ConstraintElemVar() {
        super(VarTag.CONSTRAINT_ELEM);
    }
}
