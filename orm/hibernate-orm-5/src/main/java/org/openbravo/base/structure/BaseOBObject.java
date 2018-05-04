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

package org.openbravo.base.structure;

import java.util.HashMap;
import java.util.Map;

import org.openbravo.model.ad.system.Language;

/**
 * Base business object, the root of the inheritance tree for all business
 * objects. The class model here combines an inheritance structure with
 * interface definitions. The inheritance structure is used to enable some
 * re-use of code. The interfaces are used to tag a certain implementation with
 * the functionality it provides. The outside world should use the interfaces to
 * determine if an object supports specific functionality.
 * 
 * @author mtaal
 */

public abstract class BaseOBObject {
	public static final String ID = "id";

	private Map<String, Object> values = new HashMap<>();

	public Object getId() {
		return get(ID);
	}

	public void setId(Object id) {
		set(ID, id);
	}

	public void set(String key, Object value) {
		values.put(key, value);
	}

	public abstract String getEntityName();

	/**
	 * Returns the value of the {@link Property Property} identified by the
	 * propName. This method does security checking. If a security violation occurs
	 * then a OBSecurityException is thrown.
	 * 
	 * @see BaseOBObject#get(String, Language)
	 * 
	 * @param propName
	 *            the name of the {@link Property Property} for which the value is
	 *            requested
	 * @throws OBSecurityException
	 */
	public Object get(String propName) {
		return values.get(propName);
	}

	/**
	 * Returns the value of the {@link Property Property} identified by the propName
	 * translating it, if possible, to the language. This method does security
	 * checking. If a security violation occurs then a OBSecurityException is
	 * thrown.
	 * 
	 * @see BaseOBObject#get(String)
	 * 
	 * @param propName
	 *            the name of the {@link Property Property} for which the value is
	 *            requested
	 * @param language
	 *            language to translate to
	 * @return value of the property
	 * @throws OBSecurityException
	 *             in case property is not readable
	 */
	public Object get(String propName, Language language) {
		return values.get(propName);
	}

	/**
	 * Set a value for the {@link Property Property} identified by the propName.
	 * This method checks the correctness of the value and performs security checks.
	 * 
	 * @param propName
	 *            the name of the {@link Property Property} being set
	 * @param value
	 *            the value being set
	 * @throws OBSecurityException
	 *             , ValidationException
	 */

	/**
	 * Sets a value in the object without any security or validation checking.
	 * Should be used with care. Is used by the subclasses and system classes.
	 * 
	 * @param propName
	 *            the name of the {@link Property Property} being set
	 * @param value
	 */
	public void setValue(String propName, Object value) {
		set(propName, value);
	}

	/**
	 * Returns the value of {@link Property Property} identified by the propName.
	 * This method does not do security checking.
	 * 
	 * @param propName
	 *            the name of the property for which the value is requested.
	 * @return the value
	 */
	public Object getValue(String propName) {
		return get(propName);
	}

	protected void setDefaultValue(String k, Object v) {

	}

}
