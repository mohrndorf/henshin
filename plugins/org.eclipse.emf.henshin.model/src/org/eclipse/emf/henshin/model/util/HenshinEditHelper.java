package org.eclipse.emf.henshin.model.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.GraphElement;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.MappingList;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;

public class HenshinEditHelper {

	public static void remove(GraphElement graphElement) {
		remove(graphElement, true);
	}
	
	private static void remove(GraphElement graphElement, boolean updateMultiRules) {

		// handle dangling edges:
		if (graphElement instanceof Node) {
			fix_remove((Node) graphElement);
		}

		if ((graphElement instanceof Node) || (graphElement instanceof Edge) || (graphElement instanceof Attribute)) {
			
			// update rule:
			EcoreUtil.delete(graphElement);
			
			if (graphElement instanceof Node) {
				unmap((Node) graphElement, graphElement.getGraph().getRule().getMappings());
			}
			
			// update application conditions:
			for (NestedCondition ac : getApplicationConditions(graphElement)) {
				GraphElement acGraphElement = getApplicationConditionGraphElement(ac.getConclusion(), graphElement, false);
				
				if (acGraphElement != null) {
					if (!workaround_applicationConditionContext(graphElement, acGraphElement)) {
						remove(acGraphElement);

						if (acGraphElement instanceof Node) {
							unmap(graphElement, acGraphElement);
						}
					}
				}
			}
			
			// update multi-rules:
			if (updateMultiRules) {
				for (Rule multiRule : getMultiRules(graphElement)) {
					GraphElement multiGraphElement = getMultiGraphElement(graphElement, multiRule, false);
					
					if (multiGraphElement != null) {
						remove(multiGraphElement);
					}
					
					if (multiGraphElement instanceof Node) {
						unmap(graphElement, multiGraphElement);
					}
				}
			}
		}
	}
	
	protected static void fix_remove(Node node) {
		
		// remove dangling edges:
		for (Edge outgoing : node.getOutgoing()) {
			remove(outgoing);
		}
		for (Edge incoming : node.getIncoming()) {
			remove(incoming);
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
	
	protected static boolean fix_applicationConditionContext(GraphElement graphElement, GraphElement acGraphElement) {
		
		if (acGraphElement instanceof Node) {
			Node context = getRemoteGraphElement((Node) acGraphElement);
			
			if (context.getGraph().isRhs()) {
				Node remoteContext = getRemoteGraphElement((Node) context);
				
				// remap application condition context to RHS:
				if ((remoteContext != null) && (remoteContext.getGraph().isLhs())) {
					unmap(graphElement, acGraphElement);
					map(remoteContext, acGraphElement);
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static void add(Node node, Attribute attribute) {
		
		// update rule:
		node.getAttributes().add(attribute);
		
		// update multi-rules: 
		for (Rule multiRule : getMultiRules(attribute)) {
			add(getMultiGraphElement(node, multiRule, true), copy(node, attribute));
		}
	}
	
	public static void add(Graph graph, GraphElement graphElement) {
		
		// update rule:
		if (graphElement instanceof Node) {
			graph.getNodes().add((Node) graphElement);
			
			// update application conditions:
			for (NestedCondition ac : getApplicationConditions(graphElement)) {
				GraphElement acGraphElement = getApplicationConditionGraphElement(ac.getConclusion(), graphElement, false);
				
				if (acGraphElement != null) {
					fix_applicationConditionContext(graphElement, acGraphElement);
				}
			}
		}
		
		else if (graphElement instanceof Edge) {
			graph.getEdges().add((Edge) graphElement);
		}
		
		// update multi-rules: 
		if ((graphElement instanceof Node) || (graphElement instanceof Edge)) {
			for (Rule multiRule : getMultiRules(graphElement)) {
				createMultiGraphElement(multiRule, graphElement);
			}
		}
	}
	
	public static GraphElement copy(Graph targetGraph, GraphElement graphElement) {
		
		// copy (without adding):
		if (graphElement instanceof Node) {
			Node node = (Node) graphElement;
			
			Node copy = HenshinFactory.eINSTANCE.createNode();
			copy.setDescription(node.getDescription());
			copy.setName(node.getName());
			copy.setType(node.getType());
			
			return copy;
		}
		
		else if (graphElement instanceof Edge) {
			Edge edge = (Edge) graphElement;
			
			Edge copy = HenshinFactory.eINSTANCE.createEdge();
			copy.setIndex(edge.getIndex());
			copy.setType(edge.getType());
			
			// copy to multi-rule:
			if (getMultiRules(edge).contains(targetGraph.getRule())) {
				copy.setSource(getMultiGraphElement(edge.getSource(), targetGraph.getRule(), true));
				copy.setTarget(getMultiGraphElement(edge.getTarget(), targetGraph.getRule(), true));
			}
			
			// copy to kernel-rule:
			else if (getKernelRule(edge) == targetGraph.getRule()) {
				copy.setSource(getKernelGraphElement(edge.getSource(), targetGraph.getRule(), true));
				copy.setTarget(getKernelGraphElement(edge.getTarget(), targetGraph.getRule(), true));
			}
			
			// copy to rule:
			else if (edge.getGraph().getRule() == targetGraph.getRule()) {
				
				// copy to same graph:
				if (edge.getGraph() == targetGraph) {
					copy.setSource(edge.getSource());
					copy.setTarget(edge.getTarget());
				} else {
					
					// copy to application condition
					if (targetGraph.isNestedCondition()) {
						copy.setSource(getApplicationConditionGraphElement(edge.getGraph(), edge.getSource(), true));
						copy.setTarget(getApplicationConditionGraphElement(edge.getGraph(), edge.getTarget(), true));
					} 
					
					// copy to LHS/RHS:
					else {
						copy.setSource(workaround_getRemoteGraphElement(edge.getSource()));
						copy.setTarget(workaround_getRemoteGraphElement(edge.getTarget()));
					}
				}
			}
		}
		
		return null;
	}
	
	protected static <E extends GraphElement> E workaround_getRemoteGraphElement(E graphElement) {
		E remoteGraphElement = getRemoteGraphElement(graphElement);
		
		if (remoteGraphElement != null) {
			return remoteGraphElement;
		} else {
			return graphElement;
		}
	}
	
	public static Attribute copy(Node targetNode, Attribute attribute) {
		
		// copy (without adding):
		Attribute copy = HenshinFactory.eINSTANCE.createAttribute();
		copy.setType(attribute.getType());
		copy.setValue(attribute.getValue());
		
		return copy;
	}
	
	public static void move(Graph targetGraph, GraphElement graphElement)  {
		Rule targetRule = targetGraph.getRule();
		
		// move (from LHS/RHS/application condition) to LHS/RHS/application condition
		if (graphElement.getGraph().getRule() == targetRule) {
			add(targetGraph, copy(targetGraph, graphElement));

			if (graphElement instanceof Node) {
				for (Edge outgoing : ((Node) graphElement).getOutgoing()) {
					add(targetGraph, copy(targetGraph, outgoing));
				}
				for (Edge incoming : ((Node) graphElement).getIncoming()) {
					add(targetGraph, copy(targetGraph, incoming));
				}
			}
			
			remove(graphElement);
		}
		
		// move (from kernel-rule) to multi-rule:
		if (getMultiRules(graphElement).contains(targetRule)) {
			getMultiGraphElement(graphElement, targetRule, true); // trigger fix
			
			if (graphElement instanceof Node) {
				for (Edge outgoing : ((Node) graphElement).getOutgoing()) {
					remove(outgoing);
				}
				for (Edge incoming : ((Node) graphElement).getIncoming()) {
					remove(incoming);
				}
			}
			
			remove(graphElement, false);
		}
		
		// move (from multi-rule) to kernel-rule:
		else if (getKernelRule(graphElement) == targetRule) {
			createKernelGraphElement(targetRule, graphElement);
			
			if (graphElement instanceof Node) {
				for (Edge outgoing : ((Node) graphElement).getOutgoing()) {
					createKernelGraphElement(targetRule, outgoing);
				}
				for (Edge incoming : ((Node) graphElement).getIncoming()) {
					createKernelGraphElement(targetRule, incoming);
				}
			}
		}
	}
	
	public static void move(Node targetNode, Attribute attribute)  {
		Rule targetRule = targetNode.getGraph().getRule();
	
		// move (from LHS/RHS/application condition) to LHS/RHS/application condition
		if (attribute.getGraph().getRule() == targetRule) {
			add(targetNode, copy(targetNode, attribute));
			remove(attribute);
		}
		
		// move (from kernel-rule) to multi-rule:
		if (getMultiRules(attribute).contains(targetRule)) {
			getMultiGraphElement(attribute, targetRule, true); // trigger fix
			remove(attribute, false);
		}
		
		// move (from multi-rule) to kernel-rule:
		else if (getKernelRule(attribute) == targetRule) {
			createKernelGraphElement(targetRule, attribute);
		}
	}
	
	private static void map(GraphElement originGraphElement, GraphElement imageGraphElement) {
		if ((originGraphElement instanceof Node) && (imageGraphElement instanceof Node)) {
			
			// LHS to RHS node mapping: 
			if (originGraphElement.getGraph().getRule() == imageGraphElement.getGraph().getRule()) {
				if (originGraphElement.getGraph().isLhs() && imageGraphElement.getGraph().isRhs()) {
					originGraphElement.getGraph().getRule().getMappings().add(originGraphElement, imageGraphElement);
				}
			}
			
			// Application Condition:
			if (imageGraphElement.getGraph().isNestedCondition()) {
				((NestedCondition) imageGraphElement.getGraph()).getMappings().add(originGraphElement, imageGraphElement);
			}
			
			// multi-rule node mapping:
			if (originGraphElement.getGraph().getRule().getMultiRules().contains(imageGraphElement.getGraph().getRule())) {
				imageGraphElement.getGraph().getRule().getMultiMappings().add(originGraphElement, imageGraphElement);
			}
		}
	}
	
	private static void unmap(GraphElement originGraphElement, GraphElement imageGraphElement) {
		if ((originGraphElement instanceof Node) && (imageGraphElement instanceof Node)) {
			
			// LHS to RHS node mapping: 
			if (originGraphElement.getGraph().getRule() == imageGraphElement.getGraph().getRule()) {
				if (originGraphElement.getGraph().isLhs() && imageGraphElement.getGraph().isRhs()) {
					originGraphElement.getGraph().getRule().getMappings().remove(originGraphElement, imageGraphElement);
				}
			}
			
			// Application Condition:
			if (imageGraphElement.getGraph().isNestedCondition()) {
				((NestedCondition) imageGraphElement.getGraph()).getMappings().remove(originGraphElement, imageGraphElement);
			}
			
			// multi-rule node mapping:
			if (originGraphElement.getGraph().getRule().getMultiRules().contains(imageGraphElement.getGraph().getRule())) {
				imageGraphElement.getGraph().getRule().getMultiMappings().remove(originGraphElement, imageGraphElement);
			}
		}
	}
	
	private static void unmap(GraphElement graphElement, MappingList mappings) {
		if (graphElement instanceof Node) {
			for (Iterator<Mapping> iterator = mappings.iterator(); iterator.hasNext();) {
				Mapping mapping = iterator.next();
				
				if ((mapping.getImage() == graphElement) || (mapping.getOrigin() == graphElement)) {
					iterator.remove();
				}
			}
		}
	}
	
	private static <E extends GraphElement> E getRemoteGraphElement(E graphElement) {
		
		if (graphElement.getGraph().isLhs()) {
			return graphElement.getGraph().getRule().getMappings().getImage(graphElement, graphElement.getGraph().getRule().getRhs());
		} else if (graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getMappings().getOrigin(graphElement);
		} else if (graphElement.getGraph().isNestedCondition()) {
			return ((NestedCondition) graphElement.getGraph().eContainer()).getMappings().getOrigin(graphElement);
		}

		return null;
	}
	
	private static <E extends GraphElement> E getApplicationConditionGraphElement(Graph acGraph, E graphElement, boolean create) {
		
		if (acGraph.isNestedCondition()) {
			NestedCondition ac = (NestedCondition) acGraph.eContainer();
			E acGraphElement = ac.getMappings().getImage(graphElement, acGraph);
			
			if (create) {
				acGraphElement = fix_getApplicationConditionGraphElement(ac, graphElement, acGraphElement);
			}
			
			return acGraphElement;
		}
			
		return null;
	}
	
	protected static <E extends GraphElement> E fix_getApplicationConditionGraphElement(NestedCondition ac, E graphElement, E acGraphElement) {
		if (acGraphElement == null) {
			createApplicationConditionGraphElement(ac, graphElement);
		}
		return acGraphElement;
	}
	
	private static GraphElement createApplicationConditionGraphElement(NestedCondition ac, GraphElement graphElement) {
		GraphElement acGraphElement = copy(ac.getConclusion(), graphElement);
		map(graphElement, acGraphElement);
		add(ac.getConclusion(), acGraphElement);

		return acGraphElement;
	}
	
	private static <E extends GraphElement> E getMultiGraphElement(E kernelGraphElement, Rule multiRule, boolean create) {
		Graph multiGraph = getMultiGraph(multiRule, kernelGraphElement);
		
		if (multiGraph != null) {
			E multiGraphElement = multiRule.getMultiMappings().getImage(kernelGraphElement, multiGraph);
			
			if (create){
				multiGraphElement = fix_getMultiGraphElement(multiRule, kernelGraphElement, multiGraphElement);
			}
			
			return multiGraphElement;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E fix_getMultiGraphElement(Rule multiRule, E kernelGraphElement, E multiGraphElement) {
		if (multiGraphElement == null) {
			return (E) createMultiGraphElement(multiRule, kernelGraphElement);
		}
		return multiGraphElement;
	}
	
	private static GraphElement createMultiGraphElement(Rule multiRule, GraphElement kernelGraphElement) {
		Graph multiGraph = getMultiGraph(multiRule, kernelGraphElement);
		
		if (multiGraph != null) {
			GraphElement multiGraphElement = copy(multiGraph, kernelGraphElement);
			map(multiGraphElement, multiGraphElement);
			add(multiGraph, multiGraphElement);
			
			return multiGraphElement;
		}
		
		return null;
	}
	
	private static <E extends GraphElement> E getKernelGraphElement(E multilGraphElement, Rule kernelRule, boolean create) {
		E kernelGraphElement = kernelRule.getMultiMappings().getOrigin(multilGraphElement);

		if (create){
			kernelGraphElement = fix_getMultiGraphElement(kernelRule, kernelGraphElement, multilGraphElement);
		}

		return kernelGraphElement;
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E fix_getKernelGraphElement(Rule kernelRule, E kernelGraphElement, E multiGraphElement) {
		if (kernelGraphElement == null) {
			return (E) createKernelGraphElement(kernelRule, multiGraphElement);
		}
		return kernelGraphElement;
	}
	
	private static GraphElement createKernelGraphElement(Rule kernelRule, GraphElement multiGraphElement) {
		Graph kernelGraph = getKernelGraph(kernelRule, multiGraphElement);
		
		if (kernelGraph != null) {
			GraphElement kernelGraphElement = copy(kernelGraph, multiGraphElement);
			map(kernelGraphElement, multiGraphElement);
			add(kernelGraph, multiGraphElement);
			
			return multiGraphElement;
		}
		
		return null;
	}
	
	private static Graph getMultiGraph(Rule multiRule, GraphElement kernelGraphElement) {
		
		if (kernelGraphElement.getGraph().isLhs()) {
			return multiRule.getLhs();
		}
		
		else if (kernelGraphElement.getGraph().isRhs()) {
			return multiRule.getRhs();
		}
		
		return null;
	}
	
	private static Graph getKernelGraph(Rule kernelRule, GraphElement multiGraphElement) {
		
		if (multiGraphElement.getGraph().isLhs()) {
			return kernelRule.getLhs();
		}
		
		else if (multiGraphElement.getGraph().isRhs()) {
			return kernelRule.getRhs();
		}
		
		return null;
	}
	
	private static Collection<NestedCondition> getApplicationConditions(GraphElement graphElement) {
		return graphElement.getGraph().getNestedConditions();
	}
	
	private static Collection<Rule> getMultiRules(GraphElement graphElement) {
		
		// first layer multi-rules:
		if (graphElement.getGraph().isLhs() || graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getMultiRules();
		}
		
		return Collections.emptyList();
	}
	
	private static Rule getKernelRule(GraphElement graphElement) {
		
		// direct kernel-rules:
		if (graphElement.getGraph().isLhs() || graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getKernelRule();
		}

		return null;
	}
}
