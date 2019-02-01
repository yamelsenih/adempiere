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

import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_ReportView;
import org.compiere.model.I_AD_ReportView_Col;
import org.compiere.model.X_AD_ReportView_Col;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ReportViewColElementHandler extends AbstractElementHandler {

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		int backupId = -1;
		String objectStatus = null;
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_ReportView_Col.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_ReportView.COLUMNNAME_EntityType);
		if (isProcessElement(ctx, entitytype)) {
			String reportViewUuid = getUUIDValue(atts, I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_ID);
			int reportViewId = 0;
			if (element.parent != null && element.parent.getElementValue().equals("reportview") &&
				element.parent.recordId > 0) {
				reportViewId = element.parent.recordId;
			} else {
				reportViewId = getIdFromUUID(ctx, I_AD_ReportView.Table_Name, reportViewUuid);
			}
			if (reportViewId <= 0) {
				element.defer = true;
				return;
			}
			
			String columnUuid = getUUIDValue(atts, I_AD_ReportView_Col.COLUMNNAME_AD_Column_ID);
			int columnId = 0;
			if (!Util.isEmpty(columnUuid)) {
				columnId = getIdFromUUID(ctx, I_AD_Column.Table_Name, columnUuid);
				if (columnId <= 0) {
					element.defer = true;
					return;
				}
			}
			int id = getIdFromUUID(ctx, I_AD_ReportView_Col.Table_Name, uuid);
			X_AD_ReportView_Col reportViewColumn = new X_AD_ReportView_Col(ctx, id, getTrxName(ctx));
			if (id <= 0 && getIntValue(atts, I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID) > 0 && getIntValue(atts, I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID) <= PackOut.MAX_OFFICIAL_ID) {
				reportViewColumn.setAD_ReportView_Col_ID(getIntValue(atts, I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID));
				reportViewColumn.setIsDirectLoad(true);
			}
			if (id > 0) {
				backupId = copyRecord(ctx, "AD_Reportview_Col",
						reportViewColumn);
				objectStatus = "Update";
			} else {
				objectStatus = "New";
				backupId = 0;
			}
			//	Set UUID
			reportViewColumn.setUUID(uuid);
			//	
			//	Column
			uuid = getUUIDValue(atts, I_AD_ReportView_Col.COLUMNNAME_AD_Column_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				reportViewColumn.setAD_Column_ID(id);
			}
			//	Report View
			reportViewColumn.setAD_ReportView_ID(reportViewId);
			reportViewColumn.setFunctionColumn(getStringValue(atts, I_AD_ReportView_Col.COLUMNNAME_FunctionColumn));
			reportViewColumn.setIsGroupFunction(getBooleanValue(atts, I_AD_ReportView_Col.COLUMNNAME_IsActive));
			reportViewColumn.setIsGroupFunction(getBooleanValue(atts, I_AD_ReportView_Col.COLUMNNAME_IsGroupFunction));
			try {
				reportViewColumn.saveEx(getTrxName(ctx));
				recordLog(ctx, 1, reportViewColumn.getUUID(),
						"Reportview_Col", reportViewColumn.get_ID(),
						backupId, objectStatus, "AD_Reportview_Col",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Reportview_Col"));
			} catch (Exception e) {
				recordLog(ctx, 0, reportViewColumn.getUUID(),
						"Reportview_Col", reportViewColumn.get_ID(),
						backupId, objectStatus, "AD_Reportview_Col",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Reportview_Col"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int reportViewColId = Env.getContextAsInt(ctx, X_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID);
		X_AD_ReportView_Col reportviewCol = new X_AD_ReportView_Col(ctx, reportViewColId, getTrxName(ctx));
		AttributesImpl atts = new AttributesImpl();
		createReportViewColBinding(atts, reportviewCol);
		document.startElement("", "", "reportviewcol", atts);
		document.endElement("", "", "reportviewcol");
	}

	private AttributesImpl createReportViewColBinding(AttributesImpl atts, X_AD_ReportView_Col reportviewCol) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, reportviewCol);
		if (reportviewCol.getAD_ReportView_Col_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID);
		}
		filler.addUUID();
		//	Report View
		if(reportviewCol.getAD_ReportView_ID() > 0) {
			filler.add(I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_ID, true);
			filler.addUUID(I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_ID, getUUIDFromId(reportviewCol.getCtx(), I_AD_ReportView.Table_Name, reportviewCol.getAD_ReportView_ID()));
			filler.addString(I_AD_ReportView.COLUMNNAME_EntityType, reportviewCol.getAD_ReportView().getEntityType());
		}
		//	Column
		if(reportviewCol.getAD_Column_ID() > 0) {
			filler.add(I_AD_ReportView_Col.COLUMNNAME_AD_Column_ID, true);
			filler.addUUID(I_AD_ReportView_Col.COLUMNNAME_AD_Column_ID, getUUIDFromId(reportviewCol.getCtx(), I_AD_Column.Table_Name, reportviewCol.getAD_Column_ID()));
		}
		//	Fields
		filler.add(I_AD_ReportView_Col.COLUMNNAME_FunctionColumn);
		filler.add(I_AD_ReportView_Col.COLUMNNAME_IsActive);
		filler.add(I_AD_ReportView_Col.COLUMNNAME_IsGroupFunction);
		return atts;
	}
}
