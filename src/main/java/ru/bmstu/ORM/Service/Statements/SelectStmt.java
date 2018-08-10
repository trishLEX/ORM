package ru.bmstu.ORM.Service.Statements;

import ru.bmstu.ORM.Service.Annotations.Table;
import ru.bmstu.ORM.Service.Statements.Clauses.GroupByClause;
import ru.bmstu.ORM.Service.Statements.Clauses.OrderByClause;
import ru.bmstu.ORM.Service.Statements.Clauses.WhereClause;
import ru.bmstu.ORM.Service.Statements.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Statements.Interfaces.GroupByAble;
import ru.bmstu.ORM.Service.Statements.Interfaces.OrderByAble;
import ru.bmstu.ORM.Service.Statements.Interfaces.WhereAble;
import ru.bmstu.ORM.Tables.Entity;

import java.sql.Connection;

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
}
