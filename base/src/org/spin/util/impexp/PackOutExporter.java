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

import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.handler.GenericPOHandler;
import org.compiere.model.Query;
import org.spin.model.I_AD_Package_Exp_Custom;
import org.spin.model.MADPackageExpCustom;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Exporter
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class PackOutExporter extends GenericPOHandler {
	@Override
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		//	Exporter of Exporter
		List<MADPackageExpCustom> exporterList = new Query(ctx, I_AD_Package_Exp_Custom.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export menu
		for(MADPackageExpCustom exporter : exporterList) {
			packOut.createGenericPO(document, exporter);
		}
	}
}
