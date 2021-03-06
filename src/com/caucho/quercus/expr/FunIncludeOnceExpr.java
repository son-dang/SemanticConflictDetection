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
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.env.StringValue;
import com.caucho.vfs.Path;

import edu.iastate.hungnv.debug.TraceViewer;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.FunIncludeOnceExpr_;

/**
 * Represents a PHP include statement
 */
public class FunIncludeOnceExpr extends AbstractUnaryExpr {
  protected Path _dir;
  protected boolean _isRequire;
  
  public FunIncludeOnceExpr(Location location, Path sourceFile, Expr expr)
  {
    super(location, expr);

    // XXX: issues with eval
    if (! sourceFile.getScheme().equals("string"))
      _dir = sourceFile.getParent();
  }

  public FunIncludeOnceExpr(Location location,
                            Path sourceFile,
                            Expr expr,
                            boolean isRequire)
  {
    this(location, sourceFile, expr);

    _isRequire = isRequire;
  }
  
  public FunIncludeOnceExpr(Path sourceFile, Expr expr)
  {
    this(Location.UNKNOWN, sourceFile, expr);
  }
  
  public FunIncludeOnceExpr(Path sourceFile, Expr expr, boolean isRequire)
  {
    this(Location.UNKNOWN, sourceFile, expr, isRequire);
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
		  return FunIncludeOnceExpr_.eval(env, this);
	  
	  // END OF ADDED CODE
	  
    StringValue name = _expr.eval(env).toStringValue();

    // return env.include(_dir, name);
    
	  // INST ADDED BY HUNG
      // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
    
	  try {
		  TraceViewer.inst.enterFile(name.toString(), getLocation(), env.getEnv_().getScope().getConstraint());
	  // END OF ADDED CODE 
    
    env.pushCall(this, NullValue.NULL, new Value[] { name });
    
    try {
      if (_dir != null)
        return env.includeOnce(_dir, name, _isRequire);
      else if (_isRequire)
        return env.requireOnce(name);
      else
        return env.includeOnce(name);
    }
    finally {
      env.popCall();
    }
    
	  // INST ADDED BY HUNG
      // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
    
	  } finally {
		  TraceViewer.inst.exitFile(name.toString(), getLocation());
	  }
	  // END OF ADDED CODE
  }
  
//INST ADDED BY HUNG
  public Value eval_orig(Env env, Value exprValue)
  {
    StringValue name = exprValue.toStringValue();

    // return env.include(_dir, name);
    
	  // INST ADDED BY HUNG
	  try {
		  TraceViewer.inst.enterFile(name.toString(), getLocation(), env.getEnv_().getScope().getConstraint());
	  // END OF ADDED CODE  
    
    env.pushCall(this, NullValue.NULL, new Value[] { name });
    
    try {
      if (_dir != null)
        return env.includeOnce(_dir, name, _isRequire);
      else if (_isRequire)
        return env.requireOnce(name);
      else
        return env.includeOnce(name);
    }
    finally {
      env.popCall();
    }
    
	  // INST ADDED BY HUNG
	  } finally {
		  TraceViewer.inst.exitFile(name.toString(), getLocation());
	  }
	  // END OF ADDED CODE
  }
//END OF ADDED CODE	  
  
  public boolean isRequire()
  {
    return _isRequire;
  }
  
  public String toString()
  {
    return "include_once(" + _expr.toString() + ")";
  }
}

