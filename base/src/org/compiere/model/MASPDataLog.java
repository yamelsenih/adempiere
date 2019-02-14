/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
 * This program is free software; you can redistribute it and/or modify it    		 *
 * under the terms version 2 or later of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope   		 *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 		 *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           		 *
 * See the GNU General Public License for more details.                       		 *
 * You should have received a copy of the GNU General Public License along    		 *
 * with this program; if not, write to the Free Software Foundation, Inc.,    		 *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     		 *
 * For the text or an alternative of this public license, you may reach us    		 *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com				  		                 *
 *************************************************************************************/
package org.compiere.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ASP Data Log Model
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class MASPDataLog extends X_ASP_DataLog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7788247772367181508L;
	
	public MASPDataLog(Properties ctx, int aspDataLogId, String trxName) {
		super(ctx, aspDataLogId, trxName);
	}

	public MASPDataLog(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	/**
	 * Get or create log from table
	 * @param ctx
	 * @param tableId
	 * @param recordId
	 * @param aspModuleId
	 * @param aspLevelId
	 * @param trxName
	 * @return
	 */
	public static MASPDataLog getOrCreate(Properties ctx, int tableId, int recordId, int aspModuleId, int aspLevelId, String trxName) {
		List<Object> parameters = new ArrayList<>();
		StringBuffer whereClause = new StringBuffer();
		//	Add where clause
		whereClause.append(COLUMNNAME_AD_Table_ID + " = ?");
		parameters.add(tableId);
		whereClause.append(" AND ").append(COLUMNNAME_Record_ID + " = ?");
		parameters.add(recordId);
		//	Module
		whereClause.append(" AND ").append(COLUMNNAME_ASP_Module_ID + " = ?");
		parameters.add(aspModuleId);
		if(aspLevelId > 0) {
			whereClause.append(" AND ").append(COLUMNNAME_ASP_Level_ID + " = ?");
			parameters.add(aspLevelId);
		}
		//	Get
		MASPDataLog log = new Query(ctx, Table_Name, whereClause.toString(), trxName)
				.setParameters(parameters)
				.first();
		//	Validate null
		if(log == null) {
			log = new MASPDataLog(ctx, 0, trxName);
			log.setAD_Table_ID(tableId);
			log.setRecord_ID(recordId);
			log.setASP_Module_ID(aspModuleId);
			if(aspLevelId > 0) {
				log.setASP_Level_ID(aspLevelId);
			}
		}
		//	Default return
		return log;
	}

}
