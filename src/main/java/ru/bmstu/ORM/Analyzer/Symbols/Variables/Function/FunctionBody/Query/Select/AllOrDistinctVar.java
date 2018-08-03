package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class AllOrDistinctVar extends Var {
    public AllOrDistinctVar() {
        super(VarTag.ALL_OR_DISTINCT);
    }
}
