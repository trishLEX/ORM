package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class CycleBodyVar extends Var {
    public CycleBodyVar() {
        super(VarTag.CYCLE_BODY);
    }
}
