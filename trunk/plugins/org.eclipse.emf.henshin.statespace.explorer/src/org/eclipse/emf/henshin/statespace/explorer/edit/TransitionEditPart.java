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
package org.eclipse.emf.henshin.statespace.explorer.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.henshin.statespace.Transition;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * Edit part for transitions.
 * @author Christian Krause
 */
public class TransitionEditPart extends AbstractConnectionEditPart {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// Makes the connection show a feedback, when selected by the user.
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		
		// Create the connection:
		PolylineConnection connection;
		//try {
		//	Class<?> clazz = Class.forName("org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx");
		//	connection = (PolylineConnection) clazz.newInstance();
		//	clazz.getMethod("setSmoothness", int.class).invoke(connection,32);
		//} catch (Throwable t) {
			connection = new PolylineConnection();
		//}
		connection.setTargetDecoration(new PolygonDecoration());
		connection.setConnectionRouter(new BendpointConnectionRouter());
		
		// Add the label:
		Label label = new Label(getTransition().getLabel());
		connection.add(label);
		connection.getLayoutManager().setConstraint(label, new TransitionLabelLocator(getTransition(), connection));
		return connection;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		
		// Update bend points:
		int[][] bendpoints = TransitionBendpointHelper.getBendPoints(getTransition());
		List<RelativeBendpoint> relative = new ArrayList<RelativeBendpoint>();
		for (int i=0; i<bendpoints.length; i++) {
			RelativeBendpoint bendpoint = new RelativeBendpoint(getConnectionFigure());
			Dimension d1 = new Dimension(bendpoints[i][0], bendpoints[i][1]);
			Dimension d2 = new Dimension(bendpoints[i][2], bendpoints[i][3]);
			bendpoint.setRelativeDimensions(d1, d2);
			relative.add(bendpoint);
		}
		getConnectionFigure().setRoutingConstraint(relative);
		
	}
	
	public Transition getTransition() {
		return (Transition) getModel();
	}
	
}
