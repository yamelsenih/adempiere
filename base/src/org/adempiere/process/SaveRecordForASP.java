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

package org.adempiere.process;

import org.compiere.model.MASPDataLog;

/** Generated Process for (Save / Update Record for ASP Module)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.1
 */
public class SaveRecordForASP extends SaveRecordForASPAbstract {

	@Override
	protected String doIt() throws Exception {
		if(getRecord_ID() > 0) {
			MASPDataLog log = MASPDataLog.getOrCreate(getCtx(), getTable_ID(), getRecord_ID(), getModuleId(), getLevelId(), get_TrxName());
			log.saveEx();
		}
		return "";
	}
}