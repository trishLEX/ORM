package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class RelationExpr extends Var {
    public RelationExpr() {
        super(VarTag.RELATION_EXPR);
    }
}
