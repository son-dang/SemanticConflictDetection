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
import edu.iastate.hungnv.shadow.IfStatement_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import son.hcmus.edu.GapStatement;
import son.hcmus.edu.ModelBuilder;
import son.hcmus.edu.StatementAlignment;

/**
 * Represents an if statement.
 */
public class IfStatement extends Statement {
  private final Expr _test;
  private Statement _trueBlock;
  private Statement _falseBlock;

  public IfStatement(Location location,
                     Expr test,
                     Statement trueBlock,
                     Statement falseBlock)
  {
    super(location);
    
    // make sure childs block always not null
    if (trueBlock == null)
        trueBlock = new GapStatement();
    if (falseBlock == null)
        falseBlock = new GapStatement();
    //
    
    _test = test;
    _trueBlock = trueBlock;
    _falseBlock = falseBlock;

    if (_trueBlock != null)
      _trueBlock.setParent(this);

    if (_falseBlock != null)
      _falseBlock.setParent(this);
  }

  protected Expr getTest()
  {
    return _test;
  }

  public Statement getTrueBlock()
  {
    return _trueBlock;
  }

  public Statement getFalseBlock()
  {
    return _falseBlock;
  }

  /**
   * Executes the 'if' statement, returning any value.
   */
  public Value execute(Env env)
  {
	  // EMPI ADDED BY HUNG
	  EmpiricalStudy.inst.statementExecuted(this, env);
	  // END OF ADDED CODE
	  
	// INST ADDED BY HUNG
	  
	if (Env_.INSTRUMENT)	  
	  return IfStatement_.execute(env, _test, _trueBlock, _falseBlock);
	  
	// END OF ADDED CODE
	  
    if (_test.evalBoolean(env)) {
      return _trueBlock.execute(env);
    }
    else if (_falseBlock != null) {
      return _falseBlock.execute(env);
    }
    else
      return null;
  }
  
  @Override
  public String toString(){
      String ret = "if(" + _test.toString() + ")";
      if (!(_trueBlock instanceof BlockStatement))
              ret+="\n";
      ret += _trueBlock.toString();
      if (_falseBlock != null){
          ret += "else";
          if (!(_falseBlock instanceof BlockStatement))
              if (_falseBlock instanceof IfStatement)
                ret+= " ";
              else ret+= "\n";
          ret+= _falseBlock.toString();
      }
      return ret;
  }
  
  @Override
  public ArrayList<Statement> alignStatement(ArrayList<Statement> statements){
      // align child statements
      ArrayList<ArrayList<Statement>> trueInput = new ArrayList<ArrayList<Statement>>();
      ArrayList<ArrayList<Statement>> falseInput = new ArrayList<ArrayList<Statement>>();
      
      for(Statement stm : statements){
          IfStatement ifStm = (IfStatement)stm;
          
          if (ifStm._trueBlock instanceof BlockStatement){
              BlockStatement blockStm = (BlockStatement)ifStm._trueBlock;
              trueInput.add(new ArrayList<Statement>(Arrays.asList(blockStm._statements)));
          }
          else {
              ArrayList<Statement> temp = new ArrayList<Statement>();
              temp.add(ifStm._trueBlock);
              trueInput.add(temp);
          }
          
          if (ifStm._falseBlock instanceof BlockStatement){
              BlockStatement blockStm = (BlockStatement)ifStm._falseBlock;
              falseInput.add(new ArrayList<Statement>(Arrays.asList(blockStm._statements)));
          }
          else{
              ArrayList<Statement> temp = new ArrayList<Statement>();
              temp.add(ifStm._falseBlock);
              falseInput.add(temp);
          }
      }
      ArrayList<ArrayList<Statement>> alignTrueResult = StatementAlignment.alignImpl(trueInput);
      ArrayList<ArrayList<Statement>> alignFalseResult = StatementAlignment.alignImpl(falseInput);
      
      // set align result
      ArrayList<Statement> retValue = new ArrayList<Statement>();
      for(int i=0; i<alignTrueResult.size(); i++){
          BlockStatement trueBlock = new BlockStatement(Location.UNKNOWN, alignTrueResult.get(i));
          BlockStatement falseBlock = new BlockStatement(Location.UNKNOWN, alignFalseResult.get(i));
          IfStatement ifStm = new IfStatement(Location.UNKNOWN, _test, trueBlock, falseBlock);
          ifStm.setParent(statements.get(i).getParent());
          retValue.add(ifStm);
      }
      return retValue;
  }
  
  @Override
  public String buildModel(ArrayList<Statement> statements, List<String> branches){
      StringBuilder retValue = new StringBuilder();
      ArrayList<ArrayList<Statement>> trueInput = new ArrayList<ArrayList<Statement>>();
      ArrayList<ArrayList<Statement>> falseInput = new ArrayList<ArrayList<Statement>>();
      for(Statement stm : statements){
          IfStatement ifStm = (IfStatement)stm;
          ArrayList<Statement> trueBlock = new ArrayList<Statement>();
          ArrayList<Statement> falseBlock = new ArrayList<Statement>();
          trueBlock.add(ifStm._trueBlock);
          falseBlock.add(ifStm._falseBlock);
          trueInput.add(trueBlock);
          falseInput.add(falseBlock);
      }
      
      retValue.append("if(").append(_test.toString()).append(")\r\n");
      retValue.append(ModelBuilder.BuildModel(trueInput, branches));
      retValue.append("else ");
      retValue.append(ModelBuilder.BuildModel(falseInput, branches));
      return retValue.toString();
  }
  
  @Override
  public boolean equal(Statement stm){
      if (!(stm instanceof IfStatement))
          return false;
      IfStatement ifStm = (IfStatement)stm;
      
      return this._test.toString().equals(ifStm._test.toString());
  }
  
  @Override
	public ArrayList<Statement> findStatements(Class<?> type, boolean recursive) {
		ArrayList<Statement> ret = new ArrayList<Statement>();
		if (this.getClass().equals(type))
			ret.add(this);
		if (recursive){
			ret.addAll(_trueBlock.findStatements(type, recursive));
			if (_falseBlock != null)
				ret.addAll(_falseBlock.findStatements(type, recursive));
		}
		return ret;
	}
}
