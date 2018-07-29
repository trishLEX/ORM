package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class VariableAssignVar extends Var {
    public VariableAssignVar() {
        super(VarTag.VAR_ASSIGN);
    }
}
