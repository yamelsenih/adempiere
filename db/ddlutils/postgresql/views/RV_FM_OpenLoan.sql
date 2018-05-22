-- View: RV_FM_OpenLoan
-- DROP VIEW RV_FM_OpenLoan;
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
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************
 ***
 * Title:	Loan Open
 * Description:
 *	Show Loan 
 *
 * Test:
 * 	SELECT * FROM RV_FM_OpenLoan WHERE FM_Agreement_ID = 1000000;
 ************************************************************************/
CREATE OR REPLACE VIEW RV_FM_OpenLoan AS
SELECT am.AD_Client_ID, am.AD_Org_ID, am.IsActive, am.Created, am.CreatedBy, am.Updated, am.UpdatedBy, 
am.FM_Agreement_ID, am.FM_AgreementType_ID, am.C_DocType_ID, am.DocumentNo, am.DateDoc, 
am.DocStatus, am.FM_Product_ID, am.C_BPartner_ID, am.IsSOTrx, am.Status, 
am.FM_Account_ID, am.C_Currency_ID, am.AccountNo, 
ac.FeesQty, ac.PaymentFrequency, ac.PayDate, 
ac.CapitalAmt, 
ac.InterestAmt, 
ac.TaxAmt, 
SUM(COALESCE(am.CapitalAmt, 0) + COALESCE(am.InterestAmt, 0) + COALESCE(am.TaxAmt, 0)) AS FeeAmt,
SUM(COALESCE(am.CapitalAmt, 0)) AS CurrentCapitalAmt, 
SUM(COALESCE(am.CurrentInterestAmt, 0)) AS CurrentInterestAmt, 
SUM(COALESCE(am.CurrentTaxAmt, 0)) AS CurrentTaxAmt, 
SUM(COALESCE(am.CurrentDunningAmt, 0)) AS CurrentDunningAmt, 
SUM(COALESCE(am.CurrentDunningTaxAmt, 0)) AS CurrentDunningTaxAmt, 
SUM(COALESCE(am.CapitalAmt, 0) + COALESCE(am.CurrentInterestAmt, 0) + COALESCE(am.CurrentTaxAmt, 0) + COALESCE(am.CurrentDunningAmt, 0) + COALESCE(am.CurrentDunningTaxAmt, 0)) AS CurrentFeeAmt,
COUNT(am.FM_Amortization_ID) OpenFeesQty
FROM RV_FM_LoanAmortization am
INNER JOIN FM_Account ac ON(ac.FM_Account_ID = am.FM_Account_ID)
WHERE am.DocStatus = 'CO'
AND (am.IsInvoiced = 'N' OR am.IsPaid = 'N')
GROUP BY am.AD_Client_ID, am.AD_Org_ID, am.IsActive, am.Created, am.CreatedBy, am.Updated, am.UpdatedBy, 
am.FM_Agreement_ID, am.FM_AgreementType_ID, am.C_DocType_ID, am.DocumentNo, am.DateDoc, 
am.DocStatus, am.FM_Product_ID, am.C_BPartner_ID, am.IsSOTrx, am.Status, 
am.FM_Account_ID, am.C_Currency_ID, am.AccountNo, 
ac.FeesQty, ac.PaymentFrequency, ac.PayDate, ac.CapitalAmt, ac.InterestAmt, ac.TaxAmt
