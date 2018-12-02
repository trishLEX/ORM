package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.DateTimeCastVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression.ArithmConstExprVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.BoolConstVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

public class ConstExprVar extends Var {
    private String javaValue;

    public ConstExprVar() {
        super(VarTag.CONST_EXPR);
    }

    public String getJavaValue() {
        javaValue = "";
        if (get(0).getTag() == VarTag.ARITHM_CONST_EXPR) {
            javaValue = ((ArithmConstExprVar) get(0)).getJavaValue();
            return javaValue;
        } else if (get(0).getTag() == TokenTag.NOT || get(0).getTag() == VarTag.BOOL_CONST) {
            if (get(0).getTag() == TokenTag.NOT) {
                javaValue = "NOT " + ((Token) ((BoolConstVar) get(1)).get(0)).getStringValue();
            } else {
                javaValue = ((Token) ((BoolConstVar) get(0)).get(0)).getStringValue();
            }
        } else if (get(0).getTag() == TokenTag.STRING_CONST) {
            javaValue = '"' + ((Token) get(0)).getStringValue() + '"';
        } else {
            javaValue = ((DateTimeCastVar) get(0)).getJavaValue();
        }

        return javaValue;
    }
}
