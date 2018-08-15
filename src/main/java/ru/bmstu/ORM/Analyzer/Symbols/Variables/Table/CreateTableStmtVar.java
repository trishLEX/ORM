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
import java.util.LinkedHashMap;

public class CreateTableStmtVar extends Var {
    private QualifiedNameVar tableName;
    private LinkedHashMap<IdentToken, ColumnDefVar> columns;
    private ArrayList<TableConstraintVar> tableConstraints;
    private boolean existsPK;

    public CreateTableStmtVar() {
        super(VarTag.CREATE_TABLE_STMT);
        columns = new LinkedHashMap<>();
        tableConstraints = new ArrayList<>();
        existsPK = false;
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

    public ColumnDefVar getColumnDef(IdentToken colId) {
        return columns.get(colId);
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

    public boolean isExistsPK() {
        return existsPK;
    }

    public void setExistsPK(boolean existsPK) {
        this.existsPK = existsPK;
    }

    public String getCatalog() {
        if (tableName.size() == 5) {
            String catalog = ((IdentToken)tableName.get(0)).getValue();
            return Character.toUpperCase(catalog.charAt(0)) + catalog.substring(1);
        } else {
            return "Postgres";
        }
    }

    public String getSchema() {
        if (tableName.size() == 5) {
            String schema = ((IdentToken) tableName.get(2)).getValue();
            return Character.toUpperCase(schema.charAt(0)) + schema.substring(1);
        } else if (tableName.size() == 3) {
            String schema = ((IdentToken)tableName.get(0)).getValue();
            return Character.toUpperCase(schema.charAt(0)) + schema.substring(1);
        } else {
            return "Public";
        }
    }

    public String getName() {
        String name = ((IdentToken) tableName.get(tableName.size() - 1)).getValue();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public ArrayList<ColumnDefVar> getPKs() {
        ArrayList<ColumnDefVar> res = new ArrayList<>(columns.values());
        res.removeIf(column -> !column.isPK());
        return res;
    }
}
