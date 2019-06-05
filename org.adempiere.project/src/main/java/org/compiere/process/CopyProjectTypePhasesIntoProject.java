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
import org.compiere.model.MProjectTypePhase;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *	Project Type Tasks are copied into the project (which is a process parameter).
 *  If needed, Project Phases are created.
 *  If the Project Task for the Project Phase does already exist, nothing is done.
 *	
 *  @author Mario Calderon, mario.calderon@westfalia-it.com, http://www.westfalia-it.com
 *  @version $Id: CopyProjectTypeTasksIntoProject.java,v 1.0 2018/05/27 04:58:38 marcalwestf Exp $
 *  
 */
public class CopyProjectTypePhasesIntoProject extends CopyProjectTypePhasesIntoProjectAbstract
{
	/**	Project where Phases and Tasks will be copied to */
	private int		 p_C_Project_ID = 0;
	private MProject m_project;
	
	@Override
	protected void prepare()
	{
		super.prepare();
	}	//	prepare

	@Override
	protected String doIt() throws Exception
	{
		p_C_Project_ID = getProjectId();
		if (p_C_Project_ID==0)
			return "@C_Project_ID@ @NotFound@" ;
		else
			m_project = MProject.getById(getCtx(), p_C_Project_ID,get_TrxName());
		
		int count = 0;
		List<MProjectTypePhase> projectTypePhases = (List<MProjectTypePhase>) getInstancesForSelection(get_TrxName());
		count = populateProjectPhase(m_project, projectTypePhases);
		correctProjectSequences(m_project);
		
		return "@Created@/@Updated@ #" + count;
	}	//	doIt

	/**
	 * Populate Project Tasks into Project
	 * Create Project Phases if needed
	 * @param projectTasks
	 * @return count
	 */
	private int populateProjectPhase(MProject project, List<MProjectTypePhase> projectTypePhases)
	{
		AtomicInteger count = new AtomicInteger(0);
		projectTypePhases.stream()
				.forEach(projectTypePhase -> {
					// Check if Project PHASE already exists
					MProjectPhase projectPhase = getProjectPhase(project, projectTypePhase);
					if (projectPhase==null){
						projectPhase = new MProjectPhase (project, projectTypePhase);
						projectPhase.setProjInvoiceRule(project.getProjInvoiceRule());
						projectPhase.saveEx();
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
		}
		return true;
	} // correctProjectSequences
	
} // CopyProjectTypePhasesIntoProject