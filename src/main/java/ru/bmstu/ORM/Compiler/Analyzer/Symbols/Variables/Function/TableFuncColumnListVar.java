package ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Function;

import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.VarTag;

import java.util.LinkedHashMap;

public class TableFuncColumnListVar extends Var {
    private LinkedHashMap<String, TypenameVar> table;

    public TableFuncColumnListVar() {
        super(VarTag.TABLE_FUNC_COLUMN_LIST);
        this.table = new LinkedHashMap<>();
    }

    public void addColumn(String name, TypenameVar typename) {
        if (table.containsKey(name))
            throw new RuntimeException("Column " + name + " already exists");
        table.put(name, typename);
    }

    public LinkedHashMap<String, TypenameVar> getTable() {
        return table;
    }
}
