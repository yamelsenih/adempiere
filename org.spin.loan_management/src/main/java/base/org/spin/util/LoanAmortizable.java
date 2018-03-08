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

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import org.compiere.model.MCurrency;
import org.compiere.model.MProduct;
import org.compiere.model.MTax;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.spin.model.MFMAccount;
import org.spin.model.MFMAgreement;
import org.spin.model.MFMAmortization;
import org.spin.model.MFMFunctionalSetting;
import org.spin.model.MFMProduct;
import org.spin.model.MFMRate;
import org.spin.model.MFMRateVersion;

/**
 * Financial Management
 *
 * @author Carlos Parada, cparada@erpya.com , http://www.erpya.com
 *      <li> FR [ 1586 ] Generate Amortization
 *		@see https://github.com/adempiere/adempiere/issues/1586
 */
public class LoanAmortizable extends AbstractFunctionalSetting {
	
	private static BigDecimal YEAR_DAYS = new BigDecimal(360);

	public LoanAmortizable(MFMFunctionalSetting setting) {
		super(setting);
	}

	@Override
	public String run() {
		// TODO Auto-generated method stub
		MFMAgreement loan = (MFMAgreement) getParameter(FinancialSetting.PARAMETER_PO);
		return generateAmortization(loan);
	}
	
	//Generate Loan Amortization 
	private String generateAmortization(MFMAgreement loan){
		
		boolean inserted = false;
		Properties ctx = loan.getCtx();
		String trxName = loan.get_TrxName();
		MCurrency currency = MCurrency.get(getCtx(), Env.getContextAsInt(ctx, "$C_Currency_ID"));
		int seq = 0;
		MFMProduct financialProduct = (MFMProduct) loan.getFM_Product();
		
		
		//Global 
		BigDecimal loanRate = getValidRate(financialProduct, loan.getValidFrom());
		if (loanRate.compareTo(Env.ZERO)==0)
			return "@Invalid@ @FM_Rate_ID@ @0@";
			
		BigDecimal taxRate = getTax((MProduct)financialProduct.getM_Product());
		BigDecimal AnualInterest = loanRate.add(
										taxRate.multiply(loanRate.divide(Env.ONEHUNDRED))
												);
		BigDecimal StaticFees =  Env.ZERO;
		BigDecimal CapitalAmt = Env.ZERO;
		
		
		//TimeUtil.getMonthFirstDay(currentDate)
		List<MFMAccount> accounts = new Query(ctx, MFMAccount.Table_Name, "FM_Agreement_ID = ? ", trxName)
									.setParameters(loan.getFM_Agreement_ID())
									.list();
		
		for (MFMAccount account : accounts) {
			if (MFMAmortization.checkAccount(account))
				continue;
			
			if (account.get_Value("CapitalAmt")== null)
				continue;
			
			if (account.get_Value("FeesQty")== null)
				continue;
			
				
			CapitalAmt = (BigDecimal)account.get_Value("CapitalAmt");
			//Delete Amortization 
			MFMAmortization.deleteForAccount(account);
			
			while (!inserted){
				int FeesQty = ((BigDecimal)account.get_Value("FeesQty")).intValue();
				if (account.get_Value("PayDate")==null)
					return "@Invalid@ @PayDate@";
				
				Timestamp StartDate = (Timestamp)account.get_Value("PayDate");
				Timestamp EndDate = TimeUtil.addMonths(loan.getValidFrom(), FeesQty);
				Timestamp currentDate = StartDate;
				Timestamp BeginPeriod =  currentDate;
				Timestamp EndPeriod =  currentDate;
				Timestamp DueDate = currentDate;
				//Detail
				int monthDays = 0;
				int cumulatedDays = 0;
				BigDecimal MonthInterest = Env.ZERO;
				BigDecimal MonthDayInterest = Env.ZERO;
				BigDecimal CumulatedInterest = Env.ZERO;
				BigDecimal CapitalRemain = CapitalAmt;
				BigDecimal CapitalFees = Env.ZERO;
				BigDecimal InterestFees = Env.ZERO;
				BigDecimal taxAmt = Env.ZERO;
				
				while (currentDate.before(EndDate)) {
					if (StaticFees.compareTo(Env.ZERO)!=0)
						inserted = true;
					
					BeginPeriod = TimeUtil.getMonthFirstDay(currentDate);
					EndPeriod = TimeUtil.getMonthLastDay(currentDate);
					monthDays = TimeUtil.getDaysBetween(BeginPeriod, EndPeriod) + 1;
					DueDate = TimeUtil.addDays(EndPeriod, 1);
					cumulatedDays += monthDays ;
					
					//Interest for Month
					MonthInterest = BigDecimal.ONE.divide(
											new BigDecimal(Math.pow(
																(Env.ONEHUNDRED.add(AnualInterest)).divide(Env.ONEHUNDRED,MathContext.DECIMAL128).doubleValue(),
																((new BigDecimal(cumulatedDays)).divide(LoanAmortizable.YEAR_DAYS,MathContext.DECIMAL128)).doubleValue()
																		
																	)
											
															)
														,MathContext.DECIMAL128);
					//Cumulated Interest
					CumulatedInterest = CumulatedInterest.add(MonthInterest);
					
					if (inserted){
						seq++;
						//Month Days Interest
						MonthDayInterest = new BigDecimal(Math.pow(
															(Env.ONEHUNDRED.add(AnualInterest)).divide(Env.ONEHUNDRED,MathContext.DECIMAL128).doubleValue(),
															((new BigDecimal(monthDays)).divide(LoanAmortizable.YEAR_DAYS,MathContext.DECIMAL128)).doubleValue()
																)
														).subtract(Env.ONE);
						//Capital Remain
						CapitalRemain = CapitalRemain.subtract(CapitalFees);
						
						//Interest Fees
						InterestFees = CapitalRemain.multiply(MonthDayInterest);
						
						//TaxAmt 
						taxAmt = InterestFees.subtract(InterestFees.divide(taxRate.divide(Env.ONEHUNDRED, MathContext.DECIMAL128).add(Env.ONE)
													, MathContext.DECIMAL128));
						//Capital Fees
						CapitalFees = StaticFees.subtract(InterestFees);
						
						//Create Amortization
						MFMAmortization.createAmortization(ctx, 
															CapitalFees.setScale(currency.getStdPrecision(), BigDecimal.ROUND_HALF_UP), 
															"", 
															DueDate, 
															EndPeriod, 
															account.getFM_Account_ID(), 
															InterestFees.subtract(taxAmt).setScale(currency.getStdPrecision(), BigDecimal.ROUND_HALF_UP), 
															seq, 
															BeginPeriod, 
															taxAmt.setScale(currency.getStdPrecision(), BigDecimal.ROUND_HALF_UP), 
															trxName);
					}
					
					currentDate = TimeUtil.addMonths(currentDate, 1);
					currentDate = TimeUtil.getMonthFirstDay(currentDate);
				}
				StaticFees = CapitalAmt.divide(CumulatedInterest, MathContext.DECIMAL128);
				
			}
		}
		return null;
	}
	
	/**
	 * Get Valid Rate
	 * @param financialProduct
	 * @param validDate
	 * @return
	 */
	private BigDecimal getValidRate(MFMProduct financialProduct, Timestamp validDate){
		MFMRate rate = new MFMRate(financialProduct.getCtx(), financialProduct.get_ValueAsInt("FM_Rate_ID"), financialProduct.get_TrxName());
		MFMRateVersion rateVersion = rate.getValidRateInstance(validDate);	
		if (rateVersion!= null)
			return rateVersion.getRate();
		else
			return BigDecimal.ZERO;
	}
	
	/**
	 * Get Tax Rate From Financial Product 
	 * @param product
	 * @return
	 */
	private BigDecimal getTax(MProduct product){
		MTax tax = new Query(product.getCtx(), MTax.Table_Name, "C_TaxCategory_ID = ? ", product.get_TrxName())
						.setParameters(product.getC_TaxCategory_ID())
						.setOrderBy("IsDefault DESC")
						.first();
		if (tax!=null)
			return tax.getRate();
		else
			return BigDecimal.ZERO;
	}

}
