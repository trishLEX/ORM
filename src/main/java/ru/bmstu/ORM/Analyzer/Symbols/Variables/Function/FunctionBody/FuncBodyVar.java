package ru.bmstu.ORM.Analyzer.Symbols.Variables.Function.FunctionBody;

import ru.bmstu.ORM.Analyzer.Symbols.Tokens.IdentToken;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Common.Types.TypenameVar;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.Var;
import ru.bmstu.ORM.Analyzer.Symbols.Variables.VarTag;

import java.util.HashMap;
import java.util.Map;

public class FuncBodyVar extends Var {
    private HashMap<IdentToken, TypenameVar> variables;
    public FuncBodyVar() {
        super(VarTag.FUNC_BODY);
        variables = new HashMap<>();
    }

    public void addVariables(HashMap<IdentToken, TypenameVar> variables) {
        for (Map.Entry<IdentToken, TypenameVar> entry: variables.entrySet()) {
            if (this.variables.containsKey(entry.getKey()))
                throw new RuntimeException("Parameter " + entry.getKey() + " exists");
            else
                this.variables.put(entry.getKey(), entry.getValue());
        }
    }

    public HashMap<IdentToken, TypenameVar> getVariables() {
        return variables;
    }
}
