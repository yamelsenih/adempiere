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

import org.adempiere.model.I_AD_Browse;
import org.adempiere.model.I_AD_Browse_Field;
import org.adempiere.model.I_AD_View;
import org.adempiere.model.MBrowse;
import org.adempiere.model.MBrowseField;
import org.adempiere.model.X_AD_Browse;
import org.adempiere.model.X_AD_Browse_Field;
import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_Table;
import org.compiere.model.I_AD_Window;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * @author victor.perez@e-evoluton.com, www.e-evolution.com
 * 
 */
public class BrowseElementHandler extends AbstractElementHandler {

	private List<Integer> browses = new ArrayList<Integer>();
	private BrowseFieldElementHandler browseFieldHandler = new BrowseFieldElementHandler();

	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Browse.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Browse.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			int id = getIdFromUUID(ctx, I_AD_Browse.Table_Name, uuid);
			if (id > 0 && browses.contains(id)) {
				return;
			}
			//	Instance
			X_AD_Browse browse = new X_AD_Browse(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_Browse.COLUMNNAME_AD_Browse_ID) > 0 && getIntValue(atts, I_AD_Browse.COLUMNNAME_AD_Browse_ID) <= PackOut.MAX_OFFICIAL_ID) {
				browse.setAD_Browse_ID(getIntValue(atts, I_AD_Browse.COLUMNNAME_AD_Browse_ID));
				browse.setIsDirectLoad(true);
			}
			String objectStatus = null;
			int backupId = -1;
			if (id > 0) {
				backupId = copyRecord(ctx, "AD_Browse", browse);
				objectStatus = "Update";
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			browse.setUUID(uuid);
			//	Process
			uuid = getUUIDValue(atts, I_AD_Browse.COLUMNNAME_AD_Process_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_Process.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				browse.setAD_Process_ID(id);
			}
			//	Window
			uuid = getUUIDValue(atts, I_AD_Browse.COLUMNNAME_AD_Window_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_Window.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				browse.setAD_Window_ID(id);
			}
			//	View
			uuid = getUUIDValue(atts, I_AD_Browse.COLUMNNAME_AD_View_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_View.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				browse.setAD_View_ID(id);
			}
			//	Table
			uuid = getUUIDValue(atts, I_AD_Browse.COLUMNNAME_AD_Table_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_Table.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				browse.setAD_Table_ID(id);
			}
			browse.setValue(getStringValue(atts, I_AD_Browse.COLUMNNAME_Value));
			browse.setName(getStringValue(atts, I_AD_Browse.COLUMNNAME_Name));
			browse.setDescription(getStringValue(atts, I_AD_Browse.COLUMNNAME_Description));
			browse.setHelp(getStringValue(atts, I_AD_Browse.COLUMNNAME_Help));
			browse.setAccessLevel(getStringValue(atts, I_AD_Browse.COLUMNNAME_AccessLevel));
			browse.setEntityType(getStringValue(atts, I_AD_Browse.COLUMNNAME_EntityType));
			browse.setIsActive(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsActive));
			browse.setIsBetaFunctionality(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsBetaFunctionality));
			browse.setIsCollapsibleByDefault(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsCollapsibleByDefault));
			browse.setIsDeleteable(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsDeleteable));
			browse.setIsExecutedQueryByDefault(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsExecutedQueryByDefault));
			browse.setIsSelectedByDefault(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsSelectedByDefault));
			browse.setIsShowTotal(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsShowTotal));
			browse.setIsUpdateable(getBooleanValue(atts, I_AD_Browse.COLUMNNAME_IsUpdateable));
			browse.setWhereClause(getStringValue(atts, I_AD_Browse.COLUMNNAME_WhereClause));
			//	Save
			try {
				browse.saveEx(getTrxName(ctx));
				recordLog(
						ctx,
						1,
						browse.getUUID(),
						"Browse",
						browse.get_ID(),
						backupId,
						objectStatus,
						"AD_Browse",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Browse"));
				element.recordId = browse.getAD_Browse_ID();
				browses.add(browse.getAD_Browse_ID());
			} catch (Exception e) {
				recordLog(
						ctx,
						0,
						browse.getUUID(),
						"Browse",
						browse.get_ID(),
						backupId,
						objectStatus,
						"AD_Browse",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Browse"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int browseId = Env.getContextAsInt(ctx, "AD_Browse_ID");
		if (browses.contains(browseId)) {
			return;
		}
		browses.add(browseId);
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		MBrowse browse = new MBrowse(ctx, browseId, null);
		AttributesImpl atts = new AttributesImpl();

		packOut.createView(browse.getAD_View_ID(), document);
		packOut.createProcess(browse.getAD_Process_ID(), document);
		createBrowseBinding(atts, browse);
		document.startElement("", "", "browse", atts);
		// Tab Tag
		StringBuilder whereClause = new StringBuilder(I_AD_Browse_Field.COLUMNNAME_AD_Browse_ID).append("=?");
		List<MBrowseField> browseFields = new Query(ctx,
				I_AD_Browse_Field.Table_Name, whereClause.toString(),
				getTrxName(ctx)).setParameters(browse.getAD_Browse_ID())
				.list();
		//	
		for (MBrowseField bf : browseFields) {
			createBrowseField(ctx, document, bf.getAD_Browse_Field_ID());
		}
		// Loop tags.
		document.endElement("", "", "browse");

	}

	private void createBrowseField(Properties ctx, TransformerHandler document, int browseFieldId) throws SAXException {
		Env.setContext(ctx, X_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID, browseFieldId);
		browseFieldHandler.create(ctx, document);
		ctx.remove(X_AD_Browse_Field.COLUMNNAME_AD_Browse_Field_ID);
	}

	private AttributesImpl createBrowseBinding(AttributesImpl atts, X_AD_Browse browse) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, browse);
		if (browse.getAD_Browse_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Browse.COLUMNNAME_AD_Browse_ID);
		}
		filler.addUUID();
		//	Window
		if (browse.getAD_Window_ID() > 0) {
			filler.add(I_AD_Browse.COLUMNNAME_AD_Window_ID, true);
			filler.addUUID(I_AD_Browse.COLUMNNAME_AD_Window_ID, getUUIDFromId(browse.getCtx(), I_AD_Window.Table_Name, browse.getAD_Window_ID()));
		}
		//	Process
		if (browse.getAD_Process_ID() > 0) {
			filler.add(I_AD_Browse.COLUMNNAME_AD_Process_ID, true);
			filler.addUUID(I_AD_Browse.COLUMNNAME_AD_Process_ID, getUUIDFromId(browse.getCtx(), I_AD_Process.Table_Name, browse.getAD_Process_ID()));
		}
		//	View
		if (browse.getAD_View_ID() > 0) {
			filler.add(I_AD_Browse.COLUMNNAME_AD_View_ID, true);
			filler.addUUID(I_AD_Browse.COLUMNNAME_AD_View_ID, getUUIDFromId(browse.getCtx(), I_AD_View.Table_Name, browse.getAD_View_ID()));
		}
		//	Table
		if (browse.getAD_Table_ID() > 0) {
			filler.add(I_AD_Browse.COLUMNNAME_AD_Table_ID, true);
			filler.addUUID(I_AD_Browse.COLUMNNAME_AD_Table_ID, getUUIDFromId(browse.getCtx(), I_AD_Table.Table_Name, browse.getAD_Table_ID()));
		}
		//	Attributes
		filler.add(I_AD_Browse.COLUMNNAME_Value);
		filler.add(I_AD_Browse.COLUMNNAME_Name);
		filler.add(I_AD_Browse.COLUMNNAME_Description);
		filler.add(I_AD_Browse.COLUMNNAME_Help);
		filler.add(I_AD_Browse.COLUMNNAME_AccessLevel);
		filler.add(I_AD_Browse.COLUMNNAME_EntityType);
		filler.add(I_AD_Browse.COLUMNNAME_IsActive);
		filler.add(I_AD_Browse.COLUMNNAME_IsBetaFunctionality);
		filler.add(I_AD_Browse.COLUMNNAME_IsCollapsibleByDefault);
		filler.add(I_AD_Browse.COLUMNNAME_IsDeleteable);
		filler.add(I_AD_Browse.COLUMNNAME_IsExecutedQueryByDefault);
		filler.add(I_AD_Browse.COLUMNNAME_IsSelectedByDefault);
		filler.add(I_AD_Browse.COLUMNNAME_IsShowTotal);
		filler.add(I_AD_Browse.COLUMNNAME_IsUpdateable);
		filler.add(I_AD_Browse.COLUMNNAME_WhereClause);
		return atts;
	}

	@Override
	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}
}
