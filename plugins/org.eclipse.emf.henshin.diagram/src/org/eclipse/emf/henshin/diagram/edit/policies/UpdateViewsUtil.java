package org.eclipse.emf.henshin.diagram.edit.policies;

import org.eclipse.emf.henshin.diagram.edit.parts.AttributeEditPart;
import org.eclipse.emf.henshin.diagram.edit.parts.EdgeEditPart;
import org.eclipse.emf.henshin.diagram.edit.parts.NodeCompartmentEditPart;
import org.eclipse.emf.henshin.diagram.edit.parts.NodeEditPart;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;

public class UpdateViewsUtil {

	public static void update(EditPart editpart) {
		
		// Refresh all edit parts:
		while (editpart!=null) {
			
			// Refresh the edit part itself:
			editpart.refresh();
			
			// Canonical edit policy found?
			EditPolicy policy = editpart.getEditPolicy(EditPolicyRoles.CANONICAL_ROLE);				
			if (policy instanceof CanonicalEditPolicy) {
				((CanonicalEditPolicy) policy).refresh();
			}
			
			// Refresh connections?
			if (editpart instanceof NodeEditPart) {
				for (Object con : ((NodeEditPart) editpart).getSourceConnections()) {
					updateConnection((ConnectionEditPart) con);
				}
				for (Object con : ((NodeEditPart) editpart).getTargetConnections()) {
					updateConnection((ConnectionEditPart) con);
				}
				for (Object child : editpart.getChildren()) {
					if (child instanceof NodeCompartmentEditPart) {
						for (Object subChild : ((NodeCompartmentEditPart) child).getChildren()) {
							if (subChild instanceof AttributeEditPart) {
								((AttributeEditPart) subChild).refresh();
							}
						}
					}
				}
			}
			
			// Refresh connected nodes?
			if (editpart instanceof ConnectionEditPart) {
				updateConnection((ConnectionEditPart) editpart);
			}
			
			// Refresh local graph?
			if (editpart instanceof EdgeEditPart) {
				EditPart source = ((EdgeEditPart) editpart).getSource();
				
				if (source instanceof NodeEditPart) {
					for (Object con : ((NodeEditPart) source).getSourceConnections()) {
						updateConnection((ConnectionEditPart) con);
					}
					for (Object con : ((NodeEditPart) source).getTargetConnections()) {
						updateConnection((ConnectionEditPart) con);
					}
					for (Object child : ((NodeEditPart) source).getChildren()) {
						if (child instanceof AttributeEditPart) {
							((AttributeEditPart) child).refresh();
						}
					}
				}
				
				EditPart target = ((EdgeEditPart) editpart).getTarget();
				
				if (target instanceof NodeEditPart) {
					for (Object con : ((NodeEditPart) target).getSourceConnections()) {
						updateConnection((ConnectionEditPart) con);
					}
					for (Object con : ((NodeEditPart) target).getTargetConnections()) {
						updateConnection((ConnectionEditPart) con);
					}
					for (Object child : ((NodeEditPart) target).getChildren()) {
						if (child instanceof AttributeEditPart) {
							((AttributeEditPart) child).refresh();
						}
					}
				}
			}
			
			// Continue with the parent:
			editpart = editpart.getParent();
		}
	}
	
	/*
	 * Refresh the labels of a connection edit part.
	 */
	public static void updateConnection(ConnectionEditPart connection) {
		connection.refresh();
		
		if (connection.getSource()!=null) {
			connection.getSource().refresh();
		}
		
		if (connection.getTarget()!=null) {
			connection.getTarget().refresh();
		}
		
		for (Object child : connection.getChildren()) {
			if (child instanceof LabelEditPart) {
				((LabelEditPart) child).refresh();
			}
		}
	}
}
