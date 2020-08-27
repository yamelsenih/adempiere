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
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package org.spin.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.util.Env;
import org.spin.model.MFMAccount;
import org.spin.model.MFMAgreement;
import org.spin.model.MFMAmortization;
import org.spin.model.MFMAmortizationSummary;
import org.spin.model.MFMBatch;
import org.spin.model.MFMFunctionalSetting;
import org.spin.model.MFMTransaction;
import org.spin.model.MFMTransactionType;

/**
 * Loan Daily Interest Calculation
 * Calculate Daily Interest
 * 			(A Variable)					(B Variable)
 * 			((1 + InterestRate) ^ (MonthlyDays / YEAR_DAY)) - 1
 *			Dunning Interest = (DaysDue * InterestRate)
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1666 ] Calculate daily Loan Interest
 *		@see https://github.com/adempiere/adempiere/issues/1666
 */
public class LoanInterestProcess extends AbstractFunctionalSetting {

	public LoanInterestProcess(MFMFunctionalSetting setting) {
		super(setting);
	}

	@Override
	public String run() {
		MFMAgreement agreement = (MFMAgreement) getParameter(FinancialSetting.AGREEMENT_PO);
		MFMBatch batch = (MFMBatch) getParameter(FinancialSetting.BATCH_PO);
		String trxName = (String) getParameter(FinancialSetting.PARAMETER_TRX_NAME);
		//	Nothing
		if(agreement == null
				|| batch == null) {
			return null;
		}
		
		MFMTransactionType interestType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanInterestCalculated);
		MFMTransactionType interestTaxType = MFMTransactionType.getTransactionTypeFromType(getCtx(), MFMTransactionType.TYPE_LoanInterestTaxCalculated);
		//	Validate
		if(interestType == null) {
			throw new AdempiereException("@FM_TransactionType_ID@ @NotFound@ " + MFMTransactionType.TYPE_LoanInterestCalculated);
		}
		//	
		HashMap<String, Object> returnValues = LoanUtil.calculateLoanInterest(getCtx(), agreement.getFM_Agreement_ID(), 
				new Timestamp(System.currentTimeMillis()), trxName);
		//	Process it
		if(returnValues == null
				|| returnValues.isEmpty()) {
			return null;
		}
		//	Else
		@SuppressWarnings("unchecked")
		List<AmortizationValue> amortizationList = (List<AmortizationValue>) returnValues.get("AMORTIZATION_LIST");
		if(amortizationList == null) {
			return null;
		}
		AtomicReference<BigDecimal> capitalAmount = new AtomicReference<BigDecimal>(Env.ZERO);
		AtomicReference<BigDecimal> interestAmount = new AtomicReference<BigDecimal>(Env.ZERO);
		AtomicReference<BigDecimal> interestTaxAmount = new AtomicReference<BigDecimal>(Env.ZERO);
		amortizationList.forEach(amortization -> {
			capitalAmount.updateAndGet(amount -> amount.add(amortization.getCapitalAmtFee()));
			interestAmount.updateAndGet(amount -> amount.add(amortization.getInterestAmtFee()));
			interestTaxAmount.updateAndGet(amount -> amount.add(amortization.getTaxAmtFee()));
		});
		List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
		MFMAccount account = null;
		if (accounts.isEmpty()){
			account = new MFMAccount(agreement);
			account.saveEx();
		} else {
			account = accounts.get(0);
		}
		//	Iterate
		for (AmortizationValue amortizationReference : amortizationList) {
			//	
			MFMTransaction transaction = batch.addTransaction(interestType.getFM_TransactionType_ID(), amortizationReference.getInterestAmtFee());
			if(transaction != null) {
				transaction.set_ValueOfColumn("FM_Amortization_ID", amortizationReference.getAmortizationId());
				transaction.saveEx();
			}
			if(interestTaxType != null
					&& amortizationReference.getTaxAmtFee() != null) {
				transaction = batch.addTransaction(interestTaxType.getFM_TransactionType_ID(), amortizationReference.getTaxAmtFee());
				if(transaction != null) {
					transaction.set_ValueOfColumn("FM_Amortization_ID", amortizationReference.getAmortizationId());
					transaction.saveEx();
				}
			}
			//	Summary for Amortization
			MFMAmortization amortization = new MFMAmortization(getCtx(), amortizationReference.getAmortizationId(), trxName);
			amortization.setCurrentCapitalAmt(capitalAmount.get());
			amortization.setCurrentInterestAmt(interestAmount.get());
			amortization.setCurrentTaxAmt(interestTaxAmount.get());
			amortization.saveEx();
			//	Set Interest
			MFMAmortizationSummary.setCurrentInterest(getCtx(), account.getFM_Account_ID(), amortizationReference.getAmortizationId(), batch.getDateDoc(), capitalAmount.get(), interestAmount.get(), interestTaxAmount.get(), trxName);
		}

		return null;
	}
}
