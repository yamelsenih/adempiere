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
import java.util.ArrayList;
import java.util.List;

import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Util;
import org.eevolution.service.dsl.ProcessBuilder;

/** Generated Process for (Payroll Process Report)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class EmployeePayrollReportExternalStorage extends EmployeePayrollReportExternalStorageAbstract {

	@Override
	protected String doIt() throws Exception {
		StringBuffer sql = new StringBuffer("SELECT C_BPartner_ID FROM C_BPartner "
				+ "WHERE EXISTS(SELECT 1 FROM RV_HR_ProcessDetail WHERE C_BPartner_ID = C_BPartner.C_BPartner_ID");
		List<Object> params = new ArrayList<>();
		//	Organization
		if(getOrgId() > 0) {
			sql.append(" AND AD_Org_ID = ?");
			params.add(getOrgId());
		}
		//	Contract
		if(getPayrollId() > 0) {
			sql.append(" AND HR_Contract_ID = ?");
			params.add(getPayrollId());
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
		int[] ids = DB.getIDsEx(get_TrxName(), sql.toString(), params);
		//	IDs
		for(int bPartnerId : ids) {
			ProcessInfo processInfo = ProcessBuilder.create(getCtx())
					.process(PayrollProcessReport.class)
					.withTitle("Employee Report")
					.withParameter(AD_ORG_ID, getOrgId())
					.withParameter(HR_CONTRACT_ID, getContractId())
					.withParameter(HR_PAYROLL_ID, getPayrollId())
					.withParameter(HR_PROCESS_ID, getHRProcessId())
					.withParameter(HR_DEPARTMENT_ID, getDepartmentId())
					.withParameter(HR_JOB_ID, getJobId())
					.withParameter(EMPLOYEESTATUS, getEmployeeStatus())
					.withParameter(C_BPARTNER_ID, bPartnerId)
					.withParameter(DATEACCT, getDateAcct())
					.withParameter(DATEACCT + "_To", getDateAcctTo())
					.withParameter(DOCSTATUS, getDocStatus())
					.withParameter(HR_PROCESSREPORT_ID, getProcessReportId())
					.withParameter(HR_PROCESSREPORTTEMPLATE_ID, getProcessReportTemplateId())
					.execute();
			//	Get Pdf
			if(processInfo != null) {
				File pdf = processInfo.getPDFReport();
				System.err.println(pdf);
			}
		}
		return "";
	}
}