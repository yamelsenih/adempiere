/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.               *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *																			  *
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com					  *
 * Contributor(s): Low Heng Sin hengsin@avantz.com							  *
 *                 Victor Perez  victor.perez@e-evoluton.com				  *
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_Browse_Access;
import org.adempiere.model.X_AD_Browse_Access;
import org.compiere.model.I_AD_Document_Action_Access;
import org.compiere.model.I_AD_Form_Access;
import org.compiere.model.I_AD_Process_Access;
import org.compiere.model.I_AD_Role;
import org.compiere.model.I_AD_Role_Included;
import org.compiere.model.I_AD_Role_OrgAccess;
import org.compiere.model.I_AD_Table_Access;
import org.compiere.model.I_AD_Task_Access;
import org.compiere.model.I_AD_User_Roles;
import org.compiere.model.I_AD_Window_Access;
import org.compiere.model.I_AD_Workflow_Access;
import org.compiere.model.MDocType;
import org.compiere.model.MFormAccess;
import org.compiere.model.MProcessAccess;
import org.compiere.model.MRole;
import org.compiere.model.MRoleOrgAccess;
import org.compiere.model.MTableAccess;
import org.compiere.model.MUserRoles;
import org.compiere.model.MWindowAccess;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Document_Action_Access;
import org.compiere.model.X_AD_Package_Exp_Detail;
import org.compiere.model.X_AD_Role_Included;
import org.compiere.model.X_AD_Task_Access;
import org.compiere.util.Env;
import org.compiere.wf.MWorkflowAccess;
import org.spin.model.I_AD_Dashboard_Access;
import org.spin.model.X_AD_Dashboard_Access;
import org.xml.sax.SAXException;

public class RoleElementHandler extends GenericPOHandler {
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		Env.setContext(ctx, GenericPOHandler.TABLE_ID_TAG, I_AD_Role.Table_ID);
		int roleId = Env.getContextAsInt(ctx, X_AD_Package_Exp_Detail.COLUMNNAME_AD_Role_ID);
		Env.setContext(ctx, GenericPOHandler.RECORD_ID_TAG, roleId);
		super.create(ctx, document);
		ctx.remove(GenericPOHandler.TABLE_ID_TAG);
		ctx.remove(GenericPOHandler.RECORD_ID_TAG);
		//	Org Access
		GenericPOHandler accessHandle = new GenericPOHandler(false);
		List<MRoleOrgAccess> orgAccessList = new Query(ctx, I_AD_Role_OrgAccess.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MRoleOrgAccess>list();
		for(MRoleOrgAccess access : orgAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	User Access
		List<MUserRoles> userAccessList = new Query(ctx, I_AD_User_Roles.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MUserRoles>list();
		for(MUserRoles access : userAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Process Access
		List<MProcessAccess> processAccessList = new Query(ctx, I_AD_Process_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MProcessAccess>list();
		for(MProcessAccess access : processAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Window Access
		List<MWindowAccess> windowAccessList = new Query(ctx, I_AD_Window_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MWindowAccess>list();
		for(MWindowAccess access : windowAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Form Access
		List<MFormAccess> formAccessList = new Query(ctx, I_AD_Form_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MFormAccess>list();
		for(MFormAccess access : formAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Browse Access
		List<X_AD_Browse_Access> browseAccessList = new Query(ctx, I_AD_Browse_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<X_AD_Browse_Access>list();
		for(X_AD_Browse_Access access : browseAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Task Access
		List<X_AD_Task_Access> taskAccessList = new Query(ctx, I_AD_Task_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<X_AD_Task_Access>list();
		for(X_AD_Task_Access access : taskAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Dashboard Access
		List<X_AD_Dashboard_Access> dashboardAccessList = new Query(ctx, I_AD_Dashboard_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<X_AD_Dashboard_Access>list();
		for(X_AD_Dashboard_Access access : dashboardAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Table Access
		List<MTableAccess> tableAccessList = new Query(ctx, I_AD_Table_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MTableAccess>list();
		for(MTableAccess access : tableAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Workflow Access
		List<MWorkflowAccess> workflowAccessList = new Query(ctx, I_AD_Workflow_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<MWorkflowAccess>list();
		for(MWorkflowAccess access : workflowAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Document Action Access
		List<X_AD_Document_Action_Access> documentActionAccessList = new Query(ctx, I_AD_Document_Action_Access.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<X_AD_Document_Action_Access>list();
		for(X_AD_Document_Action_Access access : documentActionAccessList) {
			accessHandle.create(ctx, document, access);
		}
		//	Include Role Access
		List<X_AD_Role_Included> includeRoleAccessList = new Query(ctx, I_AD_Role_Included.Table_Name, "AD_Role_ID = ?", getTrxName(ctx))
			.setParameters(roleId)
			.<X_AD_Role_Included>list();
		for(X_AD_Role_Included access : includeRoleAccessList) {
			MRole includedRole = MRole.get(ctx, access.getIncluded_Role_ID());
			accessHandle.create(ctx, document, includedRole);
			accessHandle.create(ctx, document, access);
		}
	}
}
