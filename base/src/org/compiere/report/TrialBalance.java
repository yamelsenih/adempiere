/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
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
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.report;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import org.compiere.model.MAcctSchemaElement;
import org.compiere.model.MPeriod;
import org.compiere.util.DB;
import org.compiere.util.Language;
import org.compiere.util.Msg;


/**
 *	Trial Balance
 *	
 *  @author Jorg Janke
 *
 *  @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 * 			<li> FR [ 2520591 ] Support multiples calendar for Org 
 *			@see http://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id=176962 
 *  @version $Id: TrialBalance.java,v 1.2 2006/07/30 00:51:05 jjanke Exp $
 *  @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<li> FR [ 2797 ] Error Trial Balance
 *		@see https://github.com/adempiere/adempiere/issues/2797
 */
public class TrialBalance extends TrialBalanceAbstract {
	
	private String				plID = null;
	private String				bsID = null;
	
	/**	Parameter Where Clause			*/
	private StringBuffer		m_parameterWhere = new StringBuffer();
	
	/**	Start Time						*/
	private long 				m_start = System.currentTimeMillis();
	/**	Insert Statement				*/
	private static String		s_insert = "INSERT INTO T_TrialBalance "
		+ "(AD_PInstance_ID, Fact_Acct_ID,"
		+ " AD_Client_ID, AD_Org_ID, Created,CreatedBy, Updated,UpdatedBy,"
		+ " C_AcctSchema_ID, Account_ID, AccountValue, DateTrx, DateAcct, C_Period_ID,"
		+ " AD_Table_ID, Record_ID, Line_ID,"
		+ " GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,"
		+ " C_Currency_ID, AmtSourceDr, AmtSourceCr, AmtSourceBalance,"
		+ " AmtAcctDr, AmtAcctCr, AmtAcctBalance, C_UOM_ID, Qty,"
		+ " M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,"
		+ " C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,"
		+ " User1_ID, User2_ID,User3_ID, User4_ID, A_Asset_ID, Description)";

	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		super.prepare();
		StringBuffer sb = new StringBuffer ("AD_PInstance_ID=")
			.append(getAD_PInstance_ID());
		//	Mandatory C_AcctSchema_ID
		m_parameterWhere.append("C_AcctSchema_ID=").append(getAcctSchemaId());
		if (getAccountValue() != null && getAccountValue().length() == 0)
			setAccountValue(null);
		if (getAccountValueTo() != null && getAccountValueTo().length() == 0)
			setAccountValue(null);
		//	Optional Org
		if (getOrgId() != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				getHierarchyId(), MAcctSchemaElement.ELEMENTTYPE_Organization, getOrgId()));
		//	Optional BPartner
		if (getBPartnerId() != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				getHierarchyId(), MAcctSchemaElement.ELEMENTTYPE_BPartner, getBPartnerId()));
		//	Optional Product
		if (getProductId() != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				getHierarchyId(), MAcctSchemaElement.ELEMENTTYPE_Product, getProductId()));
		//	Optional Project
		if (getProjectId() != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				getHierarchyId(), MAcctSchemaElement.ELEMENTTYPE_Project, getProjectId()));
		//	Optional Activity
		if (getActivityId() != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				getHierarchyId(), MAcctSchemaElement.ELEMENTTYPE_Activity, getActivityId()));
		//	Optional Campaign
		if (getCampaignId() != 0)
			m_parameterWhere.append(" AND C_Campaign_ID=").append(getCampaignId());
		//	m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
		//		MAcctSchemaElement.ELEMENTTYPE_Campaign, getCampaignId()));
		//	Optional Sales Region
		if (getSalesRegionId() != 0)
			m_parameterWhere.append(" AND ").append(MReportTree.getWhereClause(getCtx(), 
				getHierarchyId(), MAcctSchemaElement.ELEMENTTYPE_SalesRegion, getSalesRegionId()));
		//	Mandatory Posting Type
		m_parameterWhere.append(" AND PostingType='").append(getPostingType()).append("'");
		//
		setDateAcct();
		setDiffAccountByPlBs();
		sb.append(" - DateAcct ").append(getDateAcct()).append("-").append(getDateAcctTo());
		sb.append(" - Where=").append(m_parameterWhere);
		log.fine(sb.toString());
	}	//	prepare

	/**
	 * 	Set Start/End Date of Report - if not defined current Month
	 */
	private void setDateAcct()
	{
		//	Date defined
		if (getDateAcct() != null)
		{
			if (getDateAcctTo() == null)
				setDateAcct(new Timestamp (System.currentTimeMillis()));
			return;
		}
		//	Get Date from Period
		if (getPeriodId() == 0)
		{
		   GregorianCalendar cal = new GregorianCalendar(Language.getLoginLanguage().getLocale());
		   cal.setTimeInMillis(System.currentTimeMillis());
		   cal.set(Calendar.HOUR_OF_DAY, 0);
		   cal.set(Calendar.MINUTE, 0);
		   cal.set(Calendar.SECOND, 0);
		   cal.set(Calendar.MILLISECOND, 0);
		   cal.set(Calendar.DAY_OF_MONTH, 1);		//	set to first of month
		   setDateAcct(new Timestamp (cal.getTimeInMillis()));
		   cal.add(Calendar.MONTH, 1);
		   cal.add(Calendar.DAY_OF_YEAR, -1);		//	last of month
		   setDateAcctTo(new Timestamp (cal.getTimeInMillis()));
		   return;
		}

		String sql = "SELECT StartDate, EndDate FROM C_Period WHERE C_Period_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, getPeriodId());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				setDateAcct(rs.getTimestamp(1));
				setDateAcctTo(rs.getTimestamp(2));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
	}	//	setDateAcct

	/**
	 * Differentiate account element by PL or BS
	 */
	private void setDiffAccountByPlBs()
	{
		StringBuffer bs = new StringBuffer();
		StringBuffer pl = new StringBuffer();
		StringBuffer sql = new StringBuffer("SELECT C_ElementValue_ID, value, AccountType FROM C_ElementValue WHERE AD_Client_ID = ? ");
		if (getParameterAsString("SeletedID") != null && !getParameterAsString("SeletedID").isEmpty())
			sql.append(" AND C_ElementValue_ID IN (").append(getParameterAsString("SeletedID")).append(")");
		else if (getAccountId() > 0)
			sql.append(" AND C_ElementValue_ID = ").append(getAccountId());
		else if (getAccountValue() != null && getAccountValueTo() != null)
			sql.append(" AND value >= ").append(DB.TO_STRING(getAccountValue())).append(" AND Value <= ").append(DB.TO_STRING(getAccountValueTo()));
		else if (getAccountValue() != null && getAccountValueTo() == null)
			sql.append(" AND Value >= ").append(DB.TO_STRING(getAccountValue()));
		else if (getAccountValue() == null && getAccountValueTo() != null)
			sql.append("Value <= ").append(DB.TO_STRING(getAccountValueTo()));
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, getAD_Client_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (rs.getString("AccountType").equals("A") || rs.getString("AccountType").equals("L") || rs.getString("AccountType").equals("O")) // 1 Asset, 2 Liability, 3 Owner's Equity
					bs.append(rs.getInt("C_ElementValue_ID")).append(",");
				else
					pl.append(rs.getInt("C_ElementValue_ID")).append(",");
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			
			if (bs.toString() != null && !bs.toString().isEmpty())
				bsID = bs.toString().substring(0, bs.lastIndexOf(","));
			if (pl.toString() != null && !pl.toString().isEmpty())
				plID = pl.toString().substring(0, pl.lastIndexOf(","));
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
	} // setAccountDiff
	
	/**************************************************************************
	 *  Perform process.
	 *  @return Message to be translated
	 */
	protected String doIt()
	{
		if (bsID != null && !bsID.isEmpty())
			createBalanceLine(false);
		if (plID != null && !plID.isEmpty())
			createBalanceLine(true);
		
		createDetailLines();

	//	int AD_PrintFormat_ID = 134;
	//	getProcessInfo().setTransientObject (MPrintFormat.get (getCtx(), AD_PrintFormat_ID, false));

		log.fine((System.currentTimeMillis() - m_start) + " ms");
		return "";
	}	//	doIt

	/**
	 * 	Create Beginning Balance Line
	 */
	private void createBalanceLine(boolean isPL_IDs)
	{
		StringBuffer sql = new StringBuffer (s_insert);
		//	(AD_PInstance_ID, Fact_Acct_ID,
		sql.append("SELECT ").append(getAD_PInstance_ID()).append(",-1*Account_ID,");
		//	AD_Client_ID, AD_Org_ID, Created,CreatedBy, Updated,UpdatedBy,
		sql.append(getAD_Client_ID()).append(",");
		if (getOrgId() == 0)
			sql.append("0");
		else
			sql.append(getOrgId());
		sql.append(", SysDate,").append(getAD_User_ID())
			.append(",SysDate,").append(getAD_User_ID()).append(",");
		//	C_AcctSchema_ID, Account_ID, AccountValue, DateTrx, DateAcct, C_Period_ID,
		sql.append(getAcctSchemaId()).append(",");
		if (getAccountId() == 0)
			sql.append ("Account_ID");
		else
			sql.append (getAccountId());
		if (getAccountValue() != null)
			sql.append(",").append(DB.TO_STRING(getAccountValue()));
		else if (getAccountValueTo() != null)
			sql.append(",' '");
		else
			sql.append(",null");
		Timestamp balanceDay = getDateAcct(); // TimeUtil.addDays(getDateAcct(), -1);
		sql.append(",null,").append(DB.TO_DATE(balanceDay, true)).append(",");
		if (getPeriodId() == 0)
			sql.append("null");
		else
			sql.append(getPeriodId());
		sql.append(",");
		//	AD_Table_ID, Record_ID, Line_ID,
		sql.append("null,null,null,");
		//	GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,
		sql.append("null,null,null,null,'").append(getPostingType()).append("',");
		//	C_Currency_ID, AmtSourceDr, AmtSourceCr, AmtSourceBalance,
		sql.append("null,null,null,null,");
		//	AmtAcctDr, AmtAcctCr, AmtAcctBalance, C_UOM_ID, Qty,
		sql.append(" COALESCE(SUM(AmtAcctDr),0),COALESCE(SUM(AmtAcctCr),0),"
				  + "COALESCE(SUM(AmtAcctDr),0)-COALESCE(SUM(AmtAcctCr),0),"
			+ " null,COALESCE(SUM(Qty),0),");
		//	M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,
		if (getProductId() == 0)
			sql.append ("null");
		else
			sql.append (getProductId());
		sql.append(",");
		if (getBPartnerId() == 0)
			sql.append ("null");
		else
			sql.append (getBPartnerId());
		sql.append(",");
		if (getParameterAsInt("AD_OrgTrx_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("AD_OrgTrx_ID"));
		sql.append(",");
		if (getParameterAsInt("C_LocFrom_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("C_LocFrom_ID"));
		sql.append(",");
		if (getParameterAsInt("C_LocTo_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("C_LocTo_ID"));
		sql.append(",");
		//	C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,
		if (getSalesRegionId() == 0)
			sql.append ("null");
		else
			sql.append (getSalesRegionId());
		sql.append(",");
		if (getProjectId() == 0)
			sql.append ("null");
		else
			sql.append (getProjectId());
		sql.append(",");
		if (getCampaignId() == 0)
			sql.append ("null");
		else
			sql.append (getCampaignId());
		sql.append(",");
		if (getActivityId() == 0)
			sql.append ("null");
		else
			sql.append (getActivityId());
		sql.append(",");
		//	User1_ID, User2_ID, A_Asset_ID, Description)
		if (getParameterAsInt("User1_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("User1_ID"));
		sql.append(",");
		if (getParameterAsInt("User2_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("User2_ID"));
		sql.append(",");
		//	User3_ID, User4_ID, A_Asset_ID, Description)
		if (getParameterAsInt("User3_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("User3_ID"));
		sql.append(",");
		if (getParameterAsInt("User4_ID") == 0)
			sql.append ("null");
		else
			sql.append (getParameterAsInt("User4_ID"));
		
		sql.append(", null,'");
		sql.append(Msg.getMsg(getCtx(), "opening.balance") + "'");
		//
		sql.append(" FROM Fact_Acct WHERE AD_Client_ID=").append(getAD_Client_ID())
			.append (" AND ").append(m_parameterWhere)
			.append(" AND DateAcct < ").append(DB.TO_DATE(getDateAcct(), true));
		//	Start Beginning of Year
		if (isPL_IDs)
		{
			MPeriod first = MPeriod.getFirstInYear(getCtx(), getDateAcct(), getOrgId());
			if (first != null)
				sql.append(" AND DateAcct >= ").append(DB.TO_DATE(first.getStartDate(), true));
			else
				log.log(Level.SEVERE, "first period not found");
		}
		
		sql.append(" AND Account_ID IN (");
		if (isPL_IDs)
			sql.append(plID).append(")");
		else
			sql.append(bsID).append(")");
		
		sql.append(" GROUP BY Account_ID");
		//
		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no == 0)
			log.fine(sql.toString());
		log.fine("#" + no + " (Account_ID=" + getAccountId() + ")");
	}	//	createBalanceLine

	/**
	 * 	Create Beginning Balance Line
	 */
	private void createDetailLines()
	{
		StringBuffer sql = new StringBuffer (s_insert);
		//	(AD_PInstance_ID, Fact_Acct_ID,
		sql.append("SELECT ").append(getAD_PInstance_ID()).append(",Fact_Acct_ID,");
		//	AD_Client_ID, AD_Org_ID, Created,CreatedBy, Updated,UpdatedBy,
		sql.append(getAD_Client_ID()).append(",AD_Org_ID,Created,CreatedBy, Updated,UpdatedBy,");
		//	C_AcctSchema_ID, Account_ID, DateTrx, AccountValue, DateAcct, C_Period_ID,
		sql.append("C_AcctSchema_ID, Account_ID, null, DateTrx, DateAcct, C_Period_ID,");
		//	AD_Table_ID, Record_ID, Line_ID,
		sql.append("AD_Table_ID, Record_ID, Line_ID,");
		//	GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,
		sql.append("GL_Category_ID, GL_Budget_ID, C_Tax_ID, M_Locator_ID, PostingType,");
		//	C_Currency_ID, AmtSourceDr, AmtSourceCr, AmtSourceBalance,
		sql.append("C_Currency_ID, AmtSourceDr,AmtSourceCr, AmtSourceDr-AmtSourceCr,");
		//	AmtAcctDr, AmtAcctCr, AmtAcctBalance, C_UOM_ID, Qty,
		sql.append(" AmtAcctDr,AmtAcctCr, AmtAcctDr-AmtAcctCr, C_UOM_ID,Qty,");
		//	M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,
		sql.append ("M_Product_ID, C_BPartner_ID, AD_OrgTrx_ID, C_LocFrom_ID,C_LocTo_ID,");
		//	C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,
		sql.append ("C_SalesRegion_ID, C_Project_ID, C_Campaign_ID, C_Activity_ID,");
		//	User1_ID, User2_ID, User3_ID, User4_ID , A_Asset_ID, Description)
		sql.append ("User1_ID, User2_ID, User3_ID, User4_ID, A_Asset_ID, Description");
		//
		sql.append(" FROM Fact_Acct WHERE AD_Client_ID=").append(getAD_Client_ID())
			.append (" AND ").append(m_parameterWhere)
			.append(" AND DateAcct >= ").append(DB.TO_DATE(getDateAcct(), true))
			.append(" AND TRUNC(DateAcct, 'DD') <= ").append(DB.TO_DATE(getDateAcctTo(), true));
		//
		sql.append(" AND Account_ID IN (");
		if (plID != null && !plID.isEmpty())
			sql.append(plID);
		if (bsID != null && !bsID.isEmpty())
		{
			if (plID != null && !plID.isEmpty())
				sql.append(",");
			sql.append(bsID);
		}
		sql.append(")");
		
		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no == 0)
			log.fine(sql.toString());
		log.fine("#" + no + " (Account_ID=" + getAccountId() + ")");
		
		//	Update AccountValue
		String sql2 = "UPDATE T_TrialBalance tb SET AccountValue = "
			+ "(SELECT Value FROM C_ElementValue ev WHERE ev.C_ElementValue_ID=tb.Account_ID) "
			+ "WHERE tb.Account_ID IS NOT NULL AND tb.AD_PInstance_ID = " + getAD_PInstance_ID();
		no = DB.executeUpdate(sql2, get_TrxName());
		if (no > 0)
			log.fine("Set AccountValue #" + no);
		
	}	//	createDetailLines

}	//	TrialBalance
