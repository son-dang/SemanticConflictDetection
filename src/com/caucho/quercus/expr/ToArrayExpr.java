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
import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.expr.ToArrayExpr_;

/**
 * Converts to an array
 */
public class ToArrayExpr extends AbstractUnaryExpr {
  public ToArrayExpr(Location location, Expr expr)
  {
    super(location, expr);
  }

  public ToArrayExpr(Expr expr)
  {
    super(expr);
  }

  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  public Value eval(Env env)
  {
	  // INST ADDED BY HUNG
	  
	  if (Env_.INSTRUMENT)
		  return new ToArrayExpr_().eval(env, _expr);
	 
	  // END OF ADDED CODE
	  
    return _expr.eval(env).toArray();
  }

  /**
   * Evaluates the expression.
   *
   * @param env the calling environment.
   *
   * @return the expression value.
   */
  public Value evalCopy(Env env)
  {
	  // INST ADDED BY HUNG
	  
	  // Fix errors with CAR plugin
	  // class.wp-dependencies.php:73
	  // 	if ( !$handles = (array) $handles )
	  
	  if (Env_.INSTRUMENT)
		  return new ToArrayExpr_.ToArrayExpr_evalCopy().eval(env, _expr);
	 
	  // END OF ADDED CODE
	  
    Value value = _expr.eval(env).toValue();

    if (value instanceof ArrayValue)
      return value.copy();
    else
      return value.toArray();
  }

  public String toString()
  {
    return "((array) " + _expr + ")";
  }
}

