package org.eclipse.emf.henshin.diagram.edit.policies;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
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
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) 
			throws ExecutionException {
		if (editpart.getViewer() != null) {
			UpdateViewsUtil.update(editpart);
		}
		return CommandResult.newOKCommandResult();
	}
}
