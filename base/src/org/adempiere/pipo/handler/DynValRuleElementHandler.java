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
import org.compiere.model.I_AD_Val_Rule;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.model.X_AD_Val_Rule;
import org.compiere.util.Env;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class DynValRuleElementHandler extends AbstractElementHandler {

	private List<Integer> rules = new ArrayList<Integer>();
	
	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Val_Rule.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Val_Rule.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdFromUUID(ctx, I_AD_Val_Rule.Table_Name, uuid);
			X_AD_Val_Rule valRule = new X_AD_Val_Rule(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_Val_Rule.COLUMNNAME_AD_Val_Rule_ID) > 0 && getIntValue(atts, I_AD_Val_Rule.COLUMNNAME_AD_Val_Rule_ID) <= PackOut.MAX_OFFICIAL_ID) {
				valRule.setAD_Val_Rule_ID(getIntValue(atts, I_AD_Val_Rule.COLUMNNAME_AD_Val_Rule_ID));
				valRule.setIsDirectLoad(true);
			}
			int backupId = -1;
			String Object_Status = null;
			if (id > 0){		
				backupId = copyRecord(ctx, "AD_Val_Rule",valRule);
				Object_Status = "Update";			
			}
			else{
				Object_Status = "New";
				backupId =0;
			}
			valRule.setUUID(uuid);
			valRule.setName(getStringValue(atts, I_AD_Val_Rule.COLUMNNAME_Name));
			valRule.setDescription(getStringValue(atts, I_AD_Val_Rule.COLUMNNAME_Description));
			valRule.setCode(getStringValue(atts, I_AD_Val_Rule.COLUMNNAME_Code));
			valRule.setEntityType(getStringValue(atts, I_AD_Val_Rule.COLUMNNAME_EntityType));
			valRule.setIsActive(getBooleanValue(atts, I_AD_Val_Rule.COLUMNNAME_IsActive));
			valRule.setType(getStringValue(atts, I_AD_Val_Rule.COLUMNNAME_Type));			
			//	Save
			try {
				valRule.saveEx(getTrxName(ctx));
				recordLog (ctx, 1, valRule.getUUID(),"ValRule", valRule.get_ID(),backupId, Object_Status,"AD_Val_Rule",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Val_Rule"));
			} catch (Exception e) {
				recordLog (ctx, 0, valRule.getUUID(),"ValRule", valRule.get_ID(),backupId, Object_Status,"AD_Val_Rule",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Val_Rule"));
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
		int valRuleId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_Val_Rule_ID);
		if (rules.contains(valRuleId)) {
			return;
		}
		rules.add(valRuleId);
		AttributesImpl atts = new AttributesImpl();
		X_AD_Val_Rule m_ValRule = new X_AD_Val_Rule (ctx, valRuleId, null);										
		createDynamicValidationRuleBinding(atts,m_ValRule);	
		document.startElement("","","dynvalrule",atts);
		document.endElement("","","dynvalrule");
	}

	private AttributesImpl createDynamicValidationRuleBinding( AttributesImpl atts, X_AD_Val_Rule valRule) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, valRule);
		if (valRule.getAD_Val_Rule_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Val_Rule.COLUMNNAME_AD_Val_Rule_ID);
		}
		filler.addUUID();
		//	Attributes
		filler.add(I_AD_Val_Rule.COLUMNNAME_Name);
		filler.add(I_AD_Val_Rule.COLUMNNAME_Code);
		filler.add(I_AD_Val_Rule.COLUMNNAME_EntityType);
		filler.add(I_AD_Val_Rule.COLUMNNAME_IsActive);
		filler.add(I_AD_Val_Rule.COLUMNNAME_Type);
		return atts;
	}
}
