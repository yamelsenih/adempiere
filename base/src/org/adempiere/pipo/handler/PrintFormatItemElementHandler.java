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

import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_PrintColor;
import org.compiere.model.I_AD_PrintFont;
import org.compiere.model.I_AD_PrintFormat;
import org.compiere.model.I_AD_PrintFormatItem;
import org.compiere.model.I_AD_PrintGraph;
import org.compiere.model.X_AD_PrintFormatItem;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PrintFormatItemElementHandler extends AbstractElementHandler {

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		String elementValue = element.getElementValue();
		int backupId = -1;
		String objectStatus = null;
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_PrintFormatItem.Table_Name);
		log.info(elementValue + " " + uuid);
		if (element.parent != null && element.parent.getElementValue().equals("printformat") &&
			element.parent.defer) {
			element.defer = true;
			return;
		}
		//	
		int id = getIdFromUUID(ctx, I_AD_PrintFormatItem.Table_Name, uuid);
		X_AD_PrintFormatItem printFormatItem = new X_AD_PrintFormatItem(ctx, id, getTrxName(ctx));
		if (id <= 0 && getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID) > 0 && getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID) <= PackOut.MAX_OFFICIAL_ID) {
			printFormatItem.setAD_PrintFormatItem_ID(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID));
			printFormatItem.setIsDirectLoad(true);
		}
		if (id > 0) {
			backupId = copyRecord(ctx, "AD_PrintFormatItem",
					printFormatItem);
			objectStatus = "Update";
		} else {
			objectStatus = "New";
			backupId = 0;
		}
		printFormatItem.setUUID(uuid);
		// Print Format
		uuid = getUUIDValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormat_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintFormat.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormatItem.setAD_PrintFormat_ID(id);
		}
		// Print Format Child
		uuid = getUUIDValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatChild_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintFormat.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormatItem.setAD_PrintFormatChild_ID(id);
		}
		// Column
		uuid = getUUIDValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_Column_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_Column.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormatItem.setAD_Column_ID(id);
		}
		// Print Color
		uuid = getUUIDValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintColor_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintColor.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormatItem.setAD_PrintColor_ID(id);
		}
		// Print Font
		uuid = getUUIDValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFont_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintFont.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormatItem.setAD_PrintFont_ID(id);
		}
		// Print Graph
		uuid = getUUIDValue(atts, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintGraph_ID);
		if (!Util.isEmpty(uuid)) {
			id = getIdFromUUID(ctx, I_AD_PrintGraph.Table_Name, uuid);
			if (id <= 0) {
				element.defer = true;
				return;
			}
			printFormatItem.setAD_PrintGraph_ID(id);
		}
		//	Standard Attributes
		printFormatItem.setArcDiameter(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_ArcDiameter));
		printFormatItem.setBarcodeType(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_BarcodeType));
		printFormatItem.setBelowColumn(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_BelowColumn));
		printFormatItem.setDisplayLogic(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_DisplayLogic));
		printFormatItem.setFieldAlignmentType(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_FieldAlignmentType));
		printFormatItem.setFormatPattern(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_FormatPattern));
		printFormatItem.setImageIsAttached(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_ImageIsAttached));
		printFormatItem.setImageURL(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_ImageURL));
		printFormatItem.setIsActive(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsActive));
		printFormatItem.setIsAveraged(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsAveraged));
		printFormatItem.setIsCentrallyMaintained(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsCentrallyMaintained));
		printFormatItem.setIsCounted(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsCounted));
		printFormatItem.setIsDesc(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsDesc));
		printFormatItem.setIsDeviationCalc(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsDeviationCalc));
		printFormatItem.setIsFilledRectangle(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsFilledRectangle));
		printFormatItem.setIsFixedWidth(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsFixedWidth));
		printFormatItem.setIsGroupBy(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsGroupBy));
		printFormatItem.setIsHeightOneLine(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsHeightOneLine));
		printFormatItem.setIsImageField(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsImageField));
		printFormatItem.setIsMaxCalc(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsMaxCalc));
		printFormatItem.setIsMinCalc(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsMinCalc));
		printFormatItem.setIsNextLine(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsNextLine));
		printFormatItem.setIsNextPage(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsNextPage));
		printFormatItem.setIsOrderBy(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsOrderBy));
		printFormatItem.setIsPageBreak(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsPageBreak));
		printFormatItem.setIsPrintBarcodeText(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsPrintBarcodeText));
		printFormatItem.setIsPrinted(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsPrinted));
		printFormatItem.setIsRelativePosition(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsRelativePosition));
		printFormatItem.setIsRunningTotal(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsRunningTotal));
		printFormatItem.setIsSetNLPosition(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsSetNLPosition));
		printFormatItem.setIsSummarized(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsSummarized));
		printFormatItem.setIsSuppressNull(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsSuppressNull));
		printFormatItem.setIsSuppressRepeats(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsSuppressRepeats));
		printFormatItem.setIsVarianceCalc(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_IsVarianceCalc));
		printFormatItem.setImageIsAttached(getBooleanValue(atts, I_AD_PrintFormatItem.COLUMNNAME_ImageIsAttached));
		printFormatItem.setLineAlignmentType(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_LineAlignmentType));
		printFormatItem.setLineWidth(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_LineWidth));
		printFormatItem.setMaxHeight(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_MaxHeight));
		printFormatItem.setMaxWidth(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_MaxWidth));
		printFormatItem.setName(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_Name));
		printFormatItem.setPrintAreaType(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_PrintAreaType));
		printFormatItem.setPrintFormatType(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_PrintFormatType));
		printFormatItem.setPrintName(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_PrintName));
		printFormatItem.setPrintNameSuffix(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_PrintNameSuffix));
		printFormatItem.setSeqNo(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_SeqNo));
		printFormatItem.setShapeType(getStringValue(atts, I_AD_PrintFormatItem.COLUMNNAME_ShapeType));
		printFormatItem.setSortNo(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_SortNo));
		printFormatItem.setXPosition(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_XPosition));
		printFormatItem.setXSpace(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_XSpace));
		printFormatItem.setYPosition(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_YPosition));
		printFormatItem.setYSpace(getIntValue(atts, I_AD_PrintFormatItem.COLUMNNAME_YSpace));
		//	Save
		try {
			printFormatItem.saveEx(getTrxName(ctx));
			recordLog(ctx, 1, printFormatItem.getUUID(), "PrintFormatItem",
					printFormatItem.get_ID(), backupId, objectStatus,
					"AD_PrintFormatItem", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_PrintFormatItem"));
		} catch (Exception e) {
			recordLog(ctx, 0, printFormatItem.getUUID(), "PrintFormatItem",
					printFormatItem.get_ID(), backupId, objectStatus,
					"AD_PrintFormatItem", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_PrintFormatItem"));
			throw new POSaveFailedException(e);
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int printFormatItemId = Env.getContextAsInt(ctx, X_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID);
		X_AD_PrintFormatItem printFormatItem = new X_AD_PrintFormatItem(ctx, printFormatItemId, null);
		AttributesImpl atts = new AttributesImpl();
		createPrintFormatItemBinding(atts, printFormatItem);
		document.startElement("", "", "printformatitem", atts);
		document.endElement("", "", "printformatitem");
	}

	private AttributesImpl createPrintFormatItemBinding(AttributesImpl atts, X_AD_PrintFormatItem printFormatItem) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, printFormatItem);
		if (printFormatItem.getAD_PrintFormatItem_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatItem_ID);
		}
		filler.addUUID();
		//	Print Format
		if (printFormatItem.getAD_PrintFormat_ID() > 0) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormat_ID, true);
			filler.addUUID(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormat_ID, getUUIDFromId(printFormatItem.getCtx(), I_AD_PrintFormat.Table_Name, printFormatItem.getAD_PrintFormat_ID()));
		}
		//	Print Format
		if (printFormatItem.getAD_PrintFormatChild_ID() > 0) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatChild_ID, true);
			filler.addUUID(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormatChild_ID, getUUIDFromId(printFormatItem.getCtx(), I_AD_PrintFormat.Table_Name, printFormatItem.getAD_PrintFormatChild_ID()));
		}
		//	Print Format
		if (printFormatItem.getAD_Column_ID() > 0) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_Column_ID, true);
			filler.addUUID(I_AD_PrintFormatItem.COLUMNNAME_AD_Column_ID, getUUIDFromId(printFormatItem.getCtx(), I_AD_Column.Table_Name, printFormatItem.getAD_Column_ID()));
		}
		//	Print Color
		if (printFormatItem.getAD_PrintColor_ID() > 0) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintColor_ID, true);
			filler.addUUID(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintColor_ID, getUUIDFromId(printFormatItem.getCtx(), I_AD_PrintColor.Table_Name, printFormatItem.getAD_PrintColor_ID()));
		}
		//	Print Font
		if (printFormatItem.getAD_PrintFont_ID() > 0) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFont_ID, true);
			filler.addUUID(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFont_ID, getUUIDFromId(printFormatItem.getCtx(), I_AD_PrintFont.Table_Name, printFormatItem.getAD_PrintFont_ID()));
		}
		//	Print Graph
		if (printFormatItem.getAD_PrintGraph_ID() > 0) {
			filler.add(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintGraph_ID, true);
			filler.addUUID(I_AD_PrintFormatItem.COLUMNNAME_AD_PrintGraph_ID, getUUIDFromId(printFormatItem.getCtx(), I_AD_PrintGraph.Table_Name, printFormatItem.getAD_PrintGraph_ID()));
		}
		//	Standard Attributes
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_ArcDiameter);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_BarcodeType);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_BelowColumn);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_DisplayLogic);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_FieldAlignmentType);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_FormatPattern);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_ImageIsAttached);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_ImageURL);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsActive);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsAveraged);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsCentrallyMaintained);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsCounted);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsDesc);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsDeviationCalc);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsFilledRectangle);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsFixedWidth);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsGroupBy);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsHeightOneLine);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsImageField);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsMaxCalc);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsMinCalc);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsNextLine);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsNextPage);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsOrderBy);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsPageBreak);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsPrintBarcodeText);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsPrinted);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsRelativePosition);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsRunningTotal);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsSetNLPosition);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsSummarized);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsSuppressNull);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsSuppressRepeats);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_IsVarianceCalc);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_ImageIsAttached);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_LineAlignmentType);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_LineWidth);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_MaxHeight);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_MaxWidth);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_Name);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_PrintAreaType);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_PrintFormatType);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_PrintName);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_PrintNameSuffix);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_SeqNo);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_ShapeType);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_SortNo);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_XPosition);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_XSpace);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_YPosition);
		filler.add(I_AD_PrintFormatItem.COLUMNNAME_YSpace);
		return atts;
	}
}
