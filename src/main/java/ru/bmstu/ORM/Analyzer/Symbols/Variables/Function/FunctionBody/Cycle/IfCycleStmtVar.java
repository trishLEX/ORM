package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Cycle;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class IfCycleStmtVar extends Var {
    public IfCycleStmtVar() {
        super(VarTag.IF_CYCLE_STMT);
    }
}
