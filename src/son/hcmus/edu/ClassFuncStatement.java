/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.Location;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.program.Function;
import com.caucho.quercus.program.MethodDeclaration;
import com.caucho.quercus.statement.BlockStatement;
import com.caucho.quercus.statement.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author 10123_000
 */
public class ClassFuncStatement extends Statement {

    @Override
    public Value execute(Env env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Function function;
    
    public ClassFuncStatement(Function function){
        this.function = function;
    }
    
    @Override
    public boolean equal(Statement stm){
        if (!(stm instanceof ClassFuncStatement))
            return false;
        
        ClassFuncStatement cfStm = (ClassFuncStatement)stm;
        if (!this.function.getName().equals(cfStm.function.getName()))
            return false;
        
        return true;
    }
    
    @Override
    public String buildModel(ArrayList<Statement> statements, List<String> branches){
        StringBuilder retValue = new StringBuilder("{\r\n");
        int nBranch = statements.size();
        
        // Group functions have same args together
        List<Integer> curColIndex = new ArrayList<Integer>();
        for (int j = 0; j < nBranch; j++) {
            curColIndex.add(j);
        }

        List<List<Integer>> stmGroupsIndex = new ArrayList<List<Integer>>();
        while (!curColIndex.isEmpty()) {
            List<Integer> curGroupIndex = new ArrayList<Integer>();
            Statement curStm = statements.get(curColIndex.get(curColIndex.size() - 1));

            for (int j = curColIndex.size() - 1; j >= 0; j--) {
                if (statements.get(curColIndex.get(j)).toString().equals(curStm.toString())) {
                    curGroupIndex.add(curColIndex.get(j));
                    curColIndex.remove(j);
                }
            }
            stmGroupsIndex.add(curGroupIndex);
        }
        
        boolean hasIf = false;
        // build a string from each group
        for(List<Integer> stmIndex : stmGroupsIndex){
            // get constraint of current group
            String groupConstraint = ModelBuilder.createConstraint(stmIndex, branches, 
                    statements.get(stmIndex.get(0)));
            if (groupConstraint != null){
                if (hasIf)
                    retValue.append("else ");
                hasIf = true;
                retValue.append("if(").append(groupConstraint).append(")\r\n");
            }
            else hasIf = false;
            retValue.append(statements.get(stmIndex.get(0)).toString());
        }
        
        if (function instanceof MethodDeclaration){
        	retValue.append("}");
        	return retValue.toString();
        }
        
        // build model for statements in functions
        ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
        for (Statement stm : statements) {
            ClassFuncStatement cfStm = (ClassFuncStatement) stm;

            // insert global choice to the start of func
            BlockStatement blockStm = cfStm.function._statement.createBlock();
            ArrayList<Statement> globalChoices = new ArrayList<Statement>(Arrays.asList(blockStm.getStatements()));
            globalChoices.add(0, new GlobalChoiceStatement(branches));

            ArrayList<Statement> temp = new ArrayList<Statement>();
            temp.add(new BlockStatement(Location.UNKNOWN, globalChoices));
            input.add(temp);
        }
        
        retValue.append("$__Functions__ .= '").append(ModelBuilder.BuildModel(input, branches)
                .replace("'", "\\'"))
                .append("';}\r\n");
        return retValue.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder retValue = new StringBuilder();
        retValue.append("$__Functions__ .= '").append(function.toString()
                .replace("'", "\\'"))
                .append("';\r\n");
        return retValue.toString();
    }
}
