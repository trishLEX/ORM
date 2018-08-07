package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Semantics.Types;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.ColIdVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CreateTableStmtVar extends Var {
    private QualifiedNameVar tableName;
    private HashMap<ColIdVar, ColumnDefVar> columns;
    private ArrayList<TableConstraintVar> tableConstraints;

    public CreateTableStmtVar() {
        super(VarTag.CREATE_TABLE_STMT);
        columns = new HashMap<>();
        tableConstraints = new ArrayList<>();
    }

    public QualifiedNameVar getTableName() {
        return tableName;
    }

    public void setTableName(QualifiedNameVar tableName) {
        this.tableName = tableName;
    }

    public void addColumn(ColumnDefVar columnDef) {
        columns.put((ColIdVar) columnDef.get(0), columnDef);
    }

    public Types getType(ColIdVar colId) {
        ColumnDefVar column = columns.get(colId);
        TypenameVar typename = (TypenameVar) column.get(1);
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

    public Collection<ColumnDefVar> getColumns() {
        return columns.values();
    }

    public boolean containsColumn(ColIdVar colId) {
        return columns.containsKey(colId);
    }

    public ArrayList<TableConstraintVar> getTableConstraints() {
        return tableConstraints;
    }

    public void addTableConstraint(TableConstraintVar constraint) {
        tableConstraints.add(constraint);
    }
}
