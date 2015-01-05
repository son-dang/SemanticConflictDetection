package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class ToObjectExpr_ extends AbstractUnaryExpr_ {

	@Override
	protected Value evalBasicCase(Value value) {
		return value.toObject(Env.getInstance());
	}

}
