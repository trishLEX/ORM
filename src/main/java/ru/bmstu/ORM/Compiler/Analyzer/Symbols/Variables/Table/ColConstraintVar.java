package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ColConstraintVar extends Var {
    public ColConstraintVar() {
        super(VarTag.COL_CONSTRAINT);
    }
}
