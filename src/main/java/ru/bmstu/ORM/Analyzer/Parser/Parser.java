package ru.bmstu.ORM.Analyzer.Parser;

import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.ArithmeticExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.BooleanExpression.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Expressions.GeneralExpression.ConstExprVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.DeclareBlockVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.FuncAsVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.FuncBodyVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody.VariableDeclVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionDeclaration.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Table.*;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Trigger.CreateTriggerStmtVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Trigger.KeyActionVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Trigger.KeyActionsVar;

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
    }

    //CreateTableFunctionTrigger ::= (CreateTableStmt ';')
    //                           |   (CreateFunctionStmt ';')
    //                           |   (CreateTriggerStmt ';')
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
        } else if (sym.getTag() == TokenTag.TRIGGER) {
            CreateTriggerStmtVar createTriggerStmt = new CreateTriggerStmtVar();
            //TODO parseCreateTriggerStmt(createTriggerStmt);
            createTableFunctionTrigger.addSymbol(createTriggerStmt);
            createTableFunctionTrigger.setStart(createTriggerStmt.getStart());
        } else {
            throw new RuntimeException("TABLE, FUNCTION, TRIGGER expected, got " + sym);
        }

        createTableFunctionTrigger.addSymbol(sym);
        createTableFunctionTrigger.setFollow(sym.getFollow());
        parse(TokenTag.SEMICOLON);
    }

    //CreateTableStmt      ::= CREATE TABLE (IF NOT EXISTS)?
    //                         QualifiedName '(' (TableElement (',' TableElement)*)? ')' Inherit?
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

        if (sym.getTag() == TokenTag.INHERITS) {
            InheritVar inherit = new InheritVar();
            createTableStmt.addSymbol(inherit);
            parseInherit(inherit);

            createTableStmt.setFollow(inherit.getFollow());
        }
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

    //Inherit              ::= INHERITS '(' QualifiedName (',' QualifiedName)* ')'
    private void parseInherit(InheritVar inherit) throws CloneNotSupportedException {
        inherit.setStart(sym.getStart());
        inherit.addSymbol(sym);
        parse(TokenTag.INHERITS);

        inherit.addSymbol(sym);
        parse(TokenTag.LPAREN);

        QualifiedNameVar qualifiedName = new QualifiedNameVar();
        inherit.addSymbol(qualifiedName);
        parseQualifiedName(qualifiedName);

        while (sym.getTag() == TokenTag.COMMA) {
            inherit.addSymbol(sym);
            parse(TokenTag.COMMA);

            QualifiedNameVar qualifiedNameVar = new QualifiedNameVar();
            inherit.addSymbol(qualifiedNameVar);
            parseQualifiedName(qualifiedNameVar);
        }

        inherit.setFollow(sym.getFollow());
        inherit.addSymbol(sym);
        parse(TokenTag.RPAREN);
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

    private void parseIntConst(Var var) throws CloneNotSupportedException {
        var.addSymbol(sym);

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
                        || sym.getTag() == TokenTag.LONG_CONST)
                    parseIntConst(arrayType);

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

                parseIntConst(arrayType);

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

                parseIntConst(numericType);

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
            if (sym.getTag() == TokenTag.BYTE_CONST)
                parse(TokenTag.BYTE_CONST);
            else if (sym.getTag() == TokenTag.SHORT_CONST)
                parse(TokenTag.SHORT_CONST);
            else if (sym.getTag() == TokenTag.INT_CONST)
                parse(TokenTag.INT_CONST);
            else if (sym.getTag() == TokenTag.LONG_CONST)
                parse(TokenTag.LONG_CONST);

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

                parseIntConst(dateTimeType);

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

                parseIntConst(dateTimeType);

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

            parseNumber(arithmExprNoVarFactor);
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

    private void parseNumber(Var var) throws CloneNotSupportedException {
        var.addSymbol(var);

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

            ArithmRHSVar arithmRHS = new ArithmRHSVar();
            boolConstExprFactor.addSymbol(arithmRHS);
            parseArithmRHS(arithmRHS);
            boolConstExprFactor.setFollow(arithmRHS.getFollow());
        } else {
            throw new RuntimeException("Bool const expression expected, got " + sym);
        }
    }

    //ArithmRHS           ::= '<'  ArithmConstExpr
    //                    |   '<=' ArithmConstExpr
    //                    |   '>'  ArithmConstExpr
    //                    |   '>=' ArithmConstExpr
    //                    |   '='  ArithmConstExpr
    //                    |   '!=' ArithmConstExpr
    //                    |   BETWEEN ArithmConstExpr AND ArithmConstExpr     // ARITHMETIC ONLY TILL
    private void parseArithmRHS(ArithmRHSVar arithmRHS) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.LESS) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.LESS);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.LESSEQ) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.LESSEQ);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.GREATER) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.GREATER);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.GREATEREQ) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.GREATEREQ);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.EQUAL) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.EQUAL);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.NOTEQUAL) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.NOTEQUAL);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);
            arithmRHS.setFollow(arithmConstExpr.getFollow());
        } else if (sym.getTag() == TokenTag.BETWEEN) {
            arithmRHS.addSymbol(sym);
            arithmRHS.setStart(sym.getStart());
            parse(TokenTag.BETWEEN);

            ArithmConstExprVar arithmConstExpr = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExpr);
            parseArithmConstExpr(arithmConstExpr);

            arithmRHS.addSymbol(sym);
            parse(TokenTag.AND);

            ArithmConstExprVar arithmConstExprVar = new ArithmConstExprVar();
            arithmRHS.addSymbol(arithmConstExprVar);
            parseArithmConstExpr(arithmConstExprVar);
            arithmRHS.setFollow(arithmConstExprVar.getFollow());
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

            arithmExprFactor.setCoords(sym.getCoords());

            parseNumber(arithmExprFactor);
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
        } else if (sym.getTag() == TokenTag.TRIGGER) {
            createFunctionAllReturnStmt.addSymbol(sym);
            createFunctionAllReturnStmt.setStart(sym.getStart());
            parse(TokenTag.TRIGGER);

            CreateFuncBodyVar createFuncBody = new CreateFuncBodyVar();
            createFunctionAllReturnStmt.addSymbol(createFuncBody);
            parseCreateFuncBody(createFuncBody);
            createFunctionAllReturnStmt.setFollow(createFuncBody.getFollow());
        } else {
            throw new RuntimeException("Typename, TABLE or TRIGGER expected, got " + sym);
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
}
