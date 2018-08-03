package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Return;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ReturnedValueVar extends Var {
    public ReturnedValueVar() {
        super(VarTag.RETURNED_VALUE);
    }
}
