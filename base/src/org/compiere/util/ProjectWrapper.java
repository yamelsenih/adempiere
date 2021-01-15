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

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.compiere.model.I_C_ProjectIssue;
import org.compiere.model.I_C_ProjectLine;
import org.compiere.model.I_C_ProjectPhase;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_Project;
import org.compiere.model.X_C_ProjectIssue;
import org.compiere.model.X_C_ProjectLine;
import org.compiere.model.X_C_ProjectPhase;

/**
 * Project Wrapper class used because the project have wrong links with base source folder
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class ProjectWrapper {
	
	/**
	 * Static instance
	 * @param project
	 * @return
	 */
	public static ProjectWrapper newInstance(X_C_Project project) {
		return new ProjectWrapper(project);
	}
	
	/***
	 * Private constructor
	 * @param project
	 */
	private ProjectWrapper(X_C_Project project) {
		this.project = project;
	}
	
	private X_C_Project project;
	/**	Logger							*/
	protected CLogger log = CLogger.getCLogger (getClass());
	/**
	 * @return the project
	 */
	public final X_C_Project getProject() {
		return project;
	}

	/**
	 *	Get the next Line No
	 */
	public int getMaxLine() {
		return DB.getSQLValue(project.get_TrxName(), 
			"SELECT COALESCE(MAX(Line),0)+10 FROM C_ProjectLine WHERE C_Project_ID=?", project.getC_Project_ID());
	}
	
	/**
	 * Create Line from Project
	 * @param project
	 * @return
	 */
	public X_C_ProjectLine createLineFromProject() {
		X_C_ProjectLine projectLine = new X_C_ProjectLine(project.getCtx(), 0, project.get_TrxName());
		setLineFromProject(projectLine);
		return projectLine;
	}
	
	/**
	 * Set Project Line values from project
	 * @param projectLine
	 */
	public void setLineFromProject(X_C_ProjectLine projectLine) {
		projectLine.setAD_Org_ID(project.getAD_Org_ID());
		projectLine.setC_Project_ID (project.getC_Project_ID());
		projectLine.setLine(getMaxLine());
	}
	
	/**
	 * Get Price List Id from project
	 * @return
	 */
	public int getPriceListId() {
		AtomicInteger priceListId = new AtomicInteger(-1);
		Optional.ofNullable(project.getM_PriceList_Version()).ifPresent(priceListVersion -> priceListId.set(priceListVersion.getM_PriceList_ID()));
		return priceListId.get();
	}
	
	/**
	 * 	Create new Project by copying
	 * 	@param context context
	 *	@param projectId project
	 * 	@param documentDate date of the document date
	 *	@param trxName transaction
	 *	@return Project
	 */
	public static X_C_Project copyFrom (Properties context, int projectId, Timestamp documentDate, String trxName) {
		X_C_Project sourceProject = new X_C_Project(context, projectId, trxName);
		if (sourceProject.getC_Project_ID() == 0)
			throw new IllegalArgumentException ("@C_Project_ID@ @NotFound@ =" + projectId);
		//
		X_C_Project targetProject = new X_C_Project (context, 0, trxName);
		PO.copyValues(sourceProject, targetProject);
		targetProject.setAD_Org_ID(sourceProject.getAD_Org_ID());
		//	Set Value with Time
		String Value = targetProject.getValue() + " ";
		String Time = documentDate.toString();
		int length = Value.length() + Time.length();
		if (length <= 40)
			Value += Time;
		else
			Value += Time.substring (length-40);
		targetProject.setValue(Value);
		targetProject.setInvoicedAmt(Env.ZERO);
		targetProject.setProjectBalanceAmt(Env.ZERO);
		targetProject.setProcessed(false);
		//
		targetProject.saveEx();
		if (ProjectWrapper.newInstance(targetProject).copyDetailsFrom(sourceProject) == 0)
			throw new IllegalStateException("Could not create Project Details");

		return targetProject;
	}	//	copyFrom
	
	/**************************************************************************
	 * 	Copy Lines/Phase/Task from other Project
	 *	@param project project
	 *	@return number of total lines copied
	 */
	public int copyDetailsFrom (X_C_Project sourceProject) {
		if (project.isProcessed() || sourceProject == null)
			return 0;
		int count = copyLinesFrom(sourceProject)
			+ copyPhasesFrom(sourceProject);
		return count;
	}	//	copyDetailsFrom
	
	/**
	 * 	Copy Lines From other Project
	 *	@param project project
	 *	@return number of lines copied
	 */
	public int copyLinesFrom (X_C_Project project) {
		if (project.isProcessed() || project == null)
			return 0;
		AtomicInteger count = new AtomicInteger(0);
		List<X_C_ProjectLine> fromProjectLines = ProjectWrapper.newInstance(project).getLines();
		fromProjectLines.stream()
				.filter(fromProjectLine ->
						fromProjectLine.getC_ProjectPhase_ID() <= 0
					 || fromProjectLine.getC_ProjectTask_ID() <= 0)
				.forEach(fromProjectLine -> {
					X_C_ProjectLine toProjectLine = new X_C_ProjectLine(project.getCtx(), 0, project.get_TrxName());
					PO.copyValues(fromProjectLine, toProjectLine);
					toProjectLine.setAD_Org_ID(project.getAD_Org_ID());
					toProjectLine.setC_Project_ID(project.getC_Project_ID());
					toProjectLine.setInvoicedAmt(Env.ZERO);
					toProjectLine.setInvoicedQty(Env.ZERO);
					toProjectLine.setC_OrderPO_ID(0);
					toProjectLine.setC_Order_ID(0);
					toProjectLine.setProcessed(false);
					toProjectLine.saveEx();
					count.getAndUpdate(no -> no + 1);
				});

		if (fromProjectLines.size() != count.get())
			log.log(Level.SEVERE, "Lines difference - Project=" + fromProjectLines.size() + " <> Saved=" + count);
		return count.get();
	}	//	copyLinesFrom

	/**
	 * 	Copy Phases/Tasks from other Project
	 *	@param fromProject project
	 *	@return number of items copied
	 */
	public int copyPhasesFrom (X_C_Project fromProject)
	{
		if (project.isProcessed() || fromProject == null)
			return 0;
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger taskCount = new AtomicInteger(0);
		AtomicInteger lineCount = new AtomicInteger(0);
		//	Get Phases
		List<X_C_ProjectPhase> toPhases = getPhases();
		List<X_C_ProjectPhase> fromPhases = ProjectWrapper.newInstance(fromProject).getPhases();
		fromPhases.stream()
				.forEach(fromPhase -> {
					//	Check if Phase already exists
					Boolean exists = toPhases.stream().anyMatch(toPhase -> toPhase.getC_Phase_ID() == fromPhase.getC_Phase_ID());
					//	Phase exist
					if (exists)
						log.info("Phase already exists here, ignored - " + fromPhase);
					else {
						X_C_ProjectPhase toPhase = new X_C_ProjectPhase(project.getCtx(), 0, project.get_TrxName());
						PO.copyValues(fromPhase, toPhase);
						toPhase.setAD_Org_ID(project.getAD_Org_ID());
						toPhase.setC_Project_ID(project.getC_Project_ID());
						toPhase.setC_Order_ID(0);
						toPhase.setIsComplete(false);
						toPhase.saveEx();
						count.getAndUpdate(no -> no + 1);
						taskCount.getAndUpdate(taskNo -> taskNo + ProjectPhaseWrapper.newInstance(toPhase).copyTasksFrom(fromPhase));
						lineCount.getAndUpdate(lineNo -> lineNo + ProjectPhaseWrapper.newInstance(toPhase).copyLinesFrom(fromPhase));
					}
				});
		if (fromPhases.size() != count.get())
			log.warning("Count difference - Project=" + fromPhases.size() + " <> Saved=" + count.get());

		return count.get() + taskCount.get() + lineCount.get();
	}	//	copyPhasesFrom
	
	/**************************************************************************
	 * 	Get Project Lines
	 *	@return Array of lines
	 */
	public List<X_C_ProjectLine> getLines() {
		//FR: [ 2214883 ] Remove SQL code and Replace for Query - red1
		final String whereClause = "C_Project_ID=?";
		return new Query(project.getCtx(), I_C_ProjectLine.Table_Name, whereClause, project.get_TrxName())
			.setParameters(project.getC_Project_ID())
			.setOrderBy("Line")
			.list();
	}	//	getLines

	/**
	 * 	Get Project Issues
	 *	@return Array of issues
	 */
	public List<X_C_ProjectIssue> getIssues() {
		//FR: [ 2214883 ] Remove SQL code and Replace for Query - red1
		String whereClause = "C_Project_ID=?";
		return new Query(project.getCtx(), I_C_ProjectIssue.Table_Name, whereClause, project.get_TrxName())
			.setParameters(project.getC_Project_ID())
			.setOrderBy("Line")
			.list();
	}	//	getIssues

	/**
	 * 	Get Project Phases
	 *	@return Array of phases
	 */
	public List<X_C_ProjectPhase> getPhases() {
		//FR: [ 2214883 ] Remove SQL code and Replace for Query - red1
		String whereClause = "C_Project_ID=?";
		return new Query(project.getCtx(), I_C_ProjectPhase.Table_Name, whereClause, project.get_TrxName())
			.setParameters(project.getC_Project_ID())
			.setOrderBy("SeqNo")
			.list();
	}	//	getPhases
}
