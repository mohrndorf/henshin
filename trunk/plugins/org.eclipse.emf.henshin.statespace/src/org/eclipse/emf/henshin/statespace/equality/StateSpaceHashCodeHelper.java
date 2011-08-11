/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University Berlin, 
 * Philipps-University Marburg and others. All rights reserved. 
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CWI Amsterdam - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.statespace.equality;

import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.henshin.statespace.Model;

/**
 * @generated NOT
 * @author Christian Krause
 */
public class StateSpaceHashCodeHelper {

	// The ten first prime numbers.
	private static int[] PRIMES = new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29 };
	
	/** 
	 * Map for storing local hash codes of objects.
	 * This map computes local hash codes on demand.
	 */
	protected class LocalHashCodes extends HashMap<EObject,Integer> {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Integer get(Object object) {
			Integer hash = super.get(object);
			if (hash==null) {
				hash = localHashCode((EObject) object);
				put((EObject) object, hash);
			}
			return hash;
		}
		
	};
	
	// Whether to use graph equality.
	private boolean graphEquality;

	// Whether to ignore node IDs.
	private boolean ignoreNodeIDs;

	// Whether to ignore attributes.
	private boolean ignoreAttributes;
	
	// Currently used EPackage (cached).
	private EPackage ePackage;
	
	// Hash code of EPackage's nsURI.
	private int nsURIHashCode;
	
	// Current model.
	private Model model;
	
	// Current hash-code tree:
	private HashCodeTree tree;
	
	// Currently used local hash codes:
	private LocalHashCodes localHashCodes;
	
	/**
	 * Default constructor.
	 * @param graphEquality Graph equality?
	 * @param ignoreAttributes Ignore attributes?
	 */
	public StateSpaceHashCodeHelper(boolean graphEquality, boolean ignoreNodeIDs, boolean ignoreAttributes) {
		this.graphEquality = graphEquality;
		this.ignoreNodeIDs = ignoreNodeIDs;
		this.ignoreAttributes = ignoreAttributes;
	}

	/**
	 * Compute the hash code for a given model.
	 * @generated NOT
	 */
	public int hashCode(Model model) {
		return hashCode(model, null);
	}

	/**
	 * Compute the hash code for a given model
	 * and update the hash-code tree.
	 * @generated NOT
	 */
	public int hashCode(Model model, HashCodeTree tree) {
		
		// Set the required fields:
		this.model = model;
		this.tree = tree;
		this.localHashCodes = new LocalHashCodes();
		
		// Reset the hash-code tree:
		if (tree!=null) {
			tree.clear();
		}
		
		// Compute the hash-code:
		int result = totalHashCode(model.getResource().getContents(), 0);
		
		// Reset the fields:
		this.model = null;
		this.tree = null;
		this.localHashCodes = null;
		
		// Done.
		return result;
		
	}

	/*
	 * Compute the total hash code of a list of EObjects.
	 * This delegates to #totalhashCode() for a single EObject.
	 */
	protected int totalHashCode(EList<EObject> nodes, int depth) {

		// We need to store the total hash codes of all nodes:
		int[] total = new int[nodes.size()];

		// Create the children in the tree if necessary:
		if (tree!=null && total.length>0) {
			tree.createChildren(total.length); // the cursor automatically goes down
		}
		
		// Compute the total hash codes of all nodes:
		for (int i=0; i<total.length; i++) {
			total[i] = totalHashCode(nodes.get(i), depth);			
			if (tree!=null) {
				tree.setHashCode(total[i]);
				tree.goRight();
			}
		}
		
		// Now merge them:
		int result = listHashCode(total, depth);
		
		// Update the tree:
		if (tree!=null) {
			if (total.length>0) {
				tree.goUp();
			}
			tree.setHashCode(result);
		}

		// Done.
		return result;
		
	}
	
	/*
	 * Compute the total hash code of a single node. This computes 
	 * the context-aware hash code of the current node and merges 
	 * it with the ones from the contents of the object. Hence,
	 * the method walks down the containment tree of the node.
	 */
	@SuppressWarnings("unchecked")
	protected int totalHashCode(EObject object, int depth) {
		
		// Context-aware hash code of the current object:
		int hash = contextHashCode(object);
		
		// Now the children:
		for (EReference reference : GraphModelCanonicalizer.getCanonicalContainmentOrder(object.eClass())) {
			EList<EObject> children;
			if (reference.isMany()) {
				children = (EList<EObject>) object.eGet(reference);
			} else {
				EObject child = (EObject) object.eGet(reference);
				children = new BasicEList<EObject>();
				if (child!=null) {
					children.add(child);
				}
			}
			hash = (hash * 31) + totalHashCode(children, depth+1);
		}
		
		//System.out.println("Computed hash code " + hash + " for " + object);
		
		// Done.
		return hash;
		
	}
	
	/*
	 * Compute the local hash code for a node. This is done
	 * based on the type of the node, its attribute values 
	 * and the number of references to other objects.
	 */
	protected int localHashCode(EObject object) {
		
		// Class and its features:
		EClass eclass = object.eClass();
		EList<EStructuralFeature> features = eclass.getEAllStructuralFeatures();
		
		// Use classifier ID:
		int hashCode = eclass.getClassifierID() + 1;
		
		// Use node IDs:
		if (!ignoreNodeIDs) {
			int id = model.getNodeIDsMap().get(object);
			hashCode = (hashCode * PRIMES[0]) + id;
		}
		
		// Use features:
		for (int i=0; i<features.size(); i++) {
			EStructuralFeature feature = features.get(i);
			int value = 0;
			if (feature.isMany()) {
				List<?> list = (List<?>) object.eGet(feature);
				if (feature instanceof EReference) {
					value = list.size();
				} else if (feature instanceof EAttribute) {
					value = ignoreAttributes ? 0 : list.hashCode();
				}
			} else {
				Object single = object.eGet(feature);
				if (single==null) {
					value = 0;
				} else if (feature instanceof EReference) {
					value = 1;
				} else if (feature instanceof EAttribute) {
					value = ignoreAttributes ? 0 : single.hashCode();
				}
			}
			hashCode = (hashCode * PRIMES[(i+1) % PRIMES.length]) + value;
		}
		
		// Update cached ePackage:
		if (ePackage!=eclass.getEPackage()) {
			ePackage = eclass.getEPackage();
			nsURIHashCode = (ePackage.getNsURI()!=null) ? ePackage.getNsURI().hashCode() : 0;
		}
		
		// Add the hash code of the EPackage's nsURI:
		return hashCode + nsURIHashCode;

	}
	
	/*
	 * Compute the context-aware hash code for a node. This combines 
	 * the local hash code of the node and its neighbors into a single hash code.
	 */
	@SuppressWarnings("unchecked")
	protected int contextHashCode(EObject node) {
		
		// Start with the local hash code of the node itself:
		int hashCode = localHashCodes.get(node);
		
		// Iterate over all references (no attributes now):
		EList<EReference> references = node.eClass().getEAllReferences();
		for (int i=0; i<references.size(); i++) {
			EReference reference = references.get(i);
			int value = 0;
			if (reference.isMany()) {
				List<EObject> list = (List<EObject>) node.eGet(reference);
				int[] local = new int[list.size()];
				for (int j=0; j<local.length; j++) {
					local[j] = localHashCodes.get(list.get(j));
				}
				value = listHashCode(local, i);
			} else {
				EObject object = (EObject) node.eGet(reference);
				if (object==null) {
					value = 0;
				} else {
					value = localHashCodes.get(object);
				}
			}
			hashCode = (hashCode * PRIMES[i % PRIMES.length]) + value;
		}
		
		return hashCode;
		
	}
	
	/*
	 * Combine a list of hash codes into one single hash code.
	 * Depending on the equality type, the list is treated as 
	 * a sequence or as a set.
	 */
	protected int listHashCode(int[] hashCodes, int depth) {
		int hash = 0;
		for (int i=0; i<hashCodes.length; i++) {
			if (!graphEquality) {
				hash *= PRIMES[depth % PRIMES.length];
			}
			hash += hashCodes[i];
		}
		return hash;
	}	
	
}