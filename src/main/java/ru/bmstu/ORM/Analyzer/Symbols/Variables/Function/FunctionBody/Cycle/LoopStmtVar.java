package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Cycle;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class LoopStmtVar extends Var {
    public LoopStmtVar() {
        super(VarTag.LOOP_STMT);
    }
}
