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
 * Contributor(s): Carlos Parada www.erpya.com                                *
 *****************************************************************************/
package org.spin.util;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.spin.model.MFMAccount;
import org.spin.model.MFMAgreement;
import org.spin.model.MFMBatch;
import org.spin.model.MFMDunning;
import org.spin.model.MFMFunctionalSetting;
import org.spin.model.MFMProduct;
import org.spin.model.MFMRate;
import org.spin.model.MFMTransaction;
import org.spin.model.MFMTransactionType;

/**
 * Generate Batch from Invoce
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1678 ] Add Fact Accounting for Agreement Batch
 *		@see https://github.com/adempiere/adempiere/issues/1678
 */
public class CreateBatchFromInvoice extends AbstractFunctionalSetting {

	public CreateBatchFromInvoice(MFMFunctionalSetting setting) {
		super(setting);
	}

	@Override
	public String run() {
		MInvoice invoice = (MInvoice) getParameter(FinancialSetting.PARAMETER_PO);
		if(invoice != null) {
			return generateBatch(invoice);
		}
		//	
		return null;
	}
	
	/**
	 *  Getnerate Batch from invoice
	 * @param invoice
	 * @return
	 */
	private String generateBatch(MInvoice invoice){
		int financialAccountId = invoice.get_ValueAsInt("FM_Account_ID");
    	if(financialAccountId <= 0) {
    		return null;
    	}
    	//	Get Account
    	MFMAccount account = new MFMAccount(invoice.getCtx(), financialAccountId, invoice.get_TrxName());
    	setParameter(FinancialSetting.ACCOUNT_PO, account);
    	//	Get Agreement
    	MFMAgreement agreement = (MFMAgreement) account.getFM_Agreement();
    	//	Get Financial Product
    	MFMProduct financialProduct = MFMProduct.getById(getCtx(), agreement.getFM_Product_ID());
    	//	Create Batch
    	MFMBatch batch = createBatch(invoice.getDateInvoiced());
    	if(batch != null) {
    		MFMTransactionType capitalType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanInterestInvoiced);
    		//	Validate
    		if(capitalType == null) {
    			throw new AdempiereException("@FM_TransactionType_ID@ @NotFound@ " + MFMTransactionType.TYPE_LoanInterestInvoiced);
    		}
    		//	Get Interest Rate
    		int rateId = financialProduct.get_ValueAsInt("FM_Rate_ID");
    		int dunningRateId = financialProduct.get_ValueAsInt("DunningInterest_ID");
    		int dunningId = financialProduct.get_ValueAsInt("FM_Dunning_ID");
    		int interestChargeId = 0;
    		int dunningChargeId = 0;
    		//	
    		if(rateId != 0) {
    			MFMRate rate = MFMRate.getById(getCtx(), rateId);
    			interestChargeId = rate.getC_Charge_ID();
    		}
    		//	
    		if(dunningRateId != 0) {
    			MFMRate rate = MFMRate.getById(getCtx(), dunningRateId);
    			dunningChargeId = rate.getC_Charge_ID();
    		} else if(dunningId != 0) {
    			MFMDunning dunning = MFMDunning.getById(getCtx(), dunningId);
    			dunningChargeId = dunning.getChargeId();
    		}
    		MFMTransactionType interetType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanInterestInvoiced);
    		MFMTransactionType interestTaxType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanTaxAmountInvoiced);
    		//	Validate
    		if(interetType == null) {
    			throw new AdempiereException("@FM_TransactionType_ID@ @NotFound@ " + MFMTransactionType.TYPE_LoanInterestInvoiced);
    		}
    		MFMTransactionType dunningType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanDunningInterestInvoiced);
    		MFMTransactionType dunningTaxType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanDunningTaxAmountInvoiced);
    		//	Validate
    		if(dunningType == null) {
    			throw new AdempiereException("@FM_TransactionType_ID@ @NotFound@ " + MFMTransactionType.TYPE_LoanDunningInterestInvoiced);
    		}
    		//	Add Transactions
    		//	For Capital
    		for(MInvoiceLine line : invoice.getLines()) {
    			MFMTransaction transaction = null;
    			MFMTransaction transactionTax = null;
    			if(line.getM_Product_ID() != 0
    					&& line.getM_Product_ID() == financialProduct.getM_Product_ID()) {	//	Capital
    				transaction = batch.addTransaction(capitalType.getFM_TransactionType_ID(), line.getLineNetAmt());
    			} else if(line.getC_Charge_ID() != 0
    					&& line.getC_Charge_ID() == interestChargeId) {	//	Interest
    				transaction = batch.addTransaction(interetType.getFM_TransactionType_ID(), line.getLineNetAmt());
    				transactionTax = batch.addTransaction(interestTaxType.getFM_TransactionType_ID(), line.getTaxAmt());
    			} else if(line.getC_Charge_ID() != 0
    					&& line.getC_Charge_ID() == dunningChargeId) {
    				transaction = batch.addTransaction(dunningType.getFM_TransactionType_ID(), line.getLineNetAmt());
    				transactionTax = batch.addTransaction(dunningTaxType.getFM_TransactionType_ID(), line.getTaxAmt());
    			}
    			//	Add reference
    			if(transaction != null) {
					if(line.get_ValueAsInt("FM_Amortization_ID") != 0) {
						transaction.set_ValueOfColumn("FM_Amortization_ID", line.get_ValueAsInt("FM_Amortization_ID"));
						transaction.set_ValueOfColumn("C_InvoiceLine_ID", line.getC_InvoiceLine_ID());
    					transaction.saveEx();
					}
					if(transactionTax != null) {
						if(line.get_ValueAsInt("FM_Amortization_ID") != 0) {
							transactionTax.set_ValueOfColumn("FM_Amortization_ID", line.get_ValueAsInt("FM_Amortization_ID"));
							transactionTax.set_ValueOfColumn("C_InvoiceLine_ID", line.getC_InvoiceLine_ID());
							transactionTax.saveEx();
						}
					}
				}
    		}
			//	Complete Batch
    		batch.processIt(MFMBatch.ACTION_Complete);
			batch.saveEx();
    	}
		return null;
	}
}
