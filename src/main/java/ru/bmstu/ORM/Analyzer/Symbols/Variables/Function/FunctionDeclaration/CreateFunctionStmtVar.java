package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration;

import ru.bmstu.ORM.Analyzer.Semantics.Types;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateFunctionStmtVar extends Var {
    private QualifiedNameVar functionName;
    private boolean returnsTable;
    private HashMap<IdentToken, FuncArgWithDefaultVar> parameters;
    private ArrayList<TypenameVar> returnedTypes;
    public CreateFunctionStmtVar() {
        super(VarTag.CREATE_FUNCTION_STMT);
        returnsTable = false;
        parameters = new HashMap<>();
        returnedTypes = new ArrayList<>();
    }

    public QualifiedNameVar getFunctionName() {
        return functionName;
    }

    public void setFunctionName(QualifiedNameVar functionName) {
        this.functionName = functionName;
    }

    public boolean isReturnsTable() {
        return returnsTable;
    }

    public void setReturnsTable(boolean returnsTable) {
        this.returnsTable = returnsTable;
    }

    public void addParameter(FuncArgWithDefaultVar funcArgWithDefault) {
        parameters.put((IdentToken) ((FuncArgVar) funcArgWithDefault.get(0)).get(0), funcArgWithDefault);
    }

    public Types getType(IdentToken ident) {
        FuncArgVar funcArg = (FuncArgVar) parameters.get(ident).get(0);
        TypenameVar typename = (TypenameVar) funcArg.get(funcArg.getSymbols().size() - 1);
        if (typename.getSymbols().size() > 1) {
            return Types.ARRAY;
        } else {
            SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) typename.get(0);
            if (simpleTypeName.get(0).getTag() == TokenTag.RECORD)
                return Types.RECORD;
            else if (simpleTypeName.get(0).getTag() == TokenTag.BOOLEAN)
                return Types.BOOLEAN;
            else if (simpleTypeName.get(0).getTag() == VarTag.NUMERIC_TYPE) {
                if (simpleTypeName.get(0).getTag() == TokenTag.INT
                        || simpleTypeName.get(0).getTag() == TokenTag.INTEGER)
                    return Types.INT;
                else if (simpleTypeName.get(0).getTag() == TokenTag.SMALLINT)
                    return Types.SHORT;
                else if (simpleTypeName.get(0).getTag() == TokenTag.BIGINT)
                    return Types.LONG;
                else if (simpleTypeName.get(0).getTag() == TokenTag.REAL
                        || simpleTypeName.get(0).getTag() == TokenTag.FLOAT)
                    return Types.FLOAT;
                else if (simpleTypeName.get(0).getTag() == TokenTag.DOUBLE)
                    return Types.DOUBLE;
                else
                    return Types.INT;
            } else if (simpleTypeName.get(0).getTag() == VarTag.CHARACTER_TYPE)
                return Types.STRING;
            else {
                if (simpleTypeName.get(0).getTag() == TokenTag.TIMESTAMP)
                    return Types.TIMESTAMP;
                else if (simpleTypeName.get(0).getTag() == TokenTag.TIME)
                    return Types.TIME;
                else
                    return Types.DATE;
            }
        }
    }

    public void addReturnedType(TypenameVar typename) {
        returnedTypes.add(typename);
    }

    public TypenameVar getReturnedType(int i) {
        return returnedTypes.get(i);
    }

    public HashMap<IdentToken, TypenameVar> getTypedParameters() {
        HashMap<IdentToken, TypenameVar> result = new HashMap<>();
        for (Map.Entry<IdentToken, FuncArgWithDefaultVar> entry: parameters.entrySet()) {
            FuncArgVar funcArg = (FuncArgVar) entry.getValue().get(0);
            TypenameVar typename = (TypenameVar) funcArg.get(funcArg.getSymbols().size() - 1);
            result.put(entry.getKey(), typename);
        }

        return result;
    }

    public ArrayList<TypenameVar> getReturnedTypes() {
        return returnedTypes;
    }
}
