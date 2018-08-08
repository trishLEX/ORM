package ru.bmstu.ORM.Analyzer.Semantics;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.ConstExprVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.CreateTableFunctionVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.CreateFunctionStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.HashMap;

public class SemanticAnalyzer {
    private HashMap<QualifiedNameVar, CreateTableStmtVar> tables;

    public SemanticAnalyzer() {
        this.tables = new HashMap<>();
    }

    public void analyze(SVar S) {
        analyzeS(S);
    }

    private void analyzeS(SVar S) {
        analyzeCreateTableFunctionTrigger((CreateTableFunctionVar) S.get(1));
    }

    private void analyzeCreateTableFunctionTrigger(CreateTableFunctionVar createTableFunctionTrigger) {
        for (Symbol symbol: createTableFunctionTrigger.getSymbols()) {
            if (symbol.getTag() == VarTag.CREATE_TABLE_STMT) {
                analyzeCreateTableStmt((CreateTableStmtVar) symbol);
            } else if (symbol.getTag() == VarTag.CREATE_FUNCTION_STMT) {
                //TODO analyzeCreateFunctionStmt((CreateFunctionStmtVar) symbol);
            }
        }
    }

    private void analyzeCreateTableStmt(CreateTableStmtVar createTableStmt) {
        if (!tables.containsKey(createTableStmt.getTableName())) {
            tables.put(createTableStmt.getTableName(), createTableStmt);
        } else {
            throw new RuntimeException("Table " + createTableStmt.getTableName() + " is existed");
        }

        for (ColumnDefVar column: createTableStmt.getColumns()) {
            analyzeColumnDef(createTableStmt, column);
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
                for (int i = 3; i < colConstraintElem.getSymbols().size() - 1; i++) {
                    if (colConstraintElem.get(i).getTag() == TokenTag.IDENTIFIER
                            && !tables.get(colConstraintElem.get(1)).containsColumn((IdentToken) colConstraintElem.get(i)))
                        throw new RuntimeException("No column " + colConstraintElem.get(i) + " in " + tables.get(colConstraintElem.get(1)));
                }

                return;
            }

            if (!tables.containsKey(((QualifiedNameVar) colConstraintElem.get(1)).getWithoutLastName()))
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
                    && createTableStmt.getType((IdentToken) boolExprFactor.get(0)) != Types.BOOLEAN)
                throw new RuntimeException("Invalid type of " + boolExprFactor.get(0) + ", bool expected");

            if (boolExprFactor.getSymbols().size() == 2) {
                Types colType = createTableStmt.getType((IdentToken) boolExprFactor.get(0));
                RHSVar rhs = (RHSVar) boolExprFactor.get(1);
                if (rhs.get(0).getTag() == VarTag.ARITHM_RHS
                        && (colType != Types.BYTE && colType != Types.SHORT && colType != Types.INT && colType != Types.LONG
                            && colType != Types.FLOAT && colType != Types.DOUBLE))
                    throw new RuntimeException("Invalid type of " + boolExprFactor.get(0) + ", numeric exepcted");

                if (colType != Types.BOOLEAN && rhs.get(0).getTag() == VarTag.BOOL_RHS) {
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

            if (simpleType.get(0).getTag() == VarTag.DATETIME_TYPE && constExpr.get(0).getTag() != TokenTag.DATE_CONST)
                throw new RuntimeException("Invalid type of " + constExpr.get(0) + ", date/time expected");

            if (simpleType.get(0).getTag() == TokenTag.RECORD)
                throw new RuntimeException("Invalid type " + typename);

            if (simpleType.get(0).getTag() == TokenTag.BOOLEAN && (constExpr.get(0).getTag() != VarTag.BOOL_CONST ||
                    (constExpr.getSymbols().size() == 2 && constExpr.get(1).getTag() != VarTag.BOOL_CONST)))
                throw new RuntimeException("Invalid type of " + constExpr.get(0) + ", boolean expected");
        }
    }
}
