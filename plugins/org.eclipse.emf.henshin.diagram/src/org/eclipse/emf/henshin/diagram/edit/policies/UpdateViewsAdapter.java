package org.eclipse.emf.henshin.diagram.edit.policies;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.gef.EditPart;

public class UpdateViewsAdapter extends EContentAdapter {

	protected EditPart editpart;
	
	public UpdateViewsAdapter(EObject element, EditPart editpart) {
		this.editpart = editpart;
		
		if ((element != null) && (editpart != null)) {
			element.eAdapters().add(this);
		}
	}
	
	public void notifyChanged(Notification event) {
		super.notifyChanged(event);
		
    	if (event.getEventType() == Notification.REMOVING_ADAPTER) {
			return;
		}
    	
    	if ((event.getNotifier() == getTarget()) || (event.getOldValue() == getTarget()) || (event.getNewValue() == getTarget())) {
    		
    		// Really make sure that the edit part is still valid.
    		if (editpart.isActive() && (editpart.getParent() != null)) {
    			UpdateViewsUtil.update(editpart);
    		}
    	}
    }
	
	public void remove() {
		if ((getTarget() != null) && (getTarget().eAdapters() != null)) {
			getTarget().eAdapters().remove(this);
		}
	}
}
