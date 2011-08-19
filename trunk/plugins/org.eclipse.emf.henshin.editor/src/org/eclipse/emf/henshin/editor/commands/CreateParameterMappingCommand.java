/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University Berlin, 
 * Philipps-University Marburg and others. All rights reserved. 
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Philipps-University Marburg - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.editor.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Parameter;
import org.eclipse.emf.henshin.model.ParameterMapping;
import org.eclipse.emf.henshin.model.TransformationUnit;

/**
 * Creates a {@link ParameterMapping} between two given {@link Parameter}s.
 * 
 * 
 * @author Stefan Jurack (sjurack)
 */
public class CreateParameterMappingCommand extends AbstractCommand {
	
	protected Parameter source;
	protected Parameter target;
	TransformationUnit sourceUnit, targetUnit, ownerUnit;
	protected ParameterMapping mapping;
	protected Collection<?> affectedObjects;
	
	public CreateParameterMappingCommand(Parameter source, Parameter target) {
		this.source = source;
		this.target = target;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.common.command.AbstractCommand#prepare()
	 */
	@Override
	protected boolean prepare() {
		
		if (QuantUtil.anyNull(source, target) || QuantUtil.allIdentical(source, target))
			return false;
		
		/*
		 * 1) Check if one of both parameter's parent unit refers the other's
		 * parent unit. 2) Check if there already exists such a parameter
		 * mapping
		 */

		// (1)
		sourceUnit = source.getUnit();
		targetUnit = target.getUnit();
		
		if (sourceUnit.getSubUnits(false).contains(targetUnit))
			ownerUnit = sourceUnit;
		else if (targetUnit.getSubUnits(false).contains(sourceUnit))
			ownerUnit = targetUnit;
		else
			ownerUnit = null;
		
		if (ownerUnit == null) return false;
		
		// (2)
		for (ParameterMapping pm : ownerUnit.getParameterMappings()) {
			if (pm.getSource().equals(this.source) && pm.getTarget().equals(this.target))
				return false;
		}// for
		
		return true;
	}// prepare
	
	@Override
	public void execute() {
		
		mapping = HenshinFactory.eINSTANCE.createParameterMapping();
		mapping.setSource(source);
		mapping.setTarget(target);
		redo();
	}
	
	@Override
	public void redo() {
		ownerUnit.getParameterMappings().add(mapping);
		affectedObjects = Collections.singleton(mapping);
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}
	
	@Override
	public void undo() {
		ownerUnit.getParameterMappings().remove(mapping);
		List<Parameter> pList = new ArrayList<Parameter>();
		pList.add(this.source);
		pList.add(this.target);
		affectedObjects = pList;
	}
	
	@Override
	public Collection<?> getAffectedObjects() {
		return affectedObjects;
	}
	
}// class
