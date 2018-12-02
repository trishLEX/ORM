package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ArithmConstExprFactorVar extends Var {
    private String javaValue;

    public ArithmConstExprFactorVar() {
        super(VarTag.ARITHM_CONST_FACTOR);
    }

    public String getJavaValue() {
        javaValue = "";
        for (Symbol s: getSymbols()) {
            if (s.getTag() == TokenTag.SUB) {
                javaValue += "- ";
            } else if (s.getTag() == VarTag.ARITHM_CONST_FACTOR) {
                javaValue += ((ArithmConstExprFactorVar) s).javaValue;
            } else if (s.getTag() == VarTag.ARITHM_CONST_EXPR) {
                javaValue += ((ArithmConstExprVar) s).getJavaValue();
            } else {
                javaValue += ((Token) s).getStringValue() + " ";
            }
        }

        return javaValue;
    }
}
