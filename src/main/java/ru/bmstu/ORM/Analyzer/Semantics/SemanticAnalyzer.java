package ru.bmstu.ORM.Analyzer.Semantics;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.SymbolType;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.ConstExprVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.CreateTableFunctionVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.ArrayList;
import java.util.HashMap;

public class SemanticAnalyzer {
    private HashMap<QualifiedNameVar, CreateTableStmtVar> tables;

    public SemanticAnalyzer(HashMap<QualifiedNameVar, CreateTableStmtVar> tables) {
        this.tables = tables;
    }

    public void analyze(SVar S) {
        analyzeS(S);
    }

    private void analyzeS(SVar S) {
        for (Symbol symbol: S.getSymbols())
            if (symbol.getTag() == VarTag.CREATE_TABLE_FUNCTION_STMT)
                analyzeCreateTableFunctionTrigger((CreateTableFunctionVar) symbol);
    }

    private void analyzeCreateTableFunctionTrigger(CreateTableFunctionVar createTableFunction) {
        for (Symbol symbol: createTableFunction.getSymbols()) {
            if (symbol.getTag() == VarTag.CREATE_TABLE_STMT) {
                analyzeCreateTableStmt((CreateTableStmtVar) symbol);
            } else if (symbol.getTag() == VarTag.CREATE_FUNCTION_STMT) {
                //TODO analyzeCreateFunctionStmt((CreateFunctionStmtVar) symbol);
            }
        }
    }

    private void analyzeCreateTableStmt(CreateTableStmtVar createTableStmt) {
        if (!createTableStmt.isExistsPK())
            throw new RuntimeException("No primary key in " + createTableStmt.getTableName());

        for (ColumnDefVar column: createTableStmt.getColumns()) {
            analyzeColumnDef(createTableStmt, column);
        }

        for (TableConstraintVar tableConstraint: createTableStmt.getTableConstraints()) {
            analyzeTableConstraint(createTableStmt, tableConstraint);
        }
    }

    private void analyzeColumnDef(CreateTableStmtVar createTableStmt, ColumnDefVar columnDef) {
        if (columnDef.getSymbols().size() > 2) {
            for (int i = 2; i < columnDef.getSymbols().size(); i++) {
                analyzeColConstraint(createTableStmt, (TypenameVar) columnDef.get(1), (ColConstraintVar) columnDef.get(i));
            }
        }
    }

    private void analyzeColConstraint(CreateTableStmtVar createTableStmt, TypenameVar typename, ColConstraintVar colConstraint) {
        if (colConstraint.getSymbols().size() == 1) {
            analyzeColConstraintElem(createTableStmt, typename, (ColConstraintElemVar) colConstraint.get(0));
        } else {
            analyzeColConstraintElem(createTableStmt, typename, (ColConstraintElemVar) colConstraint.get(2));
        }
    }

    private void analyzeColConstraintElem(CreateTableStmtVar createTableStmt, TypenameVar typename, ColConstraintElemVar colConstraintElem) {
        if (colConstraintElem.getSymbols().size() > 2 && colConstraintElem.get(2).getTag() == VarTag.BOOL_EXPR) {
            analyzeBoolExpr(createTableStmt, (BoolExprVar) colConstraintElem.get(2));
        } else if (colConstraintElem.getSymbols().size() > 1 && colConstraintElem.get(1).getTag() == VarTag.CONST_EXPR) {
            analyzeConstExpr(typename, (ConstExprVar) colConstraintElem.get(1));
        } else if (colConstraintElem.get(0).getTag() == TokenTag.REFERENCES) {
            if (tables.containsKey(colConstraintElem.get(1))) {
                if (colConstraintElem.size() > 2) {
                    if (colConstraintElem.get(3).getTag() == TokenTag.IDENTIFIER) {
                        if (!tables.get(colConstraintElem.get(1)).containsColumn((IdentToken) colConstraintElem.get(3)))
                            throw new RuntimeException("No column " + colConstraintElem.get(3) + " in " + tables.get(colConstraintElem.get(1)));

                        if (typename.size() == 2)
                            throw new RuntimeException("Array as foreign key at " + colConstraintElem);
                        else {
                            SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) typename.get(0);
                            System.out.println(simpleTypeName.get(0).getTag() + " " + tables.get(colConstraintElem.get(1)).getTypeOfColumn((IdentToken) colConstraintElem.get(3)));
                            if (simpleTypeName.getFullType() != tables.get(colConstraintElem.get(1)).getFullTypeOfColumn((IdentToken) colConstraintElem.get(3)))
                                throw new RuntimeException("Types of foreign key and referenced key are different at " + colConstraintElem);
                        }
                    } else
                        throw new RuntimeException("Identifier expected, got " + colConstraintElem.get(3));
                } else {
                    throw new RuntimeException("No referenced column specified at " + colConstraintElem);
                }
            } else if (!tables.containsKey(((QualifiedNameVar) colConstraintElem.get(1)).getWithoutLastName()))
                throw new RuntimeException(colConstraintElem.get(1) + " does not exist");
            else {
                if (!tables.get(((QualifiedNameVar) colConstraintElem.get(1)).getWithoutLastName()).containsColumn(((QualifiedNameVar) colConstraintElem.get(1)).getLastColId()))
                    throw new RuntimeException("No column " + ((QualifiedNameVar) colConstraintElem.get(1)).getLastColId() + " in " + tables.get(((QualifiedNameVar) colConstraintElem.get(1)).getWithoutLastName()));
            }
        }
    }

    private void analyzeBoolExpr(CreateTableStmtVar createTableStmt, BoolExprVar boolExpr) {
        for (Symbol s: boolExpr.getSymbols())
            if (s.getTag() == VarTag.BOOL_EXPR_TERM)
                analyzeBoolExprTerm(createTableStmt, (BoolExprTermVar) s);
    }

    private void analyzeBoolExprTerm(CreateTableStmtVar createTableStmt, BoolExprTermVar boolExprTerm) {
        for (Symbol s: boolExprTerm.getSymbols())
            if (s.getTag() == VarTag.BOOL_EXPR_FACTOR)
                analyzeBoolExprFactor(createTableStmt, (BoolExprFactorVar) s);
    }

    private void analyzeBoolExprFactor(CreateTableStmtVar createTableStmt, BoolExprFactorVar boolExprFactor) {
        if (boolExprFactor.get(0).getTag() == TokenTag.NOT)
            analyzeBoolExprFactor(createTableStmt, (BoolExprFactorVar) boolExprFactor.get(1));
        else if (boolExprFactor.get(0).getTag() == TokenTag.LPAREN)
            analyzeBoolExpr(createTableStmt, (BoolExprVar) boolExprFactor.get(1));
        else if (boolExprFactor.get(0).getTag() == TokenTag.IDENTIFIER) {
            if (boolExprFactor.getSymbols().size() == 1
                    && createTableStmt.getTypeOfColumn((IdentToken) boolExprFactor.get(0)) != TokenTag.BOOLEAN)
                throw new RuntimeException("Invalid type of " + boolExprFactor.get(0) + ", bool expected");
            else {
                SymbolType colType = createTableStmt.getTypeOfColumn((IdentToken) boolExprFactor.get(0));
                RHSVar rhs = (RHSVar) boolExprFactor.get(1);
                if (rhs.get(0).getTag() == VarTag.DATE_RHS && colType != VarTag.DATETIME_TYPE)
                    throw new RuntimeException("Invalid type of " + boolExprFactor.get(0) + ", datetime expected");

                if (colType != TokenTag.BOOLEAN && rhs.get(0).getTag() == VarTag.BOOL_RHS) {
                    BoolRHSVar boolRHS = (BoolRHSVar) rhs.get(0);
                    BoolConstVar boolConst = (BoolConstVar) boolRHS.get(boolRHS.getSymbols().size() - 1);
                    if (boolConst.get(0).getTag() != TokenTag.NULL)
                        throw new RuntimeException("Invalid type of " + boolExprFactor.get(0));
                }
            }
        }
    }

    private void analyzeConstExpr(TypenameVar typename, ConstExprVar constExpr) {
        if (constExpr.get(0).getTag() == TokenTag.LPAREN)
            analyzeConstExpr(typename, (ConstExprVar) constExpr.get(1));

        if (typename.getSymbols().size() == 1) {
            SimpleTypeNameVar simpleType = (SimpleTypeNameVar) typename.get(0);
            if (simpleType.get(0).getTag() == VarTag.NUMERIC_TYPE && constExpr.get(0).getTag() != VarTag.ARITHM_CONST_EXPR)
                throw new RuntimeException("Invalid type of " + constExpr.get(0) + ", numeric expected");

            if (simpleType.get(0).getTag() == VarTag.CHARACTER_TYPE && constExpr.get(0).getTag() != TokenTag.STRING_CONST)
                throw new RuntimeException("Invalid type of " + constExpr.get(0) + ", string expected");

            if (simpleType.get(0).getTag() == VarTag.DATE_TIME_CAST && constExpr.get(0).getTag() != TokenTag.DATE_CONST)
                throw new RuntimeException("Invalid type of " + constExpr.get(0) + ", date/time expected");

            if (simpleType.get(0).getTag() == TokenTag.RECORD)
                throw new RuntimeException("Invalid type " + typename);

            if (simpleType.get(0).getTag() == TokenTag.BOOLEAN && (constExpr.get(0).getTag() != VarTag.BOOL_CONST ||
                    (constExpr.getSymbols().size() == 2 && constExpr.get(1).getTag() != VarTag.BOOL_CONST)))
                throw new RuntimeException("Invalid type of " + constExpr.get(0) + ", boolean expected");
        }
    }

    private void analyzeTableConstraint(CreateTableStmtVar createTableStmt, TableConstraintVar tableConstraint) {
        analyzeConstraintElem(createTableStmt, (ConstraintElemVar) tableConstraint.get(tableConstraint.size() - 1));
    }

    private void analyzeConstraintElem(CreateTableStmtVar createTableStmtVar, ConstraintElemVar constraintElem) {
        if (constraintElem.get(0).getTag() == TokenTag.UNIQUE || constraintElem.get(0).getTag() == TokenTag.PRIMARY) {
            for (Symbol s: constraintElem.getSymbols()) {
                if (s.getTag() == TokenTag.IDENTIFIER) {
                    if (!createTableStmtVar.containsColumn((IdentToken) s))
                        throw new RuntimeException("No column " + s + " at " + createTableStmtVar);
                }
            }
        } else {
            int keysIn = 0;
            ArrayList<IdentToken> foreignKeys = new ArrayList<>();
            for (int i = 3; constraintElem.get(i).getTag() != TokenTag.RPAREN; i++) {
                if (constraintElem.get(i).getTag() == TokenTag.IDENTIFIER) {
                    if (!createTableStmtVar.containsColumn((IdentToken) constraintElem.get(i)))
                        throw new RuntimeException("No column " + constraintElem.get(i) + " at " + createTableStmtVar);
                    foreignKeys.add((IdentToken) constraintElem.get(i));
                    keysIn++;
                }
            }

            if (tables.containsKey(constraintElem.get(2 * keysIn + 4))) {
                if (constraintElem.size() > 2 * keysIn + 5) {
                    int keysOut = 0;
                    ArrayList<IdentToken> referencedKeys = new ArrayList<>();
                    for (int i = 2 * keysIn + 6; constraintElem.get(i).getTag() != TokenTag.RPAREN; i++) {
                        if (constraintElem.get(i).getTag() == TokenTag.IDENTIFIER) {
                            if (!tables.get(constraintElem.get(2 * keysIn + 4)).containsColumn((IdentToken) constraintElem.get(i))) {
                                throw new RuntimeException("No column " + constraintElem.get(i) + " in " + tables.get(constraintElem.get(2 * keysIn + 4)));
                            }
                            keysOut++;
                            referencedKeys.add((IdentToken) constraintElem.get(i));
                        }
                    }

                    if (keysIn != keysOut)
                        throw new RuntimeException("Count of foreign and referenced columns is different at " + constraintElem);

                    for (int i = 0; i < keysIn; i++) {
                        if (createTableStmtVar.getFullTypeOfColumn(foreignKeys.get(i)) != tables.get(constraintElem.get(2 * keysIn + 4)).getFullTypeOfColumn(referencedKeys.get(i)))
                            throw new RuntimeException("Types of column " + foreignKeys.get(i) + " and " + referencedKeys.get(i) + " are different");
                    }
                } else {
                    throw new RuntimeException("Some referenced columns are not specified at " + constraintElem);
                }
            } else {
                throw new RuntimeException("Entity " + constraintElem.get(2 * keysIn + 4) + " does not exist");
            }
        }
    }
}
