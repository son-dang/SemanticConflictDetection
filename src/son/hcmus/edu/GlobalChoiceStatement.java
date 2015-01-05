/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.statement.Statement;
import java.util.List;

/**
 *
 * @author 10123_000
 */
public class GlobalChoiceStatement extends Statement {

    @Override
    public Value execute(Env env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private List<String> branches;
    
    public GlobalChoiceStatement(List<String> branches){
        this.branches = branches;
    }
    
    @Override
    public String toString(){
        StringBuilder retValue = new StringBuilder();
        for(String branch : branches){
            retValue.append("global ").append(branch).append(";\r\n");
        }
        
        return retValue.toString();
    }
}
