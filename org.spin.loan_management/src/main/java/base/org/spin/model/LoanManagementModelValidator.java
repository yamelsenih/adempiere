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

import java.util.List;

import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

/**
 * Loan Management Model Validator
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1583 ] New Definition for loan
 *		@see https://github.com/adempiere/adempiere/issues/1583
 */
public class LoanManagementModelValidator implements ModelValidator {
    private static CLogger log = CLogger.getCLogger(LoanManagementModelValidator.class);
    @Override
    public void initialize(ModelValidationEngine engine, MClient client) {
		//
		engine.addModelChange(MFMAgreement.Table_Name, this);
    }

    @Override
    public int getAD_Client_ID() {
        return Env.getAD_Client_ID(Env.getCtx());
    }

    @Override
    public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
        return null;
    }

    @Override
    public String modelChange(PO po, int type) throws Exception {
    	if (po.get_TableName().equals(I_FM_Agreement.Table_Name)) {
    		if(type == TYPE_AFTER_NEW || type == TYPE_AFTER_CHANGE) {
    			log.fine("FM_Agreement = type == TYPE_AFTER_NEW || type == TYPE_AFTER_CHANGE");
    			MFMAgreement agreement = (MFMAgreement) po;
    			if(agreement.isProcessed()) {
    				return null;
    			}
    			//	get all account
    			List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
    			for(MFMAccount account : accounts) {
    				account.deleteEx(true);
    			}
    			MFMAccount account = new MFMAccount(agreement);
    			account.saveEx();
    			//	Create Product
    			if(agreement.getFM_Product_ID() != 0) {
    				MFMAccountProduct accountProduct = new MFMAccountProduct(account);
    				accountProduct.setFM_Product_ID(agreement.getFM_Product_ID());
    				accountProduct.setValidFrom(agreement.getDateDoc());
    				accountProduct.saveEx();
    			}
    		} else if(type == TYPE_AFTER_DELETE) {
    			log.fine("FM_Agreement = type == TYPE_AFTER_DELETE");
    			MFMAgreement agreement = (MFMAgreement) po;
    			if(agreement.isProcessed()) {
    				return null;
    			}
    			//	get all account
    			List<MFMAccount> accounts = MFMAccount.getAccountFromAgreement(agreement);
    			for(MFMAccount account : accounts) {
    				account.deleteEx(true);
    			}
    		}
    	}
        return null;
    }

    /**
     * Document Validate for Standard Request Type
     * @param entity
     * @param timing see TIMING_ constants
     * @return
     */
    public String docValidate(PO entity, int timing) {
        return null;
    }
}
