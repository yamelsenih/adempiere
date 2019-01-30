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
 * Contributor(s): Low Heng Sin hengsin@avantz.com                            *
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
import org.compiere.model.I_AD_Color;
import org.compiere.model.I_AD_Image;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MTab;
import org.compiere.model.MWindow;
import org.compiere.model.X_AD_Tab;
import org.compiere.model.X_AD_Window;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class WindowElementHandler extends AbstractElementHandler {

	private TabElementHandler tabHandler = new TabElementHandler();
	
	private List<Integer> windows = new ArrayList<Integer>();

	public void startElement(Properties ctx, Element element)
			throws SAXException {
		// Check namespace.
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Window.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = atts.getValue("EntityType");
		if (isProcessElement(ctx, entitytype)) {
			int windowId = getIdWithFromUUID(ctx, I_AD_Window.Table_Name, uuid);
			if (windowId > 0 && windows.contains(windowId)) {
				return;
			}
			X_AD_Window window = new X_AD_Window(ctx, windowId, getTrxName(ctx));
			if (windowId <= 0 && getIntValue(atts, I_AD_Window.COLUMNNAME_AD_Window_ID) > 0 && getIntValue(atts, I_AD_Window.COLUMNNAME_AD_Window_ID) <= PackOut.MAX_OFFICIAL_ID) {
				window.setAD_Window_ID(getIntValue(atts, I_AD_Window.COLUMNNAME_AD_Window_ID));
				window.setIsDirectLoad(true);
			}
			String Object_Status = null;
			int backupId = -1;
			if (windowId > 0) {
				backupId = copyRecord(ctx, "AD_Window", window);
				Object_Status = "Update";
			} else {
				Object_Status = "New";
				backupId = 0;
			}
			//	
			window.setUUID(uuid);
			//	For Image
			uuid = getUUIDValue(atts, I_AD_Window.COLUMNNAME_AD_Image_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdWithFromUUID(ctx, I_AD_Image.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				window.setAD_Image_ID(id);
			}
			//	For Color
			uuid = getUUIDValue(atts, I_AD_Window.COLUMNNAME_AD_Color_ID);
			if (!Util.isEmpty(uuid)) {
				int id = getIdWithFromUUID(ctx, I_AD_Color.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				window.setAD_Color_ID(id);
			}			
			//	Standard Attributes
			window.setName(getStringValue(atts, I_AD_Window.COLUMNNAME_Name));
			window.setDescription(getStringValue(atts, I_AD_Window.COLUMNNAME_Description));
			window.setHelp(getStringValue(atts, I_AD_Window.COLUMNNAME_Help));
			window.setEntityType(getStringValue(atts, I_AD_Window.COLUMNNAME_EntityType));
			window.setIsBetaFunctionality(getBooleanValue(atts, I_AD_Window.COLUMNNAME_IsBetaFunctionality));
			window.setIsDefault(getBooleanValue(atts, I_AD_Window.COLUMNNAME_IsDefault));
			window.setIsSOTrx(getBooleanValue(atts, I_AD_Window.COLUMNNAME_IsSOTrx));
			window.setIsActive(getBooleanValue(atts, I_AD_Window.COLUMNNAME_IsActive));
			window.setProcessing(getBooleanValue(atts, I_AD_Window.COLUMNNAME_Processing));
			window.setWinHeight(getIntValue(atts, I_AD_Window.COLUMNNAME_WinHeight));
			window.setWinWidth(getIntValue(atts, I_AD_Window.COLUMNNAME_WinWidth));
			window.setWindowType(getStringValue(atts, I_AD_Window.COLUMNNAME_WindowType));
			//	Save
			try {
				window.saveEx(getTrxName(ctx));
				record_log(ctx, 1, window.getName(), "Window", window
						.get_ID(), backupId, Object_Status, "AD_Window",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Window"));
				element.recordId = window.getAD_Window_ID();
				windows.add(window.getAD_Window_ID());
			} catch (Exception e) {
				record_log(ctx, 0, window.getName(), "Window", window
						.get_ID(), backupId, Object_Status, "AD_Window",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Window"));
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
		int windowId = Env.getContextAsInt(ctx, "AD_Window_ID");
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");

		MWindow window = new MWindow(ctx, windowId, null);
		AttributesImpl atts = new AttributesImpl();
		createWindowBinding(atts, window);
		document.startElement("", "", "window", atts);
		//	For tabs
		for(MTab tab: window.getTabs(true, null)) {
			packOut.createTable(tab.getAD_Table_ID(), document);
			createTab(ctx, document, tab.getAD_Tab_ID());
		}
		// Loop tags.
		document.endElement("", "", "window");
	}
	
	private void createTab(Properties ctx, TransformerHandler document,
			int AD_Tab_ID) throws SAXException {
		Env.setContext(ctx, X_AD_Tab.COLUMNNAME_AD_Tab_ID, AD_Tab_ID);
		tabHandler.create(ctx, document);
		ctx.remove(X_AD_Tab.COLUMNNAME_AD_Tab_ID);
	}

	private AttributesImpl createWindowBinding(AttributesImpl atts, X_AD_Window window) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, window);
		if (window.getAD_Window_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Window.COLUMNNAME_AD_Window_ID);
		}
		filler.addUUID();
		//	Image
		if(window.getAD_Image_ID() > 0) {
			filler.add(I_AD_Window.COLUMNNAME_AD_Image_ID, true);
			filler.addUUID(I_AD_Window.COLUMNNAME_AD_Image_ID, getUUIDFromId(window.getCtx(), I_AD_Image.Table_Name, window.getAD_Image_ID()));
		}
		//	Color
		if (window.getAD_Color_ID() > 0) {
			filler.add(I_AD_Window.COLUMNNAME_AD_Color_ID, true);
			filler.addUUID(I_AD_Window.COLUMNNAME_AD_Color_ID, getUUIDFromId(window.getCtx(), I_AD_Color.Table_Name, window.getAD_Color_ID()));
		}
		//	Attributes
		filler.add(I_AD_Window.COLUMNNAME_Name);
		filler.add(I_AD_Window.COLUMNNAME_Description);
		filler.add(I_AD_Window.COLUMNNAME_Help);
		filler.add(I_AD_Window.COLUMNNAME_EntityType);
		filler.add(I_AD_Window.COLUMNNAME_IsBetaFunctionality);
		filler.add(I_AD_Window.COLUMNNAME_IsDefault);
		filler.add(I_AD_Window.COLUMNNAME_IsSOTrx);
		filler.add(I_AD_Window.COLUMNNAME_IsActive);
		filler.add(I_AD_Window.COLUMNNAME_Processing);
		filler.add(I_AD_Window.COLUMNNAME_WinHeight);
		filler.add(I_AD_Window.COLUMNNAME_WinWidth);
		filler.add(I_AD_Window.COLUMNNAME_WindowType);
		return atts;
	}
}
