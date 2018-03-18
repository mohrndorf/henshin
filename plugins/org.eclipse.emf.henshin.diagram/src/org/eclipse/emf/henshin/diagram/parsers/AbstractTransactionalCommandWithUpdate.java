package org.eclipse.emf.henshin.diagram.parsers;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.henshin.diagram.edit.policies.ActionLabelDirectEditPolicy;
import org.eclipse.emf.henshin.diagram.edit.policies.UpdateViewsCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;

public abstract class AbstractTransactionalCommandWithUpdate extends AbstractTransactionalCommand {

	protected IAdaptable element;
	
	@SuppressWarnings("rawtypes")
	public AbstractTransactionalCommandWithUpdate(IAdaptable element, TransactionalEditingDomain domain, String label, List affectedFiles) {
		super(domain, label, affectedFiles);
		this.element = element;
	}

	@Override
	protected IStatus doUndo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus result = super.doUndo(monitor, info);
		
		// update visual:
		try {
			Field field = element.getClass().getDeclaredField("this$0");
			field.setAccessible(true);
			ActionLabelDirectEditPolicy outer = (ActionLabelDirectEditPolicy) field.get(element);
			
			UpdateViewsCommand update = new UpdateViewsCommand(outer.getHost(), getEditingDomain(), null);
			update.execute(monitor, info);
		} catch (Exception e) {
		}
		
		return result;
	}
	
	@Override
	protected IStatus doRedo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus result = super.doRedo(monitor, info);
		
		// update visual:
		try {
			Field field = element.getClass().getDeclaredField("this$0");
			field.setAccessible(true);
			ActionLabelDirectEditPolicy outer = (ActionLabelDirectEditPolicy) field.get(element);
			
			UpdateViewsCommand update = new UpdateViewsCommand(outer.getHost(), getEditingDomain(), null);
			update.execute(monitor, info);
		} catch (Exception e) {
		}
		
		return result;
	}
}
