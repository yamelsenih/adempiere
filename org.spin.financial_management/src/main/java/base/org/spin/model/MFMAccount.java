/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2016 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/

package org.spin.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MClient;
import org.compiere.model.Query;
/**
 * Financial Management
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1583 ] New Definition for loan
 *		@see https://github.com/adempiere/adempiere/issues/1583
 */
public class MFMAccount extends X_FM_Account {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6954882533416850938L;

	public MFMAccount(Properties ctx, int FM_Account_ID, String trxName) {
		super(ctx, FM_Account_ID, trxName);
	}

	public MFMAccount(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public MFMAccount(MFMAgreement agreement) {
		super(agreement.getCtx(), 0, agreement.get_TrxName());
		//	Set default values
		setFM_Agreement_ID(agreement.getFM_Agreement_ID());
		setAccountNo("#" + agreement.getDocumentNo());
		int currencyId = MClient.get(agreement.getCtx()).getC_Currency_ID();
		if(agreement.getFM_Product_ID() != 0) {
			MFMProduct financialProduct = MFMProduct.getById(getCtx(), agreement.getFM_Product_ID());
			if(financialProduct.get_ValueAsInt("C_Currency_ID") != 0) {
				currencyId = financialProduct.get_ValueAsInt("C_Currency_ID");
			}
		}
		//	Set currency
		setC_Currency_ID(currencyId);
	}
	
	/**
	 * Get Account from Agreement
	 * @param agreement
	 * @return
	 */
	public static List<MFMAccount> getAccountFromAgreement(MFMAgreement agreement) {
		return new Query(agreement.getCtx(), Table_Name, "FM_Agreement_ID = ?", agreement.get_TrxName())
				.setClient_ID()
				.setParameters(agreement.getFM_Agreement_ID())
				.setOnlyActiveRecords(true)
				.<MFMAccount>list();
	}

}
