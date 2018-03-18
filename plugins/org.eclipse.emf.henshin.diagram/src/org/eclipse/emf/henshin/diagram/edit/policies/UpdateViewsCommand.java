package org.eclipse.emf.henshin.diagram.edit.policies;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.henshin.diagram.edit.parts.NodeEditPart;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.LabelEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;

/*
 * Update views command.
 */
public class UpdateViewsCommand extends AbstractTransactionalCommand {

	protected EditPart editpart;
	
	public UpdateViewsCommand(EditPart editpart, TransactionalEditingDomain domain, List<?> affectedFiles) {
		super(domain, "Update Views", affectedFiles);
		this.editpart = editpart;
	}
	
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor,
			IAdaptable info) throws ExecutionException {
		
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
			}

			// Refresh connected nodes?
			if (editpart instanceof ConnectionEditPart) {
				updateConnection((ConnectionEditPart) editpart);
			}
			
			// Continue with the parent:
			editpart = editpart.getParent();
		}
		
		// Done.
		return CommandResult.newOKCommandResult();
		
	}
	
	/*
	 * Refresh the labels of a connection edit part.
	 */
	private void updateConnection(ConnectionEditPart connection) {
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
