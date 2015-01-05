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
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;

import edu.iastate.hungnv.debug.OutputViewer;
import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;

/**
 * Represents an echo statement in a PHP program.
 */
public class EchoStatement extends Statement {
  protected final Expr _expr;

  private String _genId;
  
  /**
   * Creates the echo statement.
   */
  public EchoStatement(Location location, Expr expr)
  {
    super(location);

    _expr = expr;
  }
  
  public Value execute(Env env)
  {
	  // EMPI ADDED BY HUNG
	  EmpiricalStudy.inst.statementExecuted(this, env);
	  // END OF ADDED CODE
	  
    Value value = _expr.eval(env);
    if (value.isMultiValue()){
        Switch _swValue = new Switch();
        Switch _switch = MultiValue.flatten(value);
        for(Case _case : _switch.getCases()){
            Value temp = _case.getValue();
            if (temp == null)
                temp = NullValue.NULL;
            _swValue.addCase(new Case(_case.getConstraint(), temp));
        }
        
        value = MultiValue.createSwitchValue(_swValue);
    }
    // INST MODIFIED BY HUNG
    
    // Original code:
    //value.print(env);
    
    // New code:
    
    // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
    
    boolean curState = OutputViewer.inst.getEnabled();
	OutputViewer.inst.setEnabled(false);
	
    value.print(env);
    
    OutputViewer.inst.setEnabled(curState);
    
    OutputViewer.inst.print(value);
    
    // END OF MODIFIED CODE

    return null;
  }
  
  @Override
  public String toString(){
      return "echo " + this._expr.toString() + ";\r\n";
  }
}

