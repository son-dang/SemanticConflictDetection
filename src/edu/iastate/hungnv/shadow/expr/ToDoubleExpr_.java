package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.DoubleValue;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class ToDoubleExpr_ extends AbstractUnaryExpr_ {

	@Override
	protected Value evalBasicCase(Value value) {
		return new DoubleValue(value.toDouble());
	}

}
