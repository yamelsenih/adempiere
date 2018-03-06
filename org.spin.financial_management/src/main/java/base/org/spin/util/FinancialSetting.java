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
package org.spin.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.spin.model.MFMFunctionalApplicability;
import org.spin.model.MFMProduct;

/**
 * Financial Management
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1583 ] New Definition for loan
 *		@see https://github.com/adempiere/adempiere/issues/1583
 */
public class FinancialSetting {

	private FinancialSetting() {
		
	}
	
	/** Engine Singleton				*/
	private static FinancialSetting settingEngine = null;
	private HashMap<String, Object> returnValues;
	
	public static final String PARAMETER_PO = "PO";
	public static final String PARAMETER_CTX = "CTX";
	public static final String PARAMETER_TRX_NAME = "TRX_NAME";

	/**
	 * 	Get Singleton
	 *	@return modelValidatorEngine
	 */
	public synchronized static FinancialSetting get() {
		if (settingEngine == null)
			settingEngine = new FinancialSetting();
		return settingEngine;
	}	//	get
	
	/**
	 * Get return Values
	 * @return
	 */
	public HashMap<String, Object> getReturnValues() {
		return returnValues;
	}
	
	/**
	 * 	Fire Document Validation.
	 * 	Call docValidate method of added validators
	 *	@param po persistent objects
	 *	@param docTiming see ModelValidator.TIMING_ constants
     *	@return error message or null
	 */
	public String fireDocValidate(PO po, int docTiming, int financialProductId) {
		if (po == null
				|| financialProductId <= 0) {
			return null;
		}
		//	Get Product
		MFMProduct financialProduct = MFMProduct.getById(po.getCtx(), financialProductId);
		//	Apply Listener
		List<MFMFunctionalApplicability> applicabilityList = financialProduct.getApplicability(po.get_TableName(), ModelValidator.documentEventValidators[docTiming]);
		//	flush return values
		returnValues = new HashMap<String, Object>();
		//	default
		return runApplicability(po.getCtx(), po.get_TrxName(), po, applicabilityList, null);
	}
	
	/**
	 * 	Fire Model Change.
	 * 	Call modelChange method of added validators
	 *	@param po persistent objects
	 *	@param changeType ModelValidator.TYPE_*
	 *	@return error message or NULL for no veto
	 */
	public String fireModelChange(PO po, int changeType, int financialProductId) {
		if (po == null
				|| financialProductId <= 0) {
			return null;
		}
		//	Get Product
		MFMProduct financialProduct = MFMProduct.getById(po.getCtx(), financialProductId);
		if(financialProduct == null) {
			return null;
		}
		//	Apply Listener
		List<MFMFunctionalApplicability> applicabilityList = financialProduct.getApplicability(po.get_TableName(), ModelValidator.tableEventValidators[changeType]);
		//	flush return values
		returnValues = new HashMap<String, Object>();
		//	default
		return runApplicability(po.getCtx(), po.get_TrxName(), po, applicabilityList, null);
	}
	
	
	/**
	 * Fire it for a alert process scheduled
	 * @param ctx
	 * @param financialProductId
	 * @param trxName
	 * @return
	 */
	public String fire(Properties ctx, int financialProductId, String eventType, HashMap<String, Object> parameters, String trxName) {
		if (financialProductId <= 0) {
			return null;
		}
		//	
		MFMProduct financialProduct = MFMProduct.getById(ctx, financialProductId);
		if(financialProduct == null) {
			return null;
		}
		//	Apply Listener
		List<MFMFunctionalApplicability> applicabilityList = financialProduct.getApplicability(eventType);
		//	flush return values
		returnValues = new HashMap<String, Object>();
		//	default
		return runApplicability(ctx, trxName, null, applicabilityList, parameters);
	}
	
	/**
	 * Run Applicability from PO
	 * @param po
	 * @param applicabilityList
	 * @return
	 */
	private String runApplicability(Properties ctx, String trxName, PO po, List<MFMFunctionalApplicability> applicabilityList, HashMap<String, Object> parameters) {
		//	
		StringBuffer message = new StringBuffer();
		try {
			//	Iterate applicability for run
			for(MFMFunctionalApplicability applicability : applicabilityList) {
				AbstractFunctionalSetting settingForRun = applicability.getSetting();
				//	Validate Null Value
				if(settingForRun == null) {
					continue;
				}
				//	
				settingForRun.setFunctionalApplicability(applicability);
				settingForRun.setParameter(PARAMETER_PO, po);
				settingForRun.setParameter(PARAMETER_CTX, ctx);
				settingForRun.setParameter(PARAMETER_TRX_NAME, trxName);
				if(parameters != null) {
					settingForRun.setParameters(parameters);
				}
				//	Run It
				String runMsg = settingForRun.run();
				if(runMsg != null) {
					//	Add new line
					if(message.length() > 0) {
						message.append(Env.NL);
					}
					message.append(runMsg);
				}
				//	Copy Return Value
				for(Entry<String, Object> entry : settingForRun.getReturnValues().entrySet()) {
					returnValues.put(entry.getKey(), entry.getValue());
				}
			}
		} catch(Exception e) {
			message.append(e);
		}
		//	
		if(message.length() > 0) {
			return message.toString();
		}
		//	Default
		return null;
	}
}
