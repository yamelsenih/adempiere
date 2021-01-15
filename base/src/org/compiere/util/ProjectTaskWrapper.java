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
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package org.compiere.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.compiere.model.I_C_ProjectLine;
import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_ProjectLine;
import org.compiere.model.X_C_ProjectPhase;
import org.compiere.model.X_C_ProjectTask;
import org.compiere.model.X_C_Task;

/**
 * Project Wrapper class used because the project have wrong links with base source folder
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class ProjectTaskWrapper {
	
	/**
	 * Static instance
	 * @param projectTask
	 * @return
	 */
	public static ProjectTaskWrapper newInstance(X_C_ProjectTask projectTask) {
		return new ProjectTaskWrapper(projectTask);
	}
	
	/***
	 * Private constructor
	 * @param projectTask
	 */
	private ProjectTaskWrapper(X_C_ProjectTask projectTask) {
		this.projectTask = projectTask;
	}
	
	private X_C_ProjectTask projectTask;
	/**	Logger							*/
	protected CLogger log = CLogger.getCLogger (getClass());
	/**
	 * @return the project
	 */
	public final X_C_ProjectTask getProjectTask() {
		return projectTask;
	}
	
	/**************************************************************************
	 * 	Get Project Lines
	 * 	BF 3067850 - monhate
	 *	@return Array of lines
	 */	
	public List<X_C_ProjectLine> getLines() {
		final String whereClause = "C_ProjectPhase_ID=? and C_ProjectTask_ID=? ";
		return new Query(projectTask.getCtx(), I_C_ProjectLine.Table_Name, whereClause, projectTask.get_TrxName())
			.setParameters(projectTask.getC_ProjectPhase_ID(), projectTask.getC_ProjectTask_ID())
			.setOrderBy("Line")
			.list();		
	}
	 
	/**
	 * 	Copy Lines from other Task
	 * 	BF 3067850 - monhate
	 *	@param fromTask from Task
	 *	@return number of lines copied
	 */
	public int copyLinesFrom (X_C_ProjectTask fromTask) {
		if (fromTask == null)
			return 0;
		AtomicInteger count = new AtomicInteger(0);
		//
		ProjectTaskWrapper.newInstance(fromTask).getLines().forEach(taskLine -> {
			X_C_ProjectLine toLine = new X_C_ProjectLine(projectTask.getCtx (), 0, projectTask.get_TrxName());
			PO.copyValues (taskLine, toLine);
			toLine.setAD_Org_ID(projectTask.getAD_Org_ID());
			toLine.setC_Project_ID(getC_Project_ID(false));
			toLine.setC_ProjectPhase_ID (projectTask.getC_ProjectPhase_ID());
			toLine.setC_ProjectTask_ID(projectTask.getC_ProjectTask_ID());
			toLine.saveEx();
			count.incrementAndGet();
		});
		return count.get();		
	}

	private int C_Project_ID = 0;

	private int getC_Project_ID(boolean reQuery) {
			if (C_Project_ID==0 || reQuery)
				C_Project_ID = projectTask.getC_ProjectPhase().getC_Project_ID();
			return C_Project_ID;
	}
	
	/**
	 * Set Project Task from Phase
	 * @param phase
	 * @param task
	 * @return
	 */
	public X_C_ProjectTask setTaskFromPhase(X_C_ProjectPhase phase, X_C_Task task) {
		projectTask.setAD_Org_ID(phase.getAD_Org_ID());
		projectTask.setC_ProjectPhase_ID(phase.getC_ProjectPhase_ID());
		projectTask.setC_Task_ID (task.getC_Task_ID());			//	FK
		projectTask.setSeqNo (task.getSeqNo());
		projectTask.setName (task.getName());
		projectTask.setDescription(task.getDescription());
		projectTask.setHelp(task.getHelp());
		if (task.getM_Product_ID() > 0)
			projectTask.setM_Product_ID(task.getM_Product_ID());
		if (task.getPP_Product_BOM_ID() > 0 )
			projectTask.setPP_Product_BOM_ID(task.getPP_Product_BOM_ID());
		if (task.getAD_Workflow_ID() > 0)
			projectTask.setAD_Workflow_ID(task.getAD_Workflow_ID());
		if (phase.getC_Campaign_ID() > 0)
			projectTask.setC_Campaign_ID(phase.getC_Campaign_ID());
		if (phase.getC_Activity_ID() > 0)
			projectTask.setC_Activity_ID(phase.getC_Activity_ID());
		if (phase.getC_SalesRegion_ID() > 0)
			projectTask.setC_SalesRegion_ID(phase.getC_SalesRegion_ID());
		if (phase.getAD_OrgTrx_ID() > 0)
			projectTask.setAD_OrgTrx_ID(phase.getAD_OrgTrx_ID());
		if (phase.getUser1_ID() > 0)
			projectTask.setUser1_ID(phase.getUser1_ID());
		if (phase.getUser2_ID() > 0)
			projectTask.setUser2_ID(phase.getUser2_ID());
		if (phase.getUser3_ID() > 0)
			projectTask.setUser3_ID(phase.getUser3_ID());
		if (phase.getUser4_ID() > 0)
			projectTask.setUser4_ID(phase.getUser4_ID());
		
		projectTask.setPriorityRule(task.getPriorityRule());
		projectTask.setIsMilestone(task.isMilestone());
		projectTask.setIsIndefinite(task.isIndefinite());
		projectTask.setIsRecurrent(task.isRecurrent());
		projectTask.setFrequencyType(task.getFrequencyType());
		projectTask.setFrequency(task.getFrequency());
		projectTask.setRunsMax(task.getRunsMax());
		projectTask.setDurationUnit(task.getDurationUnit());
		projectTask.setDurationEstimated(task.getDurationEstimated());
		projectTask.setQty(task.getStandardQty());
		return projectTask;
	}

	/**
	 * Get Order based on this project Task
	 * @return
	 */
	public List<MOrder> getOrders() {
		StringBuilder whereClause = new StringBuilder();
		whereClause.append("EXISTS (SELECT 1 FROM C_OrderLine ol WHERE ol.C_Order_ID = C_Order.C_Order_ID AND ol.C_ProjectTask_ID=?)");
		return new Query(projectTask.getCtx(), MOrder.Table_Name, whereClause.toString(), projectTask.get_TrxName())
				.setClient_ID()
				.setParameters(projectTask.getC_ProjectTask_ID())
				.list();
	}
}
