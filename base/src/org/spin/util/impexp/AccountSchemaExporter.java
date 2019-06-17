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
import org.compiere.model.I_C_AcctSchema;
import org.compiere.model.I_C_AcctSchema_Default;
import org.compiere.model.I_C_AcctSchema_Element;
import org.compiere.model.I_C_AcctSchema_GL;
import org.compiere.model.I_C_Element;
import org.compiere.model.I_C_ElementValue;
import org.compiere.model.I_C_ValidCombination;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MAcctSchemaDefault;
import org.compiere.model.MAcctSchemaElement;
import org.compiere.model.MAcctSchemaGL;
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
	private int defaultTreeId;
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
		//	Export Tree
		MTree tree = MTree.get(ctx, defaultTreeId, null);
		cleanOfficialReference(tree);
		packOut.createGenericPO(document, tree);
		//	Export Account Elements
		List<MElement> elementList = new Query(ctx, I_C_Element.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		parentsToExclude = new ArrayList<String>();
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
		//	Export Account Schema
		List<MAcctSchema> accountSchemaList = new Query(ctx, I_C_AcctSchema.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		for(MAcctSchema accountSchema : accountSchemaList) {
			if(accountSchema.getC_AcctSchema_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(accountSchema);
			packOut.createGenericPO(document, accountSchema, true, parentsToExclude);
			//	Element Account Schema
			List<MAcctSchemaElement> elementAccountSchemaList = new Query(ctx, I_C_AcctSchema_Element.Table_Name, I_C_AcctSchema_Element.COLUMNNAME_C_AcctSchema_ID + " = ?", null)
				.setParameters(accountSchema.getC_AcctSchema_ID())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
			for(MAcctSchemaElement elementAccountSchema : elementAccountSchemaList) {
				if(elementAccountSchema.getC_AcctSchema_Element_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				cleanOfficialReference(elementAccountSchema);
				elementAccountSchema.setAD_Org_ID(0);
				elementAccountSchema.setOrg_ID(0);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_BPartner_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_M_Product_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_Project_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_Activity_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_Campaign_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_Location_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_SalesRegion_ID, null);
				elementAccountSchema.set_ValueOfColumn(I_C_AcctSchema_Element.COLUMNNAME_C_BPartner_ID, null);
				packOut.createGenericPO(document, elementAccountSchema, true, parentsToExclude);
			}
			//	General Ledger
			List<MAcctSchemaGL> generalLedgerList = new Query(ctx, I_C_AcctSchema_GL.Table_Name, I_C_AcctSchema_GL.COLUMNNAME_C_AcctSchema_ID + " = ?", null)
				.setParameters(accountSchema.getC_AcctSchema_ID())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
			for(MAcctSchemaGL generalLedger : generalLedgerList) {
				cleanOfficialReference(generalLedger);
				packOut.createGenericPO(document, generalLedger, true, parentsToExclude);
			}
			//	Element Account Schema
			List<MAcctSchemaDefault> defaultAccountSchemaList = new Query(ctx, I_C_AcctSchema_Default.Table_Name, I_C_AcctSchema_Default.COLUMNNAME_C_AcctSchema_ID, null)
				.setParameters(accountSchema.getC_AcctSchema_ID())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
			for(MAcctSchemaDefault defaultAccountSchema : defaultAccountSchemaList) {
				cleanOfficialReference(defaultAccountSchema);
				packOut.createGenericPO(document, defaultAccountSchema, true, parentsToExclude);
			}
			//	Valid Combination
			List<MAccount> validCombinationList = new Query(ctx, I_C_ValidCombination.Table_Name, I_C_ValidCombination.COLUMNNAME_C_AcctSchema_ID, null)
					.setParameters(accountSchema.getC_AcctSchema_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.list();
				for(MAccount validCombination : validCombinationList) {
					cleanOfficialReference(validCombination);
					validCombination.setAD_Org_ID(0);
					packOut.createGenericPO(document, validCombination, true, parentsToExclude);
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
