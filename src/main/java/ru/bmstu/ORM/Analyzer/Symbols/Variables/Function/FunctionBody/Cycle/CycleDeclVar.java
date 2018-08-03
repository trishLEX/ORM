package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Cycle;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class CycleDeclVar extends Var {
    public CycleDeclVar() {
        super(VarTag.CYCLE_DECL);
    }
}
