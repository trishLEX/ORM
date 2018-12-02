package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class BoolExprFactorVar extends Var {
    public BoolExprFactorVar() {
        super(VarTag.BOOL_EXPR_FACTOR);
    }
}
