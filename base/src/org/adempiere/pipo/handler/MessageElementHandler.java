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
 * Contributor(s):	Low Heng Sin hengsin@avantz.com
 * 					Teo Sarca, teo.sarca@gmail.com
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
import org.compiere.model.I_AD_Message;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Message;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.util.Env;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class MessageElementHandler extends AbstractElementHandler {

	private List<Integer> messages = new ArrayList<Integer>();
	
	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Message.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Message.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdFromUUID(ctx, I_AD_Message.Table_Name, uuid);
			X_AD_Message message = new X_AD_Message(ctx, id, getTrxName(ctx));
			int backupId  = -1;
			String objectStatus = null;
			if (id <= 0 && getIntValue(atts, I_AD_Message.COLUMNNAME_AD_Message_ID) > 0 && getIntValue(atts, I_AD_Message.COLUMNNAME_AD_Message_ID) <= PackOut.MAX_OFFICIAL_ID) {
				message.setAD_Message_ID(getIntValue(atts, I_AD_Message.COLUMNNAME_AD_Message_ID));
				message.setIsDirectLoad(true);
			}
			if (id > 0){		
				backupId = copyRecord(ctx, "AD_Message",message);
				objectStatus = "Update";			
			}
			else{
				objectStatus = "New";
				backupId =0;
			}
			message.setUUID(uuid);
			message.setMsgText(getStringValue(atts, I_AD_Message.COLUMNNAME_Value));
			message.setMsgText(getStringValue(atts, I_AD_Message.COLUMNNAME_MsgText));
			message.setMsgText(getStringValue(atts, I_AD_Message.COLUMNNAME_MsgTip));
			message.setMsgText(getStringValue(atts, I_AD_Message.COLUMNNAME_MsgType));
			message.setMsgText(getStringValue(atts, I_AD_Message.COLUMNNAME_EntityType));
			//	Save
			try {
				message.saveEx(getTrxName(ctx));
				recordLog (ctx, 1, message.getUUID(),"Message", message.get_ID(),backupId, objectStatus,"AD_Message",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Message"));
			} catch (Exception e) {
				recordLog (ctx, 0, message.getUUID(),"Message", message.get_ID(),backupId, objectStatus,"AD_Message",get_IDWithColumn(ctx, "AD_Table", "TableName", "AD_Message"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}

	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		for (X_AD_Message message : getMessages(ctx)) {
			if (messages.contains(message.getAD_Message_ID())) {
				continue;
			}
			messages.add(message.getAD_Message_ID());
			//
			AttributesImpl atts = new AttributesImpl();
			createMessageBinding(atts, message);	
			document.startElement("","","message",atts);
			document.endElement("","","message");
		}
	}
	
	private List<X_AD_Message> getMessages(Properties ctx) {
		int messageId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_Message_ID);
		int entityTypeId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_EntityType_ID);
		String whereClause;
		Object[] params;
		if (messageId > 0) {
			whereClause = X_AD_Message.COLUMNNAME_AD_Message_ID+"=?";
			params = new Object[]{messageId};
		} else if (entityTypeId > 0) {
			whereClause = " EXISTS (SELECT 1 FROM AD_EntityType et"
				+" WHERE et.AD_EntityType_ID=? AND et.EntityType=AD_Message.EntityType)";
			params = new Object[]{entityTypeId};
		} else {
			throw new IllegalArgumentException("@AD_Message_ID@ / @AD_EntityType_ID@");
		}
		
		List<X_AD_Message> list = new Query(ctx, X_AD_Message.Table_Name, whereClause, null)
		.setParameters(params)
		.setOrderBy(X_AD_Message.COLUMNNAME_AD_Message_ID)
		.list();
		return list;
	}

	private AttributesImpl createMessageBinding( AttributesImpl atts, X_AD_Message message) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, message);
		if (message.getAD_Message_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Message.COLUMNNAME_AD_Message_ID);
		}
		filler.addUUID();
		//	
		filler.add(I_AD_Message.COLUMNNAME_Value);
		filler.add(I_AD_Message.COLUMNNAME_MsgText);
		filler.add(I_AD_Message.COLUMNNAME_MsgTip);
		filler.add(I_AD_Message.COLUMNNAME_MsgType);
		filler.add(I_AD_Message.COLUMNNAME_EntityType);
		return atts;
	}
}
