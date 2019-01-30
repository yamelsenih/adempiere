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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackIn;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.X_AD_Column;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.model.X_AD_Table;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class TableElementHandler extends AbstractElementHandler {
	private ColumnElementHandler columnHandler = new ColumnElementHandler();
	
	private List<Integer>tables = new ArrayList<Integer>();
	
	public void startElement(Properties ctx, Element element) throws SAXException
	{
		final PackIn packIn = (PackIn)ctx.get("PackInProcess");
		final String elementValue = element.getElementValue();
		final Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Table.Table_Name);
		String tableUuid = uuid;
		log.info(elementValue + " " + uuid);
		final String entitytype = atts.getValue("EntityType");
		if (isProcessElement(ctx, entitytype)) {
			int id = packIn.getTableUUID(uuid);
			if (id <= 0) {
				id = getIdWithFromUUID(ctx, I_AD_Table.Table_Name, uuid);
				if (id > 0)
					packIn.addTable(uuid, id);
			}
			if (id > 0 && isTableProcess(ctx, id) && element.pass == 1) {
				return;
			}
			
			MTable table = new MTable(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_Table.COLUMNNAME_AD_Table_ID) > 0 && getIntValue(atts, I_AD_Table.COLUMNNAME_AD_Table_ID) <= PackOut.MAX_OFFICIAL_ID) {
				table.setAD_Table_ID(getIntValue(atts, I_AD_Table.COLUMNNAME_AD_Table_ID));
				table.setIsDirectLoad(true);
			}
			int backupId = -1;
			String Object_Status = null;
			if (id > 0) {		
				backupId = copyRecord(ctx, "AD_Table",table);
				Object_Status = "Update";			
			} else {
				Object_Status = "New";
				backupId =0;
			}
			table.setUUID(uuid);
			// Window
			uuid = getUUIDValue(atts, I_AD_Table.COLUMNNAME_AD_Window_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Window.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				table.setAD_Window_ID(id);
			}
			// PO Window
			uuid = getUUIDValue(atts, I_AD_Table.COLUMNNAME_PO_Window_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Window.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				table.setPO_Window_ID(id);
			}
			//
			table.setTableName(getStringValue(atts, I_AD_Table.COLUMNNAME_TableName));
			table.setName(getStringValue(atts, I_AD_Table.COLUMNNAME_Name));
			table.setDescription(getStringValue(atts, I_AD_Table.COLUMNNAME_Description));
			table.setHelp(getStringValue(atts, I_AD_Table.COLUMNNAME_Help));
			table.setAccessLevel(getStringValue(atts, I_AD_Table.COLUMNNAME_AccessLevel));
			table.setEntityType(getStringValue(atts, I_AD_Table.COLUMNNAME_EntityType));
			table.setImportTable(getStringValue(atts, I_AD_Table.COLUMNNAME_ImportTable));
			table.setIsChangeLog(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsChangeLog));
			table.setIsDocument(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsDocument));
			table.setIsView(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsView));
			table.setIsActive(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsActive));
			table.setIsCentrallyMaintained(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsCentrallyMaintained));
			table.setIsIgnoreMigration(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsIgnoreMigration));
			table.setIsHighVolume(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsHighVolume));
			table.setIsDeleteable(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsDeleteable));
			table.setIsSecurityEnabled(getBooleanValue(atts, I_AD_Table.COLUMNNAME_IsSecurityEnabled));
			table.setLoadSeq(getIntValue(atts, I_AD_Table.COLUMNNAME_LoadSeq));
			table.setReplicationType(getStringValue(atts, I_AD_Table.COLUMNNAME_ReplicationType));
			//	Save
			try {
				table.save(getTrxName(ctx));
				record_log (ctx, 1, table.getName(),"Table", table.get_ID(),backupId, Object_Status,"AD_Table",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Table"));
				tables.add(table.getAD_Table_ID());
				packIn.addTable(tableUuid, table.getAD_Table_ID());
				element.recordId = table.getAD_Table_ID();
			} catch (Exception e) {
				record_log (ctx, 0, table.getName(),"Table", table.get_ID(),backupId, Object_Status,"AD_Table",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Table"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int tableId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_Table_ID);
		PackOut packOut = (PackOut)ctx.get("PackOutProcess");
		boolean exported = isTableProcess(ctx, tableId);
		//Export table if not already done so
		if (!exported) {
			AttributesImpl atts = new AttributesImpl();
			MTable table = MTable.get(ctx, tableId);
			createTableBinding(atts, table);
			for(MColumn colunm : table.getColumns(true)) {
				packOut.createAdElement(colunm.getAD_Element_ID(), document);
				if (colunm.getAD_Reference_ID() > 0) {
					packOut.createReference (colunm.getAD_Reference_ID(), document);
				}
				if (colunm.getAD_Reference_Value_ID() > 0) {
					packOut.createReference (colunm.getAD_Reference_Value_ID(), document);
				}						
				if (colunm.getAD_Process_ID() > 0) {
					packOut.createProcess (colunm.getAD_Process_ID(), document);
				}	
				if (colunm.getAD_Val_Rule_ID() > 0) {
					packOut.createDynamicRuleValidation(colunm.getAD_Val_Rule_ID(), document);
				}
				createColumn(ctx, document, colunm.getAD_Column_ID());
			}
			document.endElement("","","table");
		}
		
	}

	private void createColumn(Properties ctx, TransformerHandler document, int columnId) throws SAXException {
		Env.setContext(ctx, X_AD_Column.COLUMNNAME_AD_Column_ID, columnId);
		columnHandler.create(ctx, document);
		ctx.remove(X_AD_Column.COLUMNNAME_AD_Column_ID);
	}

	private boolean isTableProcess(Properties ctx, int tableId) {
		if (tables.contains(tableId))
			return true;
		else {
			tables.add(tableId);
			return false;
		}
	}

	private AttributesImpl createTableBinding( AttributesImpl atts, X_AD_Table table) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, table);
		if (table.getAD_Table_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Table.COLUMNNAME_AD_Table_ID);
		}
		filler.addUUID();
		//	Window
		if (table.getAD_Window_ID() > 0) {
			filler.add(I_AD_Table.COLUMNNAME_AD_Window_ID, true);
			filler.addUUID(I_AD_Table.COLUMNNAME_AD_Window_ID, getUUIDFromId(table.getCtx(), I_AD_Window.Table_Name, table.getAD_Window_ID()));
		}
		//	PO Window
		if (table.getPO_Window_ID() > 0) {
			filler.add(I_AD_Table.COLUMNNAME_PO_Window_ID, true);
			filler.addUUID(I_AD_Table.COLUMNNAME_PO_Window_ID, getUUIDFromId(table.getCtx(), I_AD_Window.Table_Name, table.getPO_Window_ID()));
		}
		//	Attributes
		filler.add(I_AD_Table.COLUMNNAME_TableName);
		filler.add(I_AD_Table.COLUMNNAME_Name);
		filler.add(I_AD_Table.COLUMNNAME_Description);
		filler.add(I_AD_Table.COLUMNNAME_Help);
		filler.add(I_AD_Table.COLUMNNAME_AccessLevel);
		filler.add(I_AD_Table.COLUMNNAME_EntityType);
		filler.add(I_AD_Table.COLUMNNAME_ImportTable);
		filler.add(I_AD_Table.COLUMNNAME_IsChangeLog);
		filler.add(I_AD_Table.COLUMNNAME_IsDocument);
		filler.add(I_AD_Table.COLUMNNAME_IsView);
		filler.add(I_AD_Table.COLUMNNAME_IsActive);
		filler.add(I_AD_Table.COLUMNNAME_IsCentrallyMaintained);
		filler.add(I_AD_Table.COLUMNNAME_IsIgnoreMigration);
		filler.add(I_AD_Table.COLUMNNAME_IsHighVolume);
		filler.add(I_AD_Table.COLUMNNAME_IsDeleteable);
		filler.add(I_AD_Table.COLUMNNAME_IsSecurityEnabled);
		filler.add(I_AD_Table.COLUMNNAME_LoadSeq);
		filler.add(I_AD_Table.COLUMNNAME_ReplicationType);
		//	
		return atts;
	}
}
