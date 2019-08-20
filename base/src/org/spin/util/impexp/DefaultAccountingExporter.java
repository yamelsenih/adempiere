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
import org.compiere.model.I_C_BP_Customer_Acct;
import org.compiere.model.I_C_BP_Employee_Acct;
import org.compiere.model.I_C_BP_Group_Acct;
import org.compiere.model.I_C_BP_Vendor_Acct;
import org.compiere.model.I_C_Charge_Acct;
import org.compiere.model.I_M_Product_Acct;
import org.compiere.model.I_M_Product_Category;
import org.compiere.model.I_M_Product_Category_Acct;
import org.compiere.model.MChargeAcct;
import org.compiere.model.MProductCategoryAcct;
import org.compiere.model.Query;
import org.compiere.model.X_C_BP_Customer_Acct;
import org.compiere.model.X_C_BP_Employee_Acct;
import org.compiere.model.X_C_BP_Group_Acct;
import org.compiere.model.X_C_BP_Vendor_Acct;
import org.compiere.model.X_M_Product_Acct;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class DefaultAccountingExporter extends ClientExporterHandler {
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
		parentsToExclude.add(I_M_Product_Category.Table_Name);
		//	
		createProductCategoryAcct(ctx, document, parentsToExclude);
		createCargeAcct(ctx, document, parentsToExclude);
		createBusinessPartnerAcct(ctx, document, parentsToExclude);
	}
	
	/**
	 * Product and category
	 * @param ctx
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createProductCategoryAcct(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	Product Category Acct
		List<MProductCategoryAcct> productCategoryAcctList = new Query(ctx, I_M_Product_Category_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(MProductCategoryAcct productCategoryAcct : productCategoryAcctList) {
			packOut.createGenericPO(document, productCategoryAcct);
		}
		
		//	Product classification
		List<X_M_Product_Acct> productAcctList = new Query(ctx, I_M_Product_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(X_M_Product_Acct productAcct : productAcctList) {
			packOut.createGenericPO(document, productAcct);
		}
	}
	
	/**
	 * Charge Account
	 * @param ctx
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createCargeAcct(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	Product Category Acct
		List<MChargeAcct> chargeAcctList = new Query(ctx, I_C_Charge_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(MChargeAcct chargeAcct : chargeAcctList) {
			packOut.createGenericPO(document, chargeAcct);
		}
	}
	
	/**
	 * Business Partner Accounting
	 * @param ctx
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createBusinessPartnerAcct(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	Busriness Partner Group Acct
		List<X_C_BP_Group_Acct> bPGroupAcctList = new Query(ctx, I_C_BP_Group_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(X_C_BP_Group_Acct bPGroupAcct : bPGroupAcctList) {
			if(bPGroupAcct.getC_BP_Group_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, bPGroupAcct);
		}
		//	Customer Acct
		List<X_C_BP_Customer_Acct> customerAcctList = new Query(ctx, I_C_BP_Customer_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(X_C_BP_Customer_Acct customerAcct : customerAcctList) {
			if(customerAcct.getC_BPartner_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, customerAcct);
		}
		//	Vendor Accounting
		List<X_C_BP_Vendor_Acct> vendorAcctList = new Query(ctx, I_C_BP_Vendor_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(X_C_BP_Vendor_Acct vendorAcct : vendorAcctList) {
			if(vendorAcct.getC_BPartner_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, vendorAcct);
		}
		//	Vendor Accounting
		List<X_C_BP_Employee_Acct> employeeAcctList = new Query(ctx, I_C_BP_Employee_Acct.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export
		for(X_C_BP_Employee_Acct employeeAcct : employeeAcctList) {
			if(employeeAcct.getC_BPartner_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, employeeAcct);
		}
	}
}
