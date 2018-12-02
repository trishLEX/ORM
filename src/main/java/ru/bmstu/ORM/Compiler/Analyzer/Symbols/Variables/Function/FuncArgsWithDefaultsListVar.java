package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class FuncArgsWithDefaultsListVar extends Var {
    public FuncArgsWithDefaultsListVar() {
        super(VarTag.FUNC_ARGS_WITH_DEFAULTS_LIST);
    }
}
