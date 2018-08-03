package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Raise;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class RaiseStmtVar extends Var {
    public RaiseStmtVar() {
        super(VarTag.RAISE_STMT);
    }
}
