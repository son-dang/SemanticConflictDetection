package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class UnaryNotExpr_ extends AbstractUnaryExpr_ {

	@Override
	protected Value evalBasicCase(Value value) {
		return value.toBoolean() ? BooleanValue.FALSE : BooleanValue.TRUE;
	}

}
