package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class QualifiedNameVar extends Var {
    public QualifiedNameVar() {
        super(VarTag.QUALIFIED_NAME);
    }
}
