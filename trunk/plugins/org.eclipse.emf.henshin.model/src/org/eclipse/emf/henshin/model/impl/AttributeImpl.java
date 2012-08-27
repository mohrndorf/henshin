/**
 * <copyright>
 * Copyright (c) 2010-2012 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.model.impl;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Attribute;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinPackage;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.actions.impl.AttributeActionHelper;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Attribute</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.henshin.model.impl.AttributeImpl#getAction <em>Action</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.model.impl.AttributeImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.model.impl.AttributeImpl#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.model.impl.AttributeImpl#getNode <em>Node</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.model.impl.AttributeImpl#getConstant <em>Constant</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.model.impl.AttributeImpl#isNull <em>Null</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AttributeImpl extends EObjectImpl implements Attribute {

	/**
	 * The default value of the '{@link #getAction() <em>Action</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAction()
	 * @generated
	 * @ordered
	 */
	protected static final Action ACTION_EDEFAULT = null;

	/**
	 * An adapter that automatically updates the derived fields {@link #constant} and {@link #null_}.
	 * @generated NOT
	 */
	private Adapter derivedFieldsUpdater = new AdapterImpl() {
		@Override
		public void notifyChanged(Notification msg) {
			int featureID = msg.getFeatureID(Attribute.class);
			if (featureID==HenshinPackage.ATTRIBUTE__TYPE || featureID==HenshinPackage.ATTRIBUTE__VALUE) {
				updateDerivedFields();
			}
		}
	};
	
	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected EAttribute type;

	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected String value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getConstant() <em>Constant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConstant()
	 * @generated NOT
	 * @ordered
	 */
	protected static final Object CONSTANT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getConstant() <em>Constant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConstant()
	 * @generated
	 * @ordered
	 */
	protected Object constant = CONSTANT_EDEFAULT;

	/**
	 * The default value of the '{@link #isNull() <em>Null</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNull()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NULL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNull() <em>Null</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNull()
	 * @generated
	 * @ordered
	 */
	protected boolean null_ = NULL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * Default and only constructor.
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected AttributeImpl() {
		// Add the constant updater to the adapters list:
		eAdapters().add(derivedFieldsUpdater);
	}

	/**
	 * Update the derived {@link #constant} field of this attribute.
	 * @generated NOT
	 */
	protected void updateDerivedFields() {
		// null?
		if (value!=null && value.trim().equalsIgnoreCase("null")) {
			constant = null;
			null_ = true;
			return;
		}
		null_ = false; // not null
		if (value==null || type==null) {
			constant = null;
			return;
		}
			
		if (type.getEType()==null) {
			EcoreUtil.resolveAll(this);
		}
		if (type.getEType() instanceof EEnum) {
			constant = ((EEnum) type.getEType()).getEEnumLiteral(value);
			return;
		}
		if (type.getEType()==null || 
			type.getEType().getEPackage()==null || 
			type.getEType().getEPackage().getEFactoryInstance()==null) {
			constant = null;
			return;
		}
		if (type.getEType()==EcorePackage.eINSTANCE.getEString()) {
			// For strings we expect surrounding quotes:
			String v = value.trim();
			if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
				constant = v.substring(1, v.length()-1); // TODO: take care of cases like "x" + "y" or "say \"hello\""
			} else {
				constant = null;
			}
			return;
		}
		// Try to load it using the factory:
		try {
			EFactory factory = type.getEType().getEPackage().getEFactoryInstance();
			constant = factory.createFromString(type.getEAttributeType(), value);
		} catch (Throwable t) {
			constant = null; // this can happen if it is not a constant
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return HenshinPackage.Literals.ATTRIBUTE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Action getAction() {
		return AttributeActionHelper.INSTANCE.getAction(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public void setAction(Action action) {
		AttributeActionHelper.INSTANCE.setAction(this, action);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getType() {
		if (type != null && type.eIsProxy()) {
			InternalEObject oldType = (InternalEObject)type;
			type = (EAttribute)eResolveProxy(oldType);
			if (type != oldType) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, HenshinPackage.ATTRIBUTE__TYPE, oldType, type));
			}
		}
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute basicGetType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(EAttribute newType) {
		EAttribute oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HenshinPackage.ATTRIBUTE__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HenshinPackage.ATTRIBUTE__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Node getNode() {
		if (eContainerFeatureID() != HenshinPackage.ATTRIBUTE__NODE) return null;
		return (Node)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNode(Node newNode, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newNode, HenshinPackage.ATTRIBUTE__NODE, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNode(Node newNode) {
		if (newNode != eInternalContainer() || (eContainerFeatureID() != HenshinPackage.ATTRIBUTE__NODE && newNode != null)) {
			if (EcoreUtil.isAncestor(this, newNode))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newNode != null)
				msgs = ((InternalEObject)newNode).eInverseAdd(this, HenshinPackage.NODE__ATTRIBUTES, Node.class, msgs);
			msgs = basicSetNode(newNode, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HenshinPackage.ATTRIBUTE__NODE, newNode, newNode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getConstant() {
		return constant;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isNull() {
		return null_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Graph getGraph() {
		Node node =  getNode();
		return (node!=null) ? node.getGraph() : null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HenshinPackage.ATTRIBUTE__NODE:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetNode((Node)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HenshinPackage.ATTRIBUTE__NODE:
				return basicSetNode(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case HenshinPackage.ATTRIBUTE__NODE:
				return eInternalContainer().eInverseRemove(this, HenshinPackage.NODE__ATTRIBUTES, Node.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HenshinPackage.ATTRIBUTE__ACTION:
				return getAction();
			case HenshinPackage.ATTRIBUTE__TYPE:
				if (resolve) return getType();
				return basicGetType();
			case HenshinPackage.ATTRIBUTE__VALUE:
				return getValue();
			case HenshinPackage.ATTRIBUTE__NODE:
				return getNode();
			case HenshinPackage.ATTRIBUTE__CONSTANT:
				return getConstant();
			case HenshinPackage.ATTRIBUTE__NULL:
				return isNull();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case HenshinPackage.ATTRIBUTE__ACTION:
				setAction((Action)newValue);
				return;
			case HenshinPackage.ATTRIBUTE__TYPE:
				setType((EAttribute)newValue);
				return;
			case HenshinPackage.ATTRIBUTE__VALUE:
				setValue((String)newValue);
				return;
			case HenshinPackage.ATTRIBUTE__NODE:
				setNode((Node)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case HenshinPackage.ATTRIBUTE__ACTION:
				setAction(ACTION_EDEFAULT);
				return;
			case HenshinPackage.ATTRIBUTE__TYPE:
				setType((EAttribute)null);
				return;
			case HenshinPackage.ATTRIBUTE__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case HenshinPackage.ATTRIBUTE__NODE:
				setNode((Node)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case HenshinPackage.ATTRIBUTE__ACTION:
				return ACTION_EDEFAULT == null ? getAction() != null : !ACTION_EDEFAULT.equals(getAction());
			case HenshinPackage.ATTRIBUTE__TYPE:
				return type != null;
			case HenshinPackage.ATTRIBUTE__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
			case HenshinPackage.ATTRIBUTE__NODE:
				return getNode() != null;
			case HenshinPackage.ATTRIBUTE__CONSTANT:
				return CONSTANT_EDEFAULT == null ? constant != null : !CONSTANT_EDEFAULT.equals(constant);
			case HenshinPackage.ATTRIBUTE__NULL:
				return null_ != NULL_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (value: ");
		result.append(value);
		result.append(", constant: ");
		result.append(constant);
		result.append(", null: ");
		result.append(null_);
		result.append(')');
		return result.toString();
	}

} //AttributeImpl
