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

import org.compiere.model.MProject;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.model.MProjectTypePhase;
import org.compiere.model.MProjectTypeTask;
import org.compiere.process.ProcessInfoParameter;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;


/**
 *	Project Type Tasks are copied into the project (which is a process parameter).
 *  If needed, Project Phases are created.
 *  If the Project Task for the Project Phase does already exist, nothing is done.
 *	
 *  @author Mario Calderon, mario.calderon@westfalia-it.com, http://www.westfalia-it.com
 *  @version $Id: CopyProjectTypeTasksIntoProject.java,v 1.0 2018/05/27 04:58:38 marcalwestf Exp $
 *  @author Carlos Parada, cparada@erpya.com, http://www.erpya.com
 *  		FR[ 1961 ] Add Support to set project phase / task invoice rule on Quantity of Product 
 *  		@see https://github.com/adempiere/adempiere/issues/1961
 *  
 */
public class CopyProjectTypeTasksIntoProject extends CopyProjectTypeTasksIntoProjectAbstract
{
	/**	Project where Phases and Tasks will be copied to */
	private int		 p_C_Project_ID = 0;
	private MProject m_project;
	
	@Override
	protected void prepare()
	{
		super.prepare();
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("C_Project_ID")){
				p_C_Project_ID = ((BigDecimal)para[i].getParameter()).intValue();
				m_project = MProject.getById(getCtx(), p_C_Project_ID,get_TrxName());
			}
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	@Override
	protected String doIt() throws Exception
	{
		int count = 0;
		List<MProjectTypeTask> projectTypeTasks = (List<MProjectTypeTask>) getInstancesForSelection(get_TrxName());
		populateProjectTasks(m_project, projectTypeTasks);
		correctProjectSequences(m_project);
		
		return "@Created@/@Updated@ #" + count;
	}	//	doIt

	/**
	 * Populate Project Tasks into Project
	 * Create Project Phases if needed
	 * @param projectTasks
	 * @return count
	 */
	private int populateProjectTasks(MProject project, List<MProjectTypeTask> projectTypeTasks)
	{
		AtomicInteger count = new AtomicInteger(0);
		projectTypeTasks.stream()
				.forEach(projectTypeTask -> {
					MProjectTypePhase projectTypePhase = (MProjectTypePhase) projectTypeTask.getC_Phase();
					
					// Check if Project PHASE already exists
					MProjectPhase projectPhase = getProjectPhase(project, projectTypePhase);
					if (projectPhase==null){
						projectPhase = new MProjectPhase (project, projectTypePhase);
						projectPhase.setProjInvoiceRule(project.getProjInvoiceRule());
						//FR[ 1961 ]
						if (projectPhase.getM_Product_ID()!=0)
							projectPhase.setProjInvoiceRule(MProjectPhase.PROJINVOICERULE_ProductQuantity);
						projectPhase.saveEx();
					}
					
					// Check if Project TASK already exists
					MProjectTask projectTask = getProjectTask(projectPhase, projectTypeTask);
					if (projectTask==null){
						projectTask = new MProjectTask (projectPhase, projectTypeTask);
						projectTask.setProjInvoiceRule(project.getProjInvoiceRule());
						//FR[ 1961 ]
						if (projectTask.getM_Product_ID()!=0)
							projectTask.setProjInvoiceRule(MProjectTask.PROJINVOICERULE_ProductQuantity);
						projectTask.saveEx();
						count.updateAndGet(no -> no + 1);
					}
		});
		return count.get();
	} // populateProjectTasks


	/**
	 * Get the Project Phase for a Project/Project Type Phase combination
	 * @param project
	 * @param projectTypePhase
	 * @return MProjectPhase or null
	 */
	private MProjectPhase getProjectPhase(MProject project, MProjectTypePhase projectTypePhase)
	{
		MProjectPhase resultProjectPhase = null;
		List<MProjectPhase> projectPhases = project.getPhases();		
		for ( MProjectPhase projectPhase : projectPhases ){
			if (projectPhase.getC_Phase_ID()==projectTypePhase.getC_Phase_ID() &&
					projectPhase.getName().equalsIgnoreCase(projectTypePhase.getName())) {
				resultProjectPhase = projectPhase;
				break;
			}
		}  // for each MProjectPhase
		
		return resultProjectPhase;
	} // getProjectPhase


	/**
	 * Get the Project Task for a Project Phase/Project Type Task combination
	 * @param projectPhase
	 * @param projectTypeTask
	 * @return MProjectTask or null
	 */
	private MProjectTask getProjectTask(MProjectPhase projectPhase, MProjectTypeTask projectTypeTask)
	{
		MProjectTask resultProjectTask = null;
		List<MProjectTask> projectTasks = projectPhase.getTasks();		
		for ( MProjectTask projectTask : projectTasks ){
			if (projectTask.getName().equalsIgnoreCase(projectTypeTask.getName())) {
				resultProjectTask = projectTask;
				break;
			}
		}  // for each MProjectTask
		
		return resultProjectTask;
	} // getProjectTask

	/**
	 * Correct sequences of Project
	 * Due to the uncontrolled addition of Phases and Tasks it may be duplicated sequences
	 * The new sequences may differ from the originals
	 * Here a new sequence is set
	 * @param project
	 * @return true
	 */
	private boolean correctProjectSequences(MProject project)
	{
		int seqNoPhase = 10;
		for ( MProjectPhase projectPhase : project.getPhases() ){
			projectPhase.setSeqNo(seqNoPhase);
			seqNoPhase = seqNoPhase + 10;
			projectPhase.saveEx();

			int seqNoTask = 10;
			for ( MProjectTask projectTask : projectPhase.getTasks() ){
				projectTask.setSeqNo(seqNoTask);
				seqNoTask = seqNoTask + 10;
				projectTask.saveEx();
			};
		}
		return true;
	} // correctProjectSequences
	
} // CopyProjectTypeTasksIntoProject