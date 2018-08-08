package ru.bmstu.ORM;

import ru.bmstu.ORM.Analyzer.Lexer.Message;
import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Parser.Parser;
import ru.bmstu.ORM.Analyzer.Semantics.SemanticAnalyzer;
import ru.bmstu.ORM.Analyzer.Service.Position;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.DotToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.ColIdVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    private static final String PATH = "E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\java\\ru\\bmstu\\ORM\\Analyzer\\TestFile.txt";

    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        String program = new String(Files.readAllBytes(Paths.get(PATH)));

        Scanner scanner = new Scanner(program);

        ArrayList<Token> tokens = new ArrayList<>();

        Token t = scanner.nextToken();

        while (t.getTag() != TokenTag.END_OF_PROGRAM) {
            tokens.add(t);
            t = scanner.nextToken();
            //System.out.println(t);
        }

        for (Token token: tokens)
            System.out.println(token);

        for (Message msg: scanner.getMessages())
            System.out.println("ERROR: " + msg);

        Parser parser = new Parser(new Scanner(program));
        SVar start = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(start);

        //System.out.println(start);
//        test();
    }

//    private static void test() {
//        ColIdVar col11 = new ColIdVar();
//        col11.addSymbol(new IdentToken(Position.dummyPosition(), Position.dummyPosition(), "table"));
//        ColIdVar col12 = new ColIdVar();
//        col12.addSymbol(new IdentToken(Position.dummyPosition(), Position.dummyPosition(), "schema"));
//        DotToken dot1 = new DotToken(Position.dummyPosition(), Position.dummyPosition());
//        QualifiedNameVar qualifiedName1 = new QualifiedNameVar();
//        qualifiedName1.addSymbol(col11);
//        qualifiedName1.addSymbol(dot1);
//        qualifiedName1.addSymbol(col12);
//
//        ColIdVar col21 = new ColIdVar();
//        col21.addSymbol(new IdentToken(Position.dummyPosition(), Position.dummyPosition(), "table"));
//        ColIdVar col22 = new ColIdVar();
//        col22.addSymbol(new IdentToken(Position.dummyPosition(), Position.dummyPosition(), "schema"));
//        DotToken dot2 = new DotToken(Position.dummyPosition(), Position.dummyPosition());
//        QualifiedNameVar qualifiedName2 = new QualifiedNameVar();
//        qualifiedName2.addSymbol(col21);
//        qualifiedName2.addSymbol(dot2);
//        qualifiedName2.addSymbol(col22);
//
//        System.out.println(qualifiedName1.equals(qualifiedName2));
//    }
}
