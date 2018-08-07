package ru.bmstu.ORM.Analyzer.Semantics;

import ru.bmstu.ORM.Analyzer.Symbols.Symbol;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.ColIdVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ColumnExpression.ColExprFactorVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ColumnExpression.ColExprTermVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ColumnExpression.ColExprVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.ConstExprVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.ExprVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.QualifiedNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.NumericTypeVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.SimpleTypeNameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.DeclareBlockVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.FuncAsVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.FuncBodyVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Variable.VariableAssignVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Variable.VariableDeclVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration.CreateFuncBodyVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration.CreateFunctionStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration.CreateTableFunctionTriggerVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.SVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.HashMap;

import static ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag.*;

public class SemanticAnalyzer {
    private HashMap<QualifiedNameVar, CreateTableStmtVar> tables;
    private HashMap<QualifiedNameVar, CreateFunctionStmtVar> functions;

    public SemanticAnalyzer() {
        this.tables = new HashMap<>();
        this.functions = new HashMap<>();
    }

    public void analyze(SVar S) {
        analyzeS(S);
    }

    private void analyzeS(SVar S) {
        analyzeCreateTableFunctionTrigger((CreateTableFunctionTriggerVar) S.get(1));
    }

    private void analyzeCreateTableFunctionTrigger(CreateTableFunctionTriggerVar createTableFunctionTrigger) {
        for (Symbol symbol: createTableFunctionTrigger.getSymbols()) {
            if (symbol.getTag() == VarTag.CREATE_TABLE_STMT) {
                analyzeCreateTableStmt((CreateTableStmtVar) symbol);
            } else if (symbol.getTag() == VarTag.CREATE_FUNCTION_STMT) {
                analyzeCreateFunctionStmt((CreateFunctionStmtVar) symbol);
            }
        }
    }

    private void analyzeCreateTableStmt(CreateTableStmtVar createTableStmt) {
        if (!tables.containsKey(createTableStmt.getTableName())) {
            tables.put(createTableStmt.getTableName(), createTableStmt);
        } else {
            //TODO test this
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
                    if (colConstraintElem.get(i).getTag() == VarTag.COL_ID
                            && !tables.get(colConstraintElem.get(1)).containsColumn((ColIdVar) colConstraintElem.get(i)))
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
        else if (boolExprFactor.get(0).getTag() == VarTag.COL_ID) {
            if (boolExprFactor.getSymbols().size() == 1
                    && createTableStmt.getType((ColIdVar) boolExprFactor.get(0)) != Types.BOOLEAN)
                throw new RuntimeException("Invalid type of " + boolExprFactor.get(0) + ", bool expected");

            if (boolExprFactor.getSymbols().size() == 2) {
                Types colType = createTableStmt.getType((ColIdVar) boolExprFactor.get(0));
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

    private void analyzeCreateFunctionStmt(CreateFunctionStmtVar createFunctionStmt) {
        if (!functions.containsKey(createFunctionStmt.getFunctionName()))
            functions.put(createFunctionStmt.getFunctionName(), createFunctionStmt);
        else
            throw new RuntimeException("Function " + createFunctionStmt.getFunctionName() + " is existed");

        analyzeCreateFuncBody(createFunctionStmt, (CreateFuncBodyVar) createFunctionStmt.get(createFunctionStmt.getSymbols().size() - 1));
    }

    private void analyzeCreateFuncBody(CreateFunctionStmtVar createFunctionStmt, CreateFuncBodyVar createFuncBody) {
        if (createFuncBody.get(1).getTag() == TokenTag.DOUBLE_DOLLAR)
            analyzeFuncAs(createFunctionStmt, (FuncAsVar) createFuncBody.get(2));
        else
            analyzeFuncAs(createFunctionStmt, (FuncAsVar) createFuncBody.get(4));
    }

    private void analyzeFuncAs(CreateFunctionStmtVar createFunctionStmt, FuncAsVar funcAs) {
        if (funcAs.get(0).getTag() == TokenTag.BEGIN) {
            for (int i = 1; i < funcAs.getSymbols().size() - 2; i++) {
                FuncBodyVar funcBody = (FuncBodyVar) funcAs.get(i);
                funcBody.addVariables(createFunctionStmt.getTypedParameters());
                analyzeFuncBody(createFunctionStmt, funcBody);
            }
        } else {
            DeclareBlockVar declareBlock = (DeclareBlockVar) createFunctionStmt.get(0);
            HashMap<IdentToken, TypenameVar> declarations = new HashMap<>();
            for (int i = 1; i < declareBlock.getSymbols().size(); i++) {
                VariableDeclVar variableDecl = (VariableDeclVar) declareBlock.get(i);
                declarations.put((IdentToken) variableDecl.get(0), (TypenameVar) variableDecl.get(1));
            }
            for (int i = 1; i < funcAs.getSymbols().size() - 2; i++) {
                FuncBodyVar funcBody = (FuncBodyVar) funcAs.get(i);
                funcBody.addVariables(createFunctionStmt.getTypedParameters());
                funcBody.addVariables(declarations);
                analyzeFuncBody(createFunctionStmt, funcBody);
            }
        }
    }

    private void analyzeFuncBody(CreateFunctionStmtVar createFunctionStmt, FuncBodyVar funcBody) {
        if (funcBody.get(0).getTag() == VarTag.FUNC_AS) {
            analyzeFuncAs(createFunctionStmt, (FuncAsVar) funcBody.get(0));
        } else if (funcBody.get(0).getTag() == VarTag.VAR_ASSIGN) {
            VariableAssignVar variableAssign = (VariableAssignVar) funcBody.get(0);
            Types typeOfExpr = getTypeOfExpr(funcBody.getVariables(), (ExprVar) variableAssign.get(2));
            //TODO stopped here
        }
    }

    private Types getTypeOfExpr(HashMap<IdentToken, TypenameVar> vars, ExprVar expr) {
        if (expr.get(0).getTag() == TokenTag.DATE_CONST)
            return Types.DATE;
        else if (expr.get(0).getTag() == TokenTag.TIME_CONST)
            return Types.TIME;
        else if (expr.get(0).getTag() == TokenTag.TIMESTAMP_CONST)
            return Types.TIMESTAMP;
        else if (expr.get(0).getTag() == TokenTag.STRING_CONST)
            return Types.STRING;
        else {
            return getTypeOfColExpr(vars, (ColExprVar) expr.get(0));
        }

    }

    private Types getTypeOfColExpr(HashMap<IdentToken, TypenameVar> vars, ColExprVar expr) {
        Types type = getTypeOfColExprTerm(vars, (ColExprTermVar) expr.get(0));
        if (type == Types.SHORT || type == Types.INT || type == Types.BYTE || type == Types.LONG
                || type == Types.FLOAT || type == Types.DOUBLE) {
            for (Symbol s: expr.getSymbols()) {
                if (s.getTag() == VarTag.COL_EXPR_TERM &&
                        (getTypeOfColExprTerm(vars, (ColExprTermVar) s) != Types.SHORT
                        && getTypeOfColExprTerm(vars, (ColExprTermVar) s) != Types.INT
                        && getTypeOfColExprTerm(vars, (ColExprTermVar) s) != Types.BYTE
                        && getTypeOfColExprTerm(vars, (ColExprTermVar) s) != Types.LONG
                        && getTypeOfColExprTerm(vars, (ColExprTermVar) s) != Types.FLOAT
                        && getTypeOfColExprTerm(vars, (ColExprTermVar) s) != Types.DOUBLE)) {
                    throw new RuntimeException("Type of " + s + " must be numeric");
                }
                if (s.getTag() == TokenTag.OR)
                    throw new RuntimeException("Wrong operator " + s + ", + or - expected");
            }
        } else if (type == Types.BOOLEAN) {
            for (Symbol s: expr.getSymbols()) {
                if (s.getTag() == VarTag.COL_EXPR_TERM && getTypeOfColExprTerm(vars, (ColExprTermVar) s) == Types.BOOLEAN) {
                    throw new RuntimeException("Type of " + s + " must be boolean");
                }
                if (s.getTag() == TokenTag.ADD || s.getTag() == TokenTag.SUB) {
                    throw new RuntimeException("Wrong operator " + s + ", OR expected");
                }
            }
        } else {
            throw new RuntimeException("Wrong type of " + expr.get(0));
        }

        return type;
    }

    private Types getTypeOfColExprTerm(HashMap<IdentToken, TypenameVar> vars, ColExprTermVar colExprTerm) {
        Types type = getTypeOfColExprFactor(vars, (ColExprFactorVar) colExprTerm.get(0));
        if (type == Types.SHORT || type == Types.INT || type == Types.BYTE || type == Types.LONG
                || type == Types.FLOAT || type == Types.DOUBLE) {
            for (Symbol s: colExprTerm.getSymbols()) {
                if (s.getTag() == VarTag.COL_EXPR_TERM &&
                        (getTypeOfColExprFactor(vars, (ColExprFactorVar) s) != Types.SHORT
                                && getTypeOfColExprFactor(vars, (ColExprFactorVar) s) != Types.INT
                                && getTypeOfColExprFactor(vars, (ColExprFactorVar) s) != Types.BYTE
                                && getTypeOfColExprFactor(vars, (ColExprFactorVar) s) != Types.LONG
                                && getTypeOfColExprFactor(vars, (ColExprFactorVar) s) != Types.FLOAT
                                && getTypeOfColExprFactor(vars, (ColExprFactorVar) s) != Types.DOUBLE)) {
                    throw new RuntimeException("Type of " + s + " must be numeric");
                }
                if (s.getTag() == TokenTag.AND)
                    throw new RuntimeException("Wrong operator " + s + ", / or * expected");
            }
        } else if (type == Types.BOOLEAN) {
            for (Symbol s: colExprTerm.getSymbols()) {
                if (s.getTag() == VarTag.COL_EXPR_TERM && getTypeOfColExprFactor(vars, (ColExprFactorVar) s) == Types.BOOLEAN) {
                    throw new RuntimeException("Type of " + s + " must be boolean");
                }
                if (s.getTag() == TokenTag.MUL || s.getTag() == TokenTag.DIV) {
                    throw new RuntimeException("Wrong operator " + s + ", AND expected");
                }
            }
        } else {
            throw new RuntimeException("Wrong type of " + colExprTerm.get(0));
        }

        return type;
    }

    private Types getTypeOfColExprFactor(HashMap<IdentToken, TypenameVar> vars, ColExprFactorVar colExprFactor) {
        if (colExprFactor.get(0).getTag() == VarTag.COL_ID) {
            IdentToken col = (IdentToken) ((ColIdVar) colExprFactor.get(0)).get(0);
            if (vars.containsKey(col)) {
                if (colExprFactor.getSymbols().size() == 1) {
                    TypenameVar type = vars.get(col);
                    if (type.getSymbols().size() == 2) {
                        throw new RuntimeException("Array in expression " + col); //TODO support this
                    } else {
                        SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) type.get(0);
                        if (simpleTypeName.get(0).getTag() == TokenTag.RECORD) {
                            throw new RuntimeException("Record in expression " + col);
                        } else if (simpleTypeName.get(0).getTag() == TokenTag.BOOLEAN) {
                            return Types.BOOLEAN;
                        } else if (simpleTypeName.get(0).getTag() == VarTag.DATETIME_TYPE) {
                            throw new RuntimeException("DateTime in expression " + col);
                        } else if (simpleTypeName.get(0).getTag() == VarTag.CHARACTER_TYPE) {
                            throw new RuntimeException("String in expression " + col);
                        } else {
                            NumericTypeVar numericType = (NumericTypeVar) simpleTypeName.get(0);
                            switch ((TokenTag) numericType.get(0).getTag()) {
                                case INT:
                                case INTEGER:
                                case DECIMAL:
                                case NUMERIC:
                                    return Types.INT;
                                case SMALLINT:
                                    return Types.SHORT;
                                case BIGINT:
                                    return Types.LONG;
                                case REAL:
                                case FLOAT:
                                    return Types.FLOAT;
                                case DOUBLE:
                                    return Types.DOUBLE;
                                default:
                                    throw new RuntimeException("Wrong type " + col);
                            }
                        }
                    }
                } else {
                    analyzeColumnRHS(vars.get(col), (RHSVar) colExprFactor.get(1));
                    return Types.BOOLEAN;
                }
            } else {
                throw new RuntimeException("Identifier " + col + " is not found");
            }
        } else if (colExprFactor.get(0).getTag() == TokenTag.SUB) {
            Types factorType = getTypeOfColExprFactor(vars, (ColExprFactorVar) colExprFactor.get(1));
            if (colExprFactor.getSymbols().size() == 2) {
                if (factorType != Types.SHORT && factorType != Types.BYTE && factorType != Types.INT
                        && factorType != Types.LONG && factorType != Types.FLOAT && factorType != Types.DOUBLE)
                    throw new RuntimeException("Wrong type of " + colExprFactor.get(1) + ", numeric expected");

                return factorType;
            } else {
                if (factorType != Types.SHORT && factorType != Types.BYTE && factorType != Types.INT
                        && factorType != Types.LONG && factorType != Types.FLOAT && factorType != Types.DOUBLE)
                    throw new RuntimeException("Wrong type of " + colExprFactor.get(1) + ", numeric expected");

                return Types.BOOLEAN;
            }
        } else if (colExprFactor.get(0).getTag() == TokenTag.LPAREN) {
            Types factorType = getTypeOfColExpr(vars, (ColExprVar) colExprFactor.get(1));
            if (colExprFactor.getSymbols().size() == 3) {
                return factorType;
            } else {
                analyzeExprRHS(factorType, (RHSVar) colExprFactor.get(3));
                return Types.BOOLEAN;
            }
        } else if (colExprFactor.get(0).getTag() == TokenTag.NOT) {
            Types factorType = getTypeOfColExprFactor(vars, (ColExprFactorVar) colExprFactor.get(1));
            if (factorType != Types.BOOLEAN)
                throw new RuntimeException("Wrong type of " + colExprFactor.get(1) + ", boolean expected");

            return Types.BOOLEAN;
        } else if (colExprFactor.get(0).getTag() == TokenTag.BYTE_CONST) {
            return Types.BYTE;
        } else if (colExprFactor.get(0).getTag() == TokenTag.SHORT_CONST) {
            return Types.SHORT;
        } else if (colExprFactor.get(0).getTag() == TokenTag.INT_CONST) {
            return Types.INT;
        } else if (colExprFactor.get(0).getTag() == TokenTag.LONG_CONST) {
            return Types.LONG;
        } else if (colExprFactor.get(0).getTag() == TokenTag.FLOAT_CONST) {
            return Types.FLOAT;
        } else if (colExprFactor.get(0).getTag() == TokenTag.DOUBLE_CONST) {
            return Types.DOUBLE;
        } else if (colExprFactor.get(0).getTag() == VarTag.BOOL_CONST) {
            return Types.BOOLEAN;
        } else {
            throw new RuntimeException("Wrong type of " + colExprFactor.get(0));
        }
    }

    private void analyzeColumnRHS(TypenameVar typename, RHSVar rhs) {
        if (typename.getSymbols().size() == 2)
            throw new RuntimeException("Wrong type " + typename + " bool or arithmetic expected in " + rhs);

        SimpleTypeNameVar simpleTypeName = (SimpleTypeNameVar) typename.get(0);
        if (simpleTypeName.get(0).getTag() != VarTag.NUMERIC_TYPE && rhs.get(0).getTag() == VarTag.ARITHM_RHS)
            throw new RuntimeException("Wrong type " + typename + " arithmetic expected in " + rhs);

        if (simpleTypeName.get(0).getTag() != TokenTag.BOOLEAN && rhs.get(0).getTag() == VarTag.BOOL_RHS)
            throw new RuntimeException("Wrong type " + typename + " boolean expected in " + rhs);

        if (simpleTypeName.get(0).getTag() != TokenTag.BOOLEAN && simpleTypeName.get(0).getTag() != VarTag.NUMERIC_TYPE)
            throw new RuntimeException("Wrong type " + typename + " bool or arithmetic expected in " + rhs);
    }

    private void analyzeExprRHS(Types type, RHSVar rhs) {
        switch (type) {
            case SHORT:
            case BYTE:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                if (rhs.get(0).getTag() != VarTag.ARITHM_RHS)
                    throw new RuntimeException("Incompatible types in " + rhs);
                break;
            case BOOLEAN:
                if (rhs.get(0).getTag() != TokenTag.BOOLEAN)
                    throw new RuntimeException("Incompatible types in " + rhs);
                break;
            default:
                throw new RuntimeException("Incompatible types in " + rhs);
        }
    }
}
