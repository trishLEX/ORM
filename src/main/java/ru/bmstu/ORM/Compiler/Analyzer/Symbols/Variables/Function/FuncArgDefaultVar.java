package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class FuncArgDefaultVar extends Var {
    public FuncArgDefaultVar() {
        super(VarTag.FUNC_ARG_DEFAULT);
    }
}
