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
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Form;
import org.compiere.model.X_AD_Form;
import org.compiere.util.Env;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class FormElementHandler extends AbstractElementHandler {

	private List<Integer> forms = new ArrayList<Integer>();
	
	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Form.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Form.COLUMNNAME_EntityType);		
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdWithFromUUID(ctx, I_AD_Form.Table_Name, uuid);
			X_AD_Form form = new X_AD_Form(ctx, id, getTrxName(ctx));
			int backupId = -1;
			String objectStatus = null;
			if (id <= 0 && getIntValue(atts, I_AD_Form.COLUMNNAME_AD_Form_ID) > 0 && getIntValue(atts, I_AD_Form.COLUMNNAME_AD_Form_ID) <= PackOut.MAX_OFFICIAL_ID) {
				form.setAD_Form_ID(getIntValue(atts, I_AD_Form.COLUMNNAME_AD_Form_ID));
				form.setIsDirectLoad(true);
			}
			if (id > 0){
				backupId = copyRecord(ctx, "AD_Form",form);
				objectStatus = "Update";
			}
			else{
				objectStatus = "New";
				backupId =0;
			}
			form.setUUID(uuid);
			form.setName(getStringValue(atts, I_AD_Form.COLUMNNAME_Name));
			form.setDescription(getStringValue(atts, I_AD_Form.COLUMNNAME_Description));
			form.setHelp(getStringValue(atts, I_AD_Form.COLUMNNAME_Help));
			form.setEntityType(getStringValue(atts, I_AD_Form.COLUMNNAME_EntityType));
			form.setAccessLevel(getStringValue(atts, I_AD_Form.COLUMNNAME_AccessLevel));
			form.setClassname(getStringValue(atts, I_AD_Form.COLUMNNAME_Classname));
			form.setIsBetaFunctionality(getBooleanValue(atts, I_AD_Form.COLUMNNAME_IsBetaFunctionality));
			form.setIsActive(getBooleanValue(atts, I_AD_Form.COLUMNNAME_IsActive));
			//	Save
			try {
				form.saveEx(getTrxName(ctx));
				recordLog (ctx, 1, form.getName(),"Form", form.get_ID(),backupId, objectStatus,"AD_Form",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Form"));
			} catch (Exception e) {
				recordLog (ctx, 0, form.getName(),"Form", form.get_ID(),backupId, objectStatus,"AD_Form",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Form"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document)
			throws SAXException {
		int AD_Form_ID = Env.getContextAsInt(ctx, "AD_Form_ID");
		if (forms.contains(AD_Form_ID)) return;
		forms.add(AD_Form_ID);
		X_AD_Form m_Form = new X_AD_Form (ctx, AD_Form_ID, null);
		AttributesImpl atts = new AttributesImpl();
		createFormBinding(atts,m_Form);
		document.startElement("","","form",atts);
		document.endElement("","","form");		
	}
	
	private AttributesImpl createFormBinding( AttributesImpl atts, X_AD_Form form) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, form);
		if (form.getAD_Form_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Form.COLUMNNAME_AD_Form_ID);
		}
		filler.addUUID();
		//	
		filler.add(I_AD_Form.COLUMNNAME_Name);
		filler.add(I_AD_Form.COLUMNNAME_Description);
		filler.add(I_AD_Form.COLUMNNAME_Help);
		filler.add(I_AD_Form.COLUMNNAME_EntityType);
		filler.add(I_AD_Form.COLUMNNAME_AccessLevel);
		filler.add(I_AD_Form.COLUMNNAME_Classname);
		filler.add(I_AD_Form.COLUMNNAME_IsBetaFunctionality);
		filler.add(I_AD_Form.COLUMNNAME_IsActive);
        return atts;
	}

}
