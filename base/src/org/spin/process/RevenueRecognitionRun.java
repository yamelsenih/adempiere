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
import java.math.MathContext;
import java.util.concurrent.atomic.AtomicLong;

import org.compiere.model.I_C_RevenueRecognition_Plan;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRevenueRecognition;
import org.compiere.model.MRevenueRecognitionPlan;
import org.compiere.model.MRevenueRecognitionRun;
import org.compiere.model.Query;
import org.compiere.util.Env;

/** Generated Process for (Revenue Recognition Run)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public class RevenueRecognitionRun extends RevenueRecognitionRunAbstract {

	@Override
	protected String doIt() throws Exception {
		MRevenueRecognition revenueRecognition = new MRevenueRecognition(getCtx(), getRevenueRecognitionId(), get_TrxName());
		AtomicLong created = new AtomicLong();
		if(revenueRecognition.get_ValueAsBoolean("IsProgressBased")) {
			new Query(getCtx(), I_C_RevenueRecognition_Plan.Table_Name, "C_RevenueRecognition_ID = ?", get_TrxName())
			.setParameters(getRevenueRecognitionId())
			.<MRevenueRecognitionPlan>list()
			.stream()
			.filter(revenuePlan -> revenuePlan.get_ValueAsInt("C_OrderLine_ID") != 0)
			.forEach(revenuePlan -> {
				MOrderLine orderLine = new MOrderLine(getCtx(), revenuePlan.get_ValueAsInt("C_OrderLine_ID"), get_TrxName());
				MOrder order = orderLine.getParent();
				if(order.getC_Project_ID() != 0) {
					new Query(getCtx(), "C_ProjectProgressLog", "C_Project_ID = ?", get_TrxName())
					.setParameters(order.getC_Project_ID())
					.list()
					.forEach(projectProgressLog -> {
						MRevenueRecognitionRun revenueRun = new MRevenueRecognitionRun(getCtx(), 0, get_TrxName());
						revenueRun.setC_RevenueRecognition_Plan_ID(revenuePlan.getC_RevenueRecognition_Plan_ID());
						//	Set progress
						BigDecimal recognizedAmount = revenuePlan.getTotalAmt();
						BigDecimal progressAmount = (BigDecimal) projectProgressLog.get_Value("ProgressPercentWO");
						if(recognizedAmount != null
								&& progressAmount != null) {
							recognizedAmount = recognizedAmount.multiply(progressAmount);
							recognizedAmount = recognizedAmount.divide(Env.ONEHUNDRED, MathContext.DECIMAL128);
							revenueRun.setRecognizedAmt(recognizedAmount);
							revenueRun.set_ValueOfColumn("C_ProjectProgressLog_ID", projectProgressLog.get_ID());
							revenueRun.set_ValueOfColumn("DateDoc", getDateDoc());
							revenueRun.saveEx();
							//	
							created.addAndGet(1);
						}
					});
					revenuePlan.updateRecognizedAmount();
				}
			});
		}
		return "@Created@: " + created;
	}
}