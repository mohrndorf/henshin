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
package org.eclipse.emf.henshin.interpreter.ui.wizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.henshin.model.TransformationSystem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * 
 * @author Gregor Bonifer
 * @author Stefan Jurack
 */
public class HenshinateHenshinFileHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveMenuSelection(event);
		
		if (selection.size() == 1) {
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IFile) {
				IFile iFile = (IFile) firstElement;
				if (!iFile.getFileExtension().equals("henshin")) return null;
				
				ResourceSet resourceSet = new ResourceSetImpl();
				Resource resource = resourceSet.getResource(
						URI.createPlatformResourceURI(iFile.getFullPath().toOSString(), true), true);
				
				if (resource.getContents().size() == 0) return null;
				EObject root = resource.getContents().get(0);
				if (root instanceof TransformationSystem) {
					TransformationSystem tSystem = (TransformationSystem) root;
					HenshinWizard tWiz = new HenshinWizard(tSystem);
					HenshinWizardDialog dialog = new HenshinWizardDialog(
							HandlerUtil.getActiveShell(event), tWiz);
					dialog.open();
				}
			}
		} else if (selection.size() == 2) {
			
		}
		
		return null;
	}
}