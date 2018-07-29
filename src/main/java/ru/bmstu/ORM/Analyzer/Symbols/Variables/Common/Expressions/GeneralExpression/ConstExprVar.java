package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ConstExprVar extends Var {
    public ConstExprVar() {
        super(VarTag.CONST_EXPR);
    }
}
