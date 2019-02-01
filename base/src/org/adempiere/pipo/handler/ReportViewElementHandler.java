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
import org.compiere.model.I_AD_PrintFormat;
import org.compiere.model.I_AD_ReportView;
import org.compiere.model.I_AD_ReportView_Col;
import org.compiere.model.I_AD_Table;
import org.compiere.model.MReportView;
import org.compiere.model.Query;
import org.compiere.model.X_AD_ReportView;
import org.compiere.model.X_AD_ReportView_Col;
import org.compiere.print.MPrintFormat;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ReportViewElementHandler extends AbstractElementHandler {

	private ReportViewColElementHandler columnHandler = new ReportViewColElementHandler();

	private List<Integer> views = new ArrayList<Integer>();
	
	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		int backupId = -1;
		String objectStatus = null;
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_ReportView.Table_Name);
		log.info(elementValue + " " + uuid);
		int id = getIdFromUUID(ctx, I_AD_ReportView.Table_Name, uuid);
		X_AD_ReportView reportView = new X_AD_ReportView(ctx, id, getTrxName(ctx));
		if (id <= 0 && getIntValue(atts, I_AD_ReportView.COLUMNNAME_AD_ReportView_ID) > 0 && getIntValue(atts, I_AD_ReportView.COLUMNNAME_AD_ReportView_ID) <= PackOut.MAX_OFFICIAL_ID) {
			reportView.setAD_ReportView_ID(getIntValue(atts, I_AD_ReportView.COLUMNNAME_AD_ReportView_ID));
			reportView.setIsDirectLoad(true);
		}
		if (id > 0) {
			backupId = copyRecord(ctx, "AD_Reportview", reportView);
			objectStatus = "Update";
		} else {
			objectStatus = "New";
			backupId = 0;
		}
		reportView.setUUID(uuid);
		// Table
		uuid = getUUIDValue(atts, I_AD_ReportView.COLUMNNAME_AD_Table_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_Table.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			reportView.setAD_Table_ID(id);
		}
		//	Standard Attributes
		reportView.setName(getStringValue(atts, I_AD_ReportView.COLUMNNAME_Name));
		reportView.setPrintName(getStringValue(atts, I_AD_ReportView.COLUMNNAME_PrintName));
		reportView.setDescription(getStringValue(atts, I_AD_ReportView.COLUMNNAME_Description));
		reportView.setClassname(getStringValue(atts, I_AD_ReportView.COLUMNNAME_Classname));
		reportView.setEntityType(getStringValue(atts, I_AD_ReportView.COLUMNNAME_EntityType));
		reportView.setIsActive(getBooleanValue(atts, I_AD_ReportView.COLUMNNAME_IsActive));
		reportView.setIsCentrallyMaintained(getBooleanValue(atts, I_AD_ReportView.COLUMNNAME_IsCentrallyMaintained));
		reportView.setOrderByClause(getStringValue(atts, I_AD_ReportView.COLUMNNAME_OrderByClause));
		reportView.setWhereClause(getStringValue(atts, I_AD_ReportView.COLUMNNAME_WhereClause));
		//	Save
		try {
			reportView.saveEx(getTrxName(ctx));
			recordLog(ctx, 1, reportView.getUUID(), "Reportview",
					reportView.get_ID(), backupId, objectStatus,
					"AD_Reportview", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_Reportview"));
			element.recordId = reportView.getAD_ReportView_ID();
		} catch (Exception e) {
			recordLog(ctx, 0, reportView.getUUID(), "Reportview",
					reportView.get_ID(), backupId, objectStatus,
					"AD_Reportview", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_Reportview"));
			throw new POSaveFailedException(e);
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		int reportViewId = Env.getContextAsInt(ctx, "AD_ReportView_ID");
		if (views.contains(reportViewId)) {
			return;
		}
		views.add(reportViewId);
		MReportView eportView = new MReportView(ctx, reportViewId, null);
		AttributesImpl atts = new AttributesImpl();
		atts = createReportViewBinding(atts, eportView);
		document.startElement("", "", "reportview", atts);
		document.endElement("", "", "reportview");
		// Export Table if necessary
		packOut.createTable(eportView.getAD_Table_ID(), document);
		//	Get all columns
		List<MPrintFormat> printFormatList = new Query(ctx, I_AD_PrintFormat.Table_Name, I_AD_PrintFormat.COLUMNNAME_AD_ReportView_ID + " = ?", null)
			.setParameters(reportViewId)
			.setClient_ID()
			.<MPrintFormat>list();
		//	All
		for(MPrintFormat printFormat : printFormatList) {
			packOut.createPrintFormat(printFormat.getAD_PrintFormat_ID(), document);
		}
		//	Get all columns
		List<X_AD_ReportView_Col> reportViewColumnList = new Query(ctx, I_AD_ReportView_Col.Table_Name, I_AD_ReportView_Col.COLUMNNAME_AD_ReportView_ID + " = ?", null)
			.setParameters(reportViewId)
			.setClient_ID()
			.<X_AD_ReportView_Col>list();
		for(X_AD_ReportView_Col reportViewColumn : reportViewColumnList) {
			createReportViewCol(ctx, document, reportViewColumn.getAD_ReportView_Col_ID());
		}
	}

	private void createReportViewCol(Properties ctx, TransformerHandler document, int reportViewColId) throws SAXException {
		Env.setContext(ctx, X_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID, reportViewColId);
		columnHandler.create(ctx, document);
		ctx.remove(X_AD_ReportView_Col.COLUMNNAME_AD_ReportView_Col_ID);
	}

	private AttributesImpl createReportViewBinding(AttributesImpl atts, X_AD_ReportView reportView) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, reportView);
		if (reportView.getAD_ReportView_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_ReportView.COLUMNNAME_AD_ReportView_ID);
		}
		filler.addUUID();
		if (reportView.getAD_Table_ID() > 0) {
			filler.add(I_AD_ReportView.COLUMNNAME_AD_Table_ID, true);
			filler.addUUID(I_AD_ReportView.COLUMNNAME_AD_Table_ID, getUUIDFromId(reportView.getCtx(), I_AD_Table.Table_Name, reportView.getAD_Table_ID()));
		}
		//	Standard Attributes
		filler.add(I_AD_ReportView.COLUMNNAME_Name);
		filler.add(I_AD_ReportView.COLUMNNAME_PrintName);
		filler.add(I_AD_ReportView.COLUMNNAME_Description);
		filler.add(I_AD_ReportView.COLUMNNAME_Classname);
		filler.add(I_AD_ReportView.COLUMNNAME_EntityType);
		filler.add(I_AD_ReportView.COLUMNNAME_IsActive);
		filler.add(I_AD_ReportView.COLUMNNAME_IsCentrallyMaintained);
		filler.add(I_AD_ReportView.COLUMNNAME_OrderByClause);
		filler.add(I_AD_ReportView.COLUMNNAME_WhereClause);
		return atts;
	}
}
