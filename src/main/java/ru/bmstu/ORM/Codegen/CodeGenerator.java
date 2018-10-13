package ru.bmstu.ORM.Codegen;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.CreateTableFunctionVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.CreateFunctionStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FuncArgWithDefaultVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.ColumnDefVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.CreateTableStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class CodeGenerator {
    private SVar start;
    private String path;

    public CodeGenerator(SVar start, String path) {
        this.start = start;
        this.path = path + "\\GenService";
    }

    public void generateFiles() {
        for (Symbol symbol: start.getSymbols()) {
            if (symbol.getTag() == VarTag.CREATE_TABLE_FUNCTION_STMT) {
                generateCreateTableFunctionStmt((CreateTableFunctionVar) symbol);
            }
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
        File classFile = new File(this.path + "\\" + createTableStmt.getCatalog() + "\\" +
                createTableStmt.getSchema() + "\\Tables\\" + createTableStmt.getName() + ".java");

        mkDirs(classFile);

        try (FileWriter writer = new FileWriter(classFile, false)) {
            writer.write("package " + classFile.getParentFile().getPath()
                    .substring(classFile.getPath().indexOf("GenService"))
                    .replace('\\', '.') + ";\n\n");
            writer.write("import ru.bmstu.ORM.Service.ColumnAnnotations.*;\n");
            writer.write("import ru.bmstu.ORM.Service.Tables.Entity;\n\n");
            writer.write("import java.sql.*;\n" + "import java.util.ArrayList;\n" + "import java.util.Objects;\n\n");

            writer.write(String.format("@Table(db = \"%s\", schema = \"%s\", name = \"%s\")\n",
                    createTableStmt.getCatalog().toLowerCase(),
                    createTableStmt.getSchema().toLowerCase(),
                    createTableStmt.getName().toLowerCase()));
            writer.write(String.format("public class %s extends Entity {\n", createTableStmt.getName()));
            String foreignObj = null;
            for (ColumnDefVar column : createTableStmt.getColumns()) {
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

            for (ColumnDefVar column : createTableStmt.getColumns()) {
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
            for (ColumnDefVar column : createTableStmt.getPKs()) {
                if (!wasFirst)
                    wasFirst = true;
                else
                    writer.write(" && ");

                writer.write("Objects.equals(this." + column.getName() + ", other." + column.getName() + ")");
            }
            writer.write(";\n\t}\n\n");

            writer.write("\t@Override\n");
            writer.write("\tpublic int hashCode() {\n");
            writer.write("\t\treturn Objects.hash(");
            wasFirst = false;
            for (ColumnDefVar column : createTableStmt.getPKs()) {
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
            for (ColumnDefVar column : createTableStmt.getColumns())
                writer.write("\t\t\t\"\\t" + column.getName() + ": \" + this." + column.getName() + " + \"\\n\" +\n");
            writer.write("\t\t\t\"}\";\n");
            writer.write("\t}\n");
            writer.write("}");
        } catch (IOException e) {
            throw new RuntimeException("Can't create/write file " + classFile.getName(), e);
        }
    }

    private void generateCreateFunctionStmt(CreateFunctionStmtVar createFunctionStmt) {
        File functionsFile = new File(this.path + "\\" + createFunctionStmt.getCatalog() + "\\" +
                createFunctionStmt.getSchema() + "\\Functions\\" + createFunctionStmt.getName() + "Function.java");

        mkDirs(functionsFile);

        try (FileWriter writer = new FileWriter(functionsFile, false)) {
            writer.write("package " + functionsFile.getParentFile().getPath()
                    .substring(functionsFile.getPath().indexOf("GenService"))
                    .replace('\\', '.') + ";\n\n");

            writer.write("import ru.bmstu.ORM.Service.ColumnAnnotations.Routines;\n");
            writer.write("import ru.bmstu.ORM.Service.Session.Interfaces.FunctionsExecutor;\n\n");

            writer.write("import java.sql.Connection;\n");
            writer.write("import java.util.ArrayList;\n\n");

            writer.write(String.format("@Routines(db = \"%s\", schema = \"%s\")\n",
                    createFunctionStmt.getCatalog().toLowerCase(),
                    createFunctionStmt.getSchema().toLowerCase()));
            writer.write(String.format("public class %sFunction extends FunctionsExecutor {\n",
                    createFunctionStmt.getName()));
            writer.write(String.format("\tpublic %sFunction(Connection connection) {\n",
                    createFunctionStmt.getName()));
            writer.write("\t\tsuper(connection);\n");
            writer.write("\t}\n\n");

            generateWithArgs(createFunctionStmt, new ArrayList<>(), 0, writer);

            writer.write("}");
        } catch (IOException exception) {
            throw new RuntimeException("Can't create/write function file", exception);
        }

        if (createFunctionStmt.getReturnedTable() != null) {
            File classFile = new File(this.path + "\\" + createFunctionStmt.getCatalog() + "\\" +
                    createFunctionStmt.getSchema() + "\\Functions\\" + createFunctionStmt.getName() + "FunctionTable.java");

            mkDirs(classFile);

            try (FileWriter writer = new FileWriter(classFile, false)) {
                                writer.write("package " + functionsFile.getParentFile().getPath()
                        .substring(functionsFile.getPath().indexOf("GenService"))
                        .replace('\\', '.') + ";\n\n");

                writer.write("import ru.bmstu.ORM.Service.ColumnAnnotations.Column;\n");
                writer.write("import ru.bmstu.ORM.Service.Functions.ReturnedTable;\n\n");

                writer.write("import java.util.ArrayList;\n\n");

                writer.write(String.format("public class %sFunctionTable implements ReturnedTable {\n",
                        createFunctionStmt.getName()));
                for (Map.Entry<String, TypenameVar> entry : createFunctionStmt.getReturnedTable().entrySet()) {
                    writer.write(String.format("\t@Column(name = \"%s\")\n", entry.getKey().toLowerCase()));
                    writer.write(String.format("\tprivate %s %s;\n\n", entry.getValue().getJavaType(), entry.getKey()));
                }

                for (Map.Entry<String, TypenameVar> entry : createFunctionStmt.getReturnedTable().entrySet()) {
                    writer.write(String.format("\tpublic %s get%s() {\n", entry.getValue().getJavaType(), entry.getKey()));
                    writer.write(String.format("\t\treturn this.%s;\n", entry.getKey()));
                    writer.write("\t}\n\n");

                    writer.write(String.format("\tpublic void set%s(%s %s) {\n", entry.getKey(), entry.getValue().getJavaType(), entry.getKey()));
                    writer.write(String.format("\t\tthis.%s = %s;\n", entry.getKey(), entry.getKey()));
                    writer.write("\t}\n\n");
                }

                writer.write("}");
            } catch (IOException exception) {
                throw new RuntimeException("Can't create/write function file", exception);
            }
        }
    }

    private void mkDirs(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            file.setWritable(true);
        } catch (IOException exception) {
            throw new RuntimeException("Can't create/write function file", exception);
        }
    }

    private void generateWithArgs(CreateFunctionStmtVar createFunctionStmt, ArrayList<Boolean> defArgs,
                                  int index, FileWriter writer) {
        if (index == createFunctionStmt.getArgs().size()) {
            generateFunction(createFunctionStmt, defArgs, writer);
        } else {
            if (createFunctionStmt.getArg(index).size() == 2) {
                ArrayList<Boolean> copyDefArgs = new ArrayList<>(defArgs);
                index++;

                defArgs.add(true);
                generateWithArgs(createFunctionStmt, defArgs, index, writer);

                copyDefArgs.add(false);
                generateWithArgs(createFunctionStmt, copyDefArgs, index, writer);
            } else {
                generateWithArgs(createFunctionStmt, defArgs, ++index, writer);
            }
        }
    }

    private void generateFunction(CreateFunctionStmtVar createFunctionStmt,
                                  ArrayList<Boolean> defArgs, FileWriter writer) {
        try {
            writer.write(String.format("\tpublic %s %s(",
                    createFunctionStmt.getReturnedJavaType(),
                    createFunctionStmt.getName().toLowerCase()));

            boolean wasFirst = false;
            int defIndex = -1;
            int count = 0;
            StringBuilder args = new StringBuilder();
            for (FuncArgWithDefaultVar arg: createFunctionStmt.getArgs()) {
                if (arg.isDefault()) {
                    defIndex++;
                    if (!defArgs.get(defIndex))
                        continue;
                }
                if (!wasFirst) {
                    writer.write(String.format("%s %s", arg.getJavaType(), arg.getName()));
                    args.append(arg.getName());
                    wasFirst = true;
                } else {
                    writer.write(String.format(", %s %s", arg.getJavaType(), arg.getName()));
                    args.append(", ").append(arg.getName());
                }
                count++;
            }
            writer.write(") {\n");

            boolean isAllFalse = true;
            for (Boolean bool: defArgs)
                if (bool) {
                    isAllFalse = false;
                    break;
                }

            if (!((createFunctionStmt.getArgs().size() == 0 || createFunctionStmt.getArgs().size() == defArgs.size()) && isAllFalse)) {
                if (createFunctionStmt.getReturnedType() != null) {
                    writer.write(String.format("\t\treturn (%s) super.executeScalarFunction(this.getClass(), " +
                                    "String.format(\"%s(%s)\", %s));\n",
                            createFunctionStmt.getReturnedJavaType(),
                            createFunctionStmt.getName().toLowerCase(),
                            mulString("%s", count),
                            args.toString()
                    ));
                } else if (createFunctionStmt.getReturnedTable() != null) {
                    writer.write(String.format("\t\treturn super.executeTableFunction(this.getClass(), %sFunctionTable.class, " +
                                    "String.format(\"%s(%s)\", %s));\n",
                            createFunctionStmt.getName(),
                            createFunctionStmt.getName().toLowerCase(),
                            mulString("%s", count),
                            args.toString()
                    ));
                } else {
                    writer.write(String.format("\t\tsuper.executeVoidFunction(this.getClass(), String.format(\"%s(%s)\", %s));\n",
                            createFunctionStmt.getName().toLowerCase(),
                            mulString("%s", count),
                            args.toString()
                    ));
                }
            } else {
                if (createFunctionStmt.getReturnedType() != null) {
                    writer.write(String.format("\t\treturn (%s) super.executeScalarFunction(this.getClass(), " +
                                    "\"%s()\");\n",
                            createFunctionStmt.getReturnedJavaType(),
                            createFunctionStmt.getName().toLowerCase()
                    ));
                } else if (createFunctionStmt.getReturnedTable() != null) {
                    writer.write(String.format("\t\treturn super.executeTableFunction(this.getClass(), %sFunctionTable.class, " +
                                    "\"%s()\");\n",
                            createFunctionStmt.getName(),
                            createFunctionStmt.getName().toLowerCase()
                    ));
                } else {
                    writer.write(String.format("\t\tsuper.executeVoidFunction(this.getClass(), \"%s()\");\n",
                            createFunctionStmt.getName().toLowerCase()
                    ));
                }
            }

            writer.write("\t}\n\n");
        } catch (IOException exception) {
            throw new RuntimeException("Can't create/write function file", exception);
        }
    }

    private String mulString(String str, int n) {
        boolean wasFirst = false;
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (!wasFirst) {
                res.append(str);
                wasFirst = true;
            } else {
                res.append(", ").append(str);
            }
        }

        return res.toString();
    }
}
