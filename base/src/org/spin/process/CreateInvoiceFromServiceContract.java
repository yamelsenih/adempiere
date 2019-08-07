/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/

package org.spin.process;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Hashtable;

import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MUOMConversion;
import org.compiere.util.Env;
import org.eevolution.model.X_S_Contract;

import com.eevolution.model.X_S_ContractLine;

/** Generated Process for (Generate Invoice from Service Contract)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public class CreateInvoiceFromServiceContract extends CreateInvoiceFromServiceContractAbstract {
	
	/**	Invoices created	*/
	private int created = 0;
	/**	Message	*/
	private StringBuffer generatedDocuments = new StringBuffer();
	/**	Invoices	*/
	private Hashtable<Integer, MInvoice> invoices;
	/**	Date Next Run by invoice	*/
	private Hashtable<Integer, Timestamp> dateLastRunHash;
	
	@Override
	protected String doIt() throws Exception {
		//	
		invoices = new Hashtable<>();
		dateLastRunHash = new Hashtable<>();
		//	Loop for keys
		for(Integer key : getSelectionKeys()) {
			int contractLineId = getSelectionAsInt(key, "C_S_ContractLine_ID");
			Timestamp dateNextRun = getSelectionAsTimestamp(key, "C_DateNextRun");
			X_S_ContractLine contractLine = new X_S_ContractLine(getCtx(), contractLineId, get_TrxName());
			X_S_Contract contract = new X_S_Contract(getCtx(), contractLine.getS_Contract_ID(), get_TrxName());
			//	Get Invoice to process
			MInvoice invoice = getInvoice(contract, contractLine);
			createInvoiceLine(contractLine, invoice, dateNextRun);
			if(dateNextRun != null) {
				Timestamp dateLastRun = dateLastRunHash.get(contractLineId);
				if(dateLastRun == null
						|| dateNextRun.getTime() > dateLastRun.getTime()) {
					dateLastRunHash.put(contractLineId, dateNextRun);
				}
			}
		}
		//	Process Invoices
		if(invoices.size() > 0) {
			invoices.entrySet().stream().forEach(invoiceSet -> {
				MInvoice invoice = invoiceSet.getValue();
				invoice.processIt(getDocAction());
				invoice.saveEx();
			});
		}
		//	Update Contract Line
		if(dateLastRunHash.size() > 0) {
			dateLastRunHash.entrySet().stream().forEach(dateRunningSet -> {
				Timestamp newDate = dateRunningSet.getValue();
				X_S_ContractLine contractLine = new X_S_ContractLine(getCtx(), dateRunningSet.getKey(), get_TrxName());
				contractLine.setDateLastRun(newDate);
				contractLine.saveEx();
			});
		}
		//
		return "@Created@ " + created + (generatedDocuments.length() > 0? " [" + generatedDocuments + "]": "");
	}
	
	/**
	 * Create Invoice header
	 */
	private MInvoice getInvoice(X_S_Contract contract, X_S_ContractLine contractLine) {
		int bPartnerId = contractLine.getC_BPartner_ID();
		if(bPartnerId == 0) {
			bPartnerId = contract.getC_BPartner_ID();
		}
		MInvoice invoice = invoices.get(bPartnerId);
		if(invoice != null) {
			return invoice;
		}
		//	Create Invoice
		invoice = new MInvoice (getCtx(), 0, null);
		invoice.setDateInvoiced(getDateInvoiced());
		invoice.setDateAcct(getDateInvoiced());
		invoice.setClientOrg(contractLine.getAD_Client_ID(), contractLine.getAD_Org_ID());
		invoice.set_ValueOfColumn("S_Contract_ID", contract.getS_Contract_ID());
		if(getDocTypeTargetId() > 0) {
			invoice.setC_DocTypeTarget_ID(getDocTypeTargetId());	//	ARI
		} else {
			invoice.setC_DocTypeTarget_ID(MDocType.DOCBASETYPE_ARInvoice);	//	ARI
		}
		MBPartner businessPartner = MBPartner.get(getCtx(), bPartnerId);
		invoice.setBPartner(businessPartner);
		invoice.setSalesRep_ID(getAD_User_ID());	//	caller
		MPriceList priceList = MPriceList.get(getCtx(), contract.getM_PriceList_ID(), get_TrxName());
		invoice.setC_Currency_ID(priceList.getC_Currency_ID());
		invoice.setM_PriceList_ID(contract.getM_PriceList_ID());
		if(contractLine.getC_BPartner_Location_ID() > 0) {
			invoice.setC_BPartner_Location_ID(contractLine.getC_BPartner_Location_ID());
		}
		//		
		invoice.saveEx();
		invoices.put(businessPartner.getC_BPartner_ID(), invoice);
		//	Add to message
		created++;
		addToMessage(invoice.getDocumentNo());
		return invoice;
	}
	
	/**
	 * Create line from invoice
	 * @param commissionAmt
	 * @param invoice
	 * @param chargeId
	 */
	private void createInvoiceLine(X_S_ContractLine contractLine, MInvoice invoice, Timestamp serviceDate) {
		//	Create Invoice Line
 		MInvoiceLine invoiceLine = new MInvoiceLine(invoice);
 		BigDecimal qtyInvoiced = null;
 		BigDecimal qtyEntered = contractLine.getQtyEntered();
 		BigDecimal priceEntered = contractLine.getPriceEntered();
 		int precision = 2;
 		if(contractLine.getM_Product_ID() > 0) {
 			int uomId = contractLine.getC_UOM_ID();
 			MProduct product = MProduct.get(getCtx(), contractLine.getM_Product_ID());
 			if (product != null) {
				invoiceLine.setM_Product_ID(product.getM_Product_ID(), uomId);
				precision = product.getUOMPrecision();
				if (product.getC_UOM_ID() != uomId) {
					qtyEntered = qtyEntered.setScale(precision, BigDecimal.ROUND_HALF_DOWN);
					qtyInvoiced = MUOMConversion.convertProductFrom(Env.getCtx(), contractLine.getM_Product_ID(), uomId, qtyEntered);
					priceEntered = MUOMConversion.convertProductFrom(Env.getCtx(), contractLine.getM_Product_ID(), uomId, priceEntered);
				}
			}
 		} else {
 			invoiceLine.setC_Charge_ID(contractLine.getC_Charge_ID());
 		}
		qtyEntered = qtyEntered.setScale(precision, BigDecimal.ROUND_HALF_DOWN);
		if (qtyInvoiced == null) {
			qtyInvoiced = qtyEntered;
		}
		invoiceLine.setQty(qtyEntered);							//	Movement/Entered
		invoiceLine.setQtyInvoiced(qtyInvoiced);
		invoiceLine.setPrice(priceEntered);
		invoiceLine.setDescription(contractLine.getDescription());
		invoiceLine.setTax();
		//	Reference
		invoiceLine.set_ValueOfColumn("S_ContractLine_ID", contractLine.getS_ContractLine_ID());
		invoiceLine.set_ValueOfColumn("ServiceDate", serviceDate);
		invoiceLine.saveEx();
	}


	/**
	 * Add Document Info for message to return
	 * @param documentInfo
	 */
	private void addToMessage(String documentInfo) {
		if(generatedDocuments.length() > 0) {
			generatedDocuments.append(", ");
		}
		//	
		generatedDocuments.append(documentInfo);
	}
}