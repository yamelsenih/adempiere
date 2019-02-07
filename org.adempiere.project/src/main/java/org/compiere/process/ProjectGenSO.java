/**************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                               *
 * This program is free software; you can redistribute it and/or modify it    		  *
 * under the terms version 2 or later of the GNU General Public License as published  *
 * by the Free Software Foundation. This program is distributed in the hope           *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied         *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                   *
 * See the GNU General Public License for more details.                               *
 * You should have received a copy of the GNU General Public License along            *
 * with this program; if not, printLine to the Free Software Foundation, Inc.,        *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                             *
 * For the text or an alternative of this public license, you may reach us            *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved.  *
 * Contributor: Carlos Parada cparada@erpya.com                                       *
 * See: www.erpya.com                                                                 *
 *************************************************************************************/

package org.compiere.process;


import java.math.BigDecimal;
import java.util.List;

import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProject;
import org.compiere.model.MProjectLine;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.model.PO;
import org.compiere.model.Query;

/**
 *  Generate Sales Order Process.
 *	@author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 *	<a href="https://github.com/adempiere/adempiere/issues/2226">
 *	@see FR [ 2226 ] Add Support Generate Sales Order on Project, Phase And Task</a>
 */
public class ProjectGenSO extends ProjectGenSOAbstract
{
	/**Sales Order*/
	private MOrder 		m_Order 			= null;
	
	/**Document Type*/
	private int			m_C_DocType_ID 		= 0;
	
	/**Lines for Sales Order*/
	private int 		m_Lines 			= 0;
	
	/**Project*/
	private MProject m_MProject 			= null;
	
	/**Project Phase*/
	private MProjectPhase m_MProjectPhase 	= null;

	/**Project Task*/
	private MProjectTask m_MProjectTask 	= null;
	
	/**Project Line*/
	private MProjectLine m_MProjectLine 	= null;
	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{	
		super.prepare();
		if (getTable_ID()==MProject.Table_ID) {
			m_MProject = new MProject(getCtx(), getRecord_ID(), get_TrxName());
		}
		else if (getTable_ID()==MProjectPhase.Table_ID) {
			m_MProjectPhase = new MProjectPhase(getCtx(), getRecord_ID(), get_TrxName());
			m_MProject = new MProject(getCtx(), m_MProjectPhase.getC_Project_ID(), get_TrxName());
		}
		else if (getTable_ID()==MProjectTask.Table_ID) {
			m_MProjectTask = new MProjectTask(getCtx(), getRecord_ID(), get_TrxName());
			m_MProjectPhase = new MProjectPhase(getCtx(), m_MProjectTask.getC_ProjectPhase_ID(), get_TrxName());
			m_MProject = new MProject(getCtx(), m_MProjectPhase.getC_Project_ID(), get_TrxName());
		}
		else if (getTable_ID()==MProjectLine.Table_ID) {
			m_MProjectLine = new MProjectLine(getCtx(), getRecord_ID(), get_TrxName());
			
			if (m_MProjectLine.getC_ProjectTask_ID()!=0)
				m_MProjectTask = new MProjectTask(getCtx(), m_MProjectLine.getC_ProjectTask_ID(), get_TrxName());
			
			if (m_MProjectLine.getC_ProjectPhase_ID()!=0)
				m_MProjectPhase = new MProjectPhase(getCtx(), m_MProjectLine.getC_ProjectPhase_ID(), get_TrxName());
			
			if (m_MProjectLine.getC_Project_ID()!=0)
				m_MProject = new MProject(getCtx(), m_MProjectLine.getC_Project_ID(), get_TrxName());
		}
			
		m_C_DocType_ID = getDocTypeId();
		
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		String retValue = "";
		
		retValue = createOrderHeader();
		if (retValue.equals("")) {
			if (m_MProjectLine!=null) 
				retValue = processProjectLines(m_MProjectLine);
			else if (m_MProjectTask!=null) 
				retValue = processProjectTask(m_MProjectTask);
			else if (m_MProjectPhase!=null) 
				retValue = processProjectPhase(m_MProjectPhase);
			else if (m_MProject!=null) 
				retValue =processProject(m_MProject);
			
			if (m_Lines==0)
				this.rollback();
			else 
				retValue = "@Generated@ @C_Order_ID@ " + m_Order.getDocumentNo();
			
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
			List<MProjectPhase> lPhases = getPhases();
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
			
			if (mProjectPhase.getM_Product_ID() !=0) {
				MProduct product = (MProduct) mProjectPhase.getM_Product();
				retValue = createOrderLine(product, mProjectPhase.getQty(), mProjectPhase);
			}
			List<MProjectTask> pTasks = getTasks(mProjectPhase);
			for (MProjectTask mProjectTask : pTasks) {
				retValue+=processProjectTask(mProjectTask);
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
			if (pTask.getM_Product_ID()!=0) {
				MProduct product = (MProduct) pTask.getM_Product();
				retValue = createOrderLine(product, pTask.getQty(), pTask);
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
		
		log.info("doIt - " + projectLine);
		if (projectLine.getM_Product_ID() == 0)
			return "@NotFound@ @M_Product_ID@";
		
		MProduct product = (MProduct) projectLine.getM_Product();
		return createOrderLine(product, projectLine.getPlannedQty(), projectLine);
		
	}
	
	/**
	 * Create Order Header
	 * @return
	 */
	private String createOrderHeader() {
		if (m_C_DocType_ID == 0)
			return "@Invalid@ @C_DocType_ID@";
		MDocType docType = new MDocType(getCtx(), m_C_DocType_ID, get_TrxName());
		
		m_Order = new MOrder(m_MProject, true, docType.getDocSubTypeSO());
		
		if (!m_Order.save())
			return "@SaveError@ @C_Order_ID@";
		
		return "";
	}
	
	/**
	 * Create Order Line
	 * @param product
	 * @param Qty
	 * @param entity
	 * @return
	 */
	private String createOrderLine(MProduct product, BigDecimal Qty, PO entity) {
		if (m_Order == null
				|| m_Order.getC_Order_ID()==0)
			return "@Invalid@ @C_Order_ID@";
		
		MOrderLine line = new MOrderLine(m_Order);
		line.setM_Product_ID(product.getM_Product_ID());
		line.setC_Project_ID(m_MProject.getC_Project_ID());
		if (entity instanceof MProjectPhase) 
			line.setC_ProjectPhase_ID(entity.get_ID());
		else if (entity instanceof MProjectTask) {
			line.setC_ProjectPhase_ID(entity.get_ValueAsInt("C_ProjectPhase_ID"));
			line.setC_ProjectTask_ID(entity.get_ID());
		}
		
		line.setQty(Qty);
		
		if (!line.save())
			return "@SaveError@ @C_OrderLine_ID@";
		
		m_Lines = m_Lines +1;
		return "";
	}
	
	/**
	 * Get Project Phases
	 * @return
	 */
	public List<MProjectPhase> getPhases()
	{
		final String whereClause = "C_Project_ID=? "
								+ "AND NOT EXISTS (SELECT 1 "
												+ "FROM C_Order o "
												+ "INNER JOIN C_OrderLine ol ON (o.C_Order_ID = ol.C_Order_ID) "
												+ "WHERE ol.C_ProjectPhase_ID = C_ProjectPhase.C_ProjectPhase_ID "
												+ "AND ol.C_ProjectTask_ID IS NULL "
												+ "AND o.DocStatus IN ('" + MOrder.DOCSTATUS_Completed + "','" + MOrder.DOCSTATUS_Closed + 
																	"','" + MOrder.DOCSTATUS_Drafted   + "','" + MOrder.DOCSTATUS_InProgress +"') "
												+ ")";
		
		return new Query(getCtx(), MProjectPhase.Table_Name, whereClause, get_TrxName())
			.setParameters(m_MProject.getC_Project_ID())
			.setOrderBy(MProjectPhase.COLUMNNAME_SeqNo)
			.list();
	}	

	/**
	 * Get Project Task
	 * @param phase
	 * @return
	 */
	public List<MProjectTask> getTasks(MProjectPhase phase)
	{
		return  new Query(getCtx(), MProjectTask.Table_Name , MProjectPhase.COLUMNNAME_C_ProjectPhase_ID + "=?"
														+ "AND NOT EXISTS (SELECT 1 "
														+ "FROM C_Order o "
														+ "INNER JOIN C_OrderLine ol ON (o.C_Order_ID = ol.C_Order_ID) "
														+ "WHERE ol.C_ProjectTask_ID = C_ProjectTask.C_ProjectTask_ID "
														+ "AND o.DocStatus IN ('" + MOrder.DOCSTATUS_Completed + "','" + MOrder.DOCSTATUS_Closed + 
																			"','" + MOrder.DOCSTATUS_Drafted   + "','" + MOrder.DOCSTATUS_InProgress +"') "
														+ ")"
				, get_TrxName())
				.setClient_ID()
				.setParameters(phase.getC_ProjectPhase_ID())
				.setOrderBy(MProjectTask.COLUMNNAME_SeqNo)
				.list();
	}	//	getTasks
}	//	ProjectGenSO
