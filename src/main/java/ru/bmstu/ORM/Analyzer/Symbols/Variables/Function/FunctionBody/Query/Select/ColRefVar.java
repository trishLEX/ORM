package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ColRefVar extends Var {
    public ColRefVar() {
        super(VarTag.COL_REF);
    }
}
