package ru.bmstu.ORM;

import ru.bmstu.ORM.Service.Session.Session;
import ru.bmstu.ORM.Service.Session.SessionFactory;
import ru.bmstu.ORM.Service.Functions.Functions;
import ru.bmstu.ORM.Service.Tables.Employee;
import ru.bmstu.ORM.Service.Tables.Shop;

import java.util.List;

public class Main {
    private static final String PATH = "E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\resources\\TestFile.txt";

    public static void main(String[] args) {
        SessionFactory sessionFactory = new SessionFactory("localhost", "5432", "postgres", "shopdb", "0212");
        Session session = sessionFactory.openSession();
        Functions funcs = session.getFunctions(Functions.class);
        System.out.println(funcs.go());
        System.out.println(funcs.a(1));
        System.out.println(funcs.a());

        List<Shop> shop = session.selectFrom(Shop.class).where("shopCode = 101").fetchAll();
        System.out.println(shop);
        Employee employee = session.selectFrom(Employee.class).fetchFirst();
        System.out.println(employee);
        System.out.println(employee.getShop());
        System.out.println(session.contains(employee));
        employee.getInts().add(10);
        session.update(employee);
        session.close();
    }
}
