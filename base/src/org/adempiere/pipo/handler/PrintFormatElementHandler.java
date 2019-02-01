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
 *                 Teo Sarca, SC ARHIPAC SERVICE SRL
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
import org.compiere.model.I_AD_PrintColor;
import org.compiere.model.I_AD_PrintFont;
import org.compiere.model.I_AD_PrintFormat;
import org.compiere.model.I_AD_PrintFormatItem;
import org.compiere.model.I_AD_PrintPaper;
import org.compiere.model.I_AD_PrintTableFormat;
import org.compiere.model.I_AD_ReportView;
import org.compiere.model.I_AD_Table;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.model.X_AD_PrintFormat;
import org.compiere.model.X_AD_PrintFormatItem;
import org.compiere.print.MPrintFormat;
import org.compiere.print.MPrintFormatItem;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *	@author Dixon Martinez, dmartinez@erpcya.com, ERPCyA http://www.erpcya.com
 *		<li>BR [1019] New Icon to export report definition is show only swing but not ZK https://github.com/adempiere/adempiere/issues/1019
 */
public class PrintFormatElementHandler extends AbstractElementHandler {

	private PrintFormatItemElementHandler itemHandler = new PrintFormatItemElementHandler();

	private List<Integer> formats = new ArrayList<Integer>();
	
	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		int backupId = -1;
		String objectStatus = null;
		Attributes atts = element.attributes;
		log.info(elementValue + " " + atts.getValue("Name"));
		String uuid = getUUIDValue(atts, I_AD_PrintFormat.Table_Name);
		log.info(elementValue + " " + uuid);
		int id = getIdFromUUID(ctx, I_AD_PrintFormat.Table_Name, uuid);
		X_AD_PrintFormat printFormat = new X_AD_PrintFormat(ctx, id, getTrxName(ctx));
		if (id <= 0 && getIntValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintFormat_ID) > 0 && getIntValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintFormat_ID) <= PackOut.MAX_OFFICIAL_ID) {
			printFormat.setAD_PrintFormat_ID(getIntValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintFormat_ID));
			printFormat.setIsDirectLoad(true);
		}
		if (id > 0) {
			backupId = copyRecord(ctx, "AD_PrintFormat", printFormat);
			objectStatus = "Update";
		} else {
			objectStatus = "New";
			backupId = 0;
		}
		printFormat.setUUID(uuid);
		// Table
		uuid = getUUIDValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_Table_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_Table.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormat.setAD_Table_ID(id);
		}
		// Report View
		uuid = getUUIDValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_ReportView_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_ReportView.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormat.setAD_ReportView_ID(id);
		}
		// Table Format
		uuid = getUUIDValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintTableFormat_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintTableFormat.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormat.setAD_PrintTableFormat_ID(id);
		}
		// Print Color
		uuid = getUUIDValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintColor_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintColor.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormat.setAD_PrintColor_ID(id);
		}
		// Print Font
		uuid = getUUIDValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintFont_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintFont.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormat.setAD_PrintFont_ID(id);
		}
		// Print Paper
		uuid = getUUIDValue(atts, I_AD_PrintFormat.COLUMNNAME_AD_PrintPaper_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintPaper.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormat.setAD_PrintPaper_ID(id);
		}
		//	Standard Attributes
		printFormat.setName(getStringValue(atts, I_AD_PrintFormat.COLUMNNAME_Name));
		printFormat.setPrinterName(getStringValue(atts, I_AD_PrintFormat.COLUMNNAME_PrinterName));
		printFormat.setDescription(getStringValue(atts, I_AD_PrintFormat.COLUMNNAME_Description));
		printFormat.setArgs(getStringValue(atts, I_AD_PrintFormat.COLUMNNAME_Args));
		printFormat.setClassname(getStringValue(atts, I_AD_PrintFormat.COLUMNNAME_Classname));
		printFormat.setFooterMargin(getIntValue(atts, I_AD_PrintFormat.COLUMNNAME_FooterMargin));
		printFormat.setHeaderMargin(getIntValue(atts, I_AD_PrintFormat.COLUMNNAME_HeaderMargin));
		printFormat.setIsActive(getBooleanValue(atts, I_AD_PrintFormat.COLUMNNAME_IsActive));
		printFormat.setIsDefault(getBooleanValue(atts, I_AD_PrintFormat.COLUMNNAME_IsDefault));
		printFormat.setIsPrintParameters(getBooleanValue(atts, I_AD_PrintFormat.COLUMNNAME_IsPrintParameters));
		printFormat.setIsStandardHeaderFooter(getBooleanValue(atts, I_AD_PrintFormat.COLUMNNAME_IsStandardHeaderFooter));
		printFormat.setIsSummary(getBooleanValue(atts, I_AD_PrintFormat.COLUMNNAME_IsSummary));
		printFormat.setIsTableBased(getBooleanValue(atts, I_AD_PrintFormat.COLUMNNAME_IsTableBased));
		//	Save
		try {
			printFormat.saveEx(getTrxName(ctx));
			recordLog(ctx, 1, printFormat.getUUID(), "PrintFormat",
					printFormat.get_ID(), backupId, objectStatus,
					"AD_PrintFormat", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_PrintFormat"));
			element.recordId = printFormat.getAD_PrintFormat_ID();
			deleteItems(printFormat.getAD_PrintFormat_ID());
		} catch (Exception e) {
			recordLog(ctx, 0, printFormat.getUUID(), "PrintFormat",
					printFormat.get_ID(), backupId, objectStatus,
					"AD_PrintFormat", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_PrintFormat"));
			throw new POSaveFailedException(e);
		}
	}
	
	/**
	 * Delete Items before import
	 * @param printFormatId
	 */
	private void deleteItems(int printFormatId) {
		List<MPrintFormatItem> printFormatItemList = new Query(Env.getCtx(), I_AD_PrintFormatItem.Table_Name, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormat_ID + " = ? ", null)
				.setParameters(printFormatId)
				.<MPrintFormatItem>list();
			//	For
			for(MPrintFormatItem printFormatItem : printFormatItemList) {
				printFormatItem.deleteEx(true);
			}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document)
			throws SAXException {
		int printFormatId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_PrintFormat_ID);
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		//	BR [1019]
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		if (formats.contains(printFormatId)) {
			return;
		}
		formats.add(printFormatId);
		AttributesImpl atts = new AttributesImpl();
		List<MPrintFormat> printFormatList = new Query(ctx, I_AD_PrintFormat.Table_Name, "AD_PrintFormat_ID = ? "
				+ "OR EXISTS(SELECT 1 FROM AD_PrintFormatItem pfi WHERE pfi.AD_PrintFormatChild_ID = AD_PrintFormat.AD_PrintFormat_ID AND pfi.AD_PrintFormat_ID = ?)", null)
			.setParameters(printFormatId, printFormatId)
			.<MPrintFormat>list();
		//	
		for(MPrintFormat printFormat : printFormatList) {
			//	Paper
			if (printFormat.getAD_PrintPaper_ID() > 0) {
				packOut.createPrintPaper(printFormat.getAD_PrintPaper_ID(), document);
			}
			//	
			createPrintFormatBinding(atts, printFormat);
			document.startElement("", "", "printformat", atts);
			List<MPrintFormatItem> printFormatItemList = new Query(ctx, I_AD_PrintFormatItem.Table_Name, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormat_ID + " = ? ", null)
				.setParameters(printFormatId)
				.<MPrintFormatItem>list();
			//	For
			for(MPrintFormatItem printFormatItem : printFormatItemList) {
				createItem(ctx, document, printFormatItem.getAD_PrintFormatItem_ID());
			}
			document.endElement("", "", "printformat");
		}
	}

	private void createItem(Properties ctx, TransformerHandler document,
			int AD_PrintFormatItem_ID) throws SAXException {
		Env.setContext(ctx,
				X_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID,
				AD_PrintFormatItem_ID);
		itemHandler.create(ctx, document);
		ctx.remove(X_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID);
	}

	private AttributesImpl createPrintFormatBinding(AttributesImpl atts, X_AD_PrintFormat printFormat) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, printFormat);
		if (printFormat.getAD_PrintFormat_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_PrintFormat_ID);
		}
		filler.addUUID();
		//	Table
		if (printFormat.getAD_Table_ID() > 0) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_Table_ID, true);
			filler.addUUID(I_AD_PrintFormat.COLUMNNAME_AD_Table_ID, getUUIDFromId(printFormat.getCtx(), I_AD_Table.Table_Name, printFormat.getAD_Table_ID()));
		}
		//	View
		if (printFormat.getAD_ReportView_ID() > 0) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_ReportView_ID, true);
			filler.addUUID(I_AD_PrintFormat.COLUMNNAME_AD_ReportView_ID, getUUIDFromId(printFormat.getCtx(), I_AD_ReportView.Table_Name, printFormat.getAD_ReportView_ID()));
		}
		//	Table Format
		if (printFormat.getAD_PrintTableFormat_ID() > 0) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_PrintTableFormat_ID, true);
			filler.addUUID(I_AD_PrintFormat.COLUMNNAME_AD_PrintTableFormat_ID, getUUIDFromId(printFormat.getCtx(), I_AD_PrintTableFormat.Table_Name, printFormat.getAD_PrintTableFormat_ID()));
		}
		//	Print Color
		if (printFormat.getAD_PrintColor_ID() > 0) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_PrintColor_ID, true);
			filler.addUUID(I_AD_PrintFormat.COLUMNNAME_AD_PrintColor_ID, getUUIDFromId(printFormat.getCtx(), I_AD_PrintColor.Table_Name, printFormat.getAD_PrintColor_ID()));
		}
		//	Print Font
		if (printFormat.getAD_PrintFont_ID() > 0) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_PrintFont_ID, true);
			filler.addUUID(I_AD_PrintFormat.COLUMNNAME_AD_PrintFont_ID, getUUIDFromId(printFormat.getCtx(), I_AD_PrintFont.Table_Name, printFormat.getAD_PrintFont_ID()));
		}
		//	Print Paper
		if (printFormat.getAD_PrintPaper_ID() > 0) {
			filler.add(I_AD_PrintFormat.COLUMNNAME_AD_PrintPaper_ID, true);
			filler.addUUID(I_AD_PrintFormat.COLUMNNAME_AD_PrintPaper_ID, getUUIDFromId(printFormat.getCtx(), I_AD_PrintPaper.Table_Name, printFormat.getAD_PrintPaper_ID()));
		}
		//	Standard Attributes
		filler.add(I_AD_PrintFormat.COLUMNNAME_Name);
		filler.add(I_AD_PrintFormat.COLUMNNAME_PrinterName);
		filler.add(I_AD_PrintFormat.COLUMNNAME_Description);
		filler.add(I_AD_PrintFormat.COLUMNNAME_Args);
		filler.add(I_AD_PrintFormat.COLUMNNAME_Classname);
		filler.add(I_AD_PrintFormat.COLUMNNAME_FooterMargin);
		filler.add(I_AD_PrintFormat.COLUMNNAME_HeaderMargin);
		filler.add(I_AD_PrintFormat.COLUMNNAME_IsActive);
		filler.add(I_AD_PrintFormat.COLUMNNAME_IsDefault);
		filler.add(I_AD_PrintFormat.COLUMNNAME_IsPrintParameters);
		filler.add(I_AD_PrintFormat.COLUMNNAME_IsStandardHeaderFooter);
		filler.add(I_AD_PrintFormat.COLUMNNAME_IsSummary);
		filler.add(I_AD_PrintFormat.COLUMNNAME_IsTableBased);
		return atts;
	}
}
