package ru.bmstu.ORM;

import ru.bmstu.ORM.Analyzer.Lexer.Message;
import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;

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
        }

        for (Token token: tokens)
            System.out.println(token);

        for (Message msg: scanner.getMessages())
            System.out.println("ERROR: " + msg);
    }
}
