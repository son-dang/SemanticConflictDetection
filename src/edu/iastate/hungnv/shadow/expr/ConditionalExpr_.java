package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.constraint.Constraint.Result;
import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class ConditionalExpr_ {
	
	/**
	 * @see com.caucho.quercus.expr.ConditionalExpr.evalCopy(Env)
	 */
	public static Value evalCopy(Env env, Expr _test, Expr _trueExpr, Expr _falseExpr) {
		Value condValue = _test.eval(env);
			
		Constraint constraint = MultiValue.whenTrue(condValue);
			
		Constraint aggregatedConstraint = env.getEnv_().getScope().getConstraint();
		Constraint.Result result = aggregatedConstraint.tryAddingConstraint(constraint);
		boolean constraintAlwaysTrue = (result == Result.THE_SAME);
		boolean constraintAlwaysFalse = (result == Result.ALWAYS_FALSE);
			
		if (constraintAlwaysTrue) {
			return _trueExpr.evalCopy(env);
		}
		else if (constraintAlwaysFalse) {
			return _falseExpr.evalCopy(env);
		}
		    
		env.getEnv_().enterNewScope(constraint);
		Value retValueTrueExpr = _trueExpr.evalCopy(env);
		env.getEnv_().exitScope();
			
		Constraint notConstraint = Constraint.createNotConstraint(constraint);
				
		env.getEnv_().enterNewScope(notConstraint);
		Value retValueFalseExpr = _falseExpr.evalCopy(env);
		env.getEnv_().exitScope();

		return MultiValue.createChoiceValue(constraint, retValueTrueExpr, retValueFalseExpr);
	}

}
