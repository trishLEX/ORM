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
    public static void main(String[] arg) throws IOException, CloneNotSupportedException {
        String program = new String(Files.readAllBytes(Paths.get(arg[0])));

        Parser parser = new Parser(new Scanner(program));
        SVar start = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer(parser.getTables());
        analyzer.analyze(start);

        CodeGenerator codeGenerator = new CodeGenerator(start, "E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\test");
        codeGenerator.generateFiles();
    }
}
