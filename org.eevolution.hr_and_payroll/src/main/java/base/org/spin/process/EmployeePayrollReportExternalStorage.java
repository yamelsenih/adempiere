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

package org.spin.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Util;
import org.eevolution.model.MHRProcess;
import org.eevolution.service.dsl.ProcessBuilder;
import org.spin.model.MADAppRegistration;
import org.spin.model.MHRProcessReport;
import org.spin.util.support.AppSupportHandler;
import org.spin.util.support.IAppSupport;
import org.spin.util.support.webdav.IWebDav;

/** Generated Process for (Payroll Process Report)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class EmployeePayrollReportExternalStorage extends EmployeePayrollReportExternalStorageAbstract {

	@Override
	protected String doIt() throws Exception {
		StringBuffer sql = new StringBuffer("SELECT C_BPartner_ID, Value FROM C_BPartner "
				+ "WHERE EXISTS(SELECT 1 FROM RV_HR_ProcessDetail WHERE C_BPartner_ID = C_BPartner.C_BPartner_ID");
		List<Object> params = new ArrayList<>();
		//	Organization
		if(getOrgId() > 0) {
			sql.append(" AND AD_Org_ID = ?");
			params.add(getOrgId());
		}
		//	Contract
		if(getContractId() > 0) {
			sql.append(" AND HR_Contract_ID = ?");
			params.add(getContractId());
		}
		//	Payroll
		if(getPayrollId() > 0) {
			sql.append(" AND HR_Payroll_ID = ?");
			params.add(getPayrollId());
		}
		//	Payroll Process
		if(getHRProcessId() > 0) {
			sql.append(" AND HR_Process_ID = ?");
			params.add(getHRProcessId());
		}
		//	Department
		if(getDepartmentId() > 0) {
			sql.append(" AND HR_Department_ID = ?");
			params.add(getDepartmentId());
		}
		//	Job
		if(getJobId() > 0) {
			sql.append(" AND HR_Job_ID = ?");
			params.add(getJobId());
		}
		//	Employee Status
		if(!Util.isEmpty(getEmployeeStatus())) {
			sql.append(" AND EmployeeStatus = ?");
			params.add(getEmployeeStatus());
		}
		//	Document Status
		if(!Util.isEmpty(getDocStatus())) {
			sql.append(" AND DocStatus = ?");
			params.add(getDocStatus());
		}
		//	Process Report
		if(getProcessReportId() > 0) {
			sql.append(" AND HR_ProcessReport_ID = ?");
			params.add(getProcessReportId());
		}
		//	Accounting Date
		if(getDateAcct() != null
				&& getDateAcctTo() == null) {
			sql.append(" AND DateAcct >= ?");
			params.add(getDateAcct());
		} else if(getDateAcct() != null
				&& getDateAcctTo() != null) {
			sql.append(" AND DateAcct >= ?");
			sql.append(" AND DateAcct <= ?");
			params.add(getDateAcct());
			params.add(getDateAcctTo());
		} else if(getDateAcct() == null
				&& getDateAcctTo() != null) {
			sql.append(" AND DateAcct <= ?");
			params.add(getDateAcctTo());
		}
		//	Business Partner
		if(getBPartnerId() > 0) {
			sql.append(" AND C_BPartner_ID = ?");
			params.add(getBPartnerId());
		}
		sql.append(")");
		KeyNamePair[] pairs = DB.getKeyNamePairs(sql.toString(), false, params);
		//	
		IAppSupport supportedApi = AppSupportHandler.getInstance().getAppSupport(MADAppRegistration.getById(getCtx(), getAppRegistrationId(), get_TrxName()));
		if(supportedApi == null) {
			throw new AdempiereException("@AD_AppSupport_ID@ @NotFound@");
		}
		if(!(supportedApi instanceof IWebDav)) {
			throw new AdempiereException("@AD_AppSupport_ID@ @Unsupported@");
		}
		IWebDav webDavApi = (IWebDav) supportedApi;
		//	Folder name
		Timestamp date = new Timestamp(System.currentTimeMillis());
		if(getHRProcessId() > 0) {
			MHRProcess process = new MHRProcess(getCtx(), getHRProcessId(), get_TrxName());
			date = process.getDateAcct();
		}
		//	Payroll Process Report
		MHRProcessReport processReport = MHRProcessReport.get(getCtx(), getProcessReportId());
		String yearFolder = DisplayType.getDateFormat(DisplayType.Date, null, "yyyy").format(date);
		String monthFolder = DisplayType.getDateFormat(DisplayType.Date, null, "MM").format(date);
		String printName = processReport.getPrintName();
		if(Util.isEmpty(printName)) {
			printName = processReport.getName();
		}
		String completeFile = printName + "-" + DisplayType.getDateFormat(DisplayType.Date, null, "yyyy-MM-dd").format(date);
		//	IDs
		for(KeyNamePair bPartner : pairs) {
			ProcessBuilder builder = ProcessBuilder.create(getCtx())
					.process(PayrollProcessReport.class)
					.withTitle("Employee Report");
			//	Key
			builder.withParameter(C_BPARTNER_ID, bPartner.getKey());
			//	
			if(getOrgId() > 0) {
				builder.withParameter(AD_ORG_ID, getOrgId());
			}
			if(getContractId() > 0) {
				builder.withParameter(HR_CONTRACT_ID, getContractId());
			}
			if(getPayrollId() > 0) {
				builder.withParameter(HR_PAYROLL_ID, getPayrollId());
			}
			if(getHRProcessId() > 0) {
				builder.withParameter(HR_PROCESS_ID, getHRProcessId());
			}
			if(getDepartmentId() > 0) {
				builder.withParameter(HR_DEPARTMENT_ID, getDepartmentId());
			}
			if(getJobId() > 0) {
				builder.withParameter(HR_JOB_ID, getJobId());
			}
			if(!Util.isEmpty(getEmployeeStatus())) {
				builder.withParameter(EMPLOYEESTATUS, getEmployeeStatus());
			}
			if(getDateAcct() != null) {
				builder.withParameter(DATEACCT, getDateAcct());
			}
			if(getDateAcctTo() != null) {
				builder.withParameter(DATEACCT + "_To", getDateAcctTo());
			}		
			if(!Util.isEmpty(getDocStatus())) {
				builder.withParameter(DOCSTATUS, getDocStatus());
			}		
			if(getProcessReportId() > 0) {
				builder.withParameter(HR_PROCESSREPORT_ID, getProcessReportId());
			}
			if(getProcessReportTemplateId() > 0) {
				builder.withParameter(HR_PROCESSREPORT_ID, getProcessReportTemplateId());
			}
			//	Set print preview to 
			builder.withoutPrintPreview();
			//	
			ProcessInfo processInfo = builder.execute();
			//	Get Pdf
			if(processInfo != null) {
				File pdf = processInfo.getPDFReport();
				if(pdf != null) {
					MBPartner bPartnerInfo = MBPartner.get(getCtx(), bPartner.getKey());
					String employeeDirectory = bPartnerInfo.getValue() + "-" + bPartnerInfo.getName();
					employeeDirectory = employeeDirectory.replaceAll("[+^:&áàäéèëíìïóòöúùñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ$ ]", "-");
					completeFile = completeFile.replaceAll("[+^:&áàäéèëíìïóòöúùñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ$ ]", "-");
					String employeeYearFolder = employeeDirectory + "/" + yearFolder;
					String employeeMonthFolder = employeeYearFolder + "/" + monthFolder;
					String employeeCompleteFile = employeeMonthFolder + "/" + completeFile + ".pdf";
					//	Create for Employee
					if(!webDavApi.exists(employeeDirectory)) {
						webDavApi.createDirectory(employeeDirectory);
					}
					//	Create for Year
					if(!webDavApi.exists(employeeYearFolder)) {
						webDavApi.createDirectory(employeeYearFolder);
					}
					//	Create for Month
					if(!webDavApi.exists(employeeMonthFolder)) {
						webDavApi.createDirectory(employeeMonthFolder);
					}
					//	Add files
					InputStream fileToPut = new FileInputStream(pdf);
					//	Put File
					webDavApi.putResource(employeeCompleteFile, fileToPut);
					//	Add to Log
					addLog("@HR_Employee_ID@: " + employeeDirectory);
				}
			}
		}
		return "Ok";
	}
}