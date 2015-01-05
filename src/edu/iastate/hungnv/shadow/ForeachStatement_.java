package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.AbstractVarExpr;
import com.caucho.quercus.statement.Statement;

import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class ForeachStatement_ {
	
	/**
	 * @see com.caucho.quercus.statement.ForeachStatement.execute(Env)
	 */
	public static Value execute(Env env, Value value, final AbstractVarExpr _value, final Statement _block) {
		if (!(value instanceof MultiValue)) {
			_value.evalAssignValue(env, value);
            return _block.execute(env);
		}
		
		return ShadowInterpreter.eval(value, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value flattenedValue, Env env) {
				_value.evalAssignValue(env, flattenedValue);
	            return _block.execute(env);
			}
		}, env);
	}

}
