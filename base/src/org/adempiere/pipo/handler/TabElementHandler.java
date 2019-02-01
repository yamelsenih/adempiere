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

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Image;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_Tab;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MField;
import org.compiere.model.MTab;
import org.compiere.model.X_AD_Field;
import org.compiere.model.X_AD_Tab;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class TabElementHandler extends AbstractElementHandler
{
	private FieldElementHandler fieldHandler = new FieldElementHandler();
	
	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Tab.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Tab.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null && element.parent.getElementValue().equals("window")
				&& element.parent.defer) {
				element.defer = true;
				return;
			}
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Table_ID);
			int tableid = getIdFromUUID(ctx, I_AD_Table.Table_Name, uuid);
			if (tableid <= 0) {
				element.defer = true;
				return;
			}
			int windowId = 0;
			if (element.parent != null && element.parent.getElementValue().equals("window")
					&& element.parent.recordId > 0) {
				windowId = element.parent.recordId;
			} else {
				uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Window_ID);
				windowId = getIdFromUUID(ctx, I_AD_Window.Table_Name, uuid);
				if (element.parent != null && element.parent.getElementValue().equals("window")
						&& windowId > 0) {
					element.parent.recordId = windowId;
				}
			}
			if (windowId <= 0) {
				element.defer = true;
				return;
			}
			//	Instance tab
			uuid = atts.getValue(AttributeFiller.getUUIDAttribute(I_AD_Tab.Table_Name));
			int tabId = getIdFromUUID(ctx, I_AD_Tab.Table_Name, uuid);
			//	
			X_AD_Tab tab = new X_AD_Tab(ctx, tabId, getTrxName(ctx));
			if (tabId <= 0 && getIntValue(atts, I_AD_Tab.COLUMNNAME_AD_Tab_ID) > 0 && getIntValue(atts, I_AD_Tab.COLUMNNAME_AD_Tab_ID) <= PackOut.MAX_OFFICIAL_ID) {
				tab.setAD_Tab_ID(getIntValue(atts, I_AD_Tab.COLUMNNAME_AD_Tab_ID));
				tab.setIsDirectLoad(true);
			}
			int backupId = -1;
			String Object_Status = null;
			if (tabId > 0){			
				backupId = copyRecord(ctx, "AD_Tab",tab);
				Object_Status = "Update";
			}
			else{
				Object_Status = "New";
				backupId =0;
			}
			tab.setUUID(uuid);
			//	Column Sort
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_ColumnSortOrder_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_ColumnSortOrder_ID(id);
			}
			//	Yes/No Column
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_ColumnSortYesNo_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_ColumnSortYesNo_ID(id);
			}
			//	Column
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Column_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_Column_ID(id);
			}
			//	Parent Column
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_Parent_Column_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setParent_Column_ID(id);
			}
			//	Image
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Image_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Image.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_Image_ID(id);
			}
			//	Process
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Process_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Process.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_Process_ID(id);
			}
			//	Table
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Table_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Table.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_Table_ID(id);
			}
			//	Window
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_AD_Window_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Window.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setAD_Window_ID(id);
			}
			//	Included Tab
			uuid = getUUIDValue(atts, I_AD_Tab.COLUMNNAME_Included_Tab_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdFromUUID(ctx, I_AD_Tab.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				tab.setIncluded_Tab_ID(id);
			}
			//	Standard Attributes
			tab.setName(getStringValue(atts, I_AD_Tab.COLUMNNAME_Name));
			tab.setDescription(getStringValue(atts, I_AD_Tab.COLUMNNAME_Description));
			tab.setHelp(getStringValue(atts, I_AD_Tab.COLUMNNAME_Help));
			tab.setCommitWarning(getStringValue(atts, I_AD_Tab.COLUMNNAME_CommitWarning));
			tab.setEntityType(getStringValue(atts, I_AD_Tab.COLUMNNAME_EntityType));
			tab.setHasTree(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_HasTree));
			tab.setIsActive(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsActive));
			tab.setImportFields(getStringValue(atts, I_AD_Tab.COLUMNNAME_ImportFields));
			tab.setIsInfoTab(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsInfoTab));
			tab.setIsReadOnly(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsReadOnly));
			tab.setIsSingleRow(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsSingleRow));
			tab.setIsSortTab(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsSortTab));
			tab.setIsTranslationTab(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsTranslationTab));
			tab.setOrderByClause(getStringValue(atts, I_AD_Tab.COLUMNNAME_OrderByClause));
			tab.setProcessing(false);
			tab.setSeqNo(getIntValue(atts, I_AD_Tab.COLUMNNAME_SeqNo));
			tab.setTabLevel(getIntValue(atts, I_AD_Tab.COLUMNNAME_TabLevel));
			tab.setWhereClause(getStringValue(atts, I_AD_Tab.COLUMNNAME_WhereClause));
			tab.setReadOnlyLogic(getStringValue(atts, I_AD_Tab.COLUMNNAME_ReadOnlyLogic));
			tab.setDisplayLogic(getStringValue(atts, I_AD_Tab.COLUMNNAME_DisplayLogic));
			tab.setIsInsertRecord(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsInsertRecord));
			tab.setIsAdvancedTab(getBooleanValue(atts, I_AD_Tab.COLUMNNAME_IsAdvancedTab));
			//	Save
			try {
				tab.saveEx(getTrxName(ctx));
				recordLog (ctx, 1, tab.getUUID(),"Tab", tab.get_ID(),backupId, Object_Status,"AD_Tab",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Tab"));
				element.recordId = tab.getAD_Tab_ID();
			} catch (Exception e) {
				recordLog (ctx, 0, tab.getUUID(),"Tab", tab.get_ID(),backupId, Object_Status,"AD_Tab",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Tab"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}

	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		PackOut packOut = (PackOut)ctx.get("PackOutProcess");
		int tabId = Env.getContextAsInt(ctx, X_AD_Tab.COLUMNNAME_AD_Tab_ID);
		MTab tab = new MTab(ctx, tabId, getTrxName(ctx));
		AttributesImpl atts = new AttributesImpl();
		createTabBinding(atts, tab);
		document.startElement("","","tab",atts);
		for(MField field : tab.getFields(false, getTrxName(ctx))) {
			createField(ctx, document, field.getAD_Field_ID());
		}
		//						
		document.endElement("","","tab");
		//	Process
		if(tab.getAD_Process_ID() > 0 ) {
			packOut.createProcess(tab.getAD_Process_ID(), document);
		}
	}

	private void createField(Properties ctx, TransformerHandler document, int fieldId) throws SAXException {
		Env.setContext(ctx, X_AD_Field.COLUMNNAME_AD_Field_ID, fieldId);
		fieldHandler.create(ctx, document);
		ctx.remove(X_AD_Field.COLUMNNAME_AD_Field_ID);
	}
	
	private AttributesImpl createTabBinding( AttributesImpl atts, X_AD_Tab tab) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, tab);
		if (tab.getAD_Tab_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_Tab_ID);
		}
		filler.addUUID();
		//	Sort Column
		if(tab.getAD_ColumnSortOrder_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_ColumnSortOrder_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_ColumnSortOrder_ID, getUUIDFromId(tab.getCtx(), I_AD_Column.Table_Name, tab.getAD_ColumnSortOrder_ID()));
		}
		//	Yes/No Column
		if(tab.getAD_ColumnSortYesNo_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_ColumnSortYesNo_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_ColumnSortYesNo_ID, getUUIDFromId(tab.getCtx(), I_AD_Column.Table_Name, tab.getAD_ColumnSortYesNo_ID()));
		}
		//	Column
		if(tab.getAD_Column_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_Column_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_Column_ID, getUUIDFromId(tab.getCtx(), I_AD_Column.Table_Name, tab.getAD_Column_ID()));
		}
		//	Parent Column
		if(tab.getParent_Column_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_Parent_Column_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_Parent_Column_ID, getUUIDFromId(tab.getCtx(), I_AD_Column.Table_Name, tab.getParent_Column_ID()));
		}
		//	Image
		if(tab.getAD_Image_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_Image_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_Image_ID, getUUIDFromId(tab.getCtx(), I_AD_Image.Table_Name, tab.getAD_Image_ID()));
		}
		//	Process
		if(tab.getAD_Process_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_Process_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_Process_ID, getUUIDFromId(tab.getCtx(), I_AD_Process.Table_Name, tab.getAD_Process_ID()));
		}
		//	Table
		if(tab.getAD_Table_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_Table_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_Table_ID, getUUIDFromId(tab.getCtx(), I_AD_Table.Table_Name, tab.getAD_Table_ID()));
		}
		//	Window
		if(tab.getAD_Window_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_AD_Window_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_AD_Window_ID, getUUIDFromId(tab.getCtx(), I_AD_Window.Table_Name, tab.getAD_Window_ID()));
		}
		//	Tab Included
		if (tab.getIncluded_Tab_ID() > 0) {
			filler.add(I_AD_Tab.COLUMNNAME_Included_Tab_ID, true);
			filler.addUUID(I_AD_Tab.COLUMNNAME_Included_Tab_ID, getUUIDFromId(tab.getCtx(), I_AD_Tab.Table_Name, tab.getIncluded_Tab_ID()));
		}
		//	Attributes
		filler.add(I_AD_Tab.COLUMNNAME_Name);
		filler.add(I_AD_Tab.COLUMNNAME_Description);
		filler.add(I_AD_Tab.COLUMNNAME_Help);
		filler.add(I_AD_Tab.COLUMNNAME_CommitWarning);
		filler.add(I_AD_Tab.COLUMNNAME_EntityType);
		filler.add(I_AD_Tab.COLUMNNAME_HasTree);
		filler.add(I_AD_Tab.COLUMNNAME_IsInfoTab);
		filler.add(I_AD_Tab.COLUMNNAME_IsReadOnly);
		filler.add(I_AD_Tab.COLUMNNAME_IsSingleRow);
		filler.add(I_AD_Tab.COLUMNNAME_IsSortTab);
		filler.add(I_AD_Tab.COLUMNNAME_IsActive);
		filler.add(I_AD_Tab.COLUMNNAME_IsTranslationTab);
		filler.add(I_AD_Tab.COLUMNNAME_Processing);
		filler.add(I_AD_Tab.COLUMNNAME_OrderByClause);
		filler.add(I_AD_Tab.COLUMNNAME_SeqNo);
		filler.add(I_AD_Tab.COLUMNNAME_TabLevel);
		filler.add(I_AD_Tab.COLUMNNAME_WhereClause);
		filler.add(I_AD_Tab.COLUMNNAME_ReadOnlyLogic);
		filler.add(I_AD_Tab.COLUMNNAME_DisplayLogic);
		filler.add(I_AD_Tab.COLUMNNAME_IsInsertRecord);
		filler.add(I_AD_Tab.COLUMNNAME_IsAdvancedTab); 
		filler.add(I_AD_Tab.COLUMNNAME_ImportFields);
		return atts;		
	}

}
