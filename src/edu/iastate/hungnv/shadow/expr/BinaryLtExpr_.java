package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class BinaryLtExpr_ extends AbstractBinaryExpr_ {
	
	@Override
	protected Value evalBasicCase(Value leftValue, Value rightValue) {
		return leftValue.lt(rightValue) ? BooleanValue.TRUE : BooleanValue.FALSE;
	}

}
