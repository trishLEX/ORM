package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class FuncArgWithDefaultVar extends Var {
    public FuncArgWithDefaultVar() {
        super(VarTag.FUNC_ARG_WITH_DEFAULT);
    }
}
