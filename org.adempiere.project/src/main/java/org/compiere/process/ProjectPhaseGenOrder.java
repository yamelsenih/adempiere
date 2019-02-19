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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Order;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProject;
import org.compiere.model.MProjectLine;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.model.PO;
import org.compiere.util.Env;


/**
 *  Generate Order from Project Phase
 *
 *	@author Jorg Janke
 *	@version $Id: ProjectPhaseGenOrder.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class ProjectPhaseGenOrder  extends ProjectPhaseGenOrderAbstract
{
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		super.prepare();
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		int documentTypeTargetId = getParameterAsInt(I_C_Order.COLUMNNAME_C_DocTypeTarget_ID);
		int projectId = 0;
		MProjectPhase phase;
		List<MProjectLine> projectLines = null;
		List<MProjectTask> tasks = null;
		HashMap<String, Object> values = new HashMap<String, Object>();
		log.info("doIt - C_ProjectPhase_ID=" + getRecord_ID());
		if (getRecord_ID() == 0)
			throw new IllegalArgumentException("C_ProjectPhase_ID == 0");
		if (getDocSubTypeSO()==null)
			throw new AdempiereException("@NotFound@ @DocSubTypeSO@");
		PO fromPhase; 
		
		if(MProjectTask.Table_Name.equals(getTableName())) {
			fromPhase = new MProjectTask (getCtx(), getRecord_ID(), get_TrxName());
			int phaseId = ((MProjectTask)fromPhase).getC_ProjectPhase_ID();
			phase = new MProjectPhase(getCtx(), phaseId, get_TrxName());
			projectId = phase.getC_Project_ID();
			values.put("Name", ((MProjectTask)fromPhase).getName());
			values.put("M_Product_ID", ((MProjectTask)fromPhase).getM_Product_ID());
			values.put("SeqNo", ((MProjectTask)fromPhase).getSeqNo());
			values.put("Description", ((MProjectTask)fromPhase).getDescription());
			values.put("Qty", ((MProjectTask)fromPhase).getQty());
			values.put("C_ProjectPhase_ID", ((MProjectTask)fromPhase).getC_ProjectPhase_ID());
			values.put("PriceActual", 0);
			
			projectLines =Arrays.asList(((MProjectTask)fromPhase).getLines());
		}
		else if(MProjectPhase.Table_Name.equals(getTableName())) {
			fromPhase = new MProjectPhase (getCtx(), getRecord_ID(), get_TrxName());
			projectId = ((MProjectPhase)fromPhase).getC_Project_ID();
			values.put("Name", ((MProjectPhase)fromPhase).getName());
			values.put("M_Product_ID", ((MProjectPhase)fromPhase).getM_Product_ID());
			values.put("SeqNo", ((MProjectPhase)fromPhase).getSeqNo());
			values.put("Description", ((MProjectPhase)fromPhase).getDescription());
			values.put("Qty", ((MProjectPhase)fromPhase).getQty());
			values.put("C_ProjectPhase_ID", ((MProjectPhase)fromPhase).getC_ProjectPhase_ID());
			values.put("PriceActual", ((MProjectPhase)fromPhase).getPlannedAmt());
			projectLines =((MProjectPhase)fromPhase).getLines();
			tasks = ((MProjectPhase)fromPhase).getTasks();
		}
		
		MProduct product = new MProduct(getCtx(),(int)values.get("M_Product_ID"),get_TrxName());
		int uom = product.getC_UOM_ID();
		MProject fromProject = ProjectGenOrder.getProject (getCtx(), projectId, get_TrxName());
		if (fromProject.getC_PaymentTerm_ID() <= 0)
			throw new AdempiereException("@C_PaymentTerm_ID@ @NotFound@");

		MOrder order = new MOrder (fromProject, true, getDocSubTypeSO());
		//	Add Document Type Target
		if(documentTypeTargetId > 0) {
			order.setC_DocTypeTarget_ID(documentTypeTargetId);
		}
		order.setDescription(order.getDescription() + " - " + values.get("Name"));
		order.saveEx();
		
		//	Create an order on Phase Level
		if ((int)values.get("M_Product_ID") != 0) {
			MOrderLine orderLine = new MOrderLine(order);
			orderLine.setLine((int)values.get("SeqNo"));
			StringBuilder stringBuilder = new StringBuilder(values.get("Name").toString());
			if (values.get("Description") != null && values.get("Description").toString().length() > 0)
				stringBuilder.append(" - ").append(values.get("Description").toString());
			orderLine.setDescription(stringBuilder.toString());
			//
			orderLine.setM_Product_ID((int)values.get("M_Product_ID"), true);
			orderLine.setQty(new BigDecimal(values.get("Qty").toString()));
			orderLine.setC_Project_ID(fromProject.getC_Project_ID());
			orderLine.setC_ProjectPhase_ID((int)values.get("C_ProjectPhase_ID"));
			orderLine.setPrice();
			orderLine.setC_UOM_ID(uom);
			BigDecimal price = new BigDecimal(values.get("PriceActual").toString());
			if (price!= null && price.compareTo(Env.ZERO) != 0)
				orderLine.setPrice(price);
			orderLine.setTax();
			if (!orderLine.save())
				log.log(Level.SEVERE, "doIt - Lines not generated");
			return "@C_Order_ID@ " + order.getDocumentNo() + " (1)";
		}

		//	Project Phase Lines
		AtomicInteger count = new AtomicInteger(0);
		if(projectLines != null) {
		projectLines.stream()
				.forEach(projectLine -> {
					MOrderLine orderLine = new MOrderLine(order);
					orderLine.setLine(projectLine.getLine());
					orderLine.setDescription(projectLine.getDescription());
					//
					orderLine.setM_Product_ID(projectLine.getM_Product_ID(), true);
					MProduct productLine = new MProduct(getCtx(),projectLine.getM_Product_ID(),get_TrxName());
					int uomLine = productLine.getC_UOM_ID();
					
					orderLine.setQty(projectLine.getPlannedQty().subtract(projectLine.getInvoicedQty()));
					orderLine.setPrice();
					if (projectLine.getPlannedPrice() != null && projectLine.getPlannedPrice().compareTo(Env.ZERO) != 0)
						orderLine.setPrice(projectLine.getPlannedPrice());
					orderLine.setDiscount();
					orderLine.setTax();
					orderLine.setC_Project_ID(fromProject.getC_Project_ID());
					orderLine.setC_ProjectPhase_ID(projectLine.getC_ProjectPhase_ID());
					orderLine.setC_UOM_ID(uomLine);
					orderLine.saveEx();
					count.getAndUpdate(no -> no + 1);
				});    //	for all lines
		if (projectLines.size() != count.get())
			log.log(Level.SEVERE, "Lines difference - ProjectLines=" + projectLines.size() + " <> Saved=" + count.get());
		}
		//	Project Tasks
		if(tasks != null) {
			tasks.stream().filter(task -> task.getM_Product_ID() != 0).forEach(fromTask -> {
				{
					MOrderLine orderLine = new MOrderLine(order);
					orderLine.setLine(fromTask.getSeqNo());
					StringBuilder stringBuilder = new StringBuilder(fromTask.getName());
					if (fromTask.getDescription() != null && fromTask.getDescription().length() > 0)
						stringBuilder.append(" - ").append(fromTask.getDescription());
					orderLine.setDescription(stringBuilder.toString());
					orderLine.setM_Product_ID(fromTask.getM_Product_ID(), true);
					MProduct productLine = new MProduct(getCtx(),fromTask.getM_Product_ID(),get_TrxName());
					int uomLine = productLine.getC_UOM_ID();
					
					orderLine.setQty(fromTask.getQty());
					orderLine.setPrice();
					orderLine.setC_Project_ID(fromProject.getC_Project_ID());
					orderLine.setC_ProjectPhase_ID(fromTask.getC_ProjectPhase_ID());
					orderLine.setC_ProjectTask_ID(fromTask.getC_ProjectTask_ID());
					orderLine.setTax();

					orderLine.setC_UOM_ID(uomLine);
					orderLine.saveEx();
					count.getAndUpdate(no -> no + 1);
				}
				});    //	for all lines
			if (tasks.size() != count.get() - projectLines.size())
				log.log(Level.SEVERE, "doIt - Lines difference - ProjectTasks=" + tasks.size() + " <> Saved=" + count.get());
		}
		return "@C_Order_ID@ " + order.getDocumentNo() + " (" + count + ")";
	}	//	doIt

}	//	ProjectPhaseGenOrder
