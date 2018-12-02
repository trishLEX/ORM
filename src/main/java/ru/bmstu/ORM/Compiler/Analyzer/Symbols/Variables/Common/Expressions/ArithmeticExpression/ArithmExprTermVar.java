package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ArithmExprTermVar extends Var {
    public ArithmExprTermVar() {
        super(VarTag.ARITHM_EXPR_TERM);
    }
}
