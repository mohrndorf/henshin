/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University of Berlin, 
 * University of Marburg and others. All rights reserved. 
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CWI Amsterdam - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.statespace.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.henshin.statespace.State;
import org.eclipse.emf.henshin.statespace.StateSpace;
import org.eclipse.emf.henshin.statespace.Trace;
import org.eclipse.emf.henshin.statespace.Transition;

/**
 * State space search implementation.
 * @generated NOT
 * @author Christian Krause
 */
public class StateSpaceSearch {
	
	// Visited states.
	private final Set<State> visited = new HashSet<State>();
	
	// Current path.
	private Trace path;
	
	// Current state.
	private State state;
	
	/**
	 * Visit a state and check whether the search should be stopped.
	 * @param Current state.
	 * @param path Path from one of the start states to the current state.
	 * @return <code>true</code> if the search should stop.
	 */
	protected boolean shouldStop(State state, Trace path) {
		// By default we never stop searching.
		return false;
	}
	
	/* ------ Depth-first search ------- */
	
	/**
	 * Perform a depth-first search.
	 * @param states Start states.
	 * @param reverse Flag indicating if the traversal should be in reverse direction.
	 */
	public synchronized boolean depthFirst(List<State> states, boolean reverse) {
		visited.clear();
		for (State state : states) {
			if (depthFirst(state, reverse)) return true;
		}
		return false;
	}

	/**
	 * Perform a depth-first search, starting at the initial states.
	 * @param stateSpace State space.
	 */
	public boolean depthFirst(StateSpace stateSpace, boolean reverse) {
		return depthFirst(stateSpace.getInitialStates(), reverse);
	}

	/**
	 * Perform a depth-first search.
	 * @param state Start state.
	 */
	public boolean depthFirst(State state, boolean reverse) {
		this.state = state;
		this.path = new Trace();
		return depthFirst(reverse);
	}
	
	/*
	 * Perform a depth-first search. Note that this does NOT clear the visited states.
	 */
	private boolean depthFirst(boolean reverse) {
		
		// Visited already or finished?
		if (visited(state)) return false;
		if (shouldStop(state, path)) return true;
		
		// Get the next transitions:
		List<Transition> transitions = getNextTransitions(state, reverse);
		
		// Nowhere to go from here? Otherwise add the first transition to the empty path:
		if (transitions.isEmpty()) return false;
		path.add(transitions.get(0));
		
		// Search until the path is empty:
		while (!path.isEmpty()) {
			
			// Transition, current and next state:
			Transition transition = reverse ? path.getFirst() : path.getLast();
			State previous = reverse ? transition.getTarget() : transition.getSource();
			state = reverse ? transition.getSource() : transition.getTarget();
			
			// This will be our next transition:
			Transition nextTransition = null;
			
			// If visited already, switch to the next transition:
			if (visited(state)) {
				
				// Remove the current transition from the path:
				if (reverse) path.removeFirst();
				else path.removeLast();
				
				// Index of the current transition:
				transitions = getNextTransitions(previous, reverse);
				int index = transitions.indexOf(transition);
				
				// More transitions?
				if (index+1 < transitions.size()) {
					nextTransition = transitions.get(index+1);
				}
				
			}
			
			// Should we stop here because the search was successful?
			else if (shouldStop(state, path)) {
				return true;
			}
			
			// Otherwise go further depth-first:
			else {
				
				// Take the first transition of the next state (depth-first):
				transitions = getNextTransitions(state, reverse);
				if (!transitions.isEmpty()) {
					nextTransition = transitions.get(0);
				}
				
			}

			// Add the next transition to the path:
			if (nextTransition!=null) {				
				if (reverse) path.addFirst(nextTransition);
				else path.addLast(nextTransition);
			}
			
		}
		
		// Not found:
		return false;
		
	}
	
	/*
	 * Get the next transitions to be used in the search.
	 */
	private List<Transition> getNextTransitions(State state, boolean reverse) {
		return reverse ? state.getIncoming() : state.getOutgoing();
	}

	/*
	 * Visit a state and mark it as visited.
	 */
	private boolean visited(State state) {
		if (visited.contains(state)) return true;
		visited.add(state);
		return false;
	}
	
	/**
	 * Remove all unreachable states from a state space.
	 * @param stateSpace State space.
	 */
	public static List<State> removeUnreachableStates(StateSpace stateSpace) {
		
		// Search the state space.
		StateSpaceSearch search = new StateSpaceSearch();
		search.depthFirst(stateSpace,false);
		
		// Remove states that have not been visited:
		List<State> states = stateSpace.getStates();
		List<State> removed = new ArrayList<State>();
		Set<State> visited = search.getVisitedStates();
		
		// Number of states that were not reached:
		int numUnreached = states.size() - visited.size();
		if (numUnreached==0) return removed;
		
		// Check all states:
		for (int i=0; i<states.size(); i++) {
			
			State state = states.get(i);
			
			if (!visited.contains(state)) {
				
				// Remove the state from the state space.
				stateSpace.removeState(state);
				removed.add(state);
				
				// Check if we removed all:
				if (removed.size()==numUnreached) break;
				
				// Otherwise continue:
				i--;
			}
		}
		return removed;
		
	}

	/**
	 * Get the set of visited states during the last search.
	 * @return Visited states.
	 */
	public Set<State> getVisitedStates() {
		return visited;
	}
	
	/**
	 * Get the current state.
	 * @return State.
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Get the current path.
	 * @return Current path.
	 */
	public Trace getPath() {
		return path;
	}
}
