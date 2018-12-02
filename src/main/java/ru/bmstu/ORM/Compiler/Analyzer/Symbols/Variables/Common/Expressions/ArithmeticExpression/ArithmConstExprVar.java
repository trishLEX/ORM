package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ArithmConstExprVar extends Var {
    private String javaValue;

    public ArithmConstExprVar() {
        super(VarTag.ARITHM_CONST_EXPR);
    }

    public String getJavaValue() {
        javaValue = "";
        for (Symbol s: getSymbols()) {
            if (s.getTag() == VarTag.ARITHM_CONST_EXPR_TERM) {
                javaValue += ((ArithmConstExprTermVar) s).getJavaValue();
            } else {
                javaValue += ((Token) s).getStringValue() + " ";
            }
        }
        return javaValue;
    }
}
