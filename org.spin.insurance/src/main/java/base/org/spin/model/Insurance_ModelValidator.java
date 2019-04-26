/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2015 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Carlos Parada www.erpcya.com                               *
 *****************************************************************************/
package org.spin.model;

import java.math.BigDecimal;
import java.util.List;

import org.adempiere.model.GenericPO;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MRequest;
import org.compiere.model.MStandardRequest;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.X_HR_EmployeeInsurance;

/**
 *  Insurance Model Validator
 * 	@author Carlos Parada, cparada@erpcya.com, ERPCyA http://www.erpcya.com
 *
 */
public class Insurance_ModelValidator implements ModelValidator{
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		engine.addModelChange(X_HR_EmployeeInsurance.Table_Name, this);
		engine.addDocValidate(MOrder.Table_Name, this);
		engine.addModelChange(MRequest.Table_Name, this);
	}

	@Override
	public int getAD_Client_ID() {
		return 0;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {
		if (type == TYPE_BEFORE_NEW) {
			
			if (po.get_Table_ID()==X_HR_EmployeeInsurance.Table_ID) {
				if (po.get_ValueAsInt("Ref_BPartner_ID")!=0) {
					MBPartner bPartner = new MBPartner(po.getCtx(), po.get_ValueAsInt("Ref_BPartner_ID"), po.get_TrxName());
					po.set_ValueOfColumn(X_HR_EmployeeInsurance.COLUMNNAME_SponsorName, bPartner.getName());
				}else
					return "@IsMandatory@ @Ref_BPartner_ID@";
			}
			
			if (po.get_Table_ID()==MRequest.Table_ID) {
				MStandardRequest sr = new MStandardRequest(po.getCtx(), po.get_ValueAsInt("R_StandardRequest_ID"), po.get_TrxName());
				if (sr.get_ID()>0) {
					po.set_ValueOfColumn("IsPromissoryNote", sr.get_ValueAsBoolean("IsPromissoryNote"));
					po.set_ValueOfColumn("IsPostPaidPayment", sr.get_ValueAsBoolean("IsPostPaidPayment"));
				}
			}
		}
		return null;
	}

	@Override
	public String docValidate(PO po, int timing) {
		String result = null;
		if (po.get_Table_ID()==MOrder.Table_ID) {
			if (timing == TIMING_BEFORE_PREPARE) {
				
				MOrder order = (MOrder) po;
				MDocType docType = (MDocType) order.getC_DocTypeTarget();
				if (docType.getDocSubTypeSO() != null 
						&& !docType.getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_Proposal)
							&& !docType.getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_Quotation)) {
					List<MRequest> listRequest = new Query(order.getCtx(), MRequest.Table_Name, "Processed = 'N' AND "
																								+ "C_BPartner_ID = ? AND "
																								+ "EXISTS (SELECT 1 "
																										+ "FROM R_StandardRequest sr "
																										+ "INNER JOIN C_BPartner bp ON (sr.R_StandardRequestType_ID = bp.R_StandardRequestType_ID) "
																										+ "WHERE sr.R_StandardRequest_ID = R_Request.R_StandardRequest_ID )", order.get_TrxName())
																								.setParameters(order.getC_BPartner_ID())
																								.list();
					for (MRequest mRequest : listRequest) 
						result = (result == null ? "" : result) + mRequest.getSummary() +"\n";
					
					if (result!=null)
						result = "@R_Request_ID@ @No@ @Processed@ \n" + result;
					
					List<PO> requestOrders = new Query(order.getCtx(), "R_Request_Order", "C_Order_ID = ? ", order.get_TrxName())
													.setParameters(order.getC_Order_ID())
													.setOrderBy("Created")
													.list();
					BigDecimal remainAmt = order.getGrandTotal();
					for (PO requestOrder : requestOrders) {
						BigDecimal requestBalance = getOpenBalance(requestOrder.get_ValueAsInt("R_Request_ID"));
						remainAmt = remainAmt.subtract(requestBalance);
						if (requestBalance.compareTo(Env.ZERO)==0) 
							requestOrder.set_ValueOfColumn("Amount", Env.ZERO);
						else{
							int diff =remainAmt.compareTo(Env.ZERO);
							switch (diff) {
							case 0: //Warranty Balance equals to Sales Order
								requestOrder.set_ValueOfColumn("Amount", requestBalance);
								break;
							case 1: //Warranty Balance minor than Sales Order	
								requestOrder.set_ValueOfColumn("Amount", requestBalance);
								break;
							case -1:
								requestOrder.set_ValueOfColumn("Amount", remainAmt.add(requestBalance));
								break;

							default:
								break;
							}
						}
						requestOrder.save();
					}
					
					if (result==null 
							&& remainAmt.compareTo(Env.ZERO)>0)
						result = "@Error@ \n @GrandTotal@ > @Warranty@ " ;
					
					
				}
			}
		}
		return result;
	}

	private BigDecimal getOpenBalance(int R_Request_ID) {
		
		String sql = "SELECT COALESCE(r.PromissoryNoteAmt,0) - COALESCE(SUM(CASE WHEN o.DocStatus IN ('CO','CL') THEN ro.Amount ELSE 0 END),0) "
					+ "FROM R_Request_Order ro "
					+ "LEFT JOIN C_Order o ON (ro.C_Order_ID = o.C_Order_ID) "
					+ "INNER JOIN R_Request r ON (ro.R_Request_ID = r.R_Request_ID) "
					+ "WHERE  ro.R_Request_ID = ? "
					+ "GROUP BY r.PromissoryNoteAmt ";
		BigDecimal openAmt  = DB.getSQLValueBD(null, sql, R_Request_ID);
		
		if (openAmt == null)
			openAmt = Env.ZERO;
		
		return openAmt;
	}
}
