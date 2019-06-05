/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * or via info@adempiere.net or http://www.adempiere.net/license.html         *
 *****************************************************************************/

package org.compiere.process;


import java.util.List;

import org.compiere.model.MProduct;
import org.compiere.model.MProject;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.eevolution.model.MPPProductBOM;
import org.eevolution.service.dsl.ProcessBuilder;

/**
 *	Copy the BOM and Components from the Product inside the Project Task.
 *  The new BOM/Components will have the same contents as the original;
 *  additionally it will link to the calling Project Task.
 *	
 *  @author Mario Calderon, mario.calderon@westfalia-it.com, http://www.westfalia-it.com
 *  @version $Id: CopyProjectTypeTasksIntoProject.java,v 1.0 2018/05/29 04:58:38 marcalwestf Exp $
 *  @author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 * 			<li>BR [ 1723 ] Change for call CopyFromBOM class to copy Product BOM
 * 			@see https://github.com/adempiere/adempiere/issues/1723
 */
public class ProjectTaskPhaseCopyBOM extends ProjectTaskPhaseCopyBOMAbstract
{
	int m_C_Project_ID		= 0;
	int m_C_ProjectPhase_ID = 0;
	int m_C_ProjectTask_ID	= 0;
	private static int PROCESSID_PRODUCT_BOM_COPY = 53004;
	
	@Override
	protected void prepare()
	{
		super.prepare();
	}

	@Override
	protected String doIt() throws Exception
	{

		MProject project = null;
		MProjectPhase phase = null;
		MProjectTask task = null;
		
		
		String resultBOM = "";
		
		//Explode BOM from Product Project Phase
		
		if (getTable_ID() == MProjectTask.Table_ID) {
			task = new MProjectTask(getCtx(), getRecord_ID(), get_TrxName());
			m_C_ProjectTask_ID = task.getC_ProjectTask_ID();
			phase = (MProjectPhase) task.getC_ProjectPhase();
			m_C_ProjectPhase_ID = phase.getC_ProjectPhase_ID();
			resultBOM =copyBOM(null ,task, (MProduct)task.getM_Product());
			addLog(resultBOM);
		}
		else if (getTable_ID() == MProjectPhase.Table_ID) {
			phase = new MProjectPhase(getCtx(), getRecord_ID(), get_TrxName());
			m_C_ProjectPhase_ID = phase.getC_ProjectPhase_ID();
			resultBOM = processPhase(phase);
		}
		else if (getTable_ID() == MProject.Table_ID) {
			m_C_Project_ID = getRecord_ID();
			project = new MProject(getCtx(), m_C_Project_ID, get_TrxName());
			resultBOM = processProject(project);
		}
		
		return resultBOM;
	}	//	doIt
	
	private String processProject(MProject project) {
		String result ="";
		
		List<MProjectPhase> phases = project.getPhases();
		for (MProjectPhase mProjectPhase : phases) 
			result+=processPhase(mProjectPhase);
		
		return result;
	}
	
	private String processPhase(MProjectPhase phase) {
		String result ="";
		String resultBOM ="";
		resultBOM +=copyBOM(phase, null, (MProduct)phase.getM_Product()) ;
		addLog(resultBOM);
		result +=resultBOM + "\n";
		
		List<MProjectTask> ptasks = phase.getTasks();
		for (MProjectTask mProjectTask : ptasks) { 
			resultBOM = copyBOM(null ,mProjectTask, (MProduct)mProjectTask.getM_Product());
			addLog(resultBOM);
			result +=resultBOM + "\n";
		}
		
		return result;
	} 
	
	private String copyBOM(MProjectPhase phase,MProjectTask task, MProduct product) {
		String result ="";
		MPPProductBOM defaultBOM = null;
		MPPProductBOM newBOM = null;
		String level ="";
		
		if (phase!=null) 
			level = " @C_ProjectPhase_ID@ " + phase.getName();
		else if (task!=null)
			level = " @C_ProjectTask_ID@ " + task.getName();
		
		
		
		if (product==null )
			return "@M_Product_ID@ @NotFound@ " + level ;
		
		if (!(product.isBOM() && product.isVerified()))
			return "@invalid@ @PP_Product_BOM_ID@ " + level;

		
		if (m_C_ProjectTask_ID!=0) 
			if (task!=null)
				defaultBOM = (MPPProductBOM)task.getPP_Product_BOM();
		else if (m_C_ProjectPhase_ID!=0) 
			if (phase!=null)
				defaultBOM = (MPPProductBOM)phase.getPP_Product_BOM();
		
		if (defaultBOM==null
				|| defaultBOM.getPP_Product_BOM_ID()==0)
			defaultBOM = MPPProductBOM.getDefault(product, get_TrxName());
		
		if (defaultBOM==null)
			return "@PP_Product_BOM_ID@ @NotFound@ " + level;

		newBOM = new MPPProductBOM(getCtx(), 0, get_TrxName());
		MPPProductBOM.copyValues(defaultBOM, newBOM, true);
		newBOM.setIsDefault(false);
		newBOM.setValue("");
		newBOM.saveEx();
		if (m_C_ProjectTask_ID!=0
				&& task!=null) {
			task.setPP_Product_BOM_ID(newBOM.getPP_Product_BOM_ID());
			task.save();
		}else if (m_C_ProjectPhase_ID!=0
				&& phase!=null) {
			phase.setPP_Product_BOM_ID(newBOM.getPP_Product_BOM_ID());
			phase.save();
		}
		
		ProcessInfo processInfo = ProcessBuilder.create(getCtx())
				.process(PROCESSID_PRODUCT_BOM_COPY)
				.withRecordId(MPPProductBOM.Table_ID, newBOM.getPP_Product_BOM_ID())
				.withoutTransactionClose()
				.withParameter(MPPProductBOM.COLUMNNAME_PP_Product_BOM_ID, defaultBOM.getPP_Product_BOM_ID())
				.execute(get_TrxName());
		
		result = processInfo.getSummary() + level;
		
		return result;
	}
}