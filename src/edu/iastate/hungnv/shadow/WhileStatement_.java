/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.BreakValue;
import com.caucho.quercus.env.ContinueValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.statement.Statement;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 *
 * @author 10123_000
 */
public class WhileStatement_ {

    public static Value execute(Env env, Expr test, Statement block) {
        Value condValue = test.eval(env);
        Constraint trueConstraint = MultiValue.whenTrue(condValue);
        Switch _switch = new Switch();
        
        while (trueConstraint.isSatisfiable()) {
            env.getEnv_().enterNewScope(trueConstraint);
            Value value = block.execute(env);
            env.getEnv_().exitScope();
            
            if (value == null) {
            } else if (value instanceof BreakValue) {
                BreakValue breakValue = (BreakValue) value;

                int target = breakValue.getTarget();

                if (target > 1) {
                    return new BreakValue(target - 1);
                } else {
                    break;
                }
            } else if (value instanceof ContinueValue) {
                ContinueValue conValue = (ContinueValue) value;

                int target = conValue.getTarget();

                if (target > 1) {
                    return new ContinueValue(target - 1);
                }
            } else {
                //return value;
                if (value instanceof MultiValue){
                    Switch valueSwitch = MultiValue.flatten(value);
                    for (Case _case : valueSwitch.getCases()){
                        Constraint andConstraint = Constraint.createAndConstraint(trueConstraint, _case.getConstraint());
                        if (andConstraint.isSatisfiable()){
                            _switch.addCase(new Case(andConstraint, _case.getValue()));
                        }
                    }
                }
                else{
                    _switch.addCase(new Case(trueConstraint, value));
                }
            }

            Constraint nullConstraint = MultiValue.whenNull(MultiValue.flatten(value));
            condValue = test.eval(env);
            trueConstraint = Constraint.createAndConstraint(MultiValue.whenTrue(condValue), nullConstraint);
        }

        return null;
    }
}
