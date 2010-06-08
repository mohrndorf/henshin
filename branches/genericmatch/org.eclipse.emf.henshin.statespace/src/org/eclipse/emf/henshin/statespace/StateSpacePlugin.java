/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University of Berlin, 
 * University of Marburg and others. All rights reserved. 
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CWI Amsterdam - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.statespace;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.EMFPlugin;

import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.henshin.statespace.util.StateSpaceValidatorPlatformHelper;

/**
 * This is the central singleton for the StateSpace model plugin.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public final class StateSpacePlugin extends EMFPlugin {
	
	/**
	 * Plug-in ID.
	 * @generated NOT
	 */
	public static final String PLUGIN_ID = "org.eclipse.emf.henshin.statespace";
	
	/**
	 * Keep track of the singleton.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final StateSpacePlugin INSTANCE = new StateSpacePlugin();

	/**
	 * Keep track of the singleton.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static Implementation plugin;

	/**
	 * Registry for state space validators.
	 * @generated NOT
	 */	
	private Map<String,StateSpaceValidator> validators;
	
	/**
	 * Create the instance.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StateSpacePlugin() {
		super(new ResourceLocator [] {});
	}
	
	/**
	 * Get the map of registered state space validators.
	 * @return State space validator registry.
	 * @generated NOT
	 */
	public Map<String,StateSpaceValidator> getStateSpaceValidators() {
		if (validators==null) {
			validators = new HashMap<String,StateSpaceValidator>();
			try {
				// Load the validators registered via the platform's extension point mechanism.
				StateSpaceValidatorPlatformHelper.loadValidators();
			}
			catch (Throwable t) {
				// Not critical. Happens if the platform is not present.
			}
		}
		return validators;
	}
	
	/**
	 * Log an error.
	 * @param message Error message.
	 * @param t Exception.
	 * @generated NOT
	 */
	public void logError(String message, Throwable t) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, t));
	}
	
	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the singleton instance.
	 * @generated
	 */
	@Override
	public ResourceLocator getPluginResourceLocator() {
		return plugin;
	}

	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the singleton instance.
	 * @generated
	 */
	public static Implementation getPlugin() {
		return plugin;
	}
	
	/**
	 * The actual implementation of the Eclipse <b>Plugin</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static class Implementation extends EclipsePlugin {
		/**
		 * Creates an instance.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public Implementation() {
			super();

			// Remember the static instance.
			//
			plugin = this;
		}
	}

}
