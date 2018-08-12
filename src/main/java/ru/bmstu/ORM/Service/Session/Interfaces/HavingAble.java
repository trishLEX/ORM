package ru.bmstu.ORM.Service.Session.Interfaces;

import ru.bmstu.ORM.Service.Session.Clauses.HavingClause;
import ru.bmstu.ORM.Service.Tables.Entity;

public interface HavingAble<T extends Entity> {
    HavingClause<T> having(String havingClause);
}
