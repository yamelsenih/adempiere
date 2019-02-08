/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com
 * Contributor(s): Low Heng Sin hengsin@avantz.com
 *                 Teo Sarca, teo.sarca@gmail.com
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.PackOut;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Table;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.X_AD_Column;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.SAXException;

public class TableElementHandler extends GenericPOHandler {
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int tableId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_Table_ID);
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		//	Task
		packOut.createGenericPO(document, I_AD_Table.Table_ID, tableId, true, null);
		MTable table = MTable.get(ctx, tableId);
		for(MColumn colunm : table.getColumns(true)) {
			packOut.createGenericPO(document, I_AD_Column.Table_ID, colunm.getAD_Column_ID(), true, null);
		}
	}
	
	@Override
	protected void afterSave(PO entity) {
		if(X_AD_Column.class.isAssignableFrom(entity.getClass())) {
			MColumn column = (MColumn) entity;
			MTable table = new MTable(entity.getCtx(), column.getAD_Table_ID(), getTrxName(entity.getCtx()));
			if (!table.isView() && Util.isEmpty(column.getColumnSQL())) {
				column.syncDatabase();
			}
		}
	}
}
