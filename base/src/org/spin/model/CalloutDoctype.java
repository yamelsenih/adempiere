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
 * Contributor(s): German Anzola www.erpya.com				  		                 *
 *************************************************************************************/
package org.spin.model;

import java.util.Properties;

import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MDocType;
import org.compiere.util.Env;

public class CalloutDoctype extends CalloutEngine {

	/**
	 *	DocType value IsApprovedRequired.
	 *		
	 *  Context:
	 *  	- IsApprovedRequired
	 *
	 *  @param ctx      Context
	 *  @param WindowNo current Window No
	 *  @param mTab     Model Tab
	 *  @param mField   Model Field
	 *  @param value    The new value
	 *  @return Error message or ""
	 */
	public String approvedRequired(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value) {
		int documentTypeId = 0;
		String approved = "";
		
		if(mField.getValue() != null)
			documentTypeId=(int)mField.getValue();
		
		MDocType documentType = new MDocType(ctx, documentTypeId, null);
		//	Get value for IsApprovedRequired
		approved = documentType.get_ValueAsString("IsApprovedRequired");
		//	Set value IsApprovedRequired on context
		Env.setContext(ctx, WindowNo, "IsApprovedRequired", approved);
		return "";
	}

}
