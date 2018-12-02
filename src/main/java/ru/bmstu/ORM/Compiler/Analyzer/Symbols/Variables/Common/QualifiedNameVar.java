package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

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

    public IdentToken getLastColId() {
        return (IdentToken) get(getSymbols().size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder name = new StringBuilder();
        for (Symbol s: getSymbols()) {
            if (s.getTag() == TokenTag.IDENTIFIER) {
                name.append(((IdentToken) s).getValue());
            } else {
                name.append('.');
            }
        }

        return getTag() + " " + name + " " + getCoords();
    }

    public String getCatalog() {
        if (this.size() == 5) {
            String catalog = ((IdentToken)this.get(0)).getValue();
            return Character.toUpperCase(catalog.charAt(0)) + catalog.substring(1);
        } else {
            return "Postgres";
        }
    }

    public String getSchema() {
        if (this.size() == 5) {
            String schema = ((IdentToken) this.get(2)).getValue();
            return Character.toUpperCase(schema.charAt(0)) + schema.substring(1);
        } else if (this.size() == 3) {
            String schema = ((IdentToken)this.get(0)).getValue();
            return Character.toUpperCase(schema.charAt(0)) + schema.substring(1);
        } else {
            return "Public";
        }
    }

    public String getName() {
        String name = ((IdentToken) this.get(this.size() - 1)).getValue();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
