package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types;

import ru.bmstu.ORM.Analyzer.Symbols.SymbolType;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class SimpleTypeNameVar extends Var {
    public SimpleTypeNameVar() {
        super(VarTag.SIMPLE_TYPENAME);
    }

    public SymbolType getFullType() {
        if (this.get(0).getTag() == TokenTag.RECORD || this.get(0).getTag() == TokenTag.BOOLEAN)
            return this.get(0).getTag();
        else {
            return ((Var) this.get(0)).get(0).getTag();
        }
    }
}
