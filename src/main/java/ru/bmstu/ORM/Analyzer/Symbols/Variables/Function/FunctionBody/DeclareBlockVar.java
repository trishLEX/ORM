package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class DeclareBlockVar extends Var {
    public DeclareBlockVar() {
        super(VarTag.DECLARE_BLOCK);
    }
}
