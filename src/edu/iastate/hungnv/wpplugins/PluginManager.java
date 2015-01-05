package edu.iastate.hungnv.wpplugins;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import edu.iastate.hungnv.regressiontest.RegressionTest;
import edu.iastate.hungnv.util.FileIO;

/**
 * 
 * @author HUNG
 *
 */
public class PluginManager {
	
	private ArrayList<Plugin> plugins;
	
	/**
	 * Static instance of PluginManager
	 */
	public static PluginManager inst = new PluginManager();
	
	/**
	 * Constructor
	 */
	public PluginManager() {
		this.plugins = readPlugins();
	}
	
	private ArrayList<Plugin> readPlugins() {
		ResourceBundle bundle = ResourceBundle.getBundle(RegressionTest.pathsPropertiesFile);
		String pluginsFile = bundle.getString("pluginsFile");
		
		ArrayList<Plugin> plugins = new ArrayList<Plugin>();
		
		for (String line : FileIO.readLinesFromFile(pluginsFile)) {
			String[] parts = line.split("=");
			String pluginId = parts[0];
			String pluginPath = parts[1];
			
			Plugin plugin = new Plugin(pluginId);
			plugin.setPath(pluginPath);
			
			plugins.add(plugin);
		}
		
		return plugins;
	}
	
	/*
	 * Getters & setters
	 */
	
	public List<Plugin> getPlugins() {
		return new ArrayList<Plugin>(plugins);
	}

}
