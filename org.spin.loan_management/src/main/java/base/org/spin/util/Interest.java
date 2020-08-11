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
import org.spin.model.MFMRate;


/**
 * Interest Stub class
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class Interest {
	private int rateId;
	private int chargeId;
	private BigDecimal interestRate;
	private BigDecimal taxRate;
	private Properties context;
	private String transactionName;
	private Timestamp runningDate;
	
	/**
	 * @param context the context to set
	 */
	public final Interest withContext(Properties context) {
		this.context = context;
		return this;
	}

	/**
	 * @param transactionName the transactionName to set
	 */
	public final Interest withTransactionName(String transactionName) {
		this.transactionName = transactionName;
		return this;
	}
	
	/**
	 * @return the rateId
	 */
	public final int getRateId() {
		return rateId;
	}

	/**
	 * @param rateId the rateId to set
	 */
	public final Interest withRateId(int rateId) {
		this.rateId = rateId;
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
	public final Interest withRunningDate(Timestamp runningDate) {
		this.runningDate = runningDate;
		return this;
	}

	/**
	 * Instance for it
	 * @return
	 */
	public static Interest newInstance() {
		return new Interest();
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
	public Interest buildRate() {
		if(context == null) {
			throw new AdempiereException("Missing Context");
		}
		if(rateId <= 0) {
			throw new AdempiereException("Missing Rate ID");
		}
		//	Get Interest Rate
		//	Validate Dunning for it
		if(rateId == 0) {
			return this;
		}
		//	
		MCharge charge = null;
		if(rateId != 0) {
			MFMRate rate = MFMRate.getById(context, rateId);
			//	
			interestRate = rate.getValidRate(runningDate);
			charge = MCharge.get(context, rate.getC_Charge_ID());
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
			interestRate = interestRate.add(interestRate.multiply(taxRate));
		}
		//	
		return this;
	}

	@Override
	public String toString() {
		return "Interest [rateId=" + rateId + ", chargeId=" + chargeId + ", interestRate=" + interestRate + ", taxRate="
				+ taxRate + ", runningDate=" + runningDate + "]";
	}
}
