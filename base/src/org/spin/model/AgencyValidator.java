/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
 * This program is free software; you can redistribute it and/or modify it    		 *
 * under the terms version 2 or later of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope   		 *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 		 *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           		 *
 * See the GNU General Public License for more details.                       		 *
 * You should have received a copy of the GNU General Public License along    		 *
 * with this program; if not, write to the Free Software Foundation, Inc.,    		 *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     		 *
 * For the text or an alternative of this public license, you may reach us    		 *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com				  		                 *
 *************************************************************************************/
package org.spin.model;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Commission;
import org.compiere.model.I_C_CommissionType;
import org.compiere.model.MClient;
import org.compiere.model.MCommission;
import org.compiere.model.MCommissionRun;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProject;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.OrderPOCreateAbstract;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.eevolution.service.dsl.ProcessBuilder;


/**
 * Model validator for agency particularities
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class AgencyValidator implements ModelValidator
{
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());
	/** Client			*/
	private int		clientId = -1;
	
	
	public void initialize (ModelValidationEngine engine, MClient client) {
		if (client != null) {	
			clientId = client.getAD_Client_ID();
		}
		engine.addDocValidate(MOrder.Table_Name, this);
		engine.addModelChange(MProject.Table_Name, this);
	}	//	initialize

	public String modelChange (PO po, int type) throws Exception {
		log.info(po.get_TableName() + " Type: "+type);
		
		if (po instanceof MProject && type == TYPE_BEFORE_CHANGE) {
			MProject project = (MProject) po;
			
			if (!project.isAttachment(".pdf") && project.get_ValueAsBoolean("IsApprovedAttachment")) {
				throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
			}
			
		}		
		//
		return null;
	}	//	modelChange
	
	public String docValidate (PO po, int timing) {
		log.info(po.get_TableName() + " Timing: "+timing);
		//	Validate table
		if(po instanceof MOrder) {
			MOrder order = (MOrder) po;
			if(timing == TIMING_BEFORE_PREPARE) {
				if(order.get_ValueAsInt("S_Contract_ID") <= 0) {
					if(order.getC_Project_ID() > 0) {
						MProject parentProject = MProject.getById(order.getCtx(), order.getC_Project_ID(), order.get_TrxName());
						if(parentProject.get_ValueAsInt("S_Contract_ID") > 0) {
							order.set_ValueOfColumn("S_Contract_ID", parentProject.get_ValueAsInt("S_Contract_ID"));
							order.saveEx();
						}
					}
				}
				//	Validate
				MProject project = new MProject(order.getCtx(), order.getC_Project_ID(), null);
				MDocType  documentType = MDocType.get(order.getCtx(), order.getC_DocTypeTarget_ID());
				
				// Document type IsCustomerApproved = Y and order IsCustomerApproved = N
				if (documentType.get_ValueAsBoolean("IsApprovedRequired") 
						&& !order.get_ValueAsBoolean("IsCustomerApproved")) {
					throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@CustomerApprovedRequired@ @C_DocType_ID@"));
				}
				// Document type IsCustomerApproved = Y and order IsCustomerApproved Y and order isAttachment("PDF") = N
				if (!order.isAttachment(".pdf") 
						&& order.get_ValueAsBoolean("IsCustomerApproved")
						&& documentType.get_ValueAsBoolean("IsApprovedRequired")) {
					throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
				}
				
				//	Document type IsCustomerApproved = Y and order IsCustomerApproved Y and order isAttachment("PDF") = N and project IsCustomerApproved = N
				if (!project.get_ValueAsBoolean("IsCustomerApproved")
						&& documentType.get_ValueAsBoolean("IsApprovedRequired")
						&& order.get_ValueAsBoolean("IsCustomerApproved")
						&& order.isAttachment(".pdf")) {
					throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@CustomerApprovedRequired@"));
				}
			} else if(timing == TIMING_BEFORE_COMPLETE) {
				MDocType  documentType = MDocType.get(order.getCtx(), order.getC_DocTypeTarget_ID());
				//	Validate Document Type for commission
				if(documentType.get_ValueAsInt("C_CommissionType_ID") > 0) {
					createCommissionForOrder(order, documentType.get_ValueAsInt("C_CommissionType_ID"));
				}
			} else if (timing == TIMING_AFTER_COMPLETE) {
				if(!order.isSOTrx()) {
					return null;
				}
				//	For Sales Orders only
				if(!order.isDropShip()) {
					return null;
				}
				//	For drop ship only
				ProcessBuilder.create(order.getCtx())
					.process(OrderPOCreateAbstract.getProcessId())
					.withParameter(OrderPOCreateAbstract.C_ORDER_ID, order.getC_Order_ID())
					.withParameter(OrderPOCreateAbstract.VENDOR_ID, order.getDropShip_BPartner_ID())
					.withoutTransactionClose()
					.execute(order.get_TrxName());
			}
		}
		return null;
	}	//	docValidate
	
	/**
	 * Create a commission based on rules defined and get result inside order line 
	 * it is only running if exists a flag for document type named (Calculate commission for Order)
	 * @param order
	 * @param
	 */
	private void createCommissionForOrder(MOrder order, int commissionTypeId) {
		new Query(order.getCtx(), I_C_Commission.Table_Name, I_C_CommissionType.COLUMNNAME_C_CommissionType_ID + " = ? ", order.get_TrxName())
			.setParameters(commissionTypeId)
			.<MCommission>list().forEach(commissionDefinition -> {
				int documentTypeId = MDocType.getDocType(MDocType.DOCBASETYPE_SalesCommission, order.getAD_Org_ID());
				MCommissionRun commissionRun = new MCommissionRun(commissionDefinition);
				commissionRun.setDateDoc(order.getDateOrdered());
				commissionRun.setC_DocType_ID(documentTypeId);
				commissionRun.setDescription(Msg.parseTranslation(order.getCtx(), "@Generate@: @C_Order_ID@ - " + order.getDocumentNo()));
				commissionRun.saveEx();
				//	Process commission
				commissionRun.setCurrentDocumentId("C_Order_ID", order.getC_Order_ID());
				commissionRun.setDocStatus(MCommissionRun.DOCSTATUS_Drafted);
				//	Complete
				if(commissionRun.processIt(MCommissionRun.DOCACTION_Complete)) {
					commissionRun.getCommissionAmtList().stream()
						.filter(commissionAmt -> commissionAmt.getCommissionAmt() != null 
							&& commissionAmt.getCommissionAmt().compareTo(Env.ZERO) > 0).forEach(commissionAmt -> {
								MOrderLine orderLine = new MOrderLine(order);
								orderLine.setC_Charge_ID(commissionDefinition.getC_Charge_ID());
								orderLine.setQty(Env.ONE);
								orderLine.setPrice(commissionAmt.getCommissionAmt());
								orderLine.setTax();
								orderLine.saveEx();
						});
				}
			});
	}

	@Override
	public int getAD_Client_ID() {
		return clientId;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}
}	//	AgencyValidator