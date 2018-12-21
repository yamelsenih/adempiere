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

import org.compiere.model.MProduct;
import org.compiere.model.MProject;
import org.compiere.model.MProjectLine;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.model.MRfQ;
import org.compiere.model.MRfQLine;
import org.compiere.model.MRfQLineQty;
import org.compiere.model.PO;

/**
 *  Price Generate RFQ.
 *	@author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 *	<a href="https://github.com/adempiere/adempiere/issues/2188">
 *	@see FR [ 2188 ] Add Support to Generate RfQ from Project, Phase And Task</a>
 */
public class ProjectGenerateRFQ extends ProjectGenerateRFQAbstract
{
	/**Request For Quotation*/
	private MRfQ 		m_RFQ 				= null;
	
	/**RFQ Topic*/
	private int			m_C_RfQ_Topic_ID 	=0;
	
	/**RFQ Type*/
	private String		m_QuoteType			= "";
	/**Lines for RFQ*/
	private int 		m_Lines 			= 0;
	
	/**Project*/
	private MProject m_MProject 			= null;
	
	/**Project Phase*/
	private MProjectPhase m_MProjectPhase 	= null;

	/**Project Task*/
	private MProjectTask m_MProjectTask 	= null;
	
	/**Project Line*/
	private MProjectLine m_MProjectLine 	= null;
	
	/**Quote All Qty*/
	private boolean m_IsQuoteAllQty			= false;
	
	/**Invited Vendors Only*/
	private boolean m_IsInvitedVendorsOnly 	= false;
	
	/**RFQ Response Accepted*/
	private boolean m_IsRfQResponseAccepted	= false;
	
	/**Is Self Service*/
	private boolean m_IsSelfService			= false;
	
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
			
		m_C_RfQ_Topic_ID = getRfQTopicId();
		m_QuoteType = getQuoteType();
		m_IsQuoteAllQty = isQuoteAllQty();
		m_IsInvitedVendorsOnly =isInvitedVendorsOnly();
		m_IsRfQResponseAccepted = isRfQResponseAccepted();
		m_IsSelfService	= isSelfService();
		
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		String retValue = "";
		
		retValue = createRFQHeader();
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
				retValue = "@Generated@ @C_RfQ_ID@ " + m_RFQ.getDocumentNo();
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
				if (pLines.size()>0 && mProjectPhase.getM_Product_ID() ==0) {
					for (MProjectLine mProjectLine : pLines) 
						retValue+=processProjectLines(mProjectLine);
				}
				else {
					MProduct product = (MProduct) mProjectPhase.getM_Product();
					retValue = createRFQLine(product, mProjectPhase.getQty(), mProjectPhase);
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
			if (pLines.length>0 && pTask.getM_Product_ID()==0) {
				for (MProjectLine mProjectLine : pLines) 
					retValue+=processProjectLines(mProjectLine);	
			}
			else {
				MProduct product = (MProduct) pTask.getM_Product();
				retValue = createRFQLine(product, pTask.getQty(), pTask);
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
		return createRFQLine(product, projectLine.getPlannedQty(), projectLine);
		
	}
	
	/**
	 * Create Header for RfQ
	 * @return
	 */
	private String createRFQHeader() {
		if (m_C_RfQ_Topic_ID == 0)
			return "@Invalid@ @C_RfQ_Topic_ID@";
		
		if (m_MProject.getDateStart()==null)
			return "@Invalid@ @DateStart@";
		
		m_RFQ = new MRfQ(getCtx(), 0, get_TrxName());
		m_RFQ.setName(m_MProject.getName());
		m_RFQ.setSalesRep_ID(m_MProject.getSalesRep_ID());
		m_RFQ.setC_Currency_ID(m_MProject.getC_Currency_ID());
		m_RFQ.setC_RfQ_Topic_ID(m_C_RfQ_Topic_ID);
		m_RFQ.setQuoteType(m_QuoteType);
		m_RFQ.setDateResponse(m_MProject.getDateStart());
		m_RFQ.setDateWorkStart(m_MProject.getDateStart());
		m_RFQ.setIsQuoteAllQty(m_IsQuoteAllQty);
		m_RFQ.setIsInvitedVendorsOnly(m_IsInvitedVendorsOnly);
		m_RFQ.setIsRfQResponseAccepted(m_IsRfQResponseAccepted);
		m_RFQ.setIsInvitedVendorsOnly(m_IsInvitedVendorsOnly);
		m_RFQ.setIsRfQResponseAccepted(m_IsRfQResponseAccepted);
		m_RFQ.setIsSelfService(m_IsSelfService);
		m_RFQ.setC_BPartner_ID(m_MProject.getC_BPartner_ID());
		m_RFQ.setC_BPartner_Location_ID(m_MProject.getC_BPartner_Location_ID());
		m_RFQ.setAD_User_ID(m_MProject.getAD_User_ID());
		
		m_RFQ.setAD_Org_ID(m_MProject.getAD_Org_ID());
		m_RFQ.set_ValueOfColumn("C_Project_ID", m_MProject.getC_Project_ID());
		m_RFQ.set_ValueOfColumn("C_Campaign_ID", m_MProject.getC_Campaign_ID());
		m_RFQ.set_ValueOfColumn("User1_ID", m_MProject.getUser1_ID());
		
		if (!m_RFQ.save())
			return "@SaveError@ @C_RfQ_ID@";
		
		return "";
	}
	
	/**
	 * Create Line and Quantity from RfQ
	 * @param product
	 * @param Qty
	 * @param entity
	 * @return
	 */
	private String createRFQLine(MProduct product, BigDecimal Qty, PO entity) {
		if (m_RFQ == null
				|| m_RFQ.getC_RfQ_ID()==0)
			return "@Invalid@ @C_RfQ_ID@";
		
		MRfQLine line = new MRfQLine(m_RFQ);
		line.setM_Product_ID(product.getM_Product_ID());
		
		if (!line.save())
			return "@SaveError@ @C_RfQLine_ID@";
		
		entity.set_ValueOfColumn("C_RfQLine_ID", line.getC_RfQLine_ID());
		entity.save();
		
		MRfQLineQty lineQty = new MRfQLineQty(line);
		lineQty.setC_UOM_ID(product.getC_UOM_ID());
		lineQty.setQty(Qty);
		lineQty.setIsRfQQty(true);
		
		if (!lineQty.save())
			return "@SaveError@ @C_RfQLineQty_ID@";

		m_Lines = m_Lines +1;
		return "";
	}

}	//	ProjectLinePricing
