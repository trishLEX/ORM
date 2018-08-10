package ru.bmstu.ORM.Service.Statements.Clauses;

import ru.bmstu.ORM.Service.Statements.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Statements.Interfaces.GroupByAble;
import ru.bmstu.ORM.Service.Statements.Interfaces.OrderByAble;
import ru.bmstu.ORM.Tables.Entity;

import java.sql.Connection;

public class WhereClause<T extends Entity> extends Fetchable<T> implements GroupByAble, OrderByAble<T> {
    public WhereClause(Connection connection, Class<T> tableClass, String selectStmt, String whereClause) {
        super(connection, tableClass, selectStmt + " WHERE " + whereClause);
    }

    public WhereClause<T> and(String whereClause) {
        return new WhereClause<>(getConnection(), getTableClass(), getSelectStmt(), " AND " + whereClause);
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
