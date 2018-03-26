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
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMappedGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiGraph;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getMultiRules;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.getRemoteGraphElement;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.map;
import static org.eclipse.emf.henshin.model.util.HenshinEditMappedHelper.unmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
		
		// nothing to do:
		if (graphElement == null) {
			return;
		}
		
		// remove mappings:
		if (graphElement instanceof Node) {
			for (NestedCondition ac : getApplicationConditions(graphElement)) {
				
				// update mapped graph elements:
				if (!workaround_applicationConditionContext(graphElement, ac)) {
					unmap(graphElement, ac.getMappings());
					remove(ac.getMappings().getImage(graphElement, ac.getConclusion()));
				}
			}
			
			unmap(graphElement, graphElement.getGraph().getRule().getMultiMappings());
			unmap(graphElement, graphElement.getGraph().getRule().getMappings());
			
			if (graphElement.getGraph().isNestedCondition()) {
				unmap(graphElement, ((NestedCondition) graphElement.getGraph().eContainer()).getMappings());
			}
		}
	
		// remove from multi-rules:
		for (Rule multiRule : getMultiRules(graphElement)) {
			GraphElement multiGraphElement = getMultiGraphElement(graphElement, multiRule, false);
	
			if (multiGraphElement != null) {
				remove(multiGraphElement);
			}
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
				if (targetGraph.isNestedCondition()) {
					merge(targetGraph, graphElement, multiGraphElement);
				} else {
					move(getMultiGraph(multiRule, targetGraph), multiGraphElement);
				}
			}
		}
	
		// move node:
		if (graphElement instanceof Node) {
			
			Graph oldGraph = graphElement.getGraph();

			if (!(oldGraph.isNestedCondition() && targetGraph.isNestedCondition())) {
			
				// move element:
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
			} else {
				
				// move full application condition:
				moveApplicationCondition(targetGraph, graphElement);
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

	protected static void moveApplicationCondition(Graph targetGraph, GraphElement graphElement) {
		
		// move full application condition:
		if ((graphElement.getGraph().eContainer() instanceof NestedCondition) && (targetGraph.eContainer() instanceof NestedCondition)) {
			NestedCondition ac = (NestedCondition) graphElement.getGraph().eContainer();
			NestedCondition targetAC = (NestedCondition) targetGraph.eContainer();
			
			// collect bounded application condition graph:
			Set<Mapping> moveMappings = new HashSet<>();
			Set<Node> moveNodes = new HashSet<>();
			
			Node firstNode = (graphElement instanceof Node) ? (Node) graphElement
					: (graphElement instanceof Edge) ?  ((Edge) graphElement).getSource()
					: (graphElement instanceof Attribute) ?  ((Attribute) graphElement).getNode()
					: null;
			
			Stack<Node> currentNodes = new Stack<>();
			currentNodes.push(firstNode);
			
			while (!currentNodes.isEmpty()) {
				Node currentNode = currentNodes.pop();
				moveNodes.add(currentNode);
				
				for (Edge outgoing : currentNode.getOutgoing()) {
					Node acImage = outgoing.getTarget();
					
					if (!moveNodes.contains(acImage)) {
						Node hostOrigin = ac.getMappings().getOrigin(acImage);

						// is mapped application condition node:
						if (hostOrigin != null) {
							moveMappings.add(ac.getMappings().get(hostOrigin, acImage));
						} else {
							currentNodes.push(acImage);
						}
					}
				}
				
				for (Edge outgoing : currentNode.getIncoming()) {
					Node acImage = outgoing.getSource();
					
					if (!moveNodes.contains(acImage)) {
						Node hostOrigin = ac.getMappings().getOrigin(acImage);

						// is mapped application condition node:
						if (hostOrigin != null) {
							moveMappings.add(ac.getMappings().get(hostOrigin, acImage));
						} else {
							currentNodes.push(acImage);
						}
					}
				}
			}
			
			// move application condition:
			for (Node acNode : moveNodes) {
				acNode.setGraph(targetGraph);
				
				for (Edge outgoing : acNode.getOutgoing()) {
					outgoing.setGraph(targetGraph);
				}
			}
			
			// move application condition context:
			for (Mapping acMapping : moveMappings) {
				
				// search new origin:
				Node targetOrigin = getMappedGraphElement(targetAC.getHost(), acMapping.getOrigin(), true);
				
				// mapping exists?
				Node acContext = targetAC.getMappings().getImage(targetOrigin, targetAC.getConclusion());
				
				if (acContext == null) {

					// copy mapped application condition context node:
					acContext = copy(targetGraph, acMapping.getImage());
					acContext.setGraph(targetGraph);
					
					// create mapping:
					targetAC.getMappings().add(targetOrigin, acContext);
				}
				
				// move boundary edges:
				for (Edge boundaryEdge : new ArrayList<>(acMapping.getImage().getOutgoing())) {
					if (moveNodes.contains(boundaryEdge.getTarget())) {
						boundaryEdge.setSource(acContext);
						boundaryEdge.setGraph(targetGraph);
					}
				}
				for (Edge boundaryEdge : new ArrayList<>(acMapping.getImage().getIncoming())) {
					if (moveNodes.contains(boundaryEdge.getSource())) {
						boundaryEdge.setTarget(acContext);
					}
				}
			}
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
			
			if (targetGraph.isLhs() || targetGraph.isRhs()) {
				
				// update application conditions mappings:
				for (NestedCondition ac : getApplicationConditions(mergedNode)) {
					for (Mapping mapping : ac.getMappings()) {
						if (mapping.getOrigin() == mergedNode) {
							mapping.setOrigin(retainedNode);
						}
					}
				}
				
				// merge multi-rules:
				for (Rule multiRule : getMultiRules(mergedNode)) {
					Node multiRetainedNode = getMultiGraphElement(retainedNode, multiRule, false);
					Node multiMergedNode = getMultiGraphElement(mergedNode, multiRule, false);
					
					if ((multiRetainedNode != null) && (multiMergedNode != null)) {
						merge(getMultiGraph(multiRule, targetGraph), multiRetainedNode, multiMergedNode);
					}
				}
			}
			
			// merge edges of nodes:
			for (Edge outgoing : new ArrayList<>(mergedNode.getOutgoing())) {
				
				// check if the edge already exists:
				if (retainedNode.getOutgoing(outgoing.getType(), getMappedGraphElement(retainedNode.getGraph(), outgoing.getTarget(), false)) == null) {
					setEdgeSource(outgoing, retainedNode);
				} else {
					remove(outgoing);
				}
			}
			
			for (Edge incoming : new ArrayList<>(mergedNode.getIncoming())) {
				
				// check if the edge already exists:
				if (retainedNode.getIncoming(incoming.getType(), getMappedGraphElement(retainedNode.getGraph(), incoming.getSource(), false)) == null) {
					setEdgeTarget(incoming, retainedNode);
				} else {
					remove(incoming);
				}
			}
			
			// merge attributes of nodes:
			for (Attribute attribute : new ArrayList<>(mergedNode.getAttributes())) {
				if (retainedNode.getAttribute(attribute.getType()) == null) {
					attribute.setNode(retainedNode);
				}
			}
		}
		
		// remove merged graph element:
		remove(merged);
		
		// move to merge destination graph:
		move(targetGraph, retained);
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
		
		// clone graph element:
		GraphElement cloned = copy(cloneGraph, original);
		
		if (original instanceof Attribute) {
			setAttributeContext(cloneGraph, (Attribute) original, (Attribute) cloned);
		} else {
			add(cloneGraph, cloned);
		}
		
		// update multi-rules:
		if ((originalGraph.isLhs() || originalGraph.isRhs()) && (cloneGraph.isLhs() || cloneGraph.isRhs())) {
			for (Rule multiRule : getMultiRules(original)) {
				Graph multiOriginalGraph = getMultiGraph(multiRule, originalGraph);
				Graph multiCloneGraph = getMultiGraph(multiRule, cloneGraph);
				
				E multiOriginal = getMappedGraphElement(multiOriginalGraph, original, false);
				
				if (multiOriginal != null) {
					E multiCloned = unmerge(multiOriginal, multiOriginalGraph, multiCloneGraph);
					multiRule.getMultiMappings().add(cloned, multiCloned);
				}
			}
		}
		
		// unmerge node:
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
					
					// move node:
					Graph oldGraph = original.getGraph();
					add(originalGraph, original);
					
					// (un)move edges/attributes:
					for (Edge outgoing : new ArrayList<>(((Node) original).getOutgoing())) {
						outgoing.setSource(getMappedGraphElement(oldGraph, outgoing.getSource(), true)); 
					}
					for (Edge incoming : new ArrayList<>(((Node) original).getIncoming())) {
						incoming.setTarget(getMappedGraphElement(oldGraph, incoming.getTarget(), true)); 
					}
					for (Attribute attribute : ((Node) original).getAttributes()) {
						setAttributeContext(oldGraph, attribute);
					}
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

		// unmerge edge:
		else if (original instanceof Edge) {
			if (moveOriginal) {
				setEdgeContext(originalGraph, (Edge) original);
				add(originalGraph, original);
			}
		}

		return (E) cloned;
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
			List<Mapping> wrongMappings = new ArrayList<>();
			
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
					} else {
						if (!(mapping.getImage().getGraph().isNestedCondition()
								|| isValidLHStoRHSMapping(mapping) 
								|| isValidLhsMultiMapping(mapping)
								|| isValidRhsMultiMapping(mapping))) {
							wrongMappings.add(mapping);
						}
					}
				}
			}
			
			for (Edge danglingEdge : dangling) {
				EcoreUtil.delete(danglingEdge);
			}
			
			for (Mapping incompleteMapping : incompleteMappings) {
				EcoreUtil.remove(incompleteMapping);
			}
			
			for (Mapping wrongMapping : wrongMappings) {
				EcoreUtil.remove(wrongMapping);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// update multi-rule:
			for (Rule multiRule : rule.getMultiRules()) {
				HenshinModelCleaner.completeMultiRules(multiRule);
				
				// update application conditions:
				for (NestedCondition ac : multiRule.getLhs().getNestedConditions()) {
					update_reduceNestedCondition(ac);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		try {
			// update application conditions:
			for (NestedCondition ac : rule.getLhs().getNestedConditions()) {
				update_reduceNestedCondition(ac);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void update_reduceNestedCondition(NestedCondition ac) {
		
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

	protected static boolean workaround_applicationConditionContext(GraphElement graphElement, NestedCondition ac) {
		
		if ((graphElement instanceof Node) && (graphElement.getGraph().isLhs())) {
			GraphElement acGraphElement = getApplicationConditionGraphElement(ac.getConclusion(), graphElement, false);
			
			if (acGraphElement != null) {
				Node context = getRemoteGraphElement((Node) acGraphElement);
				Node remoteContext = getRemoteGraphElement(context);
				
				// remap application condition context to RHS:
				if (remoteContext != null) {
					unmap(graphElement, acGraphElement);
					map(remoteContext, acGraphElement);
					
					return true;
				}
			}
		}
		
		return false;
	}

	protected static void fix_applicationConditionContext(Node origin, Node image) {
		
		if ((origin.getGraph().isLhs() || origin.getGraph().isRhs()) && (image.getGraph().isLhs() || image.getGraph().isRhs())) {
			
			// remap application condition context from RHS to LHS:
			if (origin.getGraph().getRule() == image.getGraph().getRule()) {
				for (NestedCondition ac : getApplicationConditions(origin)) {
					Node acNode = getApplicationConditionGraphElement(ac.getConclusion(), image, false);
					
					if (acNode != null) {
						unmap(image, acNode);
						map(origin, acNode);
					}
				}
			}
			
			// remap application condition context from kernel (origin) to multi-rule (imgae):
			if (origin.getGraph().getRule() == image.getGraph().getRule().getKernelRule()) {
				Node kernel = origin;
				Node multi = image;
				
				// kernel application conditions:
				for (NestedCondition ac : getApplicationConditions(kernel)) {
					for (Mapping mapping : ac.getMappings()) {
						if (mapping.getOrigin() == multi) {
							mapping.setOrigin(kernel);
						}
					}
				}
				
				// multi application conditions:
				for (NestedCondition ac : getApplicationConditions(image)) {
					for (Mapping mapping : ac.getMappings()) {
						if (mapping.getOrigin() == kernel) {
							mapping.setOrigin(multi);
						}
					}
				}
			}
		}
	}
	
	protected static void fix_edgeContext(Node origin, Node image) {
		
		if ((origin != null) && (image != null)) {
			if (origin.getGraph().getRule() == image.getGraph().getRule()) {

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
	
	protected static boolean isValidLHStoRHSMapping(Mapping mapping) {
		return (mapping.getOrigin().getGraph().isLhs() && mapping.getImage().getGraph().isRhs()
				&& (mapping.getOrigin().getGraph().getRule() == mapping.getImage().getGraph().getRule()))
				|| (mapping.getOrigin().getGraph().isRhs() && mapping.getImage().getGraph().isLhs()
						&& (mapping.getOrigin().getGraph().getRule() == mapping.getImage().getGraph().getRule()));
	}
	
	protected static boolean isValidRhsMultiMapping(Mapping mapping) {
		return (mapping.getOrigin().getGraph().isRhs() && mapping.getImage().getGraph().isRhs()
				&& ((mapping.getOrigin().getGraph().getRule().getKernelRule() == mapping.getImage().getGraph().getRule())
						|| (mapping.getOrigin().getGraph().getRule() == mapping.getImage().getGraph().getRule().getKernelRule())));
	}
	
	protected static boolean isValidLhsMultiMapping(Mapping mapping) {
		return (mapping.getOrigin().getGraph().isLhs() && mapping.getImage().getGraph().isLhs()
				&& ((mapping.getOrigin().getGraph().getRule().getKernelRule() == mapping.getImage().getGraph().getRule())
						|| (mapping.getOrigin().getGraph().getRule() == mapping.getImage().getGraph().getRule().getKernelRule())));
	}
	
	protected static void setAttributeContext(Graph targetGraph, Attribute attribute) {
		setAttributeContext(targetGraph, attribute, attribute);

	}
	
	protected static void setAttributeContext(Graph targetGraph, Attribute originalAttribute,  Attribute copyAttribute) {
		add(getMappedGraphElement(targetGraph, originalAttribute.getNode(), true), copyAttribute);
	}
	
	protected static void setEdgeContext(Graph targetGraph, Edge edge) {
		setEdgeContext(targetGraph, edge, edge);
	}
	
	protected static void setEdgeContext(Graph targetGraph, Edge targetEdge, Edge remoteEdge) {
		targetEdge.setSource(getMappedGraphElement(targetGraph, remoteEdge.getSource(), true));
		targetEdge.setTarget(getMappedGraphElement(targetGraph, remoteEdge.getTarget(), true));
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
}
