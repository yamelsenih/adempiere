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

package org.compiere.process;



/** Generated Process for (Generate RFQ)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public abstract class ProjectGenerateRFQAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "C_Project_GenerateRFQ";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Generate RFQ";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 54175;
	/**	Parameter Name for RfQ Topic	*/
	public static final String C_RFQ_TOPIC_ID = "C_RfQ_Topic_ID";
	/**	Parameter Name for RfQ Type	*/
	public static final String QUOTETYPE = "QuoteType";
	/**	Parameter Name for Quote All Quantities	*/
	public static final String ISQUOTEALLQTY = "IsQuoteAllQty";
	/**	Parameter Name for Invited Vendors Only	*/
	public static final String ISINVITEDVENDORSONLY = "IsInvitedVendorsOnly";
	/**	Parameter Name for Responses Accepted	*/
	public static final String ISRFQRESPONSEACCEPTED = "IsRfQResponseAccepted";
	/**	Parameter Name for Self-Service	*/
	public static final String ISSELFSERVICE = "IsSelfService";
	/**	Parameter Value for RfQ Topic	*/
	private int rfQTopicId;
	/**	Parameter Value for RfQ Type	*/
	private String quoteType;
	/**	Parameter Value for Quote All Quantities	*/
	private boolean isQuoteAllQty;
	/**	Parameter Value for Invited Vendors Only	*/
	private boolean isInvitedVendorsOnly;
	/**	Parameter Value for Responses Accepted	*/
	private boolean isRfQResponseAccepted;
	/**	Parameter Value for Self-Service	*/
	private boolean isSelfService;

	@Override
	protected void prepare() {
		rfQTopicId = getParameterAsInt(C_RFQ_TOPIC_ID);
		quoteType = getParameterAsString(QUOTETYPE);
		isQuoteAllQty = getParameterAsBoolean(ISQUOTEALLQTY);
		isInvitedVendorsOnly = getParameterAsBoolean(ISINVITEDVENDORSONLY);
		isRfQResponseAccepted = getParameterAsBoolean(ISRFQRESPONSEACCEPTED);
		isSelfService = getParameterAsBoolean(ISSELFSERVICE);
	}

	/**	 Getter Parameter Value for RfQ Topic	*/
	protected int getRfQTopicId() {
		return rfQTopicId;
	}

	/**	 Setter Parameter Value for RfQ Topic	*/
	protected void setRfQTopicId(int rfQTopicId) {
		this.rfQTopicId = rfQTopicId;
	}

	/**	 Getter Parameter Value for RfQ Type	*/
	protected String getQuoteType() {
		return quoteType;
	}

	/**	 Setter Parameter Value for RfQ Type	*/
	protected void setQuoteType(String quoteType) {
		this.quoteType = quoteType;
	}

	/**	 Getter Parameter Value for Quote All Quantities	*/
	protected boolean isQuoteAllQty() {
		return isQuoteAllQty;
	}

	/**	 Setter Parameter Value for Quote All Quantities	*/
	protected void setIsQuoteAllQty(boolean isQuoteAllQty) {
		this.isQuoteAllQty = isQuoteAllQty;
	}

	/**	 Getter Parameter Value for Invited Vendors Only	*/
	protected boolean isInvitedVendorsOnly() {
		return isInvitedVendorsOnly;
	}

	/**	 Setter Parameter Value for Invited Vendors Only	*/
	protected void setIsInvitedVendorsOnly(boolean isInvitedVendorsOnly) {
		this.isInvitedVendorsOnly = isInvitedVendorsOnly;
	}

	/**	 Getter Parameter Value for Responses Accepted	*/
	protected boolean isRfQResponseAccepted() {
		return isRfQResponseAccepted;
	}

	/**	 Setter Parameter Value for Responses Accepted	*/
	protected void setIsRfQResponseAccepted(boolean isRfQResponseAccepted) {
		this.isRfQResponseAccepted = isRfQResponseAccepted;
	}

	/**	 Getter Parameter Value for Self-Service	*/
	protected boolean isSelfService() {
		return isSelfService;
	}

	/**	 Setter Parameter Value for Self-Service	*/
	protected void setIsSelfService(boolean isSelfService) {
		this.isSelfService = isSelfService;
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