package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Semantics.Types;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ColumnDefVar extends Var {
    private Types type;

    public ColumnDefVar() {
        super(VarTag.COLUMN_DEF);
    }

    public void setType(Types type) {
        this.type = type;
    }

    public Types getType() {
        return type;
    }
}
