/**
 * Copyright (C) 2003-2018, e-Evolution Consultants S.A. , http://www.e-evolution.com
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * Email: victor.perez@e-evolution.com, http://www.e-evolution.com , http://github.com/e-Evolution
 * Created by victor.perez@e-evolution.com , www.e-evolution.com
 */

package org.eevolution.model;

import org.compiere.model.MClient;
import org.compiere.model.MConversionRate;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.MProject;
import org.compiere.model.MProjectLine;
import org.compiere.model.MProjectPhase;
import org.compiere.model.MProjectTask;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.Env;

import java.math.BigDecimal;
import java.util.List;

/**
 * Project Model Validator
 * 
 * @author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 *   	<a href="https://github.com/adempiere/adempiere/issues/1960">
 *		@see FR [ 1960 ]  Add Support to remove Project Lines referenced on Project Phase / Task </a>
 *   	<a href="https://github.com/adempiere/adempiere/issues/2112">
 *		@see FR [ 2112 ]  Add Support to cumulated Planned Amt on Project / Phases / Tasks</a>
 */
public class ProjectModelValidator implements ModelValidator {

    public void initialize(ModelValidationEngine engine, MClient client) {
        //Check when a payment is completed
        engine.addDocValidate(MPayment.Table_Name, this);
        //Set Account dimension based on project if this not exist
        engine.addModelChange(MOrder.Table_Name, this);
        engine.addModelChange(MOrderLine.Table_Name, this);
        engine.addModelChange(MInvoice.Table_Name, this);
        engine.addModelChange(MInvoiceLine.Table_Name, this);
        engine.addModelChange(MPayment.Table_Name, this);
        
        engine.addModelChange(MProjectPhase.Table_Name, this);
        engine.addModelChange(MProjectTask.Table_Name, this);
    }

    @Override
    public int getAD_Client_ID() {
        return Env.getAD_Client_ID(Env.getCtx());
    }

    @Override
    public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
        return "";
    }

    /**
     * @param entity
     * @param type   TYPE_
     * @return
     * @throws Exception
     */
    public String modelChange(PO entity, int type) {

        if (ModelValidator.TYPE_BEFORE_CHANGE == type) {
            Integer projectId = entity.get_ValueAsInt(MProject.COLUMNNAME_C_Project_ID);
            if (projectId > 0) {
                MProject project = new MProject(entity.getCtx(), projectId, entity.get_TrxName());
                if (entity.get_ColumnIndex(MProject.COLUMNNAME_C_Campaign_ID) > 0
                        && entity.get_ValueAsInt(MProject.COLUMNNAME_C_Campaign_ID) <= 0
                        && project.getC_Campaign_ID() > 0)
                    entity.set_ValueOfColumn(MProject.COLUMNNAME_C_Campaign_ID, project.getC_Campaign_ID());

                if (entity.get_ColumnIndex(MProject.COLUMNNAME_C_Activity_ID) > 0
                        && entity.get_ValueAsInt(MProject.COLUMNNAME_C_Activity_ID) <= 0
                        && project.getC_Activity_ID() > 0)
                    entity.set_ValueOfColumn(MProject.COLUMNNAME_C_Activity_ID, project.getC_Activity_ID());

                if (entity.get_ColumnIndex(MProject.COLUMNNAME_User1_ID) > 0
                        && entity.get_ValueAsInt(MProject.COLUMNNAME_User1_ID) <= 0
                        && project.getUser1_ID() > 0)
                    entity.set_ValueOfColumn(MProject.COLUMNNAME_User1_ID, project.getUser1_ID());

                if (entity.get_ColumnIndex(MProject.COLUMNNAME_User2_ID) > 0
                        && entity.get_ValueAsInt(MProject.COLUMNNAME_User2_ID) <= 0
                        && project.getUser2_ID() > 0)
                    entity.set_ValueOfColumn(MProject.COLUMNNAME_User2_ID, project.getUser2_ID());

                if (entity.get_ColumnIndex(MProject.COLUMNNAME_User3_ID) > 0
                        && entity.get_ValueAsInt(MProject.COLUMNNAME_User3_ID) <= 0
                        && project.getUser3_ID() > 0)
                    entity.set_ValueOfColumn(MProject.COLUMNNAME_User3_ID, project.getUser3_ID());

                if (entity.get_ColumnIndex(MProject.COLUMNNAME_User4_ID) > 0
                        && entity.get_ValueAsInt(MProject.COLUMNNAME_User4_ID) <= 0
                        && project.getUser4_ID() > 0)
                    entity.set_ValueOfColumn(MProject.COLUMNNAME_User4_ID, project.getUser4_ID());
            }
            //FR [ 2112 ]
            if (entity.get_Table_ID() == MProjectPhase.Table_ID
            		&& entity.is_ValueChanged(MProjectPhase.COLUMNNAME_PlannedAmt)) {
				
            	MProjectPhase pPhase = (MProjectPhase) entity;
				if (pPhase.getC_ProjectPhase_ID()!=0) {
					BigDecimal oldAmt = Env.ZERO;
					BigDecimal diffAmt =Env.ZERO; 
					if (pPhase.get_ValueOld(MProjectPhase.COLUMNNAME_PlannedAmt)!=null
							&& pPhase.getLines().size()==0) {
						oldAmt = (BigDecimal)pPhase.get_ValueOld(MProjectPhase.COLUMNNAME_PlannedAmt);
						diffAmt = pPhase.getPlannedAmt().subtract(oldAmt);
					}
					MProject project = (MProject) pPhase.getC_Project();
					if (project.getC_Project_ID()!=0 && !diffAmt.equals(Env.ZERO)) {
						project.setPlannedAmt(project.getPlannedAmt().add(diffAmt));
						project.saveEx();
					}
					
				}
			}else if (entity.get_Table_ID() == MProjectTask.Table_ID
            		&& entity.is_ValueChanged(MProjectTask.COLUMNNAME_PlannedAmt)) {
				
				MProjectTask pTask = (MProjectTask) entity;
				if (pTask.getC_ProjectTask_ID()!=0) {
					BigDecimal oldAmt =Env.ZERO;
					BigDecimal diffAmt =Env.ZERO;
					if (pTask.get_ValueOld(MProjectPhase.COLUMNNAME_PlannedAmt)!=null
							&& pTask.getLines().length==0) {
						oldAmt = (BigDecimal)pTask.get_ValueOld(MProjectPhase.COLUMNNAME_PlannedAmt);
						diffAmt = pTask.getPlannedAmt().subtract(oldAmt);
					}
					MProjectPhase pPhase = (MProjectPhase) pTask.getC_ProjectPhase();
					if (pPhase.getC_ProjectPhase_ID()!=0 && !diffAmt.equals(Env.ZERO)) {
						pPhase.setPlannedAmt(pPhase.getPlannedAmt().add(diffAmt));
						pPhase.saveEx();
					}
					
				}
			}
            
        }
        //FR [ 2112 ]
        if (ModelValidator.TYPE_AFTER_NEW == type) {
			//Delete Project Lines when Delete Phase
        	if (entity.get_Table_ID() == MProjectPhase.Table_ID) {
				MProjectPhase pPhase = (MProjectPhase) entity;
				if (pPhase.getC_ProjectPhase_ID()!=0) {
					MProject project = (MProject) pPhase.getC_Project();
					if (project.getC_Project_ID()!=0) {
						project.setPlannedAmt(project.getPlannedAmt().add(pPhase.getPlannedAmt()));
						project.saveEx();
					}
				}
			}//Delete Project Lines when Delete Task
        	else if (entity.get_Table_ID() == MProjectTask.Table_ID) {
				MProjectTask pTask = (MProjectTask) entity;
				if (pTask.getC_ProjectTask_ID()!=0) {
					MProjectPhase pPhase = (MProjectPhase) pTask.getC_ProjectPhase();
					if (pPhase.getC_Project_ID()!=0) {
						pPhase.setPlannedAmt(pPhase.getPlannedAmt().add(pTask.getPlannedAmt()));
						pPhase.saveEx();
					}
				}
			}
		}
        
        //FR [ 1960 ]
        if (ModelValidator.TYPE_BEFORE_DELETE == type) {
			//Delete Project Lines when Delete Phase
        	if (entity.get_Table_ID() == MProjectPhase.Table_ID) {
				MProjectPhase pPhase = (MProjectPhase) entity;
				if (pPhase.getC_ProjectPhase_ID()!=0) {
					List<MProjectLine> pLines = pPhase.getLines();
					if (pLines.size()>0) {
						for (MProjectLine mProjectLine : pLines) 
							mProjectLine.delete(true);
					}else {
						MProject project = (MProject) pPhase.getC_Project();
						if (project.getC_Project_ID()!=0) {
							project.setPlannedAmt(project.getPlannedAmt().subtract(pPhase.getPlannedAmt()));
							project.saveEx();
						}
					}
				}
			}//Delete Project Lines when Delete Task
        	else if (entity.get_Table_ID() == MProjectTask.Table_ID) {
				MProjectTask pTask = (MProjectTask) entity;
				if (pTask.getC_ProjectTask_ID()!=0) {
					MProjectLine[] pLines = pTask.getLines();
					if (pLines.length>0) {
						for (MProjectLine mProjectLine : pLines) 
							mProjectLine.delete(true);
					}else {
						MProjectPhase pPhase = (MProjectPhase) pTask.getC_ProjectPhase();
						if (pPhase.getC_ProjectPhase_ID()!=0) {
							pPhase.setPlannedAmt(pPhase.getPlannedAmt().subtract(pTask.getPlannedAmt()));
							pPhase.saveEx();
						}
					}
				}
			}
		}

        return null;
    }

    /**
     * @param entity
     * @param timing see TIMING_ constants
     * @return
     */
    public String docValidate(PO entity, int timing) {
        //Update Project Balance When Payment is receipt increase project balance when payment is not receipt decrease project balance
        if (MPayment.Table_ID == entity.get_Table_ID() && entity.get_ValueAsInt(MProject.COLUMNNAME_C_Project_ID) > 0) {
            MPayment payment = (MPayment) entity;
            MProject project = (MProject) payment.getC_Project();
            BigDecimal paymentAmount = payment.getPayAmt();
            if (payment.getC_Currency_ID() != project.getC_Currency_ID()) {
                paymentAmount = MConversionRate.convert(
                        entity.getCtx(),
                        paymentAmount,
                        project.getC_Currency_ID(),
                        payment.getC_Currency_ID(),
                        payment.getDateAcct(),
                        payment.getC_ConversionType_ID(),
                        getAD_Client_ID(),
                        payment.getAD_Org_ID());
            }
            // get payment with project link
            if (ModelValidator.TIMING_AFTER_COMPLETE == timing && payment.getReversal_ID() <= 0) {
                if (payment.isReceipt())
                    project.setProjectBalanceAmt(project.getProjectBalanceAmt().add(paymentAmount));
                else
                    project.setProjectBalanceAmt(project.getProjectBalanceAmt().subtract(paymentAmount));

                project.saveEx();
            }
            if (payment.getReversal_ID() > 0 && (ModelValidator.TIMING_AFTER_REVERSECORRECT == timing || ModelValidator.TIMING_AFTER_REVERSEACCRUAL == timing)) {
                if (payment.isReceipt())
                    project.setProjectBalanceAmt(project.getProjectBalanceAmt().subtract(paymentAmount));
                else
                    project.setProjectBalanceAmt(project.getProjectBalanceAmt().add(paymentAmount));

                project.saveEx();
            }
        }
        return null;
    }
}