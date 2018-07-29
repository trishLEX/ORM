package ru.bmstu.ORM.Analyzer.Symbols.Variables.Trigger;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class CreateTriggerStmtVar extends Var {
    public CreateTriggerStmtVar() {
        super(VarTag.CREATE_TRIGGER_STMT);
    }
}
