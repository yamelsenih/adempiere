
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
package org.spin.process;

import java.math.BigDecimal;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
//import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MLocator;
import org.compiere.model.MProduct;
import org.compiere.model.MRequest;
import org.compiere.model.MRequestUpdate;
import org.compiere.model.MStatus;
import org.compiere.model.MWarehouse;
import org.compiere.model.Query;
import org.compiere.model.X_M_Inventory;
import org.compiere.model.X_R_Request;
import org.compiere.process.DocAction;
//import org.compiere.util.DB;
import org.compiere.util.Msg;
//import org.eevolution.process.CreatePOFromRequisitionLines;

/**
 * 	Create Inventory Use Internal from Request
 */
public class InventoryCreateFromRequest extends InventoryCreateFromRequestAbstract {
	/** Document Type Target 	*/
//	private int			p_C_DocTypeTarget_ID = 0;
	/**	Document Date			*/
//	private Timestamp 	p_DateDoc = null;
	/**	Document Action			*/
	private String 		p_DocAction = DocAction.ACTION_Complete;
	/**	Consolidate Document	*/
	private boolean		p_ConsolidateDocument = false;

	/**	Current Request ID		*/
	private int			m_CurrentRequest_ID;
	/**	Current Inventory		*/
	private MInventory	currentInventory;
	/** Inventory Cache : (M_Inventory_ID) -> MInventory */
	private ArrayList<MInventory> inventoryList = new ArrayList<MInventory>();
	/**	Request to verify		*/
	private HashMap<Integer, MRequest> requestList = new HashMap<Integer, MRequest>();


	/**
	 *  Prepare - e.g., get Parameters.
	 */

	protected void prepare() {  
		super.prepare();
	} 
	
	
	//	prepare

	/**
	 * 	Process
	 *	@return info
	 *	@throws Exception
	 */
	protected String doIt() throws Exception {
		//	Validate Document Type Target
		if(getDocTypeTargetId() == 0)
			throw new AdempiereException("@C_DocTypeTarget_ID@ @NotFound@");
		//	Validate Document Date
		if(getDateDoc() == null)
			throw new AdempiereException("@DateDoc@ @NotFound@");
		//	Validate Document Action
		if(getDocAction() == null)
			throw new AdempiereException("@DocAction@ @NotFound@");
		
		
//		if(getRecord_ID() == 0)
//			return "";
//		AtomicInteger referenceId = new AtomicInteger(0);
//		AtomicInteger 	created = new AtomicInteger(0);
		
		
		List<Integer> recordIds =  getSelectionKeys();
		
		recordIds.stream().forEach( key -> {
			// variable values
			int requestId = getSelectionAsInt(key, "IUR_R_Request_ID");
			int requestUpdateId = getSelectionAsInt(key, "IUR_R_RequestUpdate_ID");
			int locatorId = getSelectionAsInt(key, "IUR_M_Locator_ID");
			BigDecimal qtyInternalUse = getSelectionAsBigDecimal(key, "IUR_QtyInternalUse");
			BigDecimal qtyAvailable = getSelectionAsBigDecimal(key, "IUR_QtyAvailable");
			BigDecimal qtyToDeliver = getSelectionAsBigDecimal(key, "IUR_QtyToDeliver");

			//	Process Line
			try {
				process(requestId, requestUpdateId, 
						locatorId, qtyInternalUse, 
						qtyAvailable, qtyToDeliver);
			} catch (Exception e) {
				log.log(Level.SEVERE, e.toString(), e);
			}
		});	
				
		//	Complete inventory
		completeInventory();
		//	Close request
		closeAllRequest();
		//	
		StringBuffer msg = new StringBuffer("@Created@ (")
				.append(inventoryList.size()).append(")");
		//	
		StringBuffer detail = new StringBuffer();
		//	Return
		for(MInventory inventory : inventoryList) {
			if(detail.length() > 0)
				detail.append(", ");
			//	
			detail.append(inventory.getDocumentNo());
		}
		//	
		if(detail.length() > 0) {
			msg.append("[").append(detail).append("]");
		}
		//	
		return msg.toString();
	}	//	doit

	/**
	 * 	Process Line
	 *	@param rLine request line
	 * 	@throws Exception
	 */
	private void process(int p_R_Request_ID, int p_R_RequestUpdate_ID, 
			int p_M_Locator_ID, BigDecimal p_QtyInternalUse, 
			BigDecimal p_QtyAvailable, BigDecimal p_QtyToDeliver) throws Exception {
		//	Instance Request
		MRequest request = new MRequest(getCtx(), p_R_Request_ID, get_TrxName());
		MRequestUpdate requestUpdate = new MRequestUpdate(getCtx(), p_R_RequestUpdate_ID, get_TrxName());
		MLocator locator = MLocator.get(getCtx(), p_M_Locator_ID);
		MProduct product = MProduct.get(getCtx(), requestUpdate.getM_ProductSpent_ID());
		//	
		if(p_QtyToDeliver == null
				|| p_QtyToDeliver.signum() <= 0) {
			log.warning("Request Line ignored because quantity is zero " + requestUpdate);
			return;
		}
		//	Validate if exceed delivered
		if(requestUpdate.getQtySpent()
				.subtract(p_QtyInternalUse)
				.subtract(p_QtyToDeliver).signum() < 0) {
			throw new AdempiereException("@QtyToDeliver@ > @Request_Qty@");
		}
		//	Validate if exceed available
		if(p_QtyAvailable.subtract(p_QtyToDeliver).signum() < 0) {
			throw new AdempiereException("@QtyToDeliver@ > @QtyAvailable@");
		}
		//	Create new inventory
		if(p_R_Request_ID != m_CurrentRequest_ID) {
			m_CurrentRequest_ID = p_R_Request_ID;
			//	
			//	Validate Locator
			if(locator == null) {
				throw new AdempiereException("@M_Locator_ID@ @NotFound@");
			}
			boolean isNew = false;
			//	Create
			if(currentInventory == null
					|| !p_ConsolidateDocument) {
				currentInventory = new MInventory((MWarehouse) locator.getM_Warehouse(), get_TrxName());
				currentInventory.setMovementDate(getDateDoc());
				currentInventory.setC_DocType_ID(getDocTypeTargetId());
				currentInventory.set_ValueOfColumn("IsInternal", true);
				isNew = true;
				//	Add to list for complete
				if(!p_ConsolidateDocument) {
					currentInventory.setDescription(Msg.parseTranslation(getCtx(), 
							"@Created@ @From@ @R_Request_ID@ ") + request.getDocumentNo() 
							+ " " + request.getSummary());
					//	Add Project
					if(request.getC_Project_ID() != 0)
						currentInventory.setC_Project_ID(request.getC_Project_ID());
					//	Add Activity
					if(request.getC_Activity_ID() != 0)
						currentInventory.setC_Activity_ID(request.getC_Activity_ID());
					//	Add Campaign
					if(request.getC_Campaign_ID() != 0)
						currentInventory.setC_Campaign_ID(request.getC_Campaign_ID());
				}
				//	Save
				currentInventory.saveEx();
			}
			//	
			if(isNew) {
				inventoryList.add(currentInventory);
			}
		}
		//	
		MInventoryLine line = new MInventoryLine(getCtx(), 0, get_TrxName());
		//	Add attributes
		line.setM_Inventory_ID(currentInventory.getM_Inventory_ID());
		line.setM_Locator_ID(p_M_Locator_ID);
		line.setM_Product_ID(requestUpdate.getM_ProductSpent_ID());
		line.setQtyInternalUse(p_QtyToDeliver);
		//	Add Project from Request or RequestUpdate
		if(requestUpdate.get_ValueAsInt("C_Project_ID") > 0) {
			line.set_ValueOfColumn("C_Project_ID", requestUpdate.get_ValueAsInt("C_Project_ID"));
		} else if(request.getC_Project_ID() != 0) {
			line.set_ValueOfColumn("C_Project_ID", request.getC_Project_ID());
		}
		//	Add Activity from Request or RequestUpdate
		if(requestUpdate.get_ValueAsInt("C_Activity_ID") > 0) {
			line.set_ValueOfColumn("C_Activity_ID", requestUpdate.get_ValueAsInt("C_Activity_ID"));
		} else if(request.getC_Activity_ID() != 0) {
			line.set_ValueOfColumn("C_Activity_ID", request.getC_Activity_ID());
		}
		//	Add Campaign from Request or RequestUpdate
		if(requestUpdate.get_ValueAsInt("C_Campaign_ID") > 0) {
			line.set_ValueOfColumn("C_Campaign_ID", requestUpdate.get_ValueAsInt("C_Campaign_ID"));
		} else if(request.getC_Campaign_ID() != 0) {
			line.set_ValueOfColumn("C_Campaign_ID", request.getC_Campaign_ID());
		}
		//	Get Charge from request update
		line.setC_Charge_ID(requestUpdate.get_ValueAsInt("C_Charge_ID"));
		//	Set UOM Reference
		line.set_ValueOfColumn("C_UOM_ID", product.getC_UOM_ID());
		//	Reference to Request Update
		line.set_ValueOfColumn("R_RequestUpdate_ID", requestUpdate.getR_RequestUpdate_ID());
		line.saveEx();
		//	Add to list
		requestList.put(request.getR_Request_ID(), null);
	}	//	process

	/**
	 * Close All request when it are completely delivered
	 */
	private void closeAllRequest() {
		Set<Integer> requestIdList = requestList.keySet();
		//	Validate if exist
		if(requestIdList.size() == 0)
			return;
		StringBuffer whereClause = new StringBuffer("R_Request.R_Request_ID IN(0");
		//	
		for(int requestId : requestList.keySet()) {
			whereClause.append(", ").append(requestId);
		}
		//	
		whereClause.append(") AND "
				+ "NOT EXISTS(SELECT 1 FROM R_RequestUpdate rl "
				+ "				LEFT JOIN (SELECT il.R_RequestUpdate_ID, SUM(il.QtyInternalUse) QtyInternalUse "
				+ "								FROM M_InventoryLine il "
				+ "								WHERE EXISTS(SELECT 1 FROM M_Inventory i "
				+ "												WHERE i.M_Inventory_ID = il.M_Inventory_ID "
				+ "												AND i.DocStatus IN('CO', 'CL')) "
				+ "								GROUP BY il.R_RequestUpdate_ID) ir ON(ir.R_RequestUpdate_ID = rl.R_RequestUpdate_ID) "
				+ "				WHERE rl.R_Request_ID = R_Request.R_Request_ID "
				+ "				AND rl.QtySpent > ir.QtyInternalUse)");
		//	Get from query
		List<MRequest> requestUpdateList = new Query(getCtx(), X_R_Request.Table_Name,
				whereClause.toString(), get_TrxName()).
				<MRequest>list();
		//	
		MStatus []requestStatus = MStatus.getClosed(getCtx());
		int rStatusId = 0;
		if(requestStatus != null
				&& requestStatus.length > 0) {
			rStatusId = requestStatus[0].getR_Status_ID();
		}
		//	Close all
		for(MRequest request : requestUpdateList) {
			if(rStatusId != 0) {
				request.setR_Status_ID(rStatusId);
			} else {
				request.setProcessed(true);
			}
			//	Save
			request.saveEx();
		}
		//	
	}

	/**
	 * Complete all inventory
	 */
	private void completeInventory() {
		for(MInventory inventory : inventoryList) {
			inventory.setDocAction(p_DocAction);
			inventory.processIt(p_DocAction);
			inventory.saveEx();
			//	Validate Complete Document Status
			if(inventory.getDocStatus() != X_M_Inventory.DOCSTATUS_Completed)
				throw new AdempiereException(inventory.getProcessMsg());
		}
	}
}	//	InventoryCreateFromRequest
