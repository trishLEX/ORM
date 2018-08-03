package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class WhereClauseVar extends Var {
    public WhereClauseVar() {
        super(VarTag.WHERE_CLAUSE);
    }
}
