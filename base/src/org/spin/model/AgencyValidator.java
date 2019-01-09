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

import java.math.BigDecimal;
import java.util.Hashtable;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Commission;
import org.compiere.model.I_C_CommissionType;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_S_TimeExpense;
import org.compiere.model.MAttachment;
import org.compiere.model.MClient;
import org.compiere.model.MCommission;
import org.compiere.model.MCommissionLine;
import org.compiere.model.MCommissionRun;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProject;
import org.compiere.model.MTimeExpense;
import org.compiere.model.MTimeExpenseLine;
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
		engine.addDocValidate(I_S_TimeExpense.Table_Name, this);
		engine.addModelChange(MProject.Table_Name, this);
		engine.addModelChange(MOrder.Table_Name, this);
		engine.addModelChange(MOrderLine.Table_Name, this);
		engine.addModelChange(MCommission.Table_Name, this);
	}	//	initialize

	public String modelChange (PO po, int type) throws Exception {
		log.info(po.get_TableName() + " Type: "+type);
		
		if (type == TYPE_BEFORE_CHANGE) {
			if (po instanceof MProject) {
				MProject project = (MProject) po;
				if(project.get_ValueAsBoolean("IsApprovedAttachment")) {
					MAttachment projectAttachment = project.getAttachment(true);
					if (projectAttachment == null 
							|| projectAttachment.getAD_Attachment_ID() <= 0) {
						throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
					}
				}
			} else if(po instanceof MOrderLine) {
				if(po.is_ValueChanged(I_C_OrderLine.COLUMNNAME_Link_OrderLine_ID)) {
					MOrderLine orderLine = (MOrderLine) po;
					if(orderLine.getLink_OrderLine_ID() > 0) {
						MOrder order = orderLine.getParent();
						if(order.isSOTrx()) {
							MOrderLine linkSourceOrderLine = (MOrderLine) orderLine.getLink_OrderLine();
							MOrder linkSourceOrder = linkSourceOrderLine.getParent();
							if(!linkSourceOrder.isProcessed()) {
								linkSourceOrderLine.setPriceEntered(orderLine.getPriceEntered());
								linkSourceOrderLine.setPriceActual(orderLine.getPriceActual());
								linkSourceOrderLine.saveEx();
							}
						}
					}
				}
			} else if(po instanceof MOrder) {
				if(po.is_ValueChanged(I_C_Order.COLUMNNAME_Link_Order_ID)) {
					MOrder order = (MOrder) po;
					if(order.getLink_Order_ID() > 0
							&& order.isSOTrx()) {
						MOrder linkSourceOrder = (MOrder) order.getLink_Order();
						linkSourceOrder.setDateOrdered(order.getDateOrdered());
						linkSourceOrder.setDatePromised(order.getDatePromised());
						linkSourceOrder.saveEx();
					}
				}
			}
		} else if(type == TYPE_AFTER_CHANGE) {
			if (po instanceof MCommission) {
				MCommission commission = (MCommission) po;
				if(commission.is_ValueChanged("C_BPartner_ID")) {
					if(commission.getC_BPartner_ID() > 0) {
						for(MCommissionLine commissionLine : commission.getLines()) {
							commissionLine.set_ValueOfColumn("Vendor_ID", commission.getC_BPartner_ID());
							commissionLine.saveEx();
						}
					}
				}
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
			//	Validate
			MDocType  documentType = MDocType.get(order.getCtx(), order.getC_DocTypeTarget_ID());
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
				// Document type IsCustomerApproved = Y and order IsCustomerApproved = N
				if (documentType.get_ValueAsBoolean("IsApprovedRequired")) {
					if(!order.get_ValueAsBoolean("IsCustomerApproved")) {
						throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@CustomerApprovedRequired@"));
					}
					MAttachment orderAttachment = order.getAttachment(true);
					if(orderAttachment == null
							|| orderAttachment.getAD_Attachment_ID() <= 0) {
						throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
					}
					//	Validate project reference
					if(order.getC_Project_ID() <= 0) {
						throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@C_Project_ID@ @NotFound@"));
					}
					//	Document type IsCustomerApproved = Y and order IsCustomerApproved Y and order isAttachment("PDF") = N and project IsCustomerApproved = N
					MProject project = new MProject(order.getCtx(), order.getC_Project_ID(), null);
					if (!project.get_ValueAsBoolean("IsCustomerApproved")) {
						throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@CustomerApprovedRequired@ on @C_Project_ID@"));
					}
				}
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
				// Document type IsCustomerApproved = Y and order IsCustomerApproved = N
				if (!documentType.get_ValueAsBoolean("IsApprovedRequired")) {
					return null;
				}
				//	
				if(order.get_ValueAsBoolean("IsCustomerApproved")) {
					//	For drop ship only
					ProcessBuilder.create(order.getCtx())
						.process(OrderPOCreateAbstract.getProcessId())
						.withParameter(OrderPOCreateAbstract.C_ORDER_ID, order.getC_Order_ID())
						.withParameter(OrderPOCreateAbstract.VENDOR_ID, order.getDropShip_BPartner_ID())
						.withoutTransactionClose()
						.execute(order.get_TrxName());
				}
			} else if(timing == TIMING_AFTER_REACTIVATE) {
				if(documentType.get_ValueAsInt("C_CommissionType_ID") > 0) {
					removeLineFromCommission(order, documentType.get_ValueAsInt("C_CommissionType_ID"));
				}
			}
		} else if(po instanceof MTimeExpense) {
			MTimeExpense expenseReport = (MTimeExpense) po;
			if(timing == TIMING_AFTER_COMPLETE) {
				Hashtable<Integer, Hashtable<Integer, BigDecimal>> orders = new Hashtable<Integer, Hashtable<Integer, BigDecimal>>();
				for(MTimeExpenseLine line : expenseReport.getLines()) {
					//	Validate Orders
					int salesOrderLineId = line.getC_OrderLine_ID();
					int linkOrderLineId = line.get_ValueAsInt("Link_OrderLine_ID");
					if(salesOrderLineId <= 0
							&& linkOrderLineId <= 0
							|| line.getQty() == null
							|| line.getQty().compareTo(Env.ZERO) <= 0) {
						continue;
					}
					//	For sales
					if(salesOrderLineId > 0) {
						MOrderLine salesOrderLine = new MOrderLine(expenseReport.getCtx(), salesOrderLineId, expenseReport.get_TrxName());
						Hashtable<Integer, BigDecimal> salesOrderLines = orders.get(salesOrderLine.getC_Order_ID());
						if(salesOrderLines == null) {
							salesOrderLines = new Hashtable<Integer, BigDecimal>();
						}
						//	Add
						salesOrderLines.put(salesOrderLine.getC_OrderLine_ID(), line.getQty());
						orders.put(salesOrderLine.getC_Order_ID(), salesOrderLines);
					}
					//	For purchases
					if(linkOrderLineId > 0) {
						MOrderLine salesOrderLine = new MOrderLine(expenseReport.getCtx(), linkOrderLineId, expenseReport.get_TrxName());
						Hashtable<Integer, BigDecimal> purchaseOrderLines = orders.get(salesOrderLine.getC_Order_ID());
						if(purchaseOrderLines == null) {
							purchaseOrderLines = new Hashtable<Integer, BigDecimal>();
						}
						//	Add
						purchaseOrderLines.put(salesOrderLine.getC_OrderLine_ID(), line.getQty());
						orders.put(salesOrderLine.getC_Order_ID(), purchaseOrderLines);
					}
				}
				//	Generate from orders
				orders.entrySet().stream().forEach(orderSet -> {
					generateInOutFromOrder(expenseReport, orderSet.getKey(), orderSet.getValue());
				});
			}
		}
		return null;
	}	//	docValidate
	
	/**
	 * Generate In/Out from Sales or Purchase Orders
	 * @param orderId
	 * @param lines
	 */
	private void generateInOutFromOrder(MTimeExpense expenseReport, int orderId, Hashtable<Integer, BigDecimal> lines) {
		MOrder order = new MOrder(expenseReport.getCtx(), orderId, expenseReport.get_TrxName());
		MInOut inOut = new MInOut(order, 0, expenseReport.getDateReport());
		inOut.setM_Warehouse_ID(order.getM_Warehouse_ID());
		inOut.saveEx();
		lines.entrySet().stream().forEach(linesSet -> {
			MOrderLine orderLine = new MOrderLine(expenseReport.getCtx(), linesSet.getKey(), expenseReport.get_TrxName());
			BigDecimal toDeliver = linesSet.getValue();
			MInOutLine inOutLine = new MInOutLine (inOut);
			inOutLine.setOrderLine(orderLine, 0, toDeliver);
			inOutLine.setQty(toDeliver);
		    inOutLine.saveEx();
		});
		//	Complete In/Out
		inOut.setDocStatus(MInOut.DOCSTATUS_Drafted);
		inOut.processIt(MInOut.ACTION_Complete);
		inOut.saveEx();
	}
	
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
				commissionRun.set_ValueOfColumn("C_Order_ID", order.getC_Order_ID());
				commissionRun.saveEx();
				//	Process commission
				commissionRun.addFilterValues("C_Order_ID", order.getC_Order_ID());
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
								orderLine.saveEx(order.get_TrxName());
						});
				} else {
					throw new AdempiereException(commissionRun.getProcessMsg());
				}
			});
	}
	
	/**
	 * Remove Line From Commission
	 * @param order
	 */
	private void removeLineFromCommission(MOrder order, int commissionTypeId) {
		String whereClause = " AND EXISTS(SELECT 1 FROM C_Commission c WHERE c.C_CommissionType_ID = " + commissionTypeId 
				+ " AND c.C_Charge_ID = C_OrderLine.C_Charge_ID)";
		for(MOrderLine line : order.getLines(whereClause, "")) {
			line.deleteEx(true);
		}
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