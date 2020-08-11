/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 or later of the                                  *
 * GNU General Public License as published                                    *
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
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MCharge;
import org.compiere.model.MTax;
import org.compiere.model.MTaxCategory;
import org.compiere.util.Env;
import org.spin.model.MFMDunning;
import org.spin.model.MFMDunningLevel;
import org.spin.model.MFMRate;


/**
 * Dunning Stub class
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class Dunning {
	private int dunningId;
	private int dunningRateId;
	private int graceDay;
	private int chargeId;
	private BigDecimal interestRate;
	private BigDecimal taxRate;
	private Properties context;
	private String transactionName;
	private Timestamp runningDate;
	private MFMDunning dunning;
	
	/**
	 * @param context the context to set
	 */
	public final Dunning withContext(Properties context) {
		this.context = context;
		return this;
	}

	/**
	 * @param transactionName the transactionName to set
	 */
	public final Dunning withTransactionName(String transactionName) {
		this.transactionName = transactionName;
		return this;
	}

	/**
	 * @return the transactionName
	 */
	public final String getTransactionName() {
		return transactionName;
	}
	
	/**
	 * @return the runningDate
	 */
	public final Timestamp getRunningDate() {
		return runningDate;
	}

	/**
	 * @param runningDate the runningDate to set
	 */
	public final Dunning withRunningDate(Timestamp runningDate) {
		this.runningDate = runningDate;
		return this;
	}

	/**
	 * Instance for it
	 * @return
	 */
	public static Dunning newInstance() {
		return new Dunning();
	}

	/**
	 * @return the dunningId
	 */
	public final int getDunningId() {
		return dunningId;
	}

	/**
	 * @param dunningId the dunningId to set
	 */
	public final Dunning withDunningId(int dunningId) {
		this.dunningId = dunningId;
		return this;
	}

	/**
	 * @return the dunningRateId
	 */
	public final int getDunningRateId() {
		return dunningRateId;
	}

	/**
	 * @param dunningRateId the dunningRateId to set
	 */
	public final Dunning withDunningRateId(int dunningRateId) {
		this.dunningRateId = dunningRateId;
		return this;
	}

	/**
	 * @return the graceDay
	 */
	public final int getGraceDay() {
		return graceDay;
	}

	/**
	 * @return the chargeId
	 */
	public final int getChargeId() {
		return chargeId;
	}

	/**
	 * @return the interestRate
	 */
	public final BigDecimal getInterestRate() {
		return interestRate;
	}

	/**
	 * @return the taxRate
	 */
	public final BigDecimal getTaxRate() {
		return taxRate;
	}
	
	/**
	 * Calculate dunning based on parameters
	 * @return
	 */
	public Dunning buildRate() {
		if(context == null) {
			throw new AdempiereException("Missing Context");
		}
		if(dunningId <= 0) {
			throw new AdempiereException("Missing Dunning ID");
		}
		if(graceDay < 0) {
			throw new AdempiereException("Missing Grace Day");
		}
		//	if null then is now
		if(runningDate == null) {
			runningDate = new Timestamp(System.currentTimeMillis());
		}
		//	Validate Dunning for it
		if(dunningRateId == 0
				&& dunningId == 0) {
			return null;
		}
		//	
		MCharge charge = null;
		if(dunningRateId != 0) {
			MFMRate rate = MFMRate.getById(context, dunningRateId);
			//	
			interestRate = rate.getValidRate(runningDate);
			if(interestRate != null) {
				interestRate = interestRate.divide(Env.ONEHUNDRED);
			} else {
				interestRate = Env.ZERO;
			}
			charge = MCharge.get(context, rate.getC_Charge_ID());
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
		//	Get dunning configuration if exist
		if(dunningId > 0) {
			dunning = MFMDunning.getById(context, dunningId);
		}
		return this;
	}

	public Dunning buildRateFromLevel(int daysDue) {
		if(dunning == null) {
			return this;
		}
		MFMDunningLevel level = dunning.getValidLevelInstance(daysDue);
		if(level == null) {
			return this;
		}
		//	Apply for parent
		if(dunningRateId == 0) {
			int dunningLevelRateId = level.getFM_Rate_ID();
			if(dunningLevelRateId > 0) {
				MFMRate rate = MFMRate.getById(context, dunningLevelRateId);
				//	
				interestRate = rate.getValidRate(runningDate);
				MCharge charge = MCharge.get(context, rate.getC_Charge_ID());
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
		//	Validate Rate
		if(interestRate == null
				|| interestRate.doubleValue() == 0
				|| taxRate == null) {
			return this;
		}
		//	
		return this;
	}
	
	/**
	 * @return the dunning
	 */
	public final MFMDunning getDunning() {
		return dunning;
	}

	@Override
	public String toString() {
		return "Dunning [dunningId=" + dunningId + ", dunningRateId=" + dunningRateId + ", graceDay=" + graceDay
				+ ", chargeId=" + chargeId + ", interestRate=" + interestRate + ", taxRate=" + taxRate
				+ ", runningDate=" + runningDate + "]";
	}
}
