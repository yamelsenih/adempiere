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

/** Generated Process for (ASP Generate Level)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.1
 */
public abstract class ASPGenerateLevelAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "ASP Generate Level";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "ASP Generate Level";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 53067;
	/**	Parameter Name for ASP Status	*/
	public static final String ASP_STATUS = "ASP_Status";
	/**	Parameter Name for Menu	*/
	public static final String AD_MENU_ID = "AD_Menu_ID";
	/**	Parameter Name for Generate Fields	*/
	public static final String ISGENERATEFIELDS = "IsGenerateFields";
	/**	Parameter Value for ASP Status	*/
	private String status;
	/**	Parameter Value for Menu	*/
	private int menuId;
	/**	Parameter Value for Generate Fields	*/
	private boolean isGenerateFields;

	@Override
	protected void prepare() {
		status = getParameterAsString(ASP_STATUS);
		menuId = getParameterAsInt(AD_MENU_ID);
		isGenerateFields = getParameterAsBoolean(ISGENERATEFIELDS);
	}

	/**	 Getter Parameter Value for ASP Status	*/
	protected String getStatus() {
		return status;
	}

	/**	 Setter Parameter Value for ASP Status	*/
	protected void setStatus(String status) {
		this.status = status;
	}

	/**	 Getter Parameter Value for Menu	*/
	protected int getMenuId() {
		return menuId;
	}

	/**	 Setter Parameter Value for Menu	*/
	protected void setMenuId(int menuId) {
		this.menuId = menuId;
	}

	/**	 Getter Parameter Value for Generate Fields	*/
	protected boolean isGenerateFields() {
		return isGenerateFields;
	}

	/**	 Setter Parameter Value for Generate Fields	*/
	protected void setIsGenerateFields(boolean isGenerateFields) {
		this.isGenerateFields = isGenerateFields;
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