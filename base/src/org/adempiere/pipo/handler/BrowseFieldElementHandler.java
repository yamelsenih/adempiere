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
 * Copyright (C) 2003-2011 e-Evolution Consultants. All Rights Reserved.      *
 * Copyright (C) 2003-2011 Victor Pérez Juárez 								  * 
 * Contributor(s): Low Heng Sin hengsin@avantz.com                            *
 *                 Teo Sarca teo.sarca@gmail.com                              *
 *                 Victor Perez  victor.perez@e-evoluton.com				  *
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_Browse;
import org.adempiere.model.I_AD_Browse_Field;
import org.adempiere.model.I_AD_View;
import org.adempiere.model.I_AD_View_Column;
import org.adempiere.model.MBrowseField;
import org.adempiere.model.X_AD_Browse_Field;
import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Field;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.I_AD_Val_Rule;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * @author victor.perez@e-evoluton.com, www.e-evolution.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * 		<li><a href="https://github.com/adempiere/adempiere/issues/556">
 * 		FR [ 556 ] Criteria Search on SB don't have a parameter like only information</a>
 */
public class BrowseFieldElementHandler extends AbstractElementHandler {
	public void startElement(Properties ctx, Element element) throws SAXException {

		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Browse_Field.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null
					&& element.parent.getElementValue().equals("browse")
					&& element.parent.defer) {
				element.defer = true;
				return;
			}
			//	
			String viewUuid = getUUIDValue(atts, I_AD_Browse.COLUMNNAME_AD_View_ID);
			int viewid = getIdFromUUID(ctx, I_AD_View.Table_Name, viewUuid);
			if (viewid <= 0) {
				element.defer = true;
				return;
			}
			String viewColumnUuid = getUUIDValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_View_Column_ID);
			int viewcolumnid = getIdFromUUID(ctx, I_AD_View_Column.Table_Name, viewColumnUuid);
			if (viewcolumnid <= 0) {
				element.defer = true;
				return;
			}
			int id = 0;
			if (element.parent != null
					&& element.parent.getElementValue().equals("browse")
					&& element.parent.recordId > 0) {
				id = element.parent.recordId;
			} else {
				String browseUuid = getUUIDValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_Browse_ID);
				id = getIdFromUUID(ctx, I_AD_Browse.Table_Name, browseUuid);
				if (element.parent != null
						&& element.parent.getElementValue().equals("browse")
						&& id > 0) {
					element.parent.recordId = id;
				}
			}
			if (id > 0) {
				id = getIdFromUUID(ctx, I_AD_Browse_Field.Table_Name, uuid);
				X_AD_Browse_Field browseField = new X_AD_Browse_Field(ctx, id, getTrxName(ctx));
				if (id <= 0 && getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID) > 0 && getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID) <= PackOut.MAX_OFFICIAL_ID) {
					browseField.setAD_Browse_Field_ID(getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID));
					browseField.setIsDirectLoad(true);
				}
				int backupId = -1;
				String objectStatus = null;
				if (id > 0) {
					backupId = copyRecord(ctx, "AD_Browse_Field", browseField);
					objectStatus = "Update";
				} else {
					objectStatus = "New";
					backupId = 0;
				}
				browseField.setUUID(uuid);
				// Browse
				uuid = getUUIDValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_Browse_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Browse.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAD_Browse_ID(id);
				}
				// Element
				uuid = getUUIDValue(atts, I_AD_Browse_Field.COLUMNNAME_AD_Element_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Element.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAD_Element_ID(id);
				}
				//	Reference
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Reference_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAD_Reference_ID(id);
				}
				//	Reference Value
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Reference_Value_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAD_Reference_Value_ID(id);
				}
				//	Validation Rule
				uuid = getUUIDValue(atts, I_AD_Field.COLUMNNAME_AD_Val_Rule_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_Val_Rule.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAD_Val_Rule_ID(id);
				}
				// Axis Parent Column
				uuid = getUUIDValue(atts, I_AD_Browse_Field.COLUMNNAME_Axis_Parent_Column_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_View_Column.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAxis_Parent_Column_ID(id);
				}
				// Axis Column
				uuid = getUUIDValue(atts, I_AD_Browse_Field.COLUMNNAME_Axis_Column_ID);
				if (!Util.isEmpty(uuid)) {
					id = getIdFromUUID(ctx, I_AD_View_Column.Table_Name, uuid);
					if (id <= 0) {
						element.defer = true;
						return;
					}
					browseField.setAxis_Column_ID(id);
				}
				//	Standard Attributes
				browseField.setName(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_Name));
				browseField.setDescription(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_Description));
				browseField.setHelp(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_Help));
				browseField.setCallout(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_Callout));
				browseField.setDefaultValue(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_DefaultValue));
				browseField.setDefaultValue2(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_DefaultValue2));
				browseField.setDisplayLogic(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_DisplayLogic));
				browseField.setEntityType(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_EntityType));
				browseField.setFieldLength(getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_FieldLength));
				browseField.setInfoFactoryClass(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_InfoFactoryClass));
				browseField.setIsActive(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsActive));
				browseField.setIsCentrallyMaintained(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsCentrallyMaintained));
				browseField.setIsDisplayed(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsDisplayed));
				browseField.setIsIdentifier(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsIdentifier));
				browseField.setIsInfoOnly(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsInfoOnly));
				browseField.setIsKey(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsKey));
				browseField.setIsMandatory(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsMandatory));
				browseField.setIsOrderBy(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsOrderBy));
				browseField.setIsQueryCriteria(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsQueryCriteria));
				browseField.setIsRange(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsRange));
				browseField.setIsReadOnly(getBooleanValue(atts, I_AD_Browse_Field.COLUMNNAME_IsReadOnly));
				browseField.setReadOnlyLogic(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_ReadOnlyLogic));
				browseField.setSeqNo(getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_SeqNo));
				browseField.setSeqNoGrid(getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_SeqNoGrid));
				browseField.setSortNo(getIntValue(atts, I_AD_Browse_Field.COLUMNNAME_SortNo));
				browseField.setValueMax(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_ValueMax));
				browseField.setValueMin(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_ValueMin));
				browseField.setVFormat(getStringValue(atts, I_AD_Browse_Field.COLUMNNAME_VFormat));
				//	Save
				try {
					browseField.saveEx(getTrxName(ctx));
					recordLog(
							ctx,
							1,
							browseField.getUUID(),
							"BrowseField",
							browseField.get_ID(),
							backupId,
							objectStatus,
							"AD_Browse_Field",
							get_IDWithColumn(ctx, "AD_Table", "TableName",
									"AD_Browse_Field"));
					element.recordId = browseField.getAD_Browse_Field_ID();
				} catch (Exception e) {
					recordLog(
							ctx,
							0,
							browseField.getUUID(),
							"BrowseField",
							browseField.get_ID(),
							backupId,
							objectStatus,
							"AD_Browse_Field",
							get_IDWithColumn(ctx, "AD_Table", "TableName",
									"AD_Browse_Field"));
					throw new POSaveFailedException(e);
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
		int browseFieldId = Env.getContextAsInt(ctx, X_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID);
		MBrowseField browseField = new MBrowseField(ctx, browseFieldId, null);
		AttributesImpl atts = new AttributesImpl();
		createBrowseFieldBinding(atts, browseField);
		//	
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		if (browseField.getAD_Element_ID() > 0) {
			packOut.createAdElement(browseField.getAD_Element_ID(), document);
		}
		if (browseField.getAD_Reference_ID() > 0) {
			packOut.createReference(browseField.getAD_Reference_ID(), document);
		}
		if (browseField.getAD_Reference_Value_ID() > 0) {
			packOut.createReference(browseField.getAD_Reference_Value_ID(), document);
		}
		if (browseField.getAD_Val_Rule_ID() > 0) {
			packOut.createDynamicRuleValidation(browseField.getAD_Val_Rule_ID(), document);
		}
		document.startElement("", "", "browsefield", atts);
		document.endElement("", "", "browsefield");
	}

	private AttributesImpl createBrowseFieldBinding(AttributesImpl atts, X_AD_Browse_Field browseField) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, browseField);
		if (browseField.getAD_Browse_Field_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID);
		}
		filler.addUUID();
		//	Browse
		if (browseField.getAD_Browse_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_Browse_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_AD_Browse_ID, getUUIDFromId(browseField.getCtx(), I_AD_Browse.Table_Name, browseField.getAD_Browse_ID()));
		}
		//	View Column
		if (browseField.getAD_View_Column_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_View_Column_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_AD_View_Column_ID, getUUIDFromId(browseField.getCtx(), I_AD_View_Column.Table_Name, browseField.getAD_View_Column_ID()));
			//	View UUID
			filler.addUUID(I_AD_Browse.COLUMNNAME_AD_View_ID, getUUIDFromId(browseField.getCtx(), I_AD_View.Table_Name, browseField.getAD_View_Column().getAD_View_ID()));
		}
		//	Element
		if (browseField.getAD_Element_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_Element_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_AD_Element_ID, getUUIDFromId(browseField.getCtx(), I_AD_Element.Table_Name, browseField.getAD_Element_ID()));
		}
		//	Reference
		if (browseField.getAD_Reference_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_Reference_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_AD_Reference_ID, getUUIDFromId(browseField.getCtx(), I_AD_Reference.Table_Name, browseField.getAD_Reference_ID()));
		}
		//	Reference Value
		if (browseField.getAD_Reference_Value_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_Reference_Value_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_AD_Reference_Value_ID, getUUIDFromId(browseField.getCtx(), I_AD_Reference.Table_Name, browseField.getAD_Reference_Value_ID()));
		}
		//	Validation Rule
		if (browseField.getAD_Val_Rule_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_AD_Val_Rule_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_AD_Val_Rule_ID, getUUIDFromId(browseField.getCtx(), I_AD_Val_Rule.Table_Name, browseField.getAD_Val_Rule_ID()));
		}
		//	Axis Parent Column
		if (browseField.getAxis_Parent_Column_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_Axis_Parent_Column_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_Axis_Parent_Column_ID, getUUIDFromId(browseField.getCtx(), I_AD_View_Column.Table_Name, browseField.getAxis_Parent_Column_ID()));
		}
		//	Axis Column
		if (browseField.getAxis_Column_ID() > 0) {
			filler.add(I_AD_Browse_Field.COLUMNNAME_Axis_Column_ID, true);
			filler.addUUID(I_AD_Browse_Field.COLUMNNAME_Axis_Column_ID, getUUIDFromId(browseField.getCtx(), I_AD_View_Column.Table_Name, browseField.getAxis_Column_ID()));
		}
		//	Standard Attributes
		filler.add(I_AD_Browse_Field.COLUMNNAME_Name);
		filler.add(I_AD_Browse_Field.COLUMNNAME_Description);
		filler.add(I_AD_Browse_Field.COLUMNNAME_Help);
		filler.add(I_AD_Browse_Field.COLUMNNAME_Callout);
		filler.add(I_AD_Browse_Field.COLUMNNAME_DefaultValue);
		filler.add(I_AD_Browse_Field.COLUMNNAME_DefaultValue2);
		filler.add(I_AD_Browse_Field.COLUMNNAME_DisplayLogic);
		filler.add(I_AD_Browse_Field.COLUMNNAME_EntityType);
		filler.add(I_AD_Browse_Field.COLUMNNAME_FieldLength);
		filler.add(I_AD_Browse_Field.COLUMNNAME_InfoFactoryClass);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsActive);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsCentrallyMaintained);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsDisplayed);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsIdentifier);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsInfoOnly);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsKey);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsMandatory);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsOrderBy);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsQueryCriteria);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsRange);
		filler.add(I_AD_Browse_Field.COLUMNNAME_IsReadOnly);
		filler.add(I_AD_Browse_Field.COLUMNNAME_ReadOnlyLogic);
		filler.add(I_AD_Browse_Field.COLUMNNAME_SeqNo);
		filler.add(I_AD_Browse_Field.COLUMNNAME_SeqNoGrid);
		filler.add(I_AD_Browse_Field.COLUMNNAME_SortNo);
		filler.add(I_AD_Browse_Field.COLUMNNAME_ValueMax);
		filler.add(I_AD_Browse_Field.COLUMNNAME_ValueMin);
		filler.add(I_AD_Browse_Field.COLUMNNAME_VFormat);
		return atts;
	}
}
