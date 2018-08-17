package ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types;

import ru.bmstu.ORM.Analyzer.Symbols.Tokens.NumberToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class TypenameVar extends Var {
    public TypenameVar() {
        super(VarTag.TYPENAME);
    }

    public String getJavaType() {
        boolean isArray = false;
        if (this.size() == 2)
            isArray = true;

        SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) this.get(0);
        if (simpleTypeName.get(0).getTag() == TokenTag.BOOLEAN) {
            if (isArray)
                return "ArrayList<Boolean>";
            else
                return "Boolean";
        } else if (simpleTypeName.get(0).getTag() == VarTag.NUMERIC_TYPE) {
            NumericTypeVar numericType = (NumericTypeVar) simpleTypeName.get(0);
            if (numericType.get(0).getTag() == TokenTag.INT
                    || numericType.get(0).getTag() == TokenTag.INTEGER) {
                if (isArray)
                    return "ArrayList<Integer>";
                else
                    return "Integer";
            } else if (numericType.get(0).getTag() == TokenTag.SMALLINT) {
                if (isArray)
                    return "ArrayList<Short>";
                else
                    return "Short";
            } else if (numericType.get(0).getTag() == TokenTag.BIGINT) {
                if (isArray)
                    return "ArrayList<Long>";
                else
                    return "Long";
            } else if (numericType.get(0).getTag() == TokenTag.REAL) {
                if (isArray)
                    return "ArrayList<Float>";
                else
                    return "Float";
            } else if (numericType.get(0).getTag() == TokenTag.FLOAT) {
                if (numericType.size() > 1) {
                    if (((NumberToken) numericType.get(2)).getValue().intValue() < 25) {
                        if (isArray)
                            return "ArrayList<Float>";
                        else
                            return "Float";
                    } else {
                        if (isArray)
                            return "ArrayList<Double>";
                        else
                            return "Double";
                    }
                } else {
                    if (isArray)
                        return "ArrayList<Double>";
                    else
                        return "Double";
                }
            } else if (numericType.get(0).getTag() == TokenTag.DOUBLE) {
                if (isArray)
                    return "ArrayList<Double>";
                else
                    return "Double";
            } else {
                if (isArray)
                    return "ArrayList<Float>";
                else
                    return "Float";
            }
        } else if (simpleTypeName.get(0).getTag() == VarTag.CHARACTER_TYPE) {
            if (isArray)
                return "ArrayList<String>";
            else
                return "String";
        } else {
            DateTimeTypeVar dateTimeType = (DateTimeTypeVar) simpleTypeName.get(0);
            if (dateTimeType.get(0).getTag() == TokenTag.TIMESTAMP) {
                if (isArray)
                    return "ArrayList<Timestamp>";
                else
                    return "Timestamp";
            } else if (dateTimeType.get(0).getTag() == TokenTag.TIME) {
                if (isArray)
                    return "ArrayList<Time>";
                else
                    return "Time";
            } else {
                if (isArray)
                    return "ArrayList<Date>";
                else
                    return "Date";
            }
        }
    }
}
