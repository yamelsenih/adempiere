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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_RevenueRecognition_Run;
import org.compiere.model.I_C_ValidCombination;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MConversionType;
import org.compiere.model.MCurrency;
import org.compiere.model.MDocType;
import org.compiere.model.MGLCategory;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalBatch;
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRevenueRecognition;
import org.compiere.model.MRevenueRecognitionPlan;
import org.compiere.model.MRevenueRecognitionRun;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/** Generated Process for (Generate GL Journal from Revenue Recognition)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public class RevenueRecognitionGLJournal extends RevenueRecognitionGLJournalAbstract {

	@Override
	protected String doIt() throws Exception {
		Map<String, List<MRevenueRecognitionRun>> mapForJournal = new HashMap<>();
		MRevenueRecognition revenueRecognition = new MRevenueRecognition(getCtx(), getRevenueRecognitionId(), get_TrxName());
		if(revenueRecognition.get_ValueAsBoolean("IsProgressBased")) {
			new Query(getCtx(), I_C_RevenueRecognition_Run.Table_Name, "EXISTS(SELECT 1 FROM C_RevenueRecognition_Plan p "
					+ "WHERE p.C_RevenueRecognition_ID = ? "
					+ "AND p.C_RevenueRecognition_Plan_ID = C_RevenueRecognition_Run.C_RevenueRecognition_Plan_ID "
					+ "AND p.C_OrderLine_ID IS NOT NULL) "
					+ "AND GL_JournalLine_ID IS NULL", get_TrxName())
			.setParameters(getRevenueRecognitionId())
			.<MRevenueRecognitionRun>list()
			.forEach(revenueRun -> {
				MRevenueRecognitionPlan revenuePlan = (MRevenueRecognitionPlan) revenueRun.getC_RevenueRecognition_Plan();
				MOrderLine orderLine = new MOrderLine(getCtx(), revenuePlan.get_ValueAsInt("C_OrderLine_ID"), get_TrxName());
				String key = revenuePlan.getUnEarnedRevenue_Acct() 
						+ "|" + revenuePlan.getP_Revenue_Acct() 
						+ "|" + orderLine.getC_Project_ID() 
						+ "|" + orderLine.getC_Activity_ID() 
						+ "|" + orderLine.getC_Campaign_ID()
						+ "|" + orderLine.getUser1_ID()
						+ "|" + orderLine.getUser2_ID()
						+ "|" + orderLine.getUser3_ID()
						+ "|" + orderLine.getUser4_ID()
						+ "|" + orderLine.getC_BPartner_ID();
				List<MRevenueRecognitionRun> revenueRunList = mapForJournal.get(key);
				if(revenueRunList == null) {
					revenueRunList = new ArrayList<MRevenueRecognitionRun>();
				}
				revenueRunList.add(revenueRun);
				mapForJournal.put(key, revenueRunList);
			});
		}
		//	Create Lines
		return "@Created@: " + createJournalLines(mapForJournal);
	}
	
	/**
	 * Create Journal
	 * @param mapForJournal
	 */
	private int createJournalLines(Map<String, List<MRevenueRecognitionRun>> mapForJournal) {
		MAcctSchema accountingSchema = Arrays.asList(MAcctSchema.getClientAcctSchema(getCtx(), getAD_Client_ID(), get_TrxName())).stream().findFirst().get();
        MCurrency currency = MCurrency.get(Env.getCtx(), accountingSchema.getC_Currency_ID());
        int conversionTypeId = MConversionType.getDefault(getAD_Client_ID());
        MJournalBatch journalBatch = createJournalBatch(accountingSchema);
		mapForJournal.entrySet().stream().forEach(entry -> {
			List<MRevenueRecognitionRun> runList = entry.getValue();
			MRevenueRecognitionPlan revenuePlan = (MRevenueRecognitionPlan) runList.stream().findFirst().get().getC_RevenueRecognition_Plan();
			MOrderLine source = new MOrderLine(getCtx(), revenuePlan.get_ValueAsInt("C_OrderLine_ID"), get_TrxName());
	        I_C_ValidCombination unEarnedRevenueCombination = revenuePlan.getUnEarnedRevenue_A();
	        I_C_ValidCombination productRevenueCombination = revenuePlan.getP_Revenue_A();
	        MJournal journal = createJournal(journalBatch, accountingSchema, currency, conversionTypeId);
			AtomicInteger lineNo = new AtomicInteger(10);
	        //	Get Recognized Amount
	        AtomicReference<BigDecimal> recognizedAmount = new AtomicReference<BigDecimal>(Env.ZERO);
	        runList.stream().forEach(revenueRun -> recognizedAmount.updateAndGet(amount -> amount.add(revenueRun.getRecognizedAmt())));
	        //	Line
	        createJournalLine(journal, unEarnedRevenueCombination, currency, source, recognizedAmount.get(), lineNo);
	        //	Reverse
	        createJournalLine(journal, productRevenueCombination, currency, source, recognizedAmount.get().negate(), lineNo);
	        //	Set reference
	        runList.forEach(revenueRun -> {
	        	revenueRun.setGL_Journal_ID(journal.getGL_Journal_ID());
	        	revenueRun.saveEx();
	        });
		});
		return mapForJournal.isEmpty()? 0: 1;
	}
	
    /**
     * Create Journal Batch
     *
     * @param accountingSchema
     * @param trxName
     * @return
     */
    private MJournalBatch createJournalBatch(MAcctSchema accountingSchema) {
    	MJournalBatch journalBatch = new MJournalBatch(getCtx(), 0, get_TrxName());
        StringBuilder journalBatchDescription = new StringBuilder();
        Optional.ofNullable(journalBatch.getDescription()).ifPresent(batchDescription -> journalBatchDescription.append(batchDescription).append(" "));
        journalBatchDescription.append(getName()).append(" @DateAcct@ ").append(getDateAcct());
        journalBatch.setDateAcct(getDateAcct());
        journalBatch.setDateDoc(getDateAcct());
        journalBatch.setDescription(Msg.parseTranslation(getCtx(), journalBatchDescription.toString()));
        journalBatch.setC_DocType_ID(getDocTypeTargetId());
        journalBatch.setDateDoc(getDateAcct());
        journalBatch.setDateAcct(getDateAcct());
        journalBatch.setC_Currency_ID(accountingSchema.getC_Currency_ID());
        journalBatch.saveEx();
        return journalBatch;
    }

    /**
     * Create Journal
     * @return
     */
    private MJournal createJournal(MJournalBatch journalBatch, MAcctSchema accountingSchema, MCurrency currency, int conversionTypeId) {
        I_C_DocType documentType = MDocType.get(getCtx(), getDocTypeTargetId());
        Integer glCategoryId = Optional.ofNullable(MGLCategory.getDefaultSystem(getCtx()).get_ID())
                .orElseGet(() -> documentType.getGL_Category_ID());
        MJournal journal = new MJournal(journalBatch);
        journal.setC_DocType_ID(documentType.getC_DocType_ID());
        journal.setDateAcct(getDateAcct());
        journal.setDateDoc(getDateAcct());
        journal.setC_AcctSchema_ID(accountingSchema.get_ID());
        journal.setAD_Org_ID(Env.getAD_Org_ID(getCtx()));
        journal.setC_Currency_ID(accountingSchema.getC_Currency_ID());
        journal.setC_ConversionType_ID(conversionTypeId);
        journal.setGL_Category_ID(glCategoryId);
        journal.setPostingType(MJournal.POSTINGTYPE_Actual);
        StringBuilder journalDescription = new StringBuilder();
        journalDescription.append("@C_AcctSchema_ID@ ").append(accountingSchema.getName()).append(" @C_Currency_ID@ ")
                .append(currency.getISO_Code());
        journal.setDescription(Msg.parseTranslation(Env.getCtx(), journalDescription.toString()));
        journal.saveEx();
        return journal;
    }

    /**
     * Create Journal Line
     * @param journal
     * @param combination
     * @param currency
     * @param revenueRun
     * @param recognizedAmount
     * @param lineNo
     */
    private MJournalLine createJournalLine(MJournal journal, I_C_ValidCombination combination, MCurrency currency, MOrderLine source, BigDecimal recognizedAmount, AtomicInteger lineNo) {
        MJournalLine journalLine = new MJournalLine(journal);
        journalLine.setLine(lineNo.getAndUpdate(no -> no + 10));
        journalLine.setAccount_ID(combination.getAccount_ID());
        StringBuilder journalDescriptionLine = new StringBuilder();
        journalDescriptionLine.append("@Account_ID@ ").append(combination.getDescription())
                .append(" @C_Currency_ID@ ").append(currency.getISO_Code());
        if (recognizedAmount.compareTo(Env.ZERO) > 0) {
            journalLine.setAmtSourceDr(recognizedAmount.abs());
            journalLine.setAmtAcctDr(recognizedAmount.abs());
            journalLine.setAmtSourceCr(BigDecimal.ZERO);
            journalLine.setAmtAcctCr(BigDecimal.ZERO);
            journalDescriptionLine.append(" @RecognizedAmt@ ").append(recognizedAmount.abs().toString());
        } else {
            journalLine.setAmtSourceDr(BigDecimal.ZERO);
            journalLine.setAmtAcctDr(BigDecimal.ZERO);
            journalLine.setAmtSourceCr(recognizedAmount.abs());
            journalLine.setAmtAcctCr(recognizedAmount.abs());
            journalDescriptionLine.append(" @RecognizedAmt@ ").append(recognizedAmount.abs().toString());
        }
        journalLine.setDescription(Msg.parseTranslation(getCtx(), journalDescriptionLine.toString()));
        if(source.getC_Project_ID() != 0) {
        	journalLine.setC_Project_ID(source.getC_Project_ID());
        }
        if(source.getC_Activity_ID() != 0) {
        	journalLine.setC_Activity_ID(source.getC_Activity_ID());
        }
        if(source.getC_Campaign_ID() != 0) {
        	journalLine.setC_Campaign_ID(source.getC_Campaign_ID());
        }
        if(source.getUser1_ID() != 0) {
        	journalLine.setUser1_ID(source.getUser1_ID());
        }
        if(source.getUser2_ID() != 0) {
        	journalLine.setUser2_ID(source.getUser2_ID());
        }
        if(source.getUser3_ID() != 0) {
        	journalLine.setUser3_ID(source.getUser3_ID());
        }
        if(source.getUser4_ID() != 0) {
        	journalLine.setUser4_ID(source.getUser4_ID());
        }
        if(source.getC_BPartner_ID() != 0) {
        	journalLine.setC_BPartner_ID(source.getC_BPartner_ID());
        }
        journalLine.saveEx();
        return journalLine;
    }
}