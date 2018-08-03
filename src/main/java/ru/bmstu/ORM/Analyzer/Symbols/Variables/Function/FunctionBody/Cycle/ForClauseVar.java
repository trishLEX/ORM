package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Cycle;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ForClauseVar extends Var {
    public ForClauseVar() {
        super(VarTag.FOR_CLAUSE);
    }
}
