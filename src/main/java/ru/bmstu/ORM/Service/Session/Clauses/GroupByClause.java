package ru.bmstu.ORM.Service.Session.Clauses;

import ru.bmstu.ORM.Service.Session.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Session.Interfaces.HavingAble;
import ru.bmstu.ORM.Service.Session.Interfaces.OrderByAble;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.sql.Connection;

public class GroupByClause<T extends Entity> extends Fetchable<T> implements HavingAble<T>, OrderByAble<T> {
    GroupByClause(Connection connection, Class<T> tableClass, String selectStmt, String groupByClause) {
        super(connection, tableClass, selectStmt + " GROUP BY" + groupByClause);
    }

    @Override
    public HavingClause<T> having(String havingClause) {
        return new HavingClause<>(getConnection(), getTableClass(), getSelectStmt(), havingClause);
    }

    @Override
    public OrderByClause<T> orderBy(String orderByClause) {
        return new OrderByClause<>(getConnection(), getTableClass(), getSelectStmt(), orderByClause);
    }
}
