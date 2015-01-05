package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.FunIncludeOnceExpr;

/**
 * 
 * @author HUNG
 *
 */
public class FunIncludeOnceExpr_ {

	/**
	 * @see com.caucho.quercus.expr.FunIncludeOnceExpr.eval(Env)
	 */
	public static Value eval(Env env, final FunIncludeOnceExpr _this) {
		Value exprValue = _this.getExpr().eval(env);
		
		return ShadowInterpreter.eval(exprValue, new ShadowInterpreter.IBasicCaseHandler() {
			@Override
			public Value evalBasicCase(Value exprValue_, Env env) {
				return _this.eval_orig(env, exprValue_);
			}
		}, env);
	}
	  
}