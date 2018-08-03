package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class HavingClauseVar extends Var {
    public HavingClauseVar() {
        super(VarTag.HAVING_CLAUSE);
    }
}
