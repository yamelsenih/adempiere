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

package org.spin.report;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/** Generated Process for (Bank Balance)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.2
 */
public class BankBalanceToDate extends BankBalanceToDateAbstract {

	private String sql = null;
	private String sqlInsert = null;
	
	@Override
	protected void prepare() {
		super.prepare();
		if(getDateTrx() == null) {
			setDateTrx(new Timestamp(System.currentTimeMillis()));
		}
		if(getDateTrxTo() == null) {
			setDateTrxTo(new Timestamp(System.currentTimeMillis()));
		}
		String beginningBalanceMessage = Msg.getMsg(getCtx(), "BeginningBalance");
		sql = "SELECT p.SeqNo, p.C_BankAccount_ID, p.IsReceipt, p.DocStatus, p.StatementDate, p.DateTrx, p.PayAmt, p.DocumentNo, "
				+ "p.UnReconciledDebit, p.UnReconciledCredit, (p.UnReconciledCredit - p.UnReconciledDebit) UnReconciledBalance, " 
				+ "p.ReconciledDebit, p.ReconciledCredit, (p.ReconciledCredit - p.ReconciledDebit) ReconciledBalance, " 
				+ "p.C_BankStatement_ID, p.C_BPartner_ID, COALESCE(ba.C_Bank_ID, " + getBankId() + ") C_Bank_ID, p.Description, p.LineDescription "
				+ "FROM "
				+ "( "
				+ "SELECT "
				+ "10 AS SeqNo, "
				+ "p.C_BankAccount_ID, "
				+ "NULL AS IsReceipt, "
				+ "NULL AS DocStatus, "
				+ "NULL StatementDate, "
				+ "SUM(p.PayAmt) PayAmt, "
				+ "'' DocumentNo, "
				+ "? DateTrx, "
				+ "'* " + beginningBalanceMessage + " *' Description, "
				+ "'* " + beginningBalanceMessage + " *' LineDescription, "
				+ "SUM(CASE WHEN p.StatementDate IS NULL OR IsReconciled='N' THEN CASE WHEN IsReceipt = 'N' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END) UnReconciledDebit, "
				+ "SUM(CASE WHEN p.StatementDate IS NULL OR IsReconciled='N' THEN CASE WHEN IsReceipt = 'Y' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END) UnReconciledCredit, "
				+ "SUM(CASE WHEN p.StatementDate IS NOT NULL AND IsReconciled = 'Y' THEN CASE WHEN IsReceipt = 'N' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END) ReconciledDebit, "
				+ "SUM(CASE WHEN p.StatementDate IS NOT NULL AND IsReconciled = 'Y' THEN CASE WHEN IsReceipt = 'Y' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END) ReconciledCredit, "
				+ "NULL C_BankStatement_ID, NULL C_BPartner_ID, NULL C_Payment_ID "
				+ "FROM "
				+ "(	SELECT "
				+ "		COALESCE(bs.StatementDate, vp.DateTrx) StatementDate, "
				+ "		p.PayAmt, "
				+ "		CASE WHEN SIGN(p.PayAmt) > 0 THEN 'Y' ELSE 'N' END IsReceipt, "
				+ "		p.C_BankAccount_ID, "
				+ "		p.DateTrx, "
				+ "		CASE WHEN COALESCE(bs.StatementDate, vp.DateTrx) < ? THEN 'Y' ELSE 'N' END AS IsReconciled "
				+ "	FROM "
				+ "	RV_Payment p "
				+ "	LEFT JOIN C_BankStatementLine bsl ON(bsl.C_Payment_ID = p.C_Payment_ID) "
				+ "	LEFT JOIN C_BankStatement bs ON(bs.C_BankStatement_ID = bsl.C_BankStatement_ID) "
				+ "	LEFT JOIN C_Payment vp ON(vp.Reversal_ID = p.C_Payment_ID AND p.DocStatus IN ('RE','VO') AND vp.DocStatus IN ('RE','VO')) "
				+ "	WHERE p.Processed = 'Y' "
				+ " AND p.AD_Client_ID = " + getAD_Client_ID() + " "
				+ ")  AS p "
				+ "WHERE p.DateTrx < ? "
				+ "GROUP BY p.C_BankAccount_ID "
				
				+ "UNION ALL "

				+ "SELECT "
				+ "20 AS SeqNo, "
				+ "p.C_BankAccount_ID, " 
				+ "p.IsReceipt, "
				+ "p.DocStatus, "
				+ "p.StatementDate, "
				+ "ABS(p.PayAmt) AS PayAmt, "
				+ "p.DocumentNo, "
				+ "p.DateTrx, "
				+ "p.Description, "
				+ "p.LineDescription, "
				+ "CASE WHEN p.StatementDate IS NULL OR IsReconciled='N' THEN CASE WHEN IsReceipt = 'N' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END UnReconciledDebit, "
				+ "CASE WHEN p.StatementDate IS NULL OR IsReconciled='N' THEN CASE WHEN IsReceipt = 'Y' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END UnReconciledCredit, "
				+ "CASE WHEN p.StatementDate IS NOT NULL AND IsReconciled = 'Y' THEN CASE WHEN IsReceipt = 'N' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END ReconciledDebit, "
				+ "CASE WHEN p.StatementDate IS NOT NULL AND IsReconciled = 'Y' THEN CASE WHEN IsReceipt = 'Y' THEN ABS(p.PayAmt) ELSE 0 END ELSE 0 END ReconciledCredit, "
				+ "p.C_BankStatement_ID, "
				+ "p.C_BPartner_ID, p.C_Payment_ID "
				+ "FROM "
				+ "(	SELECT "
				+ "		COALESCE(bs.StatementDate, vp.DateTrx) StatementDate, "
				+ "		p.PayAmt,"
				+ "		CASE WHEN SIGN(p.PayAmt) > 0 THEN 'Y' ELSE 'N' END IsReceipt, "
				+ "		p.C_BankAccount_ID, "
				+ "		p.DateTrx, "
				+ "		CASE WHEN COALESCE(bs.StatementDate, vp.DateTrx) BETWEEN ? AND ? THEN 'Y' ELSE 'N' END AS IsReconciled, "
				+ "		p.DocStatus, p.DocumentNo, bsl.C_BankStatement_ID, p.C_BPartner_ID, p.C_Payment_ID, (bp.Name || COALESCE(' ' || bp.Name2, '')) AS Description, bsl.Description AS LineDescription "
				+ "	FROM "
				+ "	RV_Payment p "
				+ " INNER JOIN C_BPartner bp ON(bp.C_BPartner_ID = p.C_BPartner_ID) "
				+ "	LEFT JOIN C_BankStatementLine bsl ON(bsl.C_Payment_ID = p.C_Payment_ID) "
				+ "	LEFT JOIN C_BankStatement bs ON(bs.C_BankStatement_ID = bsl.C_BankStatement_ID) "
				+ "	LEFT JOIN C_Payment vp ON(vp.Reversal_ID = p.C_Payment_ID AND p.DocStatus IN ('RE','VO') AND vp.DocStatus IN ('RE','VO')) "
				+ "	WHERE p.Processed = 'Y' "
				+ " AND p.AD_Client_ID = " + getAD_Client_ID() + " "
				+ ")  AS p "
				+ "WHERE p.DateTrx BETWEEN ? AND ? "
				+ ") p "
				+ "INNER JOIN C_BankAccount ba ON(ba.C_BankAccount_ID = p.C_BankAccount_ID) "
				+ "WHERE ba.C_Bank_ID = " + getBankId() + " "
				+ (getBankAccountId() != 0? "AND p.C_BankAccount_ID = " + getBankAccountId() + " ": "")
				+ "ORDER BY SeqNo, DateTrx, StatementDate";
		//	Prepare SQL Insert
		sqlInsert = "INSERT INTO T_BankBalance(AD_Client_ID, AD_Org_ID, AD_PInstance_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy, "
				+ "C_Bank_ID, C_BankAccount_ID, C_BPartner_ID, Description, LineDescription, "
				+ "DocStatus, DocumentNo, IsReceipt, PayAmt, ReconciledDebit, ReconciledCredit, ReconciledBalance, SeqNo, StatementDate, DateTrx, "
				+ "UnReconciledDebit, UnReconciledCredit, UnReconciledBalance, AvailableAmt, BalanceAmount) "
				+ "VALUES(" + getAD_Client_ID() + " , " + Env.getAD_Org_ID(getCtx()) + ", " + getAD_PInstance_ID() + ", 'Y', getDate(), " + getAD_User_ID() + ", getDate(), " + getAD_User_ID() 
				+ ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}
	
	@Override
	protected String doIt() throws Exception {
		log.fine("SQL = " + sql.toString());
		//	Prepare statement
		PreparedStatement pstmtSelect = DB.prepareStatement (sql, get_TrxName());
		int i = 1;
		pstmtSelect.setTimestamp(i++, getDateTrx());
		pstmtSelect.setTimestamp(i++, getDateTrx());
		pstmtSelect.setTimestamp(i++, getDateTrx());
		pstmtSelect.setTimestamp(i++, getDateTrx());
		pstmtSelect.setTimestamp(i++, getDateTrxTo());
		pstmtSelect.setTimestamp(i++, getDateTrx());
		pstmtSelect.setTimestamp(i++, getDateTrxTo());
		ResultSet resultSet = pstmtSelect.executeQuery();
		//	
		BigDecimal reconciledSummaryBalance = Env.ZERO;
		BigDecimal unReconciledSummaryBalance = Env.ZERO;
		//	Get Data
		while(resultSet.next()) {
			//	10 = Balance
			int sequece = resultSet.getInt("SeqNo");
			BigDecimal reconciledBalance = resultSet.getBigDecimal("ReconciledBalance");
			BigDecimal unReconciledBalance = resultSet.getBigDecimal("UnReconciledBalance");
			BigDecimal reconciledDebit = resultSet.getBigDecimal("ReconciledDebit");
			BigDecimal reconciledCredit = resultSet.getBigDecimal("ReconciledCredit");
			BigDecimal unReconciledDebit = resultSet.getBigDecimal("UnReconciledDebit");
			BigDecimal unReconciledCredit = resultSet.getBigDecimal("UnReconciledCredit");
			//	Change values
			if(sequece == 10) {
				reconciledDebit = Env.ZERO;
				reconciledCredit = Env.ZERO;
				unReconciledDebit = Env.ZERO;
				unReconciledCredit = Env.ZERO;
				reconciledSummaryBalance = reconciledBalance;
				unReconciledSummaryBalance = unReconciledBalance;
			} else { 
				reconciledSummaryBalance = reconciledSummaryBalance.add(reconciledCredit.subtract(reconciledDebit));
				unReconciledSummaryBalance = unReconciledSummaryBalance.add(unReconciledCredit.subtract(unReconciledDebit));
			}
			BigDecimal availableAmount = reconciledSummaryBalance.add(unReconciledSummaryBalance);
			BigDecimal balanceAmount = reconciledBalance.add(unReconciledBalance);
			//	Insert
			DB.executeUpdateEx(sqlInsert, new Object[] {
					resultSet.getInt("C_Bank_ID"),
					resultSet.getInt("C_BankAccount_ID"),
					resultSet.getInt("C_BPartner_ID"),
					resultSet.getString("Description"),
					resultSet.getString("LineDescription"),
					resultSet.getString("DocStatus"),
					resultSet.getString("DocumentNo"),
					resultSet.getString("IsReceipt"),
					resultSet.getBigDecimal("PayAmt"),
					reconciledDebit,
					reconciledCredit,
					reconciledSummaryBalance,
					resultSet.getBigDecimal("SeqNo"),
					resultSet.getTimestamp("StatementDate"),
					resultSet.getTimestamp("DateTrx"), 
					unReconciledDebit,
					unReconciledCredit,
					unReconciledSummaryBalance,
					availableAmount,
					balanceAmount
			}, get_TrxName());
		}
		return "Ok";
	}
}