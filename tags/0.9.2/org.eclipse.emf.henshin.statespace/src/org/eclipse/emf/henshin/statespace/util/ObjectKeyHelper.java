package org.eclipse.emf.henshin.statespace.util;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

/**
 * Helper class for dealing with object keys.
 * @author Christian Krause
 */
public class ObjectKeyHelper {
	
	/**
	 * Extract the object type of an object key. 
	 */
	public static EClass getObjectType(int objectKey, EList<EClass> identityTypes) {
		int index = ((objectKey >>> 24) & 0xFF)-1;
		if (index>=0 && index<identityTypes.size()) {
			return identityTypes.get(index);
		}
		return null;
	}

	/**
	 * Extract the supported type prefix of an object key. 
	 */
	public static String getObjectTypePrefix(int objectKey) {
		int index = ((objectKey >>> 24) & 0xFF)-1;
		return String.valueOf((char) ('a'+index));
	}

	/**
	 * Extract the object ID of an object identity. 
	 */
	public static int getObjectID(int objectKey) {
		return objectKey & 0xFFFFFF;
	}
	
	/**
	 * Extract the object type of an object key. 
	 */
	public static int createObjectKey(EClass type, int id, EList<EClass> identityTypes) {
		
		// Find out the type id:
		int typeId = identityTypes.indexOf(type) + 1;
		
		// If the type is unknown, the id also does not matter:
		if (typeId==0) {
			id = 0;
		}
		
		// Compose everything:
		return ((typeId & 0xFF) << 24) | (id & 0xFFFFFF);
	}
	
}