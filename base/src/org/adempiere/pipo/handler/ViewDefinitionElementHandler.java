/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2003-2012 e-Evolution Consultants. All Rights Reserved.      *
 * Copyright (C) 2003-2012 Victor Pérez Juárez 								  * 
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Contributor(s): Victor Pérez Juárez  (victor.perez@e-evolution.com)		  *
 * Sponsors: e-Evolution Consultants (http://www.e-evolution.com/)            *
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_View;
import org.adempiere.model.I_AD_View_Column;
import org.adempiere.model.I_AD_View_Definition;
import org.adempiere.model.MViewColumn;
import org.adempiere.model.MViewDefinition;
import org.adempiere.model.X_AD_View_Column;
import org.adempiere.model.X_AD_View_Definition;
import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Table;
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
public class ViewDefinitionElementHandler extends AbstractElementHandler {
	private ViewColumnElementHandler viewColumnHandler = new ViewColumnElementHandler();

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_View_Definition.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_View.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			if (element.parent != null
					&& element.parent.getElementValue().equals("view")
					&& element.parent.defer) {
				element.defer = true;
				return;
			}
			String tableUuid = getUUIDValue(atts, I_AD_View_Definition.COLUMNNAME_AD_Table_ID);
			int tableid = getIdFromUUID(ctx, I_AD_Table.Table_Name, tableUuid);
			if (tableid <= 0) {
				element.defer = true;
				return;
			}
			
			int viewid = 0;
			if (element.parent != null
					&& element.parent.getElementValue().equals("view")
					&& element.parent.recordId > 0) {
				viewid = element.parent.recordId;
			} else {
				String viewUuid = getUUIDValue(atts, I_AD_View_Definition.COLUMNNAME_AD_View_ID);
				viewid = getIdFromUUID(ctx, I_AD_View.Table_Name, viewUuid);
				if (element.parent != null
						&& element.parent.getElementValue().equals("view")
						&& viewid > 0) {
					element.parent.recordId = viewid;
				}
			}
			if (viewid <= 0) {
				element.defer = true;
				return;
			}
			int id = getIdFromUUID(ctx, I_AD_View_Definition.Table_Name, uuid);
			X_AD_View_Definition viewDefinition = new X_AD_View_Definition(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID) > 0 && getIntValue(atts, I_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID) <= PackOut.MAX_OFFICIAL_ID) {
				viewDefinition.setAD_View_Definition_ID(getIntValue(atts, I_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID));
				viewDefinition.setIsDirectLoad(true);
			}
			int backupId = -1;
			String objectStatus = null;
			if (id > 0) {
				backupId = copyRecord(ctx, "AD_View_Definition",
						viewDefinition);
				objectStatus = "Update";
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			viewDefinition.setUUID(uuid);
			// View
			uuid = getUUIDValue(atts, I_AD_View_Definition.COLUMNNAME_AD_View_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_View.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				viewDefinition.setAD_View_ID(id);
			}
			// Table
			uuid = getUUIDValue(atts, I_AD_View_Definition.COLUMNNAME_AD_Table_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_Table.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				viewDefinition.setAD_Table_ID(id);
			}
			//	Standard Attributes
			viewDefinition.setSeqNo(getIntValue(atts, I_AD_View_Definition.COLUMNNAME_SeqNo));
			viewDefinition.setTableAlias(getStringValue(atts, I_AD_View_Definition.COLUMNNAME_TableAlias));
			viewDefinition.setJoinClause(getStringValue(atts, I_AD_View_Definition.COLUMNNAME_JoinClause));
			viewDefinition.setIsActive(getBooleanValue(atts, I_AD_View_Definition.COLUMNNAME_IsActive));
			//	Save
			try {
				viewDefinition.saveEx(getTrxName(ctx));
				recordLog(
						ctx,
						1,
						viewDefinition.getUUID(),
						"ViewDefinition",
						viewDefinition.get_ID(),
						backupId,
						objectStatus,
						"AD_View_Definition",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_View_Definition"));
				element.recordId = viewDefinition.getAD_View_Definition_ID();
			} catch (Exception e) {
				recordLog(
						ctx,
						0,
						viewDefinition.getUUID(),
						"ViewDefinition",
						viewDefinition.get_ID(),
						backupId,
						objectStatus,
						"AD_View_Definition",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_View_Definition"));
				throw new POSaveFailedException("ViewDefinition");
			}
		} else {
			element.skip = true;
		}

	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int viewDefinitionId = Env.getContextAsInt(ctx, X_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID);
		MViewDefinition viewDefinition = new MViewDefinition(ctx, viewDefinitionId, getTrxName(ctx));
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		AttributesImpl atts = new AttributesImpl();
		createViewDefinitionBinding(atts, viewDefinition);
		document.startElement("", "", "viewdefinition", atts);
		//	Create Table before
		if(viewDefinition.getAD_Table_ID() > 0) {
			packOut.createTable(viewDefinition.getAD_Table_ID(), document);
		}
		// View Columns tags.
		StringBuilder whereClause = new StringBuilder(I_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID).append("=?");
		List<MViewColumn> viewColumns = new Query(ctx,
				I_AD_View_Column.Table_Name, whereClause.toString(),
				getTrxName(ctx)).setParameters(viewDefinition.get_ID())
				.list();
		for (MViewColumn vc : viewColumns) {
			createViewColumn(ctx, document, vc.getAD_View_Column_ID());
		}
		document.endElement("", "", "viewdefinition");
	}

	private void createViewColumn(Properties ctx, TransformerHandler document, int viewColumnId) throws SAXException {
		Env.setContext(ctx, X_AD_View_Column.COLUMNNAME_AD_View_Column_ID, viewColumnId);
		viewColumnHandler.create(ctx, document);
		ctx.remove(X_AD_View_Column.COLUMNNAME_AD_View_Column_ID);
	}

	private AttributesImpl createViewDefinitionBinding(AttributesImpl atts, X_AD_View_Definition viewDefinition) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, viewDefinition);
		if (viewDefinition.getAD_View_Definition_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_View_Definition.COLUMNNAME_AD_View_Definition_ID);
		}
		filler.addUUID();
		//	View
		if(viewDefinition.getAD_View_ID() > 0) {
			filler.add(I_AD_View_Definition.COLUMNNAME_AD_View_ID, true);
			filler.addUUID(I_AD_View_Definition.COLUMNNAME_AD_View_ID, getUUIDFromId(viewDefinition.getCtx(), I_AD_View.Table_Name, viewDefinition.getAD_View_ID()));
			//	Add Entity Type
			filler.addString(I_AD_View.COLUMNNAME_EntityType, viewDefinition.getAD_View().getEntityType());
		}
		//	Table
		if(viewDefinition.getAD_Table_ID() > 0) {
			filler.add(I_AD_View_Definition.COLUMNNAME_AD_Table_ID, true);
			filler.addUUID(I_AD_View_Definition.COLUMNNAME_AD_Table_ID, getUUIDFromId(viewDefinition.getCtx(), I_AD_Table.Table_Name, viewDefinition.getAD_Table_ID()));
		}
		//	Attributes
		filler.add(I_AD_View_Definition.COLUMNNAME_SeqNo);
		filler.add(I_AD_View_Definition.COLUMNNAME_TableAlias);
		filler.add(I_AD_View_Definition.COLUMNNAME_JoinClause);
		filler.add(I_AD_View_Definition.COLUMNNAME_IsActive);
		return atts;
	}

}
