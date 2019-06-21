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
import org.compiere.model.I_C_Currency;
import org.compiere.model.I_C_UOM_Conversion;
import org.compiere.model.I_M_Locator;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_ProductPrice;
import org.compiere.model.I_M_Warehouse;
import org.compiere.model.MLocator;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MTree;
import org.compiere.model.MUOMConversion;
import org.compiere.model.MWarehouse;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class MaterialManagementExporter extends ClientExporterHandler {
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
		parentsToExclude = new ArrayList<String>();
		parentsToExclude.add(I_C_Currency.Table_Name);
		parentsToExclude.add(I_AD_Tree.Table_Name);
		//	Default Tree
		defaultTreeId = MTree.getDefaultTreeIdFromTableId(Env.getAD_Client_ID(ctx), I_M_Product.Table_ID);
		//	Export Tree
		MTree tree = MTree.get(ctx, defaultTreeId, null);
		cleanOfficialReference(tree);
		packOut.createGenericPO(document, tree);
		//	Export element Value
		List<MProduct> productList = new Query(ctx, I_M_Product.Table_Name, 
				"EXISTS(SELECT 1 "
				+ "			FROM AD_TreeNodePR tnm "
				+ "			WHERE tnm.Node_ID = M_Product.M_Product_ID "
				+ "			AND tnm.AD_Tree_ID = ? "
				+ "			AND tnm.Parent_ID = 0)"
				+ "", null)
				.setParameters(defaultTreeId)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_M_Product.COLUMNNAME_Value)
				.list();
		//	Export
		for(MProduct product : productList) {
			if(product.getM_Product_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default bank account
			cleanOfficialReference(product);
			exportMenuElementValue(product, document);
		}
		//	Warehouse
		List<MWarehouse> warehouseList = new Query(ctx, I_M_Warehouse.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(MWarehouse warehouse : warehouseList) {
			if(warehouse.getM_Warehouse_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, warehouse);
			//	Warehouse
			List<MLocator> locatorList = new Query(ctx, I_M_Locator.Table_Name, I_M_Locator.COLUMNNAME_M_Warehouse_ID + " = ?", null)
					.setParameters(warehouse.getM_Warehouse_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.list();
			//	Export
			for(MLocator locator : locatorList) {
				packOut.createGenericPO(document, locator);
			}
		}	
	}
	
	/**
	 * Export like menu export
	 * @param elementValue
	 * @param document
	 * @throws SAXException
	 */
	private void exportMenuElementValue(MProduct product, TransformerHandler document) throws SAXException {
		//	Export current
		packOut.createGenericPO(document, product, true, parentsToExclude);
		if(product.isSummary()) {
			List<MProduct> elementValueList = new Query(product.getCtx(), I_M_Product.Table_Name, 
					"EXISTS(SELECT 1 "
					+ "			FROM AD_TreeNodePR tnm "
					+ "			WHERE tnm.Node_ID = M_Product.M_Product_ID "
					+ "			AND tnm.AD_Tree_ID = ? "
					+ "			AND tnm.Parent_ID = ?)"
					+ "", null)
					.setParameters(defaultTreeId, product.getM_Product_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setOrderBy(I_M_Product.COLUMNNAME_Value)
					.list();
			//	Create Menu
			for(MProduct productChild : elementValueList) {
				if(productChild.getM_Product_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default bank account
				cleanOfficialReference(productChild);
				exportMenuElementValue(productChild, document);
			}
		}
		product.set_ValueOfColumn("M_Locator_ID", null);
		product.set_ValueOfColumn("SalesRep_ID", null);
		packOut.createGenericPO(document, product, true, parentsToExclude);
		//	Product Price
		List<MProductPrice> productPriceList = new Query(product.getCtx(), I_M_ProductPrice.Table_Name, I_M_ProductPrice.COLUMNNAME_M_Product_ID + " = ?", null)
				.setParameters(product.getM_Product_ID())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(MProductPrice productPrice : productPriceList) {
			packOut.createGenericPO(document, productPrice, true, parentsToExclude);
		}
		//	UOM Conversion
		List<MUOMConversion> productUomConversionList = new Query(product.getCtx(), I_C_UOM_Conversion.Table_Name, "M_Product_ID = ? OR C_UOM_ID = ?", null)
				.setParameters(product.getM_Product_ID(), product.getC_UOM_ID())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(MUOMConversion productUomConversion : productUomConversionList) {
			packOut.createGenericPO(document, productUomConversion, true, parentsToExclude);
		}
	}
}
