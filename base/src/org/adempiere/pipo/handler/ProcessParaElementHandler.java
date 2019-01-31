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
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_Process_Para;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.I_AD_Val_Rule;
import org.compiere.model.X_AD_Process_Para;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ProcessParaElementHandler extends AbstractElementHandler {

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Process_Para.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Process_Para.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null && element.parent.getElementValue().equals("process") &&
				element.parent.defer) {
				element.defer = true;
				return;
			}
			int id = 0;
			int masterId = 0;
			String processUuid = getUUIDValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Process_ID);
			if (element.parent != null && element.parent.getElementValue().equals("process") &&
				element.parent.recordId > 0) {
				masterId = element.parent.recordId;
			} else {
				if (!Util.isEmpty(processUuid)) {
					masterId = getIdWithFromUUID(ctx, I_AD_Process.Table_Name, processUuid);					
				}				
			}
			if (masterId <= 0) {
				element.defer = true;
				element.unresolved = "AD_Process: " + processUuid;
				return;
			}
			id = getIdWithFromUUID(ctx, I_AD_Process_Para.Table_Name, uuid);
			//	Instance
			X_AD_Process_Para processparameter = new X_AD_Process_Para(ctx, id, getTrxName(ctx));
			int backupId = -1;
			String objectStatus = null;
			if (id <= 0 && getIntValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID) > 0 && getIntValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID) <= PackOut.MAX_OFFICIAL_ID) {
				processparameter.setAD_Process_Para_ID(getIntValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID));
				processparameter.setIsDirectLoad(true);
			}
			if (id > 0) {
				backupId = copyRecord(ctx, "AD_Process_Para", processparameter);
				objectStatus = "Update";
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			processparameter.setUUID(uuid);
			// Process
			uuid = getUUIDValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Process_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Process.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				processparameter.setAD_Process_ID(id);
			}
			// Reference
			uuid = getUUIDValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Reference_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				processparameter.setAD_Reference_ID(id);
			}
			// Reference Value
			uuid = getUUIDValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Reference_Value_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				processparameter.setAD_Reference_Value_ID(id);
			}
			// Validation Rule
			uuid = getUUIDValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Val_Rule_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Val_Rule.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				processparameter.setAD_Val_Rule_ID(id);
			}
			//	Standard Attributes
			processparameter.setName(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_Name));
			processparameter.setDescription(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_Description));
			processparameter.setHelp(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_Help));
			processparameter.setColumnName(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_ColumnName));
			processparameter.setDefaultValue(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_DefaultValue));
			processparameter.setDefaultValue2(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_DefaultValue2));
			processparameter.setDisplayLogic(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_DisplayLogic));
			processparameter.setEntityType(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_EntityType));
			processparameter.setFieldLength(getIntValue(atts, I_AD_Process_Para.COLUMNNAME_FieldLength));
			processparameter.setIsActive(getBooleanValue(atts, I_AD_Process_Para.COLUMNNAME_IsActive));
			processparameter.setIsCentrallyMaintained(getBooleanValue(atts, I_AD_Process_Para.COLUMNNAME_IsCentrallyMaintained));
			processparameter.setIsInfoOnly(getBooleanValue(atts, I_AD_Process_Para.COLUMNNAME_IsInfoOnly));
			processparameter.setIsMandatory(getBooleanValue(atts, I_AD_Process_Para.COLUMNNAME_IsMandatory));
			processparameter.setIsRange(getBooleanValue(atts, I_AD_Process_Para.COLUMNNAME_IsRange));
			processparameter.setReadOnlyLogic(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_ReadOnlyLogic));
			processparameter.setSeqNo(getIntValue(atts, I_AD_Process_Para.COLUMNNAME_SeqNo));
			processparameter.setValueMax(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_ValueMax));
			processparameter.setValueMin(getStringValue(atts, I_AD_Process_Para.COLUMNNAME_ValueMin));
			// Element
			uuid = getUUIDValue(atts, I_AD_Process_Para.COLUMNNAME_AD_Element_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Element.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				processparameter.setAD_Element_ID(id);
			}
			//	Save
			try {
				processparameter.saveEx(getTrxName(ctx));
				recordLog(ctx, 1, processparameter.getName(), "Process_para",
						processparameter.get_ID(), backupId, objectStatus,
						"AD_Process_para", get_IDWithColumn(ctx, "AD_Table",
								"TableName", "AD_Process_para"));
			} catch (Exception e) {
				recordLog(ctx, 0, processparameter.getName(), "Process_para",
						processparameter.get_ID(), backupId, objectStatus,
						"AD_Process_para", get_IDWithColumn(ctx, "AD_Table",
								"TableName", "AD_Process_para"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int processParaId = Env.getContextAsInt(ctx, X_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID);
		X_AD_Process_Para processpara = new X_AD_Process_Para(ctx, processParaId, getTrxName(ctx));
		AttributesImpl atts = new AttributesImpl();
		createProcessParaBinding(atts, processpara);
		document.startElement("", "", "processpara", atts);
		document.endElement("", "", "processpara");
	}

	private AttributesImpl createProcessParaBinding(AttributesImpl atts, X_AD_Process_Para processParameter) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, processParameter);
		if (processParameter.getAD_Process_Para_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID);
		}
		filler.addUUID();
		//	Process
		if (processParameter.getAD_Process_ID() > 0) {
			filler.add(I_AD_Process_Para.COLUMNNAME_AD_Process_ID, true);
			filler.addUUID(I_AD_Process_Para.COLUMNNAME_AD_Process_ID, getUUIDFromId(processParameter.getCtx(), I_AD_Process.Table_Name, processParameter.getAD_Process_ID()));
		}
		//	Element
		if (processParameter.getAD_Element_ID() > 0) {
			filler.add(I_AD_Process_Para.COLUMNNAME_AD_Element_ID, true);
			filler.addUUID(I_AD_Process_Para.COLUMNNAME_AD_Element_ID, getUUIDFromId(processParameter.getCtx(), I_AD_Element.Table_Name, processParameter.getAD_Element_ID()));
		}
		//	Reference
		if (processParameter.getAD_Reference_ID() > 0) {
			filler.add(I_AD_Process_Para.COLUMNNAME_AD_Reference_ID, true);
			filler.addUUID(I_AD_Process_Para.COLUMNNAME_AD_Reference_ID, getUUIDFromId(processParameter.getCtx(), I_AD_Reference.Table_Name, processParameter.getAD_Reference_ID()));
		}
		//	Reference Value
		if (processParameter.getAD_Reference_Value_ID() > 0) {
			filler.add(I_AD_Process_Para.COLUMNNAME_AD_Reference_Value_ID, true);
			filler.addUUID(I_AD_Process_Para.COLUMNNAME_AD_Reference_Value_ID, getUUIDFromId(processParameter.getCtx(), I_AD_Reference.Table_Name, processParameter.getAD_Reference_Value_ID()));
		}
		//	Validation Rule
		if (processParameter.getAD_Val_Rule_ID() > 0) {
			filler.add(I_AD_Process_Para.COLUMNNAME_AD_Val_Rule_ID, true);
			filler.addUUID(I_AD_Process_Para.COLUMNNAME_AD_Val_Rule_ID, getUUIDFromId(processParameter.getCtx(), I_AD_Val_Rule.Table_Name, processParameter.getAD_Val_Rule_ID()));
		}
		//	Standard Attributes
		filler.add(I_AD_Process_Para.COLUMNNAME_Name);
		filler.add(I_AD_Process_Para.COLUMNNAME_Description);
		filler.add(I_AD_Process_Para.COLUMNNAME_Help);
		filler.add(I_AD_Process_Para.COLUMNNAME_ColumnName);
		filler.add(I_AD_Process_Para.COLUMNNAME_DefaultValue);
		filler.add(I_AD_Process_Para.COLUMNNAME_DefaultValue2);
		filler.add(I_AD_Process_Para.COLUMNNAME_DisplayLogic);
		filler.add(I_AD_Process_Para.COLUMNNAME_EntityType);
		filler.add(I_AD_Process_Para.COLUMNNAME_FieldLength);
		filler.add(I_AD_Process_Para.COLUMNNAME_IsActive);
		filler.add(I_AD_Process_Para.COLUMNNAME_IsCentrallyMaintained);
		filler.add(I_AD_Process_Para.COLUMNNAME_IsInfoOnly);
		filler.add(I_AD_Process_Para.COLUMNNAME_IsMandatory);
		filler.add(I_AD_Process_Para.COLUMNNAME_IsRange);
		filler.add(I_AD_Process_Para.COLUMNNAME_ReadOnlyLogic);
		filler.add(I_AD_Process_Para.COLUMNNAME_SeqNo);
		filler.add(I_AD_Process_Para.COLUMNNAME_ValueMax);
		filler.add(I_AD_Process_Para.COLUMNNAME_ValueMin);
		return atts;
	}
}
