/**
 * <copyright>
 * Copyright (c) 2010-2012 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.examples.probbroadcast.timed;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.henshin.statespace.StateSpace;
import org.eclipse.emf.henshin.statespace.StateSpaceManager;
import org.eclipse.emf.henshin.statespace.external.prism.MDPStateSpaceValidator;
import org.eclipse.emf.henshin.statespace.impl.ModelImpl;
import org.eclipse.emf.henshin.statespace.impl.ParallelStateSpaceManager;
import org.eclipse.emf.henshin.statespace.impl.StateSpaceImpl;
import org.eclipse.emf.henshin.statespace.ocl.OCLStateValidator;
import org.eclipse.emf.henshin.statespace.resource.StateSpaceResourceSet;
import org.eclipse.emf.henshin.statespace.util.StateSpaceExplorationHelper;
import org.eclipse.emf.henshin.statespace.util.StateSpaceXYPlot;

/**
 * Timed version of the probabilistic broadcast example for wireless sensor networks.
 * This version contains a probabilistic waiting phase before the message sending.
 * 
 * @author Christian Krause
 */
public class TimedProbBroadcast2 {

	/**
	 * Relative path to the example files.
	 */
	public static final String PATH = "src/org/eclipse/emf/henshin/examples/probbroadcast/timed";
	
	static {
		// We need the OCL validator:
		OCLStateValidator.register(); 
	}
	
	/**
	 * Compute the message reception probabilities for a set of nodes and a fixed send probability. 
	 */
	public static void fixedSendProb(String path, String module, String initModel, int[] nodes, int CF, int LO, int UP, int DL, double probSend, double probWait) {

		// Initialize the state space:
		StateSpaceResourceSet resourceSet = new StateSpaceResourceSet(path);
		StateSpace stateSpace = new StateSpaceImpl(resourceSet.getModule(module, false));
		EMap<String, String> props = stateSpace.getProperties();

		// General properties:		
		props.put(StateSpace.PROPERTY_IDENTITY_TYPES, "Node,Message");  // identity types
		props.put(StateSpace.PROPERTY_CLOCK_DECLARATIONS, "Message.c,Node.d"); // clock declarations
		props.put(StateSpace.PROPERTY_CONSTANTS, "int LO=" + LO + ",int UP=" + UP + ",int CF=" + CF + ", DL=" + DL); // time bounds
		props.put(StateSpace.PROPERTY_USE_CLOCKS, "true"); // we need clocks

		// Probabilities:
		props.put("probSend1", probSend+"");              // send probability 
		props.put("probSend2", (1-probWait-probSend)+""); // probability for not sending
		props.put("probSend3", probWait+"");              // wait probability 

		// Guards:
		props.put("guardPrep", "LO<=u.c & u.c<=UP & v.c<=CF"); // guard for rule 'prep'
		props.put("guardDel1", "LO<=u.c & u.c<=UP & v.c>CF");  // guard for rule 'delete1'
		props.put("guardDel2", "LO<=u.c & u.c<=UP & v.c>CF");  // guard for rule 'delete2'
		props.put("guardSend", "m.d=DL"); // guard for rule 'send'

		// Clock resets:
		props.put("resetsPrep",  "m.d'=0"); // initial waiting phase
		props.put("resetsSend1", "w.c'=0"); // message sent
		props.put("resetsSend3", "m.d'=0"); // node wait more
		
		// Clock invariants:
		props.put("invariantPrep", "u.c<=UP");
		props.put("invariantSend", "m.d<=DL");

		// Create a state space manager and an MDP/PTA validator:
		StateSpaceManager manager = new ParallelStateSpaceManager(stateSpace);
		MDPStateSpaceValidator validator = new MDPStateSpaceValidator(manager);

		System.out.println("\n - Rules module: " + module);
		System.out.println(" - Initial model: "  + initModel);
		System.out.println(" - Constants: CF=" + CF + ", LO=" + LO + ", UP=" + UP + ", probSend="  + probSend + ", probWait="  + probWait);
		System.out.println(" - Computing reception probabilities...");

		try {
			
			// Create the initial state:
			manager.createInitialState(new ModelImpl(resourceSet.getResource(initModel)));
			
			// Check for each node:
			System.out.println("   Node\tStates\tPmin\tPmax\tTime");
			for (int i=0; i<nodes.length; i++) {
				System.out.print("   " + nodes[i] + "\t");

				String goalProperty = "OCL: not self.nodes->at(" + nodes[i] + ").active";
				props.put(StateSpace.PROPERTY_GOAL_PROPERTY, goalProperty);

				// Reset state space and regenerate:
				long time = System.currentTimeMillis();
				manager.resetStateSpace(false);
				new StateSpaceExplorationHelper(manager).doExploration(-1, new NullProgressMonitor());
				manager.mergeTerminalStates();
				System.out.print(stateSpace.getStateCount() + "\t");

				// Run PRISM:
				validator.setProperty("label \"goal\" = <<<" + goalProperty + ">>>;\n Pmin=?[F \"goal\"]  Pmax=?[F \"goal\"]\n");
				StateSpaceXYPlot plot = (StateSpaceXYPlot) validator.validate(manager.getStateSpace(), null).getResult();
				System.out.println(plot.getY(0,0) + "\t" + plot.getY(0,1) + "\t" + ((System.currentTimeMillis()-time) / 1000) + "s");

			}
			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			manager.shutdown();
		}

	}

	/**
	 * Compute the results for the probabilistic timed broadcast example.
	 * @param path Relative path to the model files.
	 */
	public static void run(String path) {
		fixedSendProb(path, "tpb2.henshin", "init-grid2x2.xmi", new int[] { 1,2,3,4 }, 4, 5, 16, 1, 0.8, 0.1);
		fixedSendProb(path, "tpb2.henshin", "init-grid3x3.xmi", new int[] { 2,4,3,5,7,6,8,9 }, 4, 5, 16, 1, 0.8, 0.1);
	}

	public static void main(String[] args) {
		run(PATH);
	}

}
