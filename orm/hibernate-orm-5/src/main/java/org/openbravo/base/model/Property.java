/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.model.ad.module.Module;

/**
 * Together with {@link Entity Entity}, the Property is the main part of the
 * in-memory model. A property can be a primitive type, a reference or a list
 * (one-to-many) property.
 * 
 * @author mtaal
 */
// TODO: consider subclasses for different types of properties
public class Property {
	private static final Logger log = Logger.getLogger(Property.class);

	private boolean oneToOne;
	private boolean oneToMany;
	private Entity entity;
	private Entity targetEntity;
	private boolean id;
	private boolean isInactive;

	private Property referencedProperty;
	// is this a property which is referenced by another property
	// if this === otherProperty.referencedProperty then this
	// member is true, these properties can be accessed also
	// in derived read mode
	private boolean isBeingReferenced;

	private String name;
	private String columnName;
	private String columnId;
	private boolean storedInSession = false;

	private boolean isActiveColumn = false;
	private String nameOfColumn; // AD_COLUMN.NAME
	// note defaultValue contains the value as it exists in the db, for booleans
	// this for example Y or N
	private String defaultValue;
	private String minValue;
	private int fieldLength;
	private String maxValue;
	private boolean mandatory;
	private boolean identifier;
	private boolean parent;
	private boolean child;
	private boolean encrypted;
	private boolean isUuid;
	private boolean isUpdatable;
	private Property idBasedOnProperty;
	private boolean isPartOfCompositeId;
	private boolean isOrderByProperty;
	private Set<String> allowedValues;
	private Boolean allowDerivedRead;
	private boolean isClientOrOrganization;
	private boolean translatable = false;
	private Property translationProperty;

	private String sqlLogic;

	private boolean isCompositeId;
	private List<Property> idParts = new ArrayList<Property>();

	private boolean isAuditInfo;
	private boolean isTransient;

	private String transientCondition;

	private Module module;

	// keeps track of the index of this property in the entity.getProperties()
	// gives a lot of performance/memory improvements when getting property values
	private int indexInEntity;

	private Boolean hasDisplayColumn;
	private Boolean isDisplayValue;
	private String displayProperty;

	private Property trlParentProperty;
	private Property trlOneToManyProperty;

	private Integer seqno;
	private boolean usedSequence;
	private boolean isProxy;
	private boolean allowedCrossOrgReference;

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public boolean isId() {
		return id;
	}

	public void setId(boolean id) {
		this.id = id;
	}

	/**
	 * In case of an association, returns the property in the associated
	 * {@link Entity Entity} to which this property refers. Returns null if there is
	 * no referenced property this occurs in case of a reference to the primary key
	 * of the referenced Entity.
	 * 
	 * @return the associated property on the other side of the association.
	 */
	public Property getReferencedProperty() {
		return referencedProperty;
	}

	/**
	 * Sets the referenced property and also the
	 * {@link Property#setTargetEntity(Entity) targetEntity} .
	 * 
	 * @param referencedProperty
	 *            the property referenced by this property
	 */
	public void setReferencedProperty(Property referencedProperty) {
		this.referencedProperty = referencedProperty;
		referencedProperty.setBeingReferenced(true);
		setTargetEntity(referencedProperty.getEntity());
	}

	public Entity getTargetEntity() {
		if (targetEntity == null && getReferencedProperty() != null) {
			targetEntity = getReferencedProperty().getEntity();
		}
		return targetEntity;
	}

	public void setTargetEntity(Entity targetEntity) {
		this.targetEntity = targetEntity;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Returns the Object value of the default, for example a Date property with
	 * default value of today will return a new Date() object.
	 * 
	 * @return the java object which can be used to initialize the java member
	 *         corresponding to this property.
	 */

	/**
	 * Identifier, Id, audit info, active, client/organization properties are
	 * derived readable, in addition properties which are referenced by other
	 * properties are derived readable, all other properties are not derived
	 * readable.
	 * 
	 * @return true if derived readable for the current user, false otherwise.
	 * @see Property#isActiveColumn()
	 * @see Property#isAuditInfo()
	 * @see Property#isBeingReferenced()
	 * @see Property#isClientOrOrganization()
	 * @see Property#isIdentifier()
	 * @see Property#isId()
	 */
	public boolean allowDerivedRead() {
		if (allowDerivedRead == null) {
			allowDerivedRead = isActiveColumn() || isAuditInfo() || isId() || isIdentifier() || isClientOrOrganization()
					|| isBeingReferenced();
		}
		return allowDerivedRead;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isIdentifier() {
		return identifier;
	}

	public void setIdentifier(boolean identifier) {
		this.identifier = identifier;
	}

	public boolean isParent() {
		return parent;
	}

	public void setParent(boolean parent) {
		this.parent = parent;
	}

	public boolean isChild() {
		return child;
	}

	public void setChild(boolean child) {
		this.child = child;
	}

	/**
	 * Used during generate.entities to generate short java type-names if a
	 * corresponding java import statement is generated for this type.
	 */
	public String getShorterNameTargetEntity() {
		List<String> imports = entity.getJavaImportsInternal();
		String typeName = targetEntity.getClassName();
		String simpleName = targetEntity.getSimpleClassName();
		if (typeName.equals(getEntity().getClassName())) {
			return simpleName;
		}
		if (imports.contains(typeName)) {
			return simpleName;
		}
		return typeName;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the name used for creating a getter/setter in generated code.
	 */
	public String getGetterSetterName() {

		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public String toString() {
		if (getName() == null) {
			return getEntity() + "." + getColumnName();
		}
		return getEntity() + "." + getName();
	}

	public Property getIdBasedOnProperty() {
		return idBasedOnProperty;
	}

	public void setIdBasedOnProperty(Property idBasedOnProperty) {
		this.idBasedOnProperty = idBasedOnProperty;
	}

	public boolean isOneToOne() {
		return oneToOne;
	}

	public void setOneToOne(boolean oneToOne) {
		this.oneToOne = oneToOne;
	}

	public boolean isOneToMany() {
		return oneToMany;
	}

	public void setOneToMany(boolean oneToMany) {
		this.oneToMany = oneToMany;
	}

	public boolean isUuid() {
		return isUuid;
	}

	public void setUuid(boolean isUuid) {
		this.isUuid = isUuid;
	}

	public boolean isUpdatable() {
		return isUpdatable;
	}

	public void setUpdatable(boolean isUpdatable) {
		this.isUpdatable = isUpdatable;
	}

	public boolean isCompositeId() {
		return isCompositeId;
	}

	public void setCompositeId(boolean isCompositeId) {
		this.isCompositeId = isCompositeId;
	}

	/**
	 * A property is a computed column when it has sql logic, in this case it is
	 * calculated based on a sql formula and is accessed through a proxy.
	 */
	public boolean isComputedColumn() {
		return getSqlLogic() != null;
	}

	/**
	 * Proxy properties are used to access to computed columns. Computed columns are
	 * not directly within the entity they are defined in, but in a extra entity
	 * that is accessed through a proxy, in this way computed columns are lazily
	 * calculated.
	 */
	public boolean isProxy() {
		return isProxy;
	}

	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

	public List<Property> getIdParts() {
		return idParts;
	}

	public boolean isPartOfCompositeId() {
		return isPartOfCompositeId;
	}

	public void setPartOfCompositeId(boolean isPartOfCompositeId) {
		this.isPartOfCompositeId = isPartOfCompositeId;
	}

	public int getFieldLength() {
		return fieldLength;
	}

	public void setFieldLength(int fieldLength) {
		this.fieldLength = fieldLength;
	}

	public boolean doCheckAllowedValue() {
		return allowedValues != null && allowedValues.size() > 0;
	}

	public boolean isAllowedValue(String value) {
		return allowedValues.contains(value);
	}

	/**
	 * @return a comma delimited list of allowed values, is used for enums.
	 */
	public String concatenatedAllowedValues() {
		final StringBuffer sb = new StringBuffer();
		for (final String s : allowedValues) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Checks if the property is transient. It uses the business object and the
	 * {@link #getTransientCondition() transientCondition} to compute if the
	 * property is transient (won't be exported to xml).
	 * 
	 * @param bob
	 *            the business object used to compute if the property is transient
	 * @return true if the property is transient and does not need to be exported
	 */

	public Set<String> getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(Set<String> allowedValues) {
		this.allowedValues = allowedValues;
	}

	public String getJavaName() {
		return NamingUtil.getSafeJavaName(getName());
	}

	public void setOrderByProperty(boolean isOrderByProperty) {
		this.isOrderByProperty = isOrderByProperty;
	}

	public boolean isOrderByProperty() {
		return isOrderByProperty;
	}

	public boolean isTransient() {
		return isTransient;
	}

	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	public boolean isAuditInfo() {
		return isAuditInfo;
	}

	public void setAuditInfo(boolean isAuditInfo) {
		this.isAuditInfo = isAuditInfo;
	}

	public String getTransientCondition() {
		return transientCondition;
	}

	public void setTransientCondition(String transientCondition) {
		this.transientCondition = transientCondition;
	}

	public boolean isClientOrOrganization() {
		return isClientOrOrganization;
	}

	public void setClientOrOrganization(boolean isClientOrOrganization) {
		this.isClientOrOrganization = isClientOrOrganization;
	}

	public boolean isInactive() {
		return isInactive;
	}

	public void setInactive(boolean isInactive) {
		this.isInactive = isInactive;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public String getNameOfColumn() {
		return nameOfColumn;
	}

	public void setNameOfColumn(String nameOfColumn) {
		this.nameOfColumn = nameOfColumn;
	}

	public int getIndexInEntity() {
		return indexInEntity;
	}

	public void setIndexInEntity(int indexInEntity) {
		this.indexInEntity = indexInEntity;
	}

	public boolean isActiveColumn() {
		return isActiveColumn;
	}

	public void setActiveColumn(boolean isActiveColumn) {
		this.isActiveColumn = isActiveColumn;
	}

	/**
	 * Deprecated not used anymore, is computed on the basis of the
	 * {@link #getDomainType()}.
	 */
	public void setDate(boolean isDate) {
	}

	/**
	 * Deprecated not used anymore, is computed on the basis of the
	 * {@link #getDomainType()}.
	 */
	public void setDatetime(boolean isDatetime) {
	}

	/**
	 * Deprecated not used anymore, is computed on the basis of the
	 * {@link #getDomainType()}.
	 */
	public void setPrimitive(boolean primitive) {
	}

	/**
	 * Deprecated not used anymore, is computed on the basis of the
	 * {@link #getDomainType()}.
	 */
	public void setPrimitiveType(Class<?> primitiveType) {
	}

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	/**
	 * Is this a property which is referenced by another property if this ===
	 * otherProperty.referencedProperty then this method returns true. Referenced
	 * properties are also accessible in derived read mode.
	 * 
	 * @return true if this property is being referenced by another property in the
	 *         model.
	 * @see Property#getReferencedProperty()
	 */
	public boolean isBeingReferenced() {
		return isBeingReferenced;
	}

	public void setBeingReferenced(boolean isBeingReferenced) {
		this.isBeingReferenced = isBeingReferenced;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	/**
	 * @return true if the property is a table reference which defines an explicit
	 *         display column. This display column is then used as the identifier of
	 *         objects referenced through this property.
	 */
	public boolean hasDisplayColumn() {

		return hasDisplayColumn;
	}

	/**
	 * @return true if the property is a table reference which defines an explicit
	 *         display column. This display column is then used as the identifier of
	 *         objects referenced through this property.
	 */
	public boolean isDisplayValue() {
		return isDisplayValue;
	}

	public String getDisplayPropertyName() {

		return displayProperty;
	}

	/**
	 * Returns whether a property is translatable to other languages. A property can
	 * be translated in case it has been marked in AD and there are translations
	 * installed in the system.
	 * 
	 */
	public boolean isTranslatable() {
		return translatable;
	}

	/**
	 * This property is candidate to be translatable (marked in DB as isTranlated).
	 * It checks it is actually translatable and sets the property as translatable
	 * or not regarding this.
	 * 
	 * @param translationProperty
	 *            it is the property in the trl table that holds the translation for
	 *            this property
	 */
	void setTranslatable(Property translationProperty) {
		log.debug("Setting translatable for " + this.getEntity().getTableName() + "." + this.getColumnName());

		if (translationProperty == null) {
			log.warn(this.getEntity().getTableName() + "." + this.getColumnName()
					+ " is not translatable: null translationProperty");
			translatable = false;
			return;
		}

		Property pk = entity.getIdProperties().get(0); // Assuming a single property as PK

		try {
			translationProperty.getEntity().getPropertyByColumnName("ad_language");
		} catch (Exception e) {
			// This exception is raised when the property is not found
			translatable = false;
			log.warn(this.getEntity().getTableName() + "." + this.getColumnName()
					+ " is not translatable: ad_language column not found in its trl table");
			return;
		}

		Property trlPropertyListInBase = null;
		for (Property p : this.getEntity().getProperties()) {
			if (p.isOneToMany() && translationProperty.getEntity().equals(p.getTargetEntity())) {
				trlPropertyListInBase = p;
				break;
			}
		}

		if (trlPropertyListInBase == null) {
			translatable = false;
			log.warn(this.getEntity().getTableName() + "." + this.getColumnName()
					+ " is not translatable: not found one to many property to trl table");
			return;
		}

		for (Property trlParent : translationProperty.getEntity().getParentProperties()) {
			if (pk.equals(trlParent.getReferencedProperty())) {
				this.trlParentProperty = trlParent;
				this.translationProperty = translationProperty;
				this.trlOneToManyProperty = trlPropertyListInBase;
				translatable = true;
				return;
			}
		}
		log.warn(this.getEntity().getTableName() + "." + this.getColumnName()
				+ " is not translatable: not found correspoding property in its trl table");
	}

	public Property getTranslationProperty() {
		return translationProperty;
	}

	public Property getTrlParentProperty() {
		return trlParentProperty;
	}

	public Property getTrlOneToManyProperty() {
		return trlOneToManyProperty;
	}

	public boolean isStoredInSession() {
		return storedInSession;
	}

	public void setStoredInSession(boolean storedInSession) {
		this.storedInSession = storedInSession;
	}

	public Integer getSeqno() {
		return seqno;
	}

	public void setSeqno(Integer seqno) {
		this.seqno = seqno;
	}

	public boolean isUsedSequence() {
		return usedSequence;
	}

	public void setUsedSequence(boolean usedSequence) {
		this.usedSequence = "documentno".equalsIgnoreCase(columnName) || (usedSequence && "Value".equals(columnName));
		;
	}

	public String getSqlLogic() {
		return sqlLogic;
	}

	public void setSqlLogic(String sqlLogic) {
		this.sqlLogic = sqlLogic;
	}

	/**
	 * Foreign key properties allowing cross organization references can define a
	 * link to an object which organization is not in the same organization's
	 * natural tree when context is in {@code  setCrossOrgReferenceAdminMode}.
	 */
	public boolean isAllowedCrossOrgReference() {
		return allowedCrossOrgReference;
	}

	/** @see Property#isAllowedCrossOrgReference() */
	public void setAllowedCrossOrgReference(boolean allowedCrossOrgReference) {
		this.allowedCrossOrgReference = allowedCrossOrgReference;
	}
}
