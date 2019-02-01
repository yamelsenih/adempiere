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
 * Copyright (C) 2003-2012 e-Evolution Consultants. All Rights Reserved.      *
 * Copyright (C) 2003-2012 Victor Pérez Juárez 								  * 
 * Contributor(s): Low Heng Sin hengsin@avantz.com                            *
 *                 Victor Perez  victor.perez@e-evoluton.com				  *
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_View;
import org.adempiere.model.I_AD_View_Definition;
import org.adempiere.model.MViewDefinition;
import org.adempiere.model.X_AD_View;
import org.adempiere.model.X_AD_View_Definition;
import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * @author victor.perez@e-evoluton.com, www.e-evolution.com
 * 
 */
public class ViewElementHandler extends AbstractElementHandler {

	private ViewDefinitionElementHandler viewDefinitionHandler = new ViewDefinitionElementHandler();

	private List<Integer> views = new ArrayList<Integer>();

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_View.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_View.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdFromUUID(ctx, I_AD_View.Table_Name, uuid);
			if (id > 0 && views.contains(id)) {
				return;
			}
			X_AD_View view = new X_AD_View(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_View.COLUMNNAME_AD_View_ID) > 0 && getIntValue(atts, I_AD_View.COLUMNNAME_AD_View_ID) <= PackOut.MAX_OFFICIAL_ID) {
				view.setAD_View_ID(getIntValue(atts, I_AD_View.COLUMNNAME_AD_View_ID));
				view.setIsDirectLoad(true);
			}
			String objectStatus = null;
			int backupId = -1;
			if (id > 0) {
				backupId = copyRecord(ctx, "AD_View", view);
				objectStatus = "Update";
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			view.setUUID(uuid);
			//	Standard Attributes
			view.setValue(getStringValue(atts, I_AD_View.COLUMNNAME_Value));
			view.setName(getStringValue(atts, I_AD_View.COLUMNNAME_Name));
			view.setDescription(getStringValue(atts, I_AD_View.COLUMNNAME_Description));
			view.setHelp(getStringValue(atts, I_AD_View.COLUMNNAME_Help));
			view.setEntityType(getStringValue(atts, I_AD_View.COLUMNNAME_EntityType));
			view.setIsActive(getBooleanValue(atts, I_AD_View.COLUMNNAME_IsActive));
			//	Save
			try {
				view.saveEx(getTrxName(ctx));
				recordLog(
						ctx,
						1,
						view.getUUID(),
						"View",
						view.get_ID(),
						backupId,
						objectStatus,
						"AD_View",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_View"));
				element.recordId = view.getAD_View_ID();
				views.add(view.getAD_View_ID());
			} catch (Exception e) {
				recordLog(
						ctx,
						0,
						view.getUUID(),
						"View",
						view.get_ID(),
						backupId,
						objectStatus,
						"AD_View",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_View"));
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
		int viewId = Env.getContextAsInt(ctx, "AD_View_ID");
		X_AD_View view = new X_AD_View(ctx, viewId, null);
		AttributesImpl atts = new AttributesImpl();
		createViewBinding(atts, view);
		document.startElement("", "", "view", atts);
		// Tab Tag
		StringBuilder whereClause = new StringBuilder(I_AD_View.COLUMNNAME_AD_View_ID).append("=?");
		List<MViewDefinition> viewDefinitions = new Query(ctx,
				I_AD_View_Definition.Table_Name, whereClause.toString(),
				getTrxName(ctx))
				.setParameters(view.getAD_View_ID())
				.setOrderBy(
						X_AD_View_Definition.COLUMNNAME_SeqNo
								+ ","
								+ X_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID)
				.list();

		for (MViewDefinition vd : viewDefinitions) {
			//Is not export table definition because maybe cause changes in tables
			//So that of tables should are created before to import Browser
			//packOut.createTable(vd.getAD_Table_ID(), document);
			createViewDefinition(ctx, document, vd.getAD_View_Definition_ID());
		}
		// Loop tags.
		document.endElement("", "", "view");

	}

	private void createViewDefinition(Properties ctx, TransformerHandler document, int viewDefinitionId) throws SAXException {
		Env.setContext(ctx, X_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID, viewDefinitionId);
		viewDefinitionHandler.create(ctx, document);
		ctx.remove(X_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID);
	}

	private AttributesImpl createViewBinding(AttributesImpl atts, X_AD_View view) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, view);
		if (view.getAD_View_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_View.COLUMNNAME_AD_View_ID);
		}
		filler.addUUID();
		//	
		filler.add(I_AD_View.COLUMNNAME_Value);
		filler.add(I_AD_View.COLUMNNAME_Name);
		filler.add(I_AD_View.COLUMNNAME_Description);
		filler.add(I_AD_View.COLUMNNAME_Help);
		filler.add(I_AD_View.COLUMNNAME_EntityType);
		filler.add(I_AD_View.COLUMNNAME_IsActive);
		return atts;
	}
}
