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

import java.math.BigDecimal;
import java.util.logging.Level;

import org.compiere.model.I_C_Campaign;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_Project;
import org.compiere.model.I_C_ProjectPhase;
import org.compiere.model.I_C_ProjectTask;
import org.compiere.model.I_C_RfQ;
import org.compiere.model.MBPartner;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRfQ;
import org.compiere.model.MRfQLine;
import org.compiere.model.MRfQLineQty;
import org.compiere.util.Env;


/**
 *	Create SO for RfQ.
 *	
 *  @author Jorg Janke
 *  @version $Id: RfQCreateSO.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class RfQCreateSO extends SvrProcess
{
	/**	RfQ 			*/
	private int		p_C_RfQ_ID = 0;
	private int		p_C_DocType_ID = 0;

	/**	100						*/
	private static BigDecimal 	ONEHUNDRED = new BigDecimal (100);

	/**
	 * 	Prepare
	 */
	protected void prepare ()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_DocType_ID"))
				p_C_DocType_ID = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
		p_C_RfQ_ID = getRecord_ID();
	}	//	prepare

	/**
	 * 	Process.
	 * 	A Sales Order is created for the entered Business Partner.  
	 * 	A sales order line is created for each RfQ line quantity, 
	 * 	where "Offer Quantity" is selected.  
	 * 	If on the RfQ Line Quantity, an offer amount is entered (not 0), 
	 * 	that price is used. 
	 *	If a magin is entered on RfQ Line Quantity, it overwrites the 
	 *	general margin.  The margin is the percentage added to the 
	 *	Best Response Amount.
	 *	@return message
	 */
	protected String doIt () throws Exception
	{
		MRfQ rfq = new MRfQ (getCtx(), p_C_RfQ_ID, get_TrxName());
		if (rfq.get_ID() == 0)
			throw new IllegalArgumentException("No RfQ found");
		log.info("doIt - " + rfq);
		
		if (rfq.getC_BPartner_ID() == 0 || rfq.getC_BPartner_Location_ID() == 0)
			throw new Exception ("No Business Partner/Location");
		MBPartner bp = new MBPartner (getCtx(), rfq.getC_BPartner_ID(), get_TrxName());
		
		MOrder order = new MOrder (getCtx(), 0, get_TrxName());
		order.setIsSOTrx(true);
		if (p_C_DocType_ID != 0)
			order.setC_DocTypeTarget_ID(p_C_DocType_ID);
		else
			order.setC_DocTypeTarget_ID();
		order.setBPartner(bp);
		order.setC_BPartner_Location_ID(rfq.getC_BPartner_Location_ID());
		order.setSalesRep_ID(rfq.getSalesRep_ID());
		//	Set default values
		order.set_ValueOfColumn(I_C_RfQ.COLUMNNAME_C_RfQ_ID, rfq.getC_RfQ_ID());
		//	
		int campaignId = rfq.get_ValueAsInt(I_C_Campaign.COLUMNNAME_C_Campaign_ID);
		int user1Id = rfq.get_ValueAsInt(I_C_Invoice.COLUMNNAME_User1_ID);
		int projectId = rfq.get_ValueAsInt(I_C_Project.COLUMNNAME_C_Project_ID);
		if(campaignId > 0){
			order.set_ValueOfColumn(I_C_Campaign.COLUMNNAME_C_Campaign_ID, campaignId);
		}
		if(user1Id > 0){
			order.set_ValueOfColumn(I_C_Invoice.COLUMNNAME_User1_ID, user1Id);
		}
		if(projectId > 0){
			order.set_ValueOfColumn(I_C_Project.COLUMNNAME_C_Project_ID, projectId);
		}
		if (rfq.getDateWorkComplete() != null)
			order.setDatePromised(rfq.getDateWorkComplete());
		order.saveEx();

		for (MRfQLine line : rfq.getLines()) {
			for (MRfQLineQty lineQty : line.getQtys()) {
				if (lineQty.isActive() && lineQty.isOfferQty()) {
					MOrderLine orderLine = new MOrderLine (order);
					orderLine.setM_Product_ID(line.getM_Product_ID(), lineQty.getC_UOM_ID());
					orderLine.setDescription(line.getDescription());
					orderLine.setQty(lineQty.getQty());
					//
					BigDecimal price = lineQty.getOfferAmt();
					if (price == null || price.signum() == 0)
					{
						price = lineQty.getBestResponseAmt();
						if (price == null || price.signum() == 0)
						{
							price = Env.ZERO;
							log.warning(" - BestResponse=0 - " + lineQty);
						}
						else
						{
							BigDecimal margin = lineQty.getMargin();
							if (margin == null || margin.signum() == 0)
								margin = rfq.getMargin();
							if (margin != null && margin.signum() != 0)
							{
								margin = margin.add(ONEHUNDRED);
								price = price.multiply(margin)
									.divide(ONEHUNDRED, 2, BigDecimal.ROUND_HALF_UP);
							}
						}
					}	//	price
					orderLine.setPrice(price);
					int projectTaskId = line.get_ValueAsInt(I_C_ProjectTask.COLUMNNAME_C_ProjectTask_ID);
					int projectPhaseId = line.get_ValueAsInt(I_C_ProjectTask.COLUMNNAME_C_ProjectPhase_ID);
					//	Validate
					if(projectPhaseId > 0) {
						orderLine.set_ValueOfColumn(I_C_ProjectPhase.COLUMNNAME_C_ProjectPhase_ID, projectPhaseId);
					}
					if(projectTaskId > 0) {
						orderLine.set_ValueOfColumn(I_C_ProjectTask.COLUMNNAME_C_ProjectTask_ID, projectTaskId);
					}
					orderLine.saveEx();
				}	//	Offer Qty
			}	//	All Qtys
		}	//	All Lines

		//
		rfq.setC_Order_ID(order.getC_Order_ID());
		rfq.saveEx();
		return order.getDocumentNo();
	}	//	doIt
}
