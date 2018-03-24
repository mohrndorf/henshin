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

import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.add;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.copy;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.fix_applicationConditionContext;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.fix_edgeContext;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.move;
import static org.eclipse.emf.henshin.model.util.HenshinEditHelper.unmerge;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.GraphElement;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.MappingList;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;

/**
 * Functions for handling mapped elements.
 * 
 * @author Manuel Ohrndorf
 */
public class HenshinEditMappedHelper {

	/**
	 * @param mappedGraph
	 *            A graph wich contains the mapped graph element of the given graph
	 *            element.
	 * @param graphElement
	 *            A graph element.
	 * @param create
	 *            <code>true</code> to create the mapped graph element if it not
	 *            exists; <code>false</code> otherwise;
	 * @return The mapped graph element;
	 */
	public static <E extends GraphElement> E getMappedGraphElement(Graph mappedGraph, E graphElement, boolean create) {
		
		// target: self:
		if (graphElement.getGraph() == mappedGraph) {
			return graphElement;
		}
		
		// target: application condition:
		else if (mappedGraph.isNestedCondition()) {
			return getApplicationConditionGraphElement(mappedGraph, graphElement, create);
		}
		
		// target: multi-rule:
		else if (getMultiRules(graphElement).contains(mappedGraph.getRule())) {
			return getMultiGraphElement(graphElement, mappedGraph.getRule(), create);
		}
		
		// target: kernel-rule:
		else if (getKernelRule(graphElement) == mappedGraph.getRule()) {
			return getKernelGraphElement(graphElement, mappedGraph.getRule(), create);
		}
		
		// target: rule:
		else if (graphElement.getGraph().getRule() == mappedGraph.getRule()) {
			return getRemoteGraphElement(mappedGraph, graphElement, create);
		}
		
		return null;
	}

	/**
	 * @param kernelRule
	 *            A kernel-rule
	 * @param multiGraphElement
	 *            A multi-rule graph element.
	 * @return The LHS/RHS kernel-graph for the LHS/RHS multi-graph.
	 */
	public static Graph getKernelGraph(Rule kernelRule, GraphElement multiGraphElement) {
		
		if (multiGraphElement.getGraph().isLhs()) {
			return kernelRule.getLhs();
		}
		
		else if (multiGraphElement.getGraph().isRhs()) {
			return kernelRule.getRhs();
		}
		
		return null;
	}

	/**
	 * @param multiRule
	 *            A multi-rule
	 * @param kernelGraph
	 *            A kernel-rule graph.
	 * @return The LHS/RHS multi-graph for the LHS/RHS kernel-graph.
	 */
	public static Graph getMultiGraph(Rule multiRule, Graph kernelGraph) {
		
		if (kernelGraph.isLhs()) {
			return multiRule.getLhs();
		}
		
		else if (kernelGraph.isRhs()) {
			return multiRule.getRhs();
		}
		
		return null;
	}

	/**
	 * @param graph
	 *            A graph.
	 * @return The remote graph of the given graph: LHS to RHS, RHS to LHS,
	 *         Application Condition to LHS.
	 */
	public static Graph getRemoteGraph(Graph graph) {
		
		if (graph.isLhs()) {
			return graph.getRule().getRhs();
		} else if (graph.isRhs()) {
			return graph.getRule().getLhs();
		} else if (graph.isNestedCondition()) {
			return graph.getRule().getLhs();
		}
	
		return null;
	}

	/**
	 * @param graphElement
	 *            A graph element.
	 * @param remoteGraphElement
	 *            A graph element.
	 * @return The LHS element of the given elements.
	 */
	public static <E extends GraphElement> E getLHS(E graphElement, E remoteGraphElement) {
		if (graphElement.getGraph().isLhs()) {
			return graphElement;
		} else if (remoteGraphElement.getGraph().isLhs()) {
			return remoteGraphElement;
		}
		return null;
	}

	/**
	 * @param graphElement
	 *            A graph element.
	 * @param remoteGraphElement
	 *            A graph element.
	 * @return The RHS element of the given elements.
	 */
	public static <E extends GraphElement> E getRHS(E graphElement, E remoteGraphElement) {
		if (graphElement.getGraph().isRhs()) {
			return graphElement;
		} else if (remoteGraphElement.getGraph().isRhs()) {
			return remoteGraphElement;
		}
		return null;
	}

	/**
	 * @param graphElement
	 *            A graph element.
	 * @return All multi-rules of the rule that contains the given graph element.
	 */
	public static Collection<Rule> getMultiRules(GraphElement graphElement) {
		return graphElement.getGraph().getRule().getMultiRules();
	}

	/**
	 * @param graphElement
	 *            A graph element.
	 * @return The kernel-rule of the rule that contains the given graph element; or
	 *         <code>null</code> if the rule not a multi-rule.
	 */
	public static Rule getKernelRule(GraphElement graphElement) {
		return graphElement.getGraph().getRule().getKernelRule();
	}

	/**
	 * @param originGraphElement
	 *            The origin of the new mapping.
	 * @param imageGraphElement
	 *            The image of the new mapping.
	 */
	public static void map(GraphElement originGraphElement, GraphElement imageGraphElement) {
		map(originGraphElement, imageGraphElement, true);
	}

	/**
	 * @param originGraphElement
	 *            The origin of the new mapping.
	 * @param imageGraphElement
	 *            The origin of the new mapping.
	 */
	public static void unmap(GraphElement originGraphElement, GraphElement imageGraphElement) {
		if ((originGraphElement instanceof Node) && (imageGraphElement instanceof Node)) {
			
			// LHS to RHS node mapping: 
			if (originGraphElement.getGraph().getRule() == imageGraphElement.getGraph().getRule()) {
				if (originGraphElement.getGraph().isLhs() && imageGraphElement.getGraph().isRhs()) {
					
					// update rule:
					originGraphElement.getGraph().getRule().getMappings().remove(originGraphElement, imageGraphElement);
					
					// update multi-rules:
					for (Rule multiRule : getMultiRules(originGraphElement)) {
						Node originMulti = getMultiGraphElement((Node) originGraphElement, multiRule, true);
						Node imageMulti = getMultiGraphElement((Node) imageGraphElement, multiRule, true);
						
						if ((originMulti != null) && (imageMulti != null)) {
							unmap(originMulti, imageMulti);
						}
					}
				}
			}
			
			// Application Condition:
			if (imageGraphElement.getGraph().isNestedCondition()) {
				((NestedCondition) imageGraphElement.getGraph().eContainer()).getMappings().remove(originGraphElement, imageGraphElement);
			}
			
			// multi-rule node mapping:
			if (originGraphElement.getGraph().getRule().getMultiRules().contains(imageGraphElement.getGraph().getRule())) {
				imageGraphElement.getGraph().getRule().getMultiMappings().remove(originGraphElement, imageGraphElement);
			}
		}
	}

	protected static GraphElement createApplicationConditionGraphElement(NestedCondition ac, GraphElement graphElement) {
		GraphElement acGraphElement = copy(ac.getConclusion(), graphElement);
		add(ac.getConclusion(), acGraphElement);
		
		// from kernel-rule application condition to multi-rule application condition:
		if (graphElement.getGraph().getRule().getMultiRules().contains(ac.getHost().getRule())) {
			map(getMultiGraphElement(graphElement, ac.getHost().getRule(), true), acGraphElement);
		
		// from rule to application contion:
		} else {
			map(graphElement, acGraphElement);
		}

		return acGraphElement;
	}
	
	protected static GraphElement createKernelGraphElement(Rule kernelRule, GraphElement multiGraphElement) {
		Graph kernelGraph = getKernelGraph(kernelRule, multiGraphElement);
		
		if (kernelGraph != null) {
			move(kernelGraph, multiGraphElement);
			return multiGraphElement;
		}
		
		return null;
	}
	
	protected static GraphElement createMultiGraphElement(Rule multiRule, GraphElement kernelGraphElement) {
		Graph multiGraph = getMultiGraph(multiRule, kernelGraphElement.getGraph());
		
		if (multiGraph != null) {
			GraphElement multiGraphElement = copy(multiGraph, kernelGraphElement);
			
			if (multiGraphElement instanceof Attribute) {
				add(getMultiGraphElement(((Attribute) kernelGraphElement).getNode(), multiRule, true), (Attribute) multiGraphElement);
			} else {
				add(multiGraph, multiGraphElement);
			}
			
			map(kernelGraphElement, multiGraphElement);
			
			return multiGraphElement;
		}
		
		return null;
	}
	
	protected static <E extends GraphElement> E createRemoteGraphElement(Graph targetGraph, E graphElement) {
		return unmerge(graphElement, graphElement.getGraph(), targetGraph);
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E fix_getApplicationConditionGraphElement(NestedCondition ac, E graphElement, E acGraphElement) {
		if (acGraphElement == null) {
			return (E) createApplicationConditionGraphElement(ac, graphElement);
		}
		return acGraphElement;
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E fix_getKernelGraphElement(Rule kernelRule, E kernelGraphElement, E multiGraphElement) {
		if (kernelGraphElement == null) {
			return (E) createKernelGraphElement(kernelRule, multiGraphElement);
		}
		return kernelGraphElement;
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E fix_getMultiGraphElement(Rule multiRule, E kernelGraphElement, E multiGraphElement) {
		if (multiGraphElement == null) {
			return (E) createMultiGraphElement(multiRule, kernelGraphElement);
		}
		return multiGraphElement;
	}
	
	protected static <E extends GraphElement> E fix_getRemoteGraphElement(Graph targetGraph, E graphElement, E remoteGraphElement) {
		if (remoteGraphElement == null) {
			remoteGraphElement = createRemoteGraphElement(targetGraph, graphElement);
		}
		return remoteGraphElement;
	}
	
	protected static <E extends GraphElement> E getApplicationConditionGraphElement(Graph acGraph, E graphElement, boolean create) {
		
		// is already the application condition element:
		if (graphElement.getGraph() == acGraph) {
			return graphElement;
		}
		
		// search application condition element:
		else if (acGraph.isNestedCondition()) {
			
			// RHS/application condition to LHS:
			if (!graphElement.getGraph().isLhs()) {
				E lhsGraphElement = getRemoteGraphElement(graphElement);
				
				if (lhsGraphElement != null) {
					graphElement = lhsGraphElement;
				}
			}
			
			// from multi-rule application condition to kernel-rule application condition:
			if (graphElement.getGraph().getRule().getKernelRule() == acGraph.getRule()) {
				E lhsGraphElement = getKernelGraphElement(graphElement, acGraph.getRule(), true);
				
				if (lhsGraphElement != null) {
					graphElement = lhsGraphElement;
				}
			}
			
			// Search mapped element:
			NestedCondition ac = (NestedCondition) acGraph.eContainer();
			E acGraphElement = getImage(ac.getMappings(), graphElement, acGraph);
			
			if (create) {
				acGraphElement = fix_getApplicationConditionGraphElement(ac, graphElement, acGraphElement);
			}
			
			return acGraphElement;
		}
			
		return null;
	}
	
	protected static Collection<NestedCondition> getApplicationConditions(GraphElement graphElement) {
		if (graphElement.getGraph().isLhs()) {
			return graphElement.getGraph().getNestedConditions();
		} else if (graphElement.getGraph().isRhs()) {
			GraphElement remoteGraphElement = getRemoteGraphElement(graphElement);
			
			if (remoteGraphElement != null) {
				return remoteGraphElement.getGraph().getNestedConditions();
			}
		}
		return Collections.emptyList();
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E getImage(MappingList mappings, E origin, Graph imageGraph) {
		if (origin instanceof Edge) {
			Edge originEdge = (Edge) origin;
			
			if (originEdge.getSource() == null || originEdge.getTarget() == null) {
				return null;
			}

			Node source = mappings.getImage(originEdge.getSource(), imageGraph);
			Node target = mappings.getImage(originEdge.getTarget(), imageGraph);

			// allow cross graph mappings:
			if (source == null && target != null) {
				source = mappings.getImage(originEdge.getSource(), getRemoteGraph(imageGraph));
			}
			
			if (source != null && target == null) {
				target = mappings.getImage(originEdge.getTarget(), getRemoteGraph(imageGraph));
			}

			if (source == null || target == null) {
				return null;
			}

			return (E) source.getOutgoing(originEdge.getType(), target);
		} else {
			return mappings.getImage(origin, imageGraph);
		}
	}
	
	protected static <E extends GraphElement> E getKernelGraphElement(E multilGraphElement, Rule kernelRule, boolean create) {
		E kernelGraphElement = getOrigin(multilGraphElement.getGraph().getRule().getMultiMappings(), multilGraphElement);

		if (create){
			kernelGraphElement = fix_getKernelGraphElement(kernelRule, kernelGraphElement, multilGraphElement);
		}

		return kernelGraphElement;
	}
	
	protected static <E extends GraphElement> E getMultiGraphElement(E kernelGraphElement, Rule multiRule, boolean create) {
		Graph multiGraph = getMultiGraph(multiRule, kernelGraphElement.getGraph());
		
		if (multiGraph != null) {
			E multiGraphElement = getImage(multiRule.getMultiMappings(), kernelGraphElement, multiGraph);
			
			if (create){
				multiGraphElement = fix_getMultiGraphElement(multiRule, kernelGraphElement, multiGraphElement);
			}
			
			return multiGraphElement;
		}
		
		return null;
	}
	
	protected static <E extends GraphElement> E getOrigin(MappingList mappings, E origin) {
		return mappings.getOrigin(origin);
	}
	
	protected static <E extends GraphElement> E getRemoteGraphElement(E graphElement) {
		
		if (graphElement.getGraph().isLhs()) {
			return getImage(graphElement.getGraph().getRule().getMappings(), graphElement, graphElement.getGraph().getRule().getRhs());
		} else if (graphElement.getGraph().isRhs()) {
			return getOrigin(graphElement.getGraph().getRule().getMappings(), graphElement);
		} else if (graphElement.getGraph().isNestedCondition()) {
			return getOrigin(((NestedCondition) graphElement.getGraph().eContainer()).getMappings(), graphElement);
		}

		return null;
	}
	
	protected static <E extends GraphElement> E getRemoteGraphElement(Graph targetGraph, E graphElement, boolean create) {
		
		// LHS/RHS to RHS/LHS:
		// application condition to LHS:
		E remoteGraphElement = getRemoteGraphElement(graphElement);
		
		// create remote element:
		if (create) {
			remoteGraphElement = unmerge(graphElement, graphElement.getGraph(), targetGraph);
		}
		
		// LHS to RHS
		if (targetGraph.isRhs() && remoteGraphElement.getGraph().isLhs()) {
			remoteGraphElement = getRemoteGraphElement(remoteGraphElement);
		}
		
		return remoteGraphElement;
	}
	
	protected static MappingList getRemoteMappings(GraphElement graphElement) {
		
		if (graphElement.getGraph().isLhs()) {
			return graphElement.getGraph().getRule().getMappings();
		} else if (graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getMappings();
		} else if (graphElement.getGraph().isNestedCondition()) {
			return ((NestedCondition) graphElement.getGraph().eContainer()).getMappings();
		}

		return null;
	}
	
	protected static void map(GraphElement originGraphElement, GraphElement imageGraphElement, boolean fix) {
		if ((originGraphElement instanceof Node) && (imageGraphElement instanceof Node)) {
			
			// LHS to RHS node mapping: 
			if (originGraphElement.getGraph().getRule() == imageGraphElement.getGraph().getRule()) {
				if (originGraphElement.getGraph().isLhs() && imageGraphElement.getGraph().isRhs()) {
					
					// update rule:
					originGraphElement.getGraph().getRule().getMappings().add(originGraphElement, imageGraphElement);
					
					// update multi-rules:
					for (Rule multiRule : getMultiRules(originGraphElement)) {
						Node originMulti = getMultiGraphElement((Node) originGraphElement, multiRule, true);
						Node imageMulti = getMultiGraphElement((Node) imageGraphElement, multiRule, true);
						
						if ((originMulti != null) && (imageMulti != null)) {
							map(originMulti, imageMulti);
						}
					}
					
					if (fix) {
						
						// fix edge context workarounds:
						fix_edgeContext((Node) originGraphElement, (Node) imageGraphElement);
						
						// fix application context workaround:
						fix_applicationConditionContext((Node) originGraphElement, (Node) imageGraphElement);
					}
				}
			}
			
			// application Condition:
			if (imageGraphElement.getGraph().isNestedCondition()) {
				((NestedCondition) imageGraphElement.getGraph().eContainer()).getMappings().add(originGraphElement, imageGraphElement);
			}
			
			// multi-rule node mapping:
			if (originGraphElement.getGraph().getRule().getMultiRules().contains(imageGraphElement.getGraph().getRule())) {
				imageGraphElement.getGraph().getRule().getMultiMappings().add(originGraphElement, imageGraphElement);
			}
		}
	}
	
	protected static void unmap(GraphElement graphElement, MappingList mappings) {
		if (graphElement instanceof Node) {
			for (Iterator<Mapping> iterator = mappings.iterator(); iterator.hasNext();) {
				Mapping mapping = iterator.next();
				
				if ((mapping.getImage() == graphElement) || (mapping.getOrigin() == graphElement)) {
					iterator.remove();
				}
			}
		}
	}
}
