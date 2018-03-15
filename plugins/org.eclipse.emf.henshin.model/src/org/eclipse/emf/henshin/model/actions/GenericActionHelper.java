/**
 * <copyright>
 * Copyright (c) 2010-2014 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.model.actions;

import static org.eclipse.emf.henshin.model.Action.Type.CREATE;
import static org.eclipse.emf.henshin.model.Action.Type.DELETE;
import static org.eclipse.emf.henshin.model.Action.Type.FORBID;
import static org.eclipse.emf.henshin.model.Action.Type.PRESERVE;
import static org.eclipse.emf.henshin.model.Action.Type.REQUIRE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Action.Type;
import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.GraphElement;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.MappingList;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.util.HenshinEditHelper;

/**
 * Generic action helper class.
 * @author Christian Krause
 */
public abstract class GenericActionHelper<E extends GraphElement,C extends EObject> implements ActionHelper<E,C> {
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.diagram.edit.actions.ActionHelper#getAction(java.lang.Object)
	 */
	public Action getAction(E element) {
		
		// Get the graph and the rule:
		Graph graph = element.getGraph();
		if (graph==null) {
			return null;
		}
		Rule rule = graph.getRule();
		if (rule==null) {
			return null;
		}
		
		// Get the kernel rule, if existing:
		Rule kernel = rule.getKernelRule();
		
		// Check if the element is amalgamated:
		boolean isMulti = isMulti(element);
		
		// Get the path:
		String[] multiPath = isMulti ? getMultiPath(element, rule) : null;
		
		// If the rule is a multi-rule, but the action is not
		// a multi-action, the element is not an action element.
		if (kernel!=null && !isMulti) {
			return null;
		}
		
		// Map editor.
		MapEditor<E> editor;
		
		// LHS element?
		if (graph==rule.getLhs()) {
			// Try to get the image in the RHS:
			editor = getMapEditor(rule.getRhs());
			E image = editor.getOpposite(element);
			
			// Check if it is mapped to the RHS:
			if (image!=null) {
				return new Action(PRESERVE, isMulti, multiPath);
			} else {
				return new Action(DELETE, isMulti, multiPath);
			}
		}
		
		// RHS element?
		else if (graph==rule.getRhs()) {
			// Try to get the origin in the LHS:
			editor = getMapEditor(rule.getRhs());
			E origin = editor.getOpposite(element);
			
			// If it has an origin in the LHS, it is a CREATE-action:
			if (origin==null) {
				return new Action(CREATE, isMulti, multiPath);
			}
		}
		
		// PAC/NAC element?
		else if (graph.eContainer() instanceof NestedCondition) {
			
			// Find out whether it is a PAC, a NAC or something else:
			NestedCondition nc = (NestedCondition) graph.eContainer();
			Type type = null;
			if (nc.isPAC()) {
				type = REQUIRE;
			} else if (nc.isNAC()) {
				type = FORBID;
			}

			// If we know the type, we can continue:
			if (type!=null) {
				
				// Try to get the origin in the LHS:
				editor = getMapEditor(graph);
				E origin = editor.getOpposite(element);

				// If it has an origin in the LHS, it is a PAC/NAC-action:
				if (origin==null) {
					return new Action(type, isMulti, multiPath, graph.getName());
				}
			}
		}
					
		// At this point we know it is not considered as an action element.
		return null;
		
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.henshin.diagram.edit.actions.ActionHelper#setAction(java.lang.Object, org.eclipse.emf.henshin.diagram.edit.actions.Action)
	 */
	public void setAction(E element, Action newAction) {
		
		// Check the current action.
		Action oldAction = getAction(element);
		if (oldAction==null) return; // illegal
		if (newAction.equals(oldAction)) return; // nothing to do
		Type oldType = oldAction.getType();
		Type newType = newAction.getType();
		
		// Get the container graph and rule.
		Graph graph = element.getGraph();
		Rule rule = graph.getRule();

		// Get mapping:
		MappingList mappings = rule.getMappings();
		GraphElement origin = (element.getGraph().isLhs()) ? element : mappings.getOrigin(element);
		GraphElement image = (element.getGraph().isRhs()) ? element : mappings.getImage(element, element.getGraph().getRule().getRhs());

		// Current action type = PRESERVE?
		if (oldType==PRESERVE) {
			
			// For DELETE actions, delete the image in the RHS:
			if (newType==DELETE) {
				HenshinEditHelper.remove(image);
			}
			
			// For CREATE actions, replace the image in the RHS by the origin:
			else if (newType==CREATE) {
				HenshinEditHelper.remove(origin);
			}
			
			// For REQUIRE / FORBID actions, delete the image in the RHS and move the node to the AC:
			else if (newType==REQUIRE || newType==FORBID) {
				NestedCondition ac = getOrCreateAC(newAction, rule);
				HenshinEditHelper.merge(origin, image);
				HenshinEditHelper.move(ac.getConclusion(), origin);
			} 
		}
		
		// Current action type = CREATE?
		else if (oldType==CREATE) {
			
			// We move the element to the LHS if the action type has changed:
			if (newType==DELETE) {
				HenshinEditHelper.move(rule.getLhs(), image);
			}
			
			// For NONE actions, create a copy of the element in the RHS and map to it:
			else if (newType==PRESERVE) {
				origin = HenshinEditHelper.copy(rule.getLhs(), image);
				HenshinEditHelper.add(rule.getLhs(), origin);
				HenshinEditHelper.map(origin, image);
			}
			
			// For REQUIRE / FORBID actions, move the element further to the AC:
			else if (newType==REQUIRE || newType==FORBID) {
				NestedCondition ac = getOrCreateAC(newAction, rule);
				HenshinEditHelper.move(ac.getConclusion(), image);
			}	
		}

		// Current action type = DELETE?
		else if (oldType==DELETE) {
			
			// For CREATE actions, move the element to the RHS:
			if (newType==CREATE) {
				HenshinEditHelper.move(rule.getRhs(), origin);
			}
			
			// For PRESERVE actions, create a copy of the element in the RHS and map to it:
			else if (newType==PRESERVE) {
				image = HenshinEditHelper.copy(rule.getRhs(), origin);
				HenshinEditHelper.add(rule.getRhs(), image);
				HenshinEditHelper.map(element, image);
			}
			
			// For FORBID actions, move the element to the NAC:
			else if (newType==REQUIRE ||  newType==FORBID) {
				NestedCondition ac = getOrCreateAC(newAction, rule);
				HenshinEditHelper.move(ac.getConclusion(), origin);
			}	
		}		
		
		// Current action type = REQUIRE or FORBID?
		else if ((oldType==REQUIRE || oldType==FORBID) && 
				 (oldType!=newType || !oldAction.hasSameFragment(newAction))) {
			
			// For PRESERVE actions, create a copy in the RHS as well:
			if (newType==PRESERVE) {
				HenshinEditHelper.move(rule.getLhs(), origin);
				
				image = HenshinEditHelper.copy(rule.getRhs(), origin);
				HenshinEditHelper.add(rule.getRhs(), image);
				HenshinEditHelper.map(element, image);
			}
			
			// For CREATE actions, move the element to the RHS:
			else if (newType==CREATE) {
				HenshinEditHelper.move(rule.getRhs(), element);
			}
			
			// For DELETE actions, move the element to the LHS:
			else if (newType==DELETE) {
				HenshinEditHelper.move(rule.getLhs(), element);
			}
			
			// For REQUIRE and FORBID actions, move the element to the new AC:
			else if (newType==REQUIRE || newType==FORBID) {
				NestedCondition newAc = getOrCreateAC(newAction, rule);
				HenshinEditHelper.move(newAc.getConclusion(), element);
			}
		}
		
		// THE ACTION TYPE AND THE FRAGMENT ARE CORRECT NOW.
			
		// Does the new action have a different path?
		if (!oldAction.hasSamePath(newAction)) {
			HenshinEditHelper.unmap(origin, image);
			Rule multi = getOrCreateMultiRule(rule.getRootRule(), newAction);
			HenshinEditHelper.move(multi.getLhs(), origin);
			HenshinEditHelper.move(multi.getRhs(), image);
			HenshinEditHelper.map(origin, image);
		}
	}
	
	/*
	 * Create a new map editor for a given target graph.
	 */
	protected abstract MapEditor<E> getMapEditor(Graph target);

	/*
	 * Create a new map editor for a given source, target graph and mappings.
	 */
	protected abstract MapEditor<E> getMapEditor(Graph source, Graph target, MappingList mappings);

	/*
	 * Returns a list of all elements of <code>elements</code>, which are
	 * associated with the given <code>action</code>. If <code>action</code> is
	 * null, the returned list contains all elements of the given list.
	 */
	protected List<E> filterElementsByAction(List<E> elements, Action action) {
		
		// Collect all matching elements:
		List<E> result = new ArrayList<E>();
		for (E element : elements) {
			
			// Check if the current action is ok and add it:
			Action current = getAction(element);
			if (current!=null && (action==null || action.equals(current))) {
				result.add(element);
			}
			
		}
		return result;
		
	}
	
	/*
	 * Helper method for checking whether the action of an element
	 * is a multi-action.
	 */
	private boolean isMulti(E element) {
		GraphElement elem;
		if (element instanceof Attribute) {
			elem = ((Attribute) element).getNode();
		} else if (element instanceof GraphElement) {
			elem = (GraphElement) element;
		} else {
			return false;
		}
		Graph graph = elem.getGraph();
		if (graph==null) {
			return false;
		}
		Rule rule = graph.getRule();
		if (rule==null || rule.getKernelRule()==null) {
			return false;
		}
		if (rule.getMultiMappings().getOrigin(element)!=null) {
			return false;
		}
		return true;
	}
	
	/*
	 * If an element has a multi-action, this method
	 * returns the proper path for the multi-action.
	 */
	private String[] getMultiPath(E element, Rule multiRule) {
		if (!isMulti(element)) {
			return null;
		}
		List<String> path = new ArrayList<String>();
		while (multiRule.isMultiRule()) {
			String name = multiRule.getName();
			path.add(name==null ? "" : name.trim());
			multiRule = multiRule.getKernelRule();
		}
		if (path.size()==1 && path.get(0).length()==0) {
			return new String[] {};
		}
		Collections.reverse(path);
		return path.toArray(new String[0]);
	}
	
	
	private Rule getOrCreateMultiRule(Rule root, Action action) {
		
		// Must be a multi-action:
		if (!action.isMulti()) {
			return null;
		}

		// Get the names of the multi-rules (must be a modifiable list):
		List<String> path = new ArrayList<String>(Arrays.asList(action.getPath()));
		if (path.isEmpty()) {
			path.add(null);
		}
		
		// Find or create the multi-rules:
		Rule rule = root.getRootRule(); // really make sure we start with the root rule
		for (String name : path) {
			Rule multi = rule.getMultiRule(name);
			if (multi==null) {
				multi = HenshinFactory.eINSTANCE.createRule(name);
				if (name==null || name.trim().length()==0) {
					rule.getMultiRules().add(0, multi);
				} else {
					rule.getMultiRules().add(multi);
				}
			}
			
			// Ensure completeness:
			new MultiRuleMapEditor(rule, multi).ensureCompleteness();
			
			rule = multi;
		}
		return rule;
			
	}

	/**
	 * Find or create a positive or a negative application condition.
	 * @param action	FORBID/REQUIRE action
	 * @param rule		Rule
	 * @return the application condition.
	 */
	protected NestedCondition getOrCreateAC(Action action, Rule rule) {
		
		// Check if the action type is ok:
		if (action.getType() != FORBID && action.getType() != REQUIRE) {
			throw new IllegalArgumentException("Application conditions can be created only for REQUIRE/FORBID actions");
		}
		
		// Get the name of the application condition:
		String name = action.getFragment();
		
		// Find or create the application condition:
		return getOrCreateAC(rule, name, action.getType()==REQUIRE);
		
	}

	protected NestedCondition getOrCreateAC(Rule rule, String name, boolean isPAC) {
		NestedCondition ac = isPAC ? rule.getLhs().getPAC(name) : rule.getLhs().getNAC(name);
		if (ac==null) {
			ac = isPAC ? rule.getLhs().createPAC(name) : rule.getLhs().createNAC(name);
		}
		return ac;
	}

}
