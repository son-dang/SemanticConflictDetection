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
import com.caucho.quercus.program.Function;
import com.caucho.util.L10N;

import edu.iastate.hungnv.empiricalstudy.EmpiricalStudy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import son.hcmus.edu.ModelBuilder;
import son.hcmus.edu.StatementAlignment;

/**
 * Represents a function definition
 */
public class FunctionDefStatement extends Statement {
  private final static L10N L = new L10N(FunctionDefStatement.class);
  
  protected Function _fun;

  public FunctionDefStatement(Location location, Function fun)
  {
    super(location);
    
    _fun = fun;
  }
  
  public Value execute(Env env)
  {
	  // EMPI ADDED BY HUNG
	  EmpiricalStudy.inst.statementExecuted(this, env);
	  // END OF ADDED CODE
	  
    try {
      String name = _fun.getName();

      if (env.findFunction(name) == null)
        env.addFunction(name, _fun);
      else
        env.error(getLocation(),
                  L.l("function {0}() is already defined.", name));
    }
    catch (RuntimeException e) {
      rethrow(e, RuntimeException.class);
    }

    return null;
  }
  
  @Override
  public boolean equal(Statement stm){
      if (!(stm instanceof FunctionDefStatement))
          return false;
      
      FunctionDefStatement fdStm = (FunctionDefStatement)stm;
      
      return (this._fun.getName().equalsIgnoreCase(fdStm._fun.getName()));
  }
  
  @Override
  public ArrayList<Statement> alignStatement(ArrayList<Statement> statements){
      // align statements of functions
      ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
      for(Statement stm : statements){
          FunctionDefStatement fdStm = (FunctionDefStatement)stm;
          ArrayList<Statement> temp = new ArrayList<Statement>();
          temp.add(fdStm._fun._statement.createBlock());
          input.add(temp);
      }
      input = StatementAlignment.alignImpl(input);
      
      // set align result to functions
      for(int i=0; i<statements.size(); i++){
          ((FunctionDefStatement)statements.get(i))._fun._statement = new BlockStatement(Location.UNKNOWN, input.get(i));
      }
      return statements;
  }
  
  @Override
  public String buildModel(ArrayList<Statement> statements, List<String> branches){
      StringBuilder retValue = new StringBuilder("{\r\n");
      
      ArrayList<ArrayList<Statement>> input = new ArrayList<ArrayList<Statement>>();
      for (Statement stm : statements){
          FunctionDefStatement fdStm = (FunctionDefStatement)stm;
          input.add(new ArrayList<Statement>(Arrays.asList(fdStm._fun._statement.createBlock()._statements)));
      }
      retValue.append(ModelBuilder.BuildModel(input, branches));
      return retValue.toString();
  }
}

