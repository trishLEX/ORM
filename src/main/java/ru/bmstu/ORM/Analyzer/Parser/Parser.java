package ru.bmstu.ORM.Analyzer.Parser;

import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.NumberToken;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.*;

import java.util.HashMap;

public class Parser {
    private Scanner scanner;
    private SVar start;
    private Token sym;
    private CreateTableStmtVar currentTable;
    private HashMap<QualifiedNameVar, CreateTableStmtVar> tables;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.start = new SVar();
        this.tables = new HashMap<>();
    }

    public HashMap<QualifiedNameVar, CreateTableStmtVar> getTables() {
        return tables;
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

    //S                    ::= (CREATE CreateTableFunction)*
    private void parseS(SVar s) throws CloneNotSupportedException {
        boolean wasFirst = false;
        while (sym.getTag() == TokenTag.CREATE) {
            s.addSymbol(sym);
            if (!wasFirst) {
                s.setStart(sym.getStart());
                wasFirst = true;
            }
            parse(TokenTag.CREATE);

            CreateTableFunctionVar createTableFunction = new CreateTableFunctionVar();
            parseCreateTableFunction(createTableFunction);
            s.addSymbol(createTableFunction);
            s.setFollow(createTableFunction.getFollow());
        }
        if (sym.getTag() != TokenTag.END_OF_PROGRAM)
            throw new RuntimeException("EOF expected, got " + sym);
    }

    //CreateTableFunctionTrigger ::= (CreateTableStmt ';')
    //                           |   (CreateFunctionStmt ';')
    private void parseCreateTableFunction(CreateTableFunctionVar createTableFunction) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.TABLE) {
            CreateTableStmtVar createTableStmt = new CreateTableStmtVar();
            parseCreateTableStmt(createTableStmt);
            createTableFunction.addSymbol(createTableStmt);
            createTableFunction.setStart(createTableStmt.getStart());
            tables.put(createTableStmt.getTableName(), createTableStmt);
        } else if (sym.getTag() == TokenTag.FUNCTION || sym.getTag() == TokenTag.OR) {
            CreateFunctionStmtVar createFunctionStmt = new CreateFunctionStmtVar();
            parseCreateFunctionStmt(createFunctionStmt);
            createTableFunction.addSymbol(createFunctionStmt);
            createTableFunction.setStart(createFunctionStmt.getStart());
        } else {
            throw new RuntimeException("TABLE, FUNCTION, TRIGGER expected, got " + sym);
        }

        createTableFunction.addSymbol(sym);
        createTableFunction.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //CreateTableStmt      ::= TABLE (IF NOT EXISTS)?
    //                         QualifiedName '(' (TableElement (',' TableElement)*)? ')'
    private void parseCreateTableStmt(CreateTableStmtVar createTableStmt) throws CloneNotSupportedException {
        currentTable = createTableStmt;

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
        createTableStmt.setTableName(qualifiedName);
        createTableStmt.addSymbol(sym);
        parse(TokenTag.LPAREN);

        if (sym.getTag() == TokenTag.IDENTIFIER ||
                sym.getTag() == TokenTag.CONSTRAINT) {

            TableElementVar tableElement = new TableElementVar();
            createTableStmt.addSymbol(tableElement);
            parseTableElement(tableElement);

            while (sym.getTag() == TokenTag.COMMA) {
                tableElement.addSymbol(sym);
                parse(TokenTag.COMMA);

                TableElementVar tableElementVar = new TableElementVar();
                createTableStmt.addSymbol(tableElementVar);
                parseTableElement(tableElementVar);
            }
        }

        createTableStmt.addSymbol(sym);
        createTableStmt.setFollow(sym.getFollow());
        parse(TokenTag.RPAREN);
    }

    //QualifiedName        ::= IDENT('.'IDENT)*
    private void parseQualifiedName(QualifiedNameVar qualifiedName) throws CloneNotSupportedException { ;
        qualifiedName.addSymbol(sym);
        qualifiedName.setStart(sym.getStart());

        Token col = sym;

        parse(TokenTag.IDENTIFIER);

        while (sym.getTag() == TokenTag.DOT) {
            if (!col.getFollow().equals(sym.getStart()))
                throw new RuntimeException("Wrong identifier at " + sym.getStart());

            qualifiedName.addSymbol(sym);
            parse(TokenTag.DOT);

            col = sym;
            qualifiedName.addSymbol(col);
            parse(TokenTag.IDENTIFIER);
        }

        qualifiedName.setFollow(col.getFollow());
    }

    //TableElement         ::= ColumnDef | TableConstraint
    private void parseTableElement(TableElementVar tableElement) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            ColumnDefVar columnDef = new ColumnDefVar();
            tableElement.addSymbol(columnDef);
            parseColumnDef(columnDef);
            tableElement.setStart(columnDef.getStart());
            tableElement.setFollow(columnDef.getFollow());
        } else if (sym.getTag() == TokenTag.CONSTRAINT || sym.getTag() == TokenTag.UNIQUE
                || sym.getTag() == TokenTag.PRIMARY || sym.getTag() == TokenTag.FOREIGN){
            TableConstraintVar tableConstraint = new TableConstraintVar();
            tableElement.addSymbol(tableConstraint);
            parseTableConstraint(tableConstraint);
            tableElement.setStart(tableConstraint.getStart());
            tableElement.setFollow(tableConstraint.getFollow());
        } else {
            throw new RuntimeException("Identifier, CONSTRAINT, UNIQUE, PRIMARY or FOREIGN expected, got " + sym);
        }
    }

    //ColumnDef            ::= IDENT Typename ColConstraint*
    private void parseColumnDef(ColumnDefVar columnDef) throws CloneNotSupportedException {
        columnDef.addSymbol(sym);
        columnDef.setStart(sym.getStart());
        Token column = sym;
        parse(TokenTag.IDENTIFIER);

        TypenameVar typename = new TypenameVar();
        columnDef.addSymbol(typename);
        parseTypename(typename);
        columnDef.setFollow(typename.getFollow());
        currentTable.addColumn(columnDef);

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
                    Token number = sym;
                    parseIntConst();
                    Number value = ((NumberToken) number).getValue();
                    if (value.intValue() <= 0)
                        throw new RuntimeException("Array with negative size: " + number);
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
                Token number = sym;
                parseIntConst();
                Number value = ((NumberToken) number).getValue();
                if (value.intValue() <= 0)
                    throw new RuntimeException("Array with negative size: " + number);

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
            simpleTypeName.addSymbol(sym);
            simpleTypeName.setCoords(sym.getCoords());
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
            Token number = sym;
            parseIntConst();
            Number value = ((NumberToken) number).getValue();
            if (value.intValue() <= 0)
                throw new RuntimeException("String with negative size: " + number);

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

    //TableConstraint      ::= CONSTRAINT IDENT ConstraintElem | ConstraintElem
    private void parseTableConstraint(TableConstraintVar tableConstraint) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CONSTRAINT) {
            tableConstraint.setStart(sym.getStart());
            tableConstraint.addSymbol(sym);
            parse(TokenTag.CONSTRAINT);

            tableConstraint.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

            ConstraintElemVar constraintElem = new ConstraintElemVar();
            tableConstraint.addSymbol(constraintElem);
            parseConstraintElem(constraintElem);
            tableConstraint.setFollow(constraintElem.getFollow());
        } else {
            ConstraintElemVar constraintElem = new ConstraintElemVar();
            tableConstraint.addSymbol(constraintElem);
            parseConstraintElem(constraintElem);

            tableConstraint.setCoords(constraintElem.getCoords());
        }

        currentTable.addTableConstraint(tableConstraint);
    }

    //ConstraintElem       ::= UNIQUE      '(' IDENT (',' IDENT)* ')'
    //                     |   PRIMARY KEY '(' IDENT (',' IDENT)* ')'
    //                     |   FOREIGN KEY '(' IDENT (',' IDENT)* ')' REFERENCES QualifiedName
    //                         ('(' IDENT (',' IDENT)* ')' )? KeyActions?
    private void parseConstraintElem(ConstraintElemVar constraintElem) throws CloneNotSupportedException {
        constraintElem.addSymbol(sym);
        constraintElem.setStart(sym.getStart());

        if (sym.getTag() == TokenTag.UNIQUE) {
            parse(TokenTag.UNIQUE);

            constraintElem.addSymbol(sym);
            parse(TokenTag.LPAREN);

            constraintElem.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

            while (sym.getTag() == TokenTag.COMMA) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.COMMA);

                constraintElem.addSymbol(sym);
                parse(TokenTag.IDENTIFIER);
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

            constraintElem.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

            while (sym.getTag() == TokenTag.COMMA) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.COMMA);

                constraintElem.addSymbol(sym);
                parse(TokenTag.IDENTIFIER);
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

            constraintElem.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

            while (sym.getTag() == TokenTag.COMMA) {
                constraintElem.addSymbol(sym);
                parse(TokenTag.COMMA);

                constraintElem.addSymbol(sym);
                parse(TokenTag.IDENTIFIER);
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

                constraintElem.addSymbol(sym);
                parse(TokenTag.IDENTIFIER);

                while (sym.getTag() == TokenTag.COMMA) {
                    constraintElem.addSymbol(sym);
                    parse(TokenTag.COMMA);

                    constraintElem.addSymbol(sym);
                    parse(TokenTag.IDENTIFIER);
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

    //ColConstraint        ::= CONSTRAINT IDENT ColConstraintElem | ColConstraintElem
    private void parseColConstraint(ColConstraintVar colConstraint) throws  CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CONSTRAINT) {
            colConstraint.addSymbol(sym);
            colConstraint.setStart(sym.getStart());
            parse(TokenTag.CONSTRAINT);

            colConstraint.addSymbol(sym);
            parse(TokenTag.IDENTIFIER);

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
    //                     |   REFERENCES  QualifiedName ( '(' IDENT ')' )? KeyActions?
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

            if (sym.getTag() == TokenTag.LPAREN) {
                colConstraintElem.addSymbol(sym);
                parse(TokenTag.LPAREN);

                colConstraintElem.addSymbol(sym);
                parse(TokenTag.IDENTIFIER);

                colConstraintElem.addSymbol(sym);
                colConstraintElem.setFollow(sym.getFollow());
                parse(TokenTag.RPAREN);
            }

            if (sym.getTag() == TokenTag.ON) {
                KeyActionsVar keyActions = new KeyActionsVar();
                colConstraintElem.addSymbol(keyActions);
                parseKeyActions(keyActions);
                colConstraintElem.setFollow(keyActions.getFollow());
            }
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
    //                     |   IDENT RHS?                 //Check Type of Col here
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
        } else if (sym.getTag() == TokenTag.IDENTIFIER
                && (currentTable.getTypeOfColumn((IdentToken) sym) == VarTag.DATETIME_TYPE
                    || currentTable.getTypeOfColumn((IdentToken) sym) == TokenTag.BOOLEAN)) {
            boolExprFactor.addSymbol(sym);
            boolExprFactor.setCoords(sym.getCoords());
            parse(TokenTag.IDENTIFIER);

            if (sym.getTag() == TokenTag.IS
                    || sym.getTag() == TokenTag.LESS
                    || sym.getTag() == TokenTag.LESSEQ
                    || sym.getTag() == TokenTag.GREATER
                    || sym.getTag() == TokenTag.GREATEREQ
                    || sym.getTag() == TokenTag.EQUAL
                    || sym.getTag() == TokenTag.NOTEQUAL
                    || sym.getTag() == TokenTag.BETWEEN
                    || sym.getTag() == TokenTag.NOT) {
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
        } else if ((sym.getTag() == TokenTag.IDENTIFIER && currentTable.getTypeOfColumn((IdentToken) sym) == VarTag.NUMERIC_TYPE)
                || sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.SUB){
            ArithmExprVar arithmExpr = new ArithmExprVar();
            boolExprFactor.addSymbol(arithmExpr);
            parseArithmExpr(arithmExpr);
            boolExprFactor.setStart(arithmExpr.getStart());

            ArithmRHSVar arithmRHS = new ArithmRHSVar();
            boolExprFactor.addSymbol(arithmRHS);
            parseArithmRHS(arithmRHS);
            boolExprFactor.setFollow(arithmRHS.getFollow());
        } else {
            throw new RuntimeException("Wrong symbol " + sym + " boolean expression expected");
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

    //RHS                 ::= DateRHS | BoolRHS
    private void parseRHS(RHSVar rhs) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LESS
                || sym.getTag() == TokenTag.LESSEQ
                || sym.getTag() == TokenTag.GREATER
                || sym.getTag() == TokenTag.GREATEREQ
                || sym.getTag() == TokenTag.EQUAL
                || sym.getTag() == TokenTag.NOTEQUAL
                || sym.getTag() == TokenTag.BETWEEN
                || sym.getTag() == TokenTag.NOT) {
            DateRHSVar dateRHS = new DateRHSVar();
            rhs.addSymbol(dateRHS);
            parseDateRHS(dateRHS);
            rhs.setCoords(dateRHS.getCoords());
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
    //                    |   NOT? BETWEEN ArithmExpr AND ArithmExpr     // ARITHMETIC ONLY TILL
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
        } else if (sym.getTag() == TokenTag.BETWEEN || sym.getTag() == TokenTag.NOT) {
            boolean wasNot = false;
            if (sym.getTag() == TokenTag.NOT) {
                arithmRHS.addSymbol(sym);
                arithmRHS.setStart(sym.getStart());
                parse(TokenTag.NOT);
                wasNot = true;
            }
            arithmRHS.addSymbol(sym);
            if (!wasNot)
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

    //DateRHS             ::= '<'  DateTimeCast
    //                    |   '<=' DateTimeCast
    //                    |   '>'  DateTimeCast
    //                    |   '>=' DateTimeCast
    //                    |   '='  DateTimeCast
    //                    |   '!=' DateTimeCast
    //                    |   NOT? BETWEEN DateTimeCast AND DateTimeCast
    private void parseDateRHS(DateRHSVar dateRHS) throws  CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LESS) {
            dateRHS.addSymbol(sym);
            dateRHS.setStart(sym.getStart());
            parse(TokenTag.LESS);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            dateRHS.setFollow(dateTimeCast.getFollow());
        } else if (sym.getTag() == TokenTag.LESSEQ) {
            dateRHS.addSymbol(sym);
            dateRHS.setStart(sym.getStart());
            parse(TokenTag.LESSEQ);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            dateRHS.setFollow(dateTimeCast.getFollow());
        } else if (sym.getTag() == TokenTag.GREATER) {
            dateRHS.addSymbol(sym);
            dateRHS.setStart(sym.getStart());
            parse(TokenTag.GREATER);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            dateRHS.setFollow(dateTimeCast.getFollow());
        } else if (sym.getTag() == TokenTag.GREATEREQ) {
            dateRHS.addSymbol(sym);
            dateRHS.setStart(sym.getStart());
            parse(TokenTag.GREATEREQ);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            dateRHS.setFollow(dateTimeCast.getFollow());
        } else if (sym.getTag() == TokenTag.EQUAL) {
            dateRHS.addSymbol(sym);
            dateRHS.setStart(sym.getStart());
            parse(TokenTag.EQUAL);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            dateRHS.setFollow(dateTimeCast.getFollow());
        } else if (sym.getTag() == TokenTag.NOTEQUAL) {
            dateRHS.addSymbol(sym);
            dateRHS.setStart(sym.getStart());
            parse(TokenTag.NOTEQUAL);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            dateRHS.setFollow(dateTimeCast.getFollow());
        } else if (sym.getTag() == TokenTag.BETWEEN || sym.getTag() == TokenTag.NOT) {
            boolean wasNot = false;
            if (sym.getTag() == TokenTag.NOT) {
                dateRHS.addSymbol(sym);
                dateRHS.setStart(sym.getStart());
                parse(TokenTag.NOT);
                wasNot = true;
            }
            dateRHS.addSymbol(sym);
            if (!wasNot)
                dateRHS.setStart(sym.getStart());
            parse(TokenTag.BETWEEN);

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);

            dateRHS.addSymbol(sym);
            parse(TokenTag.AND);

            DateTimeCastVar dateTimeCastVar = new DateTimeCastVar();
            dateRHS.addSymbol(dateTimeCastVar);
            parseDateTimeCast(dateTimeCastVar);
            dateRHS.setFollow(dateTimeCastVar.getFollow());
        }
    }

    //ConstExpr           ::= ArithmConstExpr | NOT? BoolConst | CharacterValue | DateValue'::'DateTimeCastVar
    private void parseConstExpr(ConstExprVar constExpr) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN) {

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

            DateTimeCastVar dateTimeCast = new DateTimeCastVar();
            constExpr.addSymbol(dateTimeCast);
            parseDateTimeCast(dateTimeCast);
            constExpr.setCoords(dateTimeCast.getCoords());
        } else {
            throw new RuntimeException("Expected expression without variables, got " + sym);
        }
    }

    //DateTimeCast        ::= DateValue'::'DATE
    //                    |   TimeValue'::'TIME
    //                    |   TimestampValue'::'TIMESTAMP
    private void parseDateTimeCast(DateTimeCastVar dateTimeCast) throws CloneNotSupportedException {
        dateTimeCast.addSymbol(sym);
        dateTimeCast.setCoords(sym.getCoords());
        if (sym.getTag() == TokenTag.TIMESTAMP_CONST)
            parse(TokenTag.TIMESTAMP_CONST);
        else if (sym.getTag() == TokenTag.DATE_CONST)
            parse(TokenTag.DATE_CONST);
        else
            parse(TokenTag.TIME_CONST);

        if (!dateTimeCast.getFollow().equals(sym.getStart()))
            throw new RuntimeException("'::' expected, got ' '");

        dateTimeCast.addSymbol(sym);
        parse(TokenTag.DOUBLE_COLON);

        dateTimeCast.addSymbol(sym);
        dateTimeCast.setFollow(sym.getFollow());

        if (dateTimeCast.get(0).getTag() == TokenTag.TIMESTAMP_CONST)
            parse(TokenTag.TIMESTAMP);
        else if (dateTimeCast.get(0).getTag() == TokenTag.DATE_CONST)
            parse(TokenTag.DATE);
        else
            parse(TokenTag.TIME);
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
        } else if (sym.getTag() == TokenTag.LPAREN){
            arithmExprNoVarFactor.setStart(sym.getStart());
            arithmExprNoVarFactor.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ArithmConstExprVar arithmExprNoVar = new ArithmConstExprVar();
            arithmExprNoVarFactor.addSymbol(arithmExprNoVar);
            parseArithmConstExpr(arithmExprNoVar);

            arithmExprNoVarFactor.setFollow(sym.getFollow());
            arithmExprNoVarFactor.addSymbol(sym);
            parse(TokenTag.RPAREN);
        } else {
            throw new RuntimeException("Number, '-' or '(' expected, got " + sym);
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

    //CreateFunctionStmt          ::= (OR REPLACE)? FUNCTION QualifiedName '(' funcArgsWithDefaultsList? ')' RETURNS
    //                                CreateFunctionReturnStmt CreateFuncBody
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

        if (sym.getTag() == TokenTag.IN
                || sym.getTag() == TokenTag.INOUT
                || sym.getTag() == TokenTag.OUT
                || sym.getTag() == TokenTag.IDENTIFIER
                || sym.getTag() == TokenTag.CHARACTER
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
            FuncArgsWithDefaultsListVar funcArgsWithDefaultsList = new FuncArgsWithDefaultsListVar();
            createFunctionStmt.addSymbol(funcArgsWithDefaultsList);
            parseFuncArgsWithDefaultsList(funcArgsWithDefaultsList);
        }

        createFunctionStmt.addSymbol(sym);
        parse(TokenTag.RPAREN);

        createFunctionStmt.addSymbol(sym);
        parse(TokenTag.RETURNS);

        CreateFunctionReturnStmtVar createFunctionReturnStmt = new CreateFunctionReturnStmtVar();
        createFunctionStmt.addSymbol(createFunctionReturnStmt);
        parseCreateFunctionReturnStmt(createFunctionReturnStmt);

        CreateFuncBodyVar createFuncBody = new CreateFuncBodyVar();
        createFunctionStmt.addSymbol(createFuncBody);
        parseCreateFuncBody(createFuncBody);
        createFunctionStmt.setFollow(createFuncBody.getFollow());
    }

    //CreateFunctionReturnStmt    ::= Typename
    //                            |   TABLE '(' TableFuncColumnList ')'
    private void parseCreateFunctionReturnStmt(CreateFunctionReturnStmtVar createFunctionReturnStmt) throws CloneNotSupportedException {
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
            createFunctionReturnStmt.addSymbol(typename);
            parseTypename(typename);
            createFunctionReturnStmt.setCoords(typename.getCoords());
        } else if (sym.getTag() == TokenTag.TABLE) {
            createFunctionReturnStmt.addSymbol(sym);
            createFunctionReturnStmt.setStart(sym.getStart());
            parse(TokenTag.TABLE);

            createFunctionReturnStmt.addSymbol(sym);
            parse(TokenTag.LPAREN);

            TableFuncColumnListVar tableFuncColumnList = new TableFuncColumnListVar();
            createFunctionReturnStmt.addSymbol(tableFuncColumnList);
            parseTableFuncColumnList(tableFuncColumnList);

            createFunctionReturnStmt.addSymbol(sym);
            createFunctionReturnStmt.setFollow(sym.getFollow());
            parse(TokenTag.RPAREN);
        } else {
            throw new RuntimeException("Typename or TABLE expected, got " + sym);
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

    //CreateFuncBody              ::= AS '$$' /*skip*/ '$$' LANGUAGE plpgsql
    //                            |   LANGUAGE plpgsql AS '$$' /*skip*/ '$$'
    private void parseCreateFuncBody(CreateFuncBodyVar createFuncBody) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.AS) {
            createFuncBody.addSymbol(sym);
            createFuncBody.setStart(sym.getStart());
            parse(TokenTag.AS);

            createFuncBody.addSymbol(sym);
            parse(TokenTag.DOUBLE_DOLLAR);

            sym = scanner.searchToken("$$");

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

            sym = scanner.searchToken("$$");

            createFuncBody.addSymbol(sym);
            createFuncBody.setFollow(sym.getFollow());
            parse(TokenTag.DOUBLE_DOLLAR);
        } else {
            throw new RuntimeException("AS or LANGUAGE expected, got " + sym);
        }
    }
}
