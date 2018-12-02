package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class NumericTypeVar extends Var {
    public NumericTypeVar() {
        super(VarTag.NUMERIC_TYPE);
    }
}
