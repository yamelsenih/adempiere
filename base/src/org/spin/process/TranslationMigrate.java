/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2016 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
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

package org.spin.process;

import java.util.List;

import org.compiere.model.I_AD_Table;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;

/** Generated Process for (Translation Migrate)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class TranslationMigrate extends TranslationMigrateAbstract {

	@Override
	protected String doIt() throws Exception {
		StringBuffer whereClause = new StringBuffer("EXISTS(SELECT 1 FROM AD_Table t WHERE t.TableName = AD_Table.TableName || '_Trl')");
		if(getTableId() != 0) {
			whereClause.append(" AND AD_Table.AD_Table_ID = ").append(getTableId());
		}
		List<MTable> tables = new Query(getCtx(), 
				I_AD_Table.Table_Name, whereClause.toString(), get_TrxName())
		.list();
		//	Iterate It
		for(MTable table : tables) {
			for(MColumn column : table.getColumns(false)) {
				//	Ignore
				if(!column.isTranslated()) {
					continue;
				}
				//	
				migrate(table, column);
			}
		}
		return "";
	}
	
	/**
	 * Create
	 * @param table
	 * @param column
	 */
	private void migrate(MTable table, MColumn column) {
		int userId = Env.getAD_User_ID(getCtx());
		String tablename = table.getTableName();
		String recordId = tablename + "_ID";
		String insert = "INSERT INTO AD_Translation(AD_Translation_ID, AD_Client_ID, AD_Org_ID, "
				+ "IsActive, AD_Table_ID, "
				+ "AD_Column_ID, Record_ID, AD_Language, Value, Created, Updated, Createdby, UpdatedBy) "
				//	Select
				+ "SELECT nextID(CAST((SELECT AD_Sequence_ID FROM AD_Sequence WHERE Name = 'AD_Translation') AS Integer), 'Y'), "
				+ "AD_Client_ID, AD_Org_ID, "
				+ "'Y', " + table.getAD_Table_ID() + ", " + recordId + ", "
				+ column.getAD_Column_ID() + ", " 
				+ "AD_Language, " 
				+ column.getColumnName() + ", SYSDATE, SYSDATE, " + userId + "," + userId + " "
				+ "FROM " + tablename + "_Trl "
				+ "WHERE " + column.getColumnName() + " IS NOT NULL";
		int no = DB.executeUpdateEx(insert, null, get_TrxName());
		log.fine(tablename + "_Trl" + " #" + no);
		addLog("@AD_Table_ID@ " + table.getTableName() + "." + column.getColumnName() + " (" + no + ")");
	}
}