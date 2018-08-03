package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Cycle;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class IfCycleBodyVar extends Var {
    public IfCycleBodyVar() {
        super(VarTag.IF_CYCLE_BODY);
    }
}
