package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class ToArrayExpr_ extends AbstractUnaryExpr_ {

	@Override
	protected Value evalBasicCase(Value value) {
		return value.toArray();
	}
	
	public static class ToArrayExpr_evalCopy extends AbstractUnaryExpr_ {

		@Override
		protected Value evalBasicCase(Value value) {
		    value = value.toValue();

		    if (value instanceof ArrayValue)
		      return value.copy();
		    else
		      return value.toArray();
		}

	}

}
