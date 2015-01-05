package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * 
 * @author HUNG
 *
 */
public abstract class AbstractBinaryExpr_ {
	
	/**
	 * Evaluates a binary expression.
	 */
	public Value eval(Env env, Expr leftExpr, Expr rightExpr) {
		Value leftValue = leftExpr.eval(env);
		
		Value result = earlyEval(leftValue);
		if (result != null)
			return result;
		
		Value rightValue = rightExpr.eval(env);
		
		if (!(leftValue instanceof MultiValue) && !(rightValue instanceof MultiValue))
			return evalBasicCase(leftValue, rightValue);

		Switch switch_ = new Switch();
		
		for (Case leftCase : MultiValue.flatten(leftValue))
		for (Case rightCase : MultiValue.flatten(rightValue)) {
			Constraint constraint = Constraint.createAndConstraint(leftCase.getConstraint(), rightCase.getConstraint());
			
			// EMPI
			//EmpiricalStudy.inst.recordEvalBasicCase(env, constraint);
			if (leftCase.getValue() == null)
                            continue;
                        if (rightCase.getValue() == null)
                            continue;
			Value value = evalBasicCase(leftCase.getValue(), rightCase.getValue());
			
			if (constraint.isSatisfiable()) // This check is required
				switch_.addCase(new Case(constraint, value));
		}
		
		// EMPI
		EmpiricalStudy.inst.contextSplit(switch_);
		
		return MultiValue.createSwitchValue(switch_);
	}
	
	/**
	 * Evaluates a binary expression based on the left value only.
	 * For example, if the binary expression is C1 AND C2 and C1 is FALSE, then (C1 AND C2) is FALSE regardless of C2.
	 * Returns null if the left value alone is not enough to determine the value of the expression.
	 * @param leftValue 	The left value of the binary expression
	 * @return The value of the binary expression based on the left value, or null if it cannot be determined.
	 */
	protected Value earlyEval(Value leftValue) {
		return null;
	}
	
	/**
	 * Evaluates a binary expression.
	 * @param leftValue		A Quercus value, not null
	 * @param rightValue	A Quercus value, not null
	 * @return The evaluated value
	 */
	protected abstract Value evalBasicCase(Value leftValue, Value rightValue);
	
}
