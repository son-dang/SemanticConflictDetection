package edu.iastate.hungnv.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.caucho.quercus.Location;

import edu.iastate.hungnv.constraint.Constraint;
import edu.iastate.hungnv.util.XmlDocument;

/**
 * 
 * @author HUNG
 *
 */
public class TraceViewer {
	
	public static final String xmlFile 			= "D:\\trace.xml";
	public static final String xmlFileAll		= "D:\\trace-all.xml";
	
	public static final String XML_ROOT 		= "ROOT";
	public static final String XML_NUM_ATTRS 	= "NumAttrs";
	public static final String XML_ATTR			= "Attr";
	
	public static final String XML_CALL_SITE	= "CALLSITE";
	
	public static final String XML_DESC			= "Desc";
	
	public static final String XML_INFO1		= "Info1";
	public static final String XML_INFO2		= "Info2";
	
	/**
	 * Static instance of TraceViewer
	 */
	public static TraceViewer inst = new TraceViewer();
	
	/**
	 * Stack of callsites
	 */
	private Stack<CallSite> stack = new Stack<CallSite>();
	
	/**
	 * Constructor.
	 * Creates a default top-level callsite.
	 */
	public TraceViewer() {
		stack.add(new CallSite(null, null, null)); // Top callsite can be null because it's never displayed (only its children are)
	}
	
	/**
	 * Resets the TraceViewer as if it's newly created.
	 * Must be consistent with the constructor.
	 */
	public void reset() {
		stack.clear();
		stack.add(new CallSite(null, null, null)); // Top callsite can be null because it's never displayed (only its children are)
	}
	
	/**
	 * Enters a function
	 * @param functionName
	 * @param location
	 * @param constraint
	 */
	public void enterFunction(String functionName, Location location, Constraint constraint) {
		CallSite callSite = new CallSite(functionName, location, constraint);
                if (!stack.empty())
                    stack.peek().addChild(callSite);
		stack.push(callSite);
	}
	
	/**
	 * Modifies the function name to make it more descriptive.
	 * See the callsite in com.caucho.quercus.expr.CallExpr.evalImpl(Env, boolean, boolean)
	 * @param oldName
	 * @param newName
	 */
	public void modifyLastEnteredFunctionName(String oldName, String newName) {
		for (int i = stack.size() - 1; i > 0; i--) {
			if (stack.get(i).getName().equals(oldName)) {
				stack.get(i).setName(newName);
				break;
			}
		}
	}
	
	/**
	 * Exits a function
	 * @param functionName
	 * @param location
	 */
	public void exitFunction(String functionName, Location location) {
            if (!stack.empty())
		stack.pop();
	}
	
	/**
	 * Enters a file
	 * @param fileName
	 * @param location
	 * @param constraint
	 */
	public void enterFile(String fileName, Location location, Constraint constraint) {
		// ADHOC Adhoc code below (to shorten file names)
		String rootPath = "C:\\Eclipse\\workspace\\javaEE\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\quercus\\WebApps\\";
		if (fileName.startsWith(rootPath))
			fileName = fileName.substring(rootPath.length());
		  
		CallSite callSite = new CallSite(fileName, location, constraint);
                if (!stack.empty())
                    stack.peek().addChild(callSite);
		stack.push(callSite);
	}
	
	/**
	 * Exits a file
	 * @param fileName
	 * @param location
	 */
	public void exitFile(String fileName, Location location) {
            if (!stack.empty())
		stack.pop();
	}
	
	/**
	 * Writes the trace to an XML file.
	 * @param xmlFile
	 */
	public void writeToXmlFile(String xmlFile) {
		Document xmlDocument = XmlDocument.newDocument();
		
		Element rootElement = xmlDocument.createElement(XML_ROOT);
		rootElement.setAttribute(XML_NUM_ATTRS, "2");
		rootElement.setAttribute(XML_ATTR + "1", XML_INFO1);
		rootElement.setAttribute(XML_ATTR + "2", XML_INFO2);
		xmlDocument.appendChild(rootElement);
		
		CallSite top = stack.firstElement();
		for (CallSite callSite : top.getChildren()) {
			Element element = createXmlElementForCallSite(callSite, xmlDocument);
			rootElement.appendChild(element);
		}
		
		XmlDocument.writeXmlDocumentToFile(xmlDocument, xmlFile);
	}
	
	/**
	 * Creates an XML element for a CallSite
	 * @param callSite
	 * @param xmlDocument
	 * @return
	 */
	private Element createXmlElementForCallSite(CallSite callSite, Document xmlDocument) {
		Element element = xmlDocument.createElement(XML_CALL_SITE);
		element.setAttribute(XML_DESC, callSite.getName() + " (" + callSite.countChildren() + ")");
		element.setAttribute(XML_INFO1, callSite.getLocation().prettyPrint());
		element.setAttribute(XML_INFO2, callSite.getConstraint().toString());
		
		for (CallSite child : callSite.getChildren()) {
			Element childElement = createXmlElementForCallSite(child, xmlDocument);
			element.appendChild(childElement);
		}
		
		return element;
	}
	
	/**
	 * Class CallSite
	 */
	private class CallSite {
		
		private String name;
		
		private Location location;
		
		private Constraint constraint;
		
		private ArrayList<CallSite> children = new ArrayList<CallSite>();
		
		private int childrenCount = 0;
		
		/**
		 * Constructor
		 * @param name
		 * @param location
		 * @param constraint
		 */
		private CallSite(String name, Location location, Constraint constraint) {
			this.name = name;
			this.location = location;
			this.constraint = constraint;
		}
		
		public String getName() {
			return name;
		}
		
		public Location getLocation() {
			return location;
		}
		
		public Constraint getConstraint() {
			return constraint;
		}
		
		public void addChild(CallSite callSite) {
			children.add(callSite);
		}
		
		public List<CallSite> getChildren() {
			return new ArrayList<CallSite>(children);
		}
		
		public int countChildren() {
			if (childrenCount > 0)
				return childrenCount;
			
			for (CallSite child : children) {
				childrenCount += (1 + child.countChildren());
			}
			
			return childrenCount;
		}
		
		/**
		 * This method should be called by modifyLastEnteredFunctionName only.
		 * @see edu.iastate.hungnv.debug.TraceViewer.modifyLastEnteredFunctionName(String, String)
		 */
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
}