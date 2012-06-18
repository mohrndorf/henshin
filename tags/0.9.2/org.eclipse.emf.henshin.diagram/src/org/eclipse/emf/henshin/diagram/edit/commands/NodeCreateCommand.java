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
package org.eclipse.emf.henshin.diagram.edit.commands;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.diagram.edit.helpers.EClassComparator;
import org.eclipse.emf.henshin.diagram.edit.helpers.RootObjectEditHelper;
import org.eclipse.emf.henshin.diagram.edit.helpers.TransformationSystemEditHelper;
import org.eclipse.emf.henshin.diagram.part.HenshinDiagramEditorPlugin;
import org.eclipse.emf.henshin.diagram.part.HenshinPaletteTools.EClassNodeTool;
import org.eclipse.emf.henshin.diagram.part.Messages;
import org.eclipse.emf.henshin.diagram.providers.ActionColorProvider;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.TransformationSystem;
import org.eclipse.emf.henshin.model.actions.Action;
import org.eclipse.emf.henshin.model.actions.ActionType;
import org.eclipse.emf.henshin.model.actions.HenshinActionHelper;
import org.eclipse.emf.henshin.presentation.HenshinIcons;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @generated
 */
public class NodeCreateCommand extends EditElementCommand {

	private static final Comparator<EClassifier> ECLASS_COMPARATOR = new EClassComparator();

	private static final ILabelProvider labelProvider = new LabelProvider() {

		@Override
		public String getText(Object element) {
			return ((EClassifier) element).getName();
		}

		@Override
		public Image getImage(Object element) {
			return HenshinIcons.ECLASS;
		}
	};

	private final Shell shell = HenshinDiagramEditorPlugin.getInstance()
			.getWorkbench().getDisplay().getActiveShell();

	/**
	 * @generated
	 */
	public NodeCreateCommand(CreateElementRequest req) {
		super(req.getLabel(), null, req);
	}

	/**
	 * FIXME: replace with setElementToEdit()
	 * 
	 * @generated
	 */
	protected EObject getElementToEdit() {
		EObject container = ((CreateElementRequest) getRequest())
				.getContainer();
		if (container instanceof View) {
			container = ((View) container).getElement();
		}
		return container;
	}

	/**
	 * @generated
	 */
	public boolean canExecute() {
		return true;

	}

	/**
	 * @generated NOT
	 */
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor,
			IAdaptable info) throws ExecutionException {

		// The node is created in the context of a rule (PRESERVE-action):
		Rule rule = (Rule) getElementToEdit();
		if (rule.getLhs() == null) {
			Graph lhs = HenshinFactory.eINSTANCE.createGraph();
			lhs.setName("LHS");
			rule.setLhs(lhs);
		}
		if (rule.getRhs() == null) {
			Graph rhs = HenshinFactory.eINSTANCE.createGraph();
			rhs.setName("RHS");
			rule.setRhs(rhs);
		}

		// Create two new node instances:
		Node lhsNode = HenshinFactory.eINSTANCE.createNode();
		Node rhsNode = HenshinFactory.eINSTANCE.createNode();

		// Add them to the LHS / RHS:
		rule.getLhs().getNodes().add(lhsNode);
		rule.getRhs().getNodes().add(rhsNode);

		// Create a mapping:
		Mapping mapping = HenshinFactory.eINSTANCE.createMapping();
		mapping.setOrigin(lhsNode);
		mapping.setImage(rhsNode);
		rule.getMappings().add(mapping);

		// Set the type of the nodes:
		CreateElementRequest request = (CreateElementRequest) getRequest();
		Object type = request.getParameter(EClassNodeTool.TYPE_PARAMETER_KEY);

		TransformationSystem ts = rule.getTransformationSystem();
		SingleEClassifierSelectionDialog dialog = null;

		// if no type has been specified yet, let the user choose one
		if (type == null) {
			dialog = new SingleEClassifierSelectionDialog(ts);
			type = dialog.openAndReturnSelection();
		}

		if (type != null && type instanceof EClass) {
			EClass eclass = (EClass) type;
			lhsNode.setType(eclass);
			rhsNode.setType(eclass);
		} else {
			return CommandResult.newCancelledCommandResult();
		}

		// Update the root containment for the new node:
		View ruleView = RootObjectEditHelper.findRuleView(rule);
		RootObjectEditHelper.updateRootContainment(ruleView, lhsNode);

		// Finally, we set the user-defined action:
		if (dialog!=null) {
			HenshinActionHelper.setAction(lhsNode, dialog.getAction());
		}
		
		// This shouldn't do anything, but we call it to be sure:
		doConfigure(lhsNode, monitor, info);

		request.setNewElement(lhsNode);
		return CommandResult.newOKCommandResult(lhsNode);

	}

	/**
	 * @generated
	 */
	protected void doConfigure(Node newElement, IProgressMonitor monitor,
			IAdaptable info) throws ExecutionException {
		IElementType elementType = ((CreateElementRequest) getRequest())
				.getElementType();
		ConfigureRequest configureRequest = new ConfigureRequest(
				getEditingDomain(), newElement, elementType);
		configureRequest.setClientContext(((CreateElementRequest) getRequest())
				.getClientContext());
		configureRequest.addParameters(getRequest().getParameters());
		ICommand configureCommand = elementType
				.getEditCommand(configureRequest);
		if (configureCommand != null && configureCommand.canExecute()) {
			configureCommand.execute(monitor, info);
		}
	}

	/**
	 * Dialog for choosing an EClassifier in order to specify the node type to
	 * be created.
	 * 
	 * @author Stefan Jurack (sjurack), Christian Krause
	 * 
	 */
	private final class SingleEClassifierSelectionDialog extends
			ElementListSelectionDialog {

		final TransformationSystem ts;

		private Action action;
		
		/**
		 * Constructor
		 * 
		 * @param ts
		 */
		public SingleEClassifierSelectionDialog(TransformationSystem ts) {
			super(shell, labelProvider);
			this.setMultipleSelection(false);
			this.setBlockOnOpen(true);
			this.setTitle(Messages.SingleEClassifierSelectionDialog_title);
			this.setMessage(Messages.SingleEClassifierSelectionDialog_msg);
			this.ts = ts;
			this.action = null;
		}// constructor

		/**
		 * Opens the dialog and returns the first selected EClassifier in the
		 * list. If no EClassifier is available in the TransformationSystem or
		 * the dialog is canceled, null is returned.
		 * 
		 * @return
		 */
		public final EClassifier openAndReturnSelection() {

			final List<EClassifier> elements = TransformationSystemEditHelper
					.collectAllEClassifier(ts);

			EClassifier result = null;
			if (elements.size() > 0) {

				Collections.sort(elements, ECLASS_COMPARATOR);
				this.setElements(elements.toArray());
				int returnCode = this.open();
				if (returnCode == ElementListSelectionDialog.OK) {
					result = (EClassifier) getFirstResult();
				}// if
			}// if

			return result;
		}// openAndReturnElement

	    /*
	     * @see Dialog#createDialogArea(Composite)
	     */
		@Override
	    protected Control createDialogArea(Composite parent) {
	        Composite contents = (Composite) super.createDialogArea(parent);

	        // Action group:
	    	Group group = new Group(contents, SWT.NONE);
	        GridData data = new GridData();
	        data.grabExcessHorizontalSpace = true;
	        data.horizontalAlignment = GridData.FILL;
	        data.verticalAlignment = GridData.END;

	        group.setLayoutData(data);
	        group.setFont(parent.getFont());
	        group.setText("Action");
	        group.setLayout(new GridLayout(2, false));
	        
	        createLabel("Action Type:", group);	        
	        Composite buttons = new Composite(group, SWT.NONE);
	        buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
	        
	        // Action types as radio buttons:
	        boolean first = true;
	        for (ActionType type : ActionType.values()) {
	        	final Action current = new Action(type);
		        Button button = new Button(buttons, SWT.RADIO);
		        button.setText(type.toString());
		        button.setForeground(ActionColorProvider.getColor(current));
		        if (first) {
		        	button.setSelection(true);
		        	action = current;
		        }
		        button.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
			        	action = current;
					}
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}
		        });
		        first = false;
	        }
	        
	        // Amalgamated flag:
	        createLabel("Amalgamated:", group);
	        final Button amalgamated = new Button(group, SWT.CHECK);
	        amalgamated.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					action = new Action(action.getType(), 
							amalgamated.getSelection(), 
							action.getArguments());
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
	        });
	        
	        // Arguments:
	        createLabel("Arguments:", group);
	        final Text args = new Text(group, SWT.BORDER | SWT.SINGLE);
	        args.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        args.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String[] parsed = args.getText().split(",");
					action = new Action(action.getType(), 
							action.isAmalgamated(), 
							parsed);
				}
	        });
	        
	        return contents;
	    }
		
		private Label createLabel(String label, Composite grid) {
	        Label l = new Label(grid, SWT.NONE);
	        l.setText(label);
	        l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			applyDialogFont(l);
			return l;
		}

		public Action getAction() {
			return action;
		}

	}// inner class
	
}