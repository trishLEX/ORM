package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class CreateTableFunctionTriggerVar extends Var {
    public CreateTableFunctionTriggerVar() {
        super(VarTag.CREATE_TABLE_FUNCTION_TRIGGER_STMT);
    }
}
