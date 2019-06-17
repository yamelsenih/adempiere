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
import org.compiere.model.I_AD_Package_Exp;
import org.compiere.model.I_AD_Package_Exp_Detail;
import org.compiere.model.MPackageExp;
import org.compiere.model.MPackageExpDetail;
import org.compiere.model.Query;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Packout
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
		List<MPackageExp> packageList = new Query(ctx, I_AD_Package_Exp.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export menu
		for(MPackageExp packageExporter : packageList) {
			packOut.createGenericPO(document, packageExporter);
			//	Get Detail
			List<MPackageExpDetail> packageDetailList = new Query(ctx, I_AD_Package_Exp_Detail.Table_Name, I_AD_Package_Exp_Detail.COLUMNNAME_AD_Package_Exp_ID + " = ?", null)
					.setParameters(packageExporter.getAD_Package_Exp_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.list();
			//	Export
			for(MPackageExpDetail packageDetail : packageDetailList) {
				packOut.createGenericPO(document, packageDetail);
			}
		}
	}
}
