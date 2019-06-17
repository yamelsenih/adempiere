/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 or later of the                                  *
 * GNU General Public License as published                                    *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2015 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package org.spin.util.impexp;

import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.handler.GenericPOHandler;
import org.compiere.model.I_AD_Element;
import org.compiere.model.PO;
import org.compiere.model.POInfo;

/**
 * Custom Exporter of Account Schema
 * @author Yamel Senih, ySenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class ClientExporterHandler extends GenericPOHandler {
	
	private final int ID_FOR_UPDATE = 3000000;
	
	/**
	 * Clear Official IDs for bad references
	 * @param entity
	 */
	public void cleanOfficialReference(PO entity) {
		POInfo poInfo = POInfo.getPOInfo(entity.getCtx(), entity.get_Table_ID(), null);
		entity.setAD_Org_ID(0);
		for(int index = 0; index < poInfo.getColumnCount(); index++) {
			//	No SQL
			if(poInfo.isVirtualColumn(index)) {
				continue;
			}
			//	No Encrypted
			if(poInfo.isEncrypted(index)) {
				continue;
			}
			String columnName = poInfo.getColumnName(index);
			//	No user log
			if(columnName.equals(I_AD_Element.COLUMNNAME_Created)
					|| columnName.equals(I_AD_Element.COLUMNNAME_CreatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_Updated)
					|| columnName.equals(I_AD_Element.COLUMNNAME_UpdatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Client_ID)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Org_ID)) {
				continue;
			}
			if (poInfo.isKey(index)) {
				if(entity.get_ID() < PackOut.MAX_OFFICIAL_ID) {
					entity.set_ValueOfColumn(columnName, entity.get_ID() + ID_FOR_UPDATE);
				}
			}
			//	Verify reference
			if(poInfo.isColumnLookup(index)
					&& !poInfo.isColumnMandatory(index)) {
				if(entity.get_ValueAsInt(columnName) < PackOut.MAX_OFFICIAL_ID) {
					entity.set_ValueOfColumn(columnName, null);
				}
			}
		}
	}
}
