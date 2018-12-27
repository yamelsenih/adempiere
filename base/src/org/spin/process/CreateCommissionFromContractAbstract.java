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

package org.spin.process;

import java.sql.Timestamp;
import org.compiere.process.SvrProcess;

/** Generated Process for (Create Commission from Contract)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.1
 */
public abstract class CreateCommissionFromContractAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "CreateCommissionFromContract";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Create Commission from Contract";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 2000042;
	/**	Parameter Name for Commission Type	*/
	public static final String C_COMMISSIONTYPE_ID = "C_CommissionType_ID";
	/**	Parameter Name for Document Type	*/
	public static final String C_DOCTYPE_ID = "C_DocType_ID";
	/**	Parameter Name for Document Date	*/
	public static final String DATEDOC = "DateDoc";
	/**	Parameter Value for Commission Type	*/
	private int commissionTypeId;
	/**	Parameter Value for Document Type	*/
	private int docTypeId;
	/**	Parameter Value for Document Date	*/
	private Timestamp dateDoc;

	@Override
	protected void prepare() {
		commissionTypeId = getParameterAsInt(C_COMMISSIONTYPE_ID);
		docTypeId = getParameterAsInt(C_DOCTYPE_ID);
		dateDoc = getParameterAsTimestamp(DATEDOC);
	}

	/**	 Getter Parameter Value for Commission Type	*/
	protected int getCommissionTypeId() {
		return commissionTypeId;
	}

	/**	 Setter Parameter Value for Commission Type	*/
	protected void setCommissionTypeId(int commissionTypeId) {
		this.commissionTypeId = commissionTypeId;
	}

	/**	 Getter Parameter Value for Document Type	*/
	protected int getDocTypeId() {
		return docTypeId;
	}

	/**	 Setter Parameter Value for Document Type	*/
	protected void setDocTypeId(int docTypeId) {
		this.docTypeId = docTypeId;
	}

	/**	 Getter Parameter Value for Document Date	*/
	protected Timestamp getDateDoc() {
		return dateDoc;
	}

	/**	 Setter Parameter Value for Document Date	*/
	protected void setDateDoc(Timestamp dateDoc) {
		this.dateDoc = dateDoc;
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