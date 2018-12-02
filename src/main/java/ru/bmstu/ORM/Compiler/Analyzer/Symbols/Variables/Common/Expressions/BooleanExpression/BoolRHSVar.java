package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class BoolRHSVar extends Var {
    public BoolRHSVar() {
        super(VarTag.BOOL_RHS);
    }
}
