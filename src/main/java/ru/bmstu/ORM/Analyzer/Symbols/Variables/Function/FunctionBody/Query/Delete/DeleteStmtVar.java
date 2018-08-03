package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Delete;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class DeleteStmtVar extends Var {
    public DeleteStmtVar() {
        super(VarTag.DELETE_STMT);
    }
}
