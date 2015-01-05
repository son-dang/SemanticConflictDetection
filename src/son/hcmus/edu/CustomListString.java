package son.hcmus.edu;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class CustomListString extends ArrayList<String> {
    @Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    @Override
    public int indexOf(Object o) {
    	String paramStr = (String)o;
    	for (int i=0; i<size(); i++){
    		if (this.get(i).equalsIgnoreCase(paramStr))
    			return i;
    	}
    	return -1;
    }
}
