package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class FuncAsVar extends Var {
    public FuncAsVar() {
        super(VarTag.FUNC_AS);
    }
}
