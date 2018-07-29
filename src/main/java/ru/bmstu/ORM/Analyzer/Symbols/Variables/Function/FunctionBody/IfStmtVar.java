package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class IfStmtVar extends Var {
    public IfStmtVar() {
        super(VarTag.IF_STMT);
    }
}
