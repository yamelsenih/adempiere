/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.               *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *                                                                            *
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com                     *
 * Contributor(s): Low Heng Sin hengsin@avantz.com                            *
 *                 Teo Sarca teo.sarca@gmail.com                              *
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
import org.compiere.model.I_AD_Field;
import org.compiere.model.I_AD_FieldGroup;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.I_AD_Tab;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Val_Rule;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MTable;
import org.compiere.model.X_AD_Field;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class FieldElementHandler extends AbstractElementHandler
{
	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String includeTabUuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_Included_Tab_ID);
		// Set Included Tab ID if this task was previously postponed 
		if (element.defer && element.recordId > 0 && includeTabUuid != null) {
			X_AD_Field field = new X_AD_Field(ctx, element.recordId, getTrxName(ctx));
			int tabId = getIdFromUUID(ctx, I_AD_Tab.Table_Name, includeTabUuid);
			if(tabId > 0) {
				field.setIncluded_Tab_ID(tabId);
			}
			field.saveEx();
			return;
		}
		//	
		String uuid = getUUIDValue(atts, I_AD_Field.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Field.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null && element.parent.getElementValue().equals("tab") &&
				element.parent.defer) {
				element.defer = true;
				return;
			}
			String windowUuid = getUUIDValue(atts, I_AD_Window.COLUMNNAME_AD_Window_ID);
			String tabUuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Tab_ID);
			String tableUuid = getUUIDValue(atts, I_AD_Table.COLUMNNAME_AD_Table_ID);
			String columnUuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Column_ID);
			int tableId = getIdFromUUID(ctx, I_AD_Table.Table_Name, tableUuid);
			if (tableId <= 0) {
				element.defer = true;
				return;
			}
			int windowId = getIdFromUUID(ctx, I_AD_Window.Table_Name, windowUuid);
			if (windowId <= 0) {
				element.defer = true;
				return;
			}
			int columnId = getIdFromUUID(ctx, I_AD_Column.Table_Name, columnUuid);
			if (columnId <= 0) {
				element.defer = true;
				return;
			}
			int tabId = getIdFromUUID(ctx, I_AD_Tab.Table_Name, tabUuid);
			if (tabId > 0) {
				int id = getIdFromUUID(ctx, I_AD_Field.Table_Name, uuid);
				X_AD_Field field = new X_AD_Field(ctx, id, getTrxName(ctx));
				if (id <= 0 && getIntValue(atts, I_AD_Field.COLUMNNAME_AD_Field_ID) > 0 && getIntValue(atts, I_AD_Field.COLUMNNAME_AD_Field_ID) <= PackOut.MAX_OFFICIAL_ID) {
					field.setAD_Field_ID(getIntValue(atts, I_AD_Field.COLUMNNAME_AD_Field_ID));
					field.setIsDirectLoad(true);
				}
				int backupId = -1;
				String Object_Status = null;
				if (id > 0) {
					backupId = copyRecord(ctx, "AD_Field", field);
					Object_Status = "Update";
				} else {
					Object_Status = "New";
					backupId = 0;
				}
				field.setUUID(uuid);
				//	Tab
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Tab_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Tab.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setAD_Tab_ID(id);
				}
				//	Column
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Column_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setAD_Column_ID(id);
				}
				//	Field Group
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_FieldGroup_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_FieldGroup.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setAD_FieldGroup_ID(id);
				}
				//	Included Tab
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_Included_Tab_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Tab.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setIncluded_Tab_ID(id);
				}
				//	Reference
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Reference_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setAD_Reference_ID(id);
				}
				//	Reference Value
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Reference_Value_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setAD_Reference_Value_ID(id);
				}
				//	Validation Rule
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Val_Rule_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Val_Rule.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					field.setAD_Val_Rule_ID(id);
				}
				//	Standard Attributes
				field.setName(getStringValue(atts, I_AD_Field.COLUMNNAME_Name));
				field.setDescription(getStringValue(atts, I_AD_Field.COLUMNNAME_Description));
				field.setHelp(getStringValue(atts, I_AD_Field.COLUMNNAME_Help));
				field.setEntityType(getStringValue(atts, I_AD_Field.COLUMNNAME_EntityType));
				field.setIsSameLine(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsSameLine));
				field.setIsCentrallyMaintained(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsCentrallyMaintained));
				field.setIsDisplayed(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsDisplayed));
				field.setIsActive(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsActive));
				field.setIsEncrypted(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsEncrypted));
				field.setIsFieldOnly(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsFieldOnly));
				field.setIsHeading(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsHeading));
				field.setIsQuickEntry(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsQuickEntry));
				field.setIsReadOnly(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsReadOnly));
				field.setSeqNo(getIntValue(atts, I_AD_Field.COLUMNNAME_SeqNo));
				field.setSeqNoGrid(getIntValue(atts, I_AD_Field.COLUMNNAME_SeqNoGrid));
				field.setSortNo(getBigDecimalValue(atts, I_AD_Field.COLUMNNAME_SortNo));
				field.setDisplayLength(getIntValue(atts, I_AD_Field.COLUMNNAME_DisplayLength));
				field.setDisplayLogic(getStringValue(atts, I_AD_Field.COLUMNNAME_DisplayLogic));
				field.setObscureType(getStringValue(atts, I_AD_Field.COLUMNNAME_ObscureType));
				field.setInfoFactoryClass(getStringValue(atts, I_AD_Field.COLUMNNAME_InfoFactoryClass));
				field.setIsMandatory(getStringValue(atts, I_AD_Field.COLUMNNAME_IsMandatory));
				field.setDefaultValue(getStringValue(atts, I_AD_Field.COLUMNNAME_DefaultValue));
				field.setPreferredWidth(getIntValue(atts, I_AD_Field.COLUMNNAME_PreferredWidth));
				field.setIsDisplayedGrid(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsDisplayedGrid));
				field.setIsAllowCopy(getBooleanValue(atts, I_AD_Field.COLUMNNAME_IsAllowCopy));				
				//	Save
				try {
					field.saveEx(getTrxName(ctx));
					recordLog(ctx, 1, field.getUUID(), "Field", field
							.get_ID(), backupId, Object_Status, "AD_Field",
							get_IDWithColumn(ctx, "AD_Table", "TableName",
									"AD_Field"));
					element.recordId = field.getAD_Field_ID();
				} catch (Exception e) {
					recordLog(ctx, 0, field.getUUID(), "Field", field
							.get_ID(), backupId, Object_Status, "AD_Field",
							get_IDWithColumn(ctx, "AD_Table", "TableName",
									"AD_Field"));
					throw new POSaveFailedException(e);	
				}
				// If Included Tab not found, then postpone this task for later processing 
				if (field.getAD_Field_ID() > 0 && includeTabUuid != null && field.getIncluded_Tab_ID() <= 0)
				{
					element.defer = true;
				}
			} else {
				element.defer = true;
				return;
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int fieldId = Env.getContextAsInt(ctx, X_AD_Field.COLUMNNAME_AD_Field_ID);
		X_AD_Field field = new X_AD_Field(ctx, fieldId, null);
		AttributesImpl atts = new AttributesImpl();
		createFieldBinding(atts, field);
		PackOut packOut = (PackOut)ctx.get("PackOutProcess");
		//	Field Group
		if(field.getAD_FieldGroup_ID() > 0){
			packOut.createFieldGroupElement(field.getAD_FieldGroup_ID(), document);
		}
		//	 Reference
		if(field.getAD_Reference_ID() > 0) {
			packOut.createReference(field.getAD_Reference_ID(), document);
		}
		//	Reference Value
		if (field.getAD_Reference_Value_ID() > 0) {
			packOut.createReference(field.getAD_Reference_Value_ID(), document);
		}
		//	Validation Rule
		if (field.getAD_Val_Rule_ID() > 0) {
			packOut.createDynamicRuleValidation(field.getAD_Val_Rule_ID(), document);
		}
		//	
		document.startElement("", "", "field", atts);
		document.endElement("", "", "field");
	}

	private AttributesImpl createFieldBinding(AttributesImpl atts, X_AD_Field field) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, field);
		if (field.getAD_Field_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Field.COLUMNNAME_AD_Field_ID);
		}
		filler.addUUID();
		//	Column
		if (field.getAD_Column_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_AD_Column_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_AD_Column_ID, getUUIDFromId(field.getCtx(), I_AD_Column.Table_Name, field.getAD_Column_ID()));
			//	For table
			MTable table = MTable.get(Env.getCtx(), field.getAD_Column().getAD_Table_ID());
			filler.addUUID(I_AD_Table.COLUMNNAME_AD_Table_ID, table.getUUID());
		}
		//	For Field Group
		if (field.getAD_FieldGroup_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_AD_FieldGroup_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_AD_FieldGroup_ID, getUUIDFromId(field.getCtx(), I_AD_FieldGroup.Table_Name, field.getAD_FieldGroup_ID()));
		}
		//	For Tab
		if (field.getAD_Tab_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_AD_Tab_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_AD_Tab_ID, getUUIDFromId(field.getCtx(), I_AD_Tab.Table_Name, field.getAD_Tab_ID()));
			filler.addUUID(I_AD_Window.COLUMNNAME_AD_Window_ID, field.getAD_Tab().getAD_Window().getUUID());
		}
		//	For include Tab
		if (field.getIncluded_Tab_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_Included_Tab_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_Included_Tab_ID, getUUIDFromId(field.getCtx(), I_AD_Tab.Table_Name, field.getIncluded_Tab_ID()));
		}
		//	Reference
		if (field.getAD_Reference_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_AD_Reference_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_AD_Reference_ID, getUUIDFromId(field.getCtx(), I_AD_Reference.Table_Name, field.getAD_Reference_ID()));
		}
		//	Reference Value
		if (field.getAD_Reference_Value_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_AD_Reference_Value_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_AD_Reference_Value_ID, getUUIDFromId(field.getCtx(), I_AD_Reference.Table_Name, field.getAD_Reference_Value_ID()));
		}
		//	Validation Rule
		if (field.getAD_Val_Rule_ID() > 0) {
			filler.add(I_AD_Field.COLUMNNAME_AD_Val_Rule_ID, true);
			filler.addUUID(I_AD_Field.COLUMNNAME_AD_Val_Rule_ID, getUUIDFromId(field.getCtx(), I_AD_Val_Rule.Table_Name, field.getAD_Val_Rule_ID()));
		}
		//	Attributes
		filler.add(I_AD_Field.COLUMNNAME_Name);
		filler.add(I_AD_Field.COLUMNNAME_Description);
		filler.add(I_AD_Field.COLUMNNAME_Help);
		filler.add(I_AD_Field.COLUMNNAME_EntityType);
		filler.add(I_AD_Field.COLUMNNAME_IsSameLine);
		filler.add(I_AD_Field.COLUMNNAME_IsCentrallyMaintained);
		filler.add(I_AD_Field.COLUMNNAME_IsDisplayed);
		filler.add(I_AD_Field.COLUMNNAME_IsActive);
		filler.add(I_AD_Field.COLUMNNAME_IsEncrypted);
		filler.add(I_AD_Field.COLUMNNAME_IsFieldOnly);
		filler.add(I_AD_Field.COLUMNNAME_IsHeading);
		filler.add(I_AD_Field.COLUMNNAME_IsQuickEntry);
		filler.add(I_AD_Field.COLUMNNAME_IsReadOnly);
		filler.add(I_AD_Field.COLUMNNAME_SeqNo);
		filler.add(I_AD_Field.COLUMNNAME_SeqNoGrid);
		filler.add(I_AD_Field.COLUMNNAME_SortNo);
		filler.add(I_AD_Field.COLUMNNAME_DisplayLength);
		filler.add(I_AD_Field.COLUMNNAME_DisplayLogic);
		filler.add(I_AD_Field.COLUMNNAME_ObscureType);
		filler.add(I_AD_Field.COLUMNNAME_InfoFactoryClass);
		filler.add(I_AD_Field.COLUMNNAME_IsMandatory);
		filler.add(I_AD_Field.COLUMNNAME_DefaultValue);
		filler.add(I_AD_Field.COLUMNNAME_PreferredWidth);
		filler.add(I_AD_Field.COLUMNNAME_IsDisplayedGrid);
		filler.add(I_AD_Field.COLUMNNAME_IsAllowCopy);
		return atts;
	}
}
