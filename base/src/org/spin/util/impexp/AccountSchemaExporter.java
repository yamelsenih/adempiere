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
import org.compiere.model.I_AD_Tree;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Element;
import org.compiere.model.I_C_ElementValue;
import org.compiere.model.MElement;
import org.compiere.model.MElementValue;
import org.compiere.model.MTree;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class AccountSchemaExporter extends ClientExporterHandler {
	/**	Parents for no added	*/
	private List<String> parentsToExclude;
	/**	Default Tree	*/
	private int defaultTreeId ;
	/**	Packout	*/
	private PackOut packOut;
	@Override
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		//	Default Tree
		defaultTreeId = MTree.getDefaultTreeIdFromTableId(Env.getAD_Client_ID(ctx), I_C_ElementValue.Table_ID);
		//	Export Account Elements
		List<MElement> elementList = new Query(ctx, I_C_Element.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		parentsToExclude = new ArrayList<String>();
		parentsToExclude.add(I_AD_Tree.Table_Name);
		parentsToExclude.add(I_C_BPartner.Table_Name);
		//	Export menu
		for(MElement elementExporter : elementList) {
			if(elementExporter.getC_Element_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(elementExporter);
			packOut.createGenericPO(document, elementExporter, true, parentsToExclude);
			//	Export element Value
			List<MElementValue> elementValueList = new Query(ctx, I_C_ElementValue.Table_Name, I_C_ElementValue.COLUMNNAME_C_Element_ID + " = ? "
					+ "AND EXISTS(SELECT 1 "
					+ "			FROM AD_TreeNode tnm "
					+ "			WHERE tnm.Node_ID = C_ElementValue.C_ElementValue_ID "
					+ "			AND tnm.AD_Tree_ID = ? "
					+ "			AND tnm.Parent_ID = 0)"
					+ "", null)
					.setParameters(elementExporter.getC_Element_ID(), defaultTreeId)
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setOrderBy(I_C_ElementValue.COLUMNNAME_Value)
					.list();
			//	Export
			for(MElementValue elementValueDetail : elementValueList) {
				if(elementValueDetail.getC_ElementValue_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default bank account
				cleanOfficialReference(elementValueDetail);
				exportMenuElementValue(elementValueDetail, document);
			}
		}
	}
	
	/**
	 * Export like menu export
	 * @param elementValue
	 * @param document
	 * @throws SAXException
	 */
	private void exportMenuElementValue(MElementValue elementValue, TransformerHandler document) throws SAXException {
		//	Export current
		packOut.createGenericPO(document, elementValue, true, parentsToExclude);
		if(elementValue.isSummary()) {
			List<MElementValue> elementValueList = new Query(elementValue.getCtx(), I_C_ElementValue.Table_Name, I_C_ElementValue.COLUMNNAME_C_Element_ID + " = ? "
					+ "AND EXISTS(SELECT 1 "
					+ "			FROM AD_TreeNode tnm "
					+ "			WHERE tnm.Node_ID = C_ElementValue.C_ElementValue_ID "
					+ "			AND tnm.AD_Tree_ID = ? "
					+ "			AND tnm.Parent_ID = ?)"
					+ "", null)
					.setParameters(elementValue.getC_Element_ID(), defaultTreeId, elementValue.getC_ElementValue_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setOrderBy(I_C_ElementValue.COLUMNNAME_Value)
					.list();
			//	Create Menu
			for(MElementValue elementValueDetail : elementValueList) {
				if(elementValueDetail.getC_ElementValue_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default bank account
				cleanOfficialReference(elementValueDetail);
				exportMenuElementValue(elementValueDetail, document);
			}
		}
	}
}
