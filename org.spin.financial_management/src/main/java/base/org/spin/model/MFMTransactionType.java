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
import java.util.Properties;

import org.compiere.model.Query;
import org.compiere.util.DB;
/**
 * Financial Management
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1583 ] New Definition for loan
 *		@see https://github.com/adempiere/adempiere/issues/1583
 */
public class MFMTransactionType extends X_FM_TransactionType {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3948984837847121881L;

	public MFMTransactionType(Properties ctx, int FM_TransactionType_ID, String trxName) {
		super(ctx, FM_TransactionType_ID, trxName);
	}

	public MFMTransactionType(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

    /**
     * Get Account configuration
     * @param acctSchemaId
     * @return
     */
    public X_FM_TransactionType_Acct getTransactionTypeAcct(int acctSchemaId) {
    	return new Query(getCtx(), I_FM_TransactionType.Table_Name, 
    			"FM_TransactionType_ID = ? AND C_AcctSchema_ID = ?", get_TrxName())
    		.setParameters(getFM_TransactionType_ID(), acctSchemaId)
    		.first();
    }
    
    public int getTransactionTypeAccountCR() {
        String sql = " FM_Expense_Acct FROM FM_TransactionType c " +
                " INNER JOIN FM_TransactionType_Acct ca ON (c.FM_TransactionType_ID=ca.FM_TransactionType_ID)" +
                " WHERE c.FM_TransactionType_ID " + getFM_TransactionType_ID();
        int result = DB.getSQLValue("TransactionTypeCR", sql);
        if (result > 0)
            return result;
        return 0;
    }

    public int getTransactionTypeAccountDR() {
        String sql = " FM_Revenue_Acct FROM FM_TransactionType c " +
                " INNER JOIN FM_TransactionType_Acct ca ON (c.FM_TransactionType_ID=ca.FM_TransactionType_ID)" +
                " WHERE c.FM_TransactionType_ID " + getFM_TransactionType_ID();
        int result = DB.getSQLValue("TransactionTypeCR", sql);
        if (result > 0)
            return result;
        return 0;
    }
    
    /**
     * Return Value + Name
     *
     * @return Value
     */
    public String toString() {
        return getValue() + " - " + getName();
    }   //  toString
}
