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

import org.adempiere.pipo.PackOut;
import org.compiere.model.Query;
import org.eevolution.model.I_HR_Attribute;
import org.eevolution.model.I_HR_Concept;
import org.eevolution.model.I_HR_Payroll;
import org.eevolution.model.I_HR_PayrollConcept;
import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHRPayroll;
import org.eevolution.model.MHRPayrollConcept;
import org.spin.model.I_HR_ProcessReport;
import org.spin.model.I_HR_ProcessReportLine;
import org.spin.model.I_HR_ProcessReportPayroll;
import org.spin.model.I_HR_ProcessReportSource;
import org.spin.model.I_HR_ProcessReportTemplate;
import org.spin.model.MHRProcessReport;
import org.spin.model.MHRProcessReportLine;
import org.spin.model.MHRProcessReportPayroll;
import org.spin.model.MHRProcessReportSource;
import org.spin.model.MHRProcessReportTemplate;
import org.xml.sax.SAXException;

/**
 * Payroll Exporter
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class PayrollExporter extends ClientExporterHandler {
	@Override
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		//	add here exclusion tables
		List<String> parentsToExclude = new ArrayList<String>();
		//	Export element Value
		List<MHRConcept> conceptList = new Query(ctx, I_HR_Concept.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Concept.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRConcept concept : conceptList) {
			if(concept.getHR_Concept_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default bank account
			cleanOfficialReference(concept);
			packOut.createGenericPO(document, concept, true, parentsToExclude);
			//	Export attribute
			List<MHRAttribute> attributeList = new Query(ctx, I_HR_Attribute.Table_Name, I_HR_Attribute.COLUMNNAME_HR_Concept_ID + " = ?", null)
					.setParameters(concept.getHR_Concept_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setOrderBy(I_HR_Attribute.COLUMNNAME_ValidFrom)
					.list();
			//	Export
			for(MHRAttribute attribute : attributeList) {
				if(attribute.getHR_Attribute_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default bank account
				cleanOfficialReference(attribute);
				packOut.createGenericPO(document, attribute, true, parentsToExclude);
			}
		}
		//	Export Payroll Definition
		List<MHRPayroll> payrollList = new Query(ctx, I_HR_Payroll.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Payroll.COLUMNNAME_Value)
				.list();
		//	Export Payroll
		for(MHRPayroll payroll : payrollList) {
			if(payroll.getHR_Payroll_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default values
			cleanOfficialReference(payroll);
			packOut.createGenericPO(document, payroll, true, parentsToExclude);
			//	Export Payroll Concept
			List<MHRPayrollConcept> payrollConceptList = new Query(ctx, I_HR_PayrollConcept.Table_Name, I_HR_PayrollConcept.COLUMNNAME_HR_Payroll_ID + " = ?", null)
					.setParameters(payroll.getHR_Payroll_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setOrderBy(I_HR_PayrollConcept.COLUMNNAME_SeqNo)
					.list();
			//	Export
			for(MHRPayrollConcept payrollConcept : payrollConceptList) {
				if(payrollConcept.getHR_Payroll_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default values
				cleanOfficialReference(payrollConcept);
				packOut.createGenericPO(document, payrollConcept, true, parentsToExclude);
			}
		}
		//	Export Payroll Definition
		List<MHRProcessReport> reportList = new Query(ctx, I_HR_ProcessReport.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_ProcessReport.COLUMNNAME_Name)
				.list();
		for(MHRProcessReport report : reportList) {
			if(report.getHR_ProcessReport_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default values
			cleanOfficialReference(report);
			packOut.createGenericPO(document, report, true, parentsToExclude);
			//	Export Payroll Concept
			List<MHRProcessReportLine> processReportLineList = new Query(ctx, I_HR_ProcessReportLine.Table_Name, I_HR_ProcessReportLine.COLUMNNAME_HR_ProcessReport_ID + " = ?", null)
					.setParameters(report.getHR_ProcessReport_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.setOrderBy(I_HR_ProcessReportLine.COLUMNNAME_SeqNo)
					.list();
			//	Export
			for(MHRProcessReportLine processReportLine : processReportLineList) {
				if(processReportLine.getHR_ProcessReportLine_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default values
				cleanOfficialReference(processReportLine);
				packOut.createGenericPO(document, processReportLine, true, parentsToExclude);
				//	Export Source
				List<MHRProcessReportSource> processReportSourceList = new Query(ctx, I_HR_ProcessReportSource.Table_Name, I_HR_ProcessReportSource.COLUMNNAME_HR_ProcessReportLine_ID + " = ?", null)
						.setParameters(processReportLine.getHR_ProcessReportLine_ID())
						.setOnlyActiveRecords(true)
						.setClient_ID()
						.setOrderBy(I_HR_ProcessReportSource.COLUMNNAME_SeqNo)
						.list();
				//	Export
				for(MHRProcessReportSource processReportSource : processReportSourceList) {
					if(processReportSource.getHR_ProcessReportSource_ID() < PackOut.MAX_OFFICIAL_ID) {
						continue;
					}
					//	Remove default values
					cleanOfficialReference(processReportSource);
					packOut.createGenericPO(document, processReportSource, true, parentsToExclude);
				}
			}
			//	Export Payroll Report
			List<MHRProcessReportPayroll> processReportPayrollList = new Query(ctx, I_HR_ProcessReportPayroll.Table_Name, I_HR_ProcessReportPayroll.COLUMNNAME_HR_ProcessReport_ID + " = ?", null)
					.setParameters(report.getHR_ProcessReport_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.list();
			//	Export
			for(MHRProcessReportPayroll processReportPayroll : processReportPayrollList) {
				if(processReportPayroll.getHR_ProcessReportPayroll_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default values
				cleanOfficialReference(processReportPayroll);
				packOut.createGenericPO(document, processReportPayroll, true, parentsToExclude);
			}
			//	Export Payroll Template
			List<MHRProcessReportTemplate> processReportTemplateList = new Query(ctx, I_HR_ProcessReportTemplate.Table_Name, I_HR_ProcessReportTemplate.COLUMNNAME_HR_ProcessReport_ID + " = ?", null)
					.setParameters(report.getHR_ProcessReport_ID())
					.setOnlyActiveRecords(true)
					.setClient_ID()
					.list();
			//	Export
			for(MHRProcessReportTemplate processReportTemplate : processReportTemplateList) {
				if(processReportTemplate.getHR_ProcessReportTemplate_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default values
				cleanOfficialReference(processReportTemplate);
				packOut.createGenericPO(document, processReportTemplate, true, parentsToExclude);
			}
		}
	}
}
