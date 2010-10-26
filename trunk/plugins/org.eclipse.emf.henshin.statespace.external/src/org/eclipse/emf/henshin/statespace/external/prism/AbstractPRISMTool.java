/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University Berlin, 
 * Philipps-University Marburg and others. All rights reserved.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CWI Amsterdam - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.statespace.external.prism;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.statespace.State;
import org.eclipse.emf.henshin.statespace.StateSpace;
import org.eclipse.emf.henshin.statespace.Transition;
import org.eclipse.emf.henshin.statespace.external.AbstractFileBasedValidator;

/**
 * Abstract PRISM tool wrapper.
 * @author Christian Krause
 */
public abstract class AbstractPRISMTool extends AbstractFileBasedValidator {
	
	// Properties key for PRISM path.
	public static final String PRISM_PATH_KEY = "prismPath";
	
	// Properties key for PRISM arguments.
	public static final String PRISM_ARGS_KEY = "prismArgs";
	
	// Currently used properties.
	private Properties properties;
	
	/**
	 * Invoke PRISM.
	 * @param stateSpace State space.
	 * @param args Arguments.
	 * @param monitor Monitor.
	 * @return Created process.
	 * @throws Exception On errors.
	 */
	protected Process invokePRISM(StateSpace stateSpace, File formulaFile, String[] args) throws Exception {
		
		// Generate the SM file.
		File smFile = generatePRISMFile(stateSpace);
		
		// Get the executable, path and arguments.
		String prism = getPRISMExecutable();
		String baseArgs = properties.getProperty(PRISM_ARGS_KEY);
		String path = properties.getProperty(PRISM_PATH_KEY);
		
		// Create the command.
		List<String> command = new ArrayList<String>();
		command.add(path!=null ? new File(path+File.separator+prism).getAbsolutePath() : prism);
		command.add(smFile.getAbsolutePath());
		if (formulaFile!=null) {
			command.add(formulaFile.getAbsolutePath());
		}
		if (baseArgs!=null) {
			for (String arg : baseArgs.split(" ")) {
				command.add(arg.trim());
			}
		}
		if (args!=null) {
			for (String arg : args) {
				command.add(arg.trim());
			}
		}
		
		// Now we can invoke the PRISM tool:
		System.out.println(command);
		return Runtime.getRuntime().exec(
				command.toArray(new String[] {}), 
				null, 
				path!=null ? new File(path) : null);
		
	}

	
	/**
	 * Generate a PRISM file from a state space.
	 * @param stateSpace State space.
	 * @return The generated file.
	 * @throws Exception On errors.
	 */
	protected File generatePRISMFile(StateSpace stateSpace) throws Exception {
		
		// Check if the properties file exist.
		IFile propertiesFile = getPropertiesFile(stateSpace);
		if (!propertiesFile.exists()) {
			initializeProperties(stateSpace, propertiesFile);
			throw new Exception("Error loading rates. Please edit file '" + propertiesFile.getName() + "'.");
		}
		
		// Load properties.
		loadProperties(propertiesFile);
		Map<Rule,Double> rates = new HashMap<Rule,Double>();
		
		// Load the rates.
		for (Rule rule : stateSpace.getRules()) {
			try {
				String value = properties.getProperty(getRateName(rule));
				rates.put(rule,Double.valueOf(value.trim()));
			} catch(Throwable t) {
				throw new ParseException("Error parsing rate for rule '" + rule.getName() + 
						"'. Please correct it in file '" + propertiesFile.getName() + "'.",0);
			}
		}
				
		// Now we are ready to generate the PRIM file.		
		StringBuffer buffer = new StringBuffer();
		buffer.append("// CTMC model generated by Henshin on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + "\n\n");
		buffer.append("ctmc\n\n");
		for (Rule rule : stateSpace.getRules()) {
			buffer.append("const double " + getRateName(rule) + (rates.containsKey(rule) ? 
						  " = " + rates.get(rule)+";\n" : ";\n"));
		}
		buffer.append("\nmodule Transformation\n\n");
		buffer.append("\ts : [0.." + stateSpace.getStates().size() + "] init " + stateSpace.getInitialStates().get(0).getIndex() + ";\n\n");
		for (State s : stateSpace.getStates()) {
			for (Transition t : s.getOutgoing()) {
				buffer.append("\t[" + getRuleName(t.getRule())+ "] s=" + s.getIndex() + 
							  " -> " + getRateName(t.getRule()) + 
							  " : (s'=" + t.getTarget().getIndex() + ");\n");
			}
		}
		buffer.append("endmodule\n");
		String content = buffer.toString();
		
		// Dump the content into a temporary file.
		String filename = stateSpace.eResource()!=null ? 
				stateSpace.eResource().getURI().trimFileExtension().lastSegment() : "statespace";
		return createTempFile(filename, ".sm", content);

	}
	
	/* 
	 * Initialize properties.
	 */
	protected void initializeProperties(StateSpace stateSpace, IFile file) throws Exception {
		properties = new Properties();
		for (Rule rule : stateSpace.getRules()) {
			properties.setProperty(getRateName(rule),"1");
		}
		
		// PRISM path and arguments
		if (isWindows()) {
			properties.setProperty(PRISM_PATH_KEY, "C:\\prism");
		}
		properties.setProperty(PRISM_ARGS_KEY, "-fixdl -gaussseidel");
		
		// Save them.
		OutputStream out = new FileOutputStream(file.getLocation().toFile());
		properties.store(out, "State space properties file");
		file.getParent().refreshLocal(2, new NullProgressMonitor());
	}
	
	/*
	 * Load the properties from a file.
	 */
	protected void loadProperties(IFile file) throws Exception{
		properties = new Properties();
		properties.load(file.getContents());
	}
	
	/*
	 * Get the canonical properties file.
	 */
	protected static IFile getPropertiesFile(StateSpace stateSpace) {
		URI uri = stateSpace.eResource().getURI()
					.trimFileExtension().appendFileExtension("properties");
		IPath path;
		if (uri.isPlatform()) {
			path = new Path(uri.toPlatformString(true));
		} else {
			path = new Path(uri.toFileString());
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}
	
	/*
	 * Canonical name for rules.
	 */
	protected static String getRuleName(Rule rule) {
		return rule.getName().trim();
	}
	
	/*
	 * Canonical rate names.
	 */
	protected static String getRateName(Rule rule) {
		return "rate" + capitalize(getRuleName(rule));
	}
	
	/*
	 * Get the name of the PRISM executable.
	 */
	protected static String getPRISMExecutable() {
		return isWindows() ? "prism.bat" : "prism";
	}
	
	/*
	 * Check whether the OS is Windows.
	 */
	private static boolean isWindows() {
		return Platform.getOS()==Platform.OS_WIN32 || "win64".equalsIgnoreCase(Platform.getOS());
	}
	
	/*
	 * Capitalize a string.
	 */
	protected static String capitalize(String string) {
		if (string==null || string.length()==0) return string;
		String first = string.substring(0,1).toUpperCase();
		if (string.length()==0) return first;
		else return first + string.substring(1);
	}
	
}
