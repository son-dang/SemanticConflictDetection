package edu.iastate.hungnv.shadow;

import com.caucho.quercus.env.Value;

/**
 * 
 * @author HUNG
 *
 */
@SuppressWarnings("serial")
public class WrappedObject extends Value {
	
	private Object object;
	
	public WrappedObject(Object object) {
		this.object = object;
	}
	
	public Object getObject() {
		return object;
	}

}
