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

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MPaySelection;
import org.compiere.model.MPaySelectionLine;
import org.compiere.model.MUser;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.spin.model.MFMAccount;
import org.spin.model.MFMAgreement;

/** Generated Process for (Generate Payment Selection from Loan)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class GeneratePaySelectionFromLoan extends GeneratePaySelectionFromLoanAbstract {
	
	/**	Sequence			*/
	private int				seqNo = 10;
	/**	Payment Selection	*/
	private MPaySelection	paymentSelection = null;
	
	@Override
	protected void prepare() {
		super.prepare();
		//	Valid Record Identifier
		if(getRecord_ID() <= 0
				&& getBankAccountId() == 0
				&& getPayDate() == null)
			throw new AdempiereException("@C_PaySelection_ID@ @NotFound@");
	}

	@Override
	protected String doIt() throws Exception {
		//	Instance current Payment Selection
		StringBuffer msg = new StringBuffer();
		if(getRecord_ID() > 0
				|| !isSplitPaySelection()) {	//	Already exists
			paymentSelection = new MPaySelection(getCtx(), getRecord_ID(), get_TrxName());
			seqNo = paymentSelection.getLastLineNo();
			//	Split Payment Selection
			if(!isSplitPaySelection()) {
				paymentSelection = new MPaySelection(getCtx(), 0, get_TrxName());
				paymentSelection.setC_BankAccount_ID(getBankAccountId());
				paymentSelection.setDateDoc(getPayDate());
				paymentSelection.setPayDate(getPayDate());
				if(getDocTypeTargetId() > 0)
					paymentSelection.setC_DocType_ID(getDocTypeTargetId());
				MUser user = MUser.get(getCtx(), getAD_User_ID());
				String userName = "";
				if(user != null)
					userName = user.getName();
				//	Set description
				paymentSelection.setDescription(Msg.getMsg(Env.getCtx(), "VPaySelect")
						+ " - " + userName
						+ " - " + DisplayType.getDateFormat(DisplayType.Date).format(getPayDate()));
				//	Save
				paymentSelection.saveEx();
			}
			//	Loop for keys
			for(Integer key : getSelectionKeys()) {
				//	get values from result set
				int financialAccountId = key;
				MFMAccount account = new MFMAccount(getCtx(), financialAccountId, get_TrxName());
				BigDecimal capitalAmt = (BigDecimal) account.get_Value("CapitalAmt");
				seqNo += 10;
				MPaySelectionLine line = new MPaySelectionLine(paymentSelection, seqNo, getPaymentRule());
				MFMAgreement agreement = (MFMAgreement) account.getFM_Agreement();
				//	Account
				line.setC_BPartner_ID(agreement.get_ValueAsInt("C_BPartner_ID"));
				line.setIsSOTrx(false);
				line.setAmtSource(capitalAmt);
				line.setOpenAmt(capitalAmt);
				line.setPayAmt (capitalAmt);
				line.setDiscountAmt(Env.ZERO);
				line.setDifferenceAmt(Env.ZERO);
				//	Reference
				line.set_ValueOfColumn("FM_Account_ID", account.getFM_Account_ID());
				//	Save
				line.saveEx();
			}
			//	Complete Document
			completeDocument();
			//	Notify
			msg.append(paymentSelection.getDescription());
		} else {	//	Is a new Payment Selection
			//	Loop for keys
			for(Integer key : getSelectionKeys()) {
				paymentSelection = new MPaySelection(getCtx(), 0, get_TrxName());
				paymentSelection.setC_BankAccount_ID(getBankAccountId());
				paymentSelection.setDateDoc(getPayDate());
				paymentSelection.setPayDate(getPayDate());
				if(getDocTypeTargetId() > 0)
					paymentSelection.setC_DocType_ID(getDocTypeTargetId());

				//	get values from result set
				int financialAccountId = key;
				MFMAccount account = new MFMAccount(getCtx(), financialAccountId, get_TrxName());
				MFMAgreement agreement = (MFMAgreement) account.getFM_Agreement();
				MBPartner partner = MBPartner.get(getCtx(), agreement.get_ValueAsInt("C_BPartner_ID"));
				if(partner == null)
					continue;
				//	Set description
				paymentSelection.setDescription(Msg.parseTranslation(getCtx(), 
						"@GeneratedFromLoan@ - " 
						+ partner.getValue() + " - " + partner.getName() 
						+ DisplayType.getDateFormat(DisplayType.Date).format(getPayDate())));
				//	Save
				paymentSelection.saveEx();
				//	
				MPaySelectionLine line = new MPaySelectionLine(paymentSelection, seqNo, getPaymentRule());
				BigDecimal capitalAmt = (BigDecimal) account.get_Value("CapitalAmt");
				seqNo += 10;
				//	Account
				line.setC_BPartner_ID(partner.getC_BPartner_ID());
				line.setIsSOTrx(false);
				line.setAmtSource(capitalAmt);
				line.setOpenAmt(capitalAmt);
				line.setPayAmt (capitalAmt);
				line.setDiscountAmt(Env.ZERO);
				line.setDifferenceAmt(Env.ZERO);
				//	Reference
				line.set_ValueOfColumn("FM_Account_ID", account.getFM_Account_ID());
				//	Save
				line.saveEx();
				//	Complete
				completeDocument();
				//	Add message
				if(msg.length() > 0) {
					msg.append(", ");
				}
				msg.append("@C_PaySelection_ID@: [" + paymentSelection.getDocumentNo() + "]");
			}
		}
		//	Return
		return msg.toString();
	}
	
	/**
	 * Complete Document
	 */
	private void completeDocument() {
		//	For new
		paymentSelection.load(get_TrxName());
		//	Process Selection
		if(!paymentSelection.processIt(MPaySelection.DOCACTION_Complete)) {
			throw new AdempiereException("@Error@ " + paymentSelection.getProcessMsg());
		}
		//	
		paymentSelection.saveEx();
	}
}