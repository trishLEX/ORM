package ru.bmstu.ORM.Service.Statements.Interfaces;

import ru.bmstu.ORM.Service.Statements.Clauses.OrderByClause;
import ru.bmstu.ORM.Tables.Entity;

public interface OrderByAble<T extends Entity> {
    OrderByClause<T> orderBy(String orderByClause);
}
