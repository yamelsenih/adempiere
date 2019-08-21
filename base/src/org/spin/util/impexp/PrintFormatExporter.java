/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 or later of the                                  *
 * GNU General Public License as published                                    *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2015 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package org.spin.util.impexp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.PackOut;
import org.compiere.model.I_AD_PrintFormat;
import org.compiere.model.I_AD_PrintFormatItem;
import org.compiere.model.Query;
import org.compiere.print.MPrintFormat;
import org.compiere.print.MPrintFormatItem;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class PrintFormatExporter extends ClientExporterHandler {
	/**	Parents for no added	*/
	private List<String> parentsToExclude;
	/**	Packout	*/
	private PackOut packOut;
	@Override
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		parentsToExclude = new ArrayList<String>();
		//	
		createPrintFormat(ctx, document, parentsToExclude);
	}
	
	/**
	 * Create Print Format
	 * @param ctx
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createPrintFormat(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	Print Format
		List<MPrintFormat> printFormatList = new Query(ctx, I_AD_PrintFormat.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(MPrintFormat printFormat : printFormatList) {
			if(printFormat.getAD_PrintFormat_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, printFormat, true, parentsToExclude);
			//	Print Format Item
			List<MPrintFormatItem> printFormatItemList = new Query(ctx, I_AD_PrintFormatItem.Table_Name, I_AD_PrintFormatItem.COLUMNNAME_AD_PrintFormat_ID + " = ?", null)
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setParameters(printFormat.getAD_PrintFormat_ID())
					.list();
			//	Export
			for(MPrintFormatItem printFormatItem : printFormatItemList) {
				if(printFormatItem.getAD_PrintFormatItem_ID() < PackOut.MAX_OFFICIAL_ID
						|| (printFormatItem.getAD_PrintFormatChild_ID() > 0 && printFormatItem.getAD_PrintFormatChild_ID() < PackOut.MAX_OFFICIAL_ID)) {
					continue;
				}
				packOut.createGenericPO(document, printFormatItem, true, parentsToExclude);
			}
		}
	}
}
