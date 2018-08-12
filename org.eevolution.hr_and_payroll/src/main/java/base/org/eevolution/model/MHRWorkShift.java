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
 * Copyright (C) 2003-2014 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *****************************************************************************/

package org.eevolution.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.compiere.model.Query;
import org.compiere.util.CCache;
import org.compiere.util.TimeUtil;
import org.compiere.util.Util;
import org.spin.model.I_HR_ShiftIncidence;
import org.spin.model.MHRShiftIncidence;
import org.spin.model.X_HR_ShiftIncidence;

/**
 * Created by victor.perez@e-evolution.com, e-Evolution on 03/12/13.
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/1870>
 * 		@see FR [ 1870 ] Add Calulation for Attendance Record</a>
 */
public class MHRWorkShift extends X_HR_WorkShift {

	public MHRWorkShift(Properties ctx, int HR_WorkShift_ID, String trxName) {
        super(ctx, HR_WorkShift_ID, trxName);
    }

    public MHRWorkShift(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 955746318164528261L;
    
	/** Cache */
	private static CCache<Integer, MHRWorkShift> workShiftCache = new CCache<Integer, MHRWorkShift>(Table_Name, 1000);
	/**	Shift Incidence List	*/
	private List<MHRShiftIncidence> shiftIncidenceList = null;
	/**	Hash for days	*/
	private HashMap<String, List<MHRShiftIncidence>> shiftIncidenceForDays = new HashMap<String, List<MHRShiftIncidence>>();
	
	/**
	 * Get Work Shift by Id
	 * @param ctx
	 * @param workShiftId
	 * @return
	 */
	public static MHRWorkShift getById(Properties ctx, int workShiftId) {
		if (workShiftId <= 0)
			return null;

		MHRWorkShift employee = workShiftCache.get(workShiftId);
		if (employee != null)
			return employee;

		employee = new MHRWorkShift(ctx, workShiftId, null);
		if (employee.get_ID() == workShiftId)
			workShiftCache.put(workShiftId, employee);
		else
			employee = null;
		return employee;
	}
	
	/**
	 * Get Shift Incidence List
	 * @param requery
	 * @return
	 */
	public List<MHRShiftIncidence> getShiftIncidenceList(boolean requery) {
		if(requery
				|| shiftIncidenceList == null) {
			shiftIncidenceList = new Query(getCtx(), I_HR_ShiftIncidence.Table_Name, "HR_WorkShift_ID = ?", get_TrxName())
					.setParameters(getHR_WorkShift_ID())
					.list();
			
		}
		//	Default return
		return shiftIncidenceList;
	}
	
	/**
	 * Get Shift Incidence List
	 * @param eventType
	 * @param attendanceTime
	 * @return
	 */
	public List<MHRShiftIncidence> getShiftIncidenceList(String eventType, Timestamp attendanceTime) {
		//	Validate
		if(attendanceTime == null
				|| Util.isEmpty(eventType)) {
			return new ArrayList<MHRShiftIncidence>();
		}
		//	Get reload false
		getShiftIncidenceList(false);
		if(shiftIncidenceList == null) {
			return new ArrayList<MHRShiftIncidence>();
		}
		String entrance = X_HR_ShiftIncidence.EVENTTYPE_Entrance;
		String egress = X_HR_ShiftIncidence.EVENTTYPE_Egress;
		String shiftAttendance = X_HR_ShiftIncidence.EVENTTYPE_ShiftAttendance;
		//	Load Hash
		if(shiftIncidenceForDays.isEmpty()) {
			//	Sunday
			shiftIncidenceForDays.put(entrance + Calendar.SUNDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress + Calendar.SUNDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance + Calendar.SUNDAY, new ArrayList<MHRShiftIncidence>());
			//	Monday
			shiftIncidenceForDays.put(entrance + Calendar.MONDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress + Calendar.MONDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance + Calendar.MONDAY, new ArrayList<MHRShiftIncidence>());
			//	Tuesday
			shiftIncidenceForDays.put(entrance + Calendar.TUESDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress + Calendar.TUESDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance + Calendar.TUESDAY, new ArrayList<MHRShiftIncidence>());
			//	Wednesday
			shiftIncidenceForDays.put(entrance + Calendar.WEDNESDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress + Calendar.WEDNESDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance + Calendar.WEDNESDAY, new ArrayList<MHRShiftIncidence>());
			//	Thursday
			shiftIncidenceForDays.put(entrance + Calendar.THURSDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress + Calendar.THURSDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance + Calendar.THURSDAY, new ArrayList<MHRShiftIncidence>());
			//	Friday
			shiftIncidenceForDays.put(entrance + Calendar.FRIDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress + Calendar.FRIDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance + Calendar.FRIDAY, new ArrayList<MHRShiftIncidence>());
			//	Saturday
			shiftIncidenceForDays.put(entrance + Calendar.SATURDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(egress+ Calendar.SATURDAY, new ArrayList<MHRShiftIncidence>());
			shiftIncidenceForDays.put(shiftAttendance+ Calendar.SATURDAY, new ArrayList<MHRShiftIncidence>());
			//	Add
			for(MHRShiftIncidence shiftIncidence : shiftIncidenceList) {
				//	Sunday
				if(shiftIncidence.isOnSunday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.SUNDAY).add(shiftIncidence);
				}
				//	Monday
				if(shiftIncidence.isOnMonday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.MONDAY).add(shiftIncidence);
				}
				//	Tuesday
				if(shiftIncidence.isOnTuesday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.TUESDAY).add(shiftIncidence);
				}
				//	Wednesday
				if(shiftIncidence.isOnWednesday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.WEDNESDAY).add(shiftIncidence);
				}
				//	Thursday
				if(shiftIncidence.isOnThursday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.THURSDAY).add(shiftIncidence);
				}
				//	Friday
				if(shiftIncidence.isOnFriday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.FRIDAY).add(shiftIncidence);
				}
				//	Saturday
				if(shiftIncidence.isOnSaturday()) {
					shiftIncidenceForDays.get(shiftIncidence.getEventType() + Calendar.SATURDAY).add(shiftIncidence);
				}
			}	
		}
		//	Return
		return shiftIncidenceForDays.get(eventType + getDayOfWeek(attendanceTime));
	}
	
	/**
	 * Get Day of Week
	 * @param attendanceTime
	 * @return
	 */
	private int getDayOfWeek(Timestamp attendanceTime) {
		Timestamp truncatedDay = TimeUtil.getDay(attendanceTime);
		Calendar calendar = TimeUtil.getCalendar(truncatedDay);
		//	Get
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	@Override
	public String toString() {
		return "MHRWorkShift [getBreakEndTime()=" + getBreakEndTime() + ", getBreakHoursNo()=" + getBreakHoursNo()
				+ ", getBreakStartTime()=" + getBreakStartTime() + ", isOverTimeApplicable()=" + isOverTimeApplicable()
				+ ", getName()=" + getName() + ", getNoOfHours()=" + getNoOfHours() + ", isOnFriday()=" + isOnFriday()
				+ ", isOnMonday()=" + isOnMonday() + ", isOnSaturday()=" + isOnSaturday() + ", isOnSunday()="
				+ isOnSunday() + ", isOnThursday()=" + isOnThursday() + ", isOnTuesday()=" + isOnTuesday()
				+ ", isOnWednesday()=" + isOnWednesday() + ", getShiftFromTime()=" + getShiftFromTime()
				+ ", getShiftToTime()=" + getShiftToTime() + "]";
	}
}
