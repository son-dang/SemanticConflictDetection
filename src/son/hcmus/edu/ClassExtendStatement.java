/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.statement.Statement;

/**
 *
 * @author 10123_000
 */
public class ClassExtendStatement extends Statement {
    private String className="";
    @Override
    public Value execute(Env env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public ClassExtendStatement(String className){
        if (className == null)
            className = "";
        //this.className = className.substring(className.lastIndexOf("\\") + 1);
        this.className = className;
    }
    
    @Override
    public boolean equal(Statement stm){
        if (!(stm instanceof ClassExtendStatement))
            return false;
        
        ClassExtendStatement ceStm = (ClassExtendStatement)stm;
        
        return this.className.equals(ceStm.className);
    }
    
    @Override
    public String toString(){
        if (!className.isEmpty()){
            return "$__Extends__ .= 'extends " + className + "';\r\n";
        }
        else return "$__Extends__ .=' ';\r\n";
    }
}
