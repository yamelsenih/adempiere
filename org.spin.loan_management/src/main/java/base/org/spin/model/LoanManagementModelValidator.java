/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2016 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/

package org.spin.model;

import java.math.BigDecimal;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.model.MPaySelection;
import org.compiere.model.MPayment;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

/**
 * Loan Management Model Validator
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1583 ] New Definition for loan
 *		@see https://github.com/adempiere/adempiere/issues/1583
 */
public class LoanManagementModelValidator implements ModelValidator {
    private static CLogger log = CLogger.getCLogger(LoanManagementModelValidator.class);
    
    /** Client			*/
	private int		clientId = -1;
	
    @Override
    public void initialize(ModelValidationEngine engine, MClient client) {
		//	
    	if (client != null) {	
			clientId = client.getAD_Client_ID();
		}
		engine.addModelChange(MFMAgreement.Table_Name, this);
		engine.addDocValidate(MFMAgreement.Table_Name, this);
		engine.addDocValidate(MPaySelection.Table_Name, this);
		engine.addDocValidate(MPayment.Table_Name, this);
    }

    @Override
    public int getAD_Client_ID() {
    	return clientId;
    }

    @Override
    public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
        return null;
    }

    @Override
    public String modelChange(PO po, int type) throws Exception {
    	if (po.get_TableName().equals(I_FM_Agreement.Table_Name)) {
    		if(type == TYPE_AFTER_NEW || type == TYPE_AFTER_CHANGE) {
    			log.fine("FM_Agreement = type == TYPE_AFTER_NEW || type == TYPE_AFTER_CHANGE");
    			MFMAgreement agreement = (MFMAgreement) po;
    			if(agreement.isProcessed()) {
    				return null;
    			}
    			//	get all account
    			List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
    			MFMAccount account = null;
    			if (accounts.isEmpty()){
	    			account = new MFMAccount(agreement);
	    			account.saveEx();
    			} else {
    				account = accounts.get(0);
    			}
    			//	Create Product
    			if(agreement.getFM_Product_ID() != 0) {
    				MFMAccountProduct accountProduct = new MFMAccountProduct(account);
    				accountProduct.setFM_Product_ID(agreement.getFM_Product_ID());
    				accountProduct.setValidFrom(agreement.getDateDoc());
    				accountProduct.saveEx();
    			}
    		} else if(type == TYPE_AFTER_DELETE) {
    			log.fine("FM_Agreement = type == TYPE_AFTER_DELETE");
    			MFMAgreement agreement = (MFMAgreement) po;
    			if(agreement.isProcessed()) {
    				return null;
    			}
    			//	get all account
    			List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
    			for(MFMAccount account : accounts) {
    				account.deleteEx(true);
    			}
    		}
    	}
        return null;
    }

    /**
     * Document Validate for Standard Request Type
     * @param entity
     * @param timing see TIMING_ constants
     * @return
     */
    public String docValidate(PO entity, int timing) {
    	if (entity instanceof MPaySelection) {
			if (timing == TIMING_BEFORE_COMPLETE) {
				MPaySelection paymentSelection = (MPaySelection) entity;
				String sql = new String("SELECT ag.FM_Agreement_ID, ag.DocumentNo "
						+ "FROM FM_Agreement ag "
						+ "INNER JOIN FM_Account ac ON(ac.FM_Agreement_ID = ag.FM_Agreement_ID) "
						+ "INNER JOIN (SELECT pl.FM_Account_ID, SUM(pl.AmtSource) AmtSource "
						+ "FROM C_PaySelectionLine pl "
						+ "WHERE EXISTS(SELECT 1 FROM C_PaySelection ps "
						+ "				WHERE ps.C_PaySelection_ID = pl.C_PaySelection_ID "
						+ "				AND ps.DocStatus IN('CO')) "
						+ "				GROUP BY pl.FM_Account_ID) ps ON(ps.FM_Account_ID = ac.FM_Account_ID) "
						+ "WHERE ac.CapitalAmt = COALESCE(ps.AmtSource, 0) "
						+ "AND EXISTS(SELECT 1 FROM C_PaySelectionLine pl "
						+ "WHERE pl.FM_Account_ID = ac.FM_Account_ID "
						+ "AND pl.C_PaySelection_ID = ?)");
				//	Get From DB
				KeyNamePair[] loanArray = DB.getKeyNamePairs(paymentSelection.get_TrxName(), sql, false, paymentSelection.getC_PaySelection_ID());
				if(loanArray != null
						&& loanArray.length > 0) {
					StringBuffer msg = new StringBuffer();
					for(KeyNamePair loan : loanArray) {
						if(msg.length() > 0) {
							msg.append(",").append(Env.NL);
						}
						//	Add Message
						msg.append(loan.getName());
					}
					//	Get Message
					if(msg.length() > 0) {
						throw new AdempiereException("@Loan@ @Processed@ [" + msg.toString() + "]");
					}
				}
			}
		} else if (entity instanceof MPayment) {
			if (timing == TIMING_AFTER_COMPLETE) {
				MPayment payment = (MPayment) entity;
				if(payment.getReversal_ID() != 0) {
					return null;
				}
				String sql = new String("SELECT pl.FM_Account_ID "
						+ "FROM C_PaySelectionLine pl "
						+ "INNER JOIN C_PaySelectionCheck pc ON(pc.C_PaySelectionCheck_ID = pl.C_PaySelectionCheck_ID) "
						+ "WHERE pc.C_Payment_ID = ?");
				//	Get 
				int financialAccountId = DB.getSQLValue(payment.get_TrxName(), sql, payment.getC_Payment_ID());
				if(financialAccountId > 0) {
					MFMAccount account = new MFMAccount(payment.getCtx(), financialAccountId, payment.get_TrxName());
					//	
					account.set_ValueOfColumn("IsPaid", true);
					account.saveEx();
				}
			} else if(timing == TIMING_AFTER_REVERSECORRECT
					|| timing == TIMING_AFTER_REVERSEACCRUAL
					|| timing == TIMING_AFTER_VOID) {
				MPayment payment = (MPayment) entity;
				String sql = new String("SELECT pl.FM_Account_ID "
						+ "FROM C_PaySelectionLine pl "
						+ "INNER JOIN C_PaySelectionCheck pc ON(pc.C_PaySelectionCheck_ID = pl.C_PaySelectionCheck_ID) "
						+ "WHERE pc.C_Payment_ID = ?");
				//	Get 
				int financialAccountId = DB.getSQLValue(payment.get_TrxName(), sql, payment.getC_Payment_ID());
				if(financialAccountId > 0) {
					MFMAccount account = new MFMAccount(payment.getCtx(), financialAccountId, payment.get_TrxName());
					//	
					account.set_ValueOfColumn("IsPaid", false);
					account.saveEx();
				}
			}
		} else if (entity instanceof MFMAgreement) {
			if (timing == TIMING_AFTER_COMPLETE) {
				MFMAgreement agreement = (MFMAgreement) entity;
				//	Get values from amortization
				List<MFMAccount> accountList = agreement.getAccounts();
				if(accountList != null
						&& !accountList.isEmpty()) {
					BigDecimal capitalAmt = Env.ZERO;
					BigDecimal interestAmt = Env.ZERO;
					BigDecimal taxAmt = Env.ZERO;
					for(MFMAccount account : accountList) {
						List<MFMAmortization> amortizationList = MFMAmortization
								.getFromAccount(account.getFM_Account_ID(), account.get_TrxName());
						//	Iterate
						for(MFMAmortization amortization : amortizationList) {
							//	Capital Amount
							if(amortization.getCapitalAmt() != null) {
								capitalAmt = capitalAmt.add(amortization.getCapitalAmt());
							}
							//	Interest Amount
							if(amortization.getInterestAmt() != null) {
								interestAmt = interestAmt.add(amortization.getInterestAmt());
							}
							//	Tax Amount
							if(amortization.getTaxAmt() != null) {
								taxAmt = taxAmt.add(amortization.getTaxAmt());
							}
						}
						//	Set Capital
						if(account.get_Value("CapitalAmt") == null
								|| ((BigDecimal) account.get_Value("CapitalAmt")).equals(Env.ZERO)) {
							account.set_ValueOfColumn("CapitalAmt", capitalAmt);
						}
						//	Set Interest
						if(account.get_Value("InterestAmt") == null
								|| ((BigDecimal) account.get_Value("InterestAmt")).equals(Env.ZERO)) {
							account.set_ValueOfColumn("InterestAmt", interestAmt);
						}
						//	Set Tax
						if(account.get_Value("TaxAmt") == null
								|| ((BigDecimal) account.get_Value("TaxAmt")).equals(Env.ZERO)) {
							account.set_ValueOfColumn("TaxAmt", taxAmt);
						}
						account.saveEx();	
					}
				}
			} else if(timing == TIMING_BEFORE_REVERSECORRECT
					|| timing == TIMING_BEFORE_REVERSEACCRUAL
					|| timing == TIMING_BEFORE_VOID
					|| timing == TIMING_BEFORE_REACTIVATE) {
				MFMAgreement agreement = (MFMAgreement) entity;
				//	Get values from amortization
				List<MFMAccount> accountList = agreement.getAccounts();
				StringBuffer inClause = new StringBuffer();
				if(accountList != null
						&& !accountList.isEmpty()) {
					for(MFMAccount account : accountList) {
						if(inClause.length() > 0) {
							inClause.append(", ");
						}
						//	Add
						inClause.append(account.getFM_Account_ID());
					}
				}
				//	Validate and get result
				if(inClause.length() > 0) {
					//	For Payment Selection
					String sql = new String("SELECT ps.C_PaySelection_ID, ps.DocumentNo "
							+ "FROM C_PaySelection ps "
							+ "INNER JOIN C_PaySelectionLine pl ON(pl.C_PaySelection_ID = ps.C_PaySelection_ID) "
							+ "WHERE ps.DocStatus IN('CO', 'CL') "
							+ "AND pl.FM_Account_ID IN(" + inClause.toString() + ")");
					KeyNamePair[] paySelectionResult = DB.getKeyNamePairs(agreement.get_TrxName(), sql, false);
					//	For Payment
					sql = new String("SELECT p.C_Payment_ID, p.DocumentNo "
							+ "FROM C_PaySelectionLine pl "
							+ "INNER JOIN C_PaySelectionCheck pc ON(pc.C_PaySelectionCheck_ID = pl.C_PaySelectionCheck_ID) "
							+ "INNER JOIN C_Payment p ON(p.C_Payment_ID = pc.C_Payment_ID) "
							+ "WHERE p.DocStatus IN('CO', 'CL') "
							+ "AND EXISTS(SELECT 1 FROM C_PaySelection ps "
							+ "				WHERE ps.C_PaySelection_ID = pl.C_PaySelection_ID"
							+ "				AND ps.DocStatus IN('CO', 'CL')) "
							+ "AND pl.FM_Account_ID IN(" + inClause.toString() + ")");
					KeyNamePair[] paymentResult = DB.getKeyNamePairs(agreement.get_TrxName(), sql, false);
					//	For Invoice
					sql = new String("SELECT i.C_Invoice_ID, i.DocumentNo "
							+ "FROM C_Invoice i "
							+ "WHERE i.DocStatus IN('CO', 'CL') "
							+ "AND i.FM_Account_ID IN(" + inClause.toString() + ")");
					KeyNamePair[] invoiceResult = DB.getKeyNamePairs(agreement.get_TrxName(), sql, false);
					//	Show Result for Payment Selection
					StringBuffer resultToShow = new StringBuffer();
					if(paySelectionResult != null
							&& paySelectionResult.length > 0) {
						if(resultToShow.length() > 0) {
							resultToShow.append(Env.NL);
						}
						//	Add result
						resultToShow.append(Msg.getElement(agreement.getCtx(), "C_PaySelection_ID"));
						//	Get result
						for(KeyNamePair result : paySelectionResult) {
							if(resultToShow.length() > 0) {
								resultToShow.append(Env.NL);
							}
							//	Add result
							resultToShow.append(result.getName());
						}
					}
					//	Show Result for Payment
					if(paymentResult != null
							&& paymentResult.length > 0) {
						if(resultToShow.length() > 0) {
							resultToShow.append(Env.NL);
						}
						//	Add result
						resultToShow.append(Msg.getElement(agreement.getCtx(), "C_Payment_ID"));
						//	Get result
						for(KeyNamePair result : paymentResult) {
							if(resultToShow.length() > 0) {
								resultToShow.append(Env.NL);
							}
							//	Add result
							resultToShow.append(result.getName());
						}
					}
					//	Show Result for Invoice
					if(invoiceResult != null
							&& invoiceResult.length > 0) {
						if(resultToShow.length() > 0) {
							resultToShow.append(Env.NL);
						}
						//	Add result
						resultToShow.append(Msg.getElement(agreement.getCtx(), "C_Invoice_ID"));
						//	Get result
						for(KeyNamePair result : invoiceResult) {
							if(resultToShow.length() > 0) {
								resultToShow.append(Env.NL);
							}
							//	Add result
							resultToShow.append(result.getName());
						}
					}
					//	Return result if exists
					if(resultToShow.length() > 0) {
						return Msg.getMsg(agreement.getCtx(), "SQLErrorReferenced") + Env.NL + resultToShow.toString();
					}
				}
			}
		}
        return null;
    }
}
