/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
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
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import org.compiere.util.CCache;
import org.compiere.util.ProjectTypeWrapper;

/**
 * 	Project Type Model
 *
 *	@author Jorg Janke
 *	@version $Id: MProjectType.java,v 1.3 2006/07/30 00:51:03 jjanke Exp $
 */
public class MProjectType extends X_C_ProjectType
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6041540981032251476L;


	/**
	 * 	Get MProjectType from Cache
	 *	@param ctx context
	 *	@param C_ProjectType_ID id
	 *	@return MProjectType
	 */
	public static MProjectType get (Properties ctx, int C_ProjectType_ID)
	{
		Integer key = new Integer (C_ProjectType_ID);
		MProjectType retValue = (MProjectType)s_cache.get (key);
		if (retValue != null)
			return retValue;
		retValue = new MProjectType (ctx, C_ProjectType_ID, null);
		if (retValue.get_ID() != 0)
			s_cache.put (key, retValue);
		return retValue;
	} //	get

	/**	Cache						*/
	private static CCache<Integer, MProjectType> s_cache = new CCache<Integer, MProjectType> ("C_ProjectType", 20);
	
	
	/**************************************************************************
	 * 	Standrad Constructor
	 *	@param ctx context
	 *	@param C_ProjectType_ID id
	 *	@param trxName trx
	 */
	public MProjectType (Properties ctx, int C_ProjectType_ID, String trxName)
	{
		super (ctx, C_ProjectType_ID, trxName);
		/**
		if (C_ProjectType_ID == 0)
		{
			setC_ProjectType_ID (0);
			setName (null);
		}
		**/
	}	//	MProjectType

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName trx
	 */
	public MProjectType (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MProjectType

	/**
	 * 	String Representation
	 *	@return	info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer ("MProjectType[")
			.append(get_ID())
			.append("-").append(getName())
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * Get Project Type Phases
	 * @return list of Phases
	 */
	public List<MProjectTypePhase> getPhases(){
		return new Query(getCtx(), MProjectTypePhase.Table_Name , MProjectTypePhase.COLUMNNAME_C_ProjectType_ID + "=?", get_TrxName())
				.setClient_ID()
				.setParameters(getC_ProjectType_ID())
				.setOrderBy(MProjectTypePhase.COLUMNNAME_SeqNo)
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
		String MeasureScope, String MeasureDataType, Timestamp reportDate, MRole role) {
		return ProjectTypeWrapper.newInstance(this).getSqlPI(restrictions, MeasureScope, MeasureDataType, reportDate, role);
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
		Timestamp startDate, MRole role) {
		return ProjectTypeWrapper.newInstance(this).getSqlBarChart(restrictions, MeasureDisplay, MeasureDataType, startDate, role);
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
		String MeasureDisplay, Timestamp date, int C_Phase_ID, MRole role) {
		return ProjectTypeWrapper.newInstance(this).getQuery(restrictions, MeasureDisplay, date, C_Phase_ID, role);
	}	//	getQuery

}	//	MProjectType
