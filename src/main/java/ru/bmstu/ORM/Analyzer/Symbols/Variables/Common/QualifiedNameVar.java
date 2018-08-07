package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class QualifiedNameVar extends Var {
    public QualifiedNameVar() {
        super(VarTag.QUALIFIED_NAME);
    }

    public QualifiedNameVar getWithoutLastName() {
        if (this.getSymbols().size() == 1)
            throw new RuntimeException("There is only one element");

        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        qualifiedName.setStart(this.getStart());
        qualifiedName.setFollow(this.get(getSymbols().size() - 3).getFollow());
        for (int i = 0; i < getSymbols().size() - 2; i++) {
            qualifiedName.addSymbol(this.get(i));
        }

        return qualifiedName;
    }

    public ColIdVar getLastColId() {
        return (ColIdVar) get(getSymbols().size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder name = new StringBuilder();
        for (Symbol s: getSymbols()) {
            if (s.getTag() == VarTag.COL_ID) {
                name.append(((IdentToken) ((ColIdVar)s).get(0)).getValue());
            } else {
                name.append('.');
            }
        }

        return getTag() + " " + name + " " + getCoords();
    }
}
