package org.eclipse.emf.henshin.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.emf.henshin.model.MappingList;
import org.eclipse.emf.henshin.model.NestedCondition;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;

public class HenshinEditHelper {
	
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
	
	protected static void fix_remove(Node node) {
		
		// remove dangling edges:
		for (Edge outgoing : new ArrayList<>(node.getOutgoing())) {
			remove(outgoing);
		}
		for (Edge incoming : new ArrayList<>(node.getIncoming())) {
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
	
	public static void add(Node targetNode, Attribute attribute) {
		
		// update rule:
		targetNode.getAttributes().add(attribute);
	}
	
	public static void add(Graph graph, GraphElement graphElement) {
		
		// update rule:
		if (graphElement instanceof Node) {
			graph.getNodes().add((Node) graphElement);
		}
		
		else if (graphElement instanceof Edge) {
			graph.getEdges().add((Edge) graphElement);
		}
	}
	
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
			
			// update multi-rule:
			for (Rule multiRule : rule.getMultiRules()) {
				HenshinModelCleaner.completeMultiRules(multiRule);
				
				// update application conditions:
				for (NestedCondition ac : multiRule.getLhs().getNestedConditions()) {
					updateNestedCondition(ac);
				}
			}
			
			// update application conditions:
			for (NestedCondition ac : rule.getLhs().getNestedConditions()) {
				updateNestedCondition(ac);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void updateNestedCondition(NestedCondition ac) {
		
		// remove none context nodes:
		for (Node node : new ArrayList<>(ac.getConclusion().getNodes())) {
			if ((getRemoteGraphElement(node) != null) && node.getOutgoing().isEmpty() && node.getIncoming().isEmpty() ) {
				remove(node);
			}
		}
	}
	
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
	
	protected static void setEdgeContext(Graph targetGraph, Edge targetEdge, Edge remoteEdge) {
		
		// set to multi-rule:
		if (getMultiRules(remoteEdge).contains(targetGraph.getRule())) {
			targetEdge.setSource(getMultiGraphElement(remoteEdge.getSource(), targetGraph.getRule(), true));
			targetEdge.setTarget(getMultiGraphElement(remoteEdge.getTarget(), targetGraph.getRule(), true));
		}
		
		// set to kernel-rule:
		else if (getKernelRule(remoteEdge) == targetGraph.getRule()) {
			targetEdge.setSource(getKernelGraphElement(remoteEdge.getSource(), targetGraph.getRule(), true));
			targetEdge.setTarget(getKernelGraphElement(remoteEdge.getTarget(), targetGraph.getRule(), true));
		}
		
		// set to rule:
		else if (remoteEdge.getGraph().getRule() == targetGraph.getRule()) {
			
			// set to same graph:
			if (remoteEdge.getGraph() == targetGraph) {
				targetEdge.setSource(remoteEdge.getSource());
				targetEdge.setTarget(remoteEdge.getTarget());
			} else {
				
				// set to application condition
				if (targetGraph.isNestedCondition()) {
					targetEdge.setSource(getApplicationConditionGraphElement(targetGraph, remoteEdge.getSource(), true));
					targetEdge.setTarget(getApplicationConditionGraphElement(targetGraph, remoteEdge.getTarget(), true));
				} 
				
				// set to LHS/RHS:
				else {
					// LHS/RHS to RHS/LHS:
					// application condition to LHS:
					Node source = workaround_edgeContext(remoteEdge.getSource());
					Node target = workaround_edgeContext(remoteEdge.getTarget());
					
					// LHS to RHS
					if (targetGraph.isRhs() && source.getGraph().isLhs()) {
						source = workaround_edgeContext(source);
					}
					if (targetGraph.isRhs() && target.getGraph().isLhs()) {
						target = workaround_edgeContext(target);
					}
					
					targetEdge.setSource(source);
					targetEdge.setTarget(target);
				}
			}
		}
	}
	
	protected static Node workaround_edgeContext(Node context) {
		Node remoteContext = getRemoteGraphElement(context);
		
		if (remoteContext != null) {
			return remoteContext;
		}
		
		return context;
	}
	
	protected static void fix_edgeContext(Node origin, Node image) {
		
		if ((origin != null) && (image != null)) {
			
			// move invalid image edge to origin:
			for (Edge outgoing : new ArrayList<>(image.getOutgoing())) {
				if ((outgoing.getSource() != null) && (outgoing.getTarget() != null)) {
					if (!isValid(outgoing)) {
						if (origin.getGraph() == outgoing.getTarget().getGraph()) {
							outgoing.setSource(origin);
							outgoing.setGraph(origin.getGraph());
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
							incoming.setTarget(origin);
							incoming.setGraph(origin.getGraph());
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
							outgoing.setSource(image);
							outgoing.setGraph(image.getGraph());
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
							incoming.setTarget(image);
							incoming.setGraph(image.getGraph());
						}
					}
				} else {
					EcoreUtil.delete(incoming);
				}
			}
		}
	}
	
	public static boolean isValid(Edge edge) {
		return (edge.getSource() != null) && (edge.getTarget() != null) && (edge.getSource().getGraph() == edge.getGraph()) && (edge.getTarget().getGraph() == edge.getGraph());
	}
	
	public static void move(Graph targetGraph, GraphElement graphElement)  {
		Rule targetRule = targetGraph.getRule();

		// update multi-rules:
		for (Rule multiRule : getMultiRules(graphElement)) {
			GraphElement multiGraphElement = getMultiGraphElement(graphElement, multiRule, false);

			if (multiGraphElement != null) {
				if (targetGraph.isLhs() || targetGraph.isRhs()) {
					Graph multiGraph = getMultiGraph(multiRule, targetGraph);
					move(multiGraph, multiGraphElement);
				} else {
					// moved to application condition:
					remove(multiGraphElement);
				}
			}
		}
		
		// update rule:
		if (graphElement.getGraph().getRule() == targetRule) {

			// move outgoing edges:
			if (graphElement instanceof Node) {
				for (Edge outgoing : new ArrayList<>(((Node) graphElement).getOutgoing())) {
					move(targetGraph, outgoing);
				}
				
				for (Edge incoming : new ArrayList<>(((Node) graphElement).getIncoming())) {
					move(targetGraph, incoming);
				}
				
				// move element:
				add(targetGraph, graphElement);
			}
			
			// move source and target ends:
			else if (graphElement instanceof Edge) {
				Edge edge = (Edge) graphElement;
				setEdgeContext(targetGraph, edge, edge);
				
				// move element:
				add(targetGraph, graphElement);
			}
			
			// move attribute:
			else if (graphElement instanceof Attribute) {
				Attribute attribute = (Attribute) graphElement;
				Node targetNode = null;
				
				if (targetGraph.isNestedCondition()) {
					// move to application condition:
					targetNode = getApplicationConditionGraphElement(targetGraph, attribute.getNode(), true);
				} else {
					// move LHS/RHS to RHS/LHS:
					targetNode = getRemoteGraphElement(attribute.getNode());
				}
				
				if (targetNode != null) {
					add(targetNode, attribute);
				}
			}
		}
	}
	
	public static void merge(Graph targetGraph, GraphElement retained, GraphElement merged) {
		
		// merge edges of nodes:
		if ((retained instanceof Node) && (merged instanceof Node)) {
			Node mergedNode = (Node) merged;
			Node retainedNode = (Node) retained;
			
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
					move(targetGraph, attribute);
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
	
	public static void map(GraphElement originGraphElement, GraphElement imageGraphElement) {
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
					
					// fix edge context workarounds:
					fix_edgeContext((Node) originGraphElement, (Node) imageGraphElement);
					
					// fix application context workaround:
					fix_applicationConditionContext((Node) originGraphElement, (Node) imageGraphElement);
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
	
	public static Graph getRemoteGraph(GraphElement graphElement) {
		
		if (graphElement.getGraph().isLhs()) {
			return graphElement.getGraph().getRule().getRhs();
		} else if (graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getLhs();
		} else if (graphElement.getGraph().isNestedCondition()) {
			return graphElement.getGraph().getRule().getLhs();
		}

		return null;
	}
	
	public static <E extends GraphElement> E getRemoteGraphElement(E graphElement) {
		
		if (graphElement.getGraph().isLhs()) {
			return graphElement.getGraph().getRule().getMappings().getImage(graphElement, graphElement.getGraph().getRule().getRhs());
		} else if (graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getMappings().getOrigin(graphElement);
		} else if (graphElement.getGraph().isNestedCondition()) {
			return ((NestedCondition) graphElement.getGraph().eContainer()).getMappings().getOrigin(graphElement);
		}

		return null;
	}
	
	private static MappingList getRemoteMappings(GraphElement graphElement) {
		
		if (graphElement.getGraph().isLhs()) {
			return graphElement.getGraph().getRule().getMappings();
		} else if (graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getMappings();
		} else if (graphElement.getGraph().isNestedCondition()) {
			return ((NestedCondition) graphElement.getGraph().eContainer()).getMappings();
		}

		return null;
	}
	
	private static <E extends GraphElement> E getApplicationConditionGraphElement(Graph acGraph, E graphElement, boolean create) {
		
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
			
			// Search mapped element:
			NestedCondition ac = (NestedCondition) acGraph.eContainer();
			E acGraphElement = ac.getMappings().getImage(graphElement, acGraph);
			
			if (create) {
				acGraphElement = fix_getApplicationConditionGraphElement(ac, graphElement, acGraphElement);
			}
			
			return acGraphElement;
		}
			
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected static <E extends GraphElement> E fix_getApplicationConditionGraphElement(NestedCondition ac, E graphElement, E acGraphElement) {
		if (acGraphElement == null) {
			return (E) createApplicationConditionGraphElement(ac, graphElement);
		}
		return acGraphElement;
	}
	
	private static GraphElement createApplicationConditionGraphElement(NestedCondition ac, GraphElement graphElement) {
		GraphElement acGraphElement = copy(ac.getConclusion(), graphElement);
		add(ac.getConclusion(), acGraphElement);
		map(graphElement, acGraphElement);

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
			add(multiGraph, multiGraphElement);
			map(kernelGraphElement, multiGraphElement);
			
			return multiGraphElement;
		}
		
		return null;
	}
	
	private static <E extends GraphElement> E getKernelGraphElement(E multilGraphElement, Rule kernelRule, boolean create) {
		E kernelGraphElement = multilGraphElement.getGraph().getRule().getMultiMappings().getOrigin(multilGraphElement);

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
	
	private static Graph getMultiGraph(Rule multiRule, Graph kernelGraph) {
		
		if (kernelGraph.isLhs()) {
			return multiRule.getLhs();
		}
		
		else if (kernelGraph.isRhs()) {
			return multiRule.getRhs();
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
