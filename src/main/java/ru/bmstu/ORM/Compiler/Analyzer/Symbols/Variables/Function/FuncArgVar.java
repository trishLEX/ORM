package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class FuncArgVar extends Var {
    public FuncArgVar() {
        super(VarTag.FUNC_ARG);
    }
}
