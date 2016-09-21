/**
 * generated by Xtext 2.10.0
 */
package org.eclipse.emf.henshin.text.henshin_text.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.emf.henshin.text.henshin_text.ConditionalUnit;
import org.eclipse.emf.henshin.text.henshin_text.Henshin_textPackage;
import org.eclipse.emf.henshin.text.henshin_text.UnitElement;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Conditional Unit</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.henshin.text.henshin_text.impl.ConditionalUnitImpl#getIf <em>If</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.text.henshin_text.impl.ConditionalUnitImpl#getThen <em>Then</em>}</li>
 *   <li>{@link org.eclipse.emf.henshin.text.henshin_text.impl.ConditionalUnitImpl#getElse <em>Else</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ConditionalUnitImpl extends UnitElementImpl implements ConditionalUnit
{
  /**
   * The cached value of the '{@link #getIf() <em>If</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getIf()
   * @generated
   * @ordered
   */
  protected EList<UnitElement> if_;

  /**
   * The cached value of the '{@link #getThen() <em>Then</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getThen()
   * @generated
   * @ordered
   */
  protected EList<UnitElement> then;

  /**
   * The cached value of the '{@link #getElse() <em>Else</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getElse()
   * @generated
   * @ordered
   */
  protected EList<UnitElement> else_;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ConditionalUnitImpl()
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
    return Henshin_textPackage.Literals.CONDITIONAL_UNIT;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<UnitElement> getIf()
  {
    if (if_ == null)
    {
      if_ = new EObjectContainmentEList<UnitElement>(UnitElement.class, this, Henshin_textPackage.CONDITIONAL_UNIT__IF);
    }
    return if_;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<UnitElement> getThen()
  {
    if (then == null)
    {
      then = new EObjectContainmentEList<UnitElement>(UnitElement.class, this, Henshin_textPackage.CONDITIONAL_UNIT__THEN);
    }
    return then;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<UnitElement> getElse()
  {
    if (else_ == null)
    {
      else_ = new EObjectContainmentEList<UnitElement>(UnitElement.class, this, Henshin_textPackage.CONDITIONAL_UNIT__ELSE);
    }
    return else_;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case Henshin_textPackage.CONDITIONAL_UNIT__IF:
        return ((InternalEList<?>)getIf()).basicRemove(otherEnd, msgs);
      case Henshin_textPackage.CONDITIONAL_UNIT__THEN:
        return ((InternalEList<?>)getThen()).basicRemove(otherEnd, msgs);
      case Henshin_textPackage.CONDITIONAL_UNIT__ELSE:
        return ((InternalEList<?>)getElse()).basicRemove(otherEnd, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
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
      case Henshin_textPackage.CONDITIONAL_UNIT__IF:
        return getIf();
      case Henshin_textPackage.CONDITIONAL_UNIT__THEN:
        return getThen();
      case Henshin_textPackage.CONDITIONAL_UNIT__ELSE:
        return getElse();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case Henshin_textPackage.CONDITIONAL_UNIT__IF:
        getIf().clear();
        getIf().addAll((Collection<? extends UnitElement>)newValue);
        return;
      case Henshin_textPackage.CONDITIONAL_UNIT__THEN:
        getThen().clear();
        getThen().addAll((Collection<? extends UnitElement>)newValue);
        return;
      case Henshin_textPackage.CONDITIONAL_UNIT__ELSE:
        getElse().clear();
        getElse().addAll((Collection<? extends UnitElement>)newValue);
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
      case Henshin_textPackage.CONDITIONAL_UNIT__IF:
        getIf().clear();
        return;
      case Henshin_textPackage.CONDITIONAL_UNIT__THEN:
        getThen().clear();
        return;
      case Henshin_textPackage.CONDITIONAL_UNIT__ELSE:
        getElse().clear();
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
      case Henshin_textPackage.CONDITIONAL_UNIT__IF:
        return if_ != null && !if_.isEmpty();
      case Henshin_textPackage.CONDITIONAL_UNIT__THEN:
        return then != null && !then.isEmpty();
      case Henshin_textPackage.CONDITIONAL_UNIT__ELSE:
        return else_ != null && !else_.isEmpty();
    }
    return super.eIsSet(featureID);
  }

} //ConditionalUnitImpl
