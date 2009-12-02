package org.eclipse.emf.henshin.statespace.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.statespace.State;
import org.eclipse.emf.henshin.statespace.StateSpace;
import org.eclipse.emf.henshin.statespace.StateSpaceManager;
import org.eclipse.emf.henshin.statespace.Transition;

/**
 * Default state space manager implementation.
 * 
 * @author Christian Krause
 * @generated NOT
 */
public class StateSpaceManagerImpl extends AbstractStateSpaceManagerWithIndex {
	
	/**
	 * Default memory usage: 10%
	 */
	public static final double DEFAULT_MEMORY_USAGE = 0.1;

	// Percentage of models that are kept in memory:
	private double memoryUsage = DEFAULT_MEMORY_USAGE;
	
	/**
	 * Default constructor.
	 */
	public StateSpaceManagerImpl(StateSpace stateSpace) {
		super(stateSpace);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.StateSpaceManager#getModel(org.eclipse.emf.henshin.statespace.State)
	 */
	public Resource getModel(State state) {
		
		// Model already set?
		if (state.getModel()!=null) {
			return state.getModel();
		}
		
		// Cached?
		Resource cached = getCache().get(state);
		if (cached!=null) {
			return cached;
		}
		
		// Otherwise derive the model:
		Resource model = null;

		

		// Decide whether the current model should be kept in memory:
		int states = getStateSpace().getStates().size();
		int stored = (int) (states * memoryUsage);			
		boolean storeCurrent = (stored>0) && (states % stored)==0;
		
		// Associated the model with the state (or not):
		state.setModel(storeCurrent ? model : null);
		
		// Always add it to the cache (is maintained automatically):
		getCache().put(state, model);
		
		// Done.
		return model;
		
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.statespace.StateSpaceManager#explore(org.eclipse.emf.henshin.statespace.State)
	 */
	public List<State> exploreState(State state) {

		// ----- DUMMY IMPLEMENTATION FOR TESTING ------------------------
		
		State created = createState(new ResourceImpl(), 0);
		int[] location = state.getLocation();
		location[1] += 100;
		created.setLocation(location);
		state.setOpen(false);
		created.setOpen(true);
		Transition transition = createTransition(state, "hi", 23);
		transition.setTarget(created);
		if (true) return null;
		
		// ---------------------------------------------------------------
		
		List<State> newStates = new ArrayList<State>();
		Resource model = getModel(state);
		
		// Find all matches:
		
		// Create a copy of the model.
		Resource transformed = new ResourceImpl();
		transformed.getContents().addAll(EcoreUtil.copyAll(model.getContents()));
		
		// Transform it:
		
		
		// Check if a corresponding state exists already:
		int hash = hashCode(transformed);
		if (getState(transformed, hash)!=null) {
			// Add an outgoing transition if not existent:
			
		} else {
			State newState = createState(transformed, hash);
			createTransition(newState, null, 0);
			
			// Remember the new state:
			newStates.add(newState);
			
		}
		
		// Mark the state as closed:
		if (state.isOpen()) {
			state.setOpen(false);
		}
		
		// Done.
		return newStates;
		
	}
	
	public void setMemoryUsage(double memoryUsage) {
		this.memoryUsage = Math.max(Math.min(memoryUsage,1),0);
	}

}
