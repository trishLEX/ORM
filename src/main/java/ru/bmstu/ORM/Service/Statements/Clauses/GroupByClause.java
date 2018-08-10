package ru.bmstu.ORM.Service.Statements.Clauses;

import ru.bmstu.ORM.Service.Statements.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Statements.Interfaces.HavingAble;
import ru.bmstu.ORM.Service.Statements.Interfaces.OrderByAble;
import ru.bmstu.ORM.Tables.Entity;

import java.sql.Connection;

public class GroupByClause<T extends Entity> extends Fetchable<T> implements HavingAble<T>, OrderByAble<T> {
    public GroupByClause(Connection connection, Class<T> tableClass, String selectStmt, String groupByClause) {
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
