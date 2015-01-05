package edu.iastate.hungnv.wpplugins;

import java.util.ArrayList;
import java.util.List;

import edu.iastate.hungnv.constraint.Constraint;

/**
 * 
 * @author HUNG
 *
 */
public class TestConfig {
	
	private String binaryString;
	
	private Constraint constraint;
	private ArrayList<Plugin> activePlugins;
	private ArrayList<Plugin> inactivePlugins;
	
	/**
	 * Constructor
	 * @param binaryString
	 */
	public TestConfig(String binaryString) {
		this.binaryString = binaryString;
		
		List<Plugin> plugins = PluginManager.inst.getPlugins();
		this.constraint = computeConstraint(plugins, binaryString);
		this.activePlugins = computeActivePlugins(plugins, binaryString);
		this.inactivePlugins = computeInactivePlugins(plugins, binaryString);
	}
	
	private Constraint computeConstraint(List<Plugin> plugins, String binaryString) {
		Constraint constraint = Constraint.TRUE;
		for (int i = 0; i < binaryString.length(); i++) {
			Constraint pluginConstraint;
			
			if (binaryString.charAt(i) == '1')
				pluginConstraint = plugins.get(i).getConstraint();
			else
				pluginConstraint = Constraint.createNotConstraint(plugins.get(i).getConstraint());
			
			constraint = Constraint.createAndConstraint(constraint, pluginConstraint);
		}
		return constraint;
	}
	
	private ArrayList<Plugin> computeActivePlugins(List<Plugin> plugins, String binaryString) {
		ArrayList<Plugin> activePlugins = new ArrayList<Plugin>();
		
		for (int i = 0; i < binaryString.length(); i++) {
			if (binaryString.charAt(i) == '1')
				activePlugins.add(plugins.get(i));
		}
		
		return activePlugins;
	}
	
	private ArrayList<Plugin> computeInactivePlugins(List<Plugin> plugins, String binaryString) {
		ArrayList<Plugin> inactivePlugins = new ArrayList<Plugin>();
		
		for (int i = 0; i < binaryString.length(); i++) {
			if (binaryString.charAt(i) == '0')
				inactivePlugins.add(plugins.get(i));
		}
		
		return inactivePlugins;
	}
	
	/*
	 * Getters & setters
	 */
	
	public String getBinaryString() {
		return binaryString;
	}
	
	public Constraint getConstraint() {
		return constraint;
	}
	
	public List<Plugin> getActivePlugins() {
		return new ArrayList<Plugin>(activePlugins);
	}

	public List<Plugin> getInactivePlugins() {
		return new ArrayList<Plugin>(inactivePlugins);
	}
	
	/*
	 * Methods
	 */
	
	/**
	 * Returns the string describing active plugins.
	 */
	public String getActivePluginsString() {
		if (activePlugins.isEmpty())
			return "NONE";
		
		String activePluginsString = "";
		
		for (Plugin plugin : activePlugins) {
			if (!activePluginsString.isEmpty())
				activePluginsString += ".";
			
			activePluginsString += plugin.getPluginId();
		}

		return activePluginsString;
	}
	
}
