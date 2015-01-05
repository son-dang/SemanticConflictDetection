/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.statement.Statement;

/**
 *
 * @author 10123_000
 */
public class ClassFieldStatement extends Statement {
    private String fieldName;
    private Expr expr;
    private String visibility;
    private String fieldType;

    public ClassFieldStatement(String fieldName, Expr expr, String visibility, String fieldType){
        this.fieldName = fieldName;
        this.expr = expr;
        this.visibility = visibility;
        this.fieldType = fieldType;
    }
    
    @Override
    public String toString(){
        StringBuilder retValue = new StringBuilder();
        if (fieldType.equals("field"))
            retValue.append(visibility.toLowerCase())
                    .append(" $").append(fieldName).append(" = ")
                    .append(expr.toString());
        else if (fieldType.equals("static"))
            retValue.append("static $").append(fieldName).append(" = ")
                    .append(expr.toString());
        
        return "$__Fields__ .='" + retValue.toString().replace("'", "\\'") + ";';\r\n";
    }

    @Override
    public Value execute(Env env) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean equal(Statement stm){
        if (!(stm instanceof ClassFieldStatement))
            return false;
        ClassFieldStatement cfStm = (ClassFieldStatement)stm;
        
        if (this.expr.getClass() != cfStm.expr.getClass())
          return false;
        
        if (!this.fieldName.equals(cfStm.fieldName))
            return false;
        
        if (!this.fieldType.equals(cfStm.fieldType))
            return false;
        
        if (!this.visibility.equals(cfStm.visibility))
            return false;
        
      return (this.expr.toString().equals(cfStm.expr.toString()));
    }
}
