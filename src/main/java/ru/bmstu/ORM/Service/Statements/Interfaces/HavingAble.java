package ru.bmstu.ORM.Service.Statements.Interfaces;

import ru.bmstu.ORM.Service.Statements.Clauses.HavingClause;
import ru.bmstu.ORM.Tables.Entity;

public interface HavingAble<T extends Entity> {
    HavingClause<T> having(String havingClause);
}
