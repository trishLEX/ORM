package ru.bmstu.ORM.Compiler;

import ru.bmstu.ORM.Compiler.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Compiler.Analyzer.Parser.Parser;
import ru.bmstu.ORM.Compiler.Analyzer.Semantics.SemanticAnalyzer;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Compiler.Codegen.CodeGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        if (args.length < 1) {
            throw new IllegalArgumentException("1st argument is .sql file, " +
                    "2nd argument is optional and contains path to folder where classes will be generated");
        }

        String program = new String(Files.readAllBytes(Paths.get(args[0])));

        Parser parser = new Parser(new Scanner(program));
        SVar start = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer(parser.getTables());
        analyzer.analyze(start);

        String path = args.length == 2 ? args[1] : System.getProperty("user.dir");
        CodeGenerator codeGenerator = new CodeGenerator(start, path);
        codeGenerator.generateFiles();
    }
}
