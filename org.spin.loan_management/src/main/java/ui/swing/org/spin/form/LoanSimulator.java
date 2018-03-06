/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.									  *
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
package org.spin.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;

import org.compiere.apps.IStatusBar;
import org.compiere.model.MCurrency;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.spin.model.MFMFunctionalApplicability;
import org.spin.util.FinancialSetting;

/**
 * Financial Management
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1585 ] Loan Simulator
 *		@see https://github.com/adempiere/adempiere/issues/1585
 */
public abstract class LoanSimulator {

	/**	Window No			*/
	public int         	windowNo = 0;
	/**	Business Partner	*/
	public int 			businessPartnerId = 0;
	/**	Financial Product	*/
	public int 			financialProductId = 0;
	/**	Currency			*/
	public int 			currencyId = 0;
	/**	Capital Amount		*/
	public BigDecimal	capitalAmt;
	/**	Fees Amount		*/
	public int	feesAmt;
	/**	Fee Amount		*/
	public BigDecimal	feeAmt;
	/**	Interest Amount		*/
	public BigDecimal	interestAmt;
	/**	Tax Amount		*/
	public BigDecimal	taxAmt;	
	/**	Grand Total		*/
	public BigDecimal	grandTotalAmt;
	/**	Start Date		*/
	public Timestamp	startDate;
	/**	Logger			*/
	public static CLogger log = CLogger.getCLogger(LoanSimulator.class);
	
	/**
	 *  Dynamic Layout (Grid).
	 */
	public void dynInit(IStatusBar statusBar) {
		statusBar.setStatusLine(" ", false);
		statusBar.setStatusDB(" ");
	}   //  dynInit
	
	/**************************************************************************
	 *  Refresh - Create Query and refresh grid
	 */
	public void refresh(IStatusBar statusBar) {
		statusBar.setStatusLine(" ", false);
	}   //  refresh
	
	/**
	 * Simulate Data
	 * @param trxName
	 * @return
	 */
	public String simulateData(String trxName) {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		//	Add Parameters
		parameters.put("FINANCIAL_PRODUCT_ID", financialProductId);
		parameters.put("BUSINESS_PARTNER_ID", businessPartnerId);
		parameters.put("CAPITAL_AMT", capitalAmt);
		parameters.put("FEES_AMT", feesAmt);
		parameters.put("START_DATE", startDate);
		//	
		FinancialSetting setting = FinancialSetting.get();
		//	Set Values
		String errorMsg = setting.fire(Env.getCtx(), financialProductId, MFMFunctionalApplicability.EVENTTYPE_Simulation, parameters, trxName);
		//	
		HashMap<String, Object> returnValues = setting.getReturnValues();
		//	Round
		int currencyPrecision = MCurrency.getStdPrecision(Env.getCtx(), Env.getContextAsInt(Env.getCtx(), "#C_Currency_ID"));
		//	
		BigDecimal fixedFeeAmt = (BigDecimal) returnValues.get("FIXED_FEE_AMT");
		BigDecimal interestFeeAmt = (BigDecimal) returnValues.get("INTEREST_FEE_AMT");
		BigDecimal taxAmt = (BigDecimal) returnValues.get("TAX_FEE_AMT");
		BigDecimal grandTotal = (BigDecimal) returnValues.get("GRAND_TOTAL");
		if(fixedFeeAmt != null) {
			setFeeAmt(fixedFeeAmt.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		}
		if(interestFeeAmt != null) {
			setInterestFeeAmt(interestFeeAmt.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		}
		if(taxAmt != null) {
			setTaxFeeAmt(taxAmt.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		}
		if(grandTotal != null) {
			setGrandToral(grandTotal.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		}
		//	Set Error
		return errorMsg;
	}
	
	/**
	 * Set Fee Amount
	 * @param feeAmt
	 */
	public abstract void setFeeAmt(BigDecimal feeAmt);
	
	/**
	 * Set Interest Fee
	 * @param interestFeeAmt
	 */
	public abstract void setInterestFeeAmt(BigDecimal interestFeeAmt);
	
	/**
	 * Set Tax Fee Amount
	 * @param taxFeeAmt
	 */
	public abstract void setTaxFeeAmt(BigDecimal taxFeeAmt);
	
	/**
	 * Set Grand Tota
	 * @param grandTotal
	 */
	public abstract void setGrandToral(BigDecimal grandTotal);

}
