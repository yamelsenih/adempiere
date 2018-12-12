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
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MProject;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
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
			
			if (!project.isAttachment("PDF") && project.get_Value("IsApprovedAttachment")=="Y") {
				throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
			}
			
		}		
				//
		return null;
	}	//	modelChange
	
	public String docValidate (PO po, int timing) {
		log.info(po.get_TableName() + " Timing: "+timing);

		if (po instanceof MOrder && (timing == TIMING_BEFORE_COMPLETE || timing == TIMING_BEFORE_PREPARE)) {
			MOrder order = (MOrder) po;
//			
			MDocType  doctype = new MDocType(order.getCtx(),order.getC_DocTypeTarget_ID(), order.get_TrxName());
				
			if (doctype.get_ValueAsBoolean("IsApprovedRequired")== true && order.get_ValueAsBoolean("IsCanApproveOwnDoc")== false) {
				throw new AdempiereException(Msg.getMsg(Env.getCtx(), "CustomerApprovedRequired"));
			}
//			
		}
		
		if (po instanceof MOrder && (timing == TIMING_BEFORE_COMPLETE || timing == TIMING_BEFORE_PREPARE)) {
			MOrder order = (MOrder) po;
			if (!order.isAttachment("PDF") && order.get_Value("IsCanApproveOwnDoc")=="Y") {
				throw new AdempiereException(Msg.getMsg(Env.getCtx(), "AttachmentNotFound"));
			}
		}
		
		
		if (po instanceof MOrder && timing == TIMING_AFTER_COMPLETE) {
			MOrder order = (MOrder) po;
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
		return null;
	}	//	docValidate	

	@Override
	public int getAD_Client_ID() {
		return clientId;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}
}	//	AgencyValidator