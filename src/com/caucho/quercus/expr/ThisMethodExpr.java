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
import com.caucho.quercus.env.Value;
import com.caucho.util.L10N;

import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.shadow.Functions;
import edu.iastate.hungnv.shadow.Functions.__ASSERT__;
import edu.iastate.hungnv.shadow.Functions.__ExpectOutputRegex__;
import edu.iastate.hungnv.shadow.Functions.__ExpectOutputString__;

import java.util.ArrayList;

/**
 * Represents a PHP method call expression from $this.
 */
public class ThisMethodExpr extends ObjectMethodExpr {
	private static final L10N L = new L10N(ThisMethodExpr.class);

	public ThisMethodExpr(Location location, ThisExpr qThis, String methodName,
			ArrayList<Expr> args) {
		super(location, qThis, methodName, args);
	}

	//
	// java code generation
	//

	public String toString() {
		StringBuilder retValue = new StringBuilder();

		// if using PHP unit frame work
		// change their functions to __ASSERT__ functions
		if (Env_.testClass.equals("PHPUnit_Framework_TestCase")) {
			String methodName = _methodName.toString();
			if (methodName.equals("assertTrue")) {
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + " == true)");	
			} 
			
			else if (methodName.equals("assertFalse")){
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + " == false)");	
			}
			
			else if (methodName.equals("assertObjectHasAttribute")) {
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[1].toString() + "->");
				retValue.append(_args[0].toString().substring(1, _args[0].toString().length()-1));
				retValue.append(" != null)");
				
			}
			
			else if (methodName.equals("assertAttributeEmpty")){
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append("empty(" + _args[1].toString() + "->");
				retValue.append(_args[0].toString().substring(1, _args[0].toString().length()-1));
				retValue.append(") == true)");			
			}
			
			else if (methodName.equals("assertAttributeEquals")){
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[2].toString() + "->");
				retValue.append(_args[1].toString().substring(1, _args[1].toString().length()-1));
				retValue.append(" == " + _args[0].toString() + ")");
			}
			
			else if (methodName.equals("assertEquals")) {
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + " == " + _args[1].toString() + ")");
			}
			
			else if (methodName.equals("assertSame")){
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + " === " + _args[1].toString() + ")");
			}
			
			else if (methodName.equals("assertInstanceOf")){
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[1].toString() + " instanceof " + _args[0] + ")");
			}
			
			else if (methodName.equals("assertNotNull")){
				retValue.append(__ASSERT__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + " != null)");
			}
			
			else if (methodName.equals("expectOutputString")){
				retValue.append(__ExpectOutputString__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + ")");
			}
			
			else if (methodName.equals("expectOutputRegex")){
				retValue.append(__ExpectOutputRegex__.class.getSimpleName() + "(");
				retValue.append(_args[0].toString() + ")");
			}
			
			else {
				retValue.append("$this->").append(_methodName.toString())
						.append("(");
				for (int i = 0; i < _args.length; i++) {
					retValue.append(_args[i].toString());
					if (i < _args.length - 1) {
						retValue.append(", ");
					}
				}
				retValue.append(")");
			}
		}

		return retValue.toString();
		// return "$this->$" + _methodName + "()";
	}
}
