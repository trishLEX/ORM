package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ColConstraintElemVar extends Var {
    public ColConstraintElemVar() {
        super(VarTag.COL_CONSTRAINT_ELEM);
    }
}
