/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Value;

/**
 *
 * @author 10123_000
 */
public class BinaryModExpr_ extends AbstractBinaryExpr_{

    @Override
    protected Value evalBasicCase(Value leftValue, Value rightValue) {
        return leftValue.mod(rightValue);
    }
    
}
