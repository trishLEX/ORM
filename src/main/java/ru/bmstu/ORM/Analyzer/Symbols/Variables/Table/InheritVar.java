package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class InheritVar extends Var {
    public InheritVar() {
        super(VarTag.INHERIT);
    }
}
