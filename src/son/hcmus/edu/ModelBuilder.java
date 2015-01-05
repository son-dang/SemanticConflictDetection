/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.expr.FunIncludeExpr;
import com.caucho.quercus.expr.FunIncludeOnceExpr;
import com.caucho.quercus.statement.BlockStatement;
import com.caucho.quercus.statement.ClassDefStatement;
import com.caucho.quercus.statement.ExprStatement;
import com.caucho.quercus.statement.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author 10123_000
 */
public class ModelBuilder {

    public static String createConstraint(List<Integer> stmIndex, List<String> branches, Statement stm) {

        if (stm instanceof ExprStatement){
            ExprStatement exprStm = (ExprStatement)stm;
            if (exprStm.getExpr() instanceof FunIncludeExpr)
                return null;
            if (exprStm.getExpr() instanceof FunIncludeOnceExpr)
                return null;
        }
        
        if (stm instanceof BlockStatement)
            return null;

        if (stm instanceof GlobalChoiceStatement)
            return null;
        
        if (stmIndex.size() == branches.size()) {
        	
        	if (stm instanceof ExprStatement)
        		return null;
        	
        	if (stm instanceof NamespaceStatement)
        		return null;
        	
            if (stm instanceof ClassFuncStatement)
                return null;
            
            if (stm instanceof ClassExtendStatement)
                return null;
            
            if (stm instanceof ClassDefStatement)
                return null;
            
            return branches.get(0);
        }
        
      //  if (stmIndex.size() == 1 && stmIndex.get(0) == 0)
      //      return "true";

        String constraint = "";

        for (int j = stmIndex.size() - 1; j >= 0; j--) {

            if (j < stmIndex.size() - 1 && stmIndex.get(j) - stmIndex.get(j + 1) == 1) {
                continue;
            }

            String subConstraint = branches.get(stmIndex.get(j));
            int start = stmIndex.get(j) + 1;
            int end = -1;
            for (int k = j - 1; k >= 0; k--) {
                if (stmIndex.get(k) == start) {
                    start++;
                } else {
                    end = stmIndex.get(k);
                    break;
                }
            }

            if (end > -1) {
                for (int k = start; k < end; k++) {
                    subConstraint += " && !" + branches.get(k);
                }
            }

            if (constraint.isEmpty()) {
                constraint = subConstraint;
            } else {
                constraint += " || " + subConstraint;
            }
        }

        return constraint;
    }

    /**
     * Convert aligned statements into string
     * @param input
     * @param branchesVar
     * @return
     */
    public static String BuildModel(ArrayList<ArrayList<Statement>> input, List<String> branchesVar) {
        // init values
    	StringBuilder retValue = new StringBuilder("");
        int nStm = input.get(0).size();
        int nBranch = input.size();

        if (branchesVar == null) {
            branchesVar = new ArrayList<String>();
            for (int i = 0; i < nBranch; i++) {
                branchesVar.add("$B" + i);
            }
        }

        for (int i = 0; i < nStm; i++) {
            // Group similar statement together
            List<Integer> curColIndex = new ArrayList<Integer>();
            for (int j = 0; j < nBranch; j++) {
                curColIndex.add(j);
            }

            List<List<Integer>> stmGroupsIndex = new ArrayList<List<Integer>>();
            while (!curColIndex.isEmpty()) {
                List<Integer> curGroupIndex = new ArrayList<Integer>();
                Statement curStm = input.get(curColIndex.get(curColIndex.size() - 1)).get(i);

                for (int j = curColIndex.size() - 1; j >= 0; j--) {
                    if (input.get(curColIndex.get(j)).get(i).equal(curStm)) {
                        curGroupIndex.add(curColIndex.get(j));
                        curColIndex.remove(j);
                    }
                }
                stmGroupsIndex.add(curGroupIndex);
            }

            boolean hasIf = false;
            // build a string from each group
            for (List<Integer> stmIndex : stmGroupsIndex) {
                // get constraint of current group
                String groupConstraint = createConstraint(stmIndex, branchesVar, 
                        input.get(stmIndex.get(0)).get(i));
                if (groupConstraint != null) {
                    if (hasIf) {
                        retValue.append("else ");
                    }
                    hasIf = true;
                    retValue.append("if(").append(groupConstraint).append(")\r\n");
                }
                else
                    hasIf = false;
                ArrayList<Statement> subStms = new ArrayList<Statement>();
                List<String> subBranches = new ArrayList<String>();
                Collections.reverse(stmIndex);
                for (Integer index : stmIndex) {
                    subStms.add(input.get(index).get(i));
                    subBranches.add(branchesVar.get(index));
                }
                retValue.append(input.get(stmIndex.get(0)).get(i).buildModel(subStms, subBranches));
            }

            retValue.append("\r\n");
        }

        return retValue.toString();
    }
}
