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
package org.spin.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for FM_AmortizationSummary
 *  @author Adempiere (generated) 
 *  @version Release 3.9.3 - $Id$ */
public class X_FM_AmortizationSummary extends PO implements I_FM_AmortizationSummary, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200826L;

    /** Standard Constructor */
    public X_FM_AmortizationSummary (Properties ctx, int FM_AmortizationSummary_ID, String trxName)
    {
      super (ctx, FM_AmortizationSummary_ID, trxName);
      /** if (FM_AmortizationSummary_ID == 0)
        {
			setFM_Account_ID (0);
			setFM_Amortization_ID (0);
			setFM_AmortizationSummary_ID (0);
			setFM_Batch_ID (0);
        } */
    }

    /** Load Constructor */
    public X_FM_AmortizationSummary (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_FM_AmortizationSummary[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Current Capital Amount.
		@param CurrentCapitalAmt Current Capital Amount	  */
	public void setCurrentCapitalAmt (BigDecimal CurrentCapitalAmt)
	{
		set_Value (COLUMNNAME_CurrentCapitalAmt, CurrentCapitalAmt);
	}

	/** Get Current Capital Amount.
		@return Current Capital Amount	  */
	public BigDecimal getCurrentCapitalAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentCapitalAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Current Dunning Amount.
		@param CurrentDunningAmt Current Dunning Amount	  */
	public void setCurrentDunningAmt (BigDecimal CurrentDunningAmt)
	{
		set_Value (COLUMNNAME_CurrentDunningAmt, CurrentDunningAmt);
	}

	/** Get Current Dunning Amount.
		@return Current Dunning Amount	  */
	public BigDecimal getCurrentDunningAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentDunningAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Current Dunning Tax Amount.
		@param CurrentDunningTaxAmt Current Dunning Tax Amount	  */
	public void setCurrentDunningTaxAmt (BigDecimal CurrentDunningTaxAmt)
	{
		set_Value (COLUMNNAME_CurrentDunningTaxAmt, CurrentDunningTaxAmt);
	}

	/** Get Current Dunning Tax Amount.
		@return Current Dunning Tax Amount	  */
	public BigDecimal getCurrentDunningTaxAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentDunningTaxAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Current Fee Amount.
		@param CurrentFeeAmt Current Fee Amount	  */
	public void setCurrentFeeAmt (BigDecimal CurrentFeeAmt)
	{
		set_Value (COLUMNNAME_CurrentFeeAmt, CurrentFeeAmt);
	}

	/** Get Current Fee Amount.
		@return Current Fee Amount	  */
	public BigDecimal getCurrentFeeAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentFeeAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Current Interest Amount.
		@param CurrentInterestAmt Current Interest Amount	  */
	public void setCurrentInterestAmt (BigDecimal CurrentInterestAmt)
	{
		set_Value (COLUMNNAME_CurrentInterestAmt, CurrentInterestAmt);
	}

	/** Get Current Interest Amount.
		@return Current Interest Amount	  */
	public BigDecimal getCurrentInterestAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentInterestAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Current Tax Amount.
		@param CurrentTaxAmt Current Tax Amount	  */
	public void setCurrentTaxAmt (BigDecimal CurrentTaxAmt)
	{
		set_Value (COLUMNNAME_CurrentTaxAmt, CurrentTaxAmt);
	}

	/** Get Current Tax Amount.
		@return Current Tax Amount	  */
	public BigDecimal getCurrentTaxAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_CurrentTaxAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Document Date.
		@param DateDoc 
		Date of the Document
	  */
	public void setDateDoc (Timestamp DateDoc)
	{
		set_Value (COLUMNNAME_DateDoc, DateDoc);
	}

	/** Get Document Date.
		@return Date of the Document
	  */
	public Timestamp getDateDoc () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateDoc);
	}

	public org.spin.model.I_FM_Account getFM_Account() throws RuntimeException
    {
		return (org.spin.model.I_FM_Account)MTable.get(getCtx(), org.spin.model.I_FM_Account.Table_Name)
			.getPO(getFM_Account_ID(), get_TrxName());	}

	/** Set Financial Account.
		@param FM_Account_ID Financial Account	  */
	public void setFM_Account_ID (int FM_Account_ID)
	{
		if (FM_Account_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_FM_Account_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_FM_Account_ID, Integer.valueOf(FM_Account_ID));
	}

	/** Get Financial Account.
		@return Financial Account	  */
	public int getFM_Account_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_FM_Account_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getFM_Account_ID()));
    }

	public org.spin.model.I_FM_Amortization getFM_Amortization() throws RuntimeException
    {
		return (org.spin.model.I_FM_Amortization)MTable.get(getCtx(), org.spin.model.I_FM_Amortization.Table_Name)
			.getPO(getFM_Amortization_ID(), get_TrxName());	}

	/** Set Loan Amortization.
		@param FM_Amortization_ID Loan Amortization	  */
	public void setFM_Amortization_ID (int FM_Amortization_ID)
	{
		if (FM_Amortization_ID < 1) 
			set_Value (COLUMNNAME_FM_Amortization_ID, null);
		else 
			set_Value (COLUMNNAME_FM_Amortization_ID, Integer.valueOf(FM_Amortization_ID));
	}

	/** Get Loan Amortization.
		@return Loan Amortization	  */
	public int getFM_Amortization_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_FM_Amortization_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Loan Amortization Summary.
		@param FM_AmortizationSummary_ID Loan Amortization Summary	  */
	public void setFM_AmortizationSummary_ID (int FM_AmortizationSummary_ID)
	{
		if (FM_AmortizationSummary_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_FM_AmortizationSummary_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_FM_AmortizationSummary_ID, Integer.valueOf(FM_AmortizationSummary_ID));
	}

	/** Get Loan Amortization Summary.
		@return Loan Amortization Summary	  */
	public int getFM_AmortizationSummary_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_FM_AmortizationSummary_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.spin.model.I_FM_Batch getFM_Batch() throws RuntimeException
    {
		return (org.spin.model.I_FM_Batch)MTable.get(getCtx(), org.spin.model.I_FM_Batch.Table_Name)
			.getPO(getFM_Batch_ID(), get_TrxName());	}

	/** Set Financial Transaction Batch.
		@param FM_Batch_ID Financial Transaction Batch	  */
	public void setFM_Batch_ID (int FM_Batch_ID)
	{
		if (FM_Batch_ID < 1) 
			set_Value (COLUMNNAME_FM_Batch_ID, null);
		else 
			set_Value (COLUMNNAME_FM_Batch_ID, Integer.valueOf(FM_Batch_ID));
	}

	/** Get Financial Transaction Batch.
		@return Financial Transaction Batch	  */
	public int getFM_Batch_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_FM_Batch_ID);
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