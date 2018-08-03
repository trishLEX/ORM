package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Insert;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class InsertedValueVar extends Var {
    public InsertedValueVar() {
        super(VarTag.INSERTED_VALUE);
    }
}
