package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ReturnStmtVar extends Var {
    public ReturnStmtVar() {
        super(VarTag.RETURN_STMT);
    }
}
