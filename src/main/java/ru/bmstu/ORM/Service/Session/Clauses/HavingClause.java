package ru.bmstu.ORM.Service.Session.Clauses;

import ru.bmstu.ORM.Service.Session.Interfaces.Fetchable;
import ru.bmstu.ORM.Service.Session.Interfaces.OrderByAble;
import ru.bmstu.ORM.Service.Tables.Entity;

import java.sql.Connection;

public class HavingClause<T extends Entity> extends Fetchable<T> implements OrderByAble {
    HavingClause(Connection connection, Class<T> tableClass, String selectStmt, String havingClause) {
        super(connection, tableClass, selectStmt + " HAVING " + havingClause);
    }

    @Override
    public OrderByClause orderBy(String orderByClause) {
        return new OrderByClause<>(getConnection(), getTableClass(), getSelectStmt(), orderByClause);
    }
}
