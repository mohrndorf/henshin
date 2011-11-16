package org.eclipse.emf.henshin.statespace.hashcodes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.henshin.statespace.Model;

/**
 * Helper class for computing context hash codes.
 * @author Christian Krause
 */
class ContextHashCodeHelper extends HashMap<EObject,Integer> {

	// Serial Id, not really relevant here:
	private static final long serialVersionUID = 1L;
		
	// Number of context updates to be done:
	private static final int CONTEXT_UPDATES = 8;
	
	// The objects in the model:
	private EObject[] objects;
	
	// Indizes of the objects.
	private Map<EObject,Integer> indizes; 
	
	// Cached context hash codes.
	private int[] hashCodes, newHashCodes;
	
	// Cached cross reference matrix.
	private int[] crossReferences;
	
	/**
	 * Default constructor. Computes all context hash codes.
	 */
	ContextHashCodeHelper(Model model, 
			boolean useGraphEquality,
			boolean useObjectIdentities, 
			boolean useObjectAttributes) {
		
		super(model.getObjectCount());
		
		// Extract all relevant information:
		extractObjects(model);
		extractCrossReferences();

		// Initialize the context hash codes with the local hash codes:
		initContextHashCodes(model, useObjectIdentities, useObjectAttributes);

		// Update the context hash codes a fixed number of times:
		for (int i=0; i<CONTEXT_UPDATES; i++) {
			updateContextHashCodes(useGraphEquality);
		}
		
		// Now we can store them in the map:
		for (int i=0; i<objects.length; i++) {
			put(objects[i], hashCodes[i]);
		}
		
		// Cleanup:
		objects = null;
		indizes = null;
		hashCodes = null;
		crossReferences = null;
		
	}
	
	/**
	 * Extract the objects out of the model.
	 * 
	 * @param model Model to be analyzed.
	 */
	protected void extractObjects(Model model) {
		
		// Initialize object array and index map:
		objects = new EObject[model.getObjectCount()];
		indizes = new HashMap<EObject,Integer>();
		
		// Extract all objects:
		Iterator<EObject> iterator = model.getResource().getAllContents();
		int index = 0;
		while (iterator.hasNext()) {
			objects[index] = iterator.next();
			indizes.put(objects[index], index);
			index++;
		}
	}
	
	/**
	 * Initialize the hash codes arrays with the local hash codes.
	 * 
	 * @param model Model to be analyzed.
	 * @param useObjectIdentities Whether to use the object identities.
	 * @param useObjectAttributes Whether to use the object attributes.
	 */
	protected void initContextHashCodes(Model model,
			boolean useObjectIdentities, 
			boolean useObjectAttributes) {
		
		// We store the hash codes in two arrays:
		hashCodes    = new int[objects.length];
		newHashCodes = new int[objects.length];
		
		// Initialize main array with local hash codes:
		for (int i=0; i<objects.length; i++) {
			
			// Do we need he object identity?
			int objectId = 0;
			if (useObjectIdentities) {
				Integer id = model.getObjectIdentitiesMap().get(objects[i]);
				if (id==null) {
					throw new RuntimeException("Missing object identity for " + objects[i]);
				}
				objectId = id;
			}
			
			// Now compute the local hash code:
			hashCodes[i] = LocalHashCodeHelper.hashCode(objects[i], objectId, useObjectAttributes);	
		}
	}
	

	/**
	 * Compute a matrix with cross references for the objects.
	 */
	@SuppressWarnings("unchecked")
	private void extractCrossReferences() {
		
		// Java initializes the array with zeros:
		crossReferences = new int[objects.length * objects.length];
		
		// Now fill the array:
		int row, column, mask;
		for (row=0; row<objects.length; row++) {
			EObject object = objects[row];
			mask = 1;
			
			// Check for the container reference:
			EObject container = object.eContainer();
			if (container!=null) {
				column = indizes.get(container);
				crossReferences[row * objects.length + column] |= mask;
			}
			mask <<= 1;
			
			// Now iterate over all real references:
			for (EReference reference : object.eClass().getEAllReferences()) {
				if (reference.isMany()) {
					EList<EObject> targets = (EList<EObject>) object.eGet(reference);
					for (EObject target : targets) {
						column = indizes.get(target);
						crossReferences[row * objects.length + column] |= mask;
					}
				} else {
					EObject target = (EObject) object.eGet(reference);
					if (target!=null) {
						column = indizes.get(target);
						crossReferences[row * objects.length + column] |= mask;
					}
				}
				mask <<= 1;
			}
		}
	}


	/**
	 * Update the hash codes based on the objects contexts.
	 */
	protected void updateContextHashCodes(boolean useGraphEquality) {
		
		// Compute the new context hash codes based on the old ones:
		for (int index=0; index<objects.length; index++) {
			
			// Start with the old value:
			newHashCodes[index] = hashCodes[index];
			
			// Check outgoing references:
			for (int column=0; column<objects.length; column++) {
				if (!useGraphEquality) {
				//	newHashCodes[index] *= 17;
				}
				int outgoingRefs = crossReferences[index * objects.length + column];
				newHashCodes[index] += (hashCodes[column] * outgoingRefs);
			}
			
			// We incorporate the difference between incoming and outgoing references:
			//newHashCodes[index] *= 31;

			// Check incoming references:
			for (int row=0; row<objects.length; row++) {
				if (!useGraphEquality) {
				//	newHashCodes[index] *= 17;
				}
				int incomingRefs = crossReferences[row * objects.length + index];
				newHashCodes[index] += (hashCodes[row] * incomingRefs);
			}

		}
		
		// Update the hash codes with the new values:
		System.arraycopy(newHashCodes, 0, hashCodes, 0, hashCodes.length);
		
	}
	
}
