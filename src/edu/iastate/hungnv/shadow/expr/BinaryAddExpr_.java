/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iastate.hungnv.shadow.expr;

import com.caucho.quercus.env.Value;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 *
 * @author 10123_000
 */
public class BinaryAddExpr_ extends AbstractBinaryExpr_{

    @Override
    protected Value evalBasicCase(Value leftValue, Value rightValue) {
        return leftValue.add(rightValue);
    }
    
}
