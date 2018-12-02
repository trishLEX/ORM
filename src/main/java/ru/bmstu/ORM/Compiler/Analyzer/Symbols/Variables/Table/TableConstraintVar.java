package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class TableConstraintVar extends Var {
    public TableConstraintVar() {
        super(VarTag.TABLE_CONSTRAINT);
    }
}
