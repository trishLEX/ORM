package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class JoinedTableVar extends Var {
    public JoinedTableVar() {
        super(VarTag.JOINED_TABLE);
    }
}
