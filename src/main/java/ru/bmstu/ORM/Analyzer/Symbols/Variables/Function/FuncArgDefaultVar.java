package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class FuncArgDefaultVar extends Var {
    public FuncArgDefaultVar() {
        super(VarTag.FUNC_ARG_DEFAULT);
    }
}
