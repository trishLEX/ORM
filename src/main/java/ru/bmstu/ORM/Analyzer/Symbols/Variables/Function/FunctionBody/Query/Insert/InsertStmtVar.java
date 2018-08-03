package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Insert;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class InsertStmtVar extends Var {
    public InsertStmtVar() {
        super(VarTag.INSERT_STMT);
    }
}
