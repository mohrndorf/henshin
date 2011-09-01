package org.eclipse.emf.henshin.statespace.external.prism;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.statespace.State;
import org.eclipse.emf.henshin.statespace.StateSpace;
import org.eclipse.emf.henshin.statespace.StateSpaceExporter;
import org.eclipse.emf.henshin.statespace.Transition;

/**
 * Exporter for PRISM. This generates a CTMC model.
 * @author Christian Krause
 */
public class PRISMStateSpaceExporter implements StateSpaceExporter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.export.StateSpaceExporter#export(org.eclipse.emf.henshin.statespace.StateSpace, org.eclipse.emf.common.util.URI, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void export(StateSpace stateSpace, URI uri, IProgressMonitor monitor) throws IOException {

		monitor.beginTask("Exporting state space...", stateSpace.getTransitionCount());

		// Export to file...
		File file = new File(uri.toFileString());
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 65536);
		OutputStreamWriter writer = new OutputStreamWriter(out);
		
		// Output the header:
		writer.write("// CTMC model generated by Henshin on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + "\n\n");
		writer.write("ctmc\n\n");
		for (Rule rule : stateSpace.getRules()) {
			writer.write("const double " + RatesPropertiesManager.getRateKey(rule) + ";\n");
		}
		
		// Generate module specification:
		writer.write("\nmodule Transformation\n\n");
		writer.write("\ts : [0.." + stateSpace.getStates().size() + "];\n\n");
		
		// Output the transitions:
		for (State s : stateSpace.getStates()) {
			for (Transition t : s.getOutgoing()) {
				
				// Ouput the transition:
				writer.write("\t[" + getAction(t.getRule())+ "] s=" + s.getIndex() + 
							  " -> " + RatesPropertiesManager.getRateKey(t.getRule()) + 
							  " : (s'=" + t.getTarget().getIndex() + ");\n");
				
				// Update the monitor:
				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
		}
		writer.write("\nendmodule\n\n");
		
		// Initial states
		writer.write("init\n\t");
		for (int i=0; i<stateSpace.getInitialStates().size(); i++) {
			writer.write("s=" + stateSpace.getInitialStates().get(i).getIndex());
			if (i<stateSpace.getInitialStates().size()-1) writer.write(" | ");
		}
		writer.write("\nendinit\n");

		
		// Finished:
		writer.close();
		if (!monitor.isCanceled()) {
			monitor.done();
		}
		
	}
	
	/*
	 * Canonical name for rules.
	 */
	protected static String getAction(Rule rule) {
		return rule.getName().trim();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.export.StateSpaceExporter#getName()
	 */
	@Override
	public String getName() {
		return "PRISM";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.export.StateSpaceExporter#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { "sm" };
	}

}
