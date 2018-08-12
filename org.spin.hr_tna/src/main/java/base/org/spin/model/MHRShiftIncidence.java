/**************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                               *
 * This program is free software; you can redistribute it and/or modify it    		  *
 * under the terms version 2 or later of the GNU General Public License as published  *
 * by the Free Software Foundation. This program is distributed in the hope           *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied         *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                   *
 * See the GNU General Public License for more details.                               *
 * You should have received a copy of the GNU General Public License along            *
 * with this program; if not, printLine to the Free Software Foundation, Inc.,        *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                             *
 * For the text or an alternative of this public license, you may reach us            *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved.  *
 * Contributor: Yamel Senih ysenih@erpya.com                                          *
 * See: www.erpya.com                                                                 *
 *************************************************************************************/
package org.spin.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.util.TimeUtil;
import org.jfree.data.time.Millisecond;

/**
 * 	Class added for handle shift incidence
 * 	@author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/1870>
 * 		@see FR [ 1870 ] Add Calulation for Attendance Record</a>
 */
public class MHRShiftIncidence extends X_HR_ShiftIncidence {

	/**
	 * 
	 */
	private static final long serialVersionUID = 625050579283149689L;

	/**
	 * @param ctx
	 * @param HR_ShiftIncidence_ID
	 * @param trxName
	 */
	public MHRShiftIncidence(Properties ctx, int HR_ShiftIncidence_ID, String trxName) {
		super(ctx, HR_ShiftIncidence_ID, trxName);
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MHRShiftIncidence(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	
	/**
	 * Evaluate if it can generate a incidence
	 * @param attendanceTime
	 * @return
	 */
	public boolean evaluateTime(Timestamp attendanceTime) {
		Timestamp evaluateTime = TimeUtil.getDayTime(getTimeFrom(), attendanceTime);
		if(isMandatoryRange()) {
			return TimeUtil.isValid(getTimeFrom(), getTimeTo(), evaluateTime);
		} else {
			return TimeUtil.isValid(getTimeFrom(), evaluateTime, evaluateTime);
		}
	}
	
	/**
	 * Get Duration in {@link Millisecond}, it can be used after evaluate time
	 * A example for entrance
	 * <li>[(8:00)------------(8:30)----------------------------(9:30)-------------------------(12:00)]
	 * <li>[(From)--------(Beginning Time)-----------------(Attendance Time)----------------------(To)]
	 * <li>Range for evaluate: From 8:00 ~ 12:00
	 * <li>Range for create incidence: 8:30 ~ 12:00
	 * <li>Attendance Time is: (9:30 - 8:30) = 1 Hour
	 * @param attendanceTime
	 * @return
	 */
	public long getDurationInMillis(Timestamp attendanceTime) {
		if(!evaluateTime(attendanceTime)) {
			return 0;
		}
		long duration = 0;
		Timestamp evaluateTime = TimeUtil.getDayTime(getTimeFrom(), attendanceTime);
		Timestamp beginninTime = getBeginningTime();
		Timestamp timeFrom = getTimeFrom();
		Timestamp timeTo = getTimeTo();
		//	Change time to
		if(timeTo.before(timeFrom)) {
			timeTo = TimeUtil.getDayTime(TimeUtil.addDays(timeTo, 1), timeTo);
		}
		if(getEventType().equals(EVENTTYPE_Entrance)) {
			if(beginninTime == null) {
				beginninTime = timeFrom;
			}
			//	Add to duration
			if(TimeUtil.isValid(beginninTime, getTimeTo(), evaluateTime)) {
				duration = evaluateTime.getTime() - beginninTime.getTime();
			}
		} else if(getEventType().equals(EVENTTYPE_Egress)) {
			if(beginninTime == null) {
				beginninTime = timeFrom;
			}
			//	Add to duration
			if(evaluateTime.after(beginninTime)) {
				if(evaluateTime.after(timeTo)) {
					evaluateTime = timeTo;
				}
				//	Calculate
				duration = evaluateTime.getTime() - beginninTime.getTime();
			}
			//	Is from last to beginning
			if(duration == 0) {
				beginninTime = getTimeTo();
				if(evaluateTime.before(beginninTime)) {
					if(evaluateTime.before(timeFrom)) {
						evaluateTime = timeFrom;
					}
					duration = beginninTime.getTime() - evaluateTime.getTime();
				}
			}
		}
		//	Return
		return duration;
	}

	@Override
	public String toString() {
		return "MHRShiftIncidence [getBeginningTime()=" + getBeginningTime() + ", getDescription()=" + getDescription()
				+ ", getEventType()=" + getEventType() + ", getHR_Concept_ID()=" + getHR_Concept_ID()
				+ ", isOnFriday()=" + isOnFriday() + ", isOnMonday()=" + isOnMonday() + ", isOnSaturday()="
				+ isOnSaturday() + ", isOnSunday()=" + isOnSunday() + ", isOnThursday()=" + isOnThursday()
				+ ", isOnTuesday()=" + isOnTuesday() + ", isOnWednesday()=" + isOnWednesday() + ", getTimeFrom()="
				+ getTimeFrom() + ", getTimeTo()=" + getTimeTo() + ", getTimeUnit()=" + getTimeUnit() + "]";
	}
}
