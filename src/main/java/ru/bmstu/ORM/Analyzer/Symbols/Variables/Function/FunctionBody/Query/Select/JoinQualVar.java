package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class JoinQualVar extends Var {
    public JoinQualVar() {
        super(VarTag.JOIN_QUAL);
    }
}
