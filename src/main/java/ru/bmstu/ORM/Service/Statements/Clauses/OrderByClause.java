package ru.bmstu.ORM.Service.Statements.Clauses;

import ru.bmstu.ORM.Service.Statements.Interfaces.Fetchable;
import ru.bmstu.ORM.Tables.Entity;

import java.sql.Connection;

public class OrderByClause<T extends Entity> extends Fetchable<T> {
    public OrderByClause(Connection connection, Class<T> tableClass, String selectStmt, String orderByClause) {
        super(connection, tableClass, selectStmt + " ORDER BY " + orderByClause);

    }
}
