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

import java.sql.Timestamp;

import org.compiere.util.TimeUtil;

/**
 * Loan French Method
 * FixedFeeAmt = Loan [(Interest (1 + Interest)n) / ((1 + Interest)n – 1)]
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1588 ] Add support to Payment Period for Loan
 *		@see https://github.com/adempiere/adempiere/issues/1588
 */
public class LoanUtil {

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
}
