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
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for C_ProjectProcessorChanges
 *  @author Adempiere (generated) 
 *  @version Release 3.9.1 - $Id$ */
public class X_C_ProjectProcessorChanges extends PO implements I_C_ProjectProcessorChanges, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190108L;

    /** Standard Constructor */
    public X_C_ProjectProcessorChanges (Properties ctx, int C_ProjectProcessorChanges_ID, String trxName)
    {
      super (ctx, C_ProjectProcessorChanges_ID, trxName);
      /** if (C_ProjectProcessorChanges_ID == 0)
        {
			setC_ProjectProcessorChanges_ID (0);
        } */
    }

    /** Load Constructor */
    public X_C_ProjectProcessorChanges (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_C_ProjectProcessorChanges[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_Column getAD_Column() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Column)MTable.get(getCtx(), org.compiere.model.I_AD_Column.Table_Name)
			.getPO(getAD_Column_ID(), get_TrxName());	}

	/** Set Column.
		@param AD_Column_ID 
		Column in the table
	  */
	public void setAD_Column_ID (int AD_Column_ID)
	{
		if (AD_Column_ID < 1) 
			set_Value (COLUMNNAME_AD_Column_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Column_ID, Integer.valueOf(AD_Column_ID));
	}

	/** Get Column.
		@return Column in the table
	  */
	public int getAD_Column_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Column_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_AD_Table getAD_Table() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Table)MTable.get(getCtx(), org.compiere.model.I_AD_Table.Table_Name)
			.getPO(getAD_Table_ID(), get_TrxName());	}

	/** Set Table.
		@param AD_Table_ID 
		Database Table information
	  */
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1) 
			set_Value (COLUMNNAME_AD_Table_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get Table.
		@return Database Table information
	  */
	public int getAD_Table_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Project Processor Changes ID.
		@param C_ProjectProcessorChanges_ID Project Processor Changes ID	  */
	public void setC_ProjectProcessorChanges_ID (int C_ProjectProcessorChanges_ID)
	{
		if (C_ProjectProcessorChanges_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_ProjectProcessorChanges_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_ProjectProcessorChanges_ID, Integer.valueOf(C_ProjectProcessorChanges_ID));
	}

	/** Get Project Processor Changes ID.
		@return Project Processor Changes ID	  */
	public int getC_ProjectProcessorChanges_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ProjectProcessorChanges_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_ProjectProcessorQueued getC_ProjectProcessorQueued() throws RuntimeException
    {
		return (org.compiere.model.I_C_ProjectProcessorQueued)MTable.get(getCtx(), org.compiere.model.I_C_ProjectProcessorQueued.Table_Name)
			.getPO(getC_ProjectProcessorQueued_ID(), get_TrxName());	}

	/** Set Project Processor Queued ID.
		@param C_ProjectProcessorQueued_ID Project Processor Queued ID	  */
	public void setC_ProjectProcessorQueued_ID (int C_ProjectProcessorQueued_ID)
	{
		if (C_ProjectProcessorQueued_ID < 1) 
			set_Value (COLUMNNAME_C_ProjectProcessorQueued_ID, null);
		else 
			set_Value (COLUMNNAME_C_ProjectProcessorQueued_ID, Integer.valueOf(C_ProjectProcessorQueued_ID));
	}

	/** Get Project Processor Queued ID.
		@return Project Processor Queued ID	  */
	public int getC_ProjectProcessorQueued_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ProjectProcessorQueued_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Record ID.
		@param Record_ID 
		Direct internal record ID
	  */
	public void setRecord_ID (int Record_ID)
	{
		if (Record_ID < 0) 
			set_Value (COLUMNNAME_Record_ID, null);
		else 
			set_Value (COLUMNNAME_Record_ID, Integer.valueOf(Record_ID));
	}

	/** Get Record ID.
		@return Direct internal record ID
	  */
	public int getRecord_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Record_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Immutable Universally Unique Identifier.
		@param UUID 
		Immutable Universally Unique Identifier
	  */
	public void setUUID (String UUID)
	{
		set_Value (COLUMNNAME_UUID, UUID);
	}

	/** Get Immutable Universally Unique Identifier.
		@return Immutable Universally Unique Identifier
	  */
	public String getUUID () 
	{
		return (String)get_Value(COLUMNNAME_UUID);
	}
}