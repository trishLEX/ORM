package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CreateFunctionStmtVar extends Var {
    private TypenameVar returnedType;
    private LinkedHashMap<String, TypenameVar> returnedTable;
    private ArrayList<FuncArgWithDefaultVar> args;
    private QualifiedNameVar functionName;

    public CreateFunctionStmtVar() {
        super(VarTag.CREATE_FUNCTION_STMT);
        args = new ArrayList<>();
    }

    public LinkedHashMap<String, TypenameVar> getReturnedTable() {
        return returnedTable;
    }

    public void setReturnedTable(LinkedHashMap<String, TypenameVar> returnedTable) {
        this.returnedTable = returnedTable;
    }

    public ArrayList<FuncArgWithDefaultVar> getArgs() {
        return args;
    }

    public FuncArgWithDefaultVar getArg(int i) {
        return args.get(i);
    }

    public void addArg(FuncArgWithDefaultVar arg) {
        this.args.add(arg);
    }

    public TypenameVar getReturnedType() {
        return returnedType;
    }

    public void setReturnedType(TypenameVar returnedType) {
        this.returnedType = returnedType;
    }

    public String getReturnedJavaType() {
        if (returnedType == null && returnedTable == null) {
            return "void";
        } else if (returnedType != null) {
            return returnedType.getJavaType();
        } else {
            return String.format("ArrayList<%sFunctionTable>", getName());
        }
    }

    public String getReturnedTableName() {
        if (returnedTable == null)
            return null;
        else
            return String.format("%sFunctionTable", getName());
    }

    public QualifiedNameVar getFunctionName() {
        return functionName;
    }

    public void setFunctionName(QualifiedNameVar functionName) {
        this.functionName = functionName;
    }

    public String getCatalog() {
        return this.functionName.getCatalog();
    }

    public String getSchema() {
        return this.functionName.getSchema();
    }

    public String getName() {
        return this.functionName.getName();
    }
}
