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
package org.spin.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.model.MCurrency;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;

/**
 * Loan Amortization Summary
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class MFMAmortizationSummary extends X_FM_AmortizationSummary {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MFMAmortizationSummary(Properties ctx, int FM_Amortization_ID, String trxName) {
		super(ctx, FM_Amortization_ID, trxName);
	}

	public MFMAmortizationSummary(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	/**
	 * Set current interest from account and date
	 * @param ctx
	 * @param financialAccountId
	 * @param amortizationId
	 * @param dateDoc
	 * @param capitalAmount
	 * @param currentInsterestAmount
	 * @param currentInsterestTaxAmount
	 * @param trxName
	 * @return
	 */
	public static MFMAmortizationSummary setCurrentInterest(Properties ctx, int financialAccountId, int amortizationId, Timestamp dateDoc, BigDecimal capitalAmount, BigDecimal currentInsterestAmount, BigDecimal currentInsterestTaxAmount, String trxName) {
		MFMAmortizationSummary summary = getAmortizationSummaryFromAccountAndDate(ctx, financialAccountId, amortizationId, dateDoc, trxName);
		MFMAccount account = MFMAccount.getById(ctx, financialAccountId);
		int currencyPrecision = MCurrency.getStdPrecision(ctx, account.getC_Currency_ID());
		summary.setCurrentCapitalAmt(capitalAmount.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		summary.setCurrentInterestAmt(currentInsterestAmount.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		summary.setCurrentTaxAmt(currentInsterestTaxAmount.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		summary.saveEx();
		return summary;
	}
	
	/**
	 * Set current dunning from account and date
	 * @param ctx
	 * @param financialAccountId
	 * @param amortizationId
	 * @param dateDoc
	 * @param capitalAmount
	 * @param currentDunningAmount
	 * @param currentDunningTaxAmount
	 * @param trxName
	 * @return
	 */
	public static MFMAmortizationSummary setCurrentDunning(Properties ctx, int financialAccountId, int amortizationId, Timestamp dateDoc, BigDecimal capitalAmount, BigDecimal currentDunningAmount, BigDecimal currentDunningTaxAmount, String trxName) {
		MFMAmortizationSummary summary = getAmortizationSummaryFromAccountAndDate(ctx, financialAccountId, amortizationId, dateDoc, trxName);
		MFMAccount account = MFMAccount.getById(ctx, financialAccountId);
		int currencyPrecision = MCurrency.getStdPrecision(ctx, account.getC_Currency_ID());
		summary.setCurrentCapitalAmt(capitalAmount.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		summary.setCurrentDunningAmt(currentDunningAmount.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		summary.setCurrentDunningTaxAmt(currentDunningTaxAmount.setScale(currencyPrecision, BigDecimal.ROUND_HALF_UP));
		summary.saveEx();
		return summary;
	}
	
	/**
	 * Get Amortization summary from account and date
	 * @param ctx
	 * @param financialAccountId
	 * @param amortizationId
	 * @param dateDoc
	 * @param trxName
	 * @return
	 */
	private static MFMAmortizationSummary getAmortizationSummaryFromAccountAndDate(Properties ctx, int financialAccountId, int amortizationId, Timestamp dateDoc, String trxName) {
		dateDoc = TimeUtil.getDay(dateDoc);
		MFMAmortizationSummary summary = new Query(ctx, Table_Name, COLUMNNAME_FM_Account_ID + " = ? AND " + COLUMNNAME_FM_Amortization_ID + " = ? AND " + COLUMNNAME_DateDoc + " = ?", trxName)
				.setParameters(financialAccountId, amortizationId, dateDoc)
				.setOnlyActiveRecords(true).setClient_ID()
				.first();
		if(summary == null
				|| summary.getFM_AmortizationSummary_ID() == 0) {
			summary = new MFMAmortizationSummary(ctx, 0, trxName);
			summary.setDateDoc(dateDoc);
			summary.setFM_Account_ID(financialAccountId);
			summary.setFM_Amortization_ID(amortizationId);
		}
		//	
		return summary;
	}
	
	@Override
	public BigDecimal getCurrentCapitalAmt() {
		if(super.getCurrentCapitalAmt() == null) {
			return Env.ZERO;
		}
		return super.getCurrentCapitalAmt();
	}
	
	@Override
	public BigDecimal getCurrentInterestAmt() {
		if(super.getCurrentInterestAmt() == null) {
			return Env.ZERO;
		}
		return super.getCurrentInterestAmt();
	}
	
	@Override
	public BigDecimal getCurrentDunningAmt() {
		if(super.getCurrentDunningAmt() == null) {
			return Env.ZERO;
		}
		return super.getCurrentDunningAmt();
	}
	
	@Override
	public BigDecimal getCurrentDunningTaxAmt() {
		if(super.getCurrentDunningTaxAmt() == null) {
			return Env.ZERO;
		}
		return super.getCurrentDunningTaxAmt();
	}
	
	@Override
	public BigDecimal getCurrentTaxAmt() {
		if(super.getCurrentTaxAmt() == null) {
			return Env.ZERO;
		}
		return super.getCurrentTaxAmt();
	}
	
	@Override
	protected boolean beforeSave(boolean newRecord) {
		setCurrentFeeAmt(getCurrentCapitalAmt()
				.add(getCurrentInterestAmt())
				.add(getCurrentTaxAmt())
				.add(getCurrentDunningAmt())
				.add(getCurrentDunningTaxAmt()));
		return super.beforeSave(newRecord);
	}
	
	@Override
	public String toString() {
		return "MFMAmortizationSummary [getCurrentDunningAmt()="
				+ getCurrentDunningAmt() + ", getCurrentDunningTaxAmt()=" + getCurrentDunningTaxAmt()
				+ ", getCurrentFeeAmt()=" + getCurrentFeeAmt() + ", getCurrentInterestAmt()=" + getCurrentInterestAmt()
				+ ", getCurrentTaxAmt()=" + getCurrentTaxAmt() + ", getDateDoc()=" + getDateDoc()
				+ ", getFM_Account_ID()=" + getFM_Account_ID() + "]";
	}
}
