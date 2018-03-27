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
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.merge;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.move;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.unmerge;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.update;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getKernelGraph;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getLHS;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiGraph;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.*;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getRHS;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.map;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.unmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.HenshinModelPlugin;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Action.Type;
import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.GraphElement;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.MappingList;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Rule;

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
		if ((oldAction==null) || (newAction==null)) return; // illegal
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
				merge(rule.getLhs(), origin, image);
			}
			
			// For CREATE actions, replace the image in the RHS by the origin:
			else if (newType==CREATE) {
				merge(rule.getRhs(), origin, image);
			}
			
			// For REQUIRE / FORBID actions, delete the image in the RHS and move the node to the AC:
			else if (newType==REQUIRE || newType==FORBID) {
				NestedCondition ac = getOrCreateAC(newAction, rule);
				merge(ac.getConclusion(), origin, image);
			} 
		}
		
		// Current action type = CREATE?
		else if (oldType==CREATE) {
			
			// We move the element to the LHS if the action type has changed:
			if (newType==DELETE) {
				move(rule.getLhs(), image);
			}
			
			// For NONE actions, create a copy of the element in the RHS and map to it:
			else if (newType==PRESERVE) {

				// NOTE: Do not delete the element representing the graphical element!
				unmerge(element, rule.getLhs(), rule.getRhs());
			}
			
			// For REQUIRE / FORBID actions, move the element further to the AC:
			else if (newType==REQUIRE || newType==FORBID) {
				NestedCondition ac = getOrCreateAC(newAction, rule);
				move(ac.getConclusion(), image);
			}	
		}

		// Current action type = DELETE?
		else if (oldType==DELETE) {
			
			// For CREATE actions, move the element to the RHS:
			if (newType==CREATE) {
				move(rule.getRhs(), origin);
			}
			
			// For PRESERVE actions, create a copy of the element in the RHS and map to it:
			else if (newType==PRESERVE) {
				unmerge(element, rule.getLhs(), rule.getRhs());
			}
			
			// For FORBID actions, move the element to the NAC:
			else if (newType==REQUIRE ||  newType==FORBID) {
				NestedCondition ac = getOrCreateAC(newAction, rule);
				move(ac.getConclusion(), origin);
			}	
		}		
		
		// Current action type = REQUIRE or FORBID?
		else if ((oldType==REQUIRE || oldType==FORBID) && 
				 (oldType!=newType || !oldAction.hasSameFragment(newAction))) {
			
			// For PRESERVE actions, create a copy in the RHS as well:
			if (newType==PRESERVE) {
				unmerge(element, rule.getLhs(), rule.getRhs());
			}
			
			// For CREATE actions, move the element to the RHS:
			else if (newType==CREATE) {
				move(rule.getRhs(), element);
			}
			
			// For DELETE actions, move the element to the LHS:
			else if (newType==DELETE) {
				move(rule.getLhs(), element);
			}
			
			// For REQUIRE and FORBID actions, move the element to the new AC:
			else if (newType==REQUIRE || newType==FORBID) {
				NestedCondition newAc = getOrCreateAC(newAction, rule);
				move(newAc.getConclusion(), element);
			}
		}
		
		// THE ACTION TYPE AND THE FRAGMENT ARE CORRECT NOW.
		
		// Update the current action:
		oldAction = getAction(element);

		// Is the old action a multi-action?
		if (oldAction.isMulti()) {
			
			// If the new one is not a multi-action, move the element up to the root rule:
			if (!newAction.isMulti()) {
				moveMultiElement(rule, rule.getRootRule(), newAction, element);
			}
			
			// Does the new action have a different path? (it IS a multi-action)
			else if (!oldAction.hasSamePath(newAction)) {
				
				// Find the common sub-path:
				String[] common = getCommonPath(oldAction, newAction);
				
				// If they are completely different, move it up to the root rule:
				if (common.length==0) {
					moveMultiElement(rule, rule.getRootRule(), newAction, element);
				}
				// Otherwise move it to the common parent rule:
				else {
					Action action = new Action(oldAction.getType(), true, common);
					Rule multi = getOrCreateMultiRule(rule.getRootRule(), action); 
					moveMultiElement(rule, multi, newAction, element);					
				}
			}
		}
		
		// Update the current action:
		oldAction = getAction(element);
		
		// Still not the same?
		if (oldAction!=null && !oldAction.equals(newAction)) {
			
			// Then find the new target multi-rule and move the element there:
			Rule multi = getOrCreateMultiRule(rule.getRootRule(), newAction);
			moveMultiElement(element.getGraph().getRule(), multi, newAction, element);
			
		}
		
		// update rules:
		update(rule);
		
		// NOW EVERYTHING SHOULD BE CORRECT.
		if (!newAction.equals(getAction(element))) {
			HenshinModelPlugin.INSTANCE.logWarning("Failed to set action for " + element + 
					" (got " + getAction(element) + " instead of " + newAction, null);
		}
	}
	
	private void setMultiAction(E element, Action action, Rule kernel, Rule multi) {
		Rule currentRule = element.getGraph().getRule();
		Type actionType = action.getType();

		if ((actionType == CREATE) || (actionType == DELETE)) {

			// move from kernel to multi-rule:
			if (currentRule == kernel) {
				Graph elementTargetGraph = getMultiGraph(multi, element.getGraph());
				merge(getMultiGraph(multi, element.getGraph()), element, getMappedGraphElement(elementTargetGraph, element, true));

			// move from multi to kernel-rule:
			} else if (currentRule == multi) {
				unmerge(element, getKernelGraph(kernel, element), element.getGraph());
			}
		}

		else if (actionType == PRESERVE) {
			GraphElement remote = getMappedGraphElement(getRemoteGraph(element.getGraph()), element, false);

			// move from kernel to multi-rule:
			if (currentRule == kernel) {

				// remove LHS/RHS-mapping(s):
				unmap(getLHS(element, remote), getRHS(element, remote));

				// move mapped nodes:
				Graph elementTargetGraph = getMultiGraph(multi, element.getGraph());
				merge(elementTargetGraph, element, getMappedGraphElement(elementTargetGraph, element, true));
				
				Graph remoteTargetGraph = getMultiGraph(multi, remote.getGraph());
				merge(remoteTargetGraph, remote, getMappedGraphElement(remoteTargetGraph, remote, true));
				
				// add LHS/RHS-mapping(s):
				map(getLHS(element, remote), getRHS(element, remote));

			// move from multi to kernel-rule:
			} else if (currentRule == multi) {
				 	
				// remove kernel LHS/RHS-mapping(s):
				unmap(getLHS(element, remote), getRHS(element, remote));

				// move nodes to kernel-rule, clone nodes to multi-rule:
				GraphElement multiElement = unmerge(element, getKernelGraph(kernel, element), element.getGraph());
				GraphElement multiRemote = unmerge(remote, getKernelGraph(kernel, remote), remote.getGraph());
				
				// create multi LHS/RHS-mapping(s):
				map(getLHS(multiElement, multiRemote), getRHS(multiElement, multiRemote));

				// create kernel LHS/RHS-mapping(s):
				map(getLHS(element, remote), getRHS(element, remote));
			}
		} else if (actionType == FORBID || actionType == REQUIRE) {
			
			// move from kernel to multi-rule:
			if (currentRule == kernel) {
				NestedCondition multiAC = getOrCreateAC(multi, action.getFragment(), actionType == REQUIRE);
				move(multiAC.getConclusion(), element);
			
			// move from multi to kernel-rule:
			} else if (currentRule == multi) {
				NestedCondition kernelAC = getOrCreateAC(kernel, action.getFragment(), actionType == REQUIRE);
				move(kernelAC.getConclusion(), element);
			}
		}
	}
	
	/*
	 * Get the common start of the path of two actions.
	 */
	private static String[] getCommonPath(Action a1, Action a2) {
		List<String> path = new ArrayList<String>();
		String[] p1 = a1.getPath();
		String[] p2 = a2.getPath();
		int max = Math.min(p1.length, p2.length);
		for (int i=0; i<max; i++) {
			if (p1[i].equals(p2[i])) {
				path.add(p1[i]);
			} else break;
		}
		return path.toArray(new String[0]);
	}
	
	/*
	 * Move an element either from a (multi-) rule to another (multi-) rule.
	 */
	private void moveMultiElement(Rule rule1, Rule rule2, Action action, E element) {

		// Nothing to do?
		if (rule1 == rule2)
			return;
		if (EcoreUtil.isAncestor(rule2, rule1)) {
			moveMultiElement(rule2, rule1, action, element);
			return;
		}

		// Now we know that rule2 is a direct or indirect child of rule1.

		// Build the rule chain (from rule1 to rule2):
		List<Rule> ruleChain = new ArrayList<Rule>();
		Rule rule = rule2;
		ruleChain.add(rule);
		
		while (rule != rule1 && rule != null) {
			rule = rule.getKernelRule();
			if (rule != null) {
				ruleChain.add(0, rule);
			}
		}

		// Find out from where to where we need to move the element:
		if (element.getGraph().getRule() == rule1) {
			// correct order already
		} else if (element.getGraph().getRule() == rule2) {
			Collections.reverse(ruleChain); // reverse the order
		} else {
			return; // something is wrong, so we stop
		}

		// The element is in the first rule of the rule chain.

		// Now move the element:
		for (int i = 1; i < ruleChain.size(); i++) {

			// The two 'adjacent' rules:
			Rule r1 = ruleChain.get(i - 1);
			Rule r2 = ruleChain.get(i);

			// Which one is the kernel rule, which the multi-rule?
			Rule kernel, multi;
			
			if (r2.getKernelRule() == r1) {
				kernel = r1;
				multi = r2;
			} else {
				kernel = r2;
				multi = r1;
			}

			// Decide what and how to move the element:
			setMultiAction(element, action, kernel, multi);
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
