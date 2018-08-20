package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class FuncArgWithDefaultVar extends Var {
    private static int version = 0;

    public FuncArgWithDefaultVar() {
        super(VarTag.FUNC_ARG_WITH_DEFAULT);
    }

    public String getJavaType() {
        FuncArgVar funcArgVar = (FuncArgVar) this.get(0);
        if (funcArgVar.get(0).getTag() == VarTag.TYPENAME)
            return ((TypenameVar) funcArgVar.get(0)).getJavaType();
        else
            return ((TypenameVar) funcArgVar.get(funcArgVar.size() - 1)).getJavaType();
    }

    public String getName() {
        FuncArgVar funcArgVar = (FuncArgVar) this.get(0);
        if (funcArgVar.get(0).getTag() == TokenTag.IDENTIFIER) {
            return ((IdentToken) funcArgVar.get(0)).getValue();
        } else if (funcArgVar.size() == 1) {
            return getJavaType() + version++;
        } else if (funcArgVar.get(1).getTag() == TokenTag.IDENTIFIER) {
            return ((IdentToken) funcArgVar.get(1)).getValue();
        } else {
            return getJavaType() + version++;
        }
    }

    public boolean isDefault() {
        return this.size() == 2;
    }
}
