package ru.bmstu.ORM.Service.Statements.Clauses;

import ru.bmstu.ORM.Service.Statements.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Statements.Interfaces.OrderByAble;
import ru.bmstu.ORM.Tables.Entity;

import java.sql.Connection;

public class HavingClause<T extends Entity> extends Fetchable<T> implements OrderByAble {
    public HavingClause(Connection connection, Class<T> tableClass, String selectStmt, String havingClause) {
        super(connection, tableClass, selectStmt + " HAVING " + havingClause);
    }

    @Override
    public OrderByClause orderBy(String orderByClause) {
        return new OrderByClause<>(getConnection(), getTableClass(), getSelectStmt(), orderByClause);
    }
}
