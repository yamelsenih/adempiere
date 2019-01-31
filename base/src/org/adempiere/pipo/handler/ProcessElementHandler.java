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
 *                                                                            *
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com                     *
 * Contributor(s): Low Heng Sin hengsin@avantz.com                            *
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.model.I_AD_Browse;
import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Form;
import org.compiere.model.I_AD_PrintFormat;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_ReportView;
import org.compiere.model.I_AD_Workflow;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.X_AD_Process;
import org.compiere.model.X_AD_Process_Para;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ProcessElementHandler extends AbstractElementHandler {

	private ProcessParaElementHandler paraHandler = new ProcessParaElementHandler();
	
	private List<Integer> processes = new ArrayList<Integer>();

	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_Process.Table_Name);
		log.info(elementValue + " " + uuid);
		String entitytype = getStringValue(atts, I_AD_Process.COLUMNNAME_EntityType);
		int id = 0;
		if (isProcessElement(ctx, entitytype)) {
			// Get New process.
			id = getIdWithFromUUID(ctx, I_AD_Process.Table_Name, uuid);
			X_AD_Process process = null;
			int backupId = -1;
			String objectStatus = null;
			if (id > 0) {
				process = new X_AD_Process(ctx, id, getTrxName(ctx));
				backupId = copyRecord(ctx, "AD_Process", process);
				objectStatus = "Update";
			} else {
				process = new X_AD_Process(ctx, id, getTrxName(ctx));
				if (id <= 0 && getIntValue(atts, I_AD_Process.COLUMNNAME_AD_Process_ID) > 0 && getIntValue(atts, I_AD_Process.COLUMNNAME_AD_Process_ID) <= PackOut.MAX_OFFICIAL_ID) {
					process.setAD_Process_ID(getIntValue(atts, I_AD_Process.COLUMNNAME_AD_Process_ID));
					process.setIsDirectLoad(true);
				}
				objectStatus = "New";
				backupId = 0;
			}
			process.setUUID(uuid);
			// Workflow
			uuid = getUUIDValue(atts, I_AD_Process.COLUMNNAME_AD_Workflow_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Workflow.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				process.setAD_Workflow_ID(id);
			}
			// Print Format
			uuid = getUUIDValue(atts, I_AD_Process.COLUMNNAME_AD_PrintFormat_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_PrintFormat.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				process.setAD_PrintFormat_ID(id);
			}
			// Report View
			uuid = getUUIDValue(atts, I_AD_Process.COLUMNNAME_AD_ReportView_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_ReportView.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				process.setAD_ReportView_ID(id);
			}
			// Form
			uuid = getUUIDValue(atts, I_AD_Process.COLUMNNAME_AD_Form_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Form.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				process.setAD_Form_ID(id);
			}
			// Browse
			uuid = getUUIDValue(atts, I_AD_Process.COLUMNNAME_AD_Browse_ID);
			if (!Util.isEmpty(uuid)) {
				id = getIdWithFromUUID(ctx, I_AD_Browse.Table_Name, uuid);
				if (id <= 0) {
					element.defer = true;
					return;
				}
				process.setAD_Browse_ID(id);
			}
			//	Attributes
			process.setValue(getStringValue(atts, I_AD_Process.COLUMNNAME_Value));
			process.setName(getStringValue(atts, I_AD_Process.COLUMNNAME_Name));
			process.setDescription(getStringValue(atts, I_AD_Process.COLUMNNAME_Description));
			process.setHelp(getStringValue(atts, I_AD_Process.COLUMNNAME_Help));
			process.setAccessLevel(getStringValue(atts, I_AD_Process.COLUMNNAME_AccessLevel));
			process.setClassname(getStringValue(atts, I_AD_Process.COLUMNNAME_Classname));
			process.setEntityType(getStringValue(atts, I_AD_Process.COLUMNNAME_EntityType));
			process.setIsActive(getBooleanValue(atts, I_AD_Process.COLUMNNAME_IsActive));
			process.setIsBetaFunctionality(getBooleanValue(atts, I_AD_Process.COLUMNNAME_IsBetaFunctionality));
			process.setIsDirectPrint(getBooleanValue(atts, I_AD_Process.COLUMNNAME_IsDirectPrint));
			process.setIsReport(getBooleanValue(atts, I_AD_Process.COLUMNNAME_IsReport));
			process.setIsServerProcess(getBooleanValue(atts, I_AD_Process.COLUMNNAME_IsServerProcess));
			process.setJasperReport(getStringValue(atts, I_AD_Process.COLUMNNAME_JasperReport));
			process.setProcedureName(getStringValue(atts, I_AD_Process.COLUMNNAME_ProcedureName));
			process.setShowHelp(getStringValue(atts, I_AD_Process.COLUMNNAME_ShowHelp));
			process.setStatistic_Count(getIntValue(atts, I_AD_Process.COLUMNNAME_Statistic_Count));
			process.setStatistic_Seconds(getIntValue(atts, I_AD_Process.COLUMNNAME_Statistic_Seconds));
			process.setWorkflowValue(getStringValue(atts, I_AD_Process.COLUMNNAME_WorkflowValue));
			//	Save
			try {
				process.saveEx(getTrxName(ctx));
				recordLog(ctx, 1, process.getName(), "Process", process
						.get_ID(), backupId, objectStatus, "AD_Process",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Process"));
				element.recordId = process.getAD_Process_ID();
			} catch (Exception e) {
				recordLog(ctx, 0, process.getName(), "Process", process
						.get_ID(), backupId, objectStatus, "AD_Process",
						get_IDWithColumn(ctx, "AD_Table", "TableName",
								"AD_Process"));
				throw new POSaveFailedException(e);
			}
		} else {
			element.skip = true;
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int processId = Env.getContextAsInt(ctx, "AD_Process_ID");
		if (processes.contains(processId)) {
			return;
		}
		processes.add(processId);
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		//	
		AttributesImpl atts = new AttributesImpl();
		MProcess process = new MProcess(ctx, processId, null);
		log.log(Level.INFO, "AD_ReportView_ID: " + process.getAD_Process_ID());
		if (process.isReport() && process.getAD_ReportView_ID() > 0) {
			packOut.createReportview(process.getAD_ReportView_ID(), document);
		}
		if (process.isReport() && process.getAD_PrintFormat_ID() > 0) {
			packOut.createPrintFormat(process.getAD_PrintFormat_ID(), document);
		}
		if (process.getAD_Workflow_ID() > 0) {
			packOut.createWorkflow(process.getAD_Workflow_ID(), document);
		}
		if (process.getAD_Form_ID() > 0) {
			packOut.createForm(process.getAD_Form_ID(), document);
		}
		if (process.getAD_Browse_ID() > 0) {
			packOut.createBrowse(process.getAD_Browse_ID(), document);
		}
		createProcessBinding(atts, process);
		document.startElement("", "", "process", atts);
		for(MProcessPara parameter : process.getParameters()) {
			packOut.createAdElement(parameter.getAD_Element_ID(), document);
			createProcessPara(ctx, document, parameter.getAD_Process_Para_ID());
		}
		document.endElement("", "", "process");
	}

	private void createProcessPara(Properties ctx, TransformerHandler document, int processParaId) throws SAXException {
		Env.setContext(ctx, X_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID, processParaId);
		paraHandler.create(ctx, document);
		ctx.remove(X_AD_Process_Para.COLUMNNAME_AD_Process_Para_ID);
	}

	private AttributesImpl createProcessBinding(AttributesImpl atts, X_AD_Process process) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, process);
		if (process.getAD_Process_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_Process.COLUMNNAME_AD_Process_ID);
		}
		filler.addUUID();
		//	
		filler.add(I_AD_Process.COLUMNNAME_Name);
		//	Workflow
		if (process.getAD_Workflow_ID() > 0) {
			filler.add(I_AD_Process.COLUMNNAME_AD_Workflow_ID, true);
			filler.addUUID(I_AD_Process.COLUMNNAME_AD_Workflow_ID, getUUIDFromId(process.getCtx(), I_AD_Workflow.Table_Name, process.getAD_Workflow_ID()));
		}
		//	Print Format
		if (process.getAD_PrintFormat_ID() > 0) {
			filler.add(I_AD_Process.COLUMNNAME_AD_PrintFormat_ID, true);
			filler.addUUID(I_AD_Process.COLUMNNAME_AD_PrintFormat_ID, getUUIDFromId(process.getCtx(), I_AD_PrintFormat.Table_Name, process.getAD_PrintFormat_ID()));
		}
		//	Report View
		if (process.getAD_ReportView_ID() > 0) {
			filler.add(I_AD_Process.COLUMNNAME_AD_ReportView_ID, true);
			filler.addUUID(I_AD_Process.COLUMNNAME_AD_ReportView_ID, getUUIDFromId(process.getCtx(), I_AD_ReportView.Table_Name, process.getAD_ReportView_ID()));
		}
		//	Form
		if (process.getAD_Form_ID() > 0) {
			filler.add(I_AD_Process.COLUMNNAME_AD_Form_ID, true);
			filler.addUUID(I_AD_Process.COLUMNNAME_AD_Form_ID, getUUIDFromId(process.getCtx(), I_AD_Form.Table_Name, process.getAD_Form_ID()));
		}
		//	Browse
		if (process.getAD_Browse_ID() > 0) {
			filler.add(I_AD_Process.COLUMNNAME_AD_Browse_ID, true);
			filler.addUUID(I_AD_Process.COLUMNNAME_AD_Browse_ID, getUUIDFromId(process.getCtx(), I_AD_Browse.Table_Name, process.getAD_Browse_ID()));
		}
		//	
		filler.add(I_AD_Process.COLUMNNAME_Value);
		filler.add(I_AD_Process.COLUMNNAME_Name);
		filler.add(I_AD_Process.COLUMNNAME_Description);
		filler.add(I_AD_Process.COLUMNNAME_Help);
		filler.add(I_AD_Process.COLUMNNAME_AccessLevel);
		filler.add(I_AD_Process.COLUMNNAME_Classname);
		filler.add(I_AD_Process.COLUMNNAME_EntityType);
		filler.add(I_AD_Process.COLUMNNAME_IsActive);
		filler.add(I_AD_Process.COLUMNNAME_IsBetaFunctionality);
		filler.add(I_AD_Process.COLUMNNAME_IsDirectPrint);
		filler.add(I_AD_Process.COLUMNNAME_IsReport);
		filler.add(I_AD_Process.COLUMNNAME_IsServerProcess);
		filler.add(I_AD_Process.COLUMNNAME_JasperReport);
		filler.add(I_AD_Process.COLUMNNAME_ProcedureName);
		filler.add(I_AD_Process.COLUMNNAME_ShowHelp);
		filler.add(I_AD_Process.COLUMNNAME_Statistic_Count);
		filler.add(I_AD_Process.COLUMNNAME_Statistic_Seconds);
		filler.add(I_AD_Process.COLUMNNAME_WorkflowValue);
		return atts;
	}
}
