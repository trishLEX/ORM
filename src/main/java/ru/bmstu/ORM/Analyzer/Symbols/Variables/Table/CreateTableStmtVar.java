package ru.bmstu.ORM.Analyzer.Symbols.Variables.Table;

import ru.bmstu.ORM.Analyzer.Symbols.SymbolType;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
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
    private HashMap<IdentToken, ColumnDefVar> columns;
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
        if (columns.containsKey(columnDef))
            throw new RuntimeException("Column " + columnDef.get(0) + " already exists");
        columns.put((IdentToken) columnDef.get(0), columnDef);
    }

    public SymbolType getTypeOfColumn(IdentToken colId) {
        TypenameVar typename = (TypenameVar) columns.get(colId).get(1);

        if (typename.size() == 2) {
            return TokenTag.ARRAY;
        } else {
            SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) typename.get(0);
            return simpleTypeName.get(0).getTag();
        }
    }

    public Collection<ColumnDefVar> getColumns() {
        return columns.values();
    }

    public boolean containsColumn(IdentToken colId) {
        return columns.containsKey(colId);
    }

    public ArrayList<TableConstraintVar> getTableConstraints() {
        return tableConstraints;
    }

    public void addTableConstraint(TableConstraintVar constraint) {
        tableConstraints.add(constraint);
    }

    @Override
    public String toString() {
        return this.getTag() + " " + getCoords() + ": " + tableName;
    }

    public SymbolType getFullTypeOfColumn(IdentToken colId) {
        TypenameVar typename = (TypenameVar) columns.get(colId).get(1);
        if (typename.size() == 2) {
            return TokenTag.ARRAY;
        } else {
            SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) typename.get(0);
            return simpleTypeName.getFullType();
        }
    }
}
