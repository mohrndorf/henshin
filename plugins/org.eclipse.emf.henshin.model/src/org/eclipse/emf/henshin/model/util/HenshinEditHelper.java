package org.eclipse.emf.henshin.model.util;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.GraphElement;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;

public class HenshinEditHelper {

	public static void remove(GraphElement graphElement) {
		
		// update rule:
		if (graphElement instanceof Node) {
			Node node = (Node) graphElement;
			
			for (Edge outgoing : node.getOutgoing()) {
				remove(outgoing);
			}
			for (Edge incoming : node.getIncoming()) {
				remove(incoming);
			}
		}
		
		if ((graphElement instanceof Node) || (graphElement instanceof Edge) || (graphElement instanceof Attribute)) {
			EcoreUtil.delete(graphElement);
		}
		
		// update multi-rules: 
		if ((graphElement instanceof Node) || (graphElement instanceof Edge) || (graphElement instanceof Attribute)) {
			for (Rule multiRule : getMultiRules(graphElement)) {
				remove(getMultiGraphElement(graphElement, multiRule));
			}
		}
	}
	
	public static void add(Node node, Attribute attribute) {
		
		// update rule:
		node.getAttributes().add(attribute);
		
		// update multi-rules: 
		for (Rule multiRule : getMultiRules(attribute)) {
			add(getMultiGraphElement(node, multiRule), copy(attribute));
		}
	}
	
	public static void add(Graph graph, GraphElement graphElement) {
		
		// update rule:
		if (graphElement instanceof Node) {
			graph.getNodes().add((Node) graphElement);
		}
		
		else if (graphElement instanceof Edge) {
			graph.getEdges().add((Edge) graphElement);
		}
		
		// update multi-rules: 
		if ((graphElement instanceof Node) || (graphElement instanceof Edge)) {
			for (Rule multiRule : getMultiRules(graphElement)) {
				Graph multiGraph = getMultiGraph(multiRule, graphElement);
				
				if (multiGraph != null) {
					GraphElement multiGraphElement = copy(graphElement, multiRule);
					map(multiGraphElement, multiGraphElement);
					add(multiGraph, multiGraphElement);
				}
			}
		}
	}
	
	private static GraphElement copy(GraphElement kernelGraphElement, Rule multiRule) {
		
		// copy (without adding):
		if (kernelGraphElement instanceof Node) {
			Node node = (Node) kernelGraphElement;
			
			Node copy = HenshinFactory.eINSTANCE.createNode();
			copy.setDescription(node.getDescription());
			copy.setName(node.getName());
			copy.setType(node.getType());
			
			return copy;
		}
		
		else if (kernelGraphElement instanceof Edge) {
			Edge edge = (Edge) kernelGraphElement;
			
			Edge copy = HenshinFactory.eINSTANCE.createEdge();
			copy.setIndex(edge.getIndex());
			copy.setSource(getMultiGraphElement(edge.getSource(), multiRule));
			copy.setTarget(getMultiGraphElement(edge.getTarget(), multiRule));
			copy.setType(edge.getType());
		}
		
		return null;
	}
	
	private static Attribute copy(Attribute attribute) {
		
		// copy (without adding):
		Attribute copy = HenshinFactory.eINSTANCE.createAttribute();
		copy.setType(attribute.getType());
		copy.setValue(attribute.getValue());
		
		return copy;
	}
	
	private static void map(GraphElement originGraphElement, GraphElement targetGraphElement) {
		if ((originGraphElement instanceof Node) && (targetGraphElement instanceof Node)) {
			
			// LHS to RHS node mapping: 
			if (originGraphElement.getGraph().getRule() == targetGraphElement.getGraph().getRule()) {
				if (originGraphElement.getGraph().isLhs() && targetGraphElement.getGraph().isRhs()) {
					originGraphElement.getGraph().getRule().getMappings().add(originGraphElement, targetGraphElement);
				}
			}
			
			// multi-rule node mapping:
			if (originGraphElement.getGraph().getRule().getMultiRules().contains(targetGraphElement.getGraph().getRule())) {
				targetGraphElement.getGraph().getRule().getMultiMappings().add(originGraphElement, targetGraphElement);
			}
		}
	}
	
	private static <E extends GraphElement> E getMultiGraphElement(E kernelGraphElement, Rule multiRule) {
		return multiRule.getMultiMappings().getImage(kernelGraphElement, getMultiGraph(multiRule, kernelGraphElement));
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
	
	private static Collection<Rule> getMultiRules(GraphElement graphElement) {
		
		// first layer multi-rules:
		if (graphElement.getGraph().isLhs() || graphElement.getGraph().isRhs()) {
			return graphElement.getGraph().getRule().getMultiRules();
		}
		
		return Collections.emptyList();
	}
}
