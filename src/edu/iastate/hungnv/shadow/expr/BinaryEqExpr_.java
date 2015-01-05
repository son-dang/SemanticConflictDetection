package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.BooleanValue;
import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
public class BinaryEqExpr_ extends AbstractBinaryExpr_ {
	
	@Override
	protected Value evalBasicCase(Value leftValue, Value rightValue) {
		return leftValue.eq(rightValue) ? BooleanValue.TRUE : BooleanValue.FALSE;
	}

}
