package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class BoolConstExprVar extends Var {
    public BoolConstExprVar() {
        super(VarTag.BOOL_CONST_EXPR);
    }
}
