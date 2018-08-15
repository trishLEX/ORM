package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.NumberToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.DateTimeTypeVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.NumericTypeVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

public class ColumnDefVar extends Var {
    private String isDefault;
    private Boolean isNullable;
    private boolean isUnique = false;
    private boolean isPK = false;
    private String isFK;
    private String isFO;
    private Integer length;

    public ColumnDefVar() {
        super(VarTag.COLUMN_DEF);
    }

    public String getName() {
        return ((IdentToken) get(0)).getValue().replace("\"", "");
    }

    public String getUpperName() {
        return Character.toUpperCase(getName().charAt(0)) + getName().substring(1);
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public String isDefault() {
        return isDefault;
    }

    public void setDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean isNullable() {
        return isNullable;
    }

    public void setNullable(Boolean nullable) {
        isNullable = nullable;
    }

    public boolean isPK() {
        return isPK;
    }

    public void setPK(boolean PK) {
        isPK = PK;
    }

    public String isFK() {
        return isFK;
    }

    public void setFK(String FK) {
        isFK = FK;
    }

    public String isFO() {
        return isFO;
    }

    public void setFO(String isFO) {
        this.isFO = isFO;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getJavaType() {
        TypenameVar typename = (TypenameVar) get(1);
        boolean isArray = false;
        if (typename.size() == 2)
            isArray = true;

        SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) typename.get(0);
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
