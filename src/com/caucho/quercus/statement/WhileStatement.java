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
import com.caucho.quercus.env.BreakValue;
import com.caucho.quercus.env.ContinueValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.Value;
import com.caucho.quercus.expr.Expr;

import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.WhileStatement_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import son.hcmus.edu.ModelBuilder;
import son.hcmus.edu.StatementAlignment;

/**
 * Represents a while statement.
 */
public class WhileStatement extends Statement {
  protected /*final*/ Expr _test;
  protected /*final*/ Statement _block;
  protected final String _label;

  public WhileStatement(Location location,
                        Expr test,
                        Statement block,
                        String label)
  {
    super(location);

    _test = test;
    _block = block;
    _label = label;
    
    block.setParent(this);
  }

  @Override
  public boolean isLoop()
  {
    return true;
  }

  public Value execute(Env env)
  {
	  // EMPI ADDED BY HUNG
	  EmpiricalStudy.inst.statementExecuted(this, env);
	  // END OF ADDED CODE
          
          // CODE ADDED BY SON
          if (Env_.INSTRUMENT)
            return WhileStatement_.execute(env, _test, _block);
          // END OF ADDED CODE
	  
    try {
      env.setLocation(getLocation());

      while (_test.evalBoolean(env)) {
        env.checkTimeout();

        Value value = _block.execute(env);
        
        if (value == null) {
        }
        else if (value instanceof BreakValue) {
          BreakValue breakValue = (BreakValue) value;
          
          int target = breakValue.getTarget();
          
          if (target > 1)
            return new BreakValue(target - 1);
          else
            break;
        }
        else if (value instanceof ContinueValue) {
          ContinueValue conValue = (ContinueValue) value;
          
          int target = conValue.getTarget();
          
          if (target > 1) {
            return new ContinueValue(target - 1);
          }
        }
        else
          return value;
        
        env.setLocation(getLocation());
      }
    }
    catch (RuntimeException e) {
      rethrow(e, RuntimeException.class);
    }

    return null;
  }
  
  @Override
  public boolean equal(Statement stm){
      if (!(stm instanceof WhileStatement))
          return false;
      
      WhileStatement whileStm = (WhileStatement)stm;
      
      return (whileStm._test.toString().equals(_test.toString()));
  }
  
  @Override
  public ArrayList<Statement> alignStatement(ArrayList<Statement> statements){
      // align child statements
      ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
      for(Statement stm : statements){
          WhileStatement whileStm = (WhileStatement)stm;
          input.add(new ArrayList<Statement>(Arrays.asList(whileStm._block.createBlock())));
      }
      ArrayList<ArrayList<Statement>> alignResult = StatementAlignment.alignImpl(input);
      
      // set align result
      for (int i=0; i<statements.size(); i++){
          ((WhileStatement)statements.get(i))._block = new BlockStatement(Location.UNKNOWN, alignResult.get(i));
      }
      
      return statements;
  }
  
  @Override
  public String buildModel(ArrayList<Statement> statements, List<String> branches){
      StringBuilder retValue = new StringBuilder("while(");
      retValue.append(this._test.toString()).append(")\r\n");
      ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
      for (Statement stm : statements){
          WhileStatement whileStm = (WhileStatement)stm;
          input.add(new ArrayList<Statement>(Arrays.asList(whileStm._block.createBlock()._statements)));
      }
      retValue.append(ModelBuilder.BuildModel(input, branches));
      return retValue.toString();
  }
  
  @Override
	public String toString() {
	  StringBuilder ret = new StringBuilder("while(");
	  ret.append(_test.toString() + ")");
	  ret.append(_block.createBlock().toString());
	  return ret.toString();
	}
  
  @Override
	public ArrayList<Statement> findStatements(Class<?> type, boolean recursive) {
		ArrayList<Statement> ret = new ArrayList<Statement>();
		if (this.getClass().equals(type))
			ret.add(this);
		if (recursive){
			ret.addAll(_block.findStatements(type, recursive));
		}
		return ret;
	}
}

