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

package com.caucho.quercus.statement;

import com.caucho.quercus.Location;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;

import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.util.Logging;

/**
 * Represents an expression statement in a PHP program.
 */
public class ExprStatement extends Statement {
  private Expr _expr;

  /**
   * Creates the expression statement.
   */
  public ExprStatement(Location location, Expr expr)
  {
    super(location);

    _expr = expr;
  }

  /**
   * Returns the expression.
   */
  public Expr getExpr()
  {
    return _expr;
  }

  public Value execute(Env env)
  {
	  // EMPI ADDED BY HUNG
	  EmpiricalStudy.inst.statementExecuted(this, env);
	  // END OF ADDED CODE
	//try{
    // php/0d92
    Location oldLocation = env.setLocation(getLocation());

    // php/1a08
    _expr.evalTop(env);

    env.setLocation(oldLocation);
       // }
      //  catch(Exception ex){
       //     String str=ex.toString();
         /*   Env_.errorLog.append("Exception: ").append(ex.getMessage())
                    .append(" when ").append(env.getEnv_().getScope().getConstraint().toString())
                    .append("\r\n");*/
          //  Logging.LOGGER.severe("Exception: " + ex.getMessage() + " when " + env.getEnv_().getScope().getConstraint());
        //}
    return null;
  }
  
  @Override
  public String toString(){
      return this._expr.toString() + ";\n";
  }
  
  @Override
  public boolean equal(Statement stm){
      if (!(stm instanceof ExprStatement))
          return false;
      
      ExprStatement exprStm = (ExprStatement)stm;
      if (this.getExpr().getClass() != exprStm.getExpr().getClass())
          return false;

      return (this.getExpr().toString().equals(exprStm.getExpr().toString()));
  }
}

