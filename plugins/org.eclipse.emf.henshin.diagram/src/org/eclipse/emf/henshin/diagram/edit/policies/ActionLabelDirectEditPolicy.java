/**
 * <copyright>
 * Copyright (c) 2010-2014 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.diagram.edit.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.LabelDirectEditPolicy;

/**
 * A label direct edit policy for action labels.
 * @generated NOT
 * @author Christian Krause
 */
public class ActionLabelDirectEditPolicy extends LabelDirectEditPolicy {
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.LabelDirectEditPolicy#getDirectEditCommand(org.eclipse.gef.requests.DirectEditRequest)
	 */
	@Override
	protected Command getDirectEditCommand(DirectEditRequest request) {
		
		// Get the parse command:
		Command command = super.getDirectEditCommand(request);
		if (command==null) return null;
		
		// Add refresh commands:
		CompoundCommand result = new CompoundCommand();
		result.add(command);
		IGraphicalEditPart host = (IGraphicalEditPart) getHost();
		result.add(new ICommandProxy(new UpdateViewsCommand(host, host.getEditingDomain(), null)));
		
		return result;
	}
}
