/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University of Berlin, 
 * University of Marburg and others. All rights reserved. 
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Technical University of Berlin - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.internal.conditions.attribute;

import java.util.Collection;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class AttributeCondition {
	String conditionText;
	Collection<String> remainingParameters;

	ScriptEngine scriptEngine;

	public AttributeCondition(String condition,
			Collection<String> conditionParameters, ScriptEngine engine) {
		this.conditionText = condition;
		this.remainingParameters = conditionParameters;
		this.scriptEngine = engine;
	}

	public boolean eval() {
		if (remainingParameters.isEmpty()) {
			try {
				return (Boolean) scriptEngine.eval(conditionText);
			} catch (ScriptException ex) {
				ex.printStackTrace();
			} catch (ClassCastException ex) {
				System.err
						.println("Warning: Attribute condition did not return a boolean value");
			}
		}

		return true;
	}
	
	void addParameter(String parameterName) {
		remainingParameters.add(parameterName);
	}
	
	void removeParameter(String parameterName) {
		remainingParameters.remove(parameterName);
	}
}