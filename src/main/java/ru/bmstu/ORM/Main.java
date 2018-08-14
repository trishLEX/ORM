package ru.bmstu.ORM;

import ru.bmstu.ORM.Analyzer.Lexer.Message;
import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Parser.Parser;
import ru.bmstu.ORM.Analyzer.Semantics.SemanticAnalyzer;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Service.Session.Session;
import ru.bmstu.ORM.Service.Session.SessionFactory;
import ru.bmstu.ORM.Service.Tables.Employee;
import ru.bmstu.ORM.Service.Tables.Shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String PATH = "E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\resources\\TestFile.txt";

    public static void main(String[] args) throws IOException, CloneNotSupportedException, SQLException {
        String program = new String(Files.readAllBytes(Paths.get(PATH)));

        Scanner scanner = new Scanner(program);

        ArrayList<Token> tokens = new ArrayList<>();

        Token t = scanner.nextToken();

        while (t.getTag() != TokenTag.END_OF_PROGRAM) {
            tokens.add(t);
            t = scanner.nextToken();
            //System.out.println(t);
        }

//        for (Token token: tokens)
//            System.out.println(token);

        for (Message msg: scanner.getMessages())
            System.out.println("ERROR: " + msg);

        Parser parser = new Parser(new Scanner(program));
        SVar start = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer(parser.getTables());
        analyzer.analyze(start);

        System.out.println(start);

        SessionFactory sessionFactory = new SessionFactory("localhost", "5432", "postgres", "shopdb", "0212");
        Session session = sessionFactory.openSession();

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
