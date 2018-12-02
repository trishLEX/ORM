package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class StringRHSVar extends Var {
    public StringRHSVar() {
        super(VarTag.STRING_RHS);
    }
}
