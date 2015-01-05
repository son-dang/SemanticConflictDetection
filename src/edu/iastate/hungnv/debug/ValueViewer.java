package edu.iastate.hungnv.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.caucho.quercus.env.ArrayValueImpl;
import com.caucho.quercus.env.NullValue;
import com.caucho.quercus.env.ObjectExtValue;
import com.caucho.quercus.env.ObjectExtValue.EntrySet;
import com.caucho.quercus.env.Value;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.scope.ScopedValue;
import edu.iastate.hungnv.shadow.Env_;
import edu.iastate.hungnv.util.Logging;
import edu.iastate.hungnv.util.XmlDocument;
import edu.iastate.hungnv.value.Case;
import edu.iastate.hungnv.value.Choice;
import edu.iastate.hungnv.value.Concat;
import edu.iastate.hungnv.value.MultiValue;
import edu.iastate.hungnv.value.Switch;
import edu.iastate.hungnv.value.Undefined;

/**
 * 
 * @author HUNG
 *
 */
public class ValueViewer {
	
	public static final String xmlFile 			= "D:\\heap.xml";
	public static final String xmlFileAll		= "D:\\heap-all.xml";
	public static final String xmlFileDerived 	= "D:\\heap-derived.xml";
	
	public static final String XML_ROOT 		= "ROOT";
	public static final String XML_NUM_ATTRS 	= "NumAttrs";
	public static final String XML_ATTR			= "Attr";
	
	public static final String XML_NAME_VALUE	= "NAME-VALUE";
	public static final String XML_VALUE 		= "VALUE";
	public static final String XML_CONCAT 		= "CONCAT";
	public static final String XML_CHOICE 		= "CHOICE";
	public static final String XML_SWITCH		= "SWITCH";
	public static final String XML_CASE 		= "CASE";
	public static final String XML_UNDEFINED	= "UNDEFINED";
	public static final String XML_ARRAY		= "ARRAY";
	public static final String XML_OBJECT		= "OBJECT";
	
	public static final String XML_DESC			= "Desc";
	
	public static final String XML_INFO1		= "Info1";
	public static final String XML_INFO2		= "Info2";
	
	/**
	 * List of NameValuePairs
	 */
	private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	
	/**
	 * List of names that will not be printed out
	 */
	private static HashSet<String> excludedNames = new HashSet<String>();
	
	static {
		excludedNames.add("REMOTE_ADDR");
		excludedNames.add("REMOTE_HOST");
		excludedNames.add("REMOTE_PORT");
		excludedNames.add("REQUEST_TIME");
		excludedNames.add("SERVER_ADDR");
		excludedNames.add("_transient_doing_cron");
		excludedNames.add("cache_hits");
		excludedNames.add("cache_misses");
		excludedNames.add("last_changed");
		excludedNames.add("mc_uninstalled");
		excludedNames.add("num_queries");
		excludedNames.add("rows_affected");	
		excludedNames.add("timestart");
		excludedNames.add("update_option");
		excludedNames.add("update_option_mc_version");
		excludedNames.add("updated_option");		
	}
	
        /*public void writeToXmlFileWithConcreteConstraint(String xmlFile){
            int nConstraint = Env_.ConfigOptions.size();
            System.out.println(nConstraint + " config option(s)");
            for(int i=nConstraint-2; i>=0; i--){
                ((Choice)Env_.ConfigOptions.get(i)).setValue1(Env_.ConfigOptions.get(i+1));
                ((Choice)Env_.ConfigOptions.get(i)).setValue2(Env_.ConfigOptions.get(i+1));
            }
            Switch sw = null;
            if (nConstraint > 0)
                sw = Env_.ConfigOptions.get(0).flatten();
            if (sw!= null){
                for (Case _case : sw.getCases()){
                    String xmlFileName = xmlFile + " - " + _case.getConstraint().toString() + ".xml";
                    writeToXmlFile(xmlFileName, _case.getConstraint());
                }
            }
            writeToXmlFile(xmlFile);
        }*/
        
	/**
	 * Adds a name-value pair
	 * @param name
	 * @param value
	 */
	public void add(Value name, Value value) {
		nameValuePairs.add(new NameValuePair(name, value));
	}
	
	/**
	 * Writes all values to an XML file
	 * @param xmlFile
	 */
	public void writeToXmlFile(String xmlFile) {
		writeToXmlFile(xmlFile, null);
	}
	
	/**
	 * Writes all values satisfying a given constraint to an XML file
	 * @param xmlFile
	 * @param constraint (can be null)
	 */
	public void writeToXmlFile(String xmlFile, Constraint constraint) {
		Document xmlDocument = XmlDocument.newDocument();
		
		Element rootElement = xmlDocument.createElement(XML_ROOT);
		rootElement.setAttribute(XML_NUM_ATTRS, "2");
		rootElement.setAttribute(XML_ATTR + "1", XML_INFO1);
		rootElement.setAttribute(XML_ATTR + "2", XML_INFO2);
		xmlDocument.appendChild(rootElement);
		
		Collections.sort(nameValuePairs, SortNameValuePairByName.inst);
		
		for (NameValuePair pair : nameValuePairs) {
			Element element = createXmlElementForNameValuePair(pair.getName(), pair.getValue(), xmlDocument, constraint);
			if (element != null)
				rootElement.appendChild(element);
		}
		
		XmlDocument.writeXmlDocumentToFile(xmlDocument, xmlFile);
	}

	/**
	 * Creates an XML element for a name-value pair
	 * @param name
	 * @param value
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForNameValuePair(Value name, Value value, Document xmlDocument, Constraint constraint) {
		if (excludedNames.contains(name.toString()))
			return null;
		
		if (constraint != null)
			name = MultiValue.simplify(name, constraint);
		else {
			name = MultiValue.simplify(name);
		}
		if (name == Undefined.UNDEFINED)
			return null;

			
		if (constraint != null)
			value = MultiValue.simplify(value, constraint);
		else {
			value = MultiValue.simplify(value);
		}
		if (value == Undefined.UNDEFINED || value instanceof NullValue)
			return null;
		
		Element element = xmlDocument.createElement(XML_NAME_VALUE);
		element.setAttribute(XML_DESC, name.toString());
		element.setAttribute(XML_INFO1, value instanceof ObjectExtValue ? "Object" : value.toString());
		
		Element childElement = createXmlElementForValue(value, xmlDocument, constraint);
		if (childElement != null)
			element.appendChild(childElement);
		
		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Creates an XML element for a value
	 * @param value
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForValue(Value value, Document xmlDocument, Constraint constraint) {
		if (value instanceof ScopedValue) {
			Logging.LOGGER.warning("In ValueViewer.createXmlElementForValue: value must not be a ScopedValue. Please debug.");
			
			return createXmlElementForValue(((ScopedValue) value).getValue(), xmlDocument, constraint);
		}
		else if (value instanceof Concat) {
			return createXmlElementForConcat((Concat) value, xmlDocument, constraint);
		}
		else if (value instanceof Choice) {
			return createXmlElementForChoice((Choice) value, xmlDocument, constraint);
		}
		else if (value instanceof Switch) {
			return createXmlElementForSwitch((Switch) value, xmlDocument, constraint);
		}
		else if (value instanceof Undefined) {
			return createXmlElementForUndefined((Undefined) value, xmlDocument, constraint);
		}
		else if (value instanceof ArrayValueImpl) {
			return createXmlElementForArray((ArrayValueImpl) value, xmlDocument, constraint);
		}
		else if (value instanceof ObjectExtValue) {
			return createXmlElementForObject((ObjectExtValue) value, xmlDocument, constraint);
		}
		else {
			//System.out.println("Please handle " + value.getClass().getSimpleName());
			Element element = xmlDocument.createElement(XML_VALUE);
			element.setAttribute(XML_DESC, value.toString());

			return element;
		}
	}
	
	/**
	 * Creates an XML element for a CONCAT
	 * @param concat
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForConcat(Concat concat, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_CONCAT);
		//element.setAttribute(XML_INFO1, concat.getValue1().toString());
		//element.setAttribute(XML_INFO2, concat.getValue2().toString());

		for (Value childValue : concat) {
			Element child = createXmlElementForValue(childValue, xmlDocument, constraint);
			if (child != null)
				element.appendChild(child);
		}
		
		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Creates an XML element for a CHOICE
	 * @param choice
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForChoice(Choice choice, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_CHOICE);
		element.setAttribute(XML_INFO1, choice.getConstraint().toString());

		Element child1 = createXmlElementForValue(choice.getValue1(), xmlDocument, constraint);
		if (child1 != null)
			element.appendChild(child1);
		
		Element child2 = createXmlElementForValue(choice.getValue2(), xmlDocument, constraint);
		if (child2 != null)
			element.appendChild(child2);
		
		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Creates an XML element for a SWITCH
	 * @param switch_
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForSwitch(Switch switch_, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_SWITCH);

		for (Case case_ : switch_) {
			Element child = createXmlElementForCase(case_, xmlDocument, constraint);
			if (child != null)
				element.appendChild(child);
		}
		
		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Creates an XML element for a CASE
	 * @param case_
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForCase(Case case_, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_CASE);
		element.setAttribute(XML_INFO1, case_.getConstraint().toString());

		Element child = createXmlElementForValue(case_.getValue(), xmlDocument, constraint);
		if (child != null)
			element.appendChild(child);
		
		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Creates an XML element for an UNDEFINED
	 * @param case_
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForUndefined(Undefined undefined, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_UNDEFINED);
		return element;
	}

	/**
	 * Creates an XML element for an ARRAY
	 * @param array
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForArray(ArrayValueImpl array, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_ARRAY);
		
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Iterator<Map.Entry<Value, Value>> iter = array.getIterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			pairs.add(new NameValuePair(pair.getKey(), pair.getValue()));
		}
			
		Collections.sort(pairs, SortNameValuePairByName.inst);
		
		for (NameValuePair pair : pairs) {
			Element childElement = createXmlElementForNameValuePair(pair.getName(), pair.getValue(), xmlDocument, constraint);
			if (childElement != null)
				element.appendChild(childElement);
		}

		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Creates an XML element for an OBJECT
	 * @param object
	 * @param xmlDocument
	 * @param constraint
	 * @return
	 */
	private Element createXmlElementForObject(ObjectExtValue object, Document xmlDocument, Constraint constraint) {
		Element element = xmlDocument.createElement(XML_OBJECT);
		
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Iterator<Map.Entry<Value,Value>> iter = ((EntrySet) object.entrySet()).iterator(); iter.hasNext(); ) {
			Map.Entry<Value, Value> pair = iter.next();
			pairs.add(new NameValuePair(pair.getKey(), pair.getValue()));
		}
			
		Collections.sort(pairs, SortNameValuePairByName.inst);
		
		for (NameValuePair pair : pairs) {
			Element childElement = createXmlElementForNameValuePair(pair.getName(), pair.getValue(), xmlDocument, constraint);
			if (childElement != null)
				element.appendChild(childElement);
		}

		return (element.hasChildNodes() ? element : null);
	}
	
	/**
	 * Class NameValuePair
	 */
	private class NameValuePair {
		
		private Value name;
		private Value value;
		
		public NameValuePair(Value name, Value value) {
			this.name = name;
			this.value = value;
		}
		
		public Value getName() {
			return name;
		}
		
		public Value getValue() {
			return value;
		}
		
	}
	
	/**
	 * Helper class to support sorting of NameValuePairs
	 */
	private static class SortNameValuePairByName implements Comparator<NameValuePair> {

		public static SortNameValuePairByName inst = new SortNameValuePairByName();
		
		@Override
		public int compare(NameValuePair pair1, NameValuePair pair2) {
			return pair1.getName().toString().compareTo(pair2.getName().toString());
		}

	}

}
