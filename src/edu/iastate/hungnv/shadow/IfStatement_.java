package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;
import com.caucho.quercus.statement.Statement;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.constraint.Constraint.Result;
import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class IfStatement_ {
	
	/**
	 * @see com.caucho.quercus.statement.IfStatement.execute(Env)
	 */
	public static Value execute(Env env, Expr condition, Statement trueBlock, Statement falseBlock) {
		Value condValue = condition.eval(env);
		
		Constraint constraint = MultiValue.whenTrue(condValue);
		
		Constraint aggregatedConstraint = env.getEnv_().getScope().getConstraint();
		Constraint.Result result = aggregatedConstraint.tryAddingConstraint(constraint);
		boolean constraintAlwaysTrue = (result == Result.THE_SAME);
		boolean constraintAlwaysFalse = (result == Result.ALWAYS_FALSE);
		
	    if (constraintAlwaysTrue) {
	    	return trueBlock.execute(env);
	    }
	    else if (constraintAlwaysFalse) {
	    	if (falseBlock != null)
	    		return falseBlock.execute(env);
	    	else
	    		return null;
	    }
	    
	    Value retValueTrueBlock = null;
	    Value retValueFalseBlock = null;
		
		env.getEnv_().enterNewScope(constraint);
		retValueTrueBlock = trueBlock.execute(env);
		env.getEnv_().exitScope();
		
		if (falseBlock != null) {
			Constraint notConstraint = Constraint.createNotConstraint(constraint);
			
			env.getEnv_().enterNewScope(notConstraint);
			retValueFalseBlock = falseBlock.execute(env);
			env.getEnv_().exitScope();
		}
		
		if (retValueTrueBlock == null && retValueFalseBlock == null)
			return null;
		else
			return MultiValue.createChoiceValue(constraint, retValueTrueBlock, retValueFalseBlock);
	}
  
}