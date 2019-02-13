
/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
 * This program is free software; you can redistribute it and/or modify it    		 *
 * under the terms version 2 or later of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope   		 *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 		 *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           		 *
 * See the GNU General Public License for more details.                       		 *
 * You should have received a copy of the GNU General Public License along    		 *
 * with this program; if not, write to the Free Software Foundation, Inc.,    		 *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     		 *
 * For the text or an alternative of this public license, you may reach us    		 *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com				  		                 *
 *************************************************************************************/



package org.spin.model;



import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MBankStatement;
import org.compiere.model.MBankStatementLine;
import org.compiere.model.MClient;
import org.compiere.model.MConversionRate;
import org.compiere.model.MCurrency;
import org.compiere.model.MPOS;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;



/**
 * Model validator for Retail
 */
public class RetailModelValidator implements ModelValidator
{
	/**	Logger			*/
	private CLogger log = CLogger.getCLogger(getClass());
	/** Client			*/
	private int		clientId = -1;
	
	
	public void initialize (ModelValidationEngine engine, MClient client) {
		if (client != null) {	
			clientId = client.getAD_Client_ID();
		}
		engine.addModelChange(MBankStatementLine.Table_Name, this);
		
	}	//	initialize

	
	public String modelChange (PO po, int type) throws Exception {
		log.info(po.get_TableName() + " Type: "+type);
		
		if (type == TYPE_BEFORE_CHANGE) {
			if (po instanceof MBankStatementLine) {
				//	Get values
				
				MBankStatementLine bstl = (MBankStatementLine)po;
				Properties ctx = bstl.getCtx();
				BigDecimal stmtAmt = bstl.getStmtAmt();
				Integer bankStatementId = bstl.getC_BankStatement_ID();
				Integer currencyId = bstl.getC_Currency_ID();

				//	
				int AD_Client_ID = Env.getAD_Client_ID(ctx);
				int AD_Org_ID = 0;
				int ConversionTypeId = 0;
				
				//	Get Currency POS
				int currencyPOSId = 0;
				//	For invoice
				List<MPOS> poss = MPOS.getByOrganization(ctx,Env.getAD_Org_ID(ctx), null);
				for(MPOS pos : poss) {
					if(Env.getAD_User_ID(ctx) == pos.getSalesRep_ID() ) {
						
						currencyPOSId = pos.getC_BankAccount().getC_Currency_ID();
						ConversionTypeId = pos.get_ValueAsInt("C_ConversionType_ID");
						break;
					}
				}
				if(currencyPOSId == 0 || currencyId == currencyPOSId)
					return "";
				
				//	Get Currency Info
				MCurrency currency = MCurrency.get (ctx,currencyId);
				MBankStatement bankStatement = new  MBankStatement(ctx, bankStatementId, null);
				Timestamp ConvDate = bankStatement.getStatementDate();
			
				// Get Currency Rate
				BigDecimal CurrencyRate = Env.ONE;
				//	For Conversion Rate
				if (currencyId > 0) {
					//	Rate
					CurrencyRate = MConversionRate.getRate (currencyId,
						currencyPOSId, ConvDate, ConversionTypeId, AD_Client_ID,
						AD_Org_ID);
				}
				if(CurrencyRate != null){
				//	Set Open Amount
				BigDecimal ConvertedAmt = stmtAmt.multiply(CurrencyRate).setScale(
						currency.getStdPrecision(), BigDecimal.ROUND_HALF_UP);
			
				//	Set values
				bstl.set_ValueOfColumn("ConvertedAmt", ConvertedAmt);
			}
		}	
		} else if(type == TYPE_BEFORE_NEW) {

			
		} else if(type == TYPE_AFTER_CHANGE) {
			
		}

		//
		return "";
	}	//	modelChange
	
	@Override
	public String docValidate (PO po, int timing) {
		log.info(po.get_TableName() + " Timing: "+timing);
		//	Validate table


		return null;
	}	//	docValidate
	
	
	/**
	 * Generate Delivery from commission Order that are generated from order
	 * @param order
	 */
	
	@Override
	public int getAD_Client_ID() {
		return clientId;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		return null;
	}
}	//	AgencyValidator