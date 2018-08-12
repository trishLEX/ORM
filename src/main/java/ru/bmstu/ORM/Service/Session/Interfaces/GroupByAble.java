package ru.bmstu.ORM.Service.Session.Interfaces;

import ru.bmstu.ORM.Service.Session.Clauses.GroupByClause;
import ru.bmstu.ORM.Service.Tables.Entity;

public interface GroupByAble<T extends Entity>  {
    GroupByClause<T> groupBy(String groupByClause);
}
