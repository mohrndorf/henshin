/**
 * <copyright>
 * Copyright (c) 2010-2014 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.model.util;

import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getApplicationConditionGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getApplicationConditions;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getKernelGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getKernelRule;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiGraph;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiRules;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getRemoteGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getRemoteMappings;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.map;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.unmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.GraphElement;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;

/**
 * Function for editing mapped graphs.
 * 
 * @author Manuel Ohrndorf
 */
public class HenshinEditHelper {
	
	/**
	 * @param graph
	 *            The graph which should contain the node/edge.
	 * @param graphElement
	 *            The node/edge to add.
	 */
	public static void add(Graph graph, GraphElement graphElement) {
		if (graphElement instanceof Attribute) {
			throw new UnsupportedOperationException();
		}
		
		// update rule:
		if ((graph != null) && (graphElement != null)) {
			if (graphElement instanceof Node) {
				graph.getNodes().add((Node) graphElement);
			}
			
			else if (graphElement instanceof Edge) {
				graph.getEdges().add((Edge) graphElement);
			}
		}
	}
	
	/**
	 * @param targetNode
	 *            The node which should contain the attribute.
	 * @param attribute
	 *            The attribute to add.
	 */
	public static void add(Node targetNode, Attribute attribute) {
		
		// update rule:
		if ((targetNode != null) && (attribute != null)) {
			targetNode.getAttributes().add(attribute);
		}
	}
	
	/**
	 * @param graphElement
	 *            The element to remove.
	 */
	public static void remove(GraphElement graphElement) {
	
		// update application conditions:
		for (NestedCondition ac : getApplicationConditions(graphElement)) {
	
			// update mapped graph elements:
			GraphElement acGraphElement = getApplicationConditionGraphElement(ac.getConclusion(), graphElement, false);
	
			if (acGraphElement != null) {
				if (!workaround_applicationConditionContext(graphElement, acGraphElement)) {
					if (acGraphElement instanceof Node) {
						unmap(graphElement, acGraphElement);
					}
					remove(acGraphElement);
				}
			}
		}
	
		// update multi-rules:
		for (Rule multiRule : getMultiRules(graphElement)) {
			GraphElement multiGraphElement = getMultiGraphElement(graphElement, multiRule, false);
	
			if (multiGraphElement != null) {
				if (multiGraphElement instanceof Node) {
					unmap(graphElement, multiGraphElement);
				}
				remove(multiGraphElement);
			}
		}
		
		// update rule:
		if (graphElement instanceof Node) {
			unmap((Node) graphElement, getRemoteMappings(graphElement));
		}
	
		// handle dangling edges:
		if (graphElement instanceof Node) {
			fix_remove((Node) graphElement);
		}
	
		// remove element:
		EcoreUtil.delete(graphElement);
	}

	/**
	 * @param targetGraph
	 *            The graph of the copied graph element.
	 * @param graphElement
	 *            The element to copy.
	 * @return The copied graph element.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends GraphElement> E copy(Graph targetGraph, E graphElement) {
		
		// copy (without adding):
		if (graphElement instanceof Node) {
			Node node = (Node) graphElement;
			
			Node copy = HenshinFactory.eINSTANCE.createNode();
			copy.setDescription(node.getDescription());
			copy.setName(node.getName());
			copy.setType(node.getType());
			
			return (E) copy;
		}
		
		else if (graphElement instanceof Edge) {
			Edge edge = (Edge) graphElement;
			
			Edge copy = HenshinFactory.eINSTANCE.createEdge();
			copy.setIndex(edge.getIndex());
			copy.setType(edge.getType());
			
			// set source and target related to target graph:
			setEdgeContext(targetGraph, copy, edge);
			
			return (E) copy;
		}
		
		else if (graphElement instanceof Attribute) {
			Attribute attribute = (Attribute) graphElement;
			
			Attribute copy = HenshinFactory.eINSTANCE.createAttribute();
			copy.setType(attribute.getType());
			copy.setValue(attribute.getValue());
			
			return (E) copy;
		}
		
		return null;
	}
	
	/**
	 * @param targetGraph
	 *            The graph which should contain the given graph element.
	 * @param graphElement
	 *            The graph element to move.
	 */
	public static void move(Graph targetGraph, GraphElement graphElement)  {
	
		// update multi-rules:
		for (Rule multiRule : getMultiRules(graphElement)) {
			GraphElement multiGraphElement = getMultiGraphElement(graphElement, multiRule, false);
	
			if (multiGraphElement != null) {
				
				// move in multi-rule:
				if (targetGraph.getRule() == graphElement.getGraph().getRule()) {
					if (targetGraph.isLhs() || targetGraph.isRhs()) {
						Graph multiGraph = getMultiGraph(multiRule, targetGraph);
						move(multiGraph, multiGraphElement);
					}
				} 
				
				// move to application condition:
				if (targetGraph.isNestedCondition()) {
					// merge to LHS/RHS:
					merge(graphElement.getGraph(), graphElement, multiGraphElement);
				}
			}
		}
	
		// move node:
		if (graphElement instanceof Node) {
			
			// move element:
			Graph oldGraph = graphElement.getGraph();
			add(targetGraph, graphElement);
			
			// move edges (if not preserve node):
			if (!((graphElement.getGraph().isLhs() || graphElement.getGraph().isRhs()) && (getRemoteGraphElement(graphElement) != null))) {
				for (Edge outgoing : new ArrayList<>(((Node) graphElement).getOutgoing())) {
					move(targetGraph, outgoing);
				}
				
				for (Edge incoming : new ArrayList<>(((Node) graphElement).getIncoming())) {
					move(targetGraph, incoming);
				}
			} 
			
			// fix application condition edge context:
			else if (oldGraph.isNestedCondition()) {
				for (Edge outgoing : new ArrayList<>(((Node) graphElement).getOutgoing())) {
					move(oldGraph, outgoing);
				}
				
				for (Edge incoming : new ArrayList<>(((Node) graphElement).getIncoming())) {
					move(oldGraph, incoming);
				}
			}
		}
	
		// move edge:
		else if (graphElement instanceof Edge) {
			
			// move source and target ends:
			Edge edge = (Edge) graphElement;
			setEdgeContext(targetGraph, edge);
	
			// move element:
			add(targetGraph, graphElement);
		}
	
		// move attribute:
		else if (graphElement instanceof Attribute) {
			Attribute attribute = (Attribute) graphElement;
			setAttributeContext(targetGraph, attribute);
		}
	}
	
	/**
	 * @param targetGraph
	 *            The graph which will contain the <code>retained</code> graph element.
	 * @param retained
	 *            The graph element which will be retained.
	 * @param merged
	 *            The graph element which will be removed.
	 */
	public static void merge(Graph targetGraph, GraphElement retained, GraphElement merged) {
		
		if ((retained instanceof Node) && (merged instanceof Node)) {
			Node mergedNode = (Node) merged;
			Node retainedNode = (Node) retained;
				
			// merge multi-node(s):
			for (Rule multiRule : getMultiRules(mergedNode)) {
				Node multiMergedNode = getMultiGraphElement(mergedNode, multiRule, false);
				mergeMultiNode(mergedNode, multiMergedNode);
			}
	
			for (Rule multiRule : getMultiRules(retainedNode)) {
				Node multiRetainedNode = getMultiGraphElement(retainedNode, multiRule, false);
				mergeMultiNode(retainedNode, multiRetainedNode);
			}
			
			// mappings: update application conditions:
			for (NestedCondition ac : getApplicationConditions(mergedNode)) {
				for (Mapping mapping : ac.getMappings()) {
					if (mapping.getOrigin() == mergedNode) {
						mapping.setOrigin(retainedNode);
					}
				}
			}
			
			// mappings: update multi-rules:
			for (Rule multiRule : getMultiRules(mergedNode)) {
				for (Mapping mapping : multiRule.getMultiMappings()) {
					if (mapping.getOrigin() == mergedNode) {
						mapping.setOrigin(retainedNode);
					}
				}
			}
			
			// merge edges of nodes:
			for (Edge outgoing : new ArrayList<>(mergedNode.getOutgoing())) {
				
				// check if the edge already exists:
				if (retainedNode.getOutgoing(outgoing.getType(), getRemoteGraphElement(outgoing.getTarget())) == null) {
					move(retained.getGraph(), outgoing);
				} else {
					remove(outgoing);
				}
			}
			
			for (Edge incoming : new ArrayList<>(mergedNode.getIncoming())) {
				
				// check if the edge already exists:
				if (retainedNode.getIncoming(incoming.getType(), getRemoteGraphElement(incoming.getSource())) == null) {
					move(retained.getGraph(), incoming);
				} else {
					remove(incoming);
				}
			}
			
			// merge attributes of nodes:
			for (Attribute attribute : new ArrayList<>(mergedNode.getAttributes())) {
				if (retainedNode.getAttribute(attribute.getType()) == null) {
					move(retained.getGraph(), attribute);
				}
			}
		}
		
		// remove merged graph element:
		remove(merged);
		
		// move to merge destination graph:
		if (targetGraph != retained.getGraph()) {
			move(targetGraph, retained);
		}
	}

	/**
	 * @param original
	 *            The graph element to cloned and mapped.
	 * @param originalGraph
	 *            The graph which will contain the<code>original</code> graph element.
	 * @param cloneGraph
	 *            The graph which will contain the cloned graph element.
	 * @return The cloned element.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends GraphElement> E unmerge(E original, Graph originalGraph, Graph cloneGraph) {
		boolean moveOriginal = original.getGraph() != originalGraph;
		
		// Attributes
		if (original instanceof Attribute) {
			Node node = ((Attribute) original).getNode();
			Attribute clonedAttribtue = HenshinEditHelper.copy(cloneGraph, (Attribute) original);
			
			if (moveOriginal) {
				HenshinEditHelper.move(originalGraph, original);
				HenshinEditHelper.add(node, clonedAttribtue);
			} else {
				setAttributeContext(cloneGraph, (Attribute) original, clonedAttribtue);
			}
			
			return (E) clonedAttribtue;
			
		// Nodes and edges:
		} else {
			
			// clone graph element:
			GraphElement cloned = copy(cloneGraph, original);
			add(cloneGraph, cloned);
			
			// Nodes:
			if (original instanceof Node) {
				
				// unmerge in rule:
				if (originalGraph.getRule() == cloneGraph.getRule()) {
					
					if (originalGraph.isLhs()) {
						originalGraph.getRule().getMappings().add(original, cloned);
					} else if (originalGraph.isRhs()) {
						originalGraph.getRule().getMappings().add(cloned, original);
					} else if (originalGraph.isNestedCondition()) {
						((NestedCondition) originalGraph.eContainer()).getMappings().add(cloned, original);
					}
					
					if (moveOriginal) {
						move(originalGraph, original);
					}
	
				// unmerge multi to kernel-rule:
				} else {
	
					if (originalGraph.getRule().getKernelRule() == cloneGraph.getRule()) {
						originalGraph.getRule().getMultiMappings().add(cloned, original);
					} else {
						cloneGraph.getRule().getMultiMappings().add(original, cloned);
					}
					
					if (moveOriginal) {
						add(originalGraph, original);
					}
				}
				
				// check for fixes:
				if (originalGraph.isLhs()) {
					fix_edgeContext((Node) original, (Node) cloned);
					fix_applicationConditionContext((Node) original, (Node) cloned);
				} else {
					fix_edgeContext((Node) cloned, (Node) original);
					fix_applicationConditionContext((Node) cloned, (Node) original);
				}
			}
			
			else if (original instanceof Edge) {
				if (moveOriginal) {
					move(originalGraph, original);
				}
			}
			
			return (E) cloned;
		}
	}

	/**
	 * @param rule
	 *            The rule to be updated.
	 */
	public static void update(Rule rule) {
		
		try {
	
			// remove dangling edges/mappings:
			List<Edge> dangling = new ArrayList<>();
			List<Mapping> incompleteMappings = new ArrayList<>();
			
			for (Iterator<EObject> iterator = rule.eAllContents(); iterator.hasNext();) {
				EObject element = iterator.next();
				
				if (element instanceof Edge) {
					Edge edge = (Edge) element;
					
					if ((edge.getSource() == null) || (edge.getTarget() == null)) {
						dangling.add(edge);
					}
				}
				
				else if (element instanceof Mapping) {
					Mapping mapping = (Mapping) element;
					
					if ((mapping.getOrigin() == null) || (mapping.getImage() == null)) {
						incompleteMappings.add(mapping);
					}
				}
			}
			
			dangling.forEach(HenshinEditHelper::remove);
			incompleteMappings.forEach(EcoreUtil::remove);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// update multi-rule:
			for (Rule multiRule : rule.getMultiRules()) {
				HenshinModelCleaner.completeMultiRules(multiRule);
				
				// update application conditions:
				for (NestedCondition ac : multiRule.getLhs().getNestedConditions()) {
					updateNestedCondition(ac);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		try {
			// update application conditions:
			for (NestedCondition ac : rule.getLhs().getNestedConditions()) {
				updateNestedCondition(ac);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void fix_applicationConditionContext(Node origin, Node image) {
		
		for (NestedCondition ac : getApplicationConditions(origin)) {
			Node acNode = getApplicationConditionGraphElement(ac.getConclusion(), image, false);
			
			// remap application condition context to LHS:
			if (acNode != null) {
				unmap(image, acNode);
				map(origin, acNode);
			}
		}
	}
	
	protected static void fix_edgeContext(Node origin, Node image) {
		
		if ((origin != null) && (image != null)) {
			
			// move invalid image edge to origin:
			for (Edge outgoing : new ArrayList<>(image.getOutgoing())) {
				if ((outgoing.getSource() != null) && (outgoing.getTarget() != null)) {
					if (!isValid(outgoing)) {
						if (origin.getGraph() == outgoing.getTarget().getGraph()) {
							setEdgeSource(outgoing, origin);
							setEdgeGraph(outgoing, origin.getGraph());
						}
					}
				} else {
					EcoreUtil.delete(outgoing);
				}
			}
			for (Edge incoming : new ArrayList<>(image.getIncoming())) {
				if ((incoming.getSource() != null) && (incoming.getTarget() != null)) {
					if (!isValid(incoming)) {
						if (origin.getGraph() == incoming.getSource().getGraph()) {
							setEdgeTarget(incoming, origin);
							setEdgeGraph(incoming, origin.getGraph());
						}
					}
				} else {
					EcoreUtil.delete(incoming);
				}
			}
			
			// move invalid origin edge to image:
			for (Edge outgoing : new ArrayList<>(origin.getOutgoing())) {
				if ((outgoing.getSource() != null) && (outgoing.getTarget() != null)) {
					if (!isValid(outgoing)) {
						if (image.getGraph() == outgoing.getTarget().getGraph()) {
							setEdgeSource(outgoing, image);
							setEdgeGraph(outgoing, image.getGraph());
						}
					}
				} else {
					EcoreUtil.delete(outgoing);
				}
			}
			for (Edge incoming : new ArrayList<>(origin.getIncoming())) {
				if ((incoming.getSource() != null) && (incoming.getTarget() != null)) {
					if (!isValid(incoming)) {
						if (image.getGraph() == incoming.getSource().getGraph()) {
							setEdgeTarget(incoming, image);
							setEdgeGraph(incoming, image.getGraph());
						}
					}
				} else {
					EcoreUtil.delete(incoming);
				}
			}
		}
	}
	
	protected static void fix_remove(Node node) {
		
		// remove dangling edges:
		for (Edge outgoing : new ArrayList<>(node.getOutgoing())) {
			remove(outgoing);
		}
		for (Edge incoming : new ArrayList<>(node.getIncoming())) {
			remove(incoming);
		}
	}

	protected static boolean isValid(Edge edge) {
		return (edge.getSource() != null) && (edge.getTarget() != null) && (edge.getSource().getGraph() == edge.getGraph()) && (edge.getTarget().getGraph() == edge.getGraph());
	}
	
	protected static void mergeMultiNode(Node kernelNode, Node multiNode) {
			
		if (multiNode != null) {

			// merge edges of nodes:
			for (Edge outgoing : new ArrayList<>(multiNode.getOutgoing())) {
				if (getKernelGraphElement(outgoing, kernelNode.getGraph().getRule(), false) == null) {
					
					// check if the edge already exists:
					if (kernelNode.getOutgoing(outgoing.getType(), getRemoteGraphElement(outgoing.getTarget())) == null) {
						move(kernelNode.getGraph(), outgoing);
					}
				} else {
					remove(outgoing);
				}
			}

			for (Edge incoming : new ArrayList<>(multiNode.getIncoming())) {
				if (getKernelGraphElement(incoming, kernelNode.getGraph().getRule(), false) == null) {
					
					// check if the edge already exists:
					if (kernelNode.getIncoming(incoming.getType(), getRemoteGraphElement(incoming.getSource())) == null) {
						move(kernelNode.getGraph(), incoming);
					}
				}  else {
					remove(incoming);
				}
			}

			// merge attributes of nodes:
			for (Attribute attribute : new ArrayList<>(multiNode.getAttributes())) {
				if (getKernelGraphElement(attribute, kernelNode.getGraph().getRule(), false) == null) {
					
					// check if the attribute already exists:
					if (kernelNode.getAttribute(attribute.getType()) == null) {
						move(kernelNode.getGraph(), attribute);
					}
				} else {
					remove(attribute);
				}
			}
		}
	}
	
	protected static void setAttributeContext(Graph targetGraph, Attribute attribute) {
		setAttributeContext(targetGraph, attribute, attribute);

	}
	
	protected static void setAttributeContext(Graph targetGraph, Attribute originalAttribute,  Attribute copyAttribute) {
		
		// set to application condition:
		if (targetGraph.isNestedCondition()) {
			add(getApplicationConditionGraphElement(targetGraph, originalAttribute.getNode(), true), copyAttribute);
		}
		
		// set to multi-rule:
		else if (originalAttribute.getGraph().getRule().getMultiRules().contains(targetGraph.getRule())) {
			add(getMultiGraphElement(originalAttribute.getNode(), targetGraph.getRule(), true), copyAttribute);
		}
		
		// set to kernel-rule:
		else if (originalAttribute.getGraph().getRule().getKernelRule() == targetGraph.getRule()) {
			add(getKernelGraphElement(originalAttribute.getNode(), targetGraph.getRule(), true), copyAttribute);
		}
		
		// set to rule:
		else if (originalAttribute.getGraph().getRule() == targetGraph.getRule()) {
			
			if (originalAttribute.getGraph() != targetGraph) {

					// LHS/RHS to RHS/LHS:
					// application condition to LHS:
					Node remoteNode = getRemoteGraphElement(originalAttribute.getNode());
					
					// create remote element:
					if (remoteNode == null) {
						remoteNode = unmerge(originalAttribute.getNode(), originalAttribute.getGraph(), targetGraph);
					}
					
					// LHS to RHS
					if (targetGraph.isRhs() && remoteNode.getGraph().isLhs()) {
						remoteNode = getRemoteGraphElement(remoteNode);
					}
					
					add(remoteNode, copyAttribute);
			}
		}
	}
	
	protected static void setEdgeContext(Graph targetGraph, Edge edge) {
		setEdgeContext(targetGraph, edge, edge);
	}
	
	protected static void setEdgeContext(Graph targetGraph, Edge targetEdge, Edge remoteEdge) {
		
		// SOURCE:
		if ((targetEdge.getSource() == null) || (targetEdge.getSource().getGraph() == null) || (targetEdge.getSource().getGraph() != targetGraph)) {
			
			// set to application condition
			if (targetGraph.isNestedCondition()) {
				targetEdge.setSource(getApplicationConditionGraphElement(targetGraph, remoteEdge.getSource(), true));
			} 
			
			// set to multi-rule:
			else if (getMultiRules(remoteEdge.getSource()).contains(targetGraph.getRule())) {
				targetEdge.setSource(getMultiGraphElement(remoteEdge.getSource(), targetGraph.getRule(), true));
			}
			
			// set to kernel-rule:
			else if (getKernelRule(remoteEdge.getSource()) == targetGraph.getRule()) {
				targetEdge.setSource(getKernelGraphElement(remoteEdge.getSource(), targetGraph.getRule(), true));
			}

			// set to same graph:
			else if (remoteEdge.getSource().getGraph() == targetGraph) {
				targetEdge.setSource(remoteEdge.getSource());
			}
					
			// set to LHS/RHS:
			else if (remoteEdge.getSource().getGraph().getRule() == targetGraph.getRule()) {

				// LHS/RHS to RHS/LHS:
				// application condition to LHS:
				Node source = workaround_edgeContext(remoteEdge.getSource());

				// LHS to RHS
				if (targetGraph.isRhs() && source.getGraph().isLhs()) {
					source = workaround_edgeContext(source);
				}

				targetEdge.setSource(source);
			}
		}
		
		// TARGET:
		if ((targetEdge.getTarget() == null) || (targetEdge.getTarget().getGraph() == null) || (targetEdge.getTarget().getGraph() != targetGraph)) {
			
			// set to application condition
			if (targetGraph.isNestedCondition()) {
				targetEdge.setTarget(getApplicationConditionGraphElement(targetGraph, remoteEdge.getTarget(), true));
			} 
			
			// set to multi-rule:
			else if (getMultiRules(remoteEdge.getTarget()).contains(targetGraph.getRule())) {
				targetEdge.setTarget(getMultiGraphElement(remoteEdge.getTarget(), targetGraph.getRule(), true));
			}
			
			// set to kernel-rule:
			else if (getKernelRule(remoteEdge.getTarget()) == targetGraph.getRule()) {
				targetEdge.setTarget(getKernelGraphElement(remoteEdge.getTarget(), targetGraph.getRule(), true));
			}
			
			// set to same graph:
			else if (remoteEdge.getTarget().getGraph() == targetGraph) {
				targetEdge.setTarget(remoteEdge.getTarget());
			}
			
			// set to LHS/RHS:
			else if (remoteEdge.getTarget().getGraph().getRule() == targetGraph.getRule()) {
				
				// LHS/RHS to RHS/LHS:
				// application condition to LHS:
				Node target = workaround_edgeContext(remoteEdge.getTarget());
				
				// LHS to RHS
				if (targetGraph.isRhs() && target.getGraph().isLhs()) {
					target = workaround_edgeContext(target);
				}
				
				targetEdge.setTarget(target);
			}
		}
	}
	
	protected static void setEdgeGraph(Edge edge, Graph graph) {
		
		if ((edge != null) && (graph != null)) {
			
			// update multi-rules:
			for (Rule multiRule : getMultiRules(edge)) {
				Edge multiEdge = getMultiGraphElement(edge, multiRule, false);
				setEdgeGraph(multiEdge, getMultiGraph(multiRule, graph));
			}
			
			// update rule:
			edge.setGraph(graph);
		}
	}
	
	protected static void setEdgeSource(Edge edge, Node source) {
		
		if ((edge != null) && (source != null)) {
			
			// update multi-rules:
			for (Rule multiRule : getMultiRules(edge)) {
				Node multiSource = getMultiGraphElement(edge.getSource(), multiRule, true);
				Edge multiEdge = getMultiGraphElement(edge, multiRule, false);
				setEdgeSource(multiEdge, multiSource);
			}
			
			// update rule:
			edge.setSource(source);
		}
	}
	
	protected static void setEdgeTarget(Edge edge, Node target) {
		
		if ((edge != null) && (target != null)) {
			
			// update multi-rules:
			for (Rule multiRule : getMultiRules(edge)) {
				Node multiTarget = getMultiGraphElement(edge.getTarget(), multiRule, true);
				Edge multiEdge = getMultiGraphElement(edge, multiRule, false);
				setEdgeTarget(multiEdge, multiTarget);
			}
			
			// update rule:
			edge.setTarget(target);
		}
	}
	
	protected static void updateNestedCondition(NestedCondition ac) {
		
		// remove none context nodes:
		for (Node node : new ArrayList<>(ac.getConclusion().getNodes())) {
			if ((getRemoteGraphElement(node) != null) 
					&& node.getOutgoing().isEmpty() 
					&& node.getIncoming().isEmpty() 
					&& node.getAttributes().isEmpty()) {
				remove(node);
			}
		}
	}
	
	protected static boolean workaround_applicationConditionContext(GraphElement graphElement, GraphElement acGraphElement) {
		
		if (acGraphElement instanceof Node) {
			Node context = getRemoteGraphElement((Node) acGraphElement);
			Node remoteContext = getRemoteGraphElement(context);

			// remap application condition context to RHS:
			if (remoteContext != null) {
				unmap(graphElement, acGraphElement);
				map(remoteContext, acGraphElement);

				return true;
			}
		}
		
		return false;
	}
	
	protected static Node workaround_edgeContext(Node context) {
		Node remoteContext = getRemoteGraphElement(context);
		
		if (remoteContext != null) {
			return remoteContext;
		}
		
		return context;
	}
}
