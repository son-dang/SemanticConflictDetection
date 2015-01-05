package edu.iastate.hungnv.shadow.lib;

import java.util.Iterator;

import com.caucho.quercus.env.ArrayValue;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.env.Value;
import com.caucho.util.L10N;

import edu.iastate.hungnv.value.MultiValue;

/**
 * 
 * @author HUNG
 *
 */
public class StringModule_ {
	
	/**
	 * @see com.caucho.quercus.lib.string.StringModule.implode(Env, Value, Value)
	 */
	public static Value implode_(Env env,
								Value glueV,
								Value piecesV,
								L10N L)
	{
	     StringValue glue;
	     ArrayValue pieces;

	     if ((piecesV.isArray() && glueV.isArray())
	          || glueV.isArray()) {
	       pieces = glueV.toArrayValue(env);
	       glue = piecesV.toStringValue();   
	     }
	     else if (piecesV.isArray()) {
	       pieces = piecesV.toArrayValue(env);
	       glue = glueV.toStringValue();
	     }
	     else {
	       env.warning(L.l("neither argument to implode is an array: {0}, {1}",
	                     glueV.getClass().getName(), piecesV.getClass().getName()));

	       return NullValue.NULL;
	     }

	     Value sb = glue.createStringBuilder();
	     boolean isFirst = true;

	     Iterator<Value> iter = pieces.getValueIterator(env);

	     while (iter.hasNext()) {
	       if (! isFirst) {
	    	   if (sb instanceof MultiValue)
	    		   sb = MultiValue.createConcatValue(sb, glue, true);
	    	   else
	    		   sb = ((StringValue) sb).append(glue);
	       }

	       isFirst = false;

	       Value next = iter.next();
	       if (sb instanceof MultiValue || next instanceof MultiValue)
	    	   sb = MultiValue.createConcatValue(sb, next, true);
	       else
	    	   sb = ((StringValue) sb).append(next);
	     }

	     return sb;
	   }

}
