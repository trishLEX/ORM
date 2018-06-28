package ru.bmstu.ORM.Analyzer.Parser;

import ru.bmstu.ORM.Analyzer.Lexer.Scanner;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.Token;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.TokenTag;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.*;

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

    //S                    ::= CreateTableStmt+
    private void parseS(SVar s) throws CloneNotSupportedException {
        CreateTableStmtVar createTableStmt = new CreateTableStmtVar();
        s.addSymbol(createTableStmt);
        parseCreateTableStmt(createTableStmt);

        s.setStart(createTableStmt.getStart());

        CreateTableStmtVar createTableStmtVar;

        do {
            createTableStmtVar = new CreateTableStmtVar();
            s.addSymbol(createTableStmtVar);
            parseCreateTableStmt(createTableStmtVar);
        } while (sym.getTag() == TokenTag.CREATE);

        s.setFollow(createTableStmtVar.getFollow());
    }

    //CreateTableStmt      ::= CREATE TABLE (IF NOT EXISTS)?
    //QualifiedName '(' (TableElement (',' TableElement)*)? ')' Inherit?
    private void parseCreateTableStmt(CreateTableStmtVar createTableStmt) throws CloneNotSupportedException {
        createTableStmt.setStart(sym.getStart());

        createTableStmt.addSymbol(sym);
        parse(TokenTag.CREATE);

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
            if (colIdVar.getFollow() != sym.getStart())
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

    //Typename             ::= SimpleTypename
    private void parseTypename(TypenameVar typename) throws CloneNotSupportedException {
        SimpleTypeNameVar simpleTypeName = new SimpleTypeNameVar();
        typename.addSymbol(simpleTypeName);
        parseSympleTypeName(simpleTypeName);
        typename.setStart(simpleTypeName.getStart());
        typename.setFollow(simpleTypeName.getFollow());
    }

    //SimpleTypename       ::= NumericType | CharacterType //| DateTimeType TODO NOT SUPPORTED TILL
    private void parseSympleTypeName(SimpleTypeNameVar simpleTypeName) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.CHARACTER
                || sym.getTag() == TokenTag.CHAR
                || sym.getTag() == TokenTag.VARCHAR) {

            NumericTypeVar numericType = new NumericTypeVar();
            simpleTypeName.addSymbol(numericType);
            parseNumericType(numericType);
            simpleTypeName.setStart(numericType.getStart());
            simpleTypeName.setFollow(numericType.getFollow());
        } else {
            CharacterTypeVar characterType = new CharacterTypeVar();
            simpleTypeName.addSymbol(characterType);
            parseCharacterType(characterType);
            simpleTypeName.setStart(characterType.getStart());
            simpleTypeName.setFollow(characterType.getFollow());
        }
    }

    //NumericType          ::= INT
    //                     |   INTEGER
    //                     |   SMALLINT
    //                     |   BIGINT
    //                     |   REAL
    //                     |   FLOAT  ( '('intConst')' )? //TODO 1 <= intConst <= 53
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

                numericType.addSymbol(sym);
                parse(TokenTag.INT_CONST);

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
        } else if (sym.getTag() == TokenTag.BOOLEAN) {
            parse(TokenTag.BOOLEAN);
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
            parse(TokenTag.INT_CONST);

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

    //TODO UNSUPPORTED: DateTimeType         ::= TIMESTAMP ( '(' intConst ')' )? //0 <= intConst < 6
    //                     |   TIME ( '(' intConst ')' )?
    //                     |   DATE
//    private void parseDateTimeType(DateTimeTypeVar dateTimeType) throws CloneNotSupportedException {
//
//    }

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

                ColIdVar colIdVar = colId1;

                while (sym.getTag() == TokenTag.COMMA) {
                    constraintElem.addSymbol(sym);
                    parse(TokenTag.COMMA);

                    colIdVar = new ColIdVar();
                    constraintElem.addSymbol(colIdVar);
                    parseColId(colIdVar);
                }

                constraintElem.setFollow(colIdVar.getFollow());
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
            colConstraintElem.setStart(colConstraintElem.getStart());
            colConstraintElem.setFollow(colConstraintElem.getFollow());
        }
    }

    //ColConstraintElem    ::= NOT NULL
    //                     |   NULL
    //                     |   UNIQUE
    //                     |   PRIMARY KEY
    //                     |   CHECK '(' BoolExpr ')'    //TODO HERE NEED TO CHECK APPLICATION OF OPs
    //                     |   DEFAULT ExprNoVars        //TODO ARITHMETIC ONLY EXPR OR VALUE TILL
    //                     |   REFERENCES qualified_name ( '(' ColId (',' ColId)* ')' )? KeyActions?
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

            ExprNoVarsVar exprNoVars = new ExprNoVarsVar();
            colConstraintElem.addSymbol(exprNoVars);
            parseExprNoVars(exprNoVars);
            colConstraintElem.setFollow(exprNoVars.getFollow());
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

    //ExprNoVars           ::= ArithmExprNoVar | TRUE | FALSE | CharacterValue | DateValue
    private void parseExprNoVars(ExprNoVarsVar exprNoVars) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST
                || sym.getTag() == TokenTag.SUB
                || sym.getTag() == TokenTag.LPAREN) {

            ArithmExprNoVarVar arithmExprNoVar = new ArithmExprNoVarVar();
            exprNoVars.addSymbol(arithmExprNoVar);
            parseArithmExprNoVar(arithmExprNoVar);
            exprNoVars.setStart(arithmExprNoVar.getStart());
            exprNoVars.setFollow(arithmExprNoVar.getFollow());
        } else if (sym.getTag() == TokenTag.TRUE) {
            exprNoVars.addSymbol(sym);
            exprNoVars.setCoords(sym.getCoords());
            parse(TokenTag.TRUE);
        } else if (sym.getTag() == TokenTag.FALSE) {
            exprNoVars.addSymbol(sym);
            exprNoVars.setCoords(sym.getCoords());
            parse(TokenTag.FALSE);
        } else if (sym.getTag() == TokenTag.STRING_CONST) {
            exprNoVars.addSymbol(sym);
            exprNoVars.setCoords(sym.getCoords());
            parse(TokenTag.STRING_CONST);
        } else {
            //TODO UNSUPPORTED
            exprNoVars.addSymbol(sym);
            exprNoVars.setCoords(sym.getCoords());
            parse(TokenTag.DATE_TIME_CONST);
        }
    }

    //ArithmExprNoVar      ::= ArithmExprNoVarTerm ( {'+' | '-'} ArithmExprNoVarTerm )*
    private void parseArithmExprNoVar(ArithmExprNoVarVar arithmExprNoVar) throws CloneNotSupportedException {
        ArithmExprNoVarTermVar arithmExprNoVarTerm = new ArithmExprNoVarTermVar();
        arithmExprNoVar.addSymbol(arithmExprNoVarTerm);
        parseArithmExprNoVarTerm(arithmExprNoVarTerm);
        arithmExprNoVar.setCoords(arithmExprNoVarTerm.getCoords());

        while (sym.getTag() == TokenTag.ADD || sym.getTag() == TokenTag.SUB) {
            arithmExprNoVar.addSymbol(sym);

            if (sym.getTag() == TokenTag.ADD)
                parse(TokenTag.ADD);
            else
                parse(TokenTag.SUB);

            ArithmExprNoVarTermVar arithmExprNoVarTermVar = new ArithmExprNoVarTermVar();
            arithmExprNoVar.addSymbol(arithmExprNoVarTermVar);
            parseArithmExprNoVarTerm(arithmExprNoVarTermVar);
            arithmExprNoVar.setFollow(arithmExprNoVarTermVar.getFollow());
        }
    }

    //ArithmExprNoVarTerm  ::= ArithmExprNoVarFactor ( {'*' | '/'} ArithmExprNoVarFactor )*
    private void parseArithmExprNoVarTerm(ArithmExprNoVarTermVar arithmExprNoVarTerm) throws CloneNotSupportedException {
        ArithmExprNoVarFactorVar arithmExprNoVarFactor = new ArithmExprNoVarFactorVar();
        arithmExprNoVarTerm.addSymbol(arithmExprNoVarFactor);
        parseArithmExprNoVarFactor(arithmExprNoVarFactor);
        arithmExprNoVarTerm.setCoords(arithmExprNoVarFactor.getCoords());

        while (sym.getTag() == TokenTag.MUL || sym.getTag() == TokenTag.DIV) {
            arithmExprNoVarTerm.addSymbol(sym);

            if (sym.getTag() == TokenTag.MUL)
                parse(TokenTag.MUL);
            else
                parse(TokenTag.DIV);

            ArithmExprNoVarFactorVar arithmExprNoVarFactorVar = new ArithmExprNoVarFactorVar();
            arithmExprNoVarTerm.addSymbol(arithmExprNoVarFactorVar);
            parseArithmExprNoVarFactor(arithmExprNoVarFactorVar);
            arithmExprNoVarTerm.setFollow(arithmExprNoVarFactorVar.getFollow());
        }
    }

    //ArithmExprNoVarFactor::= NumericValue | '-' ArithmExprNoVarFactor | '(' ArithmExprNoVar ')'
    private void parseArithmExprNoVarFactor(ArithmExprNoVarFactorVar arithmExprNoVarFactor) throws CloneNotSupportedException {
        if (sym.getTag() == TokenTag.BYTE_CONST
                || sym.getTag() == TokenTag.SHORT_CONST
                || sym.getTag() == TokenTag.INT_CONST
                || sym.getTag() == TokenTag.LONG_CONST
                || sym.getTag() == TokenTag.FLOAT_CONST
                || sym.getTag() == TokenTag.DOUBLE_CONST) {

            arithmExprNoVarFactor.addSymbol(sym);
            arithmExprNoVarFactor.setCoords(sym.getCoords());

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
                throw new RuntimeException("Wrong constant at " + sym);
        } else if (sym.getTag() == TokenTag.SUB) {
            arithmExprNoVarFactor.addSymbol(sym);
            arithmExprNoVarFactor.setStart(sym.getStart());
            parse(TokenTag.SUB);

            ArithmExprNoVarFactorVar arithmExprNoVarFactorVar = new ArithmExprNoVarFactorVar();
            arithmExprNoVarFactor.addSymbol(arithmExprNoVarFactorVar);
            parseArithmExprNoVarFactor(arithmExprNoVarFactorVar);
            arithmExprNoVarFactor.setFollow(arithmExprNoVarFactorVar.getFollow());
        } else {
            arithmExprNoVarFactor.setStart(sym.getStart());
            arithmExprNoVarFactor.addSymbol(sym);
            parse(TokenTag.LPAREN);

            ArithmExprNoVarVar arithmExprNoVar = new ArithmExprNoVarVar();
            arithmExprNoVarFactor.addSymbol(arithmExprNoVar);
            parseArithmExprNoVar(arithmExprNoVar);

            arithmExprNoVarFactor.setFollow(sym.getFollow());
            arithmExprNoVarFactor.addSymbol(sym);
            parse(TokenTag.RPAREN);
        }
    }

    //BoolExpr             ::= BoolExprTerm (OR BoolExprTerm)*
    private void parseBoolExpr(BoolExprVar boolExpr) throws CloneNotSupportedException {
        BoolExprTermVar boolExprTerm = new BoolExprTermVar();
        boolExpr.addSymbol(boolExprTerm);
        parseBoolExprTerm(boolExprTerm);
        boolExpr.setStart(boolExprTerm.getStart());

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
        boolExprTerm.setStart(boolExprFactor.getStart());

        while (sym.getTag() == TokenTag.AND) {
            boolExprTerm.addSymbol(sym);
            parse(TokenTag.AND);

            BoolExprFactorVar boolExprFactorVar = new BoolExprFactorVar();
            boolExprTerm.addSymbol(boolExprFactorVar);
            parseBoolExprFactor(boolExprFactorVar);
            boolExprTerm.setFollow(boolExprFactorVar.getFollow());
        }
    }

    //BoolExprFactor       ::= BoolConst | NOT BoolExprFactor  | '(' BoolExpr ')'
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
        } else {
            BoolConstVar boolConst = new BoolConstVar();
            boolExprFactor.addSymbol(boolConst);
            parseBoolConst(boolConst);
            boolExprFactor.setCoords(boolConst.getCoords());
        }
    }

    //BoolConst            ::= ColId     | TRUE | FALSE | NULL | BoolStmt
    private void parseBoolConst(BoolConstVar boolConst) throws CloneNotSupportedException {
        //TODO UNSUPPORTED FIRST(ColId)
        if (sym.getTag() == TokenTag.IDENTIFIER) {
            boolConst.addSymbol(sym);
            boolConst.setCoords(sym.getCoords());
            parse(TokenTag.IDENTIFIER);
        } else if (sym.getTag() == TokenTag.TRUE) {
            boolConst.addSymbol(sym);
            boolConst.setCoords(sym.getCoords());
            parse(TokenTag.TRUE);
        } else if (sym.getTag() == TokenTag.FALSE) {
            boolConst.addSymbol(sym);
            boolConst.setCoords(sym.getCoords());
            parse(TokenTag.FALSE);
        } else {
            BoolStmtVar boolStmtVar = new BoolStmtVar();
            boolConst.addSymbol(boolStmtVar);
            parseBoolStmt(boolStmtVar);
            boolConst.setCoords(boolStmtVar.getCoords());
        }
    }
}
