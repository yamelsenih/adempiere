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

import java.text.DecimalFormat;
import java.util.List;

import org.compiere.model.I_AD_Table;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

/** Generated Process for (Translation Migrate)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class TranslationMigrate extends TranslationMigrateAbstract {
	/**	Client Check	*/
	private String clientCheck = null;
	@Override
	protected String doIt() throws Exception {
		clientCheck = " AND AD_Client_ID = " + getAD_Client_ID();
		StringBuffer whereClause = new StringBuffer("EXISTS(SELECT 1 FROM AD_Table t WHERE t.TableName = AD_Table.TableName || '_Trl')");
		if(getTableId() != 0) {
			whereClause.append(" AND AD_Table.AD_Table_ID = ").append(getTableId());
		}
		List<MTable> tables = new Query(getCtx(), 
				I_AD_Table.Table_Name, whereClause.toString(), get_TrxName())
		.list();
		int recordNo = 0;
		DecimalFormat format = DisplayType.getNumberFormat(DisplayType.Quantity);
		//	Iterate It
		for(MTable table : tables) {
			log.fine(table.getTableName() + "_Trl");
			StringBuffer msg = new StringBuffer("@AD_Table_ID@ " + table.getTableName() + "_Trl [");
			boolean isFirst = true;
			for(MColumn column : table.getColumns(false)) {
				//	Ignore
				if(!column.isTranslated()) {
					continue;
				}
				//	
				if(!isFirst) {
					msg.append(", ");
				} else {
					isFirst = false;
				}
				int no = migrate(table, column);
				//	Add column
				msg.append(column.getColumnName())
					.append("(")
					.append(format.format(no))
					.append(")");
				//	Add to total
				recordNo += no;
			}
			msg.append("]");
			addLog(msg.toString());
		}
		return "@AD_Translation_ID@(" + format.format(recordNo) + ")";
	}
	
	/**
	 * Create
	 * @param table
	 * @param column
	 * @return integer
	 */
	private int migrate(MTable table, MColumn column) {
		int userId = Env.getAD_User_ID(getCtx());
		String tablename = table.getTableName();
		String tablenameTrl = tablename + "_Trl";
		String keyColumn = tablename + "_ID";
		String translationColumn = column.getColumnName();
		String recordId = tablename + "_ID";
		//	Delete previous records
		int deleted = DB.executeUpdateEx("DELETE FROM AD_Translation WHERE AD_Table_ID = ?" + clientCheck, 
				new Object[]{table.getAD_Table_ID()}, get_TrxName());
		log.fine("Translation deleted = " + deleted);
		//	For insert
		String insert = "INSERT INTO AD_Translation(AD_Translation_ID, AD_Client_ID, AD_Org_ID, "
				+ "IsActive, AD_Table_ID, "
				+ "AD_Column_ID, Record_ID, AD_Language, Value, Created, Updated, Createdby, UpdatedBy) "
				//	Select
				+ "SELECT nextID(CAST((SELECT AD_Sequence_ID FROM AD_Sequence WHERE Name = 'AD_Translation') AS Integer), 'Y'), "
				+ "AD_Client_ID, AD_Org_ID, "
				+ "'Y', " + table.getAD_Table_ID() + ", "
				+ column.getAD_Column_ID() + ", " 
				+ recordId + ", "
				+ "AD_Language, " 
				+ translationColumn + ", SYSDATE, SYSDATE, " + userId + "," + userId + " "
				+ "FROM " + tablenameTrl + " "
				+ "WHERE " + column.getColumnName() + " IS NOT NULL "
				+ "AND EXISTS(SELECT 1 FROM " + tablename + " p "
						+ "WHERE p." + keyColumn + " = " + tablenameTrl + "." + keyColumn + " "
						+ "AND UPPER(TRIM(p." + translationColumn + ")) <> UPPER(TRIM(" + tablenameTrl + "." + translationColumn + ")))"
				+ clientCheck;
		return DB.executeUpdateEx(insert, null, get_TrxName());
	}
}