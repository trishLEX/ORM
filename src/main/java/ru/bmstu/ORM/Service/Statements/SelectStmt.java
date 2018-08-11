package ru.bmstu.ORM.Service.Statements;

import ru.bmstu.ORM.Service.Annotations.Column;
import ru.bmstu.ORM.Service.Annotations.PK;
import ru.bmstu.ORM.Service.Annotations.Table;
import ru.bmstu.ORM.Service.Statements.Clauses.GroupByClause;
import ru.bmstu.ORM.Service.Statements.Clauses.OrderByClause;
import ru.bmstu.ORM.Service.Statements.Clauses.WhereClause;
import ru.bmstu.ORM.Service.Statements.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Statements.Interfaces.GroupByAble;
import ru.bmstu.ORM.Service.Statements.Interfaces.OrderByAble;
import ru.bmstu.ORM.Service.Statements.Interfaces.WhereAble;
import ru.bmstu.ORM.Tables.Entity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class SelectStmt<T extends Entity> extends Fetchable<T> implements WhereAble<T>, GroupByAble<T>, OrderByAble<T> {
    public SelectStmt(Connection connection, Class<T> tableClass) {
        super(connection, tableClass);
        Table table = tableClass.getAnnotation(Table.class);

        setSelectStmt(String.format("SELECT * FROM %s.%s.%s", table.db(), table.schema(), table.name()));
    }

    @Override
    public WhereClause<T> where(String whereClause) {
        return new WhereClause<>(getConnection(), getTableClass(), getSelectStmt(), whereClause);
    }

    @Override
    public GroupByClause<T> groupBy(String groupByClause) {
        return new GroupByClause<>(getConnection(), getTableClass(), getSelectStmt(), groupByClause);
    }

    @Override
    public OrderByClause<T> orderBy(String orderByClause) {
        return new OrderByClause<>(getConnection(), getTableClass(), getSelectStmt(), orderByClause);
    }

    public T getById(Map<String, Object> id) {
        StringBuilder suffix = new StringBuilder();
        for (Map.Entry<String, Object> entry: id.entrySet()) {
            suffix.append(entry.getKey()).append(" = ").append(entry.getValue().toString());
        }
        
        return this.where(suffix.toString()).fetchFirst();
    }
    
    public T getById(Serializable id) {
        String column = "";
        int i = 0;
        for (Field field: getTableClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PK.class)) {
                column = field.getAnnotation(Column.class).name();
                i++;
            }
        }
        
        if (i != 1)
            throw new RuntimeException("Must be only one column in PK i = " + i);
        else {
            Map<String, Object> result = new HashMap<>();
            result.put(column, id);
            return getById(result);
        }
    }
}
