package ru.bmstu.ORM.Compiler.Analyzer.Lexer;

import ru.bmstu.ORM.Compiler.Analyzer.Service.Position;
import ru.bmstu.ORM.Compiler.Analyzer.Symbols.Tokens.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Scanner {
    private Position cur;
    private ArrayList<Message> messages;
    private boolean isSearch;

    public Scanner(String program) {
        this.cur = new Position(program);
        this.messages = new ArrayList<>();
        this.isSearch = false;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public Token nextToken() throws CloneNotSupportedException {
        while (!cur.isEOF()) {
            while (cur.isWhiteSpace())
                cur.nextCp();

            Position start = (Position) cur.clone();
            StringBuilder value;

            switch (cur.getChar()) {
                case 'a':
                   value = new StringBuilder("a");
                   cur.nextCp();
                   if (cur.getChar() == 'n') {
                       value.append("n");
                       cur.nextCp();
                       if (cur.getChar() == 'd') {
                           value.append("d");
                           cur.nextCp();
                           if (cur.isWhiteSpace() || cur.isSpecial())
                               return new KeywordToken(start, (Position) cur.clone(), TokenTag.AND);
                           else
                               return getIdent(start, value);
                       }
                   } else if (cur.getChar() == 'c') {
                       value.append("c");
                       cur.nextCp();
                       if (cur.getChar() == 't') {
                           value.append("t");
                           cur.nextCp();
                           if (cur.getChar() == 'i') {
                               value.append("i");
                               cur.nextCp();
                               if (cur.getChar() == 'o') {
                                   value.append("o");
                                   cur.nextCp();
                                   if (cur.getChar() == 'n') {
                                       value.append("n");
                                       cur.nextCp();
                                       if (cur.isWhiteSpace() || cur.isSpecial())
                                           return new KeywordToken(start, (Position) cur.clone(), TokenTag.ACTION);
                                       else
                                           return getIdent(start, value);
                                   }
                               }
                           }
                       }
                   } else if (cur.getChar() == 'f') {
                       value.append("f");
                       cur.nextCp();
                       if (cur.getChar() == 't') {
                           value.append("t");
                           cur.nextCp();
                           if (cur.getChar() == 'e') {
                               value.append("e");
                               cur.nextCp();
                               if (cur.getChar() == 'r') {
                                   value.append('r');
                                   cur.nextCp();
                                   if (cur.isWhiteSpace() || cur.isSpecial())
                                       return new KeywordToken(start, (Position) cur.clone(), TokenTag.AFTER);
                                   else
                                       return getIdent(start, value);
                               }
                           }
                       }
                   } else if (cur.getChar() == 'l') {
                       value.append("l");
                       cur.nextCp();
                       if (cur.getChar() == 'l') {
                           value.append("l");
                           cur.nextCp();
                           if (cur.isWhiteSpace() || cur.isSpecial())
                               return new KeywordToken(start, (Position) cur.clone(), TokenTag.ALL);
                           else
                               return getIdent(start, value);
                       }
                   } else if (cur.getChar() == 'r') {
                       value.append("r");
                       cur.nextCp();
                       if (cur.getChar() == 'r') {
                           value.append("r");
                           cur.nextCp();
                           if (cur.getChar() == 'a') {
                               value.append('a');
                               cur.nextCp();
                               if (cur.getChar() == 'y') {
                                   value.append("y");
                                   cur.nextCp();
                                   if (cur.isWhiteSpace() || cur.isSpecial())
                                       return new KeywordToken(start, (Position) cur.clone(), TokenTag.ARRAY);
                                   else
                                       return getIdent(start, value);
                               }
                           }
                       }
                   } else if (cur.getChar() == 's') {
                       value.append("s");
                       cur.nextCp();
                       if (cur.getChar() == 'c') {
                           value.append("c");
                           cur.nextCp();
                           if (cur.isWhiteSpace() || cur.isSpecial())
                               return new KeywordToken(start, (Position) cur.clone(), TokenTag.ASC);
                           else
                               return getIdent(start, value);
                       } else if (cur.isWhiteSpace() || cur.isSpecial())
                           return new KeywordToken(start, (Position) cur.clone(), TokenTag.AS);
                       else
                           return getIdent(start, value);
                   } else if (cur.getChar() == 'v') {
                       value.append("v");
                       cur.nextCp();
                       if (cur.getChar() == 'g') {
                           value.append("g");
                           cur.nextCp();
                           if (cur.isWhiteSpace() || cur.isSpecial())
                               return new KeywordToken(start, (Position) cur.clone(), TokenTag.AVG);
                           else
                               return getIdent(start, value);
                       }
                   }

                   return getIdent(start, value);

                case 'b':
                    value = new StringBuilder("b");
                    cur.nextCp();
                    if (cur.getChar() == 'e') {
                        value.append("e");
                        cur.nextCp();
                        if (cur.getChar() == 'f') {
                            value.append("f");
                            cur.nextCp();
                            if (cur.getChar() == 'o') {
                                value.append("o");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.BEFORE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'g') {
                            value.append("g");
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value.append("i");
                                cur.nextCp();
                                if (cur.getChar() == 'n') {
                                    value.append("n");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.BEGIN);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        } else if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.getChar() == 'w') {
                                value.append("w");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value.append("n");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.BETWEEN);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'i') {
                        value.append("i");
                        cur.nextCp();
                        if (cur.getChar() == 'g') {
                            value.append("g");
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value.append("i");
                                cur.nextCp();
                                if (cur.getChar() == 'n') {
                                    value.append("n");
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value.append("t");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.BIGINT);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append('l');
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value.append("a");
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value.append("n");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.BOOLEAN);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'y') {
                        value.append("y");
                        cur.nextCp();
                        if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.BY);
                        else
                            return getIdent(start, value);
                    }

                    return getIdent(start, value);

                case 'c':
                    value = new StringBuilder("c");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value.append("a");
                                    cur.nextCp();
                                    if (cur.getChar() == 'd') {
                                        value.append("d");
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value.append("e");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.CASCADE);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'h') {
                        value.append("h");
                        cur.nextCp();
                        if (cur.getChar() == 'a') {
                            value.append("a");
                            cur.nextCp();
                            if (cur.getChar() == 'r') {
                                value.append("r");
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value.append("a");
                                    cur.nextCp();
                                    if (cur.getChar() == 'c') {
                                        value.append("c");
                                        cur.nextCp();
                                        if (cur.getChar() == 't') {
                                            value.append("t");
                                            cur.nextCp();
                                            if (cur.getChar() == 'e') {
                                                value.append('e');
                                                cur.nextCp();
                                                if (cur.getChar() == 'r') {
                                                    value.append('r');
                                                    cur.nextCp();
                                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.CHARACTER);
                                                    else
                                                        return getIdent(start, value);
                                                }
                                            }
                                        }
                                    }
                                } else if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.CHAR);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 'k') {
                                    value.append("k");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.CHECK);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'n') {
                            value.append("n");
                            cur.nextCp();
                            if (cur.getChar() == 't') {
                                value.append("t");
                                cur.nextCp();
                                if (cur.getChar() == 'i') {
                                    value.append("i");
                                    cur.nextCp();
                                    if (cur.getChar() == 'n') {
                                        value.append("n");
                                        cur.nextCp();
                                        if (cur.getChar() == 'u') {
                                            value.append("u");
                                            cur.nextCp();
                                            if (cur.getChar() == 'e') {
                                                value.append("e");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.CONTINUE);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            } else if (cur.getChar() == 's') {
                                value.append("s");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.getChar() == 'r') {
                                        value.append("r");
                                        cur.nextCp();
                                        if (cur.getChar() == 'a') {
                                            value.append("a");
                                            cur.nextCp();
                                            if (cur.getChar() == 'i') {
                                                value.append("i");
                                                cur.nextCp();
                                                if (cur.getChar() == 'n') {
                                                    value.append("n");
                                                    cur.nextCp();
                                                    if (cur.getChar() == 't') {
                                                        value.append("t");
                                                        cur.nextCp();
                                                        if (cur.isWhiteSpace() || cur.isSpecial()) {
                                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.CONSTRAINT);
                                                        } else
                                                            return getIdent(start, value);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'u') {
                            value.append("u");
                            cur.nextCp();
                            if (cur.getChar() == 'n') {
                                value.append("n");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.COUNT);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'r') {
                        value.append("r");
                        cur.nextCp();
                        if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value.append("a");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.CREATE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value.append("s");
                                cur.nextCp();
                                if (cur.getChar() == 's') {
                                    value.append("s");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.CROSS);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'd':
                    value = new StringBuilder("d");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.DATE);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'e') {
                        value.append("e");
                        cur.nextCp();
                        if (cur.getChar() == 'c') {
                            value.append("c");
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value.append("i");
                                cur.nextCp();
                                if (cur.getChar() == 'm') {
                                    value.append("m");
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value.append("a");
                                        cur.nextCp();
                                        if (cur.getChar() == 'l') {
                                            value.append("l");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.DECIMAL);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            } else if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value.append("a");
                                    cur.nextCp();
                                    if (cur.getChar() == 'r') {
                                        value.append("r");
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value.append("e");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.DECLARE);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'f') {
                            value.append("f");
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value.append("a");
                                cur.nextCp();
                                if (cur.getChar() == 'u') {
                                    value.append("u");
                                    cur.nextCp();
                                    if (cur.getChar() == 'l') {
                                        value.append("l");
                                        cur.nextCp();
                                        if (cur.getChar() == 't') {
                                            value.append("t");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.DEFAULT);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.DELETE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.DESC);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'i') {
                        value.append("i");
                        cur.nextCp();
                        if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 't') {
                                value.append("t");
                                cur.nextCp();
                                if (cur.getChar() == 'i') {
                                    value.append("i");
                                    cur.nextCp();
                                    if (cur.getChar() == 'n') {
                                        value.append("n");
                                        cur.nextCp();
                                        if (cur.getChar() == 'c') {
                                            value.append("c");
                                            cur.nextCp();
                                            if (cur.getChar() == 't') {
                                                value.append("t");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.DISTINCT);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'u') {
                            value.append("u");
                            cur.nextCp();
                            if (cur.getChar() == 'b') {
                                value.append("b");
                                cur.nextCp();
                                if (cur.getChar() == 'l') {
                                    value.append("l");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.DOUBLE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'e':
                    value = new StringBuilder("e");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'c') {
                            value.append("c");
                            cur.nextCp();
                            if (cur.getChar() == 'h') {
                                value.append("h");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.EACH);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'x') {
                        value.append("x");
                        cur.nextCp();
                        if (cur.getChar() == 'c') {
                            value.append("c");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'p') {
                                    value.append("p");
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value.append("t");
                                        cur.nextCp();
                                        if (cur.getChar() == 'i') {
                                            value.append("i");
                                            cur.nextCp();
                                            if (cur.getChar() == 'o') {
                                                value.append("o");
                                                cur.nextCp();
                                                if (cur.getChar() == 'n') {
                                                    value.append("n");
                                                    cur.nextCp();
                                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.EXCEPTION);
                                                    else
                                                        return getIdent(start, value);
                                                }
                                            }
                                        } else if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.EXCEPT);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 'u') {
                                    value.append("u");
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value.append("t");
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value.append("e");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.EXECUTE);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value.append("s");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.getChar() == 's') {
                                        value.append("s");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.EXISTS);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            } else if (cur.getChar() == 't') {
                                value.append("t");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.EXIT);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'l') {
                        value.append("l");
                        cur.nextCp();
                        if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.ELSE);
                                else
                                    return getIdent(start, value);
                            } else if (cur.getChar() == 'i') {
                                value.append("i");
                                cur.nextCp();
                                if (cur.getChar() == 'f') {
                                    value.append("f");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.ELSIF);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'n') {
                        value.append("n");
                        cur.nextCp();
                        if (cur.getChar() == 'd') {
                            value.append("d");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.END);
                            else
                                return getIdent(start, value);
                        }
                    }

                    return getIdent(start, value);

                case 'f':
                    value = new StringBuilder("f");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value.append("s");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new BoolToken(start, (Position) cur.clone(), false, TokenTag.FALSE);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'l') {
                        value.append("l");
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value.append("a");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append('t');
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.FLOAT);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'r') {
                            value.append("r");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'i') {
                                    value.append("i");
                                    cur.nextCp();
                                    if (cur.getChar() == 'g') {
                                        value.append("g");
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value.append("n");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.FOREIGN);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'r') {
                        value.append("r");
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 'm') {
                                value.append("m");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.FROM);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'u') {
                        value.append("u");
                        cur.nextCp();
                        if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.FULL);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.getChar() == 'n') {
                            value.append("n");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.getChar() == 'i') {
                                        value.append("i");
                                        cur.nextCp();
                                        if (cur.getChar() == 'o') {
                                            value.append("o");
                                            cur.nextCp();
                                            if (cur.getChar() == 'n') {
                                                value.append("n");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.FUNCTION);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'g':
                    value = new StringBuilder("g");
                    cur.nextCp();
                    if (cur.getChar() == 'r') {
                        value.append("r");
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value.append('o');
                            cur.nextCp();
                            if (cur.getChar() == 'u') {
                                value.append("u");
                                cur.nextCp();
                                if (cur.getChar() == 'p') {
                                    value.append("p");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.GROUP);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'h':
                    value = new StringBuilder("h");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'v') {
                            value.append("v");
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value.append("i");
                                cur.nextCp();
                                if (cur.getChar() == 'n') {
                                    value.append("n");
                                    cur.nextCp();
                                    if (cur.getChar() == 'g') {
                                        value.append("g");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.HAVING);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'i':
                    value = new StringBuilder("i");
                    cur.nextCp();
                    if (cur.getChar() == 'f') {
                        value.append("f");
                        cur.nextCp();
                        if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.IF);
                        else
                            return getIdent(start, value);
                    } else if (cur.getChar() == 'n') {
                        value.append("n");
                        cur.nextCp();
                        if (cur.getChar() == 'h') {
                            value.append("h");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'i') {
                                        value.append("i");
                                        cur.nextCp();
                                        if (cur.getChar() == 't') {
                                            value.append("t");
                                            cur.nextCp();
                                            if (cur.getChar() == 's') {
                                                value.append("s");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.INHERITS);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'n') {
                            value.append("n");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.INNER);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        } else if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value.append("t");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.INSERT);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'g') {
                                    value.append("g");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.getChar() == 'r') {
                                            value.append("r");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.INTEGER);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                } else if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 's') {
                                        value.append("s");
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value.append("e");
                                            cur.nextCp();
                                            if (cur.getChar() == 'c') {
                                                value.append("c");
                                                cur.nextCp();
                                                if (cur.getChar() == 't') {
                                                    value.append("t");
                                                    cur.nextCp();
                                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.INTERSECT);
                                                    else
                                                        return getIdent(start, value);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (cur.getChar() == 'o') {
                                value.append("o");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.INTO);
                                else
                                    return getIdent(start, value);
                            } else if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.INT);
                            else
                                return getIdent(start, value);
                        } else if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 't') {
                                value.append("t");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value.append("a");
                                        cur.nextCp();
                                        if (cur.getChar() == 'd') {
                                            value.append("d");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.INSTEAD);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 'u') {
                                value.append("u");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.INOUT);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        } else if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.IN);
                        else
                            return getIdent(start, value);
                    } else if (cur.getChar() == 's') {
                        value.append("s");
                        cur.nextCp();
                        if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.IS);
                        else
                            return getIdent(start, value);
                    }

                    return getIdent(start, value);

                case 'j':
                    value = new StringBuilder("j");
                    cur.nextCp();
                    if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'n') {
                                value.append("n");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.JOIN);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'k':
                    value = new StringBuilder("k");
                    cur.nextCp();
                    if (cur.getChar() == 'e') {
                        value.append("e");
                        cur.nextCp();
                        if (cur.getChar() == 'y') {
                            value.append("y");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.KEY);
                            else
                                return getIdent(start, value);
                        }
                    }

                    return getIdent(start, value);

                case 'l':
                    value = new StringBuilder("l");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'n') {
                            value.append("n");
                            cur.nextCp();
                            if (cur.getChar() == 'g') {
                                value.append("g");
                                cur.nextCp();
                                if (cur.getChar() == 'u') {
                                    value.append("u");
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value.append("a");
                                        cur.nextCp();
                                        if (cur.getChar() == 'g') {
                                            value.append("g");
                                            cur.nextCp();
                                            if (cur.getChar() == 'e') {
                                                value.append("e");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.LANGUAGE);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'e') {
                        value.append("e");
                        cur.nextCp();
                        if (cur.getChar() == 'f') {
                            value.append("f");
                            cur.nextCp();
                            if (cur.getChar() == 't') {
                                value.append("t");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.LEFT);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'i') {
                        value.append("i");
                        cur.nextCp();
                        if (cur.getChar() == 'k') {
                            value.append("k");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.LIKE);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 'p') {
                                value.append("p");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.LOOP);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'm':
                    value = new StringBuilder("m");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'x') {
                            value.append("x");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.MAX);
                            else
                                return getIdent(start, value);
                        }
                    } else if (cur.getChar() == 'i') {
                        value.append("i");
                        cur.nextCp();
                        if (cur.getChar() == 'n') {
                            value.append("n");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.MIN);
                            else
                                return getIdent(start, value);
                        }
                    }

                    return getIdent(start, value);

                case 'n':
                    value = new StringBuilder("n");
                    cur.nextCp();
                    if (cur.getChar() == 'o') {
                        value.append('o');
                        cur.nextCp();
                        if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value.append("i");
                                cur.nextCp();
                                if (cur.getChar() == 'c') {
                                    value.append("c");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.NOTICE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            } else if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.NOT);
                            else
                                return getIdent(start, value);
                        } else if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.NO);
                        else
                            return getIdent(start, value);
                    } else if (cur.getChar() == 'u') {
                        value.append('u');
                        cur.nextCp();
                        if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new BoolToken(start, (Position) cur.clone(), null, TokenTag.NULL);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.getChar() == 'm') {
                            value.append("m");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'i') {
                                        value.append("i");
                                        cur.nextCp();
                                        if (cur.getChar() == 'c') {
                                            value.append("c");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.NUMERIC);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'o':
                    value = new StringBuilder("o");
                    cur.nextCp();
                    if (cur.getChar() == 'n') {
                        value.append("n");
                        cur.nextCp();
                        if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 'y') {
                                value.append("y");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.ONLY);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.ON);
                        else
                            return getIdent(start, value);
                    } else if (cur.getChar() == 'r') {
                        value.append("r");
                        cur.nextCp();
                        if (cur.getChar() == 'd') {
                            value.append("d");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.ORDER);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        } else if (cur.isWhiteSpace() || cur.isSpecial())
                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.OR);
                        else
                            return getIdent(start, value);
                    } else if (cur.getChar() == 'u') {
                        value.append("u");
                        cur.nextCp();
                        if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.OUTER);
                                    else
                                        return getIdent(start, value);
                                }
                            } else if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.OUT);
                            else
                                return getIdent(start, value);
                        }
                    }

                    return getIdent(start, value);

                case 'p':
                    value = new StringBuilder("p");
                    cur.nextCp();
                    if (cur.getChar() == 'l') {
                        value.append("l");
                        cur.nextCp();
                        if (cur.getChar() == 'p') {
                            value.append("p");
                            cur.nextCp();
                            if (cur.getChar() == 'g') {
                                value.append("g");
                                cur.nextCp();
                                if (cur.getChar() == 's') {
                                    value.append("s");
                                    cur.nextCp();
                                    if (cur.getChar() == 'q') {
                                        value.append("q");
                                        cur.nextCp();
                                        if (cur.getChar() == 'l') {
                                            value.append("l");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.PLPGSQL);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'r') {
                        value.append("r");
                        cur.nextCp();
                        if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 'i') {
                                    value.append("i");
                                    cur.nextCp();
                                    if (cur.getChar() == 's') {
                                        value.append("s");
                                        cur.nextCp();
                                        if (cur.getChar() == 'i') {
                                            value.append("i");
                                            cur.nextCp();
                                            if (cur.getChar() == 'o') {
                                                value.append("o");
                                                cur.nextCp();
                                                if (cur.getChar() == 'n') {
                                                    value.append("n");
                                                    cur.nextCp();
                                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.PRECISION);
                                                    else
                                                        return getIdent(start, value);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'm') {
                                value.append("m");
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value.append("a");
                                    cur.nextCp();
                                    if (cur.getChar() == 'r') {
                                        value.append("r");
                                        cur.nextCp();
                                        if (cur.getChar() == 'y') {
                                            value.append("y");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.PRIMARY);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'o') {
                            value.append("o");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.getChar() == 'd') {
                                        value.append("d");
                                        cur.nextCp();
                                        if (cur.getChar() == 'u') {
                                            value.append("u");
                                            cur.nextCp();
                                            if (cur.getChar() == 'r') {
                                                value.append("r");
                                                cur.nextCp();
                                                if (cur.getChar() == 'e') {
                                                    value.append("e");
                                                    cur.nextCp();
                                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.PROCEDURE);
                                                    else
                                                        return getIdent(start, value);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'q':
                    value = new StringBuilder("q");
                    cur.nextCp();
                    if (cur.getChar() == 'u') {
                        value.append("u");
                        cur.nextCp();
                        if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'r') {
                                value.append("r");
                                cur.nextCp();
                                if (cur.getChar() == 'y') {
                                    value.append("y");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.QUERY);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'r':
                    value = new StringBuilder("r");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value.append("s");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.RAISE);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'e') {
                        value.append("e");
                        cur.nextCp();
                        if (cur.getChar() == 'a') {
                            value.append("a");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.REAL);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.getChar() == 'c') {
                            value.append("c");
                            cur.nextCp();
                            if (cur.getChar() == 'o') {
                                value.append("o");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'd') {
                                        value.append("d");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.RECORD);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'f') {
                            value.append("f");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value.append("n");
                                            cur.nextCp();
                                            if (cur.getChar() == 'c') {
                                                value.append("c");
                                                cur.nextCp();
                                                if (cur.getChar() == 'e') {
                                                    value.append("e");
                                                    cur.nextCp();
                                                    if (cur.getChar() == 's') {
                                                        value.append("s");
                                                        cur.nextCp();
                                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.REFERENCES);
                                                        else
                                                            return getIdent(start, value);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'p') {
                            value.append("p");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value.append('a');
                                    cur.nextCp();
                                    if (cur.getChar() == 'c') {
                                        value.append("c");
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value.append("e");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.REPLACE);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 's') {
                            value.append("s");
                            cur.nextCp();
                            if (cur.getChar() == 't') {
                                value.append("t");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'i') {
                                        value.append("i");
                                        cur.nextCp();
                                        if (cur.getChar() == 'c') {
                                            value.append("c");
                                            cur.nextCp();
                                            if (cur.getChar() == 't') {
                                                value.append("t");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.RESTRICT);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.getChar() == 'u') {
                                value.append("u");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 'n') {
                                        value.append("n");
                                        cur.nextCp();
                                        if (cur.getChar() == 's') {
                                            value.append("s");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.RETURNS);
                                            else
                                                return getIdent(start, value);
                                        } else if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.RETURN);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'v') {
                            value.append("v");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'r') {
                                    value.append("r");
                                    cur.nextCp();
                                    if (cur.getChar() == 's') {
                                        value.append("s");
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value.append("e");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.REVERSE);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'i') {
                        value.append("i");
                        cur.nextCp();
                        if (cur.getChar() == 'g') {
                            value.append("g");
                            cur.nextCp();
                            if (cur.getChar() == 'h') {
                                value.append("h");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.RIGHT);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'w') {
                            value.append("w");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.ROW);
                            else
                                return getIdent(start, value);
                        }
                    }

                    return getIdent(start, value);

                case 's':
                    value = new StringBuilder("s");
                    cur.nextCp();
                    if (cur.getChar() == 'e') {
                        value.append("e");
                        cur.nextCp();
                        if (cur.getChar() == 't') {
                            value.append("t");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.SET);
                            else
                                return getIdent(start, value);
                        } else if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 'c') {
                                    value.append("c");
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value.append("t");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.SELECT);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'm') {
                        value.append("m");
                        cur.nextCp();
                        if (cur.getChar() == 'a') {
                            value.append("a");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.getChar() == 'l') {
                                    value.append("l");
                                    cur.nextCp();
                                    if (cur.getChar() == 'i') {
                                        value.append("i");
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value.append("n");
                                            cur.nextCp();
                                            if (cur.getChar() == 't') {
                                                value.append("t");
                                                cur.nextCp();
                                                if (cur.isWhiteSpace() || cur.isSpecial())
                                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.SMALLINT);
                                                else
                                                    return getIdent(start, value);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'u') {
                        value.append("u");
                        cur.nextCp();
                        if (cur.getChar() == 'm') {
                            value.append("m");
                            cur.nextCp();
                            if (cur.isWhiteSpace() || cur.isSpecial())
                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.SUM);
                            else
                                return getIdent(start, value);
                        }
                    }

                    return getIdent(start, value);

                case 't':
                    value = new StringBuilder("t");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'b') {
                            value.append("b");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.TABLE);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'h') {
                        value.append("h");
                        cur.nextCp();
                        if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'n') {
                                value.append("n");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.THEN);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'i') {
                        value.append("i");
                        cur.nextCp();
                        if (cur.getChar() == 'm') {
                            value.append("m");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.getChar() == 's') {
                                    value.append("s");
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value.append("t");
                                        cur.nextCp();
                                        if (cur.getChar() == 'a') {
                                            value.append("a");
                                            cur.nextCp();
                                            if (cur.getChar() == 'm') {
                                                value.append("m");
                                                cur.nextCp();
                                                if (cur.getChar() == 'p') {
                                                    value.append("p");
                                                    cur.nextCp();
                                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.TIMESTAMP);
                                                    else
                                                        return getIdent(start, value);
                                                }
                                            }
                                        }
                                    }
                                } else if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.TIME);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'r') {
                        value.append("r");
                        cur.nextCp();
                        if (cur.getChar() == 'u') {
                            value.append("u");
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value.append("e");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new BoolToken(start, (Position) cur.clone(), true, TokenTag.TRUE);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'g') {
                                value.append("g");
                                cur.nextCp();
                                if (cur.getChar() == 'g') {
                                    value.append("g");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.getChar() == 'r') {
                                            value.append("r");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.TRIGGER);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'u':
                    value = new StringBuilder("u");
                    cur.nextCp();
                    if (cur.getChar() == 'n') {
                        value.append("n");
                        cur.nextCp();
                        if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'o') {
                                value.append("o");
                                cur.nextCp();
                                if (cur.getChar() == 'n') {
                                    value.append("n");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.UNION);
                                    else
                                        return getIdent(start, value);
                                }
                            } else if (cur.getChar() == 'q') {
                                value.append("q");
                                cur.nextCp();
                                if (cur.getChar() == 'u') {
                                    value.append("u");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.UNIQUE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'p') {
                        value.append("p");
                        cur.nextCp();
                        if (cur.getChar() == 'd') {
                            value.append("d");
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value.append("a");
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value.append("t");
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value.append("e");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.UPDATE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 's') {
                        value.append("s");
                        cur.nextCp();
                        if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'n') {
                                value.append("n");
                                cur.nextCp();
                                if (cur.getChar() == 'g') {
                                    value.append("g");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.USING);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'v':
                    value = new StringBuilder("v");
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value.append("a");
                        cur.nextCp();
                        if (cur.getChar() == 'r') {
                            value.append("r");
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value.append("c");
                                cur.nextCp();
                                if (cur.getChar() == 'h') {
                                    value.append("h");
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value.append("a");
                                        cur.nextCp();
                                        if (cur.getChar() == 'r') {
                                            value.append("r");
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.VARCHAR);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'l') {
                            value.append("l");
                            cur.nextCp();
                            if (cur.getChar() == 'u') {
                                value.append("u");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.getChar() == 's') {
                                        value.append("s");
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.VALUES);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value.append("o");
                        cur.nextCp();
                        if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'd') {
                                value.append("d");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.VOID);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'w':
                    value = new StringBuilder("w");
                    cur.nextCp();

                    if (cur.getChar() == 'h') {
                        value.append("h");
                        cur.nextCp();
                        if (cur.getChar() == 'e') {
                            value.append("e");
                            cur.nextCp();
                            if (cur.getChar() == 'r') {
                                value.append("r");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.WHERE);
                                    else
                                        return getIdent(start, value);
                                }
                            } else if (cur.getChar() == 'n') {
                                value.append("n");
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.WHEN);
                                else
                                    return getIdent(start, value);
                            }
                        } else if (cur.getChar() == 'i') {
                            value.append("i");
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value.append("l");
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value.append("e");
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.WHILE);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case '(':
                    cur.nextCp();

                    return new SpecToken(TokenTag.LPAREN, start, (Position) cur.clone(), "(");
                case ')':
                    cur.nextCp();

                    return new SpecToken(TokenTag.RPAREN, start, (Position) cur.clone(), ")");
                case '[':
                    cur.nextCp();

                    return new SpecToken(TokenTag.LBRACKET, start, (Position) cur.clone(), "[");
                case ']':
                    cur.nextCp();

                    return new SpecToken(TokenTag.RBRACKET, start, (Position) cur.clone(), "]");
                case '{':
                    cur.nextCp();

                    return new SpecToken(TokenTag.LBRACE, start, (Position) cur.clone(), "{");
                case '}':
                    cur.nextCp();

                    return new SpecToken(TokenTag.RBRACE, start, (Position) cur.clone(), "}");
                case '<':
                    cur.nextCp();
                    if (cur.getChar() == '=') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.LESSEQ, start, (Position) cur.clone(), "<=");
                    } else

                        return new SpecToken(TokenTag.LESS, start, (Position) cur.clone(), "<");
                case '>':
                    cur.nextCp();
                    if (cur.getChar() == '=') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.GREATEREQ, start, (Position) cur.clone(), ">=");
                    } else

                        return new SpecToken(TokenTag.GREATER, start, (Position) cur.clone(), ">");
                case '=':
                    cur.nextCp();

                    return new SpecToken(TokenTag.EQUAL, start, (Position) cur.clone(), "=");
                case '!':
                    cur.nextCp();
                    if (cur.getChar() == '=') {
                        cur.nextCp();
                    } else {
                        error("'=' expected");
                    }

                    return new SpecToken(TokenTag.NOTEQUAL, start, (Position) cur.clone(), "!=");
                case '+':
                    cur.nextCp();

                    return new SpecToken(TokenTag.ADD, start, (Position) cur.clone(), "+");
                case '-':
                    cur.nextCp();
                    if (cur.getChar() == '-') {
                        while (cur.getChar() != '\n' && cur.getChar() != '\r')
                            cur.nextCp();

                        continue;
                    }

                    return new SpecToken(TokenTag.SUB, start, (Position) cur.clone(), "-");
                case '*':
                    cur.nextCp();

                    return new SpecToken(TokenTag.MUL, start, (Position) cur.clone(), "*");
                case '/':
                    cur.nextCp();

                    return new SpecToken(TokenTag.DIV, start, (Position) cur.clone(), "/");
                case '\'':
                    value = new StringBuilder();
                    cur.nextCp();
                    while (cur.getChar() != '\'' && cur.getChar() != (char) 0xFFFFFFFF) {
                        if (cur.getChar() == '\n' || cur.getChar() == '\r')
                            error("String must be in one line");
                        else
                            value.append(cur.getChar());

                        cur.nextCp();
                    }

                    cur.nextCp();

                    try {
                        DateFormat dfISO = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
                        Timestamp date = new Timestamp(dfISO.parse(value.toString()).getTime());
                        return new DateTimeToken(TokenTag.TIMESTAMP_CONST, start, (Position) cur.clone(), date);
                    } catch (ParseException e1) {
                        try {
                            DateFormat dateOnlyISO = new SimpleDateFormat("dd-MM-yyyy");
                            Date date = new Date(dateOnlyISO.parse(value.toString()).getTime());
                            return new DateTimeToken(TokenTag.DATE_CONST, start, (Position) cur.clone(), date);
                        } catch (ParseException e2) {
                            try {
                                DateFormat timeOnlyISO = new SimpleDateFormat("HH:mm:ss");
                                Time date = new Time(timeOnlyISO.parse(value.toString()).getTime());
                                return new DateTimeToken(TokenTag.TIME_CONST, start, (Position) cur.clone(), date);
                            } catch (ParseException e3) {
                                return new StringToken(start, (Position) cur.clone(), value.toString());
                            }
                        }
                    }
                case '"':
                    value = new StringBuilder().append('"');
                    cur.nextCp();
                    while (cur.getChar() != '"' && cur.getChar() != (char) 0xFFFFFFFF) {
                        if (cur.getChar() == '\n' || cur.getChar() == '\r')
                            error("Identifier can't contain new line symbols");
                        else
                            value.append(cur.getChar());

                        cur.nextCp();
                    }
                    value.append('"');
                    cur.nextCp();

                    return new IdentToken(start, (Position) cur.clone(), value.toString());
                case ',':
                    cur.nextCp();

                    return new SpecToken(TokenTag.COMMA, start, (Position) cur.clone(), ",");
                case '.':
                    cur.nextCp();
                    if (cur.getChar() == '.') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.DOUBLE_DOT, start, (Position) cur.clone(), "..");
                    }

                    return new SpecToken(TokenTag.DOT, start, (Position) cur.clone(), ".");
                case ':':
                    cur.nextCp();

                    if (cur.getChar() == '=') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.ASSIGN, start, (Position) cur.clone(), ":=");
                    } else if (cur.getChar() == ':') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.DOUBLE_COLON, start, (Position) cur.clone(), "::");
                    }

                    error("Unrecognizable operator");
                    break;
                case ';':
                    cur.nextCp();

                    return new SpecToken(TokenTag.SEMICOLON, start, (Position) cur.clone(), ";");
                case '$':
                    cur.nextCp();
                    if (cur.getChar() == '$') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.DOUBLE_DOLLAR, start, (Position) cur.clone(), "$$");
                    }

                    error("Unrecognizable operator");
                    break;
                case '|':
                    cur.nextCp();
                    if (cur.getChar() == '|') {
                        cur.nextCp();

                        return new SpecToken(TokenTag.DOUBLE_VERTICAL_SLASH, start, (Position) cur.clone(), "||");
                    }

                    return new SpecToken(TokenTag.VERTICAL_SLASH, start, (Position) cur.clone(), "|");
                default:
                   if (cur.isLetter())
                       return getIdent(start, new StringBuilder());
                   else if (cur.isDigit()) {
                       Token number = getNumber(start);
                       if (number != null)
                           return number;
                       else {
                           error("Unrecognizable number");
                           cur.nextCp();
                           break;
                       }
                   }
                   else {
                       error("Unrecognizable token");
                       cur.nextCp();
                       break;
                   }
            }
        }

        return new EOFToken(cur);
    }

    private IdentToken getIdent(Position start, StringBuilder value) throws CloneNotSupportedException {
        while (cur.isLetterOrDigit() || cur.getChar() == '_') {
            value.append(cur.getChar());
            cur.nextCp();
        }

        return new IdentToken(start, (Position) cur.clone(), value.toString());
    }

    private NumberToken getNumber(Position start) throws CloneNotSupportedException {
        StringBuilder value = new StringBuilder();
        boolean wasComma = false;
        while (cur.isDigit() || cur.getChar() == '.') {
            if (cur.getChar() == '.') {
                if (wasComma)
                    error("Two dots in float number");
                else
                    wasComma = true;
            }

            value.append(cur.getChar());
            cur.nextCp();
        }

        if (wasComma) {
            try {
                Float number = Float.parseFloat(value.toString());
                return new NumberToken(TokenTag.FLOAT_CONST, start, (Position) cur.clone(), number);
            } catch (NumberFormatException ef) {
                try {
                    Double number = Double.parseDouble(value.toString());
                    return new NumberToken(TokenTag.DOUBLE_CONST, start, (Position) cur.clone(), number);
                } catch (NumberFormatException ed) {
                    error("Wrong number");
                    return null;
                }
            }
        } else {
            try {
                Byte number = Byte.parseByte(value.toString());
                return new NumberToken(TokenTag.BYTE_CONST, start, (Position) cur.clone(), number);
            } catch (NumberFormatException eb) {
                try {
                    Short number = Short.parseShort(value.toString());
                    return new NumberToken(TokenTag.SHORT_CONST, start, (Position) cur.clone(), number);
                } catch (NumberFormatException es) {
                    try {
                        Integer number = Integer.parseInt(value.toString());
                        return new NumberToken(TokenTag.INT_CONST, start, (Position) cur.clone(), number);
                    } catch (NumberFormatException ei) {
                        try {
                            Long number = Long.parseLong(value.toString());
                            return new NumberToken(TokenTag.LONG_CONST, start, (Position) cur.clone(), number);
                        } catch (NumberFormatException el) {
                            error("Wrong number");
                            return null;
                        }
                    }
                }
            }
        }
    }

    private void error(String msg) throws CloneNotSupportedException {
        if (!isSearch)
            messages.add(new Message((Position) cur.clone(), msg));
    }

    public Token searchToken(String sym) throws CloneNotSupportedException {
        isSearch = true;
        Token token = nextToken();
        while (!token.getStringValue().equals(sym)) {
            token = nextToken();
            if (token.getTag() == TokenTag.END_OF_PROGRAM)
                throw new RuntimeException("No token " + sym + " was found");
        }

        isSearch = false;
        return token;
    }
}
