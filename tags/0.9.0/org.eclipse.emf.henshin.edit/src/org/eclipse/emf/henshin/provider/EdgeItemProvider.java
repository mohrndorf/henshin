/*******************************************************************************
 * Copyright (c) 2010 CWI Amsterdam, Technical University Berlin, 
 * Philipps-University Marburg and others. All rights reserved. 
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Technical University Berlin - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.henshin.provider;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemColorProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinPackage;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.util.HenshinRuleAnalysisUtil;
import org.eclipse.emf.henshin.provider.descriptors.EdgeSourcePropertyDescriptor;
import org.eclipse.emf.henshin.provider.descriptors.EdgeTargetPropertyDescriptor;
import org.eclipse.emf.henshin.provider.descriptors.EdgeTypePropertyDescriptor;
import org.eclipse.emf.henshin.provider.util.IconUtil;

/**
 * This is the item provider adapter for a {@link org.eclipse.emf.henshin.model.Edge} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class EdgeItemProvider extends HenshinItemProviderAdapter implements
		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider,
		IItemLabelProvider, IItemPropertySource, IItemColorProvider {
	/**
	 * This constructs an instance from a factory and a notifier. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EdgeItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	/**
	 * This returns the property descriptors for the adapted class. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addSourcePropertyDescriptor(object);
			addTargetPropertyDescriptor(object);
			addTypePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}
	
	/**
	 * This adds a property descriptor for the Source feature. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	protected void addSourcePropertyDescriptor(Object object) {
		
		itemPropertyDescriptors.add(new EdgeSourcePropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
				getResourceLocator(), getString("_UI_Edge_source_feature"), getString(
						"_UI_PropertyDescriptor_description", "_UI_Edge_source_feature",
						"_UI_Edge_type"), HenshinPackage.Literals.EDGE__SOURCE));
	}
	
	/**
	 * This adds a property descriptor for the Target feature. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	protected void addTargetPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(new EdgeTargetPropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
				getResourceLocator(), getString("_UI_Edge_target_feature"), getString(
						"_UI_PropertyDescriptor_description", "_UI_Edge_target_feature",
						"_UI_Edge_type"), HenshinPackage.Literals.EDGE__TARGET));
	}
	
	/**
	 * This adds a property descriptor for the Type feature. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	protected void addTypePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(new EdgeTypePropertyDescriptor(
				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
				getResourceLocator(), getString("_UI_Edge_type_feature"), getString(
						"_UI_PropertyDescriptor_description", "_UI_Edge_type_feature",
						"_UI_Edge_type"), HenshinPackage.Literals.EDGE__TYPE));
	}
	
	/**
	 * This returns Edge.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public Object getImage(Object object) {
		Edge kernelEdge = getKernelEdge((Edge) object);		
		if(kernelEdge != null){
			return getImage(kernelEdge);
		}
		Edge edge = (Edge) object;
		
		if (edge.eContainer() == null) {
			// This is used to return the palette icon needed for the visual
			// editor.
			// Otherwise, since the visual editor will create an Edge without
			// source, target nor type,
			// a red border will be drawn around the Edge icon in the palette.
			return getResourceLocator().getImage("full/obj16/Edge");
		}
		
		Object edgeImage = null;
		
		boolean needsAttention = false;
		needsAttention |= (edge.getType() == null);
		needsAttention |= (edge.getSource() == null);
		needsAttention |= (edge.getTarget() == null);
		
		if (edge.getType() != null && edge.getType().isContainment()) {
			edgeImage = getResourceLocator().getImage("full/obj16/ContainmentEdge.png");
		} else if (edge.getType() != null && edge.getType().isMany()) {
			edgeImage = IconUtil.getCompositeImage(
					getResourceLocator().getImage("full/obj16/Edge"), getResourceLocator()
							.getImage("full/obj16/Edge"), 1, 2);
		} else {
			edgeImage = getResourceLocator().getImage("full/obj16/Edge");
		}// if
		
		if (HenshinRuleAnalysisUtil.isDeletionEdge(edge)) {
			Object deleteOverlay = getResourceLocator().getImage("full/ovr16/Del_ovr.png");
			edgeImage = IconUtil.getCompositeImage(edgeImage, deleteOverlay);
		} else if (HenshinRuleAnalysisUtil.isCreationEdge(edge)) {
			Object createOverlay = getResourceLocator().getImage("full/ovr16/Create_ovr.png");
			edgeImage = IconUtil.getCompositeImage(edgeImage, createOverlay);
		}// if
		
		if (needsAttention) {
			Object attentionOverlay = getResourceLocator().getImage("full/ovr16/Attn_ovr.png");
			edgeImage = IconUtil.getCompositeImage(edgeImage, attentionOverlay);
		}
		
		return edgeImage;
	}
	
	/**
	 * This returns the label text for the adapted class. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		Edge edge = (Edge) object;
		String result = getString("_UI_Edge_type");
		
		String srcName = (edge.getSource() != null) ? ((edge.getSource().getName() != null) ? edge
				.getSource().getName() : "_") : "?";
		String trgName = (edge.getTarget() != null) ? ((edge.getTarget().getName() != null) ? edge
				.getTarget().getName() : "_") : "?";
		String edgeType = ("(" + ((edge.getType() != null) ? edge.getType().getName() : "?") + ")");
		
		result = result + " " + edgeType + " " + srcName + " -> " + trgName;
		
		return result;
	}// getText
	
	/**
	 * This handles model notifications by calling {@link #updateChildren} to
	 * update any cached children and by creating a viewer notification, which
	 * it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public void notifyChanged(Notification notification) {
		System.out.println(notification);
		if (notification.getEventType() == Notification.SET) {
			
			Edge edge = (Edge) notification.getNotifier();
			Notification notif = new ViewerNotification(notification, edge, false, true);
			this.fireNotifyChanged(notif);
			
			Graph graph = edge.getGraph();
			if (graph != null) {
				GraphItemProvider gip = (GraphItemProvider) adapterFactory
						.adapt(graph, Graph.class);
				gip.notifyCorrespondingEdges(graph, notification);
			}
			
		}// if		
		
		updateChildren(notification);
		super.notifyChanged(notification);
	}// notifyChanged
	
	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s
	 * describing the children that can be created under this object. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);
	}
	
	/**
	 * Return the resource locator for this item provider's resources. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return HenshinEditPlugin.INSTANCE;
	}
	
	protected boolean isUserEditable(Object object) {
		return getKernelEdge((Edge)object) == null;
	}
	
	protected Edge getKernelEdge(Edge edge){
		if (edge.getGraph() != null && (edge.getGraph().isLhs() || edge.getGraph().isRhs())) {
			Rule rule = edge.getGraph().getContainerRule();
			return rule.getOriginInKernelRule(edge);
		}
		return null;
	} 
	
	@Override
	public Object getForeground(Object object) {
		return isUserEditable(object) ? super.getForeground(object) : URI
				.createURI("color://rgb/0/0/255");
	}
	
	// @Override
	// protected Command createSetCommand(EditingDomain domain, EObject owner,
	// EStructuralFeature feature, Object value, int index) {
	// Edge edge = (Edge) owner;
	// CompoundCommand cmpCmd = new
	// CompoundCommand(CompoundCommand.LAST_COMMAND_ALL);
	// for (Edge dependentEdge : HenshinMultiRuleUtil.getDependentEdges(edge)) {
	// cmpCmd.append(createSetCommand(domain, dependentEdge, feature, value,
	// index));
	// }
	// cmpCmd.append(super.createSetCommand(domain, owner, feature, value,
	// index));
	// return cmpCmd.unwrap();
	// }
	
}