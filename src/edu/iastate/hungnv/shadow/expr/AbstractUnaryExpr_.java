package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;

import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.MultiValue.IOperation;

/**
 * 
 * @author HUNG
 *
 */
public abstract class AbstractUnaryExpr_ {

	/**
	 * Evaluates a unary expression.
	 */
	public Value eval(Env env, Expr expr) {
		Value value = expr.eval(env);
		
		return MultiValue.operateOnValue(value, new IOperation() {
			@Override
			public Value operate(Value value_) {
				return evalBasicCase(value_);
			}
		});
	}
	
	/**
	 * Evaluates a unary expression.
	 * @param value		A Quercus value, not null
	 * @return The evaluated value
	 */
	protected abstract Value evalBasicCase(Value value);
	
}
