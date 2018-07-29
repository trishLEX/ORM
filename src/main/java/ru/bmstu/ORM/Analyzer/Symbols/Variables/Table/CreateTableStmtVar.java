package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class CreateTableStmtVar extends Var {
    public CreateTableStmtVar() {
        super(VarTag.CREATE_TABLE_STMT);
    }
}
