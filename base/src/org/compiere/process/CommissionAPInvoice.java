/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.process;


import java.util.Hashtable;

import org.compiere.model.MBPartner;
import org.compiere.model.MCommission;
import org.compiere.model.MCommissionAmt;
import org.compiere.model.MCommissionRun;
import org.compiere.model.MDocType;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.util.Env;

/**
 *	Create AP Invoices for Commission
 *	
 *  @author Jorg Janke
 *  @version $Id: CommissionAPInvoice.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class CommissionAPInvoice extends CommissionAPInvoiceAbstract {
	
	/**	Invoices created	*/
	private int created = 0;
	/**	Message	*/
	private StringBuffer generatedDocuments = new StringBuffer();
	/**	Invoices	*/
	private Hashtable<Integer, MInvoice> invoices;
	
	/**
	 *  Perform process.
	 *  @return Message (variables are parsed)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception {
		log.info("doIt - C_CommissionRun_ID=" + getRecord_ID());
		//	Load Data
		MCommissionRun commissionRun = new MCommissionRun (getCtx(), getRecord_ID(), get_TrxName());
		if (commissionRun.getC_CommissionRun_ID() == 0)
			throw new IllegalArgumentException("@C_CommissionRun_ID@ @NotFound@");
		if (Env.ZERO.compareTo(commissionRun.getGrandTotal()) == 0)
			throw new IllegalArgumentException("@GrandTotal@ = 0");
		//	For Completed Commission
		if(!commissionRun.getDocStatus().equals(MCommissionRun.ACTION_Complete)) {
			throw new IllegalArgumentException("@C_CommissionRun_ID@ @NotValid@");
		}
		//	
		MCommission commissionDefinition = new MCommission (getCtx(), commissionRun.getC_Commission_ID(), get_TrxName());
		if (commissionDefinition.getC_Commission_ID() == 0)
			throw new IllegalArgumentException("@C_Commission_ID@ @NotFound@");
		if (commissionDefinition.getC_Charge_ID() == 0)
			throw new IllegalArgumentException("@C_Commission_ID@ - (@C_Charge_ID@) @NotFound@");
		//	
		invoices = new Hashtable<>();
		//	Get lines
		commissionRun.getCommissionAmtList().stream()
			.filter(commissionAmt -> commissionAmt.getCommissionAmt() != null 
				&& commissionAmt.getCommissionAmt().compareTo(Env.ZERO) > 0).forEach(commissionAmt -> {
			MInvoice invoice = getInvoice(commissionDefinition, commissionRun, commissionAmt);
			createInvoiceLine(commissionAmt, invoice, commissionDefinition.getC_Charge_ID());
		});
		//
		return "@Created@ " + created + (generatedDocuments.length() > 0? " [" + generatedDocuments + "]": "");
	}	//	doIt
	
	/**
	 * Create Invoice header
	 */
	private MInvoice getInvoice(MCommission commissionDefinition, MCommissionRun commissionRun, MCommissionAmt commissionAmt) {
		MInvoice invoice = invoices.get(commissionAmt.getC_BPartner_ID());
		if(invoice != null) {
			return invoice;
		}
		//	Create Invoice
		invoice = new MInvoice (getCtx(), 0, null);
		invoice.setClientOrg(commissionAmt.getAD_Client_ID(), commissionAmt.getAD_Org_ID());
		invoice.setC_DocTypeTarget_ID(MDocType.DOCBASETYPE_APInvoice);	//	API
		MBPartner businessPartner = MBPartner.get(getCtx(), commissionAmt.getC_BPartner_ID());
		invoice.setBPartner(businessPartner);
		invoice.setSalesRep_ID(getAD_User_ID());	//	caller
		//
		if (commissionDefinition.getC_Currency_ID() != invoice.getC_Currency_ID())
			throw new IllegalArgumentException("@CommissionAPInvoiceCurrency@");	//	TODO Translate it: CommissionAPInvoice - Currency of PO Price List not Commission Currency
		//		
		invoice.saveEx();
		invoices.put(commissionAmt.getC_BPartner_ID(), invoice);
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
	private void createInvoiceLine(MCommissionAmt commissionAmt, MInvoice invoice, int chargeId) {
		//	Create Invoice Line
 		MInvoiceLine iLine = new MInvoiceLine(invoice);
		iLine.setC_Charge_ID(chargeId);
 		iLine.setQty(1);
 		iLine.setPrice(commissionAmt.getCommissionAmt());
		iLine.setTax();
		iLine.saveEx();
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
}	//	CommissionAPInvoice
