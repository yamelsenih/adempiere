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

import org.compiere.model.I_C_Task;
import org.compiere.model.Query;
import org.compiere.model.X_C_Phase;
import org.compiere.model.X_C_Task;

/**
 * Project Wrapper class used because the project have wrong links with base source folder
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class ProjectPhaseTypeWrapper {
	
	/**
	 * Static instance
	 * @param projectPhase
	 * @return
	 */
	public static ProjectPhaseTypeWrapper newInstance(X_C_Phase projectPhase) {
		return new ProjectPhaseTypeWrapper(projectPhase);
	}
	
	/***
	 * Private constructor
	 * @param phase
	 */
	private ProjectPhaseTypeWrapper(X_C_Phase phase) {
		this.phase = phase;
	}
	
	private X_C_Phase phase;
	/**	Logger							*/
	protected CLogger log = CLogger.getCLogger (getClass());
	/**
	 * @return the project
	 */
	public final X_C_Phase getProjectPhase() {
		return phase;
	}
	
	/**
	 * get Project Type Tasks
	 * @return list task
	 */
	public List<X_C_Task> getTasks() {
		return new Query(phase.getCtx(), I_C_Task.Table_Name, I_C_Task.COLUMNNAME_C_Phase_ID + "=?", phase.get_TrxName())
				.setClient_ID()
				.setParameters(phase.getC_Phase_ID())
				.setOrderBy(I_C_Task.COLUMNNAME_SeqNo)
				.list();
	}
}
