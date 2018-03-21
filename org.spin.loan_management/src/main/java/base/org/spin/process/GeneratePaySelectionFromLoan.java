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
import java.sql.Timestamp;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MBankAccount;
import org.compiere.model.MConversionRate;
import org.compiere.model.MConversionType;
import org.compiere.model.MCurrency;
import org.compiere.model.MPaySelection;
import org.compiere.model.MPaySelectionLine;
import org.compiere.model.MUser;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.spin.model.MFMAccount;
import org.spin.model.MFMAgreement;
import org.spin.model.MFMProduct;

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
			if(getRecord_ID() > 0) {
				paymentSelection = new MPaySelection(getCtx(), getRecord_ID(), get_TrxName());
				seqNo = paymentSelection.getLastLineNo();
			} else if(!isSplitPaySelection()) {	//	Split Payment Selection
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
			//	Get Bank Account
			MBankAccount bankAccount = MBankAccount.get(getCtx(), getBankAccountId());
			//	Loop for keys
			for(Integer key : getSelectionKeys()) {
				//	get values from result set
				int financialAccountId = key;
				MFMAccount account = new MFMAccount(getCtx(), financialAccountId, get_TrxName());
				BigDecimal capitalAmt = (BigDecimal) account.get_Value("CapitalAmt");
				seqNo += 10;
				MPaySelectionLine line = new MPaySelectionLine(paymentSelection, seqNo, getPaymentRule());
				MFMAgreement agreement = (MFMAgreement) account.getFM_Agreement();
				MFMProduct financialProduct = MFMProduct.getById(getCtx(), agreement.get_ValueAsInt("FM_Product_ID"));
				int chargeId = financialProduct.get_ValueAsInt("C_Charge_ID");
				//	Account
				line.setC_BPartner_ID(agreement.get_ValueAsInt("C_BPartner_ID"));
				if(chargeId != 0) {
					line.setC_Charge_ID(chargeId);
				}
				line.setIsSOTrx(false);
				line.setAmtSource(capitalAmt);
				int conversionRateId = getConversionRateId(account.getC_Currency_ID(), bankAccount.getC_Currency_ID(), 
						getPayDate(), 0, getAD_Client_ID(), paymentSelection.getAD_Org_ID());
				if(conversionRateId == -1) {
					throw new AdempiereException(MConversionRate.getErrorMessage(getCtx(), "NoCurrencyConversion", 
							account.getC_Currency_ID(), bankAccount.getC_Currency_ID(), 0, getPayDate(), get_TrxName()));
				}
				BigDecimal convertedAmt = convert(conversionRateId, capitalAmt);
				line.setOpenAmt(convertedAmt);
				line.setPayAmt (convertedAmt);
				line.setDiscountAmt(Env.ZERO);
				line.setDifferenceAmt(Env.ZERO);
				//	Set Conversion Rate
				if(conversionRateId > 0) {
					line.setC_Conversion_Rate_ID(conversionRateId);
				}
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
				//	Get Bank Account
				MBankAccount bankAccount = MBankAccount.get(getCtx(), getBankAccountId());
				//	get values from result set
				int financialAccountId = key;
				MFMAccount account = new MFMAccount(getCtx(), financialAccountId, get_TrxName());
				MFMAgreement agreement = (MFMAgreement) account.getFM_Agreement();
				MFMProduct financialProduct = MFMProduct.getById(getCtx(), agreement.get_ValueAsInt("FM_Product_ID"));
				int chargeId = financialProduct.get_ValueAsInt("C_Charge_ID");
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
				if(chargeId != 0) {
					line.setC_Charge_ID(chargeId);
				}
				line.setIsSOTrx(false);
				line.setAmtSource(capitalAmt);
				int conversionRateId = getConversionRateId(account.getC_Currency_ID(), bankAccount.getC_Currency_ID(), 
						getPayDate(), 0, getAD_Client_ID(), paymentSelection.getAD_Org_ID());
				if(conversionRateId == -1) {
					throw new AdempiereException(MConversionRate.getErrorMessage(getCtx(), "NoCurrencyConversion", 
							account.getC_Currency_ID(), bankAccount.getC_Currency_ID(), 0, getPayDate(), get_TrxName()));
				}
				BigDecimal convertedAmt = convert(conversionRateId, capitalAmt);
				line.setOpenAmt(convertedAmt);
				line.setPayAmt (convertedAmt);
				line.setDiscountAmt(Env.ZERO);
				line.setDifferenceAmt(Env.ZERO);
				//	Set Conversion Rate
				if(conversionRateId > 0) {
					line.setC_Conversion_Rate_ID(conversionRateId);
				}
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
	 * 
	 * @param currencyFromId
	 * @param CurencyToId
	 * @param conversionDate
	 * @param conversionTypeId
	 * @param clientId
	 * @param orgId
	 * @return
	 */
	private int getConversionRateId(int currencyFromId, int CurencyToId, Timestamp conversionDate, int conversionTypeId, int clientId, int orgId) {
		if (currencyFromId == CurencyToId) {
			return 0;
		}
		//	Conversion Type
		int internalConversionTypeId = conversionTypeId;
		if (internalConversionTypeId == 0) {
			internalConversionTypeId = MConversionType.getDefault(clientId);
		}
		//	Conversion Date
		if (conversionDate == null) {
			conversionDate = new Timestamp (System.currentTimeMillis());
		}
		//	Get Rate
		String sql = "SELECT C_Conversion_Rate_ID "
				+ "FROM C_Conversion_Rate "
				+ "WHERE C_Currency_ID=?"					//	#1
				+ " AND C_Currency_ID_To=?"					//	#2
				+ " AND	C_ConversionType_ID=?"				//	#3
				+ " AND	? BETWEEN ValidFrom AND ValidTo"	//	#4	TRUNC (?) ORA-00932: inconsistent datatypes: expected NUMBER got TIMESTAMP
				+ " AND AD_Client_ID IN (0,?)"				//	#5
				+ " AND AD_Org_ID IN (0,?) "				//	#6
				+ "ORDER BY AD_Client_ID DESC, AD_Org_ID DESC, ValidFrom DESC";
		//	Get
		int conversionRateId = DB.getSQLValue(get_TrxName(), sql, currencyFromId, CurencyToId, internalConversionTypeId, conversionDate, clientId, orgId);
		//	Show Log
		if (conversionRateId == -1) {
			log.info ("getRate - not found - CurFrom=" + currencyFromId 
						  + ", CurTo=" + CurencyToId
						  + ", " + conversionDate 
						  + ", Type=" + conversionTypeId + (conversionTypeId==internalConversionTypeId ? "" : "->" + internalConversionTypeId) 
						  + ", Client=" + clientId 
						  + ", Org=" + orgId);
		}
		//	Return
		return conversionRateId;
	}	//	getConversionRateId
	
	/**
	 * Convert from Conversion Rate
	 * @param conversionRateId
	 * @param sourceAmt
	 * @return
	 */
	private BigDecimal convert(int conversionRateId, BigDecimal sourceAmt) {
		if(conversionRateId == -1
				|| sourceAmt == null) {
			return null;
		}
		//	
		MConversionRate conversionRate = MConversionRate.get(getCtx(), conversionRateId);
		BigDecimal convertedAmt = sourceAmt.multiply(conversionRate.getMultiplyRate());
		int stdPrecision = MCurrency.getStdPrecision(getCtx(), conversionRate.getC_Currency_ID_To());
		if (convertedAmt.scale() > stdPrecision) {
			convertedAmt = convertedAmt.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
		}
		//	Default Return
		return convertedAmt;
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