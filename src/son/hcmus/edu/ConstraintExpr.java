/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package son.hcmus.edu;

import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.value.MultiValue;

/**
 *
 * @author 10123_000
 */
public class ConstraintExpr extends Expr {
    private Constraint _constraint = null;
    public ConstraintExpr(Constraint constraint){
        _constraint = constraint;
    }
    
    public Constraint getConstraint(){
        return _constraint;
    }
    
    @Override
    public Value eval(Env env) {
        return MultiValue.createChoiceValue(_constraint, BooleanValue.TRUE, BooleanValue.FALSE);
    }
    
    @Override
    public Value evalArg(Env env, boolean isTop)
    {
        return eval(env);
    }
    
}
