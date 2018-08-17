package ru.bmstu.ORM.Codegen;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.CreateTableFunctionVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.CreateFunctionStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.ColumnDefVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.CreateTableStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.io.*;

public class CodeGenerator {
    private SVar start;
    private String path;

    //TODO pass path as parameter
    //TODO at first service files are needed to be copied
    public CodeGenerator(SVar start, String path) {
        this.start = start;
        this.path = path + "\\GenService";
    }

    public void generateFiles() {
        //copyFolder(); //tested, works fine

        for (Symbol symbol: start.getSymbols()) {
            if (symbol.getTag() == VarTag.CREATE_TABLE_FUNCTION_STMT) {
                generateCreateTableFunctionStmt((CreateTableFunctionVar) symbol);
            }
        }
    }

    private void copyFolder() {
        File srcFolder = new File("E:\\Sorry\\Documents\\IdeaProjects\\ORM\\src\\main\\java\\ru\\bmstu\\ORM\\Service");
        File destFolder = new File(this.path);

        if(!srcFolder.exists()){
            throw new RuntimeException("Can't copy folder, directory doesn't exists");
        }else{
            try{
                copyFolder(srcFolder,destFolder);
            }catch(IOException e){
                throw new RuntimeException("Can't copy folder", e);
            }
        }
    }

    private void copyFolder(File src, File dest) throws IOException{

        if(src.isDirectory()){

            if(!dest.exists()){
                dest.mkdir();
            }

            String files[] = src.list();

            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                copyFolder(srcFile,destFile);
            }

        }else{
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }

    private void generateCreateTableFunctionStmt(CreateTableFunctionVar createTableFunction) {
        for (Symbol symbol: createTableFunction.getSymbols()) {
            if (symbol.getTag() == VarTag.CREATE_TABLE_STMT)
                generateCreateTableStmt((CreateTableStmtVar) symbol);
            else if (symbol.getTag() == VarTag.CREATE_FUNCTION_STMT) {
                generateCreateFunctionStmt((CreateFunctionStmtVar) symbol);
            }
        }
    }

    private void generateCreateTableStmt(CreateTableStmtVar createTableStmt) {
        File classFile = new File(this.path + createTableStmt.getCatalog() + "\\" +
            createTableStmt.getSchema() + "\\Tables\\" + createTableStmt.getName() + ".java");

        try {
            classFile.getParentFile().mkdirs();
            classFile.createNewFile();
            classFile.setWritable(true);
            System.out.println(classFile.isFile());
            System.out.println(classFile.isDirectory());

            try (FileWriter writer = new FileWriter(classFile, false)){
                writer.write("package " + classFile.getParentFile().getPath().substring(classFile.getPath().indexOf("GenService")).replace('\\', '.') + ";\n\n");
                writer.write("import ru.bmstu.ORM.Service.ColumnAnnotations.*;\n");
                writer.write("import ru.bmstu.ORM.Service.Tables.Entity;\n\n");
                writer.write("import java.sql.*;\n" + "import java.util.ArrayList;\n" + "import java.util.Objects;\n\n");

                writer.write(String.format("@Table(db = \"%s\", schema = \"%s\", name = \"%s\")\n",
                        createTableStmt.getCatalog().toLowerCase(),
                        createTableStmt.getSchema().toLowerCase(),
                        createTableStmt.getName().toLowerCase()));
                writer.write(String.format("public class %s implements Entity {\n", createTableStmt.getName()));
                String foreignObj = null;
                for (ColumnDefVar column: createTableStmt.getColumns()) {
                    if (column.isPK())
                        writer.write("\t@PK\n");
                    if (column.isFK() != null)
                        writer.write(String.format("\t@FK(%s)\n", column.isFK()));
                    if (column.isDefault() != null)
                        writer.write("\t@Default\n");
                    writer.write("\t@Column(name = \"" + column.getName() + "\"");
                    if (column.isUnique())
                        writer.write(", unique = true");
                    if (column.isNullable() != null)
                        writer.write(", nullable = " + column.isNullable().toString());
                    if (column.getLength() != null)
                        writer.write(", length = " + column.getLength().toString());
                    writer.write(")\n");

                    writer.write("\tprivate " + column.getJavaType() + " " + column.getName());
                    if (column.isDefault() != null)
                        writer.write(" = " + column.isDefault());
                    writer.write(";\n\n");

                    if (column.isFK() != null && !column.isFO().equals(foreignObj)) {
                        foreignObj = column.isFO();
                        writer.write(String.format("\t@FO(table = \"%s\")\n", column.isFO()));
                        writer.write("\tprivate " +
                                Character.toUpperCase(column.isFO().charAt(0)) + column.isFO().substring(1) + " " +
                                column.isFO() + ";\n\n");
                    }
                }

                for (ColumnDefVar column: createTableStmt.getColumns()) {
                    writer.write("\tpublic " + column.getJavaType() + " get" + column.getUpperName() + "() {\n");
                    writer.write("\t\treturn this." + column.getName() + ";\n\t}\n\n");

                    writer.write("\tpublic void set" + column.getUpperName() + "(" + column.getJavaType() +
                        " " + column.getName() + ") {\n");
                    writer.write("\t\tthis." + column.getName() + " = " + column.getName() + ";\n\t}\n\n");
                }

                writer.write("\t@Override\n");
                writer.write("\tpublic boolean equals(Object obj) {\n");
                writer.write("\t\tif (this == obj)\n\t\t\treturn true;\n");
                writer.write("\t\tif (this.getClass() != obj.getClass())\n\t\t\treturn false;\n\n");
                writer.write("\t\t" + createTableStmt.getName() + " other = (" + createTableStmt.getName() + ") obj;\n");
                writer.write("\t\treturn ");
                boolean wasFirst = false;
                for (ColumnDefVar column: createTableStmt.getPKs()) {
                    if (!wasFirst)
                        wasFirst = true;
                    else
                        writer.write(" && ");

                    writer.write("Objects.equals(this." + column.getName() + ", other." + column.getName() + ")");
                }
                writer.write(";\n\t}\n\n");

                writer.write("\t@Override\n");
                writer.write("\tpublic int hashCode() {\n");
                writer.write("\t\treturn Objects.hashCode(");
                wasFirst = false;
                for (ColumnDefVar column: createTableStmt.getPKs()) {
                    if (!wasFirst)
                        wasFirst = true;
                    else
                        writer.write(", ");

                    writer.write("this." + column.getName());
                }
                writer.write(");\n\t}\n\n");

                writer.write("\t@Override\n");
                writer.write("\tpublic String toString() {\n");
                writer.write("\t\treturn \"" + createTableStmt.getName() + " {\\n\" +\n");
                for (ColumnDefVar column: createTableStmt.getColumns())
                    writer.write("\t\t\t\"\\t" + column.getName() + ": \" + this." + column.getName() + " + \"\\n\" +\n");
                writer.write("\t\t\t\"}\";\n");
                writer.write("\t}");
                writer.write("}");
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't create file " + classFile.getName(), e);
        }
    }

    private void generateCreateFunctionStmt(CreateFunctionStmtVar createFunctionStmt) {
        //TODO
    }
}
