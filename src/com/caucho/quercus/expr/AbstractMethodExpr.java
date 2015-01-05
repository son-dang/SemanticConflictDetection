/*
 * Copyright (c) 1998-2012 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.quercus.expr;

import com.caucho.quercus.Location;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.QuercusClass;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.StringValue;
import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;

import edu.iastate.hungnv.value.Switch;

/**
 * Represents a PHP function expression.
 */
abstract public class AbstractMethodExpr extends Expr {
  protected AbstractMethodExpr(Location location)
  {
    super(location);
  }
  

  /**
   * Evaluates the expression as a copy
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  @Override
  public Value evalCopy(Env env)
  {
    return eval(env).copy();
  }

  /**
   * Evaluates the expression as a copy
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  @Override
  public Value evalArg(Env env, boolean isTop)
  {
    return eval(env).copy();
  }
  
  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  protected Value eval(Env env, Value qThis,
                       StringValue methodName, int hashCode,
                       Expr []argExprs)
  {
    Value []args = evalArgs(env, argExprs);

    env.pushCall(this, qThis, args);

    try {
      env.checkTimeout();

    /*  if (Env_.INSTRUMENT){
          Switch _switch = new Switch();
          for(Case _case : MultiValue.flatten(qThis)){
              ObjectExtValue oev = (ObjectExtValue)_case.getValue();
              try{
                  Constraint constraint = oev.getQuercusClass().getConstraint();
                  Value value = oev.callMethod(env, methodName, hashCode, args);
                  _switch.addCase(new Case(constraint, value));
              }
              catch(Exception ex){
                  String a = ex.toString();
              }
              QuercusClass tempQC = qc;
              while (qc != null){
                  try{
                      Constraint constraint= qc.getConstraint();
                      oev.setQuercusClass(qc);
                      oev.initObject(env, qc);
                      Value value = oev.callMethod(env, methodName, hashCode, args);
                      _switch.addCase(new Case(constraint, value));
                  }
                  catch(Exception ex){
                      String a = ex.toString();
                      int abc = a.length();
                  }
                  qc = qc.getAggregateClass();
              }
              oev.setQuercusClass(tempQC);
          }
          return MultiValue.createSwitchValue(_switch);
      }*/
      Value temp = qThis.callMethod(env, methodName, hashCode, args);
      //return qThis.callMethod(env, methodName, hashCode, args);
      return temp;
    } finally {
      env.popCall();
    }
  }
}

