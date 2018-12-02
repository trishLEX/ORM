package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class CreateFuncBodyVar extends Var {
    public CreateFuncBodyVar() {
        super(VarTag.CREATE_FUNC_BODY);
    }
}
