package ru.bmstu.ORM;

import ru.bmstu.ORM.Analyzer.Lexer.Message;
import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Parser.Parser;
import ru.bmstu.ORM.Analyzer.Semantics.SemanticAnalyzer;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Codegen.CodeGenerator;
import ru.bmstu.ORM.Service.Session.Session;
import ru.bmstu.ORM.Service.Session.SessionFactory;
import ru.bmstu.ORM.Service.Tables.Employee;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Main {
    private static final String PATH = "E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\resources\\TestFile.txt";

    public static void main(String[] args) throws IOException, CloneNotSupportedException, SQLException, ParseException {
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

        DateFormat dateOnlyISO = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(dateOnlyISO.parse("11-12-2011").getTime());
        System.out.println(date.toString());

        DateFormat dfISO = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
        Timestamp timestamp = new Timestamp(dfISO.parse("11-12-2011T10:13:14").getTime());
        System.out.println(timestamp.toString());

        DateFormat timeOnlyISO = new SimpleDateFormat("HH:mm:ss");
        Time time = new Time(timeOnlyISO.parse("11:12:13").getTime());
        System.out.println(time.toString());

        CodeGenerator codeGenerator = new CodeGenerator(start, "E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\test");
        codeGenerator.generateFiles();

        //SessionFactory sessionFactory = new SessionFactory("localhost", "5432", "postgres", "shopdb", "0212");
        //Session session = sessionFactory.openSession();
//
//        List<Shop> shop = session.selectFrom(Shop.class).where("shopCode = 101").fetchAll();
//        System.out.println(shop);
        //Employee employee = session.selectFrom(Employee.class).fetchFirst();
        //System.out.println(employee);
//        System.out.println(employee.getShop());
//        System.out.println(session.contains(employee));
//        employee.getInts().add(10);
//        session.update(employee);
//        session.close();
    }
}
