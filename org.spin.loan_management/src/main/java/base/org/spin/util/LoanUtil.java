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
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MCharge;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.spin.model.MFMAccount;
import org.spin.model.MFMAgreement;
import org.spin.model.MFMAmortization;
import org.spin.model.MFMDunning;
import org.spin.model.MFMDunningLevel;
import org.spin.model.MFMProduct;
import org.spin.model.MFMRate;

/**
 * Loan French Method
 * FixedFeeAmt = Loan [(Interest (1 + Interest)n) / ((1 + Interest)n – 1)]
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1588 ] Add support to Payment Period for Loan
 *		@see https://github.com/adempiere/adempiere/issues/1588
 */
public class LoanUtil {

	/**	It is hardcode and must be changed	*/
	private final static BigDecimal YEAR_DAY = new BigDecimal(360);
	
	/**
	 * Get End Date from Frequency and Start Date
	 * @param startDate
	 * @param paymentFrequency
	 * @param feesQty
	 * @return
	 */
	public static Timestamp getEndDateFromFrequency(Timestamp startDate, String paymentFrequency, int feesQty) {
		if(startDate == null
				|| paymentFrequency == null) {
			return null;
		}
		//	Add by frequency
		if(paymentFrequency.equals("D")) {	//	Daily
			return TimeUtil.addDays(startDate, feesQty);
		} else if(paymentFrequency.equals("W")) {	//	Weekly
			return TimeUtil.addDays(startDate, feesQty * 7);
		} else if(paymentFrequency.equals("T")) {	//	Twice Monthly
			return TimeUtil.addDays(startDate, feesQty * 15);
		} else if(paymentFrequency.equals("M")) {	//	Monthly
			return TimeUtil.addMonths(startDate, feesQty);
		} else if(paymentFrequency.equals("Q")) {	//	Quarterly
			return TimeUtil.addMonths(startDate, feesQty * 3);
		} else if(paymentFrequency.equals("S")) {	//	Semi-yearly
			return TimeUtil.addMonths(startDate, feesQty * 6);
		} else if(paymentFrequency.equals("Y")) {	//	Yearly
			return TimeUtil.addYears(startDate, feesQty);
		} else if(paymentFrequency.equals("F")) {	//	Single Fee
			return startDate;
		}
		//	
		return null;
	}
	
	/**
	 * Get Fees Quantity from Start and End Date
	 * @param startDate
	 * @param endDate
	 * @param paymentFrequency
	 * @return
	 */
	public static int getFeesQtyFromFrequency(Timestamp startDate, Timestamp endDate, String paymentFrequency) {
		if(startDate == null
				|| endDate == null
				|| paymentFrequency == null) {
			return 0;
		}
		//	Add by frequency
		if(paymentFrequency.equals("D")) {	//	Daily
			return TimeUtil.getDaysBetween(startDate, endDate);
		} else if(paymentFrequency.equals("W")) {	//	Weekly
			return TimeUtil.getDaysBetween(startDate, endDate) / 7;
		} else if(paymentFrequency.equals("T")) {	//	Twice Monthly
			return TimeUtil.getDaysBetween(startDate, endDate) / 15;
		} else if(paymentFrequency.equals("M")) {	//	Monthly
			return TimeUtil.getMonthsBetween(startDate, endDate);
		} else if(paymentFrequency.equals("Q")) {	//	Quarterly
			return TimeUtil.getMonthsBetween(startDate, endDate) / 3;
		} else if(paymentFrequency.equals("S")) {	//	Semi-yearly
			return TimeUtil.getMonthsBetween(startDate, endDate) / 6;
		} else if(paymentFrequency.equals("Y")) {	//	Yearly
			return TimeUtil.getYearsBetween(startDate, endDate);
		} else if(paymentFrequency.equals("F")) {	//	Single Fee
			return 1;
		}
		//	Default
		return 0;
	}
	
	/**
	 * Based on French
	 * @param financialProductId
	 * @param capitalAmt
	 * @param feesQty
	 * @param startDate
	 * @param endDate
	 * @param payDate
	 * @param paymentFrequency
	 * @param ctx
	 * @return
	 */
	public static HashMap<String, Object> calculateFrenchAmortization(int financialProductId, BigDecimal capitalAmt, 
																			int feesQty,Timestamp startDate,
																				Timestamp endDate, Timestamp payDate,
																					String paymentFrequency, Properties ctx){
		/**	Return Value */
		HashMap<String, Object> returnValues = new HashMap<String, Object>();
		
		if(startDate == null) {
			startDate = new Timestamp(System.currentTimeMillis());
		}
		if(payDate == null
				|| payDate.before(startDate)) {
			payDate = startDate;
		}
		//	Calculate it
		MFMProduct financialProduct = MFMProduct.getById(ctx, financialProductId);
		//	Get Interest Rate
		int rateId = financialProduct.get_ValueAsInt("FM_Rate_ID");
		BigDecimal interestRate = Env.ZERO;
		BigDecimal interestRateAndTaxRate = Env.ZERO;
		BigDecimal taxRate = Env.ZERO;
		MCharge charge = null;
		if(rateId != 0) {
			MFMRate rate = MFMRate.getById(ctx, rateId);
			//	
			interestRate = rate.getValidRate(startDate);
			charge = MCharge.get(ctx, rate.getC_Charge_ID());
		}
		//	Validate Charge
		if(charge != null) {
			//	Get Tax Rate
			MTaxCategory taxCategory = (MTaxCategory) charge.getC_TaxCategory();
			MTax tax = taxCategory.getDefaultTax();
			//	Calculate rate for fee (Year Interest + (Tax Rate * Year Interest))
			interestRate = interestRate.divide(Env.ONEHUNDRED);
			taxRate = tax.getRate().divide(Env.ONEHUNDRED);
			//	Calculate
			interestRateAndTaxRate = interestRate.add(interestRate.multiply(taxRate));
		}
		//	Hash Map for Amortization
		List<AmortizationValue> amortizationList = new ArrayList<AmortizationValue>();
		//	Cumulative
		//	Current Date
		Timestamp currentDate = startDate;
		Timestamp currentDueDate = payDate;
		int cumulativeDays = 0;
		BigDecimal currentInterest = Env.ZERO;
		BigDecimal remainingCapital = capitalAmt;
		//	First Iteration for it
		for(int i = 0; i < feesQty; i++) {
			AmortizationValue row = new LoanUtil().new AmortizationValue();
			//	Period No
			row.setPeriodNo(i + 1);
			//	Start Date
			row.setStartDate(currentDate);
			//	End Date
			if(!paymentFrequency.equals("F")) {
				currentDate = LoanUtil.getEndDateFromFrequency(currentDate, paymentFrequency, 1);
			} else {
				currentDate = endDate;
			}
			row.setEndDate(currentDate);
			//	Due Date
			if(currentDueDate.before(row.getEndDate())) {
				currentDueDate = row.getEndDate();
			}
			row.setDueDate(currentDueDate);
			if(!paymentFrequency.equals("F")) {
				currentDueDate = LoanUtil.getEndDateFromFrequency(currentDate, paymentFrequency, 1);
			}
			//	Set Cumulative Days
			cumulativeDays += row.getDayOfMonth();
			row.setCumulativeDays(cumulativeDays);
			//	Calculate Monthly Interest
			BigDecimal monthlyInterest = calculateMonthlyInterest(row.getCumulativeDays(), interestRateAndTaxRate);
			BigDecimal dailyInterest = calculateDailyInterest(row.getDayOfMonth(), interestRateAndTaxRate);
			row.setMonthInterest(monthlyInterest);
			row.setDailyInterest(dailyInterest);
			currentInterest = currentInterest.add(row.getMonthInterest());
			row.setCumulativeInterest(currentInterest);
			//	Add to hash
			amortizationList.add(row);
		}
		//	Second Iteration for it (Calculate Remaining Capital, )
		//	Get Fee
		BigDecimal fixedFeeAmt = capitalAmt.divide(currentInterest, MathContext.DECIMAL128);
		BigDecimal summaryInterestAmt = Env.ZERO;
		BigDecimal summaryTaxAmt = Env.ZERO;
		for(int i = 0; i < amortizationList.size(); i++) {
			AmortizationValue row = amortizationList.get(i);
			//	Set Interest Amount Fee
			BigDecimal interestFeeAmt = row.getDailyInterest().multiply(remainingCapital);
			interestFeeAmt = interestFeeAmt.divide(Env.ONE.add(taxRate), MathContext.DECIMAL128);
			row.setInterestAmtFee(interestFeeAmt);
			//	Set Tax Fee Amount
			BigDecimal taxAmtFee = interestFeeAmt.multiply(taxRate);
			row.setTaxAmtFee(taxAmtFee);
			//	Set Capital Amount Fee
			row.setCapitalAmtFee(fixedFeeAmt.subtract(interestFeeAmt.add(taxAmtFee)));
			//	Set Remaining Capital
			remainingCapital = remainingCapital.subtract(row.getCapitalAmtFee());
			row.setRemainingCapital(remainingCapital);
			//	Set Daily Interest Amount
			row.setDailyInterestAmt(row.getInterestAmtFee().divide(new BigDecimal(row.getCumulativeDays()), MathContext.DECIMAL128));
			//	Set Fixed Fee
			row.setFixedFeeAmt(fixedFeeAmt);
			//	Set Object
			amortizationList.set(i, row);
			//	Summarize
			summaryInterestAmt = summaryInterestAmt.add(row.getInterestAmtFee());
			summaryTaxAmt = summaryTaxAmt.add(taxAmtFee);
		}
		
		returnValues.put("FIXED_FEE_AMT", fixedFeeAmt);
		returnValues.put("INTEREST_FEE_AMT", summaryInterestAmt);
		returnValues.put("TAX_FEE_AMT", summaryTaxAmt);
		returnValues.put("GRAND_TOTAL", summaryInterestAmt.add(summaryTaxAmt).add(capitalAmt));
		returnValues.put("AMORTIZATION_LIST", amortizationList);
		
		return returnValues;
	}
	
	/**
	 * Calculate Daily interest
	 * @param days
	 * @param interestRate
	 * @return
	 */
	public static BigDecimal calculateDailyInterest(int days, BigDecimal interestRate) {
		//	Calculate Daily Interest
		//				(A Variable)					(B Variable)
		//	((1 + InterestRate) ^ (MonthlyDays / YEAR_DAY)) - 1
		BigDecimal _A_Variable = (Env.ONE.add(interestRate));
		BigDecimal _B_Variable = (new BigDecimal(days).divide(YEAR_DAY, MathContext.DECIMAL128));
		BigDecimal _Result_A_pow_B = new BigDecimal(Math.pow(_A_Variable.doubleValue(), _B_Variable.doubleValue()));
		BigDecimal dailyInterest = _Result_A_pow_B.subtract(Env.ONE);
		return dailyInterest;
	}
	
	/**
	 * Calculate Monthly Interest
	 * @param cumulativeDays
	 * @param interestRate
	 * @return
	 */
	public static BigDecimal calculateMonthlyInterest(int cumulativeDays, BigDecimal interestRate) {
		//	Calculate Monthly Interest
		//				(A Variable)					(B Variable)
		//	1 / ((1 + InterestRateAndTaxRate ) ^ (CumulativeDays / YEAR_DAY))
		BigDecimal _A_Variable = (Env.ONE.add(interestRate));
		BigDecimal _B_Variable = (new BigDecimal(cumulativeDays).divide(LoanUtil.YEAR_DAY, MathContext.DECIMAL128));
		BigDecimal _Result_A_pow_B = new BigDecimal(Math.pow(_A_Variable.doubleValue(), _B_Variable.doubleValue()));
		BigDecimal monthInterest = Env.ONE.divide(_Result_A_pow_B, MathContext.DECIMAL128);
		return monthInterest;
	}
	
	/**
	 * Only dunning of loan
	 * @param ctx
	 * @param agreementId
	 * @param runningDate
	 * @param trxName
	 * @return
	 */
	public static HashMap<String, Object> calculateLoanDunning(Properties ctx, int agreementId, Timestamp runningDate, String trxName){
		//	Validate agreement
		if(agreementId <= 0) {
			return null;
		}
		/**	Return Value */
		HashMap<String, Object> returnValues = new HashMap<String, Object>();
		//	if null then is now
		if(runningDate == null) {
			runningDate = new Timestamp(System.currentTimeMillis());
		}
		//	Get agreement
		MFMAgreement agreement = new MFMAgreement(ctx, agreementId, trxName);
		//	Calculate it
		MFMProduct financialProduct = MFMProduct.getById(ctx, agreement.getFM_Product_ID());
		//	Get Interest Rate
		int dunningRateId = financialProduct.get_ValueAsInt("DunningInterest_ID");
		int dunningId = financialProduct.get_ValueAsInt("FM_Dunning_ID");
		//	Validate Dunning for it
		if(dunningRateId == 0
				&& dunningId == 0) {
			return null;
		}
		//	
		BigDecimal interestRate = Env.ZERO;
		BigDecimal taxRate = Env.ZERO;
		MCharge charge = null;
		if(dunningRateId != 0) {
			MFMRate rate = MFMRate.getById(ctx, dunningRateId);
			//	
			interestRate = rate.getValidRate(runningDate);
			if(interestRate != null) {
				interestRate = interestRate.divide(Env.ONEHUNDRED);
			} else {
				interestRate = Env.ZERO;
			}
			charge = MCharge.get(ctx, rate.getC_Charge_ID());
		}
		//	Validate Charge
		if(charge != null) {
			//	Get Tax Rate
			MTaxCategory taxCategory = (MTaxCategory) charge.getC_TaxCategory();
			MTax tax = taxCategory.getDefaultTax();
			//	Calculate rate for fee (Year Interest + (Tax Rate * Year Interest))
			taxRate = tax.getRate();
			if(taxRate != null) {
				taxRate = taxRate.divide(Env.ONEHUNDRED);
			} else {
				taxRate = Env.ZERO;
			}
		}
		MFMDunning dunning = null;
		//	Get dunning configuration if exist
		if(dunningId > 0) {
			dunning = MFMDunning.getById(ctx, dunningId);
		}
		//	Get
		List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
		MFMAccount account = null;
		if (accounts.isEmpty()){
			account = new MFMAccount(agreement);
			account.saveEx();
		} else {
			account = accounts.get(0);
		}
		//	Hash Map for Amortization
		List<AmortizationValue> amortizationList = new ArrayList<AmortizationValue>();
		//	
		for(MFMAmortization amortization : MFMAmortization.getFromAccount(account.getFM_Account_ID(), trxName)) {
			AmortizationValue row = new LoanUtil().new AmortizationValue(amortization);
			if(row.getDaysDue(runningDate) <= 0) {
				continue;
			}
			//	For distinct levels
			MFMDunningLevel level = null;
			int dunningLevelRateId = 0;
			if(dunning != null
					&& dunningRateId == 0) {
				level = dunning.getValidLevelInstance(row.getDaysDue());
				dunningLevelRateId = level.getFM_Rate_ID();
				if(dunningLevelRateId > 0) {
					MFMRate rate = MFMRate.getById(ctx, dunningLevelRateId);
					//	
					interestRate = rate.getValidRate(runningDate);
					charge = MCharge.get(ctx, rate.getC_Charge_ID());
					//	Validate Charge
					if(charge != null) {
						//	Get Tax Rate
						MTaxCategory taxCategory = (MTaxCategory) charge.getC_TaxCategory();
						MTax tax = taxCategory.getDefaultTax();
						//	Calculate rate for fee (Year Interest + (Tax Rate * Year Interest))
						interestRate = interestRate.divide(Env.ONEHUNDRED);
						taxRate = tax.getRate().divide(Env.ONEHUNDRED);
					}
				}
			}
			//	
			BigDecimal dailyInterest = calculateDailyInterest(row.getDaysDue(), interestRate);
			if(dailyInterest != null) {
				row.setDunningDailyInterest(dailyInterest);
				BigDecimal capitalAmt = row.getCapitalAmtFee();
				BigDecimal dunningInteretAmount = capitalAmt.multiply(dailyInterest);
				row.setDunningInterestAmount(dunningInteretAmount);
				//	For Tax
				if(taxRate != null) {
					row.setDunningTaxRate(taxRate);
					row.setDunningTaxAmt(dunningInteretAmount.multiply(taxRate));
				}
			}
			//	Add to list
			amortizationList.add(row);
		}
		//	Add list
		returnValues.put("AMORTIZATION_LIST", amortizationList);
		return returnValues;
	}
	
	/**
	 * Only Interest of loan
	 * @param ctx
	 * @param agreementId
	 * @param runningDate
	 * @param trxName
	 * @return
	 */
	public static HashMap<String, Object> calculateLoanInterest(Properties ctx, int agreementId, Timestamp runningDate, String trxName){
		//	Validate agreement
		if(agreementId <= 0) {
			return null;
		}
		/**	Return Value */
		HashMap<String, Object> returnValues = new HashMap<String, Object>();
		//	if null then is now
		if(runningDate == null) {
			runningDate = new Timestamp(System.currentTimeMillis());
		}
		//	Get agreement
		MFMAgreement agreement = new MFMAgreement(ctx, agreementId, trxName);
		//	Calculate it
		MFMProduct financialProduct = MFMProduct.getById(ctx, agreement.getFM_Product_ID());
		//	Get Interest Rate
		int rateId = financialProduct.get_ValueAsInt("FM_Rate_ID");
		//	Validate Dunning for it
		if(rateId == 0) {
			return null;
		}
		//	
		BigDecimal interestRate = Env.ZERO;
		BigDecimal taxRate = Env.ZERO;
		MCharge charge = null;
		if(rateId != 0) {
			MFMRate rate = MFMRate.getById(ctx, rateId);
			//	
			interestRate = rate.getValidRate(agreement.getDateDoc());
			charge = MCharge.get(ctx, rate.getC_Charge_ID());
		}
		//	Validate Charge
		if(charge != null) {
			//	Get Tax Rate
			MTaxCategory taxCategory = (MTaxCategory) charge.getC_TaxCategory();
			MTax tax = taxCategory.getDefaultTax();
			//	Calculate rate for fee (Year Interest + (Tax Rate * Year Interest))
			interestRate = interestRate.divide(Env.ONEHUNDRED);
			taxRate = tax.getRate().divide(Env.ONEHUNDRED);
		}
		//	Get
		List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
		MFMAccount account = null;
		if (accounts.isEmpty()){
			account = new MFMAccount(agreement);
			account.saveEx();
		} else {
			account = accounts.get(0);
		}
		//	Hash Map for Amortization
		List<AmortizationValue> amortizationList = new ArrayList<AmortizationValue>();
		//	
		BigDecimal remainingCapital = (BigDecimal) account.get_Value("CapitalAmt");
		for(MFMAmortization amortization : MFMAmortization.getFromAccount(account.getFM_Account_ID(), trxName)) {
			AmortizationValue row = new LoanUtil().new AmortizationValue(amortization);
			if(row.isPaid()) {
				continue;
			}
			//	Validate after
			if(runningDate.before(row.getStartDate())) {
				continue;
			}
			//	
			if(row.getEndDate().before(runningDate)) {
				runningDate = row.getEndDate();
			}
			//	
			BigDecimal dailyInterest = calculateDailyInterest(row.getDayOfMonth(runningDate), interestRate);
			if(dailyInterest != null) {
				row.setDailyInterest(dailyInterest);
				row.setInterestAmtFee(dailyInterest.multiply(remainingCapital));
				//	For Tax
				if(taxRate != null) {
					row.setTaxAmtFee(row.getInterestAmtFee().multiply(taxRate));
				}
			}
			remainingCapital = remainingCapital.subtract(row.getCapitalAmtFee());
			//	Add to list
			amortizationList.add(row);
		}
		//	Add list
		returnValues.put("AMORTIZATION_LIST", amortizationList);
		return returnValues;
	}
	
	/**
	 * Used for values on amortization
	 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
	 *      <li> FR [ 1583 ] New Definition for loan
	 *		@see https://github.com/adempiere/adempiere/issues/1583
	 */
	public class AmortizationValue {
		
		public AmortizationValue() {
			
		}
		
		/**
		 * From saved amortization
		 * @param amortization
		 */
		public AmortizationValue(MFMAmortization amortization) {
			setPeriodNo(amortization.getPeriodNo());
			setStartDate(amortization.getStartDate());
			setEndDate(amortization.getEndDate());
			setDueDate(amortization.getDueDate());
			setCapitalAmtFee(amortization.getCapitalAmt());
			setTaxAmtFee(amortization.getTaxAmt());
			setInterestAmtFee(amortization.getInterestAmt());
			setAmortizationId(amortization.getFM_Amortization_ID());
			setPaid(amortization.isPaid());
		}
		
		/**	Period No	*/
		private int periodNo = 0;
		/**	Start Date	*/
		private Timestamp startDate;
		/**	End Date	*/
		private Timestamp endDate;
		/**	Due Date	*/
		private Timestamp dueDate;
		/**	Days of Month	*/
		private int dayOfMonth = 0;
		/**	Cumulative Days	*/
		private int cumulativeDays = 0;
		/**	Remaining Capital	*/
		private BigDecimal remainingCapital;
		/**	Month Interest	*/
		private BigDecimal monthInterest;
		/**	Cumulative Interest	*/
		private BigDecimal cumulativeInterest;
		/**	Daily Interest	*/
		private BigDecimal dailyInterest;
		/**	Interest Amount	*/
		private BigDecimal interestAmtFee;
		/**	Capital Amount	*/
		private BigDecimal capitalAmtFee;
		/**	Tax Amount	*/
		private BigDecimal taxAmtFee;
		/**	Daily Interest	*/
		private BigDecimal dailyInterestAmt;
		/**	Fee Amount	*/
		private BigDecimal fixedFeeAmt;
		/**	Amortization ID	*/
		private int amortizationId;
		/**	Days Due	*/
		private int daysDue;
		/**	Dunning daily interest	*/
		private BigDecimal dunningDailyInterest;
		/**	Dunning Interest Amount	*/
		private BigDecimal dunningInterestAmount;
		/**	Dunning Tax Rate	*/
		private BigDecimal dunningTaxRate;
		/**	Dunning Tax Amount	*/
		private BigDecimal dunningTaxAmt;
		/**	Is Paid	*/
		private boolean isPaid;
		
		public int getPeriodNo() {
			return periodNo;
		}
		public void setPeriodNo(int periodNo) {
			this.periodNo = periodNo;
		}
		public Timestamp getStartDate() {
			return startDate;
		}
		public void setStartDate(Timestamp startDate) {
			this.startDate = startDate;
		}
		public Timestamp getEndDate() {
			return endDate;
		}
		public void setEndDate(Timestamp endDate) {
			this.endDate = endDate;
		}
		public int getDayOfMonth() {
			if(dayOfMonth == 0
					&& getStartDate() != null
					&& getEndDate() != null) {
				dayOfMonth = TimeUtil.getDaysBetween(getStartDate(), getEndDate());
			}
			return dayOfMonth;
		}
		/**
		 * Get Day of month from current date
		 * @param now
		 * @return
		 */
		public int getDayOfMonth(Timestamp now) {
			if(getStartDate() != null
					&& now != null) {
				return TimeUtil.getDaysBetween(getStartDate(), now);
			}
			return 0;
		}
		public void setDayOfMonth(int dayOfMonth) {
			this.dayOfMonth = dayOfMonth;
		}
		public int getCumulativeDays() {
			return cumulativeDays;
		}
		public void setCumulativeDays(int cumulativeDays) {
			this.cumulativeDays = cumulativeDays;
		}
		public BigDecimal getRemainingCapital() {
			return remainingCapital;
		}
		public void setRemainingCapital(BigDecimal remainingCapital) {
			this.remainingCapital = remainingCapital;
		}
		public BigDecimal getMonthInterest() {
			return monthInterest;
		}
		public void setMonthInterest(BigDecimal monthInterest) {
			this.monthInterest = monthInterest;
		}
		public BigDecimal getCumulativeInterest() {
			return cumulativeInterest;
		}
		public void setCumulativeInterest(BigDecimal cumulativeInterest) {
			this.cumulativeInterest = cumulativeInterest;
		}
		public BigDecimal getDailyInterest() {
			return dailyInterest;
		}
		public void setDailyInterest(BigDecimal dailyRateInterestAmt) {
			this.dailyInterest = dailyRateInterestAmt;
		}
		public BigDecimal getInterestAmtFee() {
			return interestAmtFee;
		}
		public void setInterestAmtFee(BigDecimal interestAmt) {
			this.interestAmtFee = interestAmt;
		}
		public BigDecimal getCapitalAmtFee() {
			return capitalAmtFee;
		}
		public void setCapitalAmtFee(BigDecimal capitalAmt) {
			this.capitalAmtFee = capitalAmt;
		}
		public BigDecimal getDailyInterestAmt() {
			return dailyInterestAmt;
		}
		public void setDailyInterestAmt(BigDecimal dailyInterestAmt) {
			this.dailyInterestAmt = dailyInterestAmt;
		}
		public BigDecimal getTaxAmtFee() {
			return taxAmtFee;
		}
		public void setTaxAmtFee(BigDecimal taxAmtFee) {
			this.taxAmtFee = taxAmtFee;
		}
		public BigDecimal getFixedFeeAmt() {
			return fixedFeeAmt;
		}
		public void setFixedFeeAmt(BigDecimal fixedFeeAmt) {
			this.fixedFeeAmt = fixedFeeAmt;
		}
		public Timestamp getDueDate() {
			return dueDate;
		}
		public void setDueDate(Timestamp dueDate) {
			this.dueDate = dueDate;
		}
		public int getAmortizationId() {
			return amortizationId;
		}
		public void setAmortizationId(int amortizationId) {
			this.amortizationId = amortizationId;
		}
		public int getDaysDue() {
			return daysDue;
		}
		public int getDaysDue(Timestamp now) {
			if(getDueDate() != null
					&& now != null) {
				daysDue = TimeUtil.getDaysBetween(getDueDate(), now);
			}
			return daysDue;
		}
		public void setDaysDue(int daysDue) {
			this.daysDue = daysDue;
		}
		public BigDecimal getDunningDailyInterest() {
			return dunningDailyInterest;
		}
		public void setDunningDailyInterest(BigDecimal dunningDailyInterest) {
			this.dunningDailyInterest = dunningDailyInterest;
		}
		public BigDecimal getDunningInterestAmount() {
			return dunningInterestAmount;
		}
		public void setDunningInterestAmount(BigDecimal dunningInterestAmount) {
			this.dunningInterestAmount = dunningInterestAmount;
		}
		public BigDecimal getDunningTaxRate() {
			return dunningTaxRate;
		}
		public void setDunningTaxRate(BigDecimal dunningTaxRate) {
			this.dunningTaxRate = dunningTaxRate;
		}
		public BigDecimal getDunningTaxAmt() {
			return dunningTaxAmt;
		}
		public void setDunningTaxAmt(BigDecimal dunningTaxAmt) {
			this.dunningTaxAmt = dunningTaxAmt;
		}
		public boolean isPaid() {
			return isPaid;
		}
		public void setPaid(boolean isPaid) {
			this.isPaid = isPaid;
		}

		@Override
		public String toString() {
			return "AmortizationValue [periodNo=" + periodNo + ", startDate=" + startDate + ", endDate=" + endDate
					+ ", dueDate=" + dueDate + ", dayOfMonth=" + dayOfMonth + ", cumulativeDays=" + cumulativeDays
					+ ", remainingCapital=" + remainingCapital + ", monthInterest=" + monthInterest
					+ ", cumulativeInterest=" + cumulativeInterest + ", dailyInterest=" + dailyInterest
					+ ", interestAmtFee=" + interestAmtFee + ", capitalAmtFee=" + capitalAmtFee + ", taxAmtFee="
					+ taxAmtFee + ", dailyInterestAmt=" + dailyInterestAmt + ", fixedFeeAmt=" + fixedFeeAmt
					+ ", amortizationId=" + amortizationId + ", daysDue=" + daysDue + ", dunningDailyInterest="
					+ dunningDailyInterest + ", dunningInteretAmount=" + dunningInterestAmount + ", dunningTaxRate="
					+ dunningTaxRate + ", dunningTaxAmt=" + dunningTaxAmt + "]";
		}
	}
}