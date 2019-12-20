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

/** Generated Process for (Generate GL Journal from Revenue Recognition)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public abstract class RevenueRecognitionGLJournalAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "Revenue_Recognition_GL_Journal";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Generate GL Journal from Revenue Recognition";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 2000051;
	/**	Parameter Name for Revenue Recognition	*/
	public static final String C_REVENUERECOGNITION_ID = "C_RevenueRecognition_ID";
	/**	Parameter Name for Account Date	*/
	public static final String DATEACCT = "DateAcct";
	/**	Parameter Name for Target Document Type	*/
	public static final String C_DOCTYPETARGET_ID = "C_DocTypeTarget_ID";
	/**	Parameter Value for Revenue Recognition	*/
	private int revenueRecognitionId;
	/**	Parameter Value for Account Date	*/
	private Timestamp dateAcct;
	/**	Parameter Value for Target Document Type	*/
	private int docTypeTargetId;

	@Override
	protected void prepare() {
		revenueRecognitionId = getParameterAsInt(C_REVENUERECOGNITION_ID);
		dateAcct = getParameterAsTimestamp(DATEACCT);
		docTypeTargetId = getParameterAsInt(C_DOCTYPETARGET_ID);
	}

	/**	 Getter Parameter Value for Revenue Recognition	*/
	protected int getRevenueRecognitionId() {
		return revenueRecognitionId;
	}

	/**	 Setter Parameter Value for Revenue Recognition	*/
	protected void setRevenueRecognitionId(int revenueRecognitionId) {
		this.revenueRecognitionId = revenueRecognitionId;
	}

	/**	 Getter Parameter Value for Account Date	*/
	protected Timestamp getDateAcct() {
		return dateAcct;
	}

	/**	 Setter Parameter Value for Account Date	*/
	protected void setDateAcct(Timestamp dateAcct) {
		this.dateAcct = dateAcct;
	}

	/**	 Getter Parameter Value for Target Document Type	*/
	protected int getDocTypeTargetId() {
		return docTypeTargetId;
	}

	/**	 Setter Parameter Value for Target Document Type	*/
	protected void setDocTypeTargetId(int docTypeTargetId) {
		this.docTypeTargetId = docTypeTargetId;
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