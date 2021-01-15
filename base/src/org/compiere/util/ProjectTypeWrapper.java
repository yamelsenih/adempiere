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
 * Copyright (C) 2003-2015 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package org.compiere.util;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import org.compiere.model.I_C_Phase;
import org.compiere.model.MGoal;
import org.compiere.model.MGoalRestriction;
import org.compiere.model.MMeasure;
import org.compiere.model.MMeasureCalc;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.Query;
import org.compiere.model.X_C_Phase;
import org.compiere.model.X_C_ProjectType;

/**
 * Project Wrapper class used because the project have wrong links with base source folder
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class ProjectTypeWrapper {
	
	/**
	 * Static instance
	 * @param projectType
	 * @return
	 */
	public static ProjectTypeWrapper newInstance(X_C_ProjectType projectType) {
		return new ProjectTypeWrapper(projectType);
	}
	
	/**	Cache						*/
	private static CCache<Integer, X_C_ProjectType> s_cache = new CCache<Integer, X_C_ProjectType> ("C_ProjectType", 20);
	
	/**
	 * 	Get MProjectType from Cache
	 *	@param ctx context
	 *	@param projectTypeId id
	 *	@return MProjectType
	 */
	public static X_C_ProjectType getFromId(Properties ctx, int projectTypeId, String transactionName) {
		Integer key = new Integer (projectTypeId);
		X_C_ProjectType retValue = (X_C_ProjectType)s_cache.get (key);
		if (retValue != null)
			return retValue;
		retValue = new X_C_ProjectType (ctx, projectTypeId, transactionName);
		if (retValue.get_ID() != 0)
			s_cache.put (key, retValue);
		return retValue;
	} //	get
	
	/***
	 * Private constructor
	 * @param projectType
	 */
	private ProjectTypeWrapper(X_C_ProjectType projectType) {
		this.projectType = projectType;
	}
	
	private X_C_ProjectType projectType;
	/**	Logger							*/
	protected CLogger log = CLogger.getCLogger (getClass());
	/**
	 * @return the project
	 */
	public final X_C_ProjectType getProjectType() {
		return projectType;
	}
	
	/**
	 * Get Project Type Phases
	 * @return list of Phases
	 */
	public List<X_C_Phase> getPhases(){
		return new Query(projectType.getCtx(), I_C_Phase.Table_Name , I_C_Phase.COLUMNNAME_C_ProjectType_ID + "=?", projectType.get_TrxName())
				.setClient_ID()
				.setParameters(projectType.getC_ProjectType_ID())
				.setOrderBy(I_C_Phase.COLUMNNAME_SeqNo)
				.list();

	}

	/**
	 * 	Get Sql to return single value for the Performance Indicator
	 *	@param restrictions array of goal restrictions
	 *	@param MeasureScope scope of this value  
	 *	@param MeasureDataType data type
	 *	@param reportDate optional report date
	 *	@param role role
	 *	@return sql for performance indicator
	 */
	public String getSqlPI (MGoalRestriction[] restrictions, 
		String MeasureScope, String MeasureDataType, Timestamp reportDate, MRole role)
	{
		String dateColumn = "Created";
		String orgColumn = "AD_Org_ID";
		String bpColumn = "C_BPartner_ID";
		String pColumn = null;
		//	PlannedAmt -> PlannedQty -> Count
		StringBuffer sb = new StringBuffer("SELECT COALESCE(SUM(PlannedAmt),COALESCE(SUM(PlannedQty),COUNT(*))) "
			+ "FROM C_Project WHERE C_ProjectType_ID=" + projectType.getC_ProjectType_ID()
			+ " AND Processed<>'Y')");
		//	Date Restriction
		
		if (MMeasure.MEASUREDATATYPE_QtyAmountInTime.equals(MeasureDataType)
			&& !MGoal.MEASUREDISPLAY_Total.equals(MeasureScope))
		{
			if (reportDate == null)
				reportDate = new Timestamp(System.currentTimeMillis());
			String trunc = "D";
			if (MGoal.MEASUREDISPLAY_Year.equals(MeasureScope))
				trunc = "Y";
			else if (MGoal.MEASUREDISPLAY_Quarter.equals(MeasureScope))
				trunc = "Q";
			else if (MGoal.MEASUREDISPLAY_Month.equals(MeasureScope))
				trunc = "MM";
			else if (MGoal.MEASUREDISPLAY_Week.equals(MeasureScope))
				trunc = "W";
		//	else if (MGoal.MEASUREDISPLAY_Day.equals(MeasureDisplay))
		//		;
			sb.append(" AND TRUNC(")
				.append(dateColumn).append(",'").append(trunc).append("')=TRUNC(")
				.append(DB.TO_DATE(reportDate)).append(",'").append(trunc).append("')");
		}	//	date
		//
		String sql = MMeasureCalc.addRestrictions(sb.toString(), false, restrictions, role, 
			"C_Project", orgColumn, bpColumn, pColumn);
		
		log.fine(sql);
		return sql;
	}	//	getSql
	
	/**
	 * 	Get Sql to value for the bar chart
	 *	@param restrictions array of goal restrictions
	 *	@param MeasureDisplay scope of this value  
	 *	@param MeasureDataType data type
	 *	@param startDate optional report start date
	 *	@param role role
	 *	@return sql for Bar Chart
	 */
	public String getSqlBarChart (MGoalRestriction[] restrictions, 
		String MeasureDisplay, String MeasureDataType, 
		Timestamp startDate, MRole role)
	{
		String dateColumn = "Created";
		String orgColumn = "AD_Org_ID";
		String bpColumn = "C_BPartner_ID";
		String pColumn = null;
		//
		StringBuffer sb = new StringBuffer("SELECT COALESCE(SUM(PlannedAmt),COALESCE(SUM(PlannedQty),COUNT(*))), ");
		String orderBy = null;
		String groupBy = null;
		//
		if (MMeasure.MEASUREDATATYPE_QtyAmountInTime.equals(MeasureDataType)
			&& !MGoal.MEASUREDISPLAY_Total.equals(MeasureDisplay))
		{
			String trunc = "D";
			if (MGoal.MEASUREDISPLAY_Year.equals(MeasureDisplay))
				trunc = "Y";
			else if (MGoal.MEASUREDISPLAY_Quarter.equals(MeasureDisplay))
				trunc = "Q";
			else if (MGoal.MEASUREDISPLAY_Month.equals(MeasureDisplay))
				trunc = "MM";
			else if (MGoal.MEASUREDISPLAY_Week.equals(MeasureDisplay))
				trunc = "W";
		//	else if (MGoal.MEASUREDISPLAY_Day.equals(MeasureDisplay))
		//		;
			orderBy = "TRUNC(" + dateColumn + ",'" + trunc + "')";
			groupBy = orderBy + ", 0 ";
			sb.append(groupBy)
				.append("FROM C_Project ");
		}
		else
		{
			orderBy = "p.SeqNo"; 
			groupBy = "COALESCE(p.Name,TO_NCHAR('-')), p.C_Phase_ID, p.SeqNo ";
			sb.append(groupBy)
				.append("FROM C_Project LEFT OUTER JOIN C_Phase p ON (C_Project.C_Phase_ID=p.C_Phase_ID) ");
		}
		//	Where
		sb.append("WHERE C_Project.C_ProjectType_ID=").append(projectType.getC_ProjectType_ID())
			.append(" AND C_Project.Processed<>'Y'");
		//	Date Restriction
		if (startDate != null
			&& !MGoal.MEASUREDISPLAY_Total.equals(MeasureDisplay))
		{
			String dateString = DB.TO_DATE(startDate);
			sb.append(" AND ").append(dateColumn)
				.append(">=").append(dateString);
		}	//	date
		//
		String sql = MMeasureCalc.addRestrictions(sb.toString(), false, restrictions, role, 
			"C_Project", orgColumn, bpColumn, pColumn);
		if (groupBy != null)
			sql += " GROUP BY " + groupBy + " ORDER BY " + orderBy;
		//
		log.fine(sql);
		return sql;
	}	//	getSqlBarChart
	
	/**
	 * 	Get Zoom Query
	 * 	@param restrictions restrictions
	 * 	@param MeasureDisplay display
	 * 	@param date date
	 * 	@param C_Phase_ID phase
	 * 	@param role role 
	 *	@return query
	 */
	public MQuery getQuery(MGoalRestriction[] restrictions, 
		String MeasureDisplay, Timestamp date, int C_Phase_ID, MRole role)
	{
		String dateColumn = "Created";
		String orgColumn = "AD_Org_ID";
		String bpColumn = "C_BPartner_ID";
		String pColumn = null;
		//
		MQuery query = new MQuery("C_Project");
		query.addRangeRestriction("C_ProjectType_ID", "=", projectType.getC_ProjectType_ID());
		//
		String where = null;
		if (C_Phase_ID != 0)
			where = "C_Phase_ID=" + C_Phase_ID;
		else
		{
			String trunc = "D";
			if (MGoal.MEASUREDISPLAY_Year.equals(MeasureDisplay))
				trunc = "Y";
			else if (MGoal.MEASUREDISPLAY_Quarter.equals(MeasureDisplay))
				trunc = "Q";
			else if (MGoal.MEASUREDISPLAY_Month.equals(MeasureDisplay))
				trunc = "MM";
			else if (MGoal.MEASUREDISPLAY_Week.equals(MeasureDisplay))
				trunc = "W";
		//	else if (MGoal.MEASUREDISPLAY_Day.equals(MeasureDisplay))
		//		trunc = "D";
			where = "TRUNC(" + dateColumn + ",'" + trunc
				+ "')=TRUNC(" + DB.TO_DATE(date) + ",'" + trunc + "')";
		}
		String sql = MMeasureCalc.addRestrictions(where + " AND Processed<>'Y' ",
			true, restrictions, role, 
			"C_Project", orgColumn, bpColumn, pColumn);
		query.addRestriction(sql);
		query.setRecordCount(1);
		return query;
	}	//	getQuery
}
