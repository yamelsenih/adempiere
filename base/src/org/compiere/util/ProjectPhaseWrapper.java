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
import java.util.logging.Level;

import org.compiere.model.I_C_ProjectLine;
import org.compiere.model.I_C_ProjectTask;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_Phase;
import org.compiere.model.X_C_ProjectLine;
import org.compiere.model.X_C_ProjectPhase;
import org.compiere.model.X_C_ProjectTask;
import org.compiere.model.X_C_Task;

/**
 * Project Wrapper class used because the project have wrong links with base source folder
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class ProjectPhaseWrapper {
	
	/**
	 * Static instance
	 * @param projectPhase
	 * @return
	 */
	public static ProjectPhaseWrapper newInstance(X_C_ProjectPhase projectPhase) {
		return new ProjectPhaseWrapper(projectPhase);
	}
	
	/***
	 * Private constructor
	 * @param projectPhase
	 */
	private ProjectPhaseWrapper(X_C_ProjectPhase projectPhase) {
		this.projectPhase = projectPhase;
	}
	
	private X_C_ProjectPhase projectPhase;
	/**	Logger							*/
	protected CLogger log = CLogger.getCLogger (getClass());
	/**
	 * @return the project
	 */
	public final X_C_ProjectPhase getProjectPhase() {
		return projectPhase;
	}
	
	/**
	 * 	Get Project Phase Tasks.
	 *	@return Array of tasks
	 */
	public List<X_C_ProjectTask> getTasks()
	{
		return  new Query(projectPhase.getCtx(), I_C_ProjectTask.Table_Name , I_C_ProjectTask.COLUMNNAME_C_ProjectPhase_ID + "=?", projectPhase.get_TrxName())
				.setClient_ID()
				.setParameters(projectPhase.getC_ProjectPhase_ID())
				.setOrderBy(I_C_ProjectTask.COLUMNNAME_SeqNo)
				.list();
	}	//	getTasks

	/**
	 * 	Copy Lines from other Phase
	 * 	BF 3067850 - monhate
	 *	@param fromPhase from phase
	 *	@return number of tasks copied
	 */
	public int copyLinesFrom (X_C_ProjectPhase fromPhase) {
		if (fromPhase == null)
			return 0;
		AtomicInteger count = new AtomicInteger(0);
		List<X_C_ProjectLine> fromProjectLines = ProjectPhaseWrapper.newInstance(fromPhase).getLines();
		fromProjectLines.stream()
				.filter(fromProjectLine -> fromProjectLine.getC_ProjectTask_ID() <= 0)
				.forEach(fromProjectLine -> {
					X_C_ProjectLine toProjectline = new X_C_ProjectLine(projectPhase.getCtx(), 0, projectPhase.get_TrxName());
					PO.copyValues(fromProjectLine, toProjectline);
					toProjectline.setAD_Org_ID(projectPhase.getAD_Org_ID());
					toProjectline.setC_Project_ID(projectPhase.getC_Project_ID());
					toProjectline.setC_ProjectPhase_ID(projectPhase.getC_ProjectPhase_ID());
					toProjectline.saveEx();
					count.getAndUpdate(no -> no + 1);
				});

		if (fromProjectLines.size() != count.get())
			log.warning("Count difference - ProjectLine=" + fromProjectLines.size() + " <> Saved=" + count);

		return count.get();
	}

	/**
	 * 	Copy Tasks from other Phase
	 *  BF 3067850 - monhate
	 *	@param fromProjectPhase from phase
	 *	@return number of tasks copied
	 */
	public int copyTasksFrom (X_C_ProjectPhase fromProjectPhase)
	{
		if (fromProjectPhase == null)
			return 0;
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger countLine = new AtomicInteger(0);
		List<X_C_ProjectTask> toProjectTasks = getTasks();
		List<X_C_ProjectTask> fromProjectTasks = ProjectPhaseWrapper.newInstance(fromProjectPhase).getTasks();
		fromProjectTasks.stream().forEach(fromProjectTask -> {
			Boolean exists = toProjectTasks.stream().anyMatch(taskTo -> taskTo.getC_Task_ID() == fromProjectTask.getC_Task_ID());
			if (exists) {
				log.info("Task already exists here, ignored - " + fromProjectTask);
			} else {
				X_C_ProjectTask toProjectTask = new X_C_ProjectTask(projectPhase.getCtx(), 0, projectPhase.get_TrxName());
				PO.copyValues(fromProjectTask, toProjectTask);
				toProjectTask.setAD_Org_ID(projectPhase.getAD_Org_ID());
				toProjectTask.setC_ProjectPhase_ID(projectPhase.getC_ProjectPhase_ID());
				toProjectTask.setC_Task_ID(fromProjectTask.getC_Task_ID());
				toProjectTask.setProjInvoiceRule(projectPhase.getProjInvoiceRule());
				toProjectTask.saveEx();
				count.getAndUpdate(no -> no + 1);
				countLine.getAndUpdate(no -> no + ProjectTaskWrapper.newInstance(toProjectTask).copyLinesFrom(fromProjectTask));
			}
		});

		if (fromProjectTasks.size() != count.get())
			log.warning("Count difference - ProjectPhase=" + fromProjectTasks.size() + " <> Saved=" + count.get());

		return count.get() + countLine.get();
	}	//	copyTasksFrom

	/**
	 * 	Copy Tasks from other Phase
	 *	@param fromProjectPhase from phase
	 *	@return number of tasks copied
	 */
	public int copyTasksFrom (X_C_Phase fromProjectPhase)
	{
		if (fromProjectPhase == null)
			return 0;
		AtomicInteger count = new AtomicInteger(0);
		//	Copy Type Tasks
		List<X_C_Task> fromProjectTasks = ProjectPhaseTypeWrapper.newInstance(fromProjectPhase).getTasks();
		fromProjectTasks.stream()
				.forEach(fromProjectTask -> {
					X_C_ProjectTask toProjectTask = new X_C_ProjectTask(projectPhase.getCtx(), 0, projectPhase.get_TrxName());
					ProjectTaskWrapper.newInstance(toProjectTask).setTaskFromPhase(projectPhase, fromProjectTask);
					toProjectTask.setC_ProjectPhase_ID(projectPhase.getC_ProjectPhase_ID());
					toProjectTask.setProjInvoiceRule(projectPhase.getProjInvoiceRule());
					toProjectTask.saveEx();
					count.getAndUpdate(no -> no + 1);
				});
		log.fine("#" + count.get() + " - " + fromProjectPhase);
		if (fromProjectTasks.size() != count.get())
			log.log(Level.SEVERE, "Count difference - TypePhase=" + fromProjectTasks.size() + " <> Saved=" + count.get());

		return count.get();
	}	//	copyTasksFrom
	
	/**************************************************************************
	 * 	Get Project Lines
	 * 	BF 3067850 - monhate
	 *	@return Array of lines
	 */	public List<X_C_ProjectLine> getLines() {
		final String whereClause = "C_Project_ID=? and C_ProjectPhase_ID=?";
		return new Query(projectPhase.getCtx(), I_C_ProjectLine.Table_Name, whereClause, projectPhase.get_TrxName())
			.setClient_ID()
			.setParameters(projectPhase.getC_Project_ID(), projectPhase.getC_ProjectPhase_ID())
			.setOrderBy("Line")
			.list();
	}
}
