package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.NumberToken;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types.DateTimeTypeVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types.NumericTypeVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

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
        return ((TypenameVar) get(1)).getJavaType();
    }
}
