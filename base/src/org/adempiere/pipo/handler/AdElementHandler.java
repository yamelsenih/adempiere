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
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.PoFiller;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.X_AD_Element;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class AdElementHandler extends AbstractElementHandler {

	private List<Integer> processedElements = new ArrayList<Integer>();
	
	private final String AD_ELEMENT = "AD_Element";
	
	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		int backupId = -1;
		String objectStatus = null;
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Element.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Element.COLUMNNAME_EntityType);
		//	
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdWithFromUUID(ctx, I_AD_Element.Table_Name, uuid);
			X_AD_Element importElement = new X_AD_Element(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_Element.COLUMNNAME_AD_Element_ID) > 0 && getIntValue(atts, I_AD_Element.COLUMNNAME_AD_Element_ID) <= PackOut.MAX_OFFICIAL_ID) {
				importElement.setAD_Element_ID(getIntValue(atts, I_AD_Element.COLUMNNAME_AD_Element_ID));
				importElement.setIsDirectLoad(true);
			}
			if (id > 0) {
				backupId = copyRecord(ctx, AD_ELEMENT, importElement);
				objectStatus = "Update";
				if (processedElements.contains(id)) {
					element.skip = true;
					return;
				}
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			importElement.setUUID(uuid);
			PoFiller pf = new PoFiller(importElement, atts);
			
			pf.setBoolean(I_AD_Element.COLUMNNAME_IsActive);
			
			pf.setString(X_AD_Element.COLUMNNAME_ColumnName);
			pf.setString(X_AD_Element.COLUMNNAME_Description);
			pf.setString(X_AD_Element.COLUMNNAME_EntityType);
			pf.setString(X_AD_Element.COLUMNNAME_Help);
			pf.setString(X_AD_Element.COLUMNNAME_Name);
			pf.setString(X_AD_Element.COLUMNNAME_PrintName);
			
			pf.setString(X_AD_Element.COLUMNNAME_PO_Description);
			pf.setString(X_AD_Element.COLUMNNAME_PO_Name);
			pf.setString(X_AD_Element.COLUMNNAME_PO_Help);
			pf.setString(X_AD_Element.COLUMNNAME_PO_PrintName);
			// Reference
			uuid = getUUIDValue(atts, I_AD_Element.COLUMNNAME_AD_Reference_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				importElement.setAD_Reference_ID(id);
			}
			// Reference
			uuid = getUUIDValue(atts, I_AD_Element.COLUMNNAME_AD_Reference_Value_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Reference.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				importElement.setAD_Reference_ID(id);
			}			
            //	Save
			try {
				importElement.saveEx(getTrxName(ctx));
				recordLog(ctx, 1, importElement.getName(), "Element",
						importElement.get_ID(), backupId, objectStatus,
						AD_ELEMENT, get_IDWithColumn(ctx, "AD_Table",
								"TableName", AD_ELEMENT));
				//	
				element.recordId = importElement.getAD_Element_ID();
				processedElements.add(importElement.getAD_Element_ID());
			} catch (Exception e) {
				recordLog(ctx, 0, importElement.getName(), "Element",
						importElement.get_ID(), backupId, objectStatus,
						AD_ELEMENT, get_IDWithColumn(ctx, "AD_Table",
								"TableName", AD_ELEMENT));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int elementId = Env.getContextAsInt(ctx, X_AD_Element.COLUMNNAME_AD_Element_ID);
		//	
		if (processedElements.contains(elementId)) {
			return;
		}
		//	
		processedElements.add(elementId);
		//	
		X_AD_Element element = new X_AD_Element(ctx, elementId, null);
		AttributesImpl atts = new AttributesImpl();
		createAdElementBinding(atts, element);
		document.startElement("", "", "element", atts);
		PackOut packOut = (PackOut)ctx.get("PackOutProcess");
		packOut.createTranslations(X_AD_Element.Table_Name, element.get_ID(), document);		
		document.endElement("", "", "element");
	}
	
	private AttributesImpl createAdElementBinding(AttributesImpl atts, X_AD_Element element) {
		AttributeFiller filler = new AttributeFiller(atts, element);
		if (element.getAD_Element_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(X_AD_Element.COLUMNNAME_AD_Element_ID);
		}
		filler.addUUID();
		//	Reference
		if (element.getAD_Reference_ID() > 0) {
			filler.add(I_AD_Element.COLUMNNAME_AD_Reference_ID, true);
			filler.addUUID(I_AD_Element.COLUMNNAME_AD_Reference_ID, getUUIDFromId(element.getCtx(), I_AD_Reference.Table_Name, element.getAD_Reference_ID()));
        }
		//	Reference value
		if (element.getAD_Reference_Value_ID() > 0) {
			filler.add(I_AD_Element.COLUMNNAME_AD_Reference_Value_ID, true);
			filler.addUUID(I_AD_Element.COLUMNNAME_AD_Reference_Value_ID, getUUIDFromId(element.getCtx(), I_AD_Reference.Table_Name, element.getAD_Reference_Value_ID()));
        }
		//	
		filler.add(X_AD_Element.COLUMNNAME_ColumnName);
		filler.add(X_AD_Element.COLUMNNAME_Description);
		filler.add(X_AD_Element.COLUMNNAME_EntityType);
		filler.add(X_AD_Element.COLUMNNAME_Help);
		filler.add(X_AD_Element.COLUMNNAME_Name);
		filler.add(X_AD_Element.COLUMNNAME_PrintName);
        filler.add(X_AD_Element.COLUMNNAME_FieldLength);
        filler.add(X_AD_Element.COLUMNNAME_IsActive);
		filler.add(X_AD_Element.COLUMNNAME_PO_Description);
		filler.add(X_AD_Element.COLUMNNAME_PO_Name);
		filler.add(X_AD_Element.COLUMNNAME_PO_Help);
		filler.add(X_AD_Element.COLUMNNAME_PO_PrintName);
		return atts;
	}
}
