package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class SelectStmtVar extends Var {
    public SelectStmtVar() {
        super(VarTag.SELECT_STMT);
    }
}
