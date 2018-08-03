package ru.bmstu.ORM.Analyzer.Parser;

import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ColumnExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Cycle.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.IfClause.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Insert.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Select.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Update.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Query.Delete.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Raise.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Return.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.Variable.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.*;

public class Parser {
    private Scanner scanner;
    private SVar start;
    private Token sym;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.start = new SVar();
    }

    public SVar parse() throws CloneNotSupportedException {
        sym = scanner.nextToken();
        parseS(start);
        return start;
    }

    private void parse(TokenTag tag) throws CloneNotSupportedException {
        if (sym.getTag() == tag)
            sym = scanner.nextToken();
        else
            throw new RuntimeException(tag + " expected, got " + sym);
    }

    //S                    ::= (CREATE CreateTableFunctionTrigger)*
    private void parseS(SVar s) throws CloneNotSupportedException {
        while (sym.getTag() == TokenTag.CREATE) {
            s.addSymbol(sym);
            s.setStart(sym.getStart());
            parse(TokenTag.CREATE);

            CreateTableFunctionTriggerVar createTableFunctionTrigger = new CreateTableFunctionTriggerVar();
            parseCreateTableFunctionTrigger(createTableFunctionTrigger);
            s.addSymbol(createTableFunctionTrigger);
            s.setFollow(createTableFunctionTrigger.getFollow());
        }
        if (sym.getTag() != TokenTag.END_OF_PROGRAM)
            throw new RuntimeException("CREATE expected, got " + sym);
    }

    //CreateTableFunctionTrigger ::= (CreateTableStmt ';')
    //                           |   (CreateFunctionStmt ';')
    private void parseCreateTableFunctionTrigger(CreateTableFunctionTriggerVar createTableFunctionTrigger) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.TABLE) {
            CreateTableStmtVar createTableStmt = new CreateTableStmtVar();
            parseCreateTableStmt(createTableStmt);
            createTableFunctionTrigger.addSymbol(createTableStmt);
            createTableFunctionTrigger.setStart(createTableStmt.getStart());

        } else if (sym.getTag() == TokenTag.FUNCTION || sym.getTag() == TokenTag.OR) {
            CreateFunctionStmtVar createFunctionStmt = new CreateFunctionStmtVar();
            parseCreateFunctionStmt(createFunctionStmt);
            createTableFunctionTrigger.addSymbol(createFunctionStmt);
            createTableFunctionTrigger.setStart(createFunctionStmt.getStart());
        } else {
            throw new RuntimeException("TABLE, FUNCTION, TRIGGER expected, got " + sym);
        }

        createTableFunctionTrigger.addSymbol(sym);
        createTableFunctionTrigger.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //CreateTableStmt      ::= CREATE TABLE (IF NOT EXISTS)?
    //                         QualifiedName '(' (TableElement (',' TableElement)*)? ')'
    private void parseCreateTableStmt(CreateTableStmtVar createTableStmt) throws CloneNotSupportedException {
        createTableStmt.setStart(sym.getStart());

        createTableStmt.addSymbol(sym);
        parse(TokenTag.TABLE);

        if (sym.getTag() == TokenTag.IF) {
            createTableStmt.addSymbol(sym);
            parse(TokenTag.IF);

            createTableStmt.addSymbol(sym);
            parse(TokenTag.NOT);

            createTableStmt.addSymbol(sym);
            parse(TokenTag.EXISTS);
        }

        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        createTableStmt.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);

        createTableStmt.addSymbol(sym);
        parse(TokenTag.LPAREN);

        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER ||
                sym.getTag() == TokenTag.CONSTRAINT) {

            TableElementVar tableElement = new TableElementVar();
            createTableStmt.addSymbol(tableElement);
            parseTableElement(tableElement);

            TableElementVar tableElementVar;

            while (sym.getTag() == TokenTag.COMMA) {
                tableElement.addSymbol(sym);
                parse(TokenTag.COMMA);

                tableElementVar = new TableElementVar();
                createTableStmt.addSymbol(tableElementVar);
                parseTableElement(tableElementVar);
            }
        }

        createTableStmt.setFollow(sym.getFollow());
        createTableStmt.addSymbol(sym);
        parse(TokenTag.RPAREN);
    }

    //QualifiedName        ::= ColId ('.'ColId)*
    private void parseQualifiedName(QualifiedNameVar qualifiedName) throws CloneNotSupportedException {
        ColIdVar colId = new ColIdVar();
        qualifiedName.addSymbol(colId);
        parseColId(colId);

        qualifiedName.setStart(colId.getStart());

        ColIdVar colIdVar = colId;

        while (sym.getTag() == TokenTag.DOT) {
            if (!colIdVar.getFollow().equals(sym.getStart()))
                throw new RuntimeException("Wrong identifier at " + sym.getStart());

            qualifiedName.addSymbol(sym);
            parse(TokenTag.DOT);

            colIdVar = new ColIdVar();
            qualifiedName.addSymbol(colIdVar);
            parseColId(colIdVar);
        }

        qualifiedName.setFollow(colIdVar.getFollow());
    }

    //TODO UNSUPPORTED
    //ColId                ::= IDENT //UNSUPPORTED TILL | UnreservedKeyword | ColNameKeyword
    private void parseColId(ColIdVar colId) throws CloneNotSupportedException {
        colId.setStart(sym.getStart());
        colId.setFollow(sym.getFollow());
        colId.addSymbol(sym);
        parse(TokenTag.IDENTIFIER);
    }

    //TODO UNSUPPORTED FIRST(ColumnDef)
    //TableElement         ::= ColumnDef | TableConstraint
    private void parseTableElement(TableElementVar tableElement) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColumnDefVar columnDef = new ColumnDefVar();
            tableElement.addSymbol(columnDef);
            parseColumnDef(columnDef);
            tableElement.setStart(columnDef.getStart());
            tableElement.setFollow(columnDef.getFollow());
        } else {
            TableConstraintVar tableConstraint = new TableConstraintVar();
            tableElement.addSymbol(tableConstraint);
            parseTableConstraint(tableConstraint);
            tableElement.setStart(tableConstraint.getStart());
            tableElement.setFollow(tableConstraint.getFollow());
        }
    }

    //ColumnDef            ::= ColId Typename ColConstraint*
    private void parseColumnDef(ColumnDefVar columnDef) throws CloneNotSupportedException {
        ColIdVar colId = new ColIdVar();
        columnDef.addSymbol(colId);
        parseColId(colId);
        columnDef.setStart(colId.getStart());

        TypenameVar typename = new TypenameVar();
        columnDef.addSymbol(typename);
        parseTypename(typename);
        columnDef.setFollow(typename.getFollow());

        while (sym.getTag() == TokenTag.CONSTRAINT
                || sym.getTag() == TokenTag.NOT
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.UNIQUE
                || sym.getTag() == TokenTag.PRIMARY
                || sym.getTag() == TokenTag.CHECK
                || sym.getTag() == TokenTag.DEFAULT
                || sym.getTag() == TokenTag.REFERENCES) {

            ColConstraintVar colConstraint = new ColConstraintVar();
            columnDef.addSymbol(colConstraint);
            parseColConstraint(colConstraint);
            columnDef.setFollow(colConstraint.getFollow());
        }
    }

    private void parseIntConst() throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST)
            parse(TokenTag.BYTE_CONST);
        else if (sym.getTag() == TokenTag.SHORT_CONST)
            parse(TokenTag.SHORT_CONST);
        else if (sym.getTag() == TokenTag.INT_CONST)
            parse(TokenTag.INT_CONST);
        else if (sym.getTag() == TokenTag.LONG_CONST)
            parse(TokenTag.LONG_CONST);
        else
            throw new RuntimeException("Wrong number at " + sym + " int number expected");
    }

    //Typename             ::= SimpleTypename ArrayType?
    private void parseTypename(TypenameVar typename) throws CloneNotSupportedException {
        SimpleTypeNameVar simpleTypeName = new SimpleTypeNameVar();
        typename.addSymbol(simpleTypeName);
        parseSympleTypeName(simpleTypeName);
        typename.setCoords(simpleTypeName.getCoords());

        if (sym.getTag() == TokenTag.LBRACKET || sym.getTag() == TokenTag.ARRAY) {
            ArrayTypeVar arrayType = new ArrayTypeVar();
            parseArrayType(arrayType);
            typename.addSymbol(arrayType);
            typename.setFollow(arrayType.getFollow());
        }
    }

    //ArrayType            ::= ( '[' intConst? ']' )+    //intConst ни на что не влияет
    //                     |   ARRAY ('[' intConst ']')?
    private void parseArrayType(ArrayTypeVar arrayType) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LBRACKET) {
            while (sym.getTag() == TokenTag.LBRACKET) {
                arrayType.addSymbol(sym);
                parse(TokenTag.LBRACKET);

                if (sym.getTag() == TokenTag.BYTE_CONST
                        || sym.getTag() == TokenTag.SHORT_CONST
                        || sym.getTag() == TokenTag.INT_CONST
                        || sym.getTag() == TokenTag.LONG_CONST) {
                    arrayType.addSymbol(sym);
                    parseIntConst();
                }

                arrayType.setFollow(sym.getFollow());
                arrayType.addSymbol(sym);
                parse(TokenTag.RBRACKET);
            }
        } else if (sym.getTag() == TokenTag.ARRAY) {
            arrayType.setFollow(sym.getFollow());
            arrayType.addSymbol(sym);
            parse(TokenTag.ARRAY);

            if (sym.getTag() == TokenTag.LBRACKET) {
                arrayType.addSymbol(sym);
                parse(TokenTag.LBRACKET);

                arrayType.addSymbol(sym);
                parseIntConst();

                arrayType.setFollow(sym.getFollow());
                arrayType.addSymbol(sym);
                parse(TokenTag.RBRACKET);
            }
        } else {
            throw new RuntimeException("'[' or ARRAY expected, got " + sym);
        }
    }

    //SimpleTypename       ::= NumericType | CharacterType | DateTimeType | RECORD | BOOLEAN
    private void parseSympleTypeName(SimpleTypeNameVar simpleTypeName) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CHARACTER
                || sym.getTag() == TokenTag.CHAR
                || sym.getTag() == TokenTag.VARCHAR) {

            CharacterTypeVar characterType = new CharacterTypeVar();
            simpleTypeName.addSymbol(characterType);
            parseCharacterType(characterType);
            simpleTypeName.setCoords(characterType.getCoords());
        } else if (sym.getTag() == TokenTag.DATE
                || sym.getTag() == TokenTag.TIME
                || sym.getTag() == TokenTag.TIMESTAMP) {

            DateTimeTypeVar dateTimeType = new DateTimeTypeVar();
            simpleTypeName.addSymbol(dateTimeType);
            parseDateTimeType(dateTimeType);
            simpleTypeName.setCoords(dateTimeType.getCoords());
        } else if (sym.getTag() == TokenTag.RECORD) {
            simpleTypeName.addSymbol(sym);
            simpleTypeName.setCoords(sym.getCoords());
            parse(TokenTag.RECORD);
        } else if (sym.getTag() == TokenTag.BOOLEAN) {
            parse(TokenTag.BOOLEAN);
        } else {
            NumericTypeVar numericType = new NumericTypeVar();
            simpleTypeName.addSymbol(numericType);
            parseNumericType(numericType);
            simpleTypeName.setCoords(numericType.getCoords());
        }
    }

    //NumericType          ::= INT
    //                     |   INTEGER
    //                     |   SMALLINT
    //                     |   BIGINT
    //                     |   REAL
    //                     |   FLOAT  ( '('intConst')' )?
    //                     |   DOUBLE PRECISION
    //                     |   DECIMAL
    //                     |   NUMERIC
    //                     |   BOOLEAN
    private void parseNumericType(NumericTypeVar numericType) throws CloneNotSupportedException {
        numericType.setStart(sym.getStart());
        numericType.addSymbol(sym);
        numericType.setFollow(sym.getFollow());

        if (sym.getTag() == TokenTag.INT) {
            parse(TokenTag.INT);
        } else if (sym.getTag() == TokenTag.INTEGER) {
            parse(TokenTag.INTEGER);
        } else if (sym.getTag() == TokenTag.SMALLINT) {
            parse(TokenTag.SMALLINT);
        } else if (sym.getTag() == TokenTag.REAL) {
            parse(TokenTag.REAL);
        } else if (sym.getTag() == TokenTag.FLOAT) {
            parse(TokenTag.FLOAT);

            if (sym.getTag() == TokenTag.LPAREN) {
                numericType.addSymbol(sym);
                parse(TokenTag.LPAREN);

                Token number = sym;

                numericType.addSymbol(sym);
                parseIntConst();

                Number value = (Number) number.getValue();
                if (value.byteValue() < 1 || value.byteValue() > 53)
                    throw new RuntimeException("Number: " + number + " should be >= 1 and <= 53");

                numericType.addSymbol(sym);
                numericType.setFollow(sym.getFollow());
                parse(TokenTag.RPAREN);
            }
        } else if (sym.getTag() == TokenTag.DOUBLE) {
            parse(TokenTag.DOUBLE);

            numericType.setFollow(sym.getFollow());
            numericType.addSymbol(sym);
            parse(TokenTag.PRECISION);
        } else if (sym.getTag() == TokenTag.DECIMAL) {
            parse(TokenTag.DECIMAL);
        } else if (sym.getTag() == TokenTag.NUMERIC) {
            parse(TokenTag.NUMERIC);
        } else {
            throw new RuntimeException("Invalid type at " + sym);
        }
    }

    //CharacterType        ::= CharacterKeyword ( '(' intConst ')' )?
    private void parseCharacterType(CharacterTypeVar characterType) throws CloneNotSupportedException {
        CharacterKeywordVar characterKeyword = new CharacterKeywordVar();
        characterType.addSymbol(characterKeyword);
        parseCharacterKeyword(characterKeyword);
        characterType.setStart(characterKeyword.getStart());
        characterType.setFollow(characterKeyword.getFollow());

        if (sym.getTag() == TokenTag.LPAREN) {
            characterType.addSymbol(sym);
            parse(TokenTag.LPAREN);

            characterType.addSymbol(sym);
            parseIntConst();

            characterType.setFollow(sym.getFollow());
            characterType.addSymbol(sym);
            parse(TokenTag.RPAREN);
        }
    }

    //CharacterKeyword     ::= CHARACTER | CHAR | VARCHAR
    private void parseCharacterKeyword(CharacterKeywordVar characterKeyword) throws CloneNotSupportedException {
        characterKeyword.addSymbol(sym);
        characterKeyword.setStart(sym.getStart());
        characterKeyword.setFollow(sym.getFollow());

        if (sym.getTag() == TokenTag.CHARACTER) {
            parse(TokenTag.CHARACTER);
        } else if (sym.getTag() == TokenTag.CHAR) {
            parse(TokenTag.CHAR);
        } else if (sym.getTag() == TokenTag.VARCHAR) {
            parse(TokenTag.VARCHAR);
        } else {
            throw new RuntimeException("Invalid CHARACTER type at " + sym);
        }
    }

    //DateTimeType         ::= TIMESTAMP ( '(' intConst ')' )? //0 <= intConst < 6
    //                     |   TIME ( '(' intConst ')' )?      //0 <= intConst < 6
    //                     |   DATE
    private void parseDateTimeType(DateTimeTypeVar dateTimeType) throws CloneNotSupportedException {
        dateTimeType.addSymbol(sym);
        dateTimeType.setCoords(sym.getCoords());

        if (sym.getTag() == TokenTag.TIMESTAMP) {
            parse(TokenTag.TIMESTAMP);

            if (sym.getTag() == TokenTag.LPAREN) {
                dateTimeType.addSymbol(sym);
                parse(TokenTag.LPAREN);

                Token number = sym;

                dateTimeType.addSymbol(sym);
                parseIntConst();

                Number value = (Number) number.getValue();
                if (value.byteValue() < 0 || value.byteValue() >= 6)
                    throw new RuntimeException("Number: " + number + " should be >= 0 and < 6");

                dateTimeType.addSymbol(number);

                dateTimeType.addSymbol(sym);
                dateTimeType.setFollow(sym.getFollow());
                parse(TokenTag.RPAREN);
            }
        } else if (sym.getTag() == TokenTag.TIME) {
            parse(TokenTag.TIME);

            if (sym.getTag() == TokenTag.LPAREN) {
                dateTimeType.addSymbol(sym);
                parse(TokenTag.LPAREN);

                Token number = sym;

                dateTimeType.addSymbol(sym);
                parseIntConst();

                Number value = (Number) number.getValue();
                if (value.byteValue() < 0 || value.byteValue() >= 6)
                    throw new RuntimeException("Number: " + number + " should be >= 0 and < 6");

                dateTimeType.addSymbol(number);

                dateTimeType.addSymbol(sym);
                dateTimeType.setFollow(sym.getFollow());
                parse(TokenTag.RPAREN);
            }
        } else if (sym.getTag() == TokenTag.DATE) {
            parse(TokenTag.DATE);
        } else {
            throw new RuntimeException("Invalid Date/Time type at " + sym);
        }
    }

    //TableConstraint      ::= CONSTRAINT ColId ConstraintElem | ConstraintElem
    private void parseTableConstraint(TableConstraintVar tableConstraint) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CONSTRAINT) {
            tableConstraint.setStart(sym.getStart());
            tableConstraint.addSymbol(sym);
            parse(TokenTag.CONSTRAINT);

            ColIdVar colId = new ColIdVar();
            tableConstraint.addSymbol(colId);
            parseColId(colId);

            ConstraintElemVar constraintElem = new ConstraintElemVar();
            tableConstraint.addSymbol(constraintElem);
            parseConstraintElem(constraintElem);

            tableConstraint.setFollow(constraintElem.getFollow());
        } else {
            ConstraintElemVar constraintElem = new ConstraintElemVar();
            tableConstraint.addSymbol(constraintElem);
            parseConstraintElem(constraintElem);

            tableConstraint.setStart(constraintElem.getStart());
            tableConstraint.setFollow(constraintElem.getFollow());
        }
    }

    //ConstraintElem       ::= UNIQUE      '(' ColId (',' ColId)* ')'
    //                     |   PRIMARY KEY '(' ColId (',' ColId)* ')'
    //                     |   FOREIGN KEY '(' ColId (',' ColId)* ')' REFERENCES QualifiedName
    //                         ('(' ColId (',' ColId)* ')' )? KeyActions
    private void parseConstraintElem(ConstraintElemVar constraintElem) throws CloneNotSupportedException {
        constraintElem.addSymbol(sym);
        constraintElem.setStart(sym.getStart());

        if (sym.getTag() == TokenTag.UNIQUE) {
            parse(TokenTag.UNIQUE);

            constraintElem.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            constraintElem.addSymbol(colId);
            parseColId(colId);

            while (sym.getTag() == TokenTag.COMMA) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.COMMA);

                ColIdVar colIdVar = new ColIdVar();
                constraintElem.addSymbol(colIdVar);
                parseColId(colIdVar);
            }

            constraintElem.addSymbol(sym);
            constraintElem.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.PRIMARY) {
            parse(TokenTag.PRIMARY);

            constraintElem.addSymbol(sym);
            parse(TokenTag.KEY);

            constraintElem.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            constraintElem.addSymbol(colId);
            parseColId(colId);

            while (sym.getTag() == TokenTag.COMMA) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.COMMA);

                ColIdVar colIdVar = new ColIdVar();
                constraintElem.addSymbol(colIdVar);
                parseColId(colIdVar);
            }

            constraintElem.addSymbol(sym);
            constraintElem.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.FOREIGN) {
            parse(TokenTag.FOREIGN);

            constraintElem.addSymbol(sym);
            parse(TokenTag.KEY);

            constraintElem.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            constraintElem.addSymbol(colId);
            parseColId(colId);

            while (sym.getTag() == TokenTag.COMMA) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.COMMA);

                ColIdVar colIdVar = new ColIdVar();
                constraintElem.addSymbol(colIdVar);
                parseColId(colIdVar);
            }

            constraintElem.addSymbol(sym);
            parse(TokenTag.RPAREN);

            constraintElem.addSymbol(sym);
            parse(TokenTag.REFERENCES);

            QualifiedNameVar qualifiedName = new QualifiedNameVar();
            constraintElem.addSymbol(qualifiedName);
            parseQualifiedName(qualifiedName);
            constraintElem.setFollow(qualifiedName.getFollow());

            if (sym.getTag() == TokenTag.LPAREN) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.LPAREN);

                ColIdVar colId1 = new ColIdVar();
                constraintElem.addSymbol(colId1);
                parseColId(colId1);

                while (sym.getTag() == TokenTag.COMMA) {
                    constraintElem.addSymbol(sym);
                    parse(TokenTag.COMMA);

                    ColIdVar colIdVar = new ColIdVar();
                    constraintElem.addSymbol(colIdVar);
                    parseColId(colIdVar);
                }

                constraintElem.setFollow(sym.getFollow());
                constraintElem.addSymbol(sym);
                parse(TokenTag.RPAREN);
            }

            if (sym.getTag() == TokenTag.ON) {
                KeyActionsVar keyActions = new KeyActionsVar();
                constraintElem.addSymbol(keyActions);
                parseKeyActions(keyActions);
                constraintElem.setFollow(keyActions.getFollow());
            }
        }
    }

    //ColConstraint        ::= CONSTRAINT ColId ColConstraintElem | ColConstraintElem
    private void parseColConstraint(ColConstraintVar colConstraint) throws  CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CONSTRAINT) {
            colConstraint.addSymbol(sym);
            colConstraint.setStart(sym.getStart());
            parse(TokenTag.CONSTRAINT);

            ColIdVar colId = new ColIdVar();
            colConstraint.addSymbol(colId);
            parseColId(colId);

            ColConstraintElemVar colConstraintElem = new ColConstraintElemVar();
            colConstraint.addSymbol(colConstraintElem);
            parseColConstraintElem(colConstraintElem);
            colConstraint.setFollow(colConstraintElem.getFollow());
        } else {
            ColConstraintElemVar colConstraintElem = new ColConstraintElemVar();
            colConstraint.addSymbol(colConstraintElem);
            parseColConstraintElem(colConstraintElem);
            colConstraint.setCoords(colConstraintElem.getCoords());
        }
    }

    //ColConstraintElem    ::= NOT NULL
    //                     |   NULL
    //                     |   UNIQUE
    //                     |   PRIMARY KEY
    //                     |   CHECK '(' BoolExpr ')'    //TODO HERE NEED TO CHECK APPLICATION OF OPs
    //                     |   DEFAULT ConstExpr         //TODO ARITHMETIC, BOOL ONLY EXPR OR VALUE TILL
    //                     |   REFERENCES  QualifiedName ( '(' ColId (',' ColId)* ')' )? KeyActions?
    private void parseColConstraintElem(ColConstraintElemVar colConstraintElem) throws CloneNotSupportedException {
        colConstraintElem.addSymbol(sym);
        colConstraintElem.setStart(sym.getStart());

        if (sym.getTag() == TokenTag.NOT) {
            parse(TokenTag.NOT);

            colConstraintElem.setFollow(sym.getFollow());
            colConstraintElem.addSymbol(sym);
            parse(TokenTag.NULL);
        } else if (sym.getTag() == TokenTag.NULL) {
            colConstraintElem.setFollow(sym.getFollow());
            parse(TokenTag.NULL);
        } else if (sym.getTag() == TokenTag.UNIQUE) {
            colConstraintElem.setFollow(sym.getFollow());
            parse(TokenTag.UNIQUE);
        } else if (sym.getTag() == TokenTag.PRIMARY) {
            parse(TokenTag.PRIMARY);

            colConstraintElem.setFollow(sym.getFollow());
            colConstraintElem.addSymbol(sym);
            parse(TokenTag.KEY);
        } else if (sym.getTag() == TokenTag.CHECK) {
            parse(TokenTag.CHECK);

            colConstraintElem.addSymbol(sym);
            parse(TokenTag.LPAREN);

            BoolExprVar boolExpr = new BoolExprVar();
            colConstraintElem.addSymbol(boolExpr);
            parseBoolExpr(boolExpr);

            colConstraintElem.setFollow(sym.getFollow());
            colConstraintElem.addSymbol(sym);
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.DEFAULT) {
            parse(TokenTag.DEFAULT);

            ConstExprVar constExpr = new ConstExprVar();
            colConstraintElem.addSymbol(constExpr);
            parseConstExpr(constExpr);
            colConstraintElem.setFollow(constExpr.getFollow());
        } else if (sym.getTag() == TokenTag.REFERENCES) {
            parse(TokenTag.REFERENCES);

            QualifiedNameVar qualifiedName = new QualifiedNameVar();
            colConstraintElem.addSymbol(qualifiedName);
            parseQualifiedName(qualifiedName);
            colConstraintElem.setFollow(qualifiedName.getFollow());
        } else {
            throw new RuntimeException("Invalid column constraint at " + sym);
        }
    }

    //KeyActions           ::= ON UPDATE KeyAction (ON DELETE KeyAction)?
    //                     |   ON DELETE KeyAction (ON UPDATE KeyAction)?
    private void parseKeyActions(KeyActionsVar keyActions) throws CloneNotSupportedException {
        keyActions.addSymbol(sym);
        keyActions.setStart(sym.getStart());
        parse(TokenTag.ON);

        if (sym.getTag() == TokenTag.UPDATE) {
            keyActions.addSymbol(sym);
            parse(TokenTag.UPDATE);

            KeyActionVar keyAction = new KeyActionVar();
            keyActions.addSymbol(keyAction);
            parseKeyAction(keyAction);
            keyActions.setFollow(keyAction.getFollow());

            if (sym.getTag() == TokenTag.ON) {
                keyActions.addSymbol(sym);
                parse(TokenTag.ON);

                keyActions.addSymbol(sym);
                parse(TokenTag.DELETE);

                KeyActionVar keyActionVar = new KeyActionVar();
                keyActions.addSymbol(keyActionVar);
                parseKeyAction(keyActionVar);
                keyActions.setFollow(keyActionVar.getFollow());
            }
        } else if (sym.getTag() == TokenTag.DELETE) {
            keyActions.addSymbol(sym);
            parse(TokenTag.DELETE);

            KeyActionVar keyAction = new KeyActionVar();
            keyActions.addSymbol(keyAction);
            parseKeyAction(keyAction);
            keyActions.setFollow(keyAction.getFollow());

            if (sym.getTag() == TokenTag.ON) {
                keyActions.addSymbol(sym);
                parse(TokenTag.ON);

                keyActions.addSymbol(sym);
                parse(TokenTag.UPDATE);

                KeyActionVar keyActionVar = new KeyActionVar();
                keyActions.addSymbol(keyActionVar);
                parseKeyAction(keyActionVar);
                keyActions.setFollow(keyActionVar.getFollow());
            }
        } else {
            throw new RuntimeException("Invalid key action syntax at " + sym);
        }
    }

    //KeyAction            ::= NO ACTION | RESTRICT | CASCADE | SET NULL | SET DEFAULT
    private void parseKeyAction(KeyActionVar keyAction) throws CloneNotSupportedException {
        keyAction.addSymbol(sym);
        keyAction.setStart(sym.getStart());
        keyAction.setFollow(sym.getFollow());

        if (sym.getTag() == TokenTag.NO) {
            parse(TokenTag.NO);

            keyAction.setFollow(sym.getFollow());
            keyAction.addSymbol(sym);
            parse(TokenTag.ACTION);
        } else if (sym.getTag() == TokenTag.RESTRICT) {
            parse(TokenTag.RESTRICT);
        } else if (sym.getTag() == TokenTag.CASCADE) {
            parse(TokenTag.CASCADE);
        } else if (sym.getTag() == TokenTag.SET) {
            parse(TokenTag.SET);
            keyAction.addSymbol(sym);
            keyAction.setFollow(sym.getFollow());

            if (sym.getTag() == TokenTag.NULL) {
                parse(TokenTag.NULL);
            } else if (sym.getTag() == TokenTag.DEFAULT) {
                parse(TokenTag.DEFAULT);
            } else {
                throw new RuntimeException("Invalid action at " + sym);
            }
        } else {
            throw new RuntimeException("Invalid action at " + sym);
        }
    }

    //Expr                 ::= ColExpr | CharacterValue | DateValue
    private void parseExpr(ExprVar expr) throws CloneNotSupportedException {
        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN
                || sym.getTag() == TokenTag.NOT
                || sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL) {
            ColExprVar colExpr = new ColExprVar();
            expr.addSymbol(colExpr);
            parseColExpr(colExpr);
            expr.setCoords(colExpr.getCoords());
        } else if (sym.getTag() == TokenTag.STRING_CONST) {
            expr.addSymbol(sym);
            expr.setCoords(sym.getCoords());
            parse(TokenTag.STRING_CONST);
        } else if (sym.getTag() == TokenTag.DATE_CONST) {
            expr.addSymbol(sym);
            expr.setCoords(sym.getCoords());
            parse(TokenTag.DATE_CONST);
        } else {
            throw new RuntimeException("Expression, string or datetime expected, got " + sym);
        }
    }

    //ColExpr              ::= ColExprTerm ({'+' | '-' | OR} ColExprTerm)*
    private void parseColExpr(ColExprVar colExpr) throws CloneNotSupportedException {
        ColExprTermVar colExprTerm = new ColExprTermVar();
        colExpr.addSymbol(colExprTerm);
        parseColExprTerm(colExprTerm);
        colExpr.setCoords(colExprTerm.getCoords());

        while (sym.getTag() == TokenTag.ADD
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.OR) {
            colExpr.addSymbol(sym);
            if (sym.getTag() == TokenTag.ADD)
                parse(TokenTag.ADD);
            else if (sym.getTag() == TokenTag.SUB)
                parse(TokenTag.SUB);
            else
                parse(TokenTag.OR);

            ColExprTermVar colExprTermVar = new ColExprTermVar();
            colExpr.addSymbol(colExprTermVar);
            parseColExprTerm(colExprTermVar);
            colExpr.setFollow(colExprTermVar.getFollow());
        }
    }

    //ColExprTerm          ::= ColExprFactor ({'*' | '/' | AND} ColExprFactor)*
    private void parseColExprTerm(ColExprTermVar colExprTerm) throws CloneNotSupportedException {
        ColExprFactorVar colExprFactor = new ColExprFactorVar();
        colExprTerm.addSymbol(colExprFactor);
        parseColExprFactor(colExprFactor);
        colExprTerm.setCoords(colExprFactor.getCoords());

        while (sym.getTag() == TokenTag.MUL
                || sym.getTag() == TokenTag.DIV
                || sym.getTag() == TokenTag.AND) {
            colExprTerm.addSymbol(sym);
            if (sym.getTag() == TokenTag.MUL)
                parse(TokenTag.MUL);
            else if (sym.getTag() == TokenTag.DIV)
                parse(TokenTag.DIV);
            else
                parse(TokenTag.AND);

            ColExprFactorVar colExprFactorVar = new ColExprFactorVar();
            colExprTerm.addSymbol(colExprFactorVar);
            parseColExprFactor(colExprFactorVar);
            colExprTerm.setFollow(colExprFactorVar.getFollow());
        }
    }

    //ColExprFactor        ::= ColId             RHS?
    //                     |   '-' ColExprFactor ArithmRHS?
    //                     |   '(' ColExpr ')'   RHS?
    //                     |   NOT ColExprFactor BoolRHS?
    //                     |   NumericValue      ArithmRHS?
    //                     |   BoolConst         BoolRHS?
    private void parseColExprFactor(ColExprFactorVar colExprFactor) throws CloneNotSupportedException {
        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColIdVar colId = new ColIdVar();
            colExprFactor.addSymbol(colId);
            parseColId(colId);
            colExprFactor.setCoords(colId.getCoords());

            if (sym.getTag() == TokenTag.LESS
                    || sym.getTag() == TokenTag.LESSEQ
                    || sym.getTag() == TokenTag.GREATER
                    || sym.getTag() == TokenTag.GREATEREQ
                    || sym.getTag() == TokenTag.EQUAL
                    || sym.getTag() == TokenTag.NOTEQUAL
                    || sym.getTag() == TokenTag.BETWEEN
                    || sym.getTag() == TokenTag.IS) {
                RHSVar rhs = new RHSVar();
                colExprFactor.addSymbol(rhs);
                parseRHS(rhs);
                colExprFactor.setFollow(rhs.getFollow());
            }
        } else if (sym.getTag() == TokenTag.SUB) {
            colExprFactor.addSymbol(sym);
            colExprFactor.setStart(sym.getStart());
            parse(TokenTag.SUB);

            ColExprFactorVar colExprFactorVar = new ColExprFactorVar();
            colExprFactor.addSymbol(colExprFactorVar);
            parseColExprFactor(colExprFactorVar);
            colExprFactor.setFollow(colExprFactorVar.getFollow());

            if (sym.getTag() == TokenTag.LESS
                    || sym.getTag() == TokenTag.LESSEQ
                    || sym.getTag() == TokenTag.GREATER
                    || sym.getTag() == TokenTag.GREATEREQ
                    || sym.getTag() == TokenTag.EQUAL
                    || sym.getTag() == TokenTag.NOTEQUAL
                    || sym.getTag() == TokenTag.BETWEEN) {
                ArithmRHSVar arithmRHS = new ArithmRHSVar();
                colExprFactor.addSymbol(arithmRHS);
                parseArithmRHS(arithmRHS);
                colExprFactor.setFollow(arithmRHS.getFollow());
            }
        } else if (sym.getTag() == TokenTag.LPAREN) {
            colExprFactor.addSymbol(sym);
            colExprFactor.setStart(sym.getStart());
            parse(TokenTag.LPAREN);

            ColExprVar colExpr = new ColExprVar();
            colExprFactor.addSymbol(colExpr);
            parseColExpr(colExpr);

            colExprFactor.addSymbol(sym);
            colExprFactor.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);

            if (sym.getTag() == TokenTag.LESS
                    || sym.getTag() == TokenTag.LESSEQ
                    || sym.getTag() == TokenTag.GREATER
                    || sym.getTag() == TokenTag.GREATEREQ
                    || sym.getTag() == TokenTag.EQUAL
                    || sym.getTag() == TokenTag.NOTEQUAL
                    || sym.getTag() == TokenTag.BETWEEN
                    || sym.getTag() == TokenTag.IS) {
                RHSVar rhs = new RHSVar();
                colExprFactor.addSymbol(rhs);
                parseRHS(rhs);
                colExprFactor.setFollow(rhs.getFollow());
            }
        } else if (sym.getTag() == TokenTag.NOT) {
            colExprFactor.addSymbol(sym);
            colExprFactor.setStart(sym.getStart());
            parse(TokenTag.NOT);

            ColExprFactorVar colExprFactorVar = new ColExprFactorVar();
            colExprFactor.addSymbol(colExprFactorVar);
            parseColExprFactor(colExprFactorVar);
            colExprFactor.setFollow(colExprFactorVar.getFollow());

            if (sym.getTag() == TokenTag.IS) {
                BoolRHSVar boolRHS = new BoolRHSVar();
                colExprFactor.addSymbol(boolRHS);
                parseBoolRHS(boolRHS);
                colExprFactor.setFollow(boolRHS.getFollow());
            }
        } else if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST) {
            colExprFactor.addSymbol(sym);
            colExprFactor.setCoords(sym.getCoords());
            parseNumber();

            if (sym.getTag() == TokenTag.LESS
                    || sym.getTag() == TokenTag.LESSEQ
                    || sym.getTag() == TokenTag.GREATER
                    || sym.getTag() == TokenTag.GREATEREQ
                    || sym.getTag() == TokenTag.EQUAL
                    || sym.getTag() == TokenTag.NOTEQUAL
                    || sym.getTag() == TokenTag.BETWEEN) {
                ArithmRHSVar arithmRHS = new ArithmRHSVar();
                colExprFactor.addSymbol(arithmRHS);
                parseArithmRHS(arithmRHS);
                colExprFactor.setFollow(arithmRHS.getFollow());
            }
        } else if (sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL) {
            BoolConstVar boolConst = new BoolConstVar();
            colExprFactor.addSymbol(boolConst);
            parseBoolConst(boolConst);
            colExprFactor.setCoords(boolConst.getCoords());

            if (sym.getTag() == TokenTag.IS) {
                BoolRHSVar boolRHS = new BoolRHSVar();
                colExprFactor.addSymbol(boolRHS);
                parseBoolRHS(boolRHS);
                colExprFactor.setFollow(boolRHS.getFollow());
            }
        }
    }

    //ConstExpr           ::= ArithmConstExpr | BoolConst | CharacterValue | DateValue | '(' ConstExpr ')'
    private void parseConstExpr(ConstExprVar constExpr) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.SUB) {

            ArithmConstExprVar arithmExprNoVar = new ArithmConstExprVar();
            constExpr.addSymbol(arithmExprNoVar);
            parseArithmConstExpr(arithmExprNoVar);
            constExpr.setCoords(arithmExprNoVar.getCoords());
        } else if (sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.NOT) {

            boolean wasNot = false;
            if (sym.getTag() == TokenTag.NOT) {
                constExpr.addSymbol(sym);
                constExpr.setStart(sym.getStart());
                parse(TokenTag.NOT);
                wasNot = true;
            }

            BoolConstVar boolConst = new BoolConstVar();
            constExpr.addSymbol(boolConst);
            parseBoolConst(boolConst);

            if (wasNot)
                constExpr.setFollow(boolConst.getFollow());
            else
                constExpr.setCoords(boolConst.getCoords());
        } else if (sym.getTag() == TokenTag.STRING_CONST) {
            constExpr.addSymbol(sym);
            constExpr.setCoords(sym.getCoords());
            parse(TokenTag.STRING_CONST);
        } else if (sym.getTag() == TokenTag.TIME_CONST
                || sym.getTag() == TokenTag.TIMESTAMP_CONST
                || sym.getTag() == TokenTag.DATE_CONST) {

            constExpr.addSymbol(sym);
            constExpr.setCoords(sym.getCoords());
            if (sym.getTag() == TokenTag.TIMESTAMP_CONST)
                parse(TokenTag.TIMESTAMP_CONST);
            else if (sym.getTag() == TokenTag.DATE_CONST)
                parse(TokenTag.DATE_CONST);
            else
                parse(TokenTag.TIME_CONST);
        } else {
            throw new RuntimeException("Expected expression without variables, got" + sym);
        }
    }

    //ArithmConstExpr      ::= ArithmConstExprTerm ( {'+' | '-'} ArithmConstExprTerm )*
    private void parseArithmConstExpr(ArithmConstExprVar arithmExprNoVar) throws CloneNotSupportedException {
        ArithmConstExprTermVar arithmExprNoVarTerm = new ArithmConstExprTermVar();
        arithmExprNoVar.addSymbol(arithmExprNoVarTerm);
        parseArithmConstExprTerm(arithmExprNoVarTerm);
        arithmExprNoVar.setCoords(arithmExprNoVarTerm.getCoords());

        while (sym.getTag() == TokenTag.ADD || sym.getTag() == TokenTag.SUB) {
            arithmExprNoVar.addSymbol(sym);

            if (sym.getTag() == TokenTag.ADD)
                parse(TokenTag.ADD);
            else
                parse(TokenTag.SUB);

            ArithmConstExprTermVar arithmConstExprTermVar = new ArithmConstExprTermVar();
            arithmExprNoVar.addSymbol(arithmConstExprTermVar);
            parseArithmConstExprTerm(arithmConstExprTermVar);
            arithmExprNoVar.setFollow(arithmConstExprTermVar.getFollow());
        }
    }

    //ArithmConstExprTerm  ::= ArithmConstExprFactor ( {'*' | '/'} ArithmConstExprFactor )*
    private void parseArithmConstExprTerm(ArithmConstExprTermVar arithmExprNoVarTerm) throws CloneNotSupportedException {
        ArithmConstExprFactorVar arithmExprNoVarFactor = new ArithmConstExprFactorVar();
        arithmExprNoVarTerm.addSymbol(arithmExprNoVarFactor);
        parseArithmConstExprFactor(arithmExprNoVarFactor);
        arithmExprNoVarTerm.setCoords(arithmExprNoVarFactor.getCoords());

        while (sym.getTag() == TokenTag.MUL || sym.getTag() == TokenTag.DIV) {
            arithmExprNoVarTerm.addSymbol(sym);

            if (sym.getTag() == TokenTag.MUL)
                parse(TokenTag.MUL);
            else
                parse(TokenTag.DIV);

            ArithmConstExprFactorVar arithmConstExprFactorVar = new ArithmConstExprFactorVar();
            arithmExprNoVarTerm.addSymbol(arithmConstExprFactorVar);
            parseArithmConstExprFactor(arithmConstExprFactorVar);
            arithmExprNoVarTerm.setFollow(arithmConstExprFactorVar.getFollow());
        }
    }

    //ArithmConstExprFactor::= NumericValue | '-' ArithmConstExprFactor | '(' ArithmConstExpr ')'
    private void parseArithmConstExprFactor(ArithmConstExprFactorVar arithmExprNoVarFactor) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST) {

            arithmExprNoVarFactor.setCoords(sym.getCoords());
            arithmExprNoVarFactor.addSymbol(sym);
            parseNumber();
        } else if (sym.getTag() == TokenTag.SUB) {
            arithmExprNoVarFactor.addSymbol(sym);
            arithmExprNoVarFactor.setStart(sym.getStart());
            parse(TokenTag.SUB);

            ArithmConstExprFactorVar arithmConstExprFactorVar = new ArithmConstExprFactorVar();
            arithmExprNoVarFactor.addSymbol(arithmConstExprFactorVar);
            parseArithmConstExprFactor(arithmConstExprFactorVar);
            arithmExprNoVarFactor.setFollow(arithmConstExprFactorVar.getFollow());
        } else {
            arithmExprNoVarFactor.setStart(sym.getStart());
            arithmExprNoVarFactor.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ArithmConstExprVar arithmExprNoVar = new ArithmConstExprVar();
            arithmExprNoVarFactor.addSymbol(arithmExprNoVar);
            parseArithmConstExpr(arithmExprNoVar);

            arithmExprNoVarFactor.setFollow(sym.getFollow());
            arithmExprNoVarFactor.addSymbol(sym);
            parse(TokenTag.RPAREN);
        }
    }

    private void parseNumber() throws CloneNotSupportedException {
        //var.addSymbol(var);

        if (sym.getTag() == TokenTag.BYTE_CONST)
            parse(TokenTag.BYTE_CONST);
        else if (sym.getTag() == TokenTag.SHORT_CONST)
            parse(TokenTag.SHORT_CONST);
        else if (sym.getTag() == TokenTag.INT_CONST)
            parse(TokenTag.INT_CONST);
        else if (sym.getTag() == TokenTag.LONG_CONST)
            parse(TokenTag.LONG_CONST);
        else if (sym.getTag() == TokenTag.FLOAT_CONST)
            parse(TokenTag.FLOAT_CONST);
        else if (sym.getTag() == TokenTag.DOUBLE_CONST)
            parse(TokenTag.DOUBLE_CONST);
        else
            throw new RuntimeException("Number expected, got " + sym);
    }

    //BoolConstExpr       ::= BoolConstExprTerm (OR BoolConstExprTerm)*
    private void parseBoolConstExpr(BoolConstExprVar boolConstExpr) throws CloneNotSupportedException {
        BoolConstExprTermVar boolConstExprTerm = new BoolConstExprTermVar();
        boolConstExpr.addSymbol(boolConstExprTerm);
        parseBoolConstExprTerm(boolConstExprTerm);
        boolConstExpr.setCoords(boolConstExprTerm.getCoords());

        while (sym.getTag() == TokenTag.OR) {
            boolConstExpr.addSymbol(sym);
            parse(TokenTag.OR);

            BoolConstExprTermVar boolConstExprTermVar = new BoolConstExprTermVar();
            boolConstExpr.addSymbol(boolConstExprTermVar);
            parseBoolConstExprTerm(boolConstExprTermVar);
            boolConstExpr.setFollow(boolConstExprTermVar.getFollow());
        }
    }

    //BoolConstExprTerm   ::= BoolConstExprFactor (AND BoolConstExprFactor)*
    private void parseBoolConstExprTerm(BoolConstExprTermVar boolConstExprTerm) throws CloneNotSupportedException {
        BoolConstExprFactorVar boolConstExprFactor = new BoolConstExprFactorVar();
        boolConstExprTerm.addSymbol(boolConstExprFactor);
        parseBoolConstExprFactor(boolConstExprFactor);
        boolConstExprTerm.setCoords(boolConstExprFactor.getCoords());

        while (sym.getTag() == TokenTag.AND) {
            boolConstExprTerm.addSymbol(sym);
            parse(TokenTag.AND);

            BoolConstExprFactorVar boolConstExprFactorVar = new BoolConstExprFactorVar();
            boolConstExprTerm.addSymbol(boolConstExprFactorVar);
            parseBoolConstExprFactor(boolConstExprFactorVar);
            boolConstExprTerm.setFollow(boolConstExprFactorVar.getFollow());
        }
    }

    //BoolConstExprFactor ::= BoolConst BoolRHS?
    //                    | NOT BoolConstExprFactor BoolRHS?
    //                    | ArithmConstExpr ArithmConstRHS
    private void parseBoolConstExprFactor(BoolConstExprFactorVar boolConstExprFactor) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL) {
            BoolConstVar boolConst = new BoolConstVar();
            boolConstExprFactor.addSymbol(boolConst);
            parseBoolConst(boolConst);
            boolConstExprFactor.setCoords(boolConst.getCoords());

            if (sym.getTag() == TokenTag.IS) {
                BoolRHSVar boolRHS = new BoolRHSVar();
                boolConstExprFactor.addSymbol(boolRHS);
                parseBoolRHS(boolRHS);
                boolConstExprFactor.setFollow(boolRHS.getFollow());
            }
        } else if (sym.getTag() == TokenTag.NOT) {
            boolConstExprFactor.addSymbol(sym);
            boolConstExprFactor.setStart(sym.getStart());
            parse(TokenTag.NOT);

            BoolConstExprFactorVar boolConstExprFactorVar = new BoolConstExprFactorVar();
            boolConstExprFactor.addSymbol(boolConstExprFactorVar);
            parseBoolConstExprFactor(boolConstExprFactorVar);
            boolConstExprFactor.setFollow(boolConstExprFactorVar.getFollow());

            if (sym.getTag() == TokenTag.IS) {
                BoolRHSVar boolRHS = new BoolRHSVar();
                boolConstExprFactor.addSymbol(boolRHS);
                parseBoolRHS(boolRHS);
                boolConstExprFactor.setFollow(boolRHS.getFollow());
            }
        } else if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.SUB) {
            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            boolConstExprFactor.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            boolConstExprFactor.setStart(arithmConstExpr.getStart());

            ArithmConstRHSVar arithmConstRHS = new ArithmConstRHSVar();
            boolConstExprFactor.addSymbol(arithmConstRHS);
            parseArithmConstRHS(arithmConstRHS);
            boolConstExprFactor.setFollow(arithmConstRHS.getFollow());
        } else {
            throw new RuntimeException("Bool const expression expected, got " + sym);
        }
    }

    //ArithmConstRHS      ::= '<'  ArithmConstExpr
    //                    |   '<=' ArithmConstExpr
    //                    |   '>'  ArithmConstExpr
    //                    |   '>=' ArithmConstExpr
    //                    |   '='  ArithmConstExpr
    //                    |   '!=' ArithmConstExpr
    //                    |   BETWEEN ArithmConstExpr AND ArithmConstExpr     // ARITHMETIC ONLY TILL
    private void parseArithmConstRHS(ArithmConstRHSVar arithmConstRHS) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LESS) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.LESS);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmConstRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.LESSEQ) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.LESSEQ);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmConstRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.GREATER) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.GREATER);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmConstRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.GREATEREQ) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.GREATEREQ);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmConstRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.EQUAL) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.EQUAL);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmConstRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.NOTEQUAL) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.NOTEQUAL);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmConstRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.BETWEEN) {
            arithmConstRHS.addSymbol(sym);
            arithmConstRHS.setStart(sym.getStart());
            parse(TokenTag.BETWEEN);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);

            arithmConstRHS.addSymbol(sym);
            parse(TokenTag.AND);

            ArithmConstExprVar arithmConstExprVar = new ArithmConstExprVar();
            arithmConstRHS.addSymbol(arithmConstExprVar);
            parseArithmConstExpr(arithmConstExprVar);
            arithmConstRHS.setFollow(arithmConstExprVar.getFollow());
        } else {
            throw new RuntimeException("Compare operators or BETWEEN expected, got " + sym);
        }
    }

    //BoolRHS             ::= IS NOT? BoolConst
    private void parseBoolRHS(BoolRHSVar boolRHS) throws CloneNotSupportedException {
        boolRHS.addSymbol(sym);
        boolRHS.setStart(sym.getStart());
        parse(TokenTag.IS);

        if (sym.getTag() == TokenTag.NOT) {
            boolRHS.addSymbol(sym);
            parse(TokenTag.NOT);
        }

        BoolConstVar boolConst = new BoolConstVar();
        boolRHS.addSymbol(boolConst);
        parseBoolConst(boolConst);
        boolRHS.setFollow(boolConst.getFollow());
    }

    //BoolExpr             ::= BoolExprTerm (OR BoolExprTerm)*
    private void parseBoolExpr(BoolExprVar boolExpr) throws CloneNotSupportedException {
        BoolExprTermVar boolExprTerm = new BoolExprTermVar();
        boolExpr.addSymbol(boolExprTerm);
        parseBoolExprTerm(boolExprTerm);
        boolExpr.setCoords(boolExprTerm.getCoords());

        while (sym.getTag() == TokenTag.OR) {
            boolExpr.addSymbol(sym);
            parse(TokenTag.OR);

            BoolExprTermVar boolExprTermVar = new BoolExprTermVar();
            boolExpr.addSymbol(boolExprTermVar);
            parseBoolExprTerm(boolExprTermVar);
            boolExpr.setFollow(boolExprTermVar.getFollow());
        }
    }

    //BoolExprTerm         ::= BoolExprFactor (AND BoolExprFactor)*
    private void parseBoolExprTerm(BoolExprTermVar boolExprTerm) throws CloneNotSupportedException {
        BoolExprFactorVar boolExprFactor = new BoolExprFactorVar();
        boolExprTerm.addSymbol(boolExprFactor);
        parseBoolExprFactor(boolExprFactor);
        boolExprTerm.setCoords(boolExprFactor.getCoords());

        while (sym.getTag() == TokenTag.AND) {
            boolExprTerm.addSymbol(sym);
            parse(TokenTag.AND);

            BoolExprFactorVar boolExprFactorVar = new BoolExprFactorVar();
            boolExprTerm.addSymbol(boolExprFactorVar);
            parseBoolExprFactor(boolExprFactorVar);
            boolExprTerm.setFollow(boolExprFactorVar.getFollow());
        }
    }

    //BoolExprFactor       ::= BoolConst BoolRHS?
    //                     |   NOT BoolExprFactor BoolRHS?
    //                     |   '(' BoolExpr ')' BoolRHS?
    //                     |   ColId RHS?                 //Check Type of Col here
    //                     |   ArithmConstExpr ArithmRHS
    private void parseBoolExprFactor(BoolExprFactorVar boolExprFactor) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.NOT) {
            boolExprFactor.addSymbol(sym);
            boolExprFactor.setStart(sym.getStart());
            parse(TokenTag.NOT);

            BoolExprFactorVar boolExprFactorVar = new BoolExprFactorVar();
            boolExprFactor.addSymbol(boolExprFactorVar);
            parseBoolExprFactor(boolExprFactorVar);
            boolExprFactor.setFollow(boolExprFactorVar.getFollow());
        } else if (sym.getTag() == TokenTag.LPAREN) {
            boolExprFactor.setStart(sym.getStart());
            boolExprFactor.addSymbol(sym);
            parse(TokenTag.LPAREN);

            BoolExprVar boolExpr = new BoolExprVar();
            boolExprFactor.addSymbol(boolExpr);
            parseBoolExpr(boolExpr);

            boolExprFactor.addSymbol(sym);
            boolExprFactor.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        //TODO FIRST(ColId)
        } else if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColIdVar colId = new ColIdVar();
            boolExprFactor.addSymbol(colId);
            parseColId(colId);
            boolExprFactor.setCoords(colId.getCoords());

            if (sym.getTag() == TokenTag.IS
                    || sym.getTag() == TokenTag.LESS
                    || sym.getTag() == TokenTag.LESSEQ
                    || sym.getTag() == TokenTag.GREATER
                    || sym.getTag() == TokenTag.GREATEREQ
                    || sym.getTag() == TokenTag.EQUAL
                    || sym.getTag() == TokenTag.NOTEQUAL
                    || sym.getTag() == TokenTag.BETWEEN) {
                RHSVar rhs = new RHSVar();
                boolExprFactor.addSymbol(rhs);
                parseRHS(rhs);
                boolExprFactor.setFollow(rhs.getFollow());
            }
        } else if (sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL){
            BoolConstVar boolConst = new BoolConstVar();
            boolExprFactor.addSymbol(boolConst);
            parseBoolConst(boolConst);
            boolExprFactor.setCoords(boolConst.getCoords());

            if (sym.getTag() == TokenTag.IS) {
                BoolRHSVar boolRHS = new BoolRHSVar();
                boolExprFactor.addSymbol(boolRHS);
                parseBoolRHS(boolRHS);
                boolExprFactor.setFollow(boolRHS.getFollow());
            }
        } else {
            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            boolExprFactor.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            boolExprFactor.setStart(arithmConstExpr.getStart());

            ArithmRHSVar arithmRHS = new ArithmRHSVar();
            boolExprFactor.addSymbol(arithmRHS);
            parseArithmRHS(arithmRHS);
            boolExprFactor.setFollow(arithmRHS.getFollow());
        }
    }

    //RHS                 ::= ArithmRHS | BoolRHS
    private void parseRHS(RHSVar rhs) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LESS
                || sym.getTag() == TokenTag.LESSEQ
                || sym.getTag() == TokenTag.GREATER
                || sym.getTag() == TokenTag.GREATEREQ
                || sym.getTag() == TokenTag.EQUAL
                || sym.getTag() == TokenTag.NOTEQUAL
                || sym.getTag() == TokenTag.BETWEEN) {
            ArithmRHSVar arithmRHS = new ArithmRHSVar();
            rhs.addSymbol(arithmRHS);
            parseArithmRHS(arithmRHS);
            rhs.setCoords(arithmRHS.getCoords());
        } else if (sym.getTag() == TokenTag.IS) {
            BoolRHSVar boolRHS = new BoolRHSVar();
            rhs.addSymbol(boolRHS);
            parseBoolRHS(boolRHS);
            boolRHS.setCoords(boolRHS.getCoords());
        } else {
            throw new RuntimeException("Boolean RHS expected, got " + sym);
        }
    }

    //ArithmRHS           ::= '<'  ArithmExpr
    //                    |   '<=' ArithmExpr
    //                    |   '>'  ArithmExpr
    //                    |   '>=' ArithmExpr
    //                    |   '='  ArithmExpr
    //                    |   '!=' ArithmExpr
    //                    |   BETWEEN ArithmExpr AND ArithmExpr     // ARITHMETIC ONLY TILL
    private void parseArithmRHS(ArithmRHSVar arithmRHS) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LESS) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.LESS);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            arithmRHS.setFollow(arithmExpr.getFollow());
        } else if (sym.getTag() == TokenTag.LESSEQ) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.LESSEQ);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            arithmRHS.setFollow(arithmExpr.getFollow());
        } else if (sym.getTag() == TokenTag.GREATER) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.GREATER);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            arithmRHS.setFollow(arithmExpr.getFollow());
        } else if (sym.getTag() == TokenTag.GREATEREQ) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.GREATEREQ);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            arithmRHS.setFollow(arithmExpr.getFollow());
        } else if (sym.getTag() == TokenTag.EQUAL) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.EQUAL);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            arithmRHS.setFollow(arithmExpr.getFollow());
        } else if (sym.getTag() == TokenTag.NOTEQUAL) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.NOTEQUAL);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            arithmRHS.setFollow(arithmExpr.getFollow());
        } else if (sym.getTag() == TokenTag.BETWEEN) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.BETWEEN);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);

            arithmRHS.addSymbol(sym);
            parse(TokenTag.AND);

            ArithmExprVar arithmExprVar = new ArithmExprVar();
            arithmRHS.addSymbol(arithmExprVar);
            parseArithmExpr(arithmExprVar);
            arithmRHS.setFollow(arithmExprVar.getFollow());
        } else {
            throw new RuntimeException("Compare operators or BETWEEN expected, got " + sym);
        }
    }

    //BoolConst            ::= TRUE | FALSE | NULL
    private void parseBoolConst(BoolConstVar boolConst) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.TRUE) {
            boolConst.addSymbol(sym);
            boolConst.setCoords(sym.getCoords());
            parse(TokenTag.TRUE);
        } else if (sym.getTag() == TokenTag.FALSE) {
            boolConst.addSymbol(sym);
            boolConst.setCoords(sym.getCoords());
            parse(TokenTag.FALSE);
        } else {
            boolConst.addSymbol(sym);
            boolConst.setCoords(sym.getCoords());
            parse(TokenTag.NULL);
        }
    }

    //BoolStmt             ::= ArithmExpr '<'  ArithmExpr
    //                     |   ArithmExpr '<=' ArithmExpr
    //                     |   ArithmExpr '>'  ArithmExpr
    //                     |   ArithmExpr '>=' ArithmExpr
    //                     |   ArithmExpr '='  ArithmExpr
    //                     |   ArithmExpr '!=' ArithmExpr
    //                     |   ArithmExpr IS NULL
    //                     |   ArithmExpr IS NOT NULL
    //                     |   ArithmExpr IS TRUE
    //                     |   ArithmExpr IS NOT TRUE
    //                     |   ArithmExpr IS FALSE
    //                     |   ArithmExpr IS NOT FALSE
    //                     |   ArithmExpr BETWEEN BoolExpr AND ArithmExpr       // ARITHMETIC OR DATE ONLY TILL
    //                     |   ArithmExpr NOT BETWEEN ArithmExpr AND ArithmExpr // ARITHMETIC OR DATE ONLY TILL
    //                  // |   ColId LIKE StringValue                           // ONLY FOR STRING TYPE OF ColId
    //                  // |   ColId NOT LIKE StringValue                       // ONLY FOR STRING TYPE OF ColId
    //                  // |   StringValue LIKE ColId                           // ONLY FOR STRING TYPE OF ColId
    //                  // |   StringValue NOT LIKE ColId                       // ONLY FOR STRING TYPE OF ColId
//    private void parseBoolStmt(BoolStmtVar boolStmt) throws CloneNotSupportedException {
//        //TODO UNSUPPORTED FIRST(ColId)
//        ArithmExprVar arithmExpr = new ArithmExprVar();
//        boolStmt.addSymbol(arithmExpr);
//        parseArithmExpr(arithmExpr);
//        boolStmt.setStart(arithmExpr.getStart());
//
//        if (sym.getTag() == TokenTag.LESS
//                || sym.getTag() == TokenTag.LESSEQ
//                || sym.getTag() == TokenTag.GREATER
//                || sym.getTag() == TokenTag.GREATEREQ
//                || sym.getTag() == TokenTag.EQUAL
//                || sym.getTag() == TokenTag.NOTEQUAL) {
//
//            boolStmt.addSymbol(sym);
//
//            if (sym.getTag() == TokenTag.LESS)
//                parse(TokenTag.LESS);
//            else if (sym.getTag() == TokenTag.LESSEQ)
//                parse(TokenTag.LESSEQ);
//            else if (sym.getTag() == TokenTag.GREATER)
//                parse(TokenTag.GREATER);
//            else if (sym.getTag() == TokenTag.GREATEREQ)
//                parse(TokenTag.GREATEREQ);
//            else if (sym.getTag() == TokenTag.EQUAL)
//                parse(TokenTag.EQUAL);
//            else
//                parse(TokenTag.NOTEQUAL);
//
//            ArithmExprVar arithmExprVar = new ArithmExprVar();
//            boolStmt.addSymbol(arithmExprVar);
//            parseArithmExpr(arithmExprVar);
//            boolStmt.setFollow(arithmExprVar.getFollow());
//        } else if (sym.getTag() == TokenTag.IS) {
//            boolStmt.addSymbol(sym);
//            parse(TokenTag.IS);
//
//            boolStmt.setFollow(sym.getFollow());
//            boolStmt.addSymbol(sym);
//            if (sym.getTag() == TokenTag.NULL) {
//                parse(TokenTag.NULL);
//            } else if (sym.getTag() == TokenTag.TRUE) {
//                parse(TokenTag.TRUE);
//            } else if (sym.getTag() == TokenTag.FALSE) {
//                parse(TokenTag.FALSE);
//            } else if (sym.getTag() == TokenTag.NOT) {
//                parse(TokenTag.NOT);
//
//                if (sym.getTag() == TokenTag.NULL) {
//                    boolStmt.setFollow(sym.getFollow());
//                    boolStmt.addSymbol(sym);
//                    parse(TokenTag.NULL);
//                } else if (sym.getTag() == TokenTag.TRUE) {
//                    boolStmt.setFollow(sym.getFollow());
//                    boolStmt.addSymbol(sym);
//                    parse(TokenTag.TRUE);
//                } else if (sym.getTag() == TokenTag.FALSE) {
//                    boolStmt.setFollow(sym.getFollow());
//                    boolStmt.addSymbol(sym);
//                    parse(TokenTag.FALSE);
//                } else {
//                    throw new RuntimeException("Not boolean: " + sym);
//                }
//            } else {
//                throw new RuntimeException("Not boolean: " + sym);
//            }
//        } else if (sym.getTag() == TokenTag.BETWEEN) {
//            boolStmt.addSymbol(sym);
//            parse(TokenTag.BETWEEN);
//
//            ArithmExprVar arithmExprLeft = new ArithmExprVar();
//            boolStmt.addSymbol(arithmExprLeft);
//            parseArithmExpr(arithmExprLeft);
//
//            parse(TokenTag.AND);
//
//            ArithmExprVar arithmExprRight = new ArithmExprVar();
//            boolStmt.addSymbol(arithmExprRight);
//            parseArithmExpr(arithmExprRight);
//
//            boolStmt.setFollow(arithmExprRight.getFollow());
//        } else if (sym.getTag() == TokenTag.NOT) {
//            boolStmt.addSymbol(sym);
//            parse(TokenTag.NOT);
//
//            boolStmt.addSymbol(sym);
//            parse(TokenTag.BETWEEN);
//
//            ArithmExprVar arithmExprLeft = new ArithmExprVar();
//            boolStmt.addSymbol(arithmExprLeft);
//            parseArithmExpr(arithmExprLeft);
//
//            parse(TokenTag.AND);
//
//            ArithmExprVar arithmExprRight = new ArithmExprVar();
//            boolStmt.addSymbol(arithmExprRight);
//            parseArithmExpr(arithmExprRight);
//
//            boolStmt.setFollow(arithmExprRight.getFollow());
//        } else {
//            throw new RuntimeException("Bool statement expected, got " + sym);
//        }
//    }

    //ArithmExpr           ::= ArithmExprTerm ( {'+' | '-'} ArithmExprTerm )*
    private void parseArithmExpr(ArithmExprVar arithmExpr) throws CloneNotSupportedException {
        ArithmExprTermVar arithmExprTerm = new ArithmExprTermVar();
        arithmExpr.addSymbol(arithmExprTerm);
        parseArithmExprTerm(arithmExprTerm);
        arithmExpr.setCoords(arithmExprTerm.getCoords());

        while (sym.getTag() == TokenTag.ADD || sym.getTag() == TokenTag.SUB) {
            arithmExpr.addSymbol(sym);

            if (sym.getTag() == TokenTag.ADD)
                parse(TokenTag.ADD);
            else
                parse(TokenTag.SUB);

            ArithmExprTermVar arithmExprTermVar = new ArithmExprTermVar();
            arithmExpr.addSymbol(arithmExprTermVar);
            parseArithmExprTerm(arithmExprTermVar);
            arithmExpr.setFollow(arithmExprTermVar.getFollow());
        }
    }

    //ArithmExprTerm       ::= ArithmExprFactor ( {'*' | '/'} ArithmExprFactor )*
    private void parseArithmExprTerm(ArithmExprTermVar arithmExprTerm) throws CloneNotSupportedException {
        ArithmExprFactorVar arithmExprFactor = new ArithmExprFactorVar();
        arithmExprTerm.addSymbol(arithmExprFactor);
        parseArithmExprFactor(arithmExprFactor);
        arithmExprTerm.setCoords(arithmExprFactor.getCoords());

        while (sym.getTag() == TokenTag.MUL || sym.getTag() == TokenTag.DIV) {
            arithmExprTerm.addSymbol(sym);

            if (sym.getTag() == TokenTag.MUL)
                parse(TokenTag.MUL);
            else
                parse(TokenTag.DIV);

            ArithmExprFactorVar arithmExprFactorVar = new ArithmExprFactorVar();
            arithmExprTerm.addSymbol(arithmExprFactorVar);
            parseArithmExprFactor(arithmExprFactorVar);
            arithmExprTerm.setFollow(arithmExprFactorVar.getFollow());
        }
    }

    //ArithmExprFactor     ::= ColId //MUST BE NUMERIC TYPE
    //                     |   NumericValue
    //                     |   '-' ArithmExprFactor
    //                     |   '(' ArithmExpr ')'
    private void parseArithmExprFactor(ArithmExprFactorVar arithmExprFactor) throws CloneNotSupportedException {
        //TODO UNSUPPORTED FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            arithmExprFactor.addSymbol(sym);
            arithmExprFactor.setCoords(sym.getCoords());
            parse(TokenTag.IDENTIFIER);
        } else if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST) {

            arithmExprFactor.addSymbol(sym);
            arithmExprFactor.setCoords(sym.getCoords());
            parseNumber();
        } else if (sym.getTag() == TokenTag.SUB) {
            arithmExprFactor.addSymbol(sym);
            arithmExprFactor.setStart(sym.getStart());
            parse(TokenTag.SUB);

            ArithmExprFactorVar arithmExprFactorVar = new ArithmExprFactorVar();
            arithmExprFactor.addSymbol(arithmExprFactorVar);
            parseArithmExprFactor(arithmExprFactorVar);
            arithmExprFactor.setFollow(arithmExprFactorVar.getFollow());
        } else if (sym.getTag() == TokenTag.LPAREN) {
            arithmExprFactor.addSymbol(sym);
            arithmExprFactor.setStart(sym.getStart());
            parse(TokenTag.LPAREN);

            ArithmExprVar arithmExpr = new ArithmExprVar();
            arithmExprFactor.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);

            arithmExprFactor.addSymbol(sym);
            arithmExprFactor.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else {
            throw new RuntimeException("Number or '(' arithmetic expression ')' expected, got " + sym);
        }
    }

    //CreateFunctionStmt          ::= (OR REPLACE)? FUNCTION QualifiedName '(' CreateFunctionRightPart
    private void parseCreateFunctionStmt(CreateFunctionStmtVar createFunctionStmt) throws CloneNotSupportedException {
        boolean wasOR = false;

        if (sym.getTag() == TokenTag.OR) {
            wasOR = true;
            createFunctionStmt.addSymbol(sym);
            createFunctionStmt.setStart(sym.getStart());
            parse(TokenTag.OR);

            createFunctionStmt.addSymbol(sym);
            parse(TokenTag.REPLACE);
        }

        createFunctionStmt.addSymbol(sym);
        if (!wasOR)
            createFunctionStmt.setStart(sym.getStart());
        parse(TokenTag.FUNCTION);

        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        createFunctionStmt.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);

        createFunctionStmt.addSymbol(sym);
        parse(TokenTag.LPAREN);

        CreateFunctionRightPartVar createFunctionRightPart = new CreateFunctionRightPartVar();
        createFunctionStmt.addSymbol(createFunctionRightPart);
        parseCreateFunctionRightPart(createFunctionRightPart);
        createFunctionStmt.setFollow(createFunctionRightPart.getFollow());
    }

    //CreateFunctionRightPart     ::= ')' RETURNS CreateFunctionAllReturnStmt
    //                            |    funcArgsWithDefaultsList ')' RETURNS CreateFunctionNoTrReturnStmt
    private void parseCreateFunctionRightPart(CreateFunctionRightPartVar createFunctionRightPart) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.RPAREN) {
            createFunctionRightPart.addSymbol(sym);
            createFunctionRightPart.setStart(sym.getStart());
            parse(TokenTag.RPAREN);

            createFunctionRightPart.addSymbol(sym);
            parse(TokenTag.RETURNS);

            CreateFunctionAllReturnStmtVar createFunctionAllReturnStmt = new CreateFunctionAllReturnStmtVar();
            createFunctionRightPart.addSymbol(createFunctionAllReturnStmt);
            parseCreateFunctionAllReturnStmt(createFunctionAllReturnStmt);
            createFunctionRightPart.setFollow(createFunctionAllReturnStmt.getFollow());
        } else {
            FuncArgsWithDefaultsListVar funcArgsWithDefaultsList = new FuncArgsWithDefaultsListVar();
            createFunctionRightPart.addSymbol(funcArgsWithDefaultsList);
            parseFuncArgsWithDefaultsList(funcArgsWithDefaultsList);
            createFunctionRightPart.setStart(funcArgsWithDefaultsList.getStart());

            createFunctionRightPart.addSymbol(sym);
            parse(TokenTag.RPAREN);

            createFunctionRightPart.addSymbol(sym);
            parse(TokenTag.RETURNS);

            CreateFunctionNoTrReturnStmtVar createFunctionNoTrReturnStmt = new CreateFunctionNoTrReturnStmtVar();
            createFunctionRightPart.addSymbol(createFunctionNoTrReturnStmt);
            parseCreateFunctionNoTrStmt(createFunctionNoTrReturnStmt);
            createFunctionRightPart.setFollow(createFunctionNoTrReturnStmt.getFollow());
        }
    }

    //CreateFunctionAllReturnStmt ::= Typename CreateFuncBody
    //                            |   TABLE '(' TableFuncColumnList ')' CreateFuncBody
    //                            |   TRIGGER CreateFuncBody //В семантическом анализе, если имя NEW.smth валидировать это
    private void parseCreateFunctionAllReturnStmt(CreateFunctionAllReturnStmtVar createFunctionAllReturnStmt) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CHARACTER
                || sym.getTag() == TokenTag.CHAR
                || sym.getTag() == TokenTag.VARCHAR
                || sym.getTag() == TokenTag.DATE
                || sym.getTag() == TokenTag.TIME
                || sym.getTag() == TokenTag.TIMESTAMP
                || sym.getTag() == TokenTag.RECORD
                || sym.getTag() == TokenTag.INT
                || sym.getTag() == TokenTag.INTEGER
                || sym.getTag() == TokenTag.SMALLINT
                || sym.getTag() == TokenTag.BIGINT
                || sym.getTag() == TokenTag.REAL
                || sym.getTag() == TokenTag.FLOAT
                || sym.getTag() == TokenTag.DOUBLE
                || sym.getTag() == TokenTag.DECIMAL
                || sym.getTag() == TokenTag.NUMERIC
                || sym.getTag() == TokenTag.BOOLEAN) {
            TypenameVar typename = new TypenameVar();
            createFunctionAllReturnStmt.addSymbol(typename);
            parseTypename(typename);
            createFunctionAllReturnStmt.setStart(typename.getStart());

            CreateFuncBodyVar createFuncBody = new CreateFuncBodyVar();
            createFunctionAllReturnStmt.addSymbol(createFuncBody);
            parseCreateFuncBody(createFuncBody);
            createFunctionAllReturnStmt.setFollow(createFuncBody.getFollow());
        } else if (sym.getTag() == TokenTag.TABLE) {
            createFunctionAllReturnStmt.addSymbol(sym);
            createFunctionAllReturnStmt.setStart(sym.getStart());
            parse(TokenTag.TABLE);

            createFunctionAllReturnStmt.addSymbol(sym);
            parse(TokenTag.LPAREN);

            TableFuncColumnListVar tableFuncColumnList = new TableFuncColumnListVar();
            createFunctionAllReturnStmt.addSymbol(tableFuncColumnList);
            parseTableFuncColumnList(tableFuncColumnList);

            createFunctionAllReturnStmt.addSymbol(sym);
            parse(TokenTag.RPAREN);

            CreateFuncBodyVar createFuncBody = new CreateFuncBodyVar();
            createFunctionAllReturnStmt.addSymbol(createFuncBody);
            parseCreateFuncBody(createFuncBody);
            createFunctionAllReturnStmt.setFollow(createFuncBody.getFollow());
        } else {
            throw new RuntimeException("Typename or TABLE expected, got " + sym);
        }
    }

    //CreateFunctionNoTrReturnStmt::= Typename CreateFuncBody
    //                            |   TABLE '(' TableFuncColumnList ')' CreateFuncBody
    private void parseCreateFunctionNoTrStmt(CreateFunctionNoTrReturnStmtVar createFunctionNoTrReturnStmt) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CHARACTER
                || sym.getTag() == TokenTag.CHAR
                || sym.getTag() == TokenTag.VARCHAR
                || sym.getTag() == TokenTag.DATE
                || sym.getTag() == TokenTag.TIME
                || sym.getTag() == TokenTag.TIMESTAMP
                || sym.getTag() == TokenTag.RECORD
                || sym.getTag() == TokenTag.INT
                || sym.getTag() == TokenTag.INTEGER
                || sym.getTag() == TokenTag.SMALLINT
                || sym.getTag() == TokenTag.BIGINT
                || sym.getTag() == TokenTag.REAL
                || sym.getTag() == TokenTag.FLOAT
                || sym.getTag() == TokenTag.DOUBLE
                || sym.getTag() == TokenTag.DECIMAL
                || sym.getTag() == TokenTag.NUMERIC
                || sym.getTag() == TokenTag.BOOLEAN) {
            TypenameVar typename = new TypenameVar();
            createFunctionNoTrReturnStmt.addSymbol(typename);
            parseTypename(typename);
            createFunctionNoTrReturnStmt.setStart(typename.getStart());

            CreateFuncBodyVar createFuncBody = new CreateFuncBodyVar();
            createFunctionNoTrReturnStmt.addSymbol(createFuncBody);
            parseCreateFuncBody(createFuncBody);
            createFunctionNoTrReturnStmt.setFollow(createFuncBody.getFollow());
        } else if (sym.getTag() == TokenTag.TABLE) {
            createFunctionNoTrReturnStmt.addSymbol(sym);
            createFunctionNoTrReturnStmt.setStart(sym.getStart());
            parse(TokenTag.TABLE);

            createFunctionNoTrReturnStmt.addSymbol(sym);
            parse(TokenTag.LPAREN);

            TableFuncColumnListVar tableFuncColumnList = new TableFuncColumnListVar();
            createFunctionNoTrReturnStmt.addSymbol(tableFuncColumnList);
            parseTableFuncColumnList(tableFuncColumnList);

            createFunctionNoTrReturnStmt.addSymbol(sym);
            parse(TokenTag.RPAREN);

            CreateFuncBodyVar createFuncBody = new CreateFuncBodyVar();
            createFunctionNoTrReturnStmt.addSymbol(createFuncBody);
            parseCreateFuncBody(createFuncBody);
            createFunctionNoTrReturnStmt.setFollow(createFuncBody.getFollow());
        } else {
            throw new RuntimeException("Typename or TABLE expected, got " + sym);
        }
    }

    //funcArgsWithDefaultsList    ::= funcArgWithDefault (',' funcArgWithDefault)*
    private void parseFuncArgsWithDefaultsList(FuncArgsWithDefaultsListVar funcArgsWithDefaultsList) throws CloneNotSupportedException {
        FuncArgWithDefaultVar funcArgWithDefault = new FuncArgWithDefaultVar();
        funcArgsWithDefaultsList.addSymbol(funcArgWithDefault);
        parseFuncArgWithDefault(funcArgWithDefault);
        funcArgsWithDefaultsList.setCoords(funcArgWithDefault.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            funcArgsWithDefaultsList.addSymbol(sym);
            parse(TokenTag.COMMA);

            FuncArgWithDefaultVar funcArgWithDefaultVar = new FuncArgWithDefaultVar();
            funcArgsWithDefaultsList.addSymbol(funcArgWithDefaultVar);
            parseFuncArgWithDefault(funcArgWithDefaultVar);
            funcArgsWithDefaultsList.setFollow(funcArgWithDefaultVar.getFollow());
        }
    }

    //funcArgWithDefault          ::= funcArg funcArgDefault?
    private void parseFuncArgWithDefault(FuncArgWithDefaultVar funcArgWithDefault) throws CloneNotSupportedException {
        FuncArgVar funcArg = new FuncArgVar();
        funcArgWithDefault.addSymbol(funcArg);
        parseFuncArg(funcArg);
        funcArgWithDefault.setCoords(funcArg.getCoords());

        if (sym.getTag() == TokenTag.DEFAULT
                || sym.getTag() == TokenTag.EQUAL) {
            FuncArgDefaultVar funcArgDefault = new FuncArgDefaultVar();
            funcArgWithDefault.addSymbol(funcArgDefault);
            parseFuncArgDefault(funcArgDefault);
            funcArgWithDefault.setFollow(funcArgDefault.getFollow());
        }
    }

    //funcArgDefault              ::= DEFAULT constExpr
    //                            |   '=' constExpr
    private void parseFuncArgDefault(FuncArgDefaultVar funcArgDefault) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DEFAULT) {
            funcArgDefault.addSymbol(sym);
            funcArgDefault.setStart(sym.getStart());
            parse(TokenTag.DEFAULT);

            ConstExprVar constExpr = new ConstExprVar();
            funcArgDefault.addSymbol(constExpr);
            parseConstExpr(constExpr);
            funcArgDefault.setFollow(constExpr.getFollow());
        } else if (sym.getTag() == TokenTag.EQUAL) {
            funcArgDefault.addSymbol(sym);
            funcArgDefault.setStart(sym.getStart());
            parse(TokenTag.EQUAL);

            ConstExprVar constExpr = new ConstExprVar();
            funcArgDefault.addSymbol(constExpr);
            parseConstExpr(constExpr);
            funcArgDefault.setFollow(constExpr.getFollow());
        } else {
            throw new RuntimeException("DEFAULT or '=' expected, got " + sym);
        }
    }

    //funcArg                     ::= argClass IDENT? Typename
    //                            |   IDENT argClass? Typename
    //                            |   Typename
    private void parseFuncArg(FuncArgVar funcArg) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.IN
                || sym.getTag() == TokenTag.OUT
                || sym.getTag() == TokenTag.INOUT) {
            ArgClassVar argClass = new ArgClassVar();
            funcArg.addSymbol(argClass);
            parseArgClass(argClass);
            funcArg.setStart(argClass.getStart());

            if (sym.getTag() == TokenTag.IDENTIFIER) {
                funcArg.addSymbol(sym);
                parse(TokenTag.IDENTIFIER);
            }

            TypenameVar typename = new TypenameVar();
            funcArg.addSymbol(typename);
            parseTypename(typename);
            funcArg.setFollow(typename.getFollow());
        } else if (sym.getTag() == TokenTag.IDENTIFIER) {
            funcArg.addSymbol(sym);
            funcArg.setStart(sym.getStart());
            parse(TokenTag.IDENTIFIER);

            if (sym.getTag() == TokenTag.IN
                    || sym.getTag() == TokenTag.OUT
                    || sym.getTag() == TokenTag.INOUT) {
                ArgClassVar argClass = new ArgClassVar();
                funcArg.addSymbol(argClass);
                parseArgClass(argClass);
            }

            TypenameVar typename = new TypenameVar();
            funcArg.addSymbol(typename);
            parseTypename(typename);
            funcArg.setFollow(typename.getFollow());
        } else if (sym.getTag() == TokenTag.CHARACTER
                || sym.getTag() == TokenTag.CHAR
                || sym.getTag() == TokenTag.VARCHAR
                || sym.getTag() == TokenTag.DATE
                || sym.getTag() == TokenTag.TIME
                || sym.getTag() == TokenTag.TIMESTAMP
                || sym.getTag() == TokenTag.RECORD
                || sym.getTag() == TokenTag.INT
                || sym.getTag() == TokenTag.INTEGER
                || sym.getTag() == TokenTag.SMALLINT
                || sym.getTag() == TokenTag.BIGINT
                || sym.getTag() == TokenTag.REAL
                || sym.getTag() == TokenTag.FLOAT
                || sym.getTag() == TokenTag.DOUBLE
                || sym.getTag() == TokenTag.DECIMAL
                || sym.getTag() == TokenTag.NUMERIC
                || sym.getTag() == TokenTag.BOOLEAN) {
            TypenameVar typename = new TypenameVar();
            funcArg.addSymbol(typename);
            parseTypename(typename);
            funcArg.setCoords(typename.getCoords());
        } else {
            throw new RuntimeException("IN, OUT, identifier or typename expected, got " + sym);
        }
    }

    //argClass                    ::= IN OUT?
    //                            |   OUT
    //                            |   INOUT
    private void parseArgClass(ArgClassVar argClass) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.IN) {
            argClass.addSymbol(sym);
            argClass.setCoords(sym.getCoords());
            parse(TokenTag.IN);

            if (sym.getTag() == TokenTag.OUT) {
                argClass.addSymbol(sym);
                argClass.setFollow(sym.getFollow());
                parse(TokenTag.OUT);
            }
        } else if (sym.getTag() == TokenTag.OUT) {
            argClass.addSymbol(sym);
            argClass.setCoords(sym.getCoords());
            parse(TokenTag.OUT);
        } else if (sym.getTag() == TokenTag.INOUT) {
            argClass.addSymbol(sym);
            argClass.setCoords(sym.getCoords());
            parse(TokenTag.INOUT);
        } else {
            throw new RuntimeException("IN, OUT, or INOUT expected, got " + sym);
        }
    }

    //TableFuncColumnList         ::= IDENT Typename (',' IDENT Typename)*
    private void parseTableFuncColumnList(TableFuncColumnListVar tableFuncColumnList) throws CloneNotSupportedException {
        tableFuncColumnList.addSymbol(sym);
        tableFuncColumnList.setStart(sym.getStart());
        parse(TokenTag.IDENTIFIER);

        TypenameVar typename = new TypenameVar();
        tableFuncColumnList.addSymbol(typename);
        parseTypename(typename);
        tableFuncColumnList.setFollow(typename.getFollow());

        while (sym.getTag() == TokenTag.COMMA) {
            tableFuncColumnList.addSymbol(sym);
            parse(TokenTag.COMMA);

            tableFuncColumnList.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

            TypenameVar typenameVar = new TypenameVar();
            tableFuncColumnList.addSymbol(typenameVar);
            parseTypename(typenameVar);
            tableFuncColumnList.setFollow(typenameVar.getFollow());
        }
    }

    //CreateFuncBody              ::= AS '$$' funcAs '$$' LANGUAGE plpgsql
    //                            |   LANGUAGE plpgsql AS '$$' funcAs '$$'
    private void parseCreateFuncBody(CreateFuncBodyVar createFuncBody) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.AS) {
            createFuncBody.addSymbol(sym);
            createFuncBody.setStart(sym.getStart());
            parse(TokenTag.AS);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.DOUBLE_DOLLAR);

            FuncAsVar funcAs = new FuncAsVar();
            createFuncBody.addSymbol(funcAs);
            parseFuncAs(funcAs);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.DOUBLE_DOLLAR);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.LANGUAGE);

            createFuncBody.addSymbol(sym);
            createFuncBody.setFollow(sym.getFollow());
            parse(TokenTag.PLPGSQL);
        } else if (sym.getTag() == TokenTag.LANGUAGE) {
            createFuncBody.addSymbol(sym);
            createFuncBody.setStart(sym.getStart());
            parse(TokenTag.LANGUAGE);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.PLPGSQL);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.AS);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.DOUBLE_DOLLAR);

            FuncAsVar funcAs = new FuncAsVar();
            createFuncBody.addSymbol(funcAs);
            parseFuncAs(funcAs);

            createFuncBody.addSymbol(sym);
            createFuncBody.setFollow(sym.getFollow());
            parse(TokenTag.DOUBLE_DOLLAR);
        } else {
            throw new RuntimeException("AS or LANGUAGE expected, got " + sym);
        }
    }

    //funcAs                      ::= declareBlock? BEGIN funcBody* END ';'
    private void parseFuncAs(FuncAsVar funcAsVar) throws CloneNotSupportedException {
        boolean wasDeclareBlock = false;

        if (sym.getTag() == TokenTag.DECLARE) {
            DeclareBlockVar declareBlock = new DeclareBlockVar();
            funcAsVar.addSymbol(declareBlock);
            parseDeclareBlock(declareBlock);
            funcAsVar.setStart(declareBlock.getStart());

            wasDeclareBlock = true;
        }

        if (!wasDeclareBlock)
            funcAsVar.setStart(sym.getStart());
        funcAsVar.addSymbol(sym);
        parse(TokenTag.BEGIN);

        //TODO FIRST(QUAILIFIED_NAME, COlId)
        while (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN
                || sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.RETURN
                || sym.getTag() == TokenTag.IF
                || sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.LOOP
                || sym.getTag() == TokenTag.FOR
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.RAISE
                || sym.getTag() == TokenTag.INSERT
                || sym.getTag() == TokenTag.UPDATE
                || sym.getTag() == TokenTag.DELETE
                || sym.getTag() == TokenTag.SELECT
                || sym.getTag() == TokenTag.LPAREN) {
            FuncBodyVar funcBody = new FuncBodyVar();
            funcAsVar.addSymbol(funcBody);
            parseFuncBody(funcBody);
        }

        funcAsVar.addSymbol(sym);
        parse(TokenTag.END);

        funcAsVar.addSymbol(sym);
        funcAsVar.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //declareBlock                ::= DECLARE variableDecl+
    private void parseDeclareBlock(DeclareBlockVar declareBlock) throws CloneNotSupportedException {
        declareBlock.addSymbol(sym);
        declareBlock.setStart(sym.getStart());
        parse(TokenTag.DECLARE);

        do {
            VariableDeclVar variableDecl = new VariableDeclVar();
            declareBlock.addSymbol(variableDecl);
            parseVariableDecl(variableDecl);
            declareBlock.setFollow(variableDecl.getFollow());
        //TODO FIRST(ColId)
        } while (sym.getTag() == TokenTag.IDENTIFIER);
    }

    //variableDecl                ::= QualifiedName Typename (':=' ConstExpr)? ';'
    private void parseVariableDecl(VariableDeclVar variableDecl) throws CloneNotSupportedException {
        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        variableDecl.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);
        variableDecl.setStart(qualifiedName.getStart());

        TypenameVar typename = new TypenameVar();
        variableDecl.addSymbol(typename);
        parseTypename(typename);

        if (sym.getTag() == TokenTag.ASSIGN) {
            variableDecl.addSymbol(sym);
            parse(TokenTag.ASSIGN);

            ConstExprVar constExpr = new ConstExprVar();
            variableDecl.addSymbol(constExpr);
            parseConstExpr(constExpr);
        }

        variableDecl.addSymbol(sym);
        variableDecl.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //funcBody                    ::= funcAs
    //                            |   variableAssign
    //                            |   returnStmt
    //                            |   ifStmt
    //                            |   loopStmt
    //                            |   NULL ';'
    //                            |   raiseStmt
    //                            |   InsertStmt ';'
    //                            |   UpdateStmt ';'
    //                            |   DeleteStmt ';'
    //                            |   SelectStmt ';'
    private void parseFuncBody(FuncBodyVar funcBody) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN) {
            FuncAsVar funcAs = new FuncAsVar();
            funcBody.addSymbol(funcAs);
            parseFuncAs(funcAs);
            funcBody.setCoords(funcAs.getCoords());
        //TODO FIRST(ColId)
        } else if (sym.getTag() == TokenTag.IDENTIFIER) {
            VariableAssignVar variableAssign = new VariableAssignVar();
            funcBody.addSymbol(variableAssign);
            parseVariableAssign(variableAssign);
            funcBody.setCoords(variableAssign.getCoords());
        } else if (sym.getTag() == TokenTag.RETURN) {
            ReturnStmtVar returnStmt = new ReturnStmtVar();
            funcBody.addSymbol(returnStmt);
            parseReturnStmt(returnStmt);
            funcBody.setCoords(returnStmt.getCoords());
        } else if (sym.getTag() == TokenTag.IF) {
            IfStmtVar ifStmt = new IfStmtVar();
            funcBody.addSymbol(ifStmt);
            parseIfStmt(ifStmt);
            funcBody.setCoords(ifStmt.getCoords());
        } else if (sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.LOOP
                || sym.getTag() == TokenTag.FOR) {
            LoopStmtVar loopStmt = new LoopStmtVar();
            funcBody.addSymbol(loopStmt);
            parseLoopStmt(loopStmt);
            funcBody.setCoords(loopStmt.getCoords());
        } else if (sym.getTag() == TokenTag.NULL) {
            funcBody.addSymbol(sym);
            funcBody.setStart(sym.getStart());
            parse(TokenTag.NULL);

            funcBody.addSymbol(sym);
            funcBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.RAISE) {
            RaiseStmtVar raiseStmt = new RaiseStmtVar();
            funcBody.addSymbol(raiseStmt);
            parseRaiseStmt(raiseStmt);
            funcBody.setCoords(raiseStmt.getCoords());
        } else if (sym.getTag() == TokenTag.INSERT) {
            InsertStmtVar insertStmt = new InsertStmtVar();
            funcBody.addSymbol(insertStmt);
            parseInsertStmt(insertStmt);
            funcBody.setStart(insertStmt.getStart());

            funcBody.addSymbol(sym);
            funcBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.UPDATE) {
            UpdateStmtVar updateStmt = new UpdateStmtVar();
            funcBody.addSymbol(updateStmt);
            parseUpdateStmt(updateStmt);
            funcBody.setStart(updateStmt.getStart());

            funcBody.addSymbol(sym);
            funcBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.DELETE) {
            DeleteStmtVar deleteStmt = new DeleteStmtVar();
            funcBody.addSymbol(deleteStmt);
            parseDeleteStmt(deleteStmt);
            funcBody.setStart(deleteStmt.getStart());

            funcBody.addSymbol(sym);
            funcBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.SELECT) {
            SelectStmtVar selectStmt = new SelectStmtVar();
            funcBody.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            funcBody.setStart(selectStmt.getStart());

            funcBody.addSymbol(sym);
            funcBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else {
            throw new RuntimeException("Function body expression expected, got " + sym);
        }
    }

    //variableAssign              ::= QualifiedName ':=' Expr ';'
    private void parseVariableAssign(VariableAssignVar variableAssign) throws CloneNotSupportedException {
        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        variableAssign.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);
        variableAssign.setStart(qualifiedName.getStart());

        variableAssign.addSymbol(sym);
        parse(TokenTag.ASSIGN);

        ExprVar expr = new ExprVar();
        variableAssign.addSymbol(expr);
        parseExpr(expr);

        variableAssign.addSymbol(sym);
        variableAssign.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //raiseStmt                   ::= RAISE raiseLevel CharacterValue ';'
    private void parseRaiseStmt(RaiseStmtVar raiseStmt) throws CloneNotSupportedException {
        raiseStmt.addSymbol(sym);
        raiseStmt.setStart(sym.getStart());
        parse(TokenTag.RAISE);

        RaiseLevelVar raiseLevel = new RaiseLevelVar();
        raiseStmt.addSymbol(raiseLevel);
        parseRaiseLevel(raiseLevel);

        raiseStmt.addSymbol(sym);
        parse(TokenTag.STRING_CONST);

        raiseStmt.addSymbol(sym);
        raiseStmt.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //raiseLevel                  ::= NOTICE
    //                            |   EXCEPTION?
    private void parseRaiseLevel(RaiseLevelVar raiseLevel) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.NOTICE) {
            raiseLevel.addSymbol(sym);
            raiseLevel.setCoords(sym.getCoords());
            parse(TokenTag.NOTICE);
        } else if (sym.getTag() == TokenTag.EXCEPTION) {
            raiseLevel.addSymbol(sym);
            raiseLevel.setCoords(sym.getCoords());
            parse(TokenTag.EXCEPTION);
        }
    }

    //returnStmt                  ::= RETURN returnedValue ';'
    private void parseReturnStmt(ReturnStmtVar returnStmt) throws CloneNotSupportedException {
        returnStmt.addSymbol(sym);
        returnStmt.setStart(sym.getStart());
        parse(TokenTag.RETURN);

        ReturnedValueVar returnedValue = new ReturnedValueVar();
        returnStmt.addSymbol(returnedValue);
        parseReturnedValue(returnedValue);

        returnStmt.addSymbol(sym);
        returnedValue.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //returnedValue               ::= Expr
    //                            |   QUERY SelectStmt
    private void parseReturnedValue(ReturnedValueVar returnedValue) throws CloneNotSupportedException {
        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN
                || sym.getTag() == TokenTag.NOT
                || sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.STRING_CONST
                || sym.getTag() == TokenTag.DATE_CONST) {
            ExprVar expr = new ExprVar();
            returnedValue.addSymbol(expr);
            parseExpr(expr);
            returnedValue.setCoords(expr.getCoords());
        } else if (sym.getTag() == TokenTag.QUERY) {
            returnedValue.addSymbol(sym);
            returnedValue.setStart(sym.getStart());
            parse(TokenTag.QUERY);

            SelectStmtVar selectStmt = new SelectStmtVar();
            returnedValue.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            returnedValue.setFollow(selectStmt.getFollow());
        } else {
            throw new RuntimeException("Expression or QUERY expected, got " + sym);
        }
    }

    //ifStmt                      ::= IF boolExpr THEN ifBody (ELSIF boolExpr THEN ifBody)? (ELSE ifBody)? END IF ';'
    private void parseIfStmt(IfStmtVar ifStmt) throws CloneNotSupportedException {
        ifStmt.addSymbol(sym);
        ifStmt.setStart(sym.getStart());
        parse(TokenTag.IF);

        BoolExprVar boolExpr = new BoolExprVar();
        ifStmt.addSymbol(boolExpr);
        parseBoolExpr(boolExpr);

        ifStmt.addSymbol(sym);
        parse(TokenTag.THEN);

        IfBodyVar ifBody = new IfBodyVar();
        ifStmt.addSymbol(ifBody);
        parseIfBody(ifBody);

        if (sym.getTag() == TokenTag.ELSIF) {
            ifStmt.addSymbol(sym);
            parse(TokenTag.ELSIF);

            BoolExprVar boolExprVar = new BoolExprVar();
            ifStmt.addSymbol(boolExprVar);
            parseBoolExpr(boolExprVar);

            ifStmt.addSymbol(sym);
            parse(TokenTag.THEN);

            IfBodyVar ifBodyVar = new IfBodyVar();
            ifStmt.addSymbol(ifBodyVar);
            parseIfBody(ifBodyVar);
        }

        if (sym.getTag() == TokenTag.ELSE) {
            ifStmt.addSymbol(sym);
            parse(TokenTag.ELSE);

            IfBodyVar ifBodyVar = new IfBodyVar();
            ifStmt.addSymbol(ifBodyVar);
            parseIfBody(ifBodyVar);
        }

        ifStmt.addSymbol(sym);
        parse(TokenTag.END);

        ifStmt.addSymbol(sym);
        parse(TokenTag.IF);

        ifStmt.addSymbol(sym);
        parse(TokenTag.SEMICOLON);
    }

    //ifBody                      ::= funcAs
    //                            |   (funcBody)?
    private void parseIfBody(IfBodyVar ifBody) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN) {
            FuncAsVar funcAs = new FuncAsVar();
            ifBody.addSymbol(funcAs);
            parseFuncAs(funcAs);
            ifBody.setCoords(funcAs.getCoords());
        } else if (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN
                || sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.RETURN
                || sym.getTag() == TokenTag.IF
                || sym.getTag() == TokenTag.LOOP
                || sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.FOR
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.RAISE
                || sym.getTag() == TokenTag.INSERT
                || sym.getTag() == TokenTag.UPDATE
                || sym.getTag() == TokenTag.DELETE
                || sym.getTag() == TokenTag.SELECT) {
            FuncBodyVar funcBody = new FuncBodyVar();
            ifBody.addSymbol(funcBody);
            parseFuncBody(funcBody);
            ifBody.setCoords(funcBody.getCoords());
        }
    }

    //loopStmt                    ::= (WHILE boolExpr)? LOOP cycleDecl END LOOP ';'
    //                            |   FOR IDENT IN forClause
    private void parseLoopStmt(LoopStmtVar loopStmt) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.LOOP) {
            boolean wasWhile = false;
            if (sym.getTag() == TokenTag.WHILE) {
                loopStmt.addSymbol(sym);
                loopStmt.setStart(sym.getStart());
                parse(TokenTag.WHILE);

                BoolExprVar boolExpr = new BoolExprVar();
                loopStmt.addSymbol(boolExpr);
                parseBoolExpr(boolExpr);

                wasWhile = true;
            }

            loopStmt.addSymbol(sym);
            if (!wasWhile)
                loopStmt.setStart(sym.getStart());
            parse(TokenTag.LOOP);

            CycleDeclVar cycleDecl = new CycleDeclVar();
            loopStmt.addSymbol(cycleDecl);
            parseCycleDecl(cycleDecl);

            loopStmt.addSymbol(sym);
            parse(TokenTag.END);

            loopStmt.addSymbol(sym);
            parse(TokenTag.LOOP);

            loopStmt.addSymbol(sym);
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.FOR) {
            loopStmt.addSymbol(sym);
            loopStmt.setStart(sym.getStart());
            parse(TokenTag.FOR);

            loopStmt.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

            loopStmt.addSymbol(sym);
            parse(TokenTag.IN);

            ForClauseVar forClause = new ForClauseVar();
            loopStmt.addSymbol(forClause);
            parseForClause(forClause);
            loopStmt.setFollow(forClause.getFollow());
        } else {
            throw new RuntimeException("WHILE, LOOP or FOR expected, got " + sym);
        }
    }

    //forClause                   ::= REVERSE? arithmExpr '..' arithmExpr (BY arithmExpr)? LOOP cycleDecl END LOOP ';'
    //                            |   SelectStmt cycleDecl END LOOP ';'  //ident should be of type RECORD
    private void parseForClause(ForClauseVar forClause) throws CloneNotSupportedException {
        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.REVERSE
                || sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN
                || sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST) {
            boolean wasReverse = false;
            if (sym.getTag() == TokenTag.REVERSE) {
                forClause.addSymbol(sym);
                forClause.setStart(sym.getStart());
                parse(TokenTag.REVERSE);
                wasReverse = true;
            }

            ArithmExprVar arithmExpr = new ArithmExprVar();
            forClause.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            if (!wasReverse)
                forClause.setStart(arithmExpr.getStart());

            forClause.addSymbol(sym);
            parse(TokenTag.DOUBLE_DOT);

            ArithmExprVar arithmExprVar = new ArithmExprVar();
            forClause.addSymbol(arithmExprVar);
            parseArithmExpr(arithmExprVar);

            if (sym.getTag() == TokenTag.BY) {
                forClause.addSymbol(sym);
                parse(TokenTag.BY);

                ArithmExprVar arithmExprVarBy = new ArithmExprVar();
                forClause.addSymbol(arithmExprVarBy);
                parseArithmExpr(arithmExprVarBy);
            }

            forClause.addSymbol(sym);
            parse(TokenTag.LOOP);

            CycleDeclVar cycleDecl = new CycleDeclVar();
            forClause.addSymbol(cycleDecl);
            parseCycleDecl(cycleDecl);

            forClause.addSymbol(sym);
            parse(TokenTag.END);

            forClause.addSymbol(sym);
            parse(TokenTag.LOOP);

            forClause.addSymbol(sym);
            forClause.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        //TODO FIRST(ColId, QualifiedName)
        } else if (sym.getTag() == TokenTag.SELECT) {
            SelectStmtVar selectStmt = new SelectStmtVar();
            forClause.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            forClause.setStart(selectStmt.getStart());

            CycleDeclVar cycleDecl = new CycleDeclVar();
            forClause.addSymbol(cycleDecl);
            parseCycleDecl(cycleDecl);

            forClause.addSymbol(sym);
            parse(TokenTag.END);

            forClause.addSymbol(sym);
            parse(TokenTag.LOOP);

            forClause.addSymbol(sym);
            forClause.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else {
            throw new RuntimeException("REVERSE, arithmetic expression or SELECT expected, got " + sym);
        }
    }

    //cycleDecl                   ::= declareBlock? BEGIN cycleBody* END ';'
    //                            |   cycleBody*
    private void parseCycleDecl(CycleDeclVar cycleDecl) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN) {
            boolean wasDeclare = false;
            if (sym.getTag() == TokenTag.DECLARE) {
                DeclareBlockVar declareBlock = new DeclareBlockVar();
                cycleDecl.addSymbol(declareBlock);
                parseDeclareBlock(declareBlock);
                cycleDecl.setStart(declareBlock.getStart());
                wasDeclare = true;
            }

            cycleDecl.addSymbol(sym);
            if (!wasDeclare)
                cycleDecl.setStart(sym.getStart());
            parse(TokenTag.BEGIN);

            CycleBodyVar cycleBody = new CycleBodyVar();
            cycleDecl.addSymbol(cycleBody);
            parseCycleBody(cycleBody);

            cycleDecl.addSymbol(sym);
            parse(TokenTag.END);

            cycleDecl.addSymbol(sym);
            parse(TokenTag.SEMICOLON);
        } else while (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN
                || sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.RETURN
                || sym.getTag() == TokenTag.IF
                || sym.getTag() == TokenTag.LOOP
                || sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.FOR
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.RAISE
                || sym.getTag() == TokenTag.INSERT
                || sym.getTag() == TokenTag.UPDATE
                || sym.getTag() == TokenTag.DELETE
                || sym.getTag() == TokenTag.SELECT
                || sym.getTag() == TokenTag.EXIT
                || sym.getTag() == TokenTag.CONTINUE) {
            CycleBodyVar cycleBody = new CycleBodyVar();
            cycleDecl.addSymbol(cycleBody);
            parseCycleBody(cycleBody);
            cycleDecl.setCoords(cycleBody.getCoords());
        }
    }

    //cycleBody                   ::= variableAssign
    //                            |   returnStmt
    //                            |   ifCycleStmt
    //                            |   loopStmt
    //                            |   NULL ';'
    //                            |   raiseStmt
    //                            |   InsertStmt ';'
    //                            |   UpdateStmt ';'
    //                            |   DeleteStmt ';'
    //                            |   SelectStmt ';'
    //                            |   EXIT (WHEN boolExpr)? ';'
    //                            |   CONTINUE (WHEN boolExpr)? ';'
    private void parseCycleBody(CycleBodyVar cycleBody) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN) {
            FuncAsVar funcAs = new FuncAsVar();
            cycleBody.addSymbol(funcAs);
            parseFuncAs(funcAs);
            cycleBody.setCoords(funcAs.getCoords());
            //TODO FIRST(ColId)
        } else if (sym.getTag() == TokenTag.IDENTIFIER) {
            VariableAssignVar variableAssign = new VariableAssignVar();
            cycleBody.addSymbol(variableAssign);
            parseVariableAssign(variableAssign);
            cycleBody.setCoords(variableAssign.getCoords());
        } else if (sym.getTag() == TokenTag.RETURN) {
            ReturnStmtVar returnStmt = new ReturnStmtVar();
            cycleBody.addSymbol(returnStmt);
            parseReturnStmt(returnStmt);
            cycleBody.setCoords(returnStmt.getCoords());
        } else if (sym.getTag() == TokenTag.IF) {
            IfCycleStmtVar ifCycleStmt = new IfCycleStmtVar();
            cycleBody.addSymbol(ifCycleStmt);
            parseIfCycleStmt(ifCycleStmt);
            cycleBody.setCoords(ifCycleStmt.getCoords());
        } else if (sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.LOOP
                || sym.getTag() == TokenTag.FOR) {
            LoopStmtVar loopStmt = new LoopStmtVar();
            cycleBody.addSymbol(loopStmt);
            parseLoopStmt(loopStmt);
            cycleBody.setCoords(loopStmt.getCoords());
        } else if (sym.getTag() == TokenTag.NULL) {
            cycleBody.addSymbol(sym);
            cycleBody.setStart(sym.getStart());
            parse(TokenTag.NULL);

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.RAISE) {
            RaiseStmtVar raiseStmt = new RaiseStmtVar();
            cycleBody.addSymbol(raiseStmt);
            parseRaiseStmt(raiseStmt);
            cycleBody.setCoords(raiseStmt.getCoords());
        } else if (sym.getTag() == TokenTag.INSERT) {
            InsertStmtVar insertStmt = new InsertStmtVar();
            cycleBody.addSymbol(insertStmt);
            parseInsertStmt(insertStmt);
            cycleBody.setStart(insertStmt.getStart());

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.UPDATE) {
            UpdateStmtVar updateStmt = new UpdateStmtVar();
            cycleBody.addSymbol(updateStmt);
            parseUpdateStmt(updateStmt);
            cycleBody.setStart(updateStmt.getStart());

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.DELETE) {
            DeleteStmtVar deleteStmt = new DeleteStmtVar();
            cycleBody.addSymbol(deleteStmt);
            parseDeleteStmt(deleteStmt);
            cycleBody.setStart(deleteStmt.getStart());

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.SELECT) {
            SelectStmtVar selectStmt = new SelectStmtVar();
            cycleBody.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            cycleBody.setStart(selectStmt.getStart());

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.EXIT) {
            cycleBody.addSymbol(sym);
            cycleBody.setStart(sym.getStart());
            parse(TokenTag.EXIT);

            if (sym.getTag() == TokenTag.WHEN) {
                cycleBody.addSymbol(sym);
                parse(TokenTag.WHEN);

                BoolExprVar boolExpr = new BoolExprVar();
                cycleBody.addSymbol(boolExpr);
                parseBoolExpr(boolExpr);
            }

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.CONTINUE) {
            cycleBody.addSymbol(sym);
            cycleBody.setStart(sym.getStart());
            parse(TokenTag.CONTINUE);

            if (sym.getTag() == TokenTag.WHEN) {
                cycleBody.addSymbol(sym);
                parse(TokenTag.WHEN);

                BoolExprVar boolExpr = new BoolExprVar();
                cycleBody.addSymbol(boolExpr);
                parseBoolExpr(boolExpr);
            }

            cycleBody.addSymbol(sym);
            cycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else {
            throw new RuntimeException("Cycle body expression expected, got " + sym);
        }
    }

    //ifCycleStmt                 ::= IF boolExpr THEN ifCycleBody (ELSIF boolExpr THEN ifCycleBody)? (ELSE ifCycleBody)? END IF ';'
    private void parseIfCycleStmt(IfCycleStmtVar ifCycleStmt) throws CloneNotSupportedException{
        ifCycleStmt.addSymbol(sym);
        ifCycleStmt.setStart(sym.getStart());
        parse(TokenTag.IF);

        BoolExprVar boolExpr = new BoolExprVar();
        ifCycleStmt.addSymbol(boolExpr);
        parseBoolExpr(boolExpr);

        ifCycleStmt.addSymbol(sym);
        parse(TokenTag.THEN);

        IfCycleBodyVar ifCycleBody = new IfCycleBodyVar();
        ifCycleBody.addSymbol(ifCycleBody);
        parseIfCycleBody(ifCycleBody);

        if (sym.getTag() == TokenTag.ELSIF) {
            ifCycleStmt.addSymbol(sym);
            parse(TokenTag.ELSIF);

            BoolExprVar boolExprVar = new BoolExprVar();
            ifCycleStmt.addSymbol(boolExprVar);
            parseBoolExpr(boolExprVar);

            ifCycleStmt.addSymbol(sym);
            parse(TokenTag.THEN);

            IfCycleBodyVar ifCycleBodyVar = new IfCycleBodyVar();
            ifCycleStmt.addSymbol(ifCycleBodyVar);
            parseIfCycleBody(ifCycleBodyVar);
        }

        if (sym.getTag() == TokenTag.ELSE) {
            ifCycleStmt.addSymbol(sym);
            parse(TokenTag.ELSE);

            IfCycleBodyVar ifCycleBodyVar = new IfCycleBodyVar();
            ifCycleStmt.addSymbol(ifCycleBodyVar);
            parseIfCycleBody(ifCycleBodyVar);
        }

        ifCycleStmt.addSymbol(sym);
        parse(TokenTag.END);

        ifCycleStmt.addSymbol(sym);
        parse(TokenTag.IF);

        ifCycleStmt.addSymbol(sym);
        parse(TokenTag.SEMICOLON);
    }

    //ifCycleBody                 ::= declareBlock? BEGIN cycleBody* END ';'
    //                            |   (cycleBody)?
    private void parseIfCycleBody(IfCycleBodyVar ifCycleBody) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DECLARE
                || sym.getTag() == TokenTag.BEGIN) {
            boolean wasDeclare = false;
            if (sym.getTag() == TokenTag.DECLARE) {
                DeclareBlockVar declareBlock = new DeclareBlockVar();
                ifCycleBody.addSymbol(declareBlock);
                parseDeclareBlock(declareBlock);
                ifCycleBody.setStart(declareBlock.getStart());
                wasDeclare = true;
            }

            ifCycleBody.addSymbol(sym);
            if (!wasDeclare)
                ifCycleBody.setStart(sym.getStart());
            parse(TokenTag.BEGIN);

            while (sym.getTag() == TokenTag.IDENTIFIER
                    || sym.getTag() == TokenTag.RETURN
                    || sym.getTag() == TokenTag.IF
                    || sym.getTag() == TokenTag.WHILE
                    || sym.getTag() == TokenTag.LOOP
                    || sym.getTag() == TokenTag.FOR
                    || sym.getTag() == TokenTag.NULL
                    || sym.getTag() == TokenTag.RAISE
                    || sym.getTag() == TokenTag.INSERT
                    || sym.getTag() == TokenTag.UPDATE
                    || sym.getTag() == TokenTag.DELETE
                    || sym.getTag() == TokenTag.SELECT
                    || sym.getTag() == TokenTag.LPAREN
                    || sym.getTag() == TokenTag.EXIT
                    || sym.getTag() == TokenTag.CONTINUE) {
                CycleBodyVar cycleBody = new CycleBodyVar();
                ifCycleBody.addSymbol(cycleBody);
                parseCycleBody(cycleBody);
            }

            ifCycleBody.addSymbol(sym);
            parse(TokenTag.END);

            ifCycleBody.addSymbol(sym);
            ifCycleBody.setFollow(sym.getFollow());
            parse(TokenTag.SEMICOLON);
        } else if (sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.RETURN
                || sym.getTag() == TokenTag.IF
                || sym.getTag() == TokenTag.WHILE
                || sym.getTag() == TokenTag.LOOP
                || sym.getTag() == TokenTag.FOR
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.RAISE
                || sym.getTag() == TokenTag.INSERT
                || sym.getTag() == TokenTag.UPDATE
                || sym.getTag() == TokenTag.DELETE
                || sym.getTag() == TokenTag.SELECT
                || sym.getTag() == TokenTag.LPAREN
                || sym.getTag() == TokenTag.EXIT
                || sym.getTag() == TokenTag.CONTINUE) {
            CycleBodyVar cycleBody = new CycleBodyVar();
            ifCycleBody.addSymbol(cycleBody);
            parseCycleBody(cycleBody);
            ifCycleBody.setCoords(cycleBody.getCoords());
        }
    }

    //SelectStmt                  ::= SELECT allDistinctClause? targetList?
    //                                FROM fromList whereClause?
    //                                groupClause? havingClause? sortClause?
    //                                ( unionIntOps allOrDistinct? SelectStmt )?
    private void parseSelectStmt(SelectStmtVar selectStmt) throws CloneNotSupportedException {
        selectStmt.addSymbol(sym);
        selectStmt.setStart(sym.getStart());
        parse(TokenTag.SELECT);

        if (sym.getTag() == TokenTag.ALL
                || sym.getTag() == TokenTag.DISTINCT) {
            AllDistinctClauseVar allDistinctClause = new AllDistinctClauseVar();
            selectStmt.addSymbol(allDistinctClause);
            parseAllDistinctClause(allDistinctClause);
        }

        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.MUL
                || sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.AVG
                || sym.getTag() == TokenTag.SUM
                || sym.getTag() == TokenTag.COUNT
                || sym.getTag() == TokenTag.MIN
                || sym.getTag() == TokenTag.MAX) {
            TargetListVar targetList = new TargetListVar();
            selectStmt.addSymbol(targetList);
            parseTargetList(targetList);
        }

        selectStmt.addSymbol(sym);
        parse(TokenTag.FROM);

        FromListVar fromList = new FromListVar();
        selectStmt.addSymbol(fromList);
        parseFromList(fromList);
        selectStmt.setFollow(fromList.getFollow());

        if (sym.getTag() == TokenTag.WHERE) {
            WhereClauseVar whereClause = new WhereClauseVar();
            selectStmt.addSymbol(whereClause);
            parseWhereClause(whereClause);
            selectStmt.setFollow(whereClause.getFollow());
        }

        if (sym.getTag() == TokenTag.GROUP) {
            GroupClauseVar groupClause = new GroupClauseVar();
            selectStmt.addSymbol(groupClause);
            parseGroupClause(groupClause);
            selectStmt.setFollow(groupClause.getFollow());
        }

        if (sym.getTag() == TokenTag.HAVING) {
            HavingClauseVar havingClause = new HavingClauseVar();
            selectStmt.addSymbol(havingClause);
            parseHavingClause(havingClause);
            selectStmt.setFollow(havingClause.getFollow());
        }

        if (sym.getTag() == TokenTag.ORDER) {
            SortClauseVar sortClause = new SortClauseVar();
            selectStmt.addSymbol(sortClause);
            parseSortClause(sortClause);
            selectStmt.setFollow(sortClause.getFollow());
        }

        if (sym.getTag() == TokenTag.UNION
                || sym.getTag() == TokenTag.INTERSECT
                || sym.getTag() == TokenTag.EXCEPT) {
            UnionIntOpsVar unionIntOps = new UnionIntOpsVar();
            selectStmt.addSymbol(unionIntOps);
            parseUnionIntOps(unionIntOps);

            if (sym.getTag() == TokenTag.ALL
                    || sym.getTag() == TokenTag.DISTINCT) {
                AllOrDistinctVar allOrDistinct = new AllOrDistinctVar();
                selectStmt.addSymbol(allOrDistinct);
                parseAllOrDistinct(allOrDistinct);
            }

            SelectStmtVar selectStmtVar = new SelectStmtVar();
            selectStmt.addSymbol(selectStmtVar);
            parseSelectStmt(selectStmtVar);
            selectStmt.setFollow(selectStmtVar.getFollow());
        }
    }

    //allDistinctClause           ::= ALL
    //                            |   DISTINCT (ON '(' colRefList ')' )?
    private void parseAllDistinctClause(AllDistinctClauseVar allDistinctClause) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.ALL) {
            allDistinctClause.addSymbol(sym);
            allDistinctClause.setCoords(sym.getCoords());
            parse(TokenTag.ALL);
        } else if (sym.getTag() == TokenTag.DISTINCT) {
            allDistinctClause.addSymbol(sym);
            allDistinctClause.setCoords(sym.getCoords());
            parse(TokenTag.DISTINCT);

            if (sym.getTag() == TokenTag.ON) {
                allDistinctClause.addSymbol(sym);
                parse(TokenTag.ON);

                allDistinctClause.addSymbol(sym);
                parse(TokenTag.LPAREN);

                ColRefListVar colRefList = new ColRefListVar();
                allDistinctClause.addSymbol(colRefList);
                parseColRefList(colRefList);

                allDistinctClause.addSymbol(sym);
                allDistinctClause.setFollow(sym.getFollow());
                parse(TokenTag.RPAREN);
            }
        } else {
            throw new RuntimeException("ALL or DISTINCT expected, got " + sym);
        }
    }

    //allOrDistinct               ::= ALL | DISTINCT
    private void parseAllOrDistinct(AllOrDistinctVar allOrDistinct) throws CloneNotSupportedException {
        allOrDistinct.addSymbol(sym);
        allOrDistinct.setCoords(sym.getCoords());
        if (sym.getTag() == TokenTag.ALL) {
            parse(TokenTag.ALL);
        } else if (sym.getTag() == TokenTag.DISTINCT) {
            parse(TokenTag.DISTINCT);
        } else {
            throw new RuntimeException("ALL or DISTINCT expected, got " + sym);
        }
    }

    //unionIntOps                 ::= UNION
    //                            |   INTERSECT
    //                            |   EXCEPT
    private void parseUnionIntOps(UnionIntOpsVar unionIntOps) throws CloneNotSupportedException {
        unionIntOps.addSymbol(sym);
        unionIntOps.setCoords(sym.getCoords());
        if (sym.getTag() == TokenTag.UNION) {
            parse(TokenTag.UNION);
        } else if (sym.getTag() == TokenTag.INTERSECT) {
            parse(TokenTag.INTERSECT);
        } else if (sym.getTag() == TokenTag.EXCEPT) {
            parse(TokenTag.EXCEPT);
        } else {
            throw new RuntimeException("UNION, INTERSECT or EXCEPT expected, got " + sym);
        }
    }

    //sortClause                  ::= ORDER BY sortByElem (',' sortByElem)*
    private void parseSortClause(SortClauseVar sortClause) throws CloneNotSupportedException {
        sortClause.addSymbol(sym);
        sortClause.setStart(sym.getStart());
        parse(TokenTag.ORDER);

        sortClause.addSymbol(sym);
        parse(TokenTag.BY);

        SortByElemVar sortByElem = new SortByElemVar();
        sortClause.addSymbol(sortByElem);
        parseSortByElem(sortByElem);
        sortClause.setFollow(sortByElem.getFollow());

        //TODO FIRST(ColId)
        while (sym.getTag() == TokenTag.COMMA) {
            sortClause.addSymbol(sym);
            parse(TokenTag.COMMA);

            SortByElemVar sortByElemVar = new SortByElemVar();
            sortClause.addSymbol(sortByElemVar);
            parseSortByElem(sortByElemVar);
            sortClause.setFollow(sortByElemVar.getFollow());
        }
    }

    //sortByElem                  ::= colRef ascDesc?
    private void parseSortByElem(SortByElemVar sortByElem) throws  CloneNotSupportedException {
        ColRefVar colRef = new ColRefVar();
        sortByElem.addSymbol(colRef);
        parseColRef(colRef);
        sortByElem.setCoords(colRef.getCoords());

        if (sym.getTag() == TokenTag.ASC
                || sym.getTag() == TokenTag.DESC) {
            AscDescVar ascDesc = new AscDescVar();
            sortByElem.addSymbol(ascDesc);
            parseAscDesc(ascDesc);
            sortByElem.setFollow(ascDesc.getFollow());
        }
    }

    //colRef                      ::= intConst //>=0 номер столбца
    //                            |   ColId
    private void parseColRef(ColRefVar colRef) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST) {
            colRef.addSymbol(sym);
            colRef.setCoords(sym.getCoords());
            parseIntConst();
        } else if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColIdVar colId = new ColIdVar();
            colRef.addSymbol(colId);
            parseColId(colId);
            colRef.setCoords(colId.getCoords());
        } else {
            throw new RuntimeException("Int const or identifier expected, got" + sym);
        }
    }

    //ascDesc                     ::= ASC | DESC //ASC ON DEFAULT
    private void parseAscDesc(AscDescVar ascDesc) throws CloneNotSupportedException {
        ascDesc.addSymbol(sym);
        ascDesc.setCoords(sym.getCoords());
        if (sym.getTag() == TokenTag.ASC) {
            parse(TokenTag.ASC);
        } else if (sym.getTag() == TokenTag.DESC) {
            parse(TokenTag.DESC);
        } else {
            throw new RuntimeException("ASC or DESC expected, got " + sym);
        }
    }

    //targetList                  ::= targetEl (',' targetEl)*
    private void parseTargetList(TargetListVar targetList) throws CloneNotSupportedException {
        TargetElVar targetEl = new TargetElVar();
        targetList.addSymbol(targetEl);
        parseTargetEl(targetEl);
        targetList.setCoords(targetEl.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            targetList.addSymbol(sym);
            parse(TokenTag.COMMA);

            TargetElVar targetElVar = new TargetElVar();
            targetList.addSymbol(targetElVar);
            parseTargetEl(targetEl);
            targetList.setFollow(targetElVar.getFollow());
        }
    }

    //targetEl                    ::= targetExpr aliasClause?
    //                            |   '*'
    private void parseTargetEl(TargetElVar targetEl) throws CloneNotSupportedException {
        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.AVG
                || sym.getTag() == TokenTag.SUM
                || sym.getTag() == TokenTag.COUNT
                || sym.getTag() == TokenTag.MIN
                || sym.getTag() == TokenTag.MAX) {
            TargetExprVar targetExpr = new TargetExprVar();
            targetEl.addSymbol(targetExpr);
            parseTargetExpr(targetExpr);
            targetEl.setCoords(targetExpr.getCoords());

            //TODO FIRST(ColId)
            if (sym.getTag() == TokenTag.AS
                    || sym.getTag() == TokenTag.IDENTIFIER) {
                AliasClauseVar aliasClause = new AliasClauseVar();
                targetEl.addSymbol(aliasClause);
                parseAliasClause(aliasClause);
                targetEl.setFollow(aliasClause.getFollow());
            }
        } else if (sym.getTag() == TokenTag.MUL) {
            targetEl.addSymbol(sym);
            targetEl.setCoords(sym.getCoords());
            parse(TokenTag.MUL);
        } else {
            throw new RuntimeException("Target columns or '*' expected, got " + sym);
        }
    }

    //targetExpr                  ::= ColId                //если есть аггрегатные функции, то должен содержаться или в них или в GROUP BY
    //                            |   AVG   '(' ColId ')'
    //                            |   SUM   '(' ColId ')'
    //                            |   COUNT '(' ColId ')'
    //                            |   MIN   '(' ColId ')'
    //                            |   MAX   '(' ColId ')'
    private void parseTargetExpr(TargetExprVar targetExpr) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColIdVar colId = new ColIdVar();
            targetExpr.addSymbol(colId);
            parseColId(colId);
            targetExpr.setCoords(colId.getCoords());
        } else if (sym.getTag() == TokenTag.AVG) {
            targetExpr.addSymbol(sym);
            targetExpr.setStart(sym.getStart());
            parse(TokenTag.AVG);

            targetExpr.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            targetExpr.addSymbol(colId);
            parseColId(colId);

            targetExpr.addSymbol(sym);
            targetExpr.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.SUM) {
            targetExpr.addSymbol(sym);
            targetExpr.setStart(sym.getStart());
            parse(TokenTag.SUM);

            targetExpr.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            targetExpr.addSymbol(colId);
            parseColId(colId);

            targetExpr.addSymbol(sym);
            targetExpr.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.COUNT) {
            targetExpr.addSymbol(sym);
            targetExpr.setStart(sym.getStart());
            parse(TokenTag.COUNT);

            targetExpr.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            targetExpr.addSymbol(colId);
            parseColId(colId);

            targetExpr.addSymbol(sym);
            targetExpr.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.MIN) {
            targetExpr.addSymbol(sym);
            targetExpr.setStart(sym.getStart());
            parse(TokenTag.MIN);

            targetExpr.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            targetExpr.addSymbol(colId);
            parseColId(colId);

            targetExpr.addSymbol(sym);
            targetExpr.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.MAX) {
            targetExpr.addSymbol(sym);
            targetExpr.setStart(sym.getStart());
            parse(TokenTag.MAX);

            targetExpr.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            targetExpr.addSymbol(colId);
            parseColId(colId);

            targetExpr.addSymbol(sym);
            targetExpr.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else {
            throw new RuntimeException("Identifier or aggregation function expected, got " + sym);
        }
    }

    //fromList                    ::= tableRef (',' tableRef)*
    private void parseFromList(FromListVar fromList) throws CloneNotSupportedException {
        TableRefVar tableRef = new TableRefVar();
        fromList.addSymbol(tableRef);
        parseTableRef(tableRef);
        fromList.setCoords(tableRef.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            fromList.addSymbol(sym);
            parse(TokenTag.COMMA);

            TableRefVar tableRefVar = new TableRefVar();
            fromList.addSymbol(tableRefVar);
            parseTableRef(tableRefVar);
            fromList.setFollow(tableRefVar.getFollow());
        }
    }

    //whereClause                 ::= WHERE boolExpr
    private void parseWhereClause(WhereClauseVar whereClause) throws CloneNotSupportedException {
        whereClause.addSymbol(sym);
        whereClause.setStart(sym.getStart());
        parse(TokenTag.WHERE);

        BoolExprVar boolExpr = new BoolExprVar();
        whereClause.addSymbol(boolExpr);
        parseBoolExpr(boolExpr);
        whereClause.setFollow(boolExpr.getFollow());
    }

    //groupClause                 ::= GROUP BY colRef (',' colRef)*
    private void parseGroupClause(GroupClauseVar groupClause) throws CloneNotSupportedException {
        groupClause.addSymbol(sym);
        groupClause.setStart(sym.getStart());
        parse(TokenTag.GROUP);

        groupClause.addSymbol(sym);
        parse(TokenTag.BY);

        ColRefVar colRef = new ColRefVar();
        groupClause.addSymbol(colRef);
        parseColRef(colRef);
        groupClause.setFollow(colRef.getFollow());

        while (sym.getTag() == TokenTag.COMMA) {
            ColRefVar colRefVar = new ColRefVar();
            groupClause.addSymbol(colRefVar);
            parseColRef(colRefVar);
            groupClause.setFollow(colRefVar.getFollow());
        }
    }

    //havingClause                ::= HAVING boolExpr
    private void parseHavingClause(HavingClauseVar havingClause) throws CloneNotSupportedException {
        havingClause.addSymbol(sym);
        havingClause.setStart(sym.getStart());
        parse(TokenTag.HAVING);

        BoolExprVar boolExpr = new BoolExprVar();
        havingClause.addSymbol(boolExpr);
        parseBoolExpr(boolExpr);
        havingClause.setFollow(boolExpr.getFollow());
    }

    //colRefList                  ::= colRef (',' colRef)*
    private void parseColRefList(ColRefListVar colRefList) throws CloneNotSupportedException {
        ColRefVar colRef = new ColRefVar();
        colRefList.addSymbol(colRef);
        parseColRef(colRef);
        colRefList.setCoords(colRef.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            colRefList.addSymbol(sym);
            parse(TokenTag.COMMA);

            ColRefVar colRefVar = new ColRefVar();
            colRefList.addSymbol(colRefVar);
            parseColRef(colRefVar);
            colRefList.setFollow(colRefVar.getFollow());
        }
    }

    //tableRef                    ::= QualifiedName aliasClause? ( joinType? JOIN tableRef joinQual )?
    private void parseTableRef(TableRefVar tableRef) throws CloneNotSupportedException {
        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        tableRef.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);
        tableRef.setCoords(qualifiedName.getCoords());

        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.AS
                || sym.getTag() == TokenTag.IDENTIFIER) {
            AliasClauseVar aliasClause = new AliasClauseVar();
            tableRef.addSymbol(aliasClause);
            parseAliasClause(aliasClause);
            tableRef.setFollow(aliasClause.getFollow());
        }

        if (sym.getTag() == TokenTag.JOIN
                || sym.getTag() == TokenTag.FULL
                || sym.getTag() == TokenTag.LEFT
                || sym.getTag() == TokenTag.RIGHT
                || sym.getTag() == TokenTag.INNER) {

            if (sym.getTag() == TokenTag.FULL
                    || sym.getTag() == TokenTag.LEFT
                    || sym.getTag() == TokenTag.RIGHT
                    || sym.getTag() == TokenTag.INNER) {
                JoinTypeVar joinType = new JoinTypeVar();
                tableRef.addSymbol(joinType);
                parseJoinType(joinType);
            }

            tableRef.addSymbol(sym);
            parse(TokenTag.JOIN);

            TableRefVar tableRefVar = new TableRefVar();
            tableRef.addSymbol(tableRefVar);
            parseTableRef(tableRefVar);

            JoinQualVar joinQual = new JoinQualVar();
            tableRef.addSymbol(joinQual);
            parseJoinQual(joinQual);
            tableRef.setFollow(joinQual.getFollow());
        }
    }

    //aliasClause                 ::= AS ColId
    //                            |   ColId
    private void parseAliasClause(AliasClauseVar aliasClause) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.AS) {
            aliasClause.addSymbol(sym);
            aliasClause.setStart(sym.getStart());
            parse(TokenTag.AS);

            ColIdVar colId = new ColIdVar();
            aliasClause.addSymbol(colId);
            parseColId(colId);
            aliasClause.setFollow(colId.getFollow());
        //TODO FIRST(ColId)
        } else if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColIdVar colId = new ColIdVar();
            aliasClause.addSymbol(colId);
            parseColId(colId);
            aliasClause.setCoords(colId.getCoords());
        } else {
            throw new RuntimeException("AS or identifier expected, got " + sym);
        }
    }

    //joinType                    ::= FULL OUTER?
    //                            |   LEFT OUTER?
    //                            |   RIGHT OUTER?
    //                            |   INNER
    private void parseJoinType(JoinTypeVar joinType) throws CloneNotSupportedException {
        joinType.addSymbol(sym);
        joinType.setCoords(sym.getCoords());
        if (sym.getTag() == TokenTag.FULL) {
            parse(TokenTag.FULL);
            if (sym.getTag() == TokenTag.OUTER) {
                joinType.addSymbol(sym);
                joinType.setFollow(sym.getFollow());
                parse(TokenTag.OUTER);
            }
        } else if (sym.getTag() == TokenTag.LEFT) {
            parse(TokenTag.LEFT);
            if (sym.getTag() == TokenTag.OUTER) {
                joinType.addSymbol(sym);
                joinType.setFollow(sym.getFollow());
                parse(TokenTag.OUTER);
            }
        } else if (sym.getTag() == TokenTag.RIGHT) {
            parse(TokenTag.RIGHT);
            if (sym.getTag() == TokenTag.OUTER) {
                joinType.addSymbol(sym);
                joinType.setFollow(sym.getFollow());
                parse(TokenTag.OUTER);
            }
        } else if (sym.getTag() == TokenTag.INNER) {
            parse(TokenTag.INNER);
        } else {
            throw new RuntimeException("FULL, LEFT, RIGHT or INNER expected, got " + sym);
        }
    }

    //joinQual                    ::= USING '(' ColId (',' ColId)* ')'
    //                            |   ON boolExpr
    private void parseJoinQual(JoinQualVar joinQual) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.USING) {
            joinQual.addSymbol(sym);
            joinQual.setStart(sym.getStart());
            parse(TokenTag.USING);

            joinQual.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ColIdVar colId = new ColIdVar();
            joinQual.addSymbol(colId);
            parseColId(colId);

            while (sym.getTag() == TokenTag.COMMA) {
                joinQual.addSymbol(sym);
                parse(TokenTag.COMMA);

                ColIdVar colIdVar = new ColIdVar();
                joinQual.addSymbol(colIdVar);
                parseColId(colIdVar);
            }

            joinQual.addSymbol(sym);
            joinQual.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else if (sym.getTag() == TokenTag.ON) {
            joinQual.addSymbol(sym);
            joinQual.setStart(sym.getStart());
            parse(TokenTag.ON);

            BoolExprVar boolExpr = new BoolExprVar();
            joinQual.addSymbol(boolExpr);
            parseBoolExpr(boolExpr);
            joinQual.setFollow(boolExpr.getFollow());
        } else {
            throw new RuntimeException("USING or ON expected, got " + sym);
        }
    }

    //InsertStmt                  ::= INSERT INTO insertTarget insertRest
    private void parseInsertStmt(InsertStmtVar insertStmt) throws CloneNotSupportedException {
        insertStmt.addSymbol(sym);
        insertStmt.setStart(sym.getStart());
        parse(TokenTag.INSERT);

        insertStmt.addSymbol(sym);
        parse(TokenTag.INTO);

        InsertTargetVar insertTarget = new InsertTargetVar();
        insertStmt.addSymbol(insertTarget);
        parseInsertTarget(insertTarget);

        InsertRestVar insertRest = new InsertRestVar();
        insertStmt.addSymbol(insertRest);
        parseInsertRest(insertRest);
        insertStmt.setFollow(insertRest.getFollow());
    }

    //insertTarget                ::= QualifiedName (AS ColId)?
    private void parseInsertTarget(InsertTargetVar insertTarget) throws CloneNotSupportedException {
        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        insertTarget.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);
        insertTarget.setCoords(qualifiedName.getCoords());

        if (sym.getTag() == TokenTag.AS) {
            insertTarget.addSymbol(sym);
            parse(TokenTag.AS);

            ColIdVar colId = new ColIdVar();
            insertTarget.addSymbol(colId);
            parseColId(colId);
            insertTarget.setFollow(colId.getFollow());
        }
    }

    //insertRest                  ::= SelectStmt
    //                            |   '(' insertColumnList ')' InsertSelectOrValues
    //                            |   DEFAULT VALUES
    private void parseInsertRest(InsertRestVar insertRest) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.SELECT) {
            SelectStmtVar selectStmt = new SelectStmtVar();
            insertRest.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            insertRest.setCoords(selectStmt.getCoords());
        } else if (sym.getTag() == TokenTag.LPAREN) {
            insertRest.addSymbol(sym);
            insertRest.setStart(sym.getStart());
            parse(TokenTag.LPAREN);

            InsertColumnListVar insertColumnList = new InsertColumnListVar();
            insertRest.addSymbol(insertColumnList);
            parseInsertColumnList(insertColumnList);

            insertRest.addSymbol(sym);
            parse(TokenTag.RPAREN);

            InsertSelectOrValuesVar insertSelectOrValues = new InsertSelectOrValuesVar();
            insertRest.addSymbol(insertSelectOrValues);
            parseInsertSelectOrValues(insertSelectOrValues);
            insertRest.setFollow(insertSelectOrValues.getFollow());
        } else if (sym.getTag() == TokenTag.DEFAULT) {
            insertRest.addSymbol(sym);
            insertRest.setStart(sym.getStart());
            parse(TokenTag.DEFAULT);

            insertRest.addSymbol(sym);
            insertRest.setFollow(sym.getFollow());
            parse(TokenTag.VALUES);
        } else {
            throw new RuntimeException("Select statement, values or default values expected, got " + sym);
        }
    }

    //InsertSelectOrValues        ::= SelectStmt
    //                            |   VALUES '(' insertedValue (',' insertedValue)* ')'
    private void parseInsertSelectOrValues(InsertSelectOrValuesVar insertSelectOrValues) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.SELECT) {
            SelectStmtVar selectStmt = new SelectStmtVar();
            insertSelectOrValues.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            insertSelectOrValues.setFollow(selectStmt.getFollow());
        } else if (sym.getTag() == TokenTag.VALUES) {
            insertSelectOrValues.addSymbol(sym);
            insertSelectOrValues.setFollow(sym.getFollow());
            parse(TokenTag.VALUES);

            insertSelectOrValues.addSymbol(sym);
            parse(TokenTag.LPAREN);

            InsertedValueVar insertedValue = new InsertedValueVar();
            insertSelectOrValues.addSymbol(insertedValue);
            parseInsertedValue(insertedValue);

            while (sym.getTag() == TokenTag.COMMA) {
                insertSelectOrValues.addSymbol(sym);
                parse(TokenTag.COMMA);

                InsertedValueVar insertedValueVar = new InsertedValueVar();
                insertSelectOrValues.addSymbol(insertedValueVar);
                parseInsertedValue(insertedValueVar);
            }

            insertSelectOrValues.addSymbol(sym);
            insertSelectOrValues.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else {
            throw new RuntimeException("Select statement or VALUES expected, got" + sym);
        }
    }

    //insertedValue               ::= DEFAULT
    //                            |   Expr
    private void parseInsertedValue(InsertedValueVar insertedValue) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.DEFAULT) {
            insertedValue.addSymbol(sym);
            insertedValue.setCoords(sym.getCoords());
            parse(TokenTag.DEFAULT);
        } else if (sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN
                || sym.getTag() == TokenTag.NOT
                || sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.STRING_CONST
                || sym.getTag() == TokenTag.DATE_CONST) {
            ExprVar expr = new ExprVar();
            insertedValue.addSymbol(expr);
            parseExpr(expr);
            insertedValue.setCoords(expr.getCoords());
        } else {
            throw new RuntimeException("DEFAULT or expression expected, got " + sym);
        }
    }

    //insertColumnList            ::= ColId (',' ColId)*
    private void parseInsertColumnList(InsertColumnListVar insertColumnList) throws CloneNotSupportedException {
        ColIdVar colId = new ColIdVar();
        insertColumnList.addSymbol(colId);
        parseColId(colId);
        insertColumnList.setCoords(colId.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            insertColumnList.addSymbol(sym);
            parse(TokenTag.COMMA);

            ColIdVar colIdVar = new ColIdVar();
            insertColumnList.addSymbol(colIdVar);
            parseColId(colIdVar);
            insertColumnList.setFollow(colIdVar.getFollow());
        }
    }

    //UpdateStmt                  ::= UPDATE qualifiedName aliasClause?
    //                                SET setClauseList (FROM fromList)? whereClause?
    private void parseUpdateStmt(UpdateStmtVar updateStmt) throws CloneNotSupportedException {
        updateStmt.addSymbol(sym);
        updateStmt.setStart(sym.getStart());
        parse(TokenTag.UPDATE);

        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        updateStmt.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);

        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.AS
                || sym.getTag() == TokenTag.IDENTIFIER) {
            AliasClauseVar aliasClause = new AliasClauseVar();
            updateStmt.addSymbol(aliasClause);
            parseAliasClause(aliasClause);
        }

        updateStmt.addSymbol(sym);
        parse(TokenTag.SET);

        SetClauseListVar setClauseList = new SetClauseListVar();
        updateStmt.addSymbol(setClauseList);
        parseSetClauseList(setClauseList);
        updateStmt.setFollow(setClauseList.getFollow());

        if (sym.getTag() == TokenTag.FROM) {
            updateStmt.addSymbol(sym);
            parse(TokenTag.FROM);

            FromListVar fromList = new FromListVar();
            updateStmt.addSymbol(fromList);
            parseFromList(fromList);
            updateStmt.setFollow(fromList.getFollow());
        }

        if (sym.getTag() == TokenTag.WHERE) {
            WhereClauseVar whereClause = new WhereClauseVar();
            updateStmt.addSymbol(whereClause);
            parseWhereClause(whereClause);
            updateStmt.setFollow(whereClause.getFollow());
        }
    }

    //setClauseList               ::= setClause (',' setClause)*
    private void parseSetClauseList(SetClauseListVar setClauseList) throws CloneNotSupportedException {
        SetClauseVar setClause = new SetClauseVar();
        setClauseList.addSymbol(setClause);
        parseSetClause(setClause);
        setClauseList.setCoords(setClause.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            setClauseList.addSymbol(sym);
            parse(TokenTag.COMMA);

            SetClauseVar setClauseVar = new SetClauseVar();
            setClauseList.addSymbol(setClauseVar);
            parseSetClause(setClauseVar);
            setClauseList.setFollow(setClauseVar.getFollow());
        }
    }

    //setClause                   ::= QualifiedName '=' setClauseRest
    //                            |   '(' setTargetList ')' '=' setClauseRest
    private void parseSetClause(SetClauseVar setClause) throws CloneNotSupportedException {
        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            QualifiedNameVar qualifiedName = new QualifiedNameVar();
            setClause.addSymbol(qualifiedName);
            parseQualifiedName(qualifiedName);
            setClause.setStart(qualifiedName.getStart());

            setClause.addSymbol(sym);
            parse(TokenTag.EQUAL);

            SetClauseRestVar setClauseRest = new SetClauseRestVar();
            setClause.addSymbol(setClauseRest);
            parseSetClauseRest(setClauseRest);
            setClause.setFollow(setClauseRest.getFollow());
        } else if (sym.getTag() == TokenTag.LPAREN) {
            setClause.addSymbol(sym);
            setClause.setStart(sym.getStart());
            parse(TokenTag.LPAREN);

            SetTargetListVar setTargetList = new SetTargetListVar();
            setClause.addSymbol(setTargetList);
            parseSetTargetList(setTargetList);

            setClause.addSymbol(sym);
            parse(TokenTag.RPAREN);

            setClause.addSymbol(sym);
            parse(TokenTag.EQUAL);

            SetClauseRestVar setClauseRest = new SetClauseRestVar();
            setClause.addSymbol(setClauseRest);
            parseSetClauseRest(setClauseRest);
            setClause.setFollow(setClauseRest.getFollow());
        } else {
            throw new RuntimeException("Identifier or '(' expected, got " + sym);
        }
    }

    //setClauseRest               ::= Expr
    //                            |   DEFAULT
    //                            |   SelectStmt  //UNSUPPORTED SelectStmt должен возвращать 1 строку (try_catch)
    private void parseSetClauseRest(SetClauseRestVar setClauseRest) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN
                || sym.getTag() == TokenTag.NOT
                || sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.TRUE
                || sym.getTag() == TokenTag.FALSE
                || sym.getTag() == TokenTag.NULL
                || sym.getTag() == TokenTag.STRING_CONST
                || sym.getTag() == TokenTag.DATE_CONST) {
            ExprVar expr = new ExprVar();
            setClauseRest.addSymbol(expr);
            parseExpr(expr);
            setClauseRest.setCoords(expr.getCoords());
        } else if (sym.getTag() == TokenTag.DEFAULT) {
            setClauseRest.addSymbol(sym);
            setClauseRest.setCoords(sym.getCoords());
            parse(TokenTag.DEFAULT);
        } else if (sym.getTag() == TokenTag.SELECT) {
            SelectStmtVar selectStmt = new SelectStmtVar();
            setClauseRest.addSymbol(selectStmt);
            parseSelectStmt(selectStmt);
            setClauseRest.setCoords(selectStmt.getCoords());
        } else {
            throw new RuntimeException("Expression, DEFAULT or SELECT expected, got " + sym);
        }
    }

    //setTargetList              ::= QualifiedName (',' QualifiedName)*
    private void parseSetTargetList(SetTargetListVar setTargetList) throws CloneNotSupportedException {
        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        setTargetList.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);
        setTargetList.setCoords(qualifiedName.getCoords());

        while (sym.getTag() == TokenTag.COMMA) {
            setTargetList.addSymbol(sym);
            parse(TokenTag.COMMA);

            QualifiedNameVar qualifiedNameVar = new QualifiedNameVar();
            setTargetList.addSymbol(qualifiedNameVar);
            parseQualifiedName(qualifiedNameVar);
            setTargetList.setFollow(qualifiedNameVar.getFollow());
        }
    }

    //DeleteStmt                  ::= DELETE FROM qualifiedName aliasClause? whereClause?
    private void parseDeleteStmt(DeleteStmtVar deleteStmt) throws CloneNotSupportedException {
        deleteStmt.addSymbol(sym);
        deleteStmt.setStart(sym.getStart());
        parse(TokenTag.DELETE);

        deleteStmt.addSymbol(sym);
        parse(TokenTag.FROM);

        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        deleteStmt.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);
        deleteStmt.setFollow(qualifiedName.getFollow());

        //TODO FIRST(ColId)
        if (sym.getTag() == TokenTag.AS
                || sym.getTag() == TokenTag.IDENTIFIER) {
            AliasClauseVar aliasClause = new AliasClauseVar();
            deleteStmt.addSymbol(aliasClause);
            parseAliasClause(aliasClause);
            deleteStmt.setFollow(aliasClause.getFollow());
        }

        if (sym.getTag() == TokenTag.WHERE) {
            WhereClauseVar whereClause = new WhereClauseVar();
            deleteStmt.addSymbol(whereClause);
            parseWhereClause(whereClause);
            deleteStmt.setFollow(whereClause.getFollow());
        }
    }
}
