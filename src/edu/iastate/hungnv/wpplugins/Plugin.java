package edu.iastate.hungnv.wpplugins;

import edu.iastate.hungnv.constraint.Constraint;

/**
 * 
 * @author HUNG
 *
 */
public class Plugin {
	
	private String pluginId;
	
	private Constraint constraint;
	
	private String path;
	
	/**
	 * Constructor
	 * @param pluginId
	 */
	public Plugin(String pluginId) {
		this.pluginId = pluginId;
		this.constraint = Constraint.createConstraint(pluginId);
	}
	
	/*
	 * Getters and setters
	 */
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPluginId() {
		return pluginId;
	}
	
	public Constraint getConstraint() {
		return constraint;
	}
	
	public String getPath() {
		return path;
	}
	
}
