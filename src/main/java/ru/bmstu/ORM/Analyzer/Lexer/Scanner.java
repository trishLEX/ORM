package ru.bmstu.ORM.Analyzer.Lexer;

import ru.bmstu.ORM.Analyzer.Service.Position;
import ru.bmstu.ORM.Analyzer.Symbols.Tokens.*;

import java.util.ArrayList;

public class Scanner {
    private Position cur;
    private ArrayList<Message> messages;

    public Scanner(String program) {
        this.cur = new Position(program);
        this.messages = new ArrayList<>();
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public Token nextToken() throws CloneNotSupportedException {
        while (!cur.isEOF()) {
            while (cur.isWhiteSpace())
                cur.nextCp();

            Position start = (Position) cur.clone();
            String value;

            switch (cur.getChar()) {
                case 'a':
                   value = "a";
                   cur.nextCp();
                   if (cur.getChar() == 'n') {
                       value += "n";
                       cur.nextCp();
                       if (cur.getChar() == 'd') {
                           value += "d";
                           cur.nextCp();
                           if (cur.isWhiteSpace() || cur.isSpecial())
                               return new KeywordToken(start, (Position) cur.clone(), TokenTag.AND);
                           else
                               return getIdent(start, value);
                       }
                   } else if (cur.getChar() == 'c') {
                       value += "c";
                       cur.nextCp();
                       if (cur.getChar() == 't') {
                           value += "t";
                           cur.nextCp();
                           if (cur.getChar() == 'i') {
                               value += "i";
                               cur.nextCp();
                               if (cur.getChar() == 'o') {
                                   value += "o";
                                   cur.nextCp();
                                   if (cur.getChar() == 'n') {
                                       value += "n";
                                       cur.nextCp();
                                       if (cur.isWhiteSpace() || cur.isSpecial())
                                           return new KeywordToken(start, (Position) cur.clone(), TokenTag.ACTION);
                                       else
                                           return getIdent(start, value);
                                   }
                               }
                           }
                       }
                   }

                   return getIdent(start, value);

                case 'b':
                    value = "b";
                    cur.nextCp();
                    if (cur.getChar() == 'e') {
                        value += "e";
                        cur.nextCp();
                        if (cur.getChar() == 't') {
                            value += "t";
                            cur.nextCp();
                            if (cur.getChar() == 'w') {
                                value += "w";
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value += "e";
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value += "e";
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value += "n";
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
                        value += "i";
                        cur.nextCp();
                        if (cur.getChar() == 'g') {
                            value += "g";
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value += "i";
                                cur.nextCp();
                                if (cur.getChar() == 'n') {
                                    value += "n";
                                    cur.nextCp();
                                    if (cur.getChar() == 't') {
                                        value += "t";
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
                        value += "o";
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value += "o";
                            cur.nextCp();
                            if (cur.getChar() == 'l') {
                                value += 'l';
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value += "e";
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value += "a";
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value += "n";
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
                    }

                    return getIdent(start, value);

                case 'c':
                    value = "c";
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value += "a";
                        cur.nextCp();
                        if (cur.getChar() == 's') {
                            value += "s";
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value += "c";
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value += "a";
                                    cur.nextCp();
                                    if (cur.getChar() == 'd') {
                                        value += "d";
                                        cur.nextCp();
                                        if (cur.getChar() == 'e') {
                                            value += "e";
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
                        value += "h";
                        cur.nextCp();
                        if (cur.getChar() == 'a') {
                            value += "a";
                            cur.nextCp();
                            if (cur.getChar() == 'r') {
                                value += "r";
                                cur.nextCp();
                                if (cur.getChar() == 'a') {
                                    value += "a";
                                    cur.nextCp();
                                    if (cur.getChar() == 'c') {
                                        value += "c";
                                        cur.nextCp();
                                        if (cur.getChar() == 't') {
                                            value += "t";
                                            cur.nextCp();
                                            if (cur.getChar() == 'e') {
                                                value += 'e';
                                                cur.nextCp();
                                                if (cur.getChar() == 'r') {
                                                    value += 'r';
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
                            value += "e";
                            cur.nextCp();
                            if (cur.getChar() == 'c') {
                                value += "c";
                                cur.nextCp();
                                if (cur.getChar() == 'k') {
                                    value += "k";
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.CHECK);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value += "o";
                        cur.nextCp();
                        if (cur.getChar() == 'n') {
                            value += "n";
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value += "s";
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value += "t";
                                    cur.nextCp();
                                    if (cur.getChar() == 'r') {
                                        value += "r";
                                        cur.nextCp();
                                        if (cur.getChar() == 'a') {
                                            value += "a";
                                            cur.nextCp();
                                            if (cur.getChar() == 'i') {
                                                value += "i";
                                                cur.nextCp();
                                                if (cur.getChar() == 'n') {
                                                    value += "n";
                                                    cur.nextCp();
                                                    if (cur.getChar() == 't') {
                                                        value += "t";
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
                        }
                    } else if (cur.getChar() == 'r') {
                        value += "r";
                        cur.nextCp();
                        if (cur.getChar() == 'e') {
                            value += "e";
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value += "a";
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value += "t";
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value += "e";
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.CREATE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'd':
                    value = "d";
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value += "a";
                        cur.nextCp();
                        if (cur.getChar() == 't') {
                            value += "t";
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value += "e";
                                cur.nextCp();
                                if (cur.isWhiteSpace() || cur.isSpecial())
                                    return new KeywordToken(start, (Position) cur.clone(), TokenTag.DATE);
                                else
                                    return getIdent(start, value);
                            }
                        }
                    } else if (cur.getChar() == 'e') {
                        value += "e";
                        cur.nextCp();
                        if (cur.getChar() == 'c') {
                            value += "c";
                            cur.nextCp();
                            if (cur.getChar() == 'i') {
                                value += "i";
                                cur.nextCp();
                                if (cur.getChar() == 'm') {
                                    value += "m";
                                    cur.nextCp();
                                    if (cur.getChar() == 'a') {
                                        value += "a";
                                        cur.nextCp();
                                        if (cur.getChar() == 'l') {
                                            value += "l";
                                            cur.nextCp();
                                            if (cur.isWhiteSpace() || cur.isSpecial())
                                                return new KeywordToken(start, (Position) cur.clone(), TokenTag.DECIMAL);
                                            else
                                                return getIdent(start, value);
                                        }
                                    }
                                }
                            }
                        } else if (cur.getChar() == 'f') {
                            value += "f";
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value += "a";
                                cur.nextCp();
                                if (cur.getChar() == 'u') {
                                    value += "u";
                                    cur.nextCp();
                                    if (cur.getChar() == 'l') {
                                        value += "l";
                                        cur.nextCp();
                                        if (cur.getChar() == 't') {
                                            value += "t";
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
                            value += "l";
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value += "e";
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value += "t";
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value += "e";
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.DELETE);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value += "o";
                        cur.nextCp();
                        if (cur.getChar() == 'u') {
                            value += "u";
                            cur.nextCp();
                            if (cur.getChar() == 'b') {
                                value += "b";
                                cur.nextCp();
                                if (cur.getChar() == 'l') {
                                    value += "l";
                                    cur.nextCp();
                                    if (cur.getChar() == 'e') {
                                        value += "e";
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
                    value = "e";
                    cur.nextCp();
                    if (cur.getChar() == 'x') {
                        value += "x";
                        cur.nextCp();
                        if (cur.getChar() == 'i') {
                            value += "i";
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value += "s";
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value += "t";
                                    cur.nextCp();
                                    if (cur.getChar() == 's') {
                                        value += "s";
                                        cur.nextCp();
                                        if (cur.isWhiteSpace() || cur.isSpecial())
                                            return new KeywordToken(start, (Position) cur.clone(), TokenTag.EXISTS);
                                        else
                                            return getIdent(start, value);
                                    }
                                }
                            }
                        }
                    }

                    return getIdent(start, value);

                case 'f':
                    value = "f";
                    cur.nextCp();
                    if (cur.getChar() == 'a') {
                        value += "a";
                        cur.nextCp();
                        if (cur.getChar() == 'l') {
                            value += "l";
                            cur.nextCp();
                            if (cur.getChar() == 's') {
                                value += "s";
                                cur.nextCp();
                                if (cur.getChar() == 'e') {
                                    value += "e";
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.FALSE);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'l') {
                        value += "l";
                        cur.nextCp();
                        if (cur.getChar() == 'o') {
                            value += "o";
                            cur.nextCp();
                            if (cur.getChar() == 'a') {
                                value += "a";
                                cur.nextCp();
                                if (cur.getChar() == 't') {
                                    value += 't';
                                    cur.nextCp();
                                    if (cur.isWhiteSpace() || cur.isSpecial())
                                        return new KeywordToken(start, (Position) cur.clone(), TokenTag.FLOAT);
                                    else
                                        return getIdent(start, value);
                                }
                            }
                        }
                    } else if (cur.getChar() == 'o') {
                        value += "o";
                        cur.nextCp();
                        if (cur.getChar() == 'r') {
                            value += "r";
                            cur.nextCp();
                            if (cur.getChar() == 'e') {
                                value += "e";
                                cur.nextCp();
                                if (cur.getChar() == 'i') {
                                    value += "i";
                                    cur.nextCp();
                                    if (cur.getChar() == 'g') {
                                        value += "g";
                                        cur.nextCp();
                                        if (cur.getChar() == 'n') {
                                            value += "n";
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
                    }

                    return getIdent(start, value);
            }
        }

        return new EOFToken(cur);
    }

    private IdentToken getIdent(Position start, String value) throws CloneNotSupportedException {
        StringBuilder valueBuilder = new StringBuilder(value);
        boolean wasLastComma = false;
        while (cur.isLetterOrDigit() || cur.getChar() == '.') {
            if (cur.getChar() == '.' && wasLastComma)
                messages.add(new Message((Position) cur.clone(), "Two dots are in order"));

            if (cur.isDigit() && wasLastComma)
                messages.add(new Message((Position) cur.clone(), "Wrong identifier"));

            wasLastComma = cur.getChar() == '.';
            valueBuilder.append(cur.getChar());
            cur.nextCp();
        }
        value = valueBuilder.toString();

        return new IdentToken(start, (Position) cur.clone(), value);
    }
}
