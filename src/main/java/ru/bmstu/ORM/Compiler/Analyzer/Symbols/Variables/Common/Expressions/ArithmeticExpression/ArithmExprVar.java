package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ArithmExprVar extends Var {
    public ArithmExprVar() {
        super(VarTag.ARITHM_EXPR);
    }
}
