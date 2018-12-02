package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class TableElementVar extends Var {
    public TableElementVar() {
        super(VarTag.TABLE_ELEMENT);
    }
}
