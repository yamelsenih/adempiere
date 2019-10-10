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

package org.spin.report;

import java.sql.Timestamp;
import org.compiere.process.SvrProcess;

/** Generated Process for (Bank Balance)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public abstract class BankBalanceToDateAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "T_BankBalance Report";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Bank Balance";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 1000007;
	/**	Parameter Name for Bank	*/
	public static final String C_BANK_ID = "C_Bank_ID";
	/**	Parameter Name for Bank Account	*/
	public static final String C_BANKACCOUNT_ID = "C_BankAccount_ID";
	/**	Parameter Name for List Sources	*/
	public static final String LISTSOURCES = "ListSources";
	/**	Parameter Name for Transaction Date	*/
	public static final String DATETRX = "DateTrx";
	/**	Parameter Value for Bank	*/
	private int bankId;
	/**	Parameter Value for Bank Account	*/
	private int bankAccountId;
	/**	Parameter Value for List Sources	*/
	private boolean isListSources;
	/**	Parameter Value for Transaction Date	*/
	private Timestamp dateTrx;
	/**	Parameter Value for Transaction Date(To)	*/
	private Timestamp dateTrxTo;

	@Override
	protected void prepare() {
		bankId = getParameterAsInt(C_BANK_ID);
		bankAccountId = getParameterAsInt(C_BANKACCOUNT_ID);
		isListSources = getParameterAsBoolean(LISTSOURCES);
		dateTrx = getParameterAsTimestamp(DATETRX);
		dateTrxTo = getParameterToAsTimestamp(DATETRX);
	}

	/**	 Getter Parameter Value for Bank	*/
	protected int getBankId() {
		return bankId;
	}

	/**	 Setter Parameter Value for Bank	*/
	protected void setBankId(int bankId) {
		this.bankId = bankId;
	}

	/**	 Getter Parameter Value for Bank Account	*/
	protected int getBankAccountId() {
		return bankAccountId;
	}

	/**	 Setter Parameter Value for Bank Account	*/
	protected void setBankAccountId(int bankAccountId) {
		this.bankAccountId = bankAccountId;
	}

	/**	 Getter Parameter Value for List Sources	*/
	protected boolean isListSources() {
		return isListSources;
	}

	/**	 Setter Parameter Value for List Sources	*/
	protected void setListSources(boolean isListSources) {
		this.isListSources = isListSources;
	}

	/**	 Getter Parameter Value for Transaction Date	*/
	protected Timestamp getDateTrx() {
		return dateTrx;
	}

	/**	 Setter Parameter Value for Transaction Date	*/
	protected void setDateTrx(Timestamp dateTrx) {
		this.dateTrx = dateTrx;
	}

	/**	 Getter Parameter Value for Transaction Date(To)	*/
	protected Timestamp getDateTrxTo() {
		return dateTrxTo;
	}

	/**	 Setter Parameter Value for Transaction Date(To)	*/
	protected void setDateTrxTo(Timestamp dateTrxTo) {
		this.dateTrxTo = dateTrxTo;
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