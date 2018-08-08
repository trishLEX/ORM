package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class FuncArgVar extends Var {
    public FuncArgVar() {
        super(VarTag.FUNC_ARG);
    }
}
