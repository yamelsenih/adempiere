/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2009 Adempiere, Inc. All Rights Reserved.               *
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
 * Copyright (C) 2009 Teo Sarca, teo.sarca@gmail.com                          *
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
import org.compiere.model.I_AD_ModelValidator;
import org.compiere.model.X_AD_ModelValidator;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.util.Env;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Teo Sarca, teo.sarca@gmail.com
 * 			<li>FR [ 2847669 ] 2pack export model validator functionality
 * 				https://sourceforge.net/tracker/?func=detail&aid=2847669&group_id=176962&atid=879335
 */
public class ModelValidatorElementHandler extends AbstractElementHandler
{
	public static final String TAG_Name = "modelvalidator";
	
	private final List<Integer> validators = new ArrayList<Integer>();

	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_ModelValidator.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdWithFromUUID(ctx, I_AD_ModelValidator.Table_Name, uuid);
			X_AD_ModelValidator validator = new X_AD_ModelValidator(ctx, id, getTrxName(ctx));
			int backupId;
			String objectStatus;
			if (id <= 0 && getIntValue(atts, I_AD_ModelValidator.COLUMNNAME_AD_ModelValidator_ID) > 0 && getIntValue(atts, I_AD_ModelValidator.COLUMNNAME_AD_ModelValidator_ID) <= PackOut.MAX_OFFICIAL_ID) {
				validator.setAD_ModelValidator_ID(getIntValue(atts, I_AD_ModelValidator.COLUMNNAME_AD_ModelValidator_ID));
				validator.setIsDirectLoad(true);
			}
			if (id > 0) {		
				backupId = copyRecord(ctx, I_AD_ModelValidator.Table_Name, validator);
				objectStatus = "Update";			
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			validator.setUUID(uuid);
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_Name));
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_Description));
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_Help));
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_ModelValidationClass));
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_EntityType));
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_IsActive));
			validator.setName(getStringValue(atts, I_AD_ModelValidator.COLUMNNAME_SeqNo));
			//	Save
			try {
				validator.saveEx(getTrxName(ctx));
				recordLog (ctx, 1, validator.getName(),TAG_Name, validator.get_ID(),
						backupId, objectStatus,
						I_AD_ModelValidator.Table_Name, I_AD_ModelValidator.Table_ID);
			} catch (Exception e) {
				recordLog (ctx, 0, validator.getName(),TAG_Name, validator.get_ID(),
						backupId, objectStatus,
						I_AD_ModelValidator.Table_Name, I_AD_ModelValidator.Table_ID);
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		final int modelValidatorId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_ModelValidator_ID);
		if (validators.contains(modelValidatorId))
			return;
		validators.add(modelValidatorId);
		final X_AD_ModelValidator validator = new X_AD_ModelValidator(ctx, modelValidatorId, null);
		AttributesImpl atts = new AttributesImpl();
		createMessageBinding(atts, validator);	
		document.startElement("", "", TAG_Name, atts);
		document.endElement("", "", TAG_Name);
	}

	private AttributesImpl createMessageBinding(AttributesImpl atts, X_AD_ModelValidator validator) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, validator);
		if (validator.getAD_ModelValidator_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_ModelValidator.COLUMNNAME_AD_ModelValidator_ID);
		}
		filler.addUUID();
		//	
		filler.add(I_AD_ModelValidator.COLUMNNAME_Name);
		filler.add(I_AD_ModelValidator.COLUMNNAME_Description);
		filler.add(I_AD_ModelValidator.COLUMNNAME_Help);
		filler.add(I_AD_ModelValidator.COLUMNNAME_ModelValidationClass);
		filler.add(I_AD_ModelValidator.COLUMNNAME_EntityType);
		filler.add(I_AD_ModelValidator.COLUMNNAME_IsActive);
		filler.add(I_AD_ModelValidator.COLUMNNAME_SeqNo);
		return atts;
	}
}
