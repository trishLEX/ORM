package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ArithmConstExprTermVar extends Var {
    private String javaValue;

    public ArithmConstExprTermVar() {
        super(VarTag.ARITHM_CONST_EXPR_TERM);
    }

    public String getJavaValue() {
        javaValue = "";
        for (Symbol s: getSymbols()) {
            if (s.getTag() == VarTag.ARITHM_CONST_FACTOR) {
                javaValue += ((ArithmConstExprFactorVar) s).getJavaValue();
            } else {
                javaValue += ((Token) s).getStringValue() + " ";
            }
        }

        return javaValue;
    }
}
