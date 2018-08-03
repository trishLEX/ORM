package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class AliasClauseVar extends Var {
    public AliasClauseVar() {
        super(VarTag.ALIAS_CLAUSE);
    }
}
