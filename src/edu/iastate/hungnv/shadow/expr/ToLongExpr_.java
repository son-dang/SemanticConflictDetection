package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.LongValue;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class ToLongExpr_ extends AbstractUnaryExpr_ {

	@Override
	protected Value evalBasicCase(Value value) {
		return LongValue.create(value.toLong());
	}

}
