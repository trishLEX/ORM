package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Update;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class UpdateStmtVar extends Var {
    public UpdateStmtVar() {
        super(VarTag.UPDATE_STMT);
    }
}
