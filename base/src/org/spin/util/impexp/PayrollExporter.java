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
import org.eevolution.model.I_HR_CareerLevel;
import org.eevolution.model.I_HR_Concept;
import org.eevolution.model.I_HR_Concept_Category;
import org.eevolution.model.I_HR_Concept_Type;
import org.eevolution.model.I_HR_Degree;
import org.eevolution.model.I_HR_Department;
import org.eevolution.model.I_HR_Designation;
import org.eevolution.model.I_HR_EmployeeType;
import org.eevolution.model.I_HR_Grade;
import org.eevolution.model.I_HR_InsuranceType;
import org.eevolution.model.I_HR_Job;
import org.eevolution.model.I_HR_JobEducation;
import org.eevolution.model.I_HR_JobOpening;
import org.eevolution.model.I_HR_JobOpeningHistory;
import org.eevolution.model.I_HR_JobType;
import org.eevolution.model.I_HR_List;
import org.eevolution.model.I_HR_ListLine;
import org.eevolution.model.I_HR_ListType;
import org.eevolution.model.I_HR_ListVersion;
import org.eevolution.model.I_HR_Payroll;
import org.eevolution.model.I_HR_PayrollConcept;
import org.eevolution.model.I_HR_Race;
import org.eevolution.model.I_HR_Relationship;
import org.eevolution.model.I_HR_SalaryRange;
import org.eevolution.model.I_HR_SalaryStructure;
import org.eevolution.model.I_HR_SalaryStructureLine;
import org.eevolution.model.I_HR_SkillType;
import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHRCareerLevel;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHRConceptCategory;
import org.eevolution.model.MHRConceptType;
import org.eevolution.model.MHRDegree;
import org.eevolution.model.MHRDepartment;
import org.eevolution.model.MHRDesignation;
import org.eevolution.model.MHREmployeeType;
import org.eevolution.model.MHRGrade;
import org.eevolution.model.MHRInsuranceType;
import org.eevolution.model.MHRJob;
import org.eevolution.model.MHRJobEducation;
import org.eevolution.model.MHRJobOpenings;
import org.eevolution.model.MHRJobType;
import org.eevolution.model.MHRPayroll;
import org.eevolution.model.MHRPayrollConcept;
import org.eevolution.model.MHRRace;
import org.eevolution.model.MHRRelationship;
import org.eevolution.model.MHRSalaryRange;
import org.eevolution.model.MHRSalaryStructure;
import org.eevolution.model.MHRSalaryStructureLine;
import org.eevolution.model.MHRSkillType;
import org.eevolution.model.X_HR_JobOpeningHistory;
import org.eevolution.model.X_HR_List;
import org.eevolution.model.X_HR_ListLine;
import org.eevolution.model.X_HR_ListType;
import org.eevolution.model.X_HR_ListVersion;
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
	
	private List<String> parentsToExclude = new ArrayList<String>();
	@Override
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		PackOut packOut = (PackOut) ctx.get("PackOutProcess");
		if(packOut == null ) {
			packOut = new PackOut();
			packOut.setLocalContext(ctx);
		}
		//	Create Employee Setup
		createEmployeeSetup(ctx, document, packOut);
		//	Recruitment
		createRecruitment(ctx, document, packOut);
		//	Payroll Definition
		createPayroll(ctx, document, packOut);
		//	Payroll Report
		createPayrollReport(ctx, document, packOut);
	}
	
	
	public void createRecruitment(Properties ctx, TransformerHandler document, PackOut packOut) throws SAXException {
		//	Career Level
		List<MHRCareerLevel> careerLevelList = new Query(ctx, I_HR_CareerLevel.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_CareerLevel.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRCareerLevel careerLevel : careerLevelList) {
			if(careerLevel.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(careerLevel);
			packOut.createGenericPO(document, careerLevel, true, parentsToExclude);
		}
		//	Job Education
		List<MHRJobEducation> jobEducationList = new Query(ctx, I_HR_JobEducation.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_JobEducation.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRJobEducation jobEducation : jobEducationList) {
			if(jobEducation.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(jobEducation);
			packOut.createGenericPO(document, jobEducation, true, parentsToExclude);
		}
		//	Job Type
		List<MHRJobType> jobTypeList = new Query(ctx, I_HR_JobType.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_JobType.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRJobType jobType : jobTypeList) {
			if(jobType.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(jobType);
			packOut.createGenericPO(document, jobType, true, parentsToExclude);
		}
		//	Salary Range
		List<MHRSalaryRange> salaryRangeList = new Query(ctx, I_HR_SalaryRange.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_SalaryRange.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRSalaryRange salaryRange : salaryRangeList) {
			if(salaryRange.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(salaryRange);
			packOut.createGenericPO(document, salaryRange, true, parentsToExclude);
		}
		//	Job Openings
		List<MHRJobOpenings> jobOpeningsList = new Query(ctx, I_HR_JobOpening.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_JobOpening.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRJobOpenings jobOpening : jobOpeningsList) {
			if(jobOpening.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(jobOpening);
			packOut.createGenericPO(document, jobOpening, true, parentsToExclude);
			List<X_HR_JobOpeningHistory> jobOpeningHistoryList = new Query(ctx, I_HR_JobOpeningHistory.Table_Name, I_HR_JobOpeningHistory.COLUMNNAME_HR_JobOpening_ID + " = ?", null)
					.setOnlyActiveRecords(true)
					.setParameters(jobOpening.getHR_JobOpening_ID())
					.setClient_ID()
					.setOrderBy(I_HR_JobOpeningHistory.COLUMNNAME_DateTrx)
					.list();
			//	Export
			for(X_HR_JobOpeningHistory jobOpeningHistory : jobOpeningHistoryList) {
				if(jobOpeningHistory.get_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default
				cleanOfficialReference(jobOpeningHistory);
				packOut.createGenericPO(document, jobOpeningHistory, true, parentsToExclude);
			}
		}
	}
	
	/**
	 * Create Employee Setup
	 * @param ctx
	 * @param document
	 * @param packOut
	 * @throws SAXException
	 */
	public void createEmployeeSetup(Properties ctx, TransformerHandler document, PackOut packOut) throws SAXException {
		//	Structure
		List<MHRSalaryStructure> salaryStructureList = new Query(ctx, I_HR_SalaryStructure.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_SalaryStructure.COLUMNNAME_Value)
				.list();
		//	Salary Structure
		for(MHRSalaryStructure salaryStructure : salaryStructureList) {
			if(salaryStructure.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(salaryStructure);
			packOut.createGenericPO(document, salaryStructure, true, parentsToExclude);
			List<MHRSalaryStructureLine> salaryStructureLineList = new Query(ctx, I_HR_SalaryStructureLine.Table_Name, I_HR_SalaryStructureLine.COLUMNNAME_HR_SalaryStructure_ID + " = ?", null)
					.setOnlyActiveRecords(true)
					.setParameters(salaryStructure.getHR_SalaryStructure_ID())
					.setClient_ID()
					.setOrderBy(I_HR_SalaryStructureLine.COLUMNNAME_Amount)
					.list();
			//	Export
			for(MHRSalaryStructureLine salaryStructureLine : salaryStructureLineList) {
				if(salaryStructureLine.get_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default
				cleanOfficialReference(salaryStructureLine);
				packOut.createGenericPO(document, salaryStructureLine, true, parentsToExclude);
			}
		}
		//	Designation
		List<MHRDesignation> designationList = new Query(ctx, I_HR_Designation.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Designation.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRDesignation designation : designationList) {
			if(designation.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(designation);
			packOut.createGenericPO(document, designation, true, parentsToExclude);
		}
		//	Degree
		List<MHRDegree> degreeList = new Query(ctx, I_HR_Degree.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Degree.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRDegree degree : degreeList) {
			if(degree.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(degree);
			packOut.createGenericPO(document, degree, true, parentsToExclude);
		}
		//	Insurance Type
		List<MHRInsuranceType> insuranceTypeList = new Query(ctx, I_HR_InsuranceType.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_InsuranceType.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRInsuranceType insuranceType : insuranceTypeList) {
			if(insuranceType.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(insuranceType);
			packOut.createGenericPO(document, insuranceType, true, parentsToExclude);
		}
		//	Skill Type
		List<MHRSkillType> skillTypeList = new Query(ctx, I_HR_SkillType.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_SkillType.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRSkillType skillType : skillTypeList) {
			if(skillType.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(skillType);
			packOut.createGenericPO(document, skillType, true, parentsToExclude);
		}
		//	Race
		List<MHRRace> raceList = new Query(ctx, I_HR_Race.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Race.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRRace race : raceList) {
			if(race.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(race);
			packOut.createGenericPO(document, race, true, parentsToExclude);
		}
		//	Grade
		List<MHRGrade> gradeList = new Query(ctx, I_HR_Grade.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Grade.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRGrade grade : gradeList) {
			if(grade.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(grade);
			packOut.createGenericPO(document, grade, true, parentsToExclude);
		}
		//	Employee Type
		List<MHREmployeeType> employeeTypeList = new Query(ctx, I_HR_EmployeeType.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_EmployeeType.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHREmployeeType employeeType : employeeTypeList) {
			if(employeeType.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(employeeType);
			packOut.createGenericPO(document, employeeType, true, parentsToExclude);
		}
		//	Employee Relation
		List<MHRRelationship> relationshipList = new Query(ctx, I_HR_Relationship.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Relationship.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRRelationship relationship : relationshipList) {
			if(relationship.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(relationship);
			packOut.createGenericPO(document, relationship, true, parentsToExclude);
		}
	}
	
	/**
	 * Create Payroll export
	 * @param ctx
	 * @param document
	 * @param packOut
	 * @throws SAXException
	 */
	public void createPayroll(Properties ctx, TransformerHandler document, PackOut packOut) throws SAXException {
		//	Concept Category
		List<MHRDepartment> departmentList = new Query(ctx, I_HR_Department.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Department.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRDepartment department : departmentList) {
			if(department.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(department);
			packOut.createGenericPO(document, department, true, parentsToExclude);
		}
		//	Concept Category
		List<MHRJob> jobList = new Query(ctx, I_HR_Job.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Job.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRJob job : jobList) {
			if(job.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(job);
			packOut.createGenericPO(document, job, true, parentsToExclude);
		}
		//	Concept Category
		List<MHRConceptCategory> conceptCategoryList = new Query(ctx, I_HR_Concept_Category.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Concept_Category.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRConceptCategory conceptCategory : conceptCategoryList) {
			if(conceptCategory.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(conceptCategory);
			packOut.createGenericPO(document, conceptCategory, true, parentsToExclude);
		}
		//	List Type
		List<X_HR_ListType> listTypeDefinitionList = new Query(ctx, I_HR_ListType.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_ListType.COLUMNNAME_Value)
				.list();
		//	Export
		for(X_HR_ListType listType : listTypeDefinitionList) {
			if(listType.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(listType);
			packOut.createGenericPO(document, listType, true, parentsToExclude);
			//	List
			List<X_HR_List> listDefinitionList = new Query(ctx, I_HR_List.Table_Name, I_HR_List.COLUMNNAME_HR_ListType_ID + " = ?", null)
					.setOnlyActiveRecords(true)
					.setParameters(listType.getHR_ListType_ID())
					.setClient_ID()
					.setOrderBy(I_HR_List.COLUMNNAME_Value)
					.list();
			//	Export
			for(X_HR_List listDefinition : listDefinitionList) {
				if(listDefinition.get_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default
				cleanOfficialReference(listDefinition);
				packOut.createGenericPO(document, listDefinition, true, parentsToExclude);
				//	List Version
				List<X_HR_ListVersion> listVersionList = new Query(ctx, I_HR_ListVersion.Table_Name, I_HR_ListVersion.COLUMNNAME_HR_List_ID + " = ?", null)
						.setOnlyActiveRecords(true)
						.setParameters(listDefinition.getHR_List_ID())
						.setClient_ID()
						.setOrderBy(I_HR_ListVersion.COLUMNNAME_ValidFrom)
						.list();
				//	Export
				for(X_HR_ListVersion listVersion : listVersionList) {
					if(listVersion.get_ID() < PackOut.MAX_OFFICIAL_ID) {
						continue;
					}
					//	Remove default
					cleanOfficialReference(listVersion);
					packOut.createGenericPO(document, listVersion, true, parentsToExclude);
					//	List Line
					List<X_HR_ListLine> listLineList = new Query(ctx, I_HR_ListLine.Table_Name, I_HR_ListLine.COLUMNNAME_HR_ListVersion_ID + " = ?", null)
							.setOnlyActiveRecords(true)
							.setParameters(listVersion.getHR_ListVersion_ID())
							.setClient_ID()
							.setOrderBy(I_HR_ListLine.COLUMNNAME_MinValue)
							.list();
					//	Export
					for(X_HR_ListLine listLine : listLineList) {
						if(listLine.get_ID() < PackOut.MAX_OFFICIAL_ID) {
							continue;
						}
						//	Remove default
						cleanOfficialReference(listLine);
						packOut.createGenericPO(document, listLine, true, parentsToExclude);
					}
				}
			}
		}
		//	Concept Group
		List<MHRConceptType> conceptTypeList = new Query(ctx, I_HR_Concept_Type.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Concept_Type.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRConceptType conceptType : conceptTypeList) {
			if(conceptType.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default
			cleanOfficialReference(conceptType);
			packOut.createGenericPO(document, conceptType, true, parentsToExclude);
		}
		//	Export Concepts
		List<MHRConcept> conceptList = new Query(ctx, I_HR_Concept.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_Concept.COLUMNNAME_Value)
				.list();
		//	Export
		for(MHRConcept concept : conceptList) {
			if(concept.get_ID() < PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	Remove default bank account
			cleanOfficialReference(concept);
			packOut.createGenericPO(document, concept, true, parentsToExclude);
			//	Only No employee
			if(!concept.isEmployee()) {
				//	Export attribute
				List<MHRAttribute> attributeList = new Query(ctx, I_HR_Attribute.Table_Name, I_HR_Attribute.COLUMNNAME_HR_Concept_ID + " = ?", null)
						.setParameters(concept.getHR_Concept_ID())
						.setOnlyActiveRecords(true)
						.setClient_ID()
						.setOrderBy(I_HR_Attribute.COLUMNNAME_ValidFrom)
						.list();
				//	Export
				for(MHRAttribute attribute : attributeList) {
					if(attribute.get_ID() < PackOut.MAX_OFFICIAL_ID) {
						continue;
					}
					//	Remove default bank account
					cleanOfficialReference(attribute);
					attribute.set_ValueOfColumn(MHRAttribute.COLUMNNAME_HR_Employee_ID, null);
					attribute.set_ValueOfColumn(MHRAttribute.COLUMNNAME_AD_OrgTrx_ID, null);
					packOut.createGenericPO(document, attribute, true, parentsToExclude);
				}
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
			if(payroll.get_ID() < PackOut.MAX_OFFICIAL_ID) {
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
				if(payrollConcept.get_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default values
				cleanOfficialReference(payrollConcept);
				packOut.createGenericPO(document, payrollConcept, true, parentsToExclude);
			}
		}
	}
	
	/**
	 * Create Payroll Report
	 * @param ctx
	 * @param document
	 * @param packOut
	 * @throws SAXException
	 */
	public void createPayrollReport(Properties ctx, TransformerHandler document, PackOut packOut) throws SAXException {
		//	Export Payroll Definition
		List<MHRProcessReport> reportList = new Query(ctx, I_HR_ProcessReport.Table_Name, null, null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setOrderBy(I_HR_ProcessReport.COLUMNNAME_Name)
				.list();
		for(MHRProcessReport report : reportList) {
			if(report.get_ID() < PackOut.MAX_OFFICIAL_ID) {
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
				if(processReportLine.get_ID() < PackOut.MAX_OFFICIAL_ID) {
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
					if(processReportSource.get_ID() < PackOut.MAX_OFFICIAL_ID) {
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
				if(processReportPayroll.get_ID() < PackOut.MAX_OFFICIAL_ID) {
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
				if(processReportTemplate.get_ID() < PackOut.MAX_OFFICIAL_ID) {
					continue;
				}
				//	Remove default values
				cleanOfficialReference(processReportTemplate);
				packOut.createGenericPO(document, processReportTemplate, true, parentsToExclude);
			}
		}
	}
}
