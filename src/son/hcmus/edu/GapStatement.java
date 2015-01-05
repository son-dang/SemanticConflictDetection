/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.UnsetValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.statement.Statement;

/**
 *
 * @author 10123_000
 */
public class GapStatement extends Statement {

    @Override
    public Value execute(Env env) {
        return null;
    }
    
    @Override
    public String toString(){
        return "{}\n";
    }
}
