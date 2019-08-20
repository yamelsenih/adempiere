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
import org.compiere.model.I_AD_Sequence;
import org.compiere.model.I_AD_Sequence_No;
import org.compiere.model.I_AD_Table_Access;
import org.compiere.model.I_AD_Task_Access;
import org.compiere.model.I_AD_User_Roles;
import org.compiere.model.I_AD_Window_Access;
import org.compiere.model.I_AD_Workflow_Access;
import org.compiere.model.I_C_BP_Group;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Charge;
import org.compiere.model.I_C_ChargeType;
import org.compiere.model.I_C_ChargeType_DocType;
import org.compiere.model.I_C_Currency;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Location;
import org.compiere.model.I_C_NonBusinessDay;
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
import org.compiere.model.MNonBusinessDay;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MProcessAccess;
import org.compiere.model.MRecordAccess;
import org.compiere.model.MRole;
import org.compiere.model.MRoleOrgAccess;
import org.compiere.model.MTableAccess;
import org.compiere.model.MWindowAccess;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Document_Action_Access;
import org.compiere.model.X_AD_Role_Included;
import org.compiere.model.X_AD_Task_Access;
import org.compiere.model.X_C_ChargeType;
import org.compiere.model.X_C_ChargeType_DocType;
import org.compiere.util.Env;
import org.compiere.wf.MWorkflowAccess;
import org.spin.model.I_AD_Dashboard_Access;
import org.spin.model.X_AD_Dashboard_Access;
import org.xml.sax.SAXException;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class GeneralExporter extends ClientExporterHandler {
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
		parentsToExclude.add(I_C_Currency.Table_Name);
		parentsToExclude.add(I_GL_Category.Table_Name);
		parentsToExclude.add(I_AD_User_Roles.Table_Name);
		parentsToExclude.add(I_AD_Sequence.Table_Name);
		parentsToExclude.add(I_AD_Sequence_No.Table_Name);
		//	Export Account Elements
		List<MDocType> documentTypeList = new Query(ctx, I_C_DocType.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export menu
		for(MDocType documentTypeExporter : documentTypeList) {
			if(documentTypeExporter.getC_DocType_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(documentTypeExporter);
			packOut.createGenericPO(document, documentTypeExporter, true, parentsToExclude);
		}
		//	Role
		List<MRole> roleList = new Query(ctx, I_AD_Role.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Export menu
		for(MRole role : roleList) {
			if(role.getAD_Role_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(role);
			createRole(ctx, role, document, parentsToExclude);
		}
		//	Charge Type List
		List<X_C_ChargeType> chargeTypeList = new Query(ctx, I_C_ChargeType.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export menu
		for(X_C_ChargeType chargeType : chargeTypeList) {
			if(chargeType.getC_ChargeType_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(chargeType);
			packOut.createGenericPO(document, chargeType, true, parentsToExclude);
			//	Charge List
			List<X_C_ChargeType_DocType> chargeTypeDocumentTypeList = new Query(ctx, I_C_ChargeType_DocType.Table_Name, I_C_ChargeType_DocType.COLUMNNAME_C_ChargeType_ID + " = ?", null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setParameters(chargeType.getC_ChargeType_ID())
				.list();
			//	Export menu
			for(X_C_ChargeType_DocType chargeTypeDocumentType : chargeTypeDocumentTypeList) {
				if(chargeTypeDocumentType.getC_DocType_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				MDocType documentType = MDocType.get(chargeTypeDocumentType.getCtx(), chargeTypeDocumentType.getC_DocType_ID());
				if(documentType.getC_DocTypeInvoice_ID() < PackOut.MAX_OFFICIAL_ID) {
					documentType.set_ValueOfColumn("C_DocTypeInvoice_ID", null);
				}
				if(documentType.getC_DocTypeDifference_ID() < PackOut.MAX_OFFICIAL_ID) {
					documentType.set_ValueOfColumn("C_DocTypeDifference_ID", null);
				}
				if(documentType.getC_DocTypeShipment_ID() < PackOut.MAX_OFFICIAL_ID) {
					documentType.set_ValueOfColumn("C_DocTypeShipment_ID", null);
				}
				if(documentType.getC_DocTypePayment_ID() < PackOut.MAX_OFFICIAL_ID) {
					documentType.set_ValueOfColumn("C_DocTypePayment_ID", null);
				}
				if(documentType.getC_DocTypeProforma_ID() < PackOut.MAX_OFFICIAL_ID) {
					documentType.set_ValueOfColumn("C_DocTypeProforma_ID", null);
				}
				cleanOfficialReference(documentType);
				packOut.createGenericPO(document, documentType, true, parentsToExclude);
				//	
				cleanOfficialReference(chargeTypeDocumentType);
				packOut.createGenericPO(document, chargeTypeDocumentType, true, parentsToExclude);
			}
		}
		//	Charge List
		List<MCharge> chargeList = new Query(ctx, I_C_Charge.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export menu
		for(MCharge charge : chargeList) {
			if(charge.getC_Charge_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(charge);
			packOut.createGenericPO(document, charge, true, parentsToExclude);
		}
		//	BP
		createBusinessPartners(ctx, document, parentsToExclude);
		createCalendar(ctx, document, parentsToExclude);
	}
	
	/**
	 * Export Calendar
	 * @param ctx
	 * @param document
	 * @param parentsToExclude
	 * @throws SAXException
	 */
	private void createCalendar(Properties ctx, TransformerHandler document, List<String> parentsToExclude) throws SAXException {
		//	Charge List
		List<MNonBusinessDay> nonBusinessDayList = new Query(ctx, I_C_NonBusinessDay.Table_Name, null, null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.list();
		//	Export menu
		for(MNonBusinessDay nonBusinessDay : nonBusinessDayList) {
			if(nonBusinessDay.getC_NonBusinessDay_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			cleanOfficialReference(nonBusinessDay);
			packOut.createGenericPO(document, nonBusinessDay, true, parentsToExclude);
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
