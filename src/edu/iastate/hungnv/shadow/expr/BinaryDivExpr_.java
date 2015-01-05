package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Value;

public class BinaryDivExpr_ extends AbstractBinaryExpr_{

    @Override
    protected Value evalBasicCase(Value leftValue, Value rightValue) {
        return leftValue.div(rightValue);
    }
    
}