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

import java.util.List;

import org.compiere.model.I_C_RevenueRecognition_Plan;
import org.compiere.model.MRevenueRecognitionPlan;
import org.compiere.model.PO;
import org.compiere.model.Query;

/** Generated Process for (Revenue Recognition Run)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public class RevenueRecognitionRun extends RevenueRecognitionRunAbstract {

	@Override
	protected String doIt() throws Exception {
		List<PO> progressLog = new Query(getCtx(), "C_ProjectProgressLog", null, get_TrxName())
				.setParameters(1)
				.list();
		new Query(getCtx(), I_C_RevenueRecognition_Plan.Table_Name, "C_RevenueRecognition_ID = ?", get_TrxName())
			.setParameters(getRevenueRecognitionId())
			.<MRevenueRecognitionPlan>list()
			.forEach(revenuePlan -> {
				
			});
		return "";
	}
}