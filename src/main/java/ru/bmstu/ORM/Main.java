package ru.bmstu.ORM;

import ru.bmstu.ORM.Analyzer.Lexer.Message;
import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Parser.Parser;
import ru.bmstu.ORM.Analyzer.Semantics.SemanticAnalyzer;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Service.Session.Session;
import ru.bmstu.ORM.Tables.Employee;
import ru.bmstu.ORM.Tables.Shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;

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

        Session session = new Session("localhost", "5432", "postgres", "shopdb", "0212");
        session.open();
        Shop shop = (Shop) session.selectFrom(Shop.class).where("shopCode = 100").fetchFirst();
        System.out.println(shop);
        Employee employee = (Employee) session.selectFrom(Employee.class).fetchFirst();
        System.out.println(employee);
        System.out.println(employee.getShop());
        session.close();
    }
}
