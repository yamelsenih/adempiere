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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_Commission;
import org.compiere.model.I_C_CommissionRun;
import org.compiere.model.I_C_CommissionSalesRep;
import org.compiere.model.I_C_CommissionType;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_C_Project;
import org.compiere.model.I_S_TimeExpense;
import org.compiere.model.I_S_TimeExpenseLine;
import org.compiere.model.MAttachment;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MCommission;
import org.compiere.model.MCommissionLine;
import org.compiere.model.MCommissionRun;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProject;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.model.MRequest;
import org.compiere.model.MRequestType;
import org.compiere.model.MTimeExpense;
import org.compiere.model.MTimeExpenseLine;
import org.compiere.model.MTree;
import org.compiere.model.MTree_NodeBP;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.OrderLineCreateShipmentAbstract;
import org.compiere.process.OrderPOCreateAbstract;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.eevolution.service.dsl.ProcessBuilder;
import org.spin.process.CommissionPOCreateAbstract;

import com.eevolution.model.MSContract;



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
		engine.addModelChange(MInvoice.Table_Name, this);
		engine.addModelChange(MOrderLine.Table_Name, this);
		engine.addModelChange(MCommissionLine.Table_Name, this);
		engine.addModelChange(MProjectTask.Table_Name, this);
		engine.addModelChange(MBPartner.Table_Name, this);
		engine.addModelChange(MRequest.Table_Name, this);
		engine.addDocValidate(MTimeExpense.Table_Name, this);
		engine.addDocValidate(MSContract.Table_Name, this);
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
					int projectPhaseId = orderLine.getC_ProjectPhase_ID();
					int projectTaskId = orderLine.getC_ProjectTask_ID();
					if(orderLine.getLink_OrderLine_ID() > 0) {
						MOrder order = orderLine.getParent();
						if(order.isSOTrx()) {
							MOrderLine generatedOrderLine = (MOrderLine) orderLine.getLink_OrderLine();
							MOrder generatedOrder = generatedOrderLine.getParent();
							if(!generatedOrder.isProcessed()) {
								MDocType sourceDocumentType = MDocType.get(order.getCtx(), order.getC_DocTypeTarget_ID());
								if(sourceDocumentType.get_ValueAsBoolean("IsSetPOPriceFromSO")) {
									generatedOrderLine.setPriceEntered(orderLine.getPriceEntered());
									generatedOrderLine.setPriceActual(orderLine.getPriceActual());
								}
								if(projectPhaseId > 0) {
									generatedOrderLine.setC_ProjectPhase_ID(projectPhaseId);
								} else if(projectTaskId > 0) {
									generatedOrderLine.setC_ProjectTask_ID(projectTaskId);
								}								
								if(orderLine.getC_Campaign_ID() != 0)
									generatedOrderLine.set_ValueOfColumn("C_Campaign_ID", orderLine.getC_Campaign_ID());
								if(orderLine.getUser1_ID() != 0)
									generatedOrderLine.set_ValueOfColumn("User1_ID", orderLine.getUser1_ID());
								if(orderLine.getC_Project_ID() != 0)
									generatedOrderLine.set_ValueOfColumn("C_Project_ID", orderLine.getC_Project_ID());
								if(orderLine.get_ValueAsInt("CUST_MediaType_ID") != 0)
									generatedOrderLine.set_ValueOfColumn("CUST_MediaType_ID", orderLine.get_ValueAsInt("CUST_MediaType_ID"));
								generatedOrderLine.saveEx();
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
						if(order.isDropShip()) {
							linkSourceOrder.set_ValueOfColumn("IsDirectInvoice", order.get_ValueAsBoolean("IsDirectInvoice"));
						}
						linkSourceOrder.saveEx();
					}
				}
			} else if(po instanceof MProjectTask) {
				MProjectTask projectTask = (MProjectTask) po;
				if(projectTask.get_ValueAsBoolean("IsCustomerApproved")){
					if(projectTask.get_ValueAsBoolean("IsApprovedAttachment")) {
						MAttachment projectTaskAttachment = projectTask.getAttachment(true);
						if (projectTaskAttachment == null 
								|| projectTaskAttachment.getAD_Attachment_ID() <= 0) {
							throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
						}
					}
				}
			}else if(po instanceof MRequest) {
				MRequest request = (MRequest) po;
				if(request.getR_RequestType_ID() != 0) {
					MRequestType requestType = new MRequestType(request.getCtx(), request.getR_RequestType_ID(), request.get_TrxName());
					// Validates Approved on Request Type					
					if(requestType.get_ValueAsBoolean("IsApproved")) {
						// Validates Approved 1 on Request						
						if(request.get_ValueAsBoolean("IsApproved1")) {
							// Validates Approved 2 on Request
							if(!request.get_ValueAsBoolean("IsApproved2")) {
								throw new AdempiereException(Msg.getMsg(Env.getCtx(), "NotApproved2"));
							}
						}else {
							throw new AdempiereException(Msg.getMsg(Env.getCtx(), "NotApproved1"));
						}
					}		
				}				
			}
		} else if(type == TYPE_BEFORE_NEW) {
			if (po instanceof MCommissionLine) {
				MCommissionLine commissionLine = (MCommissionLine) po;
				MCommission commission = (MCommission) commissionLine.getC_Commission();
				if(commission != null
						&& commissionLine.get_ValueAsInt("Vendor_ID") <= 0) {
					if(commissionLine.get_ValueAsInt("C_Order_ID") > 0) {
						MOrder order = new MOrder(commission.getCtx(), commissionLine.get_ValueAsInt("C_Order_ID"), commission.get_TrxName());
						commissionLine.set_ValueOfColumn("Vendor_ID", order.getC_BPartner_ID());
					}
				}
			} else if(po instanceof MOrderLine) {
				MOrderLine orderLine = (MOrderLine) po;
				int projectPhaseId = orderLine.getC_ProjectPhase_ID();
				int projectTaskId = orderLine.getC_ProjectTask_ID();
				if(projectPhaseId > 0) {
					MProjectPhase projectPhase = new MProjectPhase(orderLine.getCtx(), projectPhaseId, orderLine.get_TrxName());						
					if(projectPhase.getC_Campaign_ID() != 0)
						orderLine.set_ValueOfColumn("C_Campaign_ID", projectPhase.getC_Campaign_ID());
					if(projectPhase.getUser1_ID() != 0)
						orderLine.set_ValueOfColumn("User1_ID", projectPhase.getUser1_ID());
					if(projectPhase.getC_Project_ID() != 0)
						orderLine.set_ValueOfColumn("C_Project_ID", projectPhase.getC_Project_ID());
					if(projectPhase.get_ValueAsInt("CUST_MediaType_ID") != 0)
						orderLine.set_ValueOfColumn("CUST_MediaType_ID", projectPhase.get_ValueAsInt("CUST_MediaType_ID"));
				} else if(projectTaskId > 0) {
					MProjectTask projectTask = new MProjectTask(orderLine.getCtx(), projectTaskId,orderLine.get_TrxName());
					if(projectTask.getC_Campaign_ID() != 0)
						orderLine.set_ValueOfColumn("C_Campaign_ID", projectTask.getC_Campaign_ID());
					if(projectTask.getUser1_ID() != 0)
						orderLine.set_ValueOfColumn("User1_ID", projectTask.getUser1_ID());
					if(projectTask.get_ValueAsInt("CUST_MediaType_ID") != 0)
						orderLine.set_ValueOfColumn("CUST_MediaType_ID", projectTask.get_ValueAsInt("CUST_MediaType_ID"));
					MProjectPhase projectPhasefromTask = new MProjectPhase(orderLine.getCtx(), projectTask.getC_ProjectPhase_ID(),orderLine.get_TrxName());
					if(projectPhasefromTask.getC_Project_ID() != 0)
					orderLine.set_ValueOfColumn("C_Project_ID", projectPhasefromTask.getC_Project_ID());						
				}
			}else if(po instanceof MOrder) {
				MOrder order = (MOrder) po;
				int orderprojectId = order.getC_Project_ID();
				if(orderprojectId > 0) {					
					MProject project = new MProject(order.getCtx(), orderprojectId,order.get_TrxName());
					// Validates Customer Approved
					if(!project.get_ValueAsBoolean("IsCustomerApproved")) {
						throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@CustomerApprovedRequired@"));
					}
				}
				// Validates Order Has ProjectPorcentaje 
				int serviceContractId = order.get_ValueAsInt("S_Contract_ID");
				if(serviceContractId > 0) {
					MSContract serviceContract = new MSContract(order.getCtx(), serviceContractId, order.get_TrxName());
					//	Get first contract
					int projectId = new Query(order.getCtx(), I_C_Project.Table_Name, "S_Contract_ID = ?", po.get_TrxName())
						.setParameters(serviceContract.getS_Contract_ID())
						.setOnlyActiveRecords(true)
						.firstId();
					if(serviceContract.getUser1_ID() > 0) {
						order.setUser1_ID(serviceContract.getUser1_ID());
					}
					if(projectId > 0) {
						order.setC_Project_ID(projectId);
					}
				}
			}
		} else if(type == TYPE_AFTER_CHANGE) {
			if (po instanceof MBPartner
					&& po.is_ValueChanged(I_C_BPartner.COLUMNNAME_BPartner_Parent_ID)) {
				MBPartner bPartner = (MBPartner) po;
				int treeId = MTree.getDefaultTreeIdFromTableId(bPartner.getAD_Client_ID(), I_C_BPartner.Table_ID);
				if(treeId > 0) {
					MTree tree = MTree.get(bPartner.getCtx(), treeId, null);
					MTree_NodeBP node = MTree_NodeBP.get(tree, bPartner.getC_BPartner_ID());
					if(node != null) {
						int parentId = bPartner.getBPartner_Parent_ID();
						if(parentId < 0) {
							parentId = 0;
						}
						node.setParent_ID(parentId);
						node.saveEx();
					}
				}
			}
		}

		//
		return null;
	}	//	modelChange
	
	@Override
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
						throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@CustomerApprovedRequired@ @C_Project_ID@"));
					}
				}
				//	Validate Document Type for commission
				if(documentType.get_ValueAsInt("C_CommissionType_ID") > 0) {
					createCommissionForOrder(order, documentType.get_ValueAsInt("C_CommissionType_ID"), false);
				}
			} else if (timing == TIMING_AFTER_COMPLETE) {
				// Document type IsCustomerApproved = Y and order IsCustomerApproved = N
				if (documentType.get_ValueAsBoolean("IsApprovedRequired")) {
					if(order.isSOTrx()) {
						//	For Sales Orders only
						if(order.isDropShip()) {
							//	For drop ship only
							ProcessBuilder.create(order.getCtx())
								.process(OrderPOCreateAbstract.getProcessId())
								.withParameter(OrderPOCreateAbstract.C_ORDER_ID, order.getC_Order_ID())
								.withParameter(OrderPOCreateAbstract.VENDOR_ID, order.getDropShip_BPartner_ID())
								.withoutTransactionClose()
								.execute(order.get_TrxName());
						}
						//	Validate Document Type for commission
						if(documentType.get_ValueAsInt("C_CommissionType_ID") > 0) {
							createCommissionForOrder(order, documentType.get_ValueAsInt("C_CommissionType_ID"), true);
						}
					}
				} else if(!order.isSOTrx()) {
					//	Validate Document Type for commission
					if(documentType.get_ValueAsInt("C_CommissionType_ID") > 0) {
						createCommissionForOrder(order, documentType.get_ValueAsInt("C_CommissionType_ID"), true);
					}
				}
				//	Generate Pre-Purchase reverse
				generateReverseAmount(order);
			}
		} else if(po instanceof MTimeExpense) {
			MTimeExpense expenseReport = (MTimeExpense) po;
			
			if(timing == TIMING_BEFORE_COMPLETE) {
				Map<Integer,BigDecimal> projectList = new HashMap<>();
					MTimeExpenseLine[] expenseReportLine = expenseReport.getLines();
						for(int i=0; i<expenseReportLine.length; i++) {
							StringBuffer whereClause = new StringBuffer();
							
							whereClause.append(" EXISTS(SELECT 1 FROm S_TimeExpense te")
									   .append(" WHERE S_TimeExpenseLine.S_TimeExpense_ID=te.S_TimeExpense_ID and te.DocStatus=?)");
							BigDecimal value = new Query(po.getCtx(), I_S_TimeExpenseLine.Table_Name, whereClause.toString(), po.get_TrxName())
									.setClient_ID()
									.setParameters(MTimeExpense.DOCSTATUS_Completed)
									.sum(MTimeExpenseLine.COLUMNNAME_ExpenseAmt);

							int projectId=expenseReportLine[i].getC_Project_ID();
							if(projectId > 0){
								projectList.put(projectId, value);
							}
						}
		
				projectList.forEach((projectId, value)-> {
					MProject project = MProject.getById(po.getCtx(), projectId, null);
					if( project != null){
						BigDecimal plannedAmt = project.getPlannedAmt();
						if(plannedAmt != null
								&& plannedAmt.compareTo(value) < 0){
							throw new AdempiereException(Msg.parseTranslation(Env.getCtx(), "@PlannedAmt@ < @S_TimeExpenseLine_ID@"));
						}
					}
				});				
			} else if(timing == TIMING_AFTER_COMPLETE) {
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
		} else if(po instanceof MInvoice) {
			if(timing == TIMING_BEFORE_PREPARE) {
				MInvoice invoice = (MInvoice) po;
				if(invoice.get_ValueAsInt("S_Contract_ID") <= 0) {
					if(invoice.getC_Project_ID() > 0) {
						MProject parentProject = MProject.getById(invoice.getCtx(), invoice.getC_Project_ID(), invoice.get_TrxName());
						if(parentProject.get_ValueAsInt("S_Contract_ID") > 0) {
							invoice.set_ValueOfColumn("S_Contract_ID", parentProject.get_ValueAsInt("S_Contract_ID"));
							invoice.saveEx();
						}
					}
				}
			}
		} else if(po instanceof MSContract) {
			if(timing == TIMING_BEFORE_COMPLETE) {
				MSContract serviceContract = (MSContract) po;
				String whereClause =(" S_Contract_ID = ?");				
				BigDecimal sumPercent = new Query(po.getCtx(), I_C_CommissionSalesRep.Table_Name, whereClause.toString(), po.get_TrxName())
						.setParameters(serviceContract.getS_Contract_ID())
						.sum("AmtMultiplier");

				BigDecimal comparepercent = new BigDecimal("100.0");
				if(sumPercent.compareTo(comparepercent) != 0){					
					throw new AdempiereException(Msg.getMsg(Env.getCtx(), "TotalPercentageIsNot100"));
				}								
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
		//	Generate Delivery for Commission
		generateInOutFromCommissionOrder(order);
	}
	
	/**
	 * Reverse amount of pre-purchase order from a purchase order
	 * @param sourceOrder
	 */
	private void generateReverseAmount(MOrder sourceOrder) {
		if(sourceOrder.getC_Project_ID() <= 0) {
			return;
		}
		//	
		MDocType documentType = MDocType.get(sourceOrder.getCtx(), sourceOrder.getC_DocTypeTarget_ID());
		if(documentType.get_ValueAsBoolean("IsConsumePreOrder")) {
			//	find all purchase order of pre-purchase
			MOrder preOrder = new Query(sourceOrder.getCtx(), I_C_Order.Table_Name, "DocStatus = 'CO' "
					+ "AND C_Project_ID = ? "
					+ "AND C_BPartner_ID = ? "
					+ "AND IsSOTrx = '" + (sourceOrder.isSOTrx()? "Y": "N") + "' "
					+ "AND EXISTS(SELECT 1 FROM C_DocType dt WHERE dt.C_DocType_ID = C_Order.C_DocType_ID AND dt.IsPreOrder = 'Y')", sourceOrder.get_TrxName())
				.setParameters(sourceOrder.getC_Project_ID(), sourceOrder.getC_BPartner_ID())
				.first();
			//	Validate
			if(preOrder != null
					&& preOrder.getC_Order_ID() > 0) {
				MOrder reverseOrder = new MOrder(sourceOrder.getCtx(), 0, sourceOrder.get_TrxName());
				PO.copyValues(preOrder, reverseOrder);
				reverseOrder.setDocumentNo(null);
				reverseOrder.setDateOrdered(sourceOrder.getDateOrdered());
				reverseOrder.setDatePromised(sourceOrder.getDatePromised());
				reverseOrder.addDescription(Msg.parseTranslation(sourceOrder.getCtx(), "@Generated@ [@C_Order_ID@ " + sourceOrder.getDocumentNo()) + "]");
				reverseOrder.setDocStatus(MOrder.DOCSTATUS_Drafted);
				reverseOrder.setDocAction(MOrder.DOCACTION_Complete);
				reverseOrder.setTotalLines(Env.ZERO);
				reverseOrder.setGrandTotal(Env.ZERO);
				reverseOrder.setIsSOTrx(sourceOrder.isSOTrx());
				reverseOrder.saveEx();
				//	Add Line
				MOrderLine preOrderLine = preOrder.getLines(true, null)[0];
				
				MOrderLine reverseOrderLine = new MOrderLine(reverseOrder);
				PO.copyValues(reverseOrderLine, preOrderLine);
				reverseOrderLine.setOrder(reverseOrder);
				reverseOrderLine.setProduct(preOrderLine.getProduct());
				reverseOrderLine.setLineNetAmt(Env.ZERO);
				reverseOrderLine.setQty(Env.ONE);
				reverseOrderLine.setPrice(sourceOrder.getTotalLines().negate());
				reverseOrderLine.setTax();
				reverseOrderLine.saveEx();
				//	Complete
				if(!reverseOrder.processIt(MOrder.DOCACTION_Complete)) {
					throw new AdempiereException(reverseOrder.getProcessMsg());
				}
			}
		}
	}
	
	/**
	 * Generate Delivery from commission Order that are generated from order
	 * @param order
	 */
	private void generateInOutFromCommissionOrder(MOrder order) {
		new Query(order.getCtx(), I_C_Order.Table_Name, 
				"DocStatus = 'CO' "
				+ "AND EXISTS(SELECT 1 FROM C_CommissionRun cr "
				+ "WHERE cr.C_CommissionRun_ID = C_Order.C_CommissionRun_ID "
				+ "AND cr.C_Order_ID = ?) "
				+ "AND EXISTS(SELECT 1 FROM C_OrderLine ol "
				+ "WHERE ol.C_Order_ID = C_Order.C_Order_ID "
				+ "AND ol.QtyOrdered > COALESCE(QtyDelivered, 0))", order.get_TrxName())
			.setOnlyActiveRecords(true)
			.setParameters(order.getC_Order_ID())
			.<MOrder>list()
			.stream().forEach(commissionOrder -> {
				for(MOrderLine line : commissionOrder.getLines()) {
					ProcessBuilder.create(order.getCtx())
						.process(OrderLineCreateShipmentAbstract.getProcessId())
						.withRecordId(I_C_OrderLine.Table_ID, line.getC_OrderLine_ID())
						.withParameter(OrderLineCreateShipmentAbstract.DOCACTION, DocAction.ACTION_Complete)
						.withoutTransactionClose()
					.execute(order.get_TrxName());
				}
			});
	}
	
	/**
	 * Create a commission based on rules defined and get result inside order line 
	 * it is only running if exists a flag for document type named (Calculate commission for Order)
	 * @param order
	 * @param
	 */
	private void createCommissionForOrder(MOrder order, int commissionTypeId, boolean splitDocuments) {
		removeLineFromCommission(order, commissionTypeId);
		new Query(order.getCtx(), I_C_Commission.Table_Name, I_C_CommissionType.COLUMNNAME_C_CommissionType_ID + " = ? "
				+ "AND IsSplitDocuments = ?", order.get_TrxName())
			.setOnlyActiveRecords(true)
			.setParameters(commissionTypeId, (splitDocuments? "Y": "N"))
			.<MCommission>list().forEach(commissionDefinition -> {
				int documentTypeId = MDocType.getDocType(MDocType.DOCBASETYPE_SalesCommission, order.getAD_Org_ID());
				MCommissionRun commissionRun = new MCommissionRun(commissionDefinition);
				commissionRun.setDateDoc(order.getDateOrdered());
				commissionRun.setC_DocType_ID(documentTypeId);
				commissionRun.setDescription(Msg.parseTranslation(order.getCtx(), "@Generate@: @C_Order_ID@ - " + order.getDocumentNo()));
				commissionRun.set_ValueOfColumn("C_Order_ID", order.getC_Order_ID());
				commissionRun.setAD_Org_ID(order.getAD_Org_ID());
				commissionRun.saveEx();
				//	Process commission
				commissionRun.addFilterValues("C_Order_ID", order.getC_Order_ID());
				commissionRun.setDocStatus(MCommissionRun.DOCSTATUS_Drafted);
				//	Complete
				if(commissionRun.processIt(MCommissionRun.DOCACTION_Complete)) {
					commissionRun.updateFromAmt();
					commissionRun.saveEx();
					if(commissionRun.getGrandTotal() != null
							&& commissionRun.getGrandTotal().compareTo(Env.ZERO) > 0) {
						if(commissionDefinition.get_ValueAsBoolean("IsSplitDocuments")) {
							ProcessBuilder.create(order.getCtx())
								.process(CommissionPOCreateAbstract.getProcessId())
								.withRecordId(I_C_CommissionRun.Table_ID, commissionRun.getC_CommissionRun_ID())
								.withParameter(CommissionPOCreateAbstract.ISSOTRX, true)
								.withParameter(CommissionPOCreateAbstract.DATEORDERED, order.getDateOrdered())
								.withParameter(CommissionPOCreateAbstract.DOCACTION, DocAction.ACTION_Complete)
								.withParameter(CommissionPOCreateAbstract.C_BPARTNER_ID, order.getC_BPartner_ID())
								.withParameter(CommissionPOCreateAbstract.C_DOCTYPE_ID, commissionDefinition.get_ValueAsInt("C_DocTypeOrder_ID"))
								.withoutTransactionClose()
							.execute(order.get_TrxName());
						} else {
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
						}
					}
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