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

import edu.iastate.hungnv.debug.Debugger;
import edu.iastate.hungnv.shadow.BlockStatement_;
import edu.iastate.hungnv.shadow.Env_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import son.hcmus.edu.ModelBuilder;
import son.hcmus.edu.StatementAlignment;
import son.hcmus.edu.VarexException;

/**
 * Represents sequence of statements.
 */
public class BlockStatement extends Statement {
  protected Statement []_statements;

  public void setStatements(Statement[] stms){
      this._statements = stms;
  }
  
  public BlockStatement(Location location, Statement []statements)
  {
    super(location);

    _statements = statements;

    for (Statement stmt : _statements)
      stmt.setParent(this);
  }

  public BlockStatement(Location location, ArrayList<Statement> statementList)
  {
    super(location);

    _statements = new Statement[statementList.size()];
    statementList.toArray(_statements);

    for (Statement stmt : _statements)
      stmt.setParent(this);
  }

  public BlockStatement append(ArrayList<Statement> statementList)
  {
    Statement []statements
      = new Statement[_statements.length + statementList.size()];

    System.arraycopy(_statements, 0, statements, 0, _statements.length);

    for (int i = 0; i < statementList.size(); i++)
      statements[i + _statements.length] = statementList.get(i);

    return new BlockStatement(getLocation(), statements);
  }

  public Statement []getStatements()
  {
    return _statements;
  }

  /**
   * Returns true if the statement can fallthrough.
   */
  public int fallThrough()
  {
    for (int i = 0; i < getStatements().length; i++) {
      Statement stmt = getStatements()[i];

      int fallThrough = stmt.fallThrough();

      if (fallThrough != FALL_THROUGH)
        return fallThrough;
    }

    return FALL_THROUGH;
  }

  public Value execute(Env env)
  {
      // INST ADDED BY HUNG
      
      if (Env_.INSTRUMENT) {
    	  return BlockStatement_.execute(env, this, _statements);
      }
      
      // END OF ADDED CODE
      
    for (int i = 0; i < _statements.length; i++) {
      Statement statement = _statements[i];

	  // INST ADDED BY HUNG
      // NOTE: This code is not guarded by if (Env_.INSTRUMENT)
      
      Debugger.inst.checkBreakpoint(statement.getLocation());
	  // END OF ADDED CODE
      
      Value value = statement.execute(env);

      if (value != null) {
        return value;
      }
    }

    return null;
  }
  
  @Override
  public String toString(){
      String ret="{\n";
      
      for (Statement _stm : _statements)
          ret += _stm.toString();
      
      ret += "}\n";
      return ret;
  }
  
  @Override
  public ArrayList<Statement> alignStatement(ArrayList<Statement> statements){
      // align child statements
      ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
      for(Statement stm : statements){
          BlockStatement blockStm = (BlockStatement)stm;
          input.add(new ArrayList<Statement>(Arrays.asList(blockStm._statements)));
      }
      ArrayList<ArrayList<Statement>> alignResult = StatementAlignment.alignImpl(input);
      
      // set align result
      ArrayList<Statement> retValue = new ArrayList<Statement>();
      for (int i=0; i<statements.size(); i++){
          BlockStatement blockStm = new BlockStatement(Location.UNKNOWN, alignResult.get(i));
          blockStm.setParent(statements.get(i).getParent());
          retValue.add(blockStm);
      }
      
      return retValue;
  }
  
  @Override
  public String buildModel(ArrayList<Statement> statements, List<String> branches){
      StringBuilder retValue = new StringBuilder("{\r\n");
      ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
      for (Statement stm : statements){
          BlockStatement blockStm = (BlockStatement)stm;
          input.add(new ArrayList<Statement>(Arrays.asList(blockStm._statements)));
      }
      retValue.append(ModelBuilder.BuildModel(input, branches));
      retValue.append("}\r\n");
      return retValue.toString();
  }
  
  	@Override
	public ArrayList<Statement> findStatements(Class<?> type, boolean recursive) {
  		ArrayList<Statement> ret = new ArrayList<Statement>();
  		if (this.getClass().equals(type))
  			ret.add(this);
  		if (recursive){
  			for (Statement stm : _statements)
  				ret.addAll(stm.findStatements(type, recursive));
  		}
  		return ret;
	}
}

