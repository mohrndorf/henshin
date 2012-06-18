package org.eclipse.emf.henshin.statespace.external.prism;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.statespace.State;
import org.eclipse.emf.henshin.statespace.StateSpace;
import org.eclipse.emf.henshin.statespace.StateSpacePlugin;
import org.eclipse.emf.henshin.statespace.Transition;
import org.eclipse.emf.henshin.statespace.external.AbstractStateSpaceExporter;
import org.eclipse.emf.henshin.statespace.external.prism.PRISMUtil.Range;
import org.eclipse.emf.henshin.statespace.tuples.SimpleTupleGenerator;
import org.eclipse.emf.henshin.statespace.tuples.Tuple;
import org.eclipse.emf.henshin.statespace.tuples.TupleUtil;

/**
 * Exporter for PRISM. This generates a MDP model.
 * @author Christian Krause
 */
public class MDPStateSpaceExporter extends AbstractStateSpaceExporter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.export.StateSpaceExporter#export(org.eclipse.emf.henshin.statespace.StateSpace, org.eclipse.emf.common.util.URI, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void export(StateSpace stateSpace, URI uri, String parameters, IProgressMonitor monitor) throws IOException {

		int stateCount = stateSpace.getStates().size();
		monitor.beginTask("Exporting state space...", 3 * stateCount);
		
		// Shall we produce an explicit model?
		boolean explicit = "tra".equalsIgnoreCase(uri.fileExtension());
		
		// Generate the tuples:
		tuples = TupleUtil.generateTuples(new SimpleTupleGenerator(), index, true, new SubProgressMonitor(monitor,stateCount));

		// Get the probabilistic rules:
		Map<String, List<Rule>> probRules = PRISMUtil.getProbabilisticRules(stateSpace);

		// Export to file...
		File file = new File(uri.toFileString());
		OutputStreamWriter writer = createWriter(file);

		// Get the probability constants:
		Map<String, String> probs = PRISMUtil.getAllProbs(stateSpace, explicit);

		// Output the header and constants:
		if (!explicit) {			
			writer.write(PRISMUtil.getModelHeader("mdp"));
			for (String ruleName : probRules.keySet()) {
				List<Rule> rules = probRules.get(ruleName);
				if (rules.size()>1) {
					for (int i=0; i<rules.size(); i++) {
						String key = PRISMUtil.getProbKey(rules.get(i), i);
						String value = probs.get(key);
						writer.write("const double " + key);
						if (value!=null && !Range.isRange(value)) {
							writer.write(" = " + value);
						}
						writer.write(";\n");
					}
				}
			}
			writer.write("\nmodule " + uri.trimFileExtension().lastSegment() + "\n\n");
		}
		
		// State and transition count:
		if (explicit) {
			writer.write(stateCount + " " + stateSpace.getTransitionCount() + "\n");
		} else {
			writer.write(PRISMUtil.getVariableDeclarations(tuples, false));
		}

		// Output the transitions:
		int removedIllegal = 0;
		for (State s : stateSpace.getStates()) {
			
			// Sort transitions by labels:
			Map<MDPLabel, List<Transition>> trs = MDPLabel.getTransitionsByLabel(s);
			int transitionIndex = 0;
			for (MDPLabel l : trs.keySet()) {
				List<Transition> ts = trs.get(l);
				if (ts.isEmpty()) continue;
				
				// Check if all rules are enabled:
				String label = l.getTransition().getRule().getName();
				List<Rule> rules = probRules.get(label);
				boolean allEnabled = true;
				for (Rule r : rules) {
					boolean enabled = false;
					for (Transition t : ts) {
						if (t.getRule()==r) {
							enabled = true;
							break;
						}
					}
					if (!enabled) {
						allEnabled = false;
						break;
					}
				}
				if (!allEnabled) {
					removedIllegal++;
					continue;
				}
				
				// Output the transition:
				if (!explicit) {
					writer.write("\t[" + label + "] " + PRISMUtil.getPRISMState(tuples.get(s.getIndex()), false) + " -> ");
				}

				boolean first = true;
				for (Transition t : ts) {
					if (!first) {
						writer.write(explicit ? "\n" : " + ");
					}
					String probKey = PRISMUtil.getProbKey(t.getRule(), rules.indexOf(t.getRule()));
					if (explicit) {
						String prob = (rules.size()>1) ? probs.get(probKey) : "1";
						writer.write(s.getIndex() + " " + transitionIndex + " " + t.getTarget().getIndex() + " " + prob);
					} else {
						String prob = (rules.size()>1) ? probKey+":" : "";
						writer.write(prob + PRISMUtil.getPRISMState(tuples.get(t.getTarget().getIndex()), true));
					}
					first = false;
				}
				
				if (explicit) {
					writer.write("\n");
					transitionIndex++;
				} else {				
					writer.write(";\n");
				}
			}
			
			// Update the monitor:
			monitor.worked(1);
			if (monitor.isCanceled()) {
				break;
			}

		}
		
		// Did we remove any illegal transitions?
		if (removedIllegal>0) {
			StateSpacePlugin.INSTANCE.logWarning("Removed " + removedIllegal + " illegal probabilistic transitions");
		}

		// Initial states
		if (!explicit) {
			writer.write("\nendmodule\n\n");
			writer.write("init\n\t");
			for (int i=0; i<stateSpace.getInitialStates().size(); i++) {
				Tuple t = tuples.get(stateSpace.getInitialStates().get(i).getIndex());
				writer.write(PRISMUtil.getPRISMState(t, false));
				if (i<stateSpace.getInitialStates().size()-1) writer.write(" | ");
			}
			writer.write("\nendinit\n");
		}

		// State labels:		
		if (parameters!=null) {
			try {
				String expanded = PRISMUtil.expandLabels(parameters, index, 
						tuples, new SubProgressMonitor(monitor, stateCount));
				if (explicit) {
					OutputStreamWriter labelsWriter = createWriter(new File(uri.toFileString().replaceAll(".tra", ".lab")));
					labelsWriter.write(expanded);
					labelsWriter.close();
				} else {
					writer.write("\n" + expanded + "\n");
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		
		// States file:
		if (explicit) {
			OutputStreamWriter statesWriter = createWriter(new File(uri.toFileString().replaceAll(".tra", ".sta")));
			statesWriter.write(PRISMUtil.getVariableDeclarations(tuples, true) + "\n");					
			for (int i=0; i<stateCount; i++) {
				statesWriter.write(i + ":" + tuples.get(i) + "\n");
			}
			statesWriter.close();
		}

		// Finished:
		writer.close();
		if (!monitor.isCanceled()) {
			monitor.done();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.export.StateSpaceExporter#getName()
	 */
	@Override
	public String getName() {
		return "PRISM MDP";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.export.StateSpaceExporter#getFileExtensions()
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { "nm", "tra" };
	}

}
