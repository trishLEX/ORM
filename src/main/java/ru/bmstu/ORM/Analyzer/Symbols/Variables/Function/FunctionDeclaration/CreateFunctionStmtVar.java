package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class CreateFunctionStmtVar extends Var {
    public CreateFunctionStmtVar() {
        super(VarTag.CREATE_FUNCTION_STMT);
    }
}
