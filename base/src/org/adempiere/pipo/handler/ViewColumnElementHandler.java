/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2003-2012 e-Evolution Consultants. All Rights Reserved.      *
 * Copyright (C) 2003-2012 Victor Pérez Juárez 								  * 
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
 * Contributor(s): Victor Pérez Juárez  (victor.perez@e-evolution.com)		  *
 * Sponsors: e-Evolution Consultants (http://www.e-evolution.com/)            *
 *****************************************************************************/

package org.adempiere.pipo.handler;

import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_View;
import org.adempiere.model.I_AD_View_Column;
import org.adempiere.model.I_AD_View_Definition;
import org.adempiere.model.MViewColumn;
import org.adempiere.model.X_AD_View_Column;
import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Table;
import org.compiere.model.MTable;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * @author victor.perez@e-evoluton.com, www.e-evolution.com
 * 
 */
public class ViewColumnElementHandler extends AbstractElementHandler {
	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_View_Column.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_View.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null
					&& element.parent.getElementValue()
							.equals("viewdefinition") && element.parent.defer) {
				element.defer = true;
				return;
			}
			String tableUuid = getUUIDValue(atts, I_AD_Table.COLUMNNAME_AD_Table_ID);
			int tableId = getIdFromUUID(ctx, I_AD_Table.Table_Name, tableUuid);
			if (tableId <= 0  && !Util.isEmpty(tableUuid)) {
				element.defer = true;
				return;
			}
			String viewUuid = getUUIDValue(atts, I_AD_View.COLUMNNAME_AD_View_ID);
			int viewId = getIdFromUUID(ctx, I_AD_View.Table_Name, viewUuid);
			if (viewId <= 0) {
				element.defer = true;
				return;
			}
			String columnUuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Column_ID);
			int columnId = getIdFromUUID(ctx, I_AD_Column.Table_Name, columnUuid);
			if (columnId <= 0 && !Util.isEmpty(columnUuid)) {
				element.defer = true;
				return;
			}
			int viewdefinitionid = 0;
			if (element.parent != null
					&& element.parent.getElementValue()
							.equals("viewdefinition")
					&& element.parent.recordId > 0) {
				viewdefinitionid = element.parent.recordId;
			} else {
				String viewDefinitionUuid = getUUIDValue(atts, I_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID);
				viewdefinitionid = getIdFromUUID(ctx, I_AD_View_Definition.Table_Name, viewDefinitionUuid);
				if (element.parent != null
						&& element.parent.getElementValue().equals(
								"viewdefinition") && viewdefinitionid > 0) {
					element.parent.recordId = viewdefinitionid;
				}
			}
			if (viewdefinitionid > 0) {
				int id = getIdFromUUID(ctx, I_AD_View_Column.Table_Name, uuid);
				X_AD_View_Column viewColumn = new X_AD_View_Column(ctx, id, getTrxName(ctx));
				if (id <= 0 && getIntValue(atts, I_AD_View_Column.COLUMNNAME_AD_View_Column_ID) > 0 && getIntValue(atts, I_AD_View_Column.COLUMNNAME_AD_View_Column_ID) <= PackOut.MAX_OFFICIAL_ID) {
					viewColumn.setAD_View_Column_ID(getIntValue(atts, I_AD_View_Column.COLUMNNAME_AD_View_Column_ID));
					viewColumn.setIsDirectLoad(true);
				}
				int backupId = -1;
				String objectStatus = null;
				if (id > 0) {
					backupId = copyRecord(ctx, "AD_View_Column_ID",
							viewColumn);
					objectStatus = "Update";
				} else {
					objectStatus = "New";
					backupId = 0;
				}
				viewColumn.setUUID(uuid);
				//	Column
				uuid = getUUIDValue(atts, I_AD_View_Column.COLUMNNAME_AD_Column_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					viewColumn.setAD_Column_ID(id);
				}
				//	View Definition
				uuid = getUUIDValue(atts, I_AD_View_Column.COLUMNNAME_AD_View_Definition_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_View_Definition.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					viewColumn.setAD_View_Definition_ID(id);
				}
				//	Attribute
				viewColumn.setColumnName(getStringValue(atts, I_AD_View_Column.COLUMNNAME_ColumnName));
				viewColumn.setName(getStringValue(atts, I_AD_View_Column.COLUMNNAME_Name));
				viewColumn.setDescription(getStringValue(atts, I_AD_View_Column.COLUMNNAME_Description));
				viewColumn.setHelp(getStringValue(atts, I_AD_View_Column.COLUMNNAME_Help));
				viewColumn.setColumnSQL(getStringValue(atts, I_AD_View_Column.COLUMNNAME_ColumnSQL));
				viewColumn.setEntityType(getStringValue(atts, I_AD_View_Column.COLUMNNAME_EntityType));
				viewColumn.setIsActive(getBooleanValue(atts, I_AD_View_Column.COLUMNNAME_IsActive));
				//	Save
				try {
					viewColumn.saveEx(getTrxName(ctx));
					recordLog(
							ctx,
							1,
							viewColumn.getUUID(),
							"ViewColumn",
							viewColumn.get_ID(),
							backupId,
							objectStatus,
							"ViewColumn",
							get_IDWithColumn(ctx, "AD_Table", "TableName",
									"AD_View_Column"));
					element.recordId = viewColumn.getAD_View_Column_ID();
				} catch (Exception e) {
					recordLog(
							ctx,
							0,
							viewColumn.getUUID(),
							"ViewColumn",
							viewColumn.get_ID(),
							backupId,
							objectStatus,
							"AD_View_Column",
							get_IDWithColumn(ctx, "AD_Table", "TableName",
									"AD_View_Column"));
					throw new POSaveFailedException(e);
				}
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int viewColumnId = Env.getContextAsInt(ctx, X_AD_View_Column.COLUMNNAME_AD_View_Column_ID);
		MViewColumn viewColumn = new MViewColumn(ctx, viewColumnId, null);
		AttributesImpl atts = new AttributesImpl();
		createViewColumnBinding(atts, viewColumn);
		document.startElement("", "", "viewcolumn", atts);
		document.endElement("", "", "viewcolumn");
	}

	private AttributesImpl createViewColumnBinding(AttributesImpl atts, X_AD_View_Column viewColumn) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, viewColumn);
		if (viewColumn.getAD_View_Column_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_View_Column.COLUMNNAME_AD_View_Column_ID);
		}
		filler.addUUID();
		//	View Definition reference
		if(viewColumn.getAD_View_Definition_ID() > 0) {
			filler.add(I_AD_View_Column.COLUMNNAME_AD_View_Definition_ID, true);
			filler.addUUID(I_AD_View_Column.COLUMNNAME_AD_View_Definition_ID, getUUIDFromId(viewColumn.getCtx(), I_AD_View_Definition.Table_Name, viewColumn.getAD_View_Definition_ID()));
			//	Get View
			filler.addUUID(I_AD_View.COLUMNNAME_AD_View_ID, viewColumn.getAD_View_Definition().getAD_View().getUUID());
		}
		//	Column Reference
		if (viewColumn.getAD_Column_ID() > 0) {
			filler.add(I_AD_View_Column.COLUMNNAME_AD_Column_ID, true);
			filler.addUUID(I_AD_View_Column.COLUMNNAME_AD_Column_ID, getUUIDFromId(viewColumn.getCtx(), I_AD_Column.Table_Name, viewColumn.getAD_Column_ID()));
			//	For table
			MTable table = MTable.get(Env.getCtx(), viewColumn.getAD_Column().getAD_Table_ID());
			filler.addUUID(I_AD_Table.COLUMNNAME_AD_Table_ID, table.getUUID());
			//	Add Entity Type
			filler.addString(I_AD_View.COLUMNNAME_EntityType, viewColumn.getAD_View_Definition().getAD_View().getEntityType());
		}
		//	Standard Attributes
		filler.add(I_AD_View_Column.COLUMNNAME_ColumnName);
		filler.add(I_AD_View_Column.COLUMNNAME_Name);
		filler.add(I_AD_View_Column.COLUMNNAME_Description);
		filler.add(I_AD_View_Column.COLUMNNAME_Help);
		filler.add(I_AD_View_Column.COLUMNNAME_ColumnSQL);
		filler.add(I_AD_View_Column.COLUMNNAME_EntityType);
		filler.add(I_AD_View_Column.COLUMNNAME_IsActive);
		return atts;
	}
}
