/**
 * generated by Xtext 2.10.0
 */
package org.eclipse.emf.henshin.text.henshin_text.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.henshin.text.henshin_text.ConditionGraph;
import org.eclipse.emf.henshin.text.henshin_text.ConditionGraphRef;
import org.eclipse.emf.henshin.text.henshin_text.Henshin_textPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Condition Graph Ref</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.henshin.text.henshin_text.impl.ConditionGraphRefImpl#getConditionGraphRef <em>Condition Graph Ref</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ConditionGraphRefImpl extends LogicImpl implements ConditionGraphRef
{
  /**
   * The cached value of the '{@link #getConditionGraphRef() <em>Condition Graph Ref</em>}' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getConditionGraphRef()
   * @generated
   * @ordered
   */
  protected ConditionGraph conditionGraphRef;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ConditionGraphRefImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return Henshin_textPackage.Literals.CONDITION_GRAPH_REF;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ConditionGraph getConditionGraphRef()
  {
    if (conditionGraphRef != null && conditionGraphRef.eIsProxy())
    {
      InternalEObject oldConditionGraphRef = (InternalEObject)conditionGraphRef;
      conditionGraphRef = (ConditionGraph)eResolveProxy(oldConditionGraphRef);
      if (conditionGraphRef != oldConditionGraphRef)
      {
        if (eNotificationRequired())
          eNotify(new ENotificationImpl(this, Notification.RESOLVE, Henshin_textPackage.CONDITION_GRAPH_REF__CONDITION_GRAPH_REF, oldConditionGraphRef, conditionGraphRef));
      }
    }
    return conditionGraphRef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ConditionGraph basicGetConditionGraphRef()
  {
    return conditionGraphRef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setConditionGraphRef(ConditionGraph newConditionGraphRef)
  {
    ConditionGraph oldConditionGraphRef = conditionGraphRef;
    conditionGraphRef = newConditionGraphRef;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, Henshin_textPackage.CONDITION_GRAPH_REF__CONDITION_GRAPH_REF, oldConditionGraphRef, conditionGraphRef));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case Henshin_textPackage.CONDITION_GRAPH_REF__CONDITION_GRAPH_REF:
        if (resolve) return getConditionGraphRef();
        return basicGetConditionGraphRef();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case Henshin_textPackage.CONDITION_GRAPH_REF__CONDITION_GRAPH_REF:
        setConditionGraphRef((ConditionGraph)newValue);
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
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case Henshin_textPackage.CONDITION_GRAPH_REF__CONDITION_GRAPH_REF:
        setConditionGraphRef((ConditionGraph)null);
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
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case Henshin_textPackage.CONDITION_GRAPH_REF__CONDITION_GRAPH_REF:
        return conditionGraphRef != null;
    }
    return super.eIsSet(featureID);
  }

} //ConditionGraphRefImpl
