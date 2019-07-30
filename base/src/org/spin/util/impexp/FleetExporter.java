/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 or later of the                                  *
 * GNU General Public License as published                                    *
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
package org.spin.util.impexp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_Browse_Access;
import org.adempiere.model.X_AD_Browse_Access;
import org.adempiere.pipo.PackOut;
import org.compiere.model.I_AD_Column_Access;
import org.compiere.model.I_AD_Document_Action_Access;
import org.compiere.model.I_AD_Form_Access;
import org.compiere.model.I_AD_Process_Access;
import org.compiere.model.I_AD_Record_Access;
import org.compiere.model.I_AD_Role;
import org.compiere.model.I_AD_Role_Included;
import org.compiere.model.I_AD_Role_OrgAccess;
import org.compiere.model.I_AD_Table_Access;
import org.compiere.model.I_AD_Task_Access;
import org.compiere.model.I_AD_TreeNodeBP;
import org.compiere.model.I_AD_User_Roles;
import org.compiere.model.I_AD_Window_Access;
import org.compiere.model.I_AD_Workflow_Access;
import org.compiere.model.I_C_BP_Group;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Charge;
import org.compiere.model.I_C_Currency;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Location;
import org.compiere.model.I_C_PaymentTerm;
import org.compiere.model.I_GL_Category;
import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCharge;
import org.compiere.model.MColumnAccess;
import org.compiere.model.MDocType;
import org.compiere.model.MFormAccess;
import org.compiere.model.MLocation;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MProcessAccess;
import org.compiere.model.MRecordAccess;
import org.compiere.model.MRole;
import org.compiere.model.MRoleOrgAccess;
import org.compiere.model.MTableAccess;
import org.compiere.model.MWindowAccess;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Document_Action_Access;
import org.compiere.model.X_AD_Role_Included;
import org.compiere.model.X_AD_Task_Access;
import org.compiere.util.Env;
import org.compiere.wf.MWorkflowAccess;
import org.eevolution.model.I_DD_CompatibilityGroup;
import org.eevolution.model.I_DD_VehicleType;
import org.eevolution.model.MDDCompatibilityGroup;
import org.eevolution.model.MDDVehicleType;
import org.spin.model.I_AD_Dashboard_Access;
import org.spin.model.X_AD_Dashboard_Access;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class FleetExporter extends ClientExporterHandler {
	/**	Parents for no added	*/
	private List<String> parentsToExclude;
	/**	Packout	*/
	private PackOut packOut;
	@Override
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		parentsToExclude = new ArrayList<String>();
		createFleetManagement(ctx, document, parentsToExclude);
	}
	
	private void createFleetManagement(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	BP Group
		List<MDDCompatibilityGroup> compatibilityGroupList = new Query(ctx, I_DD_CompatibilityGroup.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export BP
		for(MDDCompatibilityGroup compatibilityGroup : compatibilityGroupList) {
			if(compatibilityGroup.getDD_CompatibilityGroup_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(compatibilityGroup);
			packOut.createGenericPO(document, compatibilityGroup, true, parentsToExclude);
		}
		
		//	BP List
		List<PO> vehicleGroupList = new Query(ctx, "DD_VehicleGroup", null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export BP
		for(PO vehicleGroup : vehicleGroupList) {
			if(vehicleGroup.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(vehicleGroup);
			packOut.createGenericPO(document, vehicleGroup, true, parentsToExclude);
		}
		//	BP Group
		List<MDDVehicleType> vehicleTypeList = new Query(ctx, I_DD_VehicleType.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export BP
		for(MDDVehicleType vehicleType : vehicleTypeList) {
			if(vehicleType.getDD_VehicleType_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(vehicleType);
			packOut.createGenericPO(document, vehicleType, true, parentsToExclude);
		}
	}
	
	/**
	 * Export BPartner
	 * @param ctx
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createBusinessPartners(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	BP Group
		List<MBPGroup> bPartnerGroupList = new Query(ctx, I_C_BP_Group.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
			//	Export BP
		for(MBPGroup bPartnerGroup : bPartnerGroupList) {
			if(bPartnerGroup.getC_BP_Group_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(bPartnerGroup);
			packOut.createGenericPO(document, bPartnerGroup, true, parentsToExclude);
		}
		//	BP List
		List<MPaymentTerm> paymentTermList = new Query(ctx, I_C_PaymentTerm.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export BP
		for(MPaymentTerm paymentTerm : paymentTermList) {
			if(paymentTerm.getC_PaymentTerm_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(paymentTerm);
			packOut.createGenericPO(document, paymentTerm, true, parentsToExclude);
		}
		//	BP List
		List<MBPartner> bPartnerList = new Query(ctx, I_C_BPartner.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export BP
		for(MBPartner bPartner : bPartnerList) {
			if(bPartner.getC_BPartner_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(bPartner);
			packOut.createGenericPO(document, bPartner, true, parentsToExclude);
		}
		List<MBPartnerLocation> bPartnerLocationList = new Query(ctx, I_C_BPartner_Location.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export BP
		for(MBPartnerLocation bPartnerLocation : bPartnerLocationList) {
			if(bPartnerLocation.getC_BPartner_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			List<MLocation> locationList = new Query(ctx, I_C_Location.Table_Name, I_C_Location.COLUMNNAME_C_Location_ID + " = ?", null)
					.setOnlyActiveRecords(true)
					.setParameters(bPartnerLocation.getC_Location_ID())
					.list();
			//	Export BPMBPartnerLocation
			for(MLocation location : locationList) {
				if(location.getC_Location_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				cleanOfficialReference(location);
				packOut.createGenericPO(document, location, true, parentsToExclude);
			}
			cleanOfficialReference(bPartnerLocation);
			packOut.createGenericPO(document, bPartnerLocation, true, parentsToExclude);
		}
	}
	
	/**
	 * Create Roles
	 * @param ctx
	 * @param role
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createRole(Properties ctx, MRole role, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		packOut.createGenericPO(document, role, true, parentsToExclude);
		//	Org Access
		List<MRoleOrgAccess> orgAccessList = new Query(ctx, I_AD_Role_OrgAccess.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<MRoleOrgAccess>list();
		for(MRoleOrgAccess access : orgAccessList) {
			if(access.getAD_Org_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Process Access
		List<MProcessAccess> processAccessList = new Query(ctx, I_AD_Process_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<MProcessAccess>list();
		for(MProcessAccess access : processAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Window Access
		List<MWindowAccess> windowAccessList = new Query(ctx, I_AD_Window_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<MWindowAccess>list();
		for(MWindowAccess access : windowAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Form Access
		List<MFormAccess> formAccessList = new Query(ctx, I_AD_Form_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<MFormAccess>list();
		for(MFormAccess access : formAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Browse Access
		List<X_AD_Browse_Access> browseAccessList = new Query(ctx, I_AD_Browse_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<X_AD_Browse_Access>list();
		for(X_AD_Browse_Access access : browseAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Task Access
		List<X_AD_Task_Access> taskAccessList = new Query(ctx, I_AD_Task_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<X_AD_Task_Access>list();
		for(X_AD_Task_Access access : taskAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Dashboard Access
		List<X_AD_Dashboard_Access> dashboardAccessList = new Query(ctx, I_AD_Dashboard_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<X_AD_Dashboard_Access>list();
		for(X_AD_Dashboard_Access access : dashboardAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Workflow Access
		List<MWorkflowAccess> workflowAccessList = new Query(ctx, I_AD_Workflow_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<MWorkflowAccess>list();
		for(MWorkflowAccess access : workflowAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Document Action Access
		List<X_AD_Document_Action_Access> documentActionAccessList = new Query(ctx, I_AD_Document_Action_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<X_AD_Document_Action_Access>list();
		for(X_AD_Document_Action_Access access : documentActionAccessList) {
			if(access.getC_DocType_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Include Role Access
		List<X_AD_Role_Included> includeRoleAccessList = new Query(ctx, I_AD_Role_Included.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<X_AD_Role_Included>list();
		for(X_AD_Role_Included access : includeRoleAccessList) {
			if(access.getIncluded_Role_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			MRole includedRole = MRole.get(ctx, access.getIncluded_Role_ID());
			packOut.createGenericPO(document, includedRole, true, parentsToExclude);
			packOut.createGenericPO(document, access);
		}
		//	Table Access
		List<MTableAccess> tableAccessList = new Query(ctx, I_AD_Table_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.<MTableAccess>list();
		for(MTableAccess access : tableAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Column Access
		List<MColumnAccess> columnAccessList = new Query(Env.getCtx(), I_AD_Column_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.setOnlyActiveRecords(true)
			.<MColumnAccess>list();
		for(MColumnAccess access : columnAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
		//	Record Access
		List<MRecordAccess> recordAccessList = new Query(Env.getCtx(), I_AD_Record_Access.Table_Name, "AD_Role_ID = ?", null)
			.setParameters(role.getAD_Role_ID())
			.setOnlyActiveRecords(true)
			.<MRecordAccess>list();
		for(MRecordAccess access : recordAccessList) {
			packOut.createGenericPO(document, access, true, parentsToExclude);
		}
	}
}
