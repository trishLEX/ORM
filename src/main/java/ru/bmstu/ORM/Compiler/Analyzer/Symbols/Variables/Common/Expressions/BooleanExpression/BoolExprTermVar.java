package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class BoolExprTermVar extends Var {
    public BoolExprTermVar() {
        super(VarTag.BOOL_EXPR_TERM);
    }
}
