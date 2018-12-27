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

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Commission;
import org.compiere.model.I_C_CommissionType;
import org.compiere.model.MCommission;
import org.compiere.model.MCommissionRun;
import org.compiere.model.MDocType;
import org.compiere.model.Query;
import org.compiere.util.Msg;

import com.eevolution.model.X_S_Contract;

/** Generated Process for (Create Commission from Contract)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.1
 */
public class CreateCommissionFromContract extends CreateCommissionFromContractAbstract {

	/**	Result	*/
	private StringBuffer result = new StringBuffer();
	/**	Counter	*/
	private int created = 0;
	
	@Override
	protected String doIt() throws Exception {
		int contractId = getRecord_ID();
		if(contractId <= 0) {
			throw new AdempiereException("@S_Contract_ID@ @NotFound@");
		}
		X_S_Contract contract = new X_S_Contract(getCtx(), contractId, get_TrxName());
		//	Generate
		new Query(getCtx(), I_C_Commission.Table_Name, I_C_CommissionType.COLUMNNAME_C_CommissionType_ID + " = ? ", get_TrxName())
			.setParameters(getCommissionTypeId())
			.<MCommission>list().forEach(commissionDefinition -> {
				if(getDocTypeId() <= 0) {
					setDocTypeId(MDocType.getDocType(MDocType.DOCBASETYPE_SalesCommission, contract.getAD_Org_ID()));
				}
				MCommissionRun commissionRun = new MCommissionRun(commissionDefinition);
				commissionRun.setDateDoc(getDateDoc());
				commissionRun.setC_DocType_ID(getDocTypeId());
				commissionRun.setDescription(Msg.parseTranslation(getCtx(), "@Generate@: @S_Contract_ID@ - " + contract.getDocumentNo()));
				commissionRun.set_ValueOfColumn("S_Contract_ID", contract.getS_Contract_ID());
				commissionRun.saveEx();
				//	Process commission
				commissionRun.setDocStatus(MCommissionRun.DOCSTATUS_Drafted);
				//	Complete
				if(commissionRun.processIt(MCommissionRun.DOCACTION_Complete)) {
					addDocumentResult(commissionRun.getDocumentNo());
				}
		});
		return getDocumentResult();
	}
	
	/**
	 * Add document to result
	 * @param documentNo
	 */
	private void addDocumentResult(String documentNo) {
		created++;
		//	Add message
		if(result.length() > 0) {
			result.append(", ");
		}
		result.append(documentNo);
	}
	
	/**
	 * Get Document Result
	 * @return
	 */
	private String getDocumentResult() {
		//	Add message
		String returnMessage = "@Created@: " + created;
		if(result.length() > 0) {
			returnMessage = "@Created@: " + created + " [" + result + "]";
		}
		return returnMessage;
	}
}