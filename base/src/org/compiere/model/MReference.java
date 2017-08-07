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
 * Copyright (C) 2003-2015 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpcya.com                                 *
 *****************************************************************************/
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.CCache;

/**
 * Added for handle global translation
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/1000">
 * 		@see FR [ 1000 ] Add new feature for unique translation table</a>
 */
public class MReference extends X_AD_Reference {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4717667011975753005L;

	/**
	 * @param ctx
	 * @param AD_Reference_ID
	 * @param trxName
	 */
	public MReference(Properties ctx, int AD_Reference_ID, String trxName) {
		super(ctx, AD_Reference_ID, trxName);
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MReference(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/** Value Cache						*/
	private static CCache<Integer, MReference> cache = new CCache<Integer,MReference>(Table_Name, 20);
	
	/**
	 * Get From Cache
	 * @param ctx
	 * @param referenceId
	 * @return
	 */
	public static MReference getById(Properties ctx, int referenceId) {
		if (referenceId <= 0)
			return null;

		MReference reference = cache.get(referenceId);
		if (reference != null)
			return reference;

		reference = new MReference(ctx, referenceId, null);
		if (reference.get_ID() == referenceId) {
			cache.put(referenceId, reference);
		} else {
			reference = null;
		}
		return reference;
	}
}
