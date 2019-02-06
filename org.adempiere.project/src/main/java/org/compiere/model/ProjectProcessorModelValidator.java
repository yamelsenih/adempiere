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

package org.compiere.model;

import java.util.ArrayList;
import java.util.List;

import org.compiere.util.ProjectProcessorUtils;
import org.eevolution.model.MProjectProcessorLog;

/**
 * Project Processor Model Validator
 * @author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 *  	<a href="https://github.com/adempiere/adempiere/issues/2202">
 *		@see FR [ 2202 ] Add Support to Project Processor</a>
 */
public class ProjectProcessorModelValidator implements ModelValidator{

	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		engine.addModelChange(MProject.Table_Name, this);
		engine.addModelChange(MProjectPhase.Table_Name, this);
		engine.addModelChange(MProjectTask.Table_Name, this);
	}

	@Override
	public int getAD_Client_ID() {
		return 0;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(PO entity, int type) throws Exception {
		
		if (entity.get_TableName().equals(MProject.Table_Name)
				|| entity.get_TableName().equals(MProjectPhase.Table_Name)
					|| entity.get_TableName().equals(MProjectTask.Table_Name)) {

			if (type == TYPE_AFTER_NEW
					|| (type == TYPE_AFTER_CHANGE
							&& columnsValids(entity))) {
				String eventChangeLog = (type == TYPE_AFTER_NEW ? MProjectProcessorLog.EVENTCHANGELOG_Insert : MProjectProcessorLog.EVENTCHANGELOG_Update);
				ProjectProcessorUtils.runProjectProcessor(entity, null, "", eventChangeLog);
				
			}
		}
		
		return null;
	}

	@Override
	public String docValidate(PO po, int timing) {
		return null;
	}
	
	/**
	 * Returns columns changed
	 * @param entity
	 * @return
	 */
	private List<String> columnsChanged(PO entity){
		ArrayList<String> retValue = new ArrayList<String>();
		for (int i=0; i< entity.get_ColumnCount(); i++) {
			if (entity.is_ValueChanged(i))
				retValue.add(entity.get_ColumnName(i));
		}
		
		return retValue;
	}
	
	/**
	 * Verified if column is valid
	 * @param entity
	 * @return
	 */
	private boolean columnsValids(PO entity) {
		List<String> columnsChanged = columnsChanged(entity);
		for (String columnName : columnsChanged) {
			if (ProjectProcessorUtils.isListenColumn(columnName))
				return true;
		}
		
		return false;
	}

}
