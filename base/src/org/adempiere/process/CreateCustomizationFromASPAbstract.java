/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/

package org.adempiere.process;

import org.compiere.process.SvrProcess;

/** Generated Process for (Create Customization)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.1
 */
public abstract class CreateCustomizationFromASPAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "ASPCreateCustomization";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Create Customization";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 1000000;
	/**	Parameter Name for Menu	*/
	public static final String AD_MENU_ID = "AD_Menu_ID";
	/**	Parameter Name for AllFields	*/
	public static final String ALLFIELDS = "AllFields";
	/**	Parameter Value for Menu	*/
	private int menuId;
	/**	Parameter Value for AllFields	*/
	private boolean isAllFields;

	@Override
	protected void prepare() {
		menuId = getParameterAsInt(AD_MENU_ID);
		isAllFields = getParameterAsBoolean(ALLFIELDS);
	}

	/**	 Getter Parameter Value for Menu	*/
	protected int getMenuId() {
		return menuId;
	}

	/**	 Setter Parameter Value for Menu	*/
	protected void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	/**	 Getter Parameter Value for AllFields	*/
	protected boolean isAllFields() {
		return isAllFields;
	}

	/**	 Setter Parameter Value for AllFields	*/
	protected void setAllFields(boolean isAllFields) {
		this.isAllFields = isAllFields;
	}

	/**	 Getter Parameter Value for Process ID	*/
	public static final int getProcessId() {
		return ID_FOR_PROCESS;
	}

	/**	 Getter Parameter Value for Process Value	*/
	public static final String getProcessValue() {
		return VALUE_FOR_PROCESS;
	}

	/**	 Getter Parameter Value for Process Name	*/
	public static final String getProcessName() {
		return NAME_FOR_PROCESS;
	}
}