package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class BoolConstVar extends Var {
    public BoolConstVar() {
        super(VarTag.BOOL_CONST);
    }
}
