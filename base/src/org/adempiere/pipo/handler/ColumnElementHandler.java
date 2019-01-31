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
import org.adempiere.pipo.PackIn;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.DatabaseAccessException;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Chart;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Image;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Val_Rule;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.X_AD_Column;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ColumnElementHandler extends AbstractElementHandler {

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		PackIn packIn = (PackIn)ctx.get("PackInProcess");
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Column.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Column.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null && element.parent.getElementValue().equals("table") &&
				element.parent.defer) {
				element.defer = true;
				return;
			}
			String tableUuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Table_ID);
			int tableid = 0;
			if (element.parent != null && element.parent.getElementValue().equals("table") &&
				element.parent.recordId > 0) {
				tableid = element.parent.recordId;
			} else {
				tableid = packIn.getTableUUID(tableUuid);
			}
			if (tableid <= 0) {
				tableid = getIdWithFromUUID(ctx, I_AD_Table.Table_Name, tableUuid);
				if (tableid > 0)
					packIn.addTable(tableUuid, tableid);
			}
			int id = packIn.getColumnId(tableUuid, uuid);
			if (id <= 0) {
				id = getIdWithFromUUID(ctx, I_AD_Column.Table_Name, uuid);
				if (id > 0) {
					packIn.addColumn(tableUuid, uuid, id);
				}
			}
			X_AD_Column column = new X_AD_Column(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_Column.COLUMNNAME_AD_Column_ID) > 0 && getIntValue(atts, I_AD_Column.COLUMNNAME_AD_Column_ID) <= PackOut.MAX_OFFICIAL_ID) {
				column.setAD_Column_ID(getIntValue(atts, I_AD_Column.COLUMNNAME_AD_Column_ID));
				column.setIsDirectLoad(true);
			}
			int backupId = -1;
			String object_Status = null;
			if (id > 0) {
				backupId = copyRecord(ctx, "AD_Column", column);
				object_Status = "Update";
			} else {
				object_Status = "New";
				backupId = 0;
			}
			column.setUUID(uuid);
			// Process
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Process_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Process.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Process_ID(id);
			}
			// Reference
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Reference_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Reference_ID(id);
			}
			// Reference Value
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Reference_Value_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Reference_Value_ID(id);
			}			
			// Table
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Table_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Table.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Table_ID(id);
			}
			// Validation Rule
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Val_Rule_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Val_Rule.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Val_Rule_ID(id);
			}
			// Image
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Image_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Image.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Image_ID(id);
			}
			// Chart
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Chart_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Chart.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Chart_ID(id);
			}
			//	Attributes
			column.setCallout(getStringValue(atts, I_AD_Column.COLUMNNAME_Callout));
			column.setColumnName(getStringValue(atts, I_AD_Column.COLUMNNAME_ColumnName));
			column.setDefaultValue(getStringValue(atts, I_AD_Column.COLUMNNAME_DefaultValue));
			column.setDescription(getStringValue(atts, I_AD_Column.COLUMNNAME_Description));
			column.setEntityType(getStringValue(atts, I_AD_Column.COLUMNNAME_EntityType));
			column.setFieldLength(getIntValue(atts, I_AD_Column.COLUMNNAME_FieldLength));
			column.setHelp(getStringValue(atts, I_AD_Column.COLUMNNAME_Help));
			column.setIsAlwaysUpdateable(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsAlwaysUpdateable));
			column.setIsIdentifier(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsIdentifier));
			column.setIsKey(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsKey));
			column.setIsMandatory(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsMandatory));
			column.setIsSelectionColumn(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsSelectionColumn));
			column.setIsParent(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsParent));
			column.setIsActive(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsActive));
			column.setIsTranslated(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsTranslated));
			column.setIsUpdateable(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsUpdateable));
			column.setName(getStringValue(atts, I_AD_Column.COLUMNNAME_Name));
			column.setReadOnlyLogic(getStringValue(atts, I_AD_Column.COLUMNNAME_ReadOnlyLogic));
			column.setSeqNo(getIntValue(atts, I_AD_Column.COLUMNNAME_SeqNo));
			column.setVFormat(getStringValue(atts, I_AD_Column.COLUMNNAME_VFormat));
			column.setValueMax(getStringValue(atts, I_AD_Column.COLUMNNAME_ValueMax));
			column.setValueMin(getStringValue(atts, I_AD_Column.COLUMNNAME_ValueMin));
			column.setVersion(getBigDecimalValue(atts, I_AD_Column.COLUMNNAME_Version));
			column.setInfoFactoryClass(getStringValue(atts, I_AD_Column.COLUMNNAME_InfoFactoryClass));
			column.setIsAllowCopy(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsAllowCopy));
			column.setIsAllowLogging(getBooleanValue(atts, I_AD_Column.COLUMNNAME_IsAllowLogging));
			column.setFormatPattern(getStringValue(atts, I_AD_Column.COLUMNNAME_FormatPattern));
			// Element
			uuid = getUUIDValue(atts, I_AD_Column.COLUMNNAME_AD_Element_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Element.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				column.setAD_Element_ID(id);
			}
			//	
			boolean recreateColumn = (column.is_new()
					|| column.is_ValueChanged("AD_Reference_ID")
					|| column.is_ValueChanged("FieldLength")
					|| column.is_ValueChanged("ColumnName") || column.is_ValueChanged("IsMandatory"));
			
			//ignore fieldlength change for clob and lob
			if (!column.is_ValueChanged("AD_Reference_ID") && column.is_ValueChanged("FieldLength")) {
				if (DisplayType.isLOB(column.getAD_Reference_ID())) {
					recreateColumn = false;
				}
			}
			// changed default ??
			// m_Column.is_ValueChanged("DefaultValue") doesn't work well with
			// nulls
			if (!recreateColumn) {
				String oldDefault = (String) column.get_ValueOld("DefaultValue");
				String newDefault = column.getDefaultValue();
				if (oldDefault != null && oldDefault.length() == 0)
					oldDefault = null;
				if (newDefault != null && newDefault.length() == 0)
					newDefault = null;
				if ((oldDefault == null && newDefault != null)
						|| (oldDefault != null && newDefault == null)) {
					recreateColumn = true;
				} else if (oldDefault != null && newDefault != null) {
					if (!oldDefault.equals(newDefault))
						recreateColumn = true;
				}
			}

			// Don't create database column for virtual columns
			boolean syncDatabase = "Y".equalsIgnoreCase(atts.getValue("getIsSyncDatabase"));
			if (recreateColumn) {
				if (!Util.isEmpty(column.getColumnSQL()) || !syncDatabase)
					recreateColumn = false;
			}
			//	Save
			try {
				column.saveEx(getTrxName(ctx));
				recordLog(ctx, 1, column.getName(), "Column", column
						.get_ID(), backupId, object_Status, "AD_Column",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Column"));
				element.recordId = column.getAD_Column_ID();
			} catch (Exception e) {
				recordLog(ctx, 0, column.getName(), "Column", column
						.get_ID(), backupId, object_Status, "AD_Column",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Column"));
				throw new POSaveFailedException(e);
			}
			//	Recreate
			if (recreateColumn || syncDatabase) {
				MTable table = new MTable(ctx, column.getAD_Table_ID(), getTrxName(ctx));
				if (!table.isView() && Util.isEmpty(column.getColumnSQL())) {
					try {
						MColumn syncColumn = new MColumn(column.getCtx(), column.getAD_Column_ID(), column.get_TrxName());
						syncColumn.syncDatabase();
						recordLog(ctx, 1, column.getColumnName(), "dbColumn",
								column.get_ID(), 0, object_Status, atts.getValue(
										"ADTableNameID").toUpperCase(),
								get_IDWithColumn(ctx, "AD_Table", "TableName", atts
										.getValue("ADTableNameID").toUpperCase()));
					} catch (Exception e) {
						recordLog(ctx, 0, column.getColumnName(), "dbColumn",
								column.get_ID(), 0, object_Status, atts.getValue(
										"ADTableNameID").toUpperCase(),
								get_IDWithColumn(ctx, "AD_Table", "TableName", atts
										.getValue("ADTableNameID").toUpperCase()));
						throw new DatabaseAccessException(e);
					}
				}
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int columnId = Env.getContextAsInt(ctx, X_AD_Column.COLUMNNAME_AD_Column_ID);
		AttributesImpl atts = new AttributesImpl();
		X_AD_Column column = new X_AD_Column(ctx, columnId, getTrxName(ctx));
		createColumnBinding(atts, column);
		document.startElement("", "", "column", atts);
		document.endElement("", "", "column");
	}

	private AttributesImpl createColumnBinding(AttributesImpl atts, X_AD_Column column) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, column);
		if (column.getAD_Column_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Column_ID);
		}
		filler.addUUID();
		//	Process reference
		if (column.getAD_Process_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Process_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Process_ID, getUUIDFromId(column.getCtx(), I_AD_Process.Table_Name, column.getAD_Process_ID()));
		}
		//	Element Reference
		if (column.getAD_Element_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Element_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Element_ID, getUUIDFromId(column.getCtx(), I_AD_Element.Table_Name, column.getAD_Element_ID()));
		}
		//	reference
		if (column.getAD_Reference_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Reference_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Reference_ID, getUUIDFromId(column.getCtx(), I_AD_Reference.Table_Name, column.getAD_Reference_ID()));
		}
		//	reference Value
		if (column.getAD_Reference_Value_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Reference_Value_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Reference_Value_ID, getUUIDFromId(column.getCtx(), I_AD_Reference.Table_Name, column.getAD_Reference_Value_ID()));
		}
		//	Table Reference
		if (column.getAD_Table_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Table_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Table_ID, getUUIDFromId(column.getCtx(), I_AD_Table.Table_Name, column.getAD_Table_ID()));
			MTable table = MTable.get(Env.getCtx(), column.getAD_Table_ID());
			filler.addString(I_AD_Table.COLUMNNAME_TableName, table.getTableName());
		}
		//	Validation Rule
		if (column.getAD_Val_Rule_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Val_Rule_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Val_Rule_ID, getUUIDFromId(column.getCtx(), I_AD_Val_Rule.Table_Name, column.getAD_Val_Rule_ID()));
		}
		//	Image
		if (column.getAD_Image_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Image_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Image_ID, getUUIDFromId(column.getCtx(), I_AD_Image.Table_Name, column.getAD_Image_ID()));
		}
		//	Chart
		if (column.getAD_Chart_ID() > 0) {
			filler.add(I_AD_Column.COLUMNNAME_AD_Chart_ID, true);
			filler.addUUID(I_AD_Column.COLUMNNAME_AD_Chart_ID, getUUIDFromId(column.getCtx(), I_AD_Chart.Table_Name, column.getAD_Chart_ID()));
		}
		filler.add(I_AD_Column.COLUMNNAME_Callout);
		filler.add(I_AD_Column.COLUMNNAME_ColumnSQL);
		filler.add(I_AD_Column.COLUMNNAME_ColumnName);
		filler.add(I_AD_Column.COLUMNNAME_DefaultValue);
		filler.add(I_AD_Column.COLUMNNAME_Description);
		filler.add(I_AD_Column.COLUMNNAME_EntityType);
		filler.add(I_AD_Column.COLUMNNAME_FieldLength);
		filler.add(I_AD_Column.COLUMNNAME_Help);
		filler.add(I_AD_Column.COLUMNNAME_IsAlwaysUpdateable);
		filler.add(I_AD_Column.COLUMNNAME_IsIdentifier);
		filler.add(I_AD_Column.COLUMNNAME_IsKey);
		filler.add(I_AD_Column.COLUMNNAME_IsMandatory);
		filler.add(I_AD_Column.COLUMNNAME_IsSelectionColumn);
		filler.add(I_AD_Column.COLUMNNAME_IsParent);
		filler.add(I_AD_Column.COLUMNNAME_IsActive);
		filler.add(I_AD_Column.COLUMNNAME_IsTranslated);
		filler.add(I_AD_Column.COLUMNNAME_IsUpdateable);
		filler.add(I_AD_Column.COLUMNNAME_Name);
		filler.add(I_AD_Column.COLUMNNAME_ReadOnlyLogic);
		filler.add(I_AD_Column.COLUMNNAME_SeqNo);
		filler.add(I_AD_Column.COLUMNNAME_VFormat);
		filler.add(I_AD_Column.COLUMNNAME_ValueMax);
		filler.add(I_AD_Column.COLUMNNAME_ValueMin);
		filler.add(I_AD_Column.COLUMNNAME_Version);
		filler.add(I_AD_Column.COLUMNNAME_InfoFactoryClass);
		filler.add(I_AD_Column.COLUMNNAME_IsAllowCopy);
		filler.add(I_AD_Column.COLUMNNAME_IsAllowLogging);
		filler.add(I_AD_Column.COLUMNNAME_FormatPattern);
		
		return atts;
	}
}
