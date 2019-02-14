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

/** Generated Model for ASP_DataLog
 *  @author Adempiere (generated) 
 *  @version Release 3.9.1 - $Id$ */
public class X_ASP_DataLog extends PO implements I_ASP_DataLog, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190213L;

    /** Standard Constructor */
    public X_ASP_DataLog (Properties ctx, int ASP_DataLog_ID, String trxName)
    {
      super (ctx, ASP_DataLog_ID, trxName);
      /** if (ASP_DataLog_ID == 0)
        {
			setAD_Table_ID (0);
			setASP_DataLog_ID (0);
			setASP_Module_ID (0);
			setRecord_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ASP_DataLog (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 6 - System - Client 
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
      StringBuffer sb = new StringBuffer ("X_ASP_DataLog[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	public org.compiere.model.I_AD_Window getAD_Window() throws RuntimeException
    {
		return (org.compiere.model.I_AD_Window)MTable.get(getCtx(), org.compiere.model.I_AD_Window.Table_Name)
			.getPO(getAD_Window_ID(), get_TrxName());	}

	/** Set Window.
		@param AD_Window_ID 
		Data entry or display window
	  */
	public void setAD_Window_ID (int AD_Window_ID)
	{
		if (AD_Window_ID < 1) 
			set_Value (COLUMNNAME_AD_Window_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Window_ID, Integer.valueOf(AD_Window_ID));
	}

	/** Get Window.
		@return Data entry or display window
	  */
	public int getAD_Window_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Window_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ASP Data Log ID.
		@param ASP_DataLog_ID ASP Data Log ID	  */
	public void setASP_DataLog_ID (int ASP_DataLog_ID)
	{
		if (ASP_DataLog_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_ASP_DataLog_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_ASP_DataLog_ID, Integer.valueOf(ASP_DataLog_ID));
	}

	/** Get ASP Data Log ID.
		@return ASP Data Log ID	  */
	public int getASP_DataLog_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ASP_DataLog_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_ASP_Level getASP_Level() throws RuntimeException
    {
		return (org.compiere.model.I_ASP_Level)MTable.get(getCtx(), org.compiere.model.I_ASP_Level.Table_Name)
			.getPO(getASP_Level_ID(), get_TrxName());	}

	/** Set ASP Level.
		@param ASP_Level_ID ASP Level	  */
	public void setASP_Level_ID (int ASP_Level_ID)
	{
		if (ASP_Level_ID < 1) 
			set_Value (COLUMNNAME_ASP_Level_ID, null);
		else 
			set_Value (COLUMNNAME_ASP_Level_ID, Integer.valueOf(ASP_Level_ID));
	}

	/** Get ASP Level.
		@return ASP Level	  */
	public int getASP_Level_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ASP_Level_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_ASP_Module getASP_Module() throws RuntimeException
    {
		return (org.compiere.model.I_ASP_Module)MTable.get(getCtx(), org.compiere.model.I_ASP_Module.Table_Name)
			.getPO(getASP_Module_ID(), get_TrxName());	}

	/** Set ASP Module.
		@param ASP_Module_ID ASP Module	  */
	public void setASP_Module_ID (int ASP_Module_ID)
	{
		if (ASP_Module_ID < 1) 
			set_Value (COLUMNNAME_ASP_Module_ID, null);
		else 
			set_Value (COLUMNNAME_ASP_Module_ID, Integer.valueOf(ASP_Module_ID));
	}

	/** Get ASP Module.
		@return ASP Module	  */
	public int getASP_Module_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ASP_Module_ID);
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