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

package org.compiere.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.*;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.wf.MWorkflow;
import org.eevolution.model.MPPProductBOM;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.compiere.model.X_C_ProjectPhase.PROJINVOICERULE_ProductQuantity;

/**
 * Project Generate Production Order
 * Process create a Production Order from Phase , Trask or Project Line
 */
public class ProjectGenerateProductionBatchOrder extends ProjectGenerateProductionBatchOrderAbstract {
	
    @Override
    protected void prepare() {
        super.prepare();
    }

    @Override
    protected String doIt() throws Exception {
        MProject project = new MProject(getCtx(), getProjectId(), get_TrxName());
        if (project.getC_Project_ID() <= 0)
            throw new AdempiereException("@C_Project@ @NotFound@");

        AtomicReference<Optional<Timestamp>> atomicDateOrdered = new AtomicReference<>();
        AtomicInteger atomicBOMId = new AtomicInteger(0);
        AtomicInteger atomicWorkflowId = new AtomicInteger(0);
        AtomicReference<MProjectPhase> atomicProjectPhase = new AtomicReference<>();
        AtomicReference<MProjectTask> atomicProjectTask = new AtomicReference<>();
        AtomicReference<BigDecimal> atomicQuantity = new AtomicReference<>(BigDecimal.ZERO);
        MProduct product = null;
        if (getProjectLineId() > 0) {
            MProjectLine projectLine = new MProjectLine(getCtx(), getProjectLineId(), get_TrxName());
            //production batch
            if (projectLine.getM_ProductionBatch_ID() > 0) {
                MProductionBatch order = (MProductionBatch) projectLine.getM_ProductionBatch();
                throw new AdempiereException(order.getDocumentInfo() + " @AlreadyExists@ @To@ @C_ProjectLine_ID@");
            }

            product = MProduct.get(getCtx(), projectLine.getM_Product_ID());
            validProduct(product);
            atomicBOMId.set(getProductBOMId() > 0
                    ? getProductBOMId() : MPPProductBOM.getDefault(product, get_TrxName()).getPP_Product_BOM_ID());
            atomicWorkflowId.set(getWorkflowId() > 0
                    ? getWorkflowId() : MWorkflow.getWorkflowSearchKey(product));
            atomicQuantity.set(projectLine.getPlannedQty());
            projectLine.getProjectPhase().ifPresent(projectPhaseLine -> {
                Optional<Timestamp> dateOrderedOptional = Optional.ofNullable(
                        Optional.ofNullable(projectPhaseLine.getDateStartSchedule()).orElse(project.getDateStartSchedule()));
                atomicProjectPhase.set((MProjectPhase) projectPhaseLine);
                atomicDateOrdered.set(Optional.ofNullable(dateOrderedOptional.orElse(
                        Optional.ofNullable(projectPhaseLine.getStartDate()).orElse(project.getDateStartSchedule()))));
            });
            projectLine.getProjectTask().ifPresent(projectTaskLine -> {
                MProjectPhase projectPhase = (MProjectPhase) projectTaskLine.getC_ProjectPhase();
                atomicProjectTask.set((MProjectTask) projectTaskLine);
                atomicProjectPhase.set(projectPhase);
                Optional<Timestamp> dateOrderedOptional = Optional.ofNullable(Optional.ofNullable(projectTaskLine.getDateStartSchedule())
                        .orElse(Optional.ofNullable(projectPhase.getDateStartSchedule())
                                .orElse(project.getDateStartSchedule())));
                dateOrderedOptional.ifPresent(dateOrdered -> atomicDateOrdered.set(Optional.ofNullable(dateOrdered)));
            });
        } else if (getProjectTaskId() > 0) {
            MProjectTask projectTask = new MProjectTask(getCtx(), getProjectTaskId(), get_TrxName());
            if (!PROJINVOICERULE_ProductQuantity.equals(projectTask.getProjInvoiceRule())) {
                String errorMessage = "@ProjInvoiceRule@ "
                        + MRefList.getListName(getCtx(), MProjectTask.PROJINVOICERULE_AD_Reference_ID, MProjectTask.PROJINVOICERULE_ProductQuantity);
                throw new AdempiereException(errorMessage);
            }
            product = MProduct.get(getCtx(), projectTask.getM_Product_ID());
            validProduct(product);
            atomicBOMId.set(getProductBOMId() > 0 ? getProductBOMId() : MPPProductBOM.getDefault(product, get_TrxName()).getPP_Product_BOM_ID());
            atomicWorkflowId.set(getWorkflowId() > 0 ? getWorkflowId() : MWorkflow.getWorkflowSearchKey(product));
            atomicQuantity.set(projectTask.getQty());
            MProjectPhase projectPhase = (MProjectPhase) projectTask.getC_ProjectPhase();
            Optional<Timestamp> dateOrderedPhase = Optional.ofNullable(projectPhase.getDateStartSchedule());
            atomicProjectPhase.set(projectPhase);
            atomicProjectTask.set(projectTask);
            Optional<Timestamp> dateOrderedTask = Optional.ofNullable(projectTask.getDateStartSchedule());
            atomicDateOrdered.set(Optional.ofNullable(dateOrderedTask.orElse(dateOrderedPhase
                    .orElse(project.getDateStartSchedule()))));
        } else if (getProjectPhaseId() > 0) {
            MProjectPhase projectPhase = new MProjectPhase(getCtx(), getProjectPhaseId(), get_TrxName());
            if (!PROJINVOICERULE_ProductQuantity.equals(projectPhase.getProjInvoiceRule())) {
                String errorMessage = "@ProjInvoiceRule@ "
                        + MRefList.getListName(getCtx(), MProjectPhase.PROJINVOICERULE_AD_Reference_ID, MProjectPhase.PROJINVOICERULE_ProductQuantity);
                throw new AdempiereException(errorMessage);
            }
            product = MProduct.get(getCtx(), projectPhase.getM_Product_ID());
            validProduct(product);
            atomicBOMId.set(getProductBOMId() > 0
                    ? getProductBOMId() : MPPProductBOM.getDefault(product, get_TrxName()).getPP_Product_BOM_ID());
            atomicWorkflowId.set(getWorkflowId() > 0
                    ? getWorkflowId() : MWorkflow.getWorkflowSearchKey(product));
            atomicQuantity.set(projectPhase.getQty());
            Optional<Timestamp> dateOrderedPhase = Optional.ofNullable(projectPhase.getDateStartSchedule());
            atomicProjectPhase.set(projectPhase);
            atomicDateOrdered.set(Optional.ofNullable(dateOrderedPhase.orElse(
                    Optional.ofNullable(projectPhase.getStartDate()).orElse(project.getDateStartSchedule()))));
        }

        Timestamp dateOrdered = atomicDateOrdered.get()
                .orElseThrow(() -> new AdempiereException("@DateStartSchedule@ @NotFound@"));
        
        MProductionBatch order = createOrderProductionBatch(
                project,
                Optional.ofNullable(atomicProjectPhase.get()),
                Optional.ofNullable(atomicProjectTask.get()),
                Optional.ofNullable(product),
                atomicBOMId.get(),
                dateOrdered,
                atomicQuantity.get());

        addLog(Msg.parseTranslation(getCtx(), "@M_ProductionBatch_ID@ ") + order.getDocumentInfo());

        if (getProjectLineId() <= 0) {
            MProjectLine projectLine = new MProjectLine(project);
            Optional.ofNullable(atomicProjectPhase.get()).ifPresent(projectPhase -> {
            	projectLine.setC_ProjectPhase_ID(projectPhase.getC_ProjectPhase_ID());
            });
            Optional.ofNullable(atomicProjectTask.get()).ifPresent(projectTask -> {
            	projectLine.setC_ProjectPhase_ID(projectTask.getC_ProjectTask_ID());
            });
            projectLine.setM_Product_ID(order.getM_Product_ID());
            projectLine.setPP_Product_BOM_ID(atomicBOMId.get());
            projectLine.setM_ProductionBatch_ID(order.getM_ProductionBatch_ID());;
            projectLine.setPlannedQty(order.getQtyOrdered());
            projectLine.saveEx();
            addLog(Msg.parseTranslation(getCtx(), "@C_ProjectLine_ID@ ") + projectLine.getM_Product().getName());
        }

        

        return "@Ok@";
    }

    private MProductionBatch createOrderProductionBatch(
            MProject project,
            Optional<MProjectPhase> projectPhaseOptional,
            Optional<MProjectTask> projectTaskOptional,
            Optional<MProduct> productOptional,
            Integer bomId,
            Timestamp dateOrdered,
            BigDecimal orderdQuantity) throws Exception {
    	MProductionBatch order = new MProductionBatch(getCtx(), 0, get_TrxName());
    	order.setMovementDate(dateOrdered);
    	order.setQtyOrdered(orderdQuantity);
    	order.setTargetQty(orderdQuantity);
    	order.setC_Project_ID(project.getC_Project_ID());
    	Optional.ofNullable((MWarehouse)project.getM_Warehouse())
        	.ifPresent(warehouse -> order.setM_Locator_ID(MLocator.getDefault(warehouse).getM_Locator_ID()));
    	
    	Optional.ofNullable(MDocType.getDocType(MDocType.DOCBASETYPE_ManufacturingPlannedOrder))
    			.ifPresent(docTypeID -> order.setC_DocType_ID(docTypeID));
	
    	productOptional.ifPresent(product -> order.setM_Product_ID(product.getM_Product_ID()));
    	order.setCountOrder(0);
    	order.setQtyCompleted(Env.ZERO);
    	
    	order.saveEx();
    	
    	projectPhaseOptional.ifPresent(projectPhase -> {
    		projectPhase.setM_ProductionBatch_ID(order.getM_ProductionBatch_ID());
    		projectPhase.saveEx();
        });
    	
    	projectTaskOptional.ifPresent(projectTask -> {
    		projectTask.setM_ProductionBatch_ID(order.getM_ProductionBatch_ID());
    		projectTask.saveEx();
        });
    	
    	order.processIt(MProductionBatch.DOCACTION_Complete);
    	
    	return order;
    }

    private void validProduct(MProduct product) {
        if (product == null)
            throw new AdempiereException("@M_Product_ID@ @NotFound@");
        if (!product.isBOM())
            throw new AdempiereException("@M_Product_ID@ @IsBOM@ @NotFound@");

    }

}