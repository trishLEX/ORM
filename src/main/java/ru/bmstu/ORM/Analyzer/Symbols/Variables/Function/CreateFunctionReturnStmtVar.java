package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.LinkedHashMap;


public class CreateFunctionReturnStmtVar extends Var {
    private LinkedHashMap<String, TypenameVar> table;
    private TypenameVar returnedType;

    public CreateFunctionReturnStmtVar() {
        super(VarTag.CREATE_FUNCTION_RETURN_STMT);
    }

    public LinkedHashMap<String, TypenameVar> getTable() {
        return table;
    }

    public void setTable(LinkedHashMap<String, TypenameVar> table) {
        this.table = table;
    }

    public TypenameVar getReturnedType() {
        return returnedType;
    }

    public void setReturnedType(TypenameVar returnedType) {
        this.returnedType = returnedType;
    }
}