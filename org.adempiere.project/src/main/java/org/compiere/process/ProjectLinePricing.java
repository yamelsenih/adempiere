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


import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MProductPricing;
import org.compiere.model.MProject;
import org.compiere.model.MProjectLine;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.util.Msg;

/**
 *  Price Project Line.
 *
 *	@author Jorg Janke
 *	@version $Id: ProjectLinePricing.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 *	@author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 *	<a href="https://github.com/adempiere/adempiere/issues/2112">
 *	@see FR [ 2112 ] Add Support to Calculate Price on Project, Phase And Task</a>
 */
public class ProjectLinePricing extends SvrProcess
{
	/**	Project Line from Record			*/
	private int 		m_C_ProjectLine_ID = 0;
	
	/**Project*/
	private int			m_C_Project_ID =0;
	
	/**Project Phase*/
	private int			m_C_ProjectPhase_ID = 0;
	
	/**Project Task*/
	private int			m_C_ProjectTask_ID = 0;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
		
		if (getTable_ID()==MProject.Table_ID)
			m_C_Project_ID = getRecord_ID();
		else if (getTable_ID()==MProjectPhase.Table_ID)
			m_C_ProjectPhase_ID = getRecord_ID();
		else if (getTable_ID()==MProjectTask.Table_ID)
			m_C_ProjectTask_ID = getRecord_ID();
		else if (getTable_ID()==MProjectLine.Table_ID)
			m_C_ProjectLine_ID = getRecord_ID();
		
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		String retValue = "";
		
		if (m_C_Project_ID!=0) {
			MProject project = new MProject(getCtx(), m_C_Project_ID, get_TrxName());
			retValue =processProject(project);
		}
		else if (m_C_ProjectPhase_ID!=0) {
			MProjectPhase phase = new MProjectPhase(getCtx(), m_C_ProjectPhase_ID, get_TrxName());
			retValue = processProjectPhase(phase);
		}
		else if (m_C_ProjectTask_ID!=0) {
			MProjectTask pTask = new MProjectTask(getCtx(), m_C_ProjectTask_ID, get_TrxName());
			retValue = processProjectTask(pTask);
		}
		else if (m_C_ProjectLine_ID!=0) {
			MProjectLine pLine = new MProjectLine(getCtx(), m_C_ProjectLine_ID, get_TrxName());
			retValue = processProjectLines(pLine);
		}
		
		
		return retValue;
	}	//	doIt
	
	
	/**
	 * Process Project
	 * @param project
	 * @return
	 */
	private String processProject(MProject project) {
		String retValue = "";
		if (project.getProjectLineLevel().equals(MProject.PROJECTLINELEVEL_Project)) {
			List<MProjectLine> pLines = project.getLines();
			if (pLines.size()>0) {
				for (MProjectLine mProjectLine : pLines) 
					retValue+=processProjectLines(mProjectLine);
			}
		}else {
			List<MProjectPhase> lPhases = project.getPhases();
			if (lPhases.size()>0) {
				for (MProjectPhase mProjectPhase : lPhases) 
					retValue+=processProjectPhase(mProjectPhase);	
			}
		}
		return retValue;
	}
	
	/**
	 * Process Project Phase
	 * @param mProjectPhase
	 * @return
	 */
	private String processProjectPhase(MProjectPhase mProjectPhase) {
		String retValue = "";
		if (mProjectPhase.getC_ProjectPhase_ID()!=0) {
			MProject project = (MProject) mProjectPhase.getC_Project();
			if (project.getProjectLineLevel().equals(MProject.PROJECTLINELEVEL_Phase)) {
				List<MProjectLine> pLines = mProjectPhase.getLines();
				if (pLines.size()>0) {
					for (MProjectLine mProjectLine : pLines) 
						retValue+=processProjectLines(mProjectLine);
				}
				else {
					if (project.getM_PriceList_ID() == 0)
						throw new IllegalArgumentException("No PriceList");
					//
					boolean isSOTrx = true;
					MProductPricing pp = new MProductPricing (mProjectPhase.getM_Product_ID(), 
						project.getC_BPartner_ID(), mProjectPhase.getPP_Product_BOM_ID(), mProjectPhase.getQty(), isSOTrx, null);
					pp.setM_PriceList_ID(project.getM_PriceList_ID());
					pp.setPriceDate(project.getDateContract());
					//
					mProjectPhase.setPlannedAmt(pp.getPriceStd().multiply(mProjectPhase.getQty()));
					mProjectPhase.saveEx();
					//
					retValue += Msg.getElement(getCtx(), "PriceList") + pp.getPriceList() + " - "
						+ Msg.getElement(getCtx(), "PriceStd") + pp.getPriceStd() + " - "
						+ Msg.getElement(getCtx(), "PriceLimit") + pp.getPriceLimit();
				}
			}else if (project.getProjectLineLevel().equals(MProject.PROJECTLINELEVEL_Task)) {
				List<MProjectTask> pTasks = mProjectPhase.getTasks();
				for (MProjectTask mProjectTask : pTasks) {
					retValue+=processProjectTask(mProjectTask);
				}
				
			}
		}

		return retValue;
	}
	
	/**
	 * Process Project Task
	 * @param pTask
	 * @return
	 */
	private String processProjectTask(MProjectTask pTask) {
		String retValue = "";
		if (pTask.getC_ProjectTask_ID()!=0) {
			MProjectLine[] pLines = pTask.getLines();
			if (pLines.length>0) {
				for (MProjectLine mProjectLine : pLines) 
					retValue+=processProjectLines(mProjectLine);	
			}
			else {
				MProject project = new MProject (getCtx(), pTask.getC_ProjectPhase().getC_Project_ID(), get_TrxName());
				if (project.getM_PriceList_ID() == 0)
					throw new IllegalArgumentException("No PriceList");
				//
				boolean isSOTrx = true;
				MProductPricing pp = new MProductPricing (pTask.getM_Product_ID(), 
					project.getC_BPartner_ID(), pTask.getPP_Product_BOM_ID(), pTask.getQty(), isSOTrx, null);
				pp.setM_PriceList_ID(project.getM_PriceList_ID());
				pp.setPriceDate(project.getDateContract());
				//
				pTask.setPlannedAmt(pp.getPriceStd().multiply(pTask.getQty()));
				pTask.saveEx();
				//
				retValue += Msg.getElement(getCtx(), "PriceList") + pp.getPriceList() + " - "
					+ Msg.getElement(getCtx(), "PriceStd") + pp.getPriceStd() + " - "
					+ Msg.getElement(getCtx(), "PriceLimit") + pp.getPriceLimit();
			}
		}
		return retValue;
	}

	/**
	 * Process Project Lines
	 * @param projectLine
	 * @return
	 */
	private String processProjectLines(MProjectLine projectLine) {
		
		String retValue ="";
		log.info("doIt - " + projectLine);
		if (projectLine.getM_Product_ID() == 0)
			throw new IllegalArgumentException("No Product");
		//
		MProject project = new MProject (getCtx(), projectLine.getC_Project_ID(), get_TrxName());
		if (project.getM_PriceList_ID() == 0)
			throw new IllegalArgumentException("No PriceList");
		//
		boolean isSOTrx = true;
		MProductPricing pp = new MProductPricing (projectLine.getM_Product_ID(), 
			project.getC_BPartner_ID(), projectLine.getPP_Product_BOM_ID(), projectLine.getPlannedQty(), isSOTrx, null);
		pp.setM_PriceList_ID(project.getM_PriceList_ID());
		pp.setPriceDate(project.getDateContract());
		//
		projectLine.setPlannedPrice(pp.getPriceStd());
		projectLine.setPlannedMarginAmt(pp.getPriceStd().subtract(pp.getPriceLimit()));
		projectLine.saveEx();
		//
		retValue += Msg.getElement(getCtx(), "PriceList") + pp.getPriceList() + " - "
			+ Msg.getElement(getCtx(), "PriceStd") + pp.getPriceStd() + " - "
			+ Msg.getElement(getCtx(), "PriceLimit") + pp.getPriceLimit();
		
		return retValue;
	}

}	//	ProjectLinePricing
