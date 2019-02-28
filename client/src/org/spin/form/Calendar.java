package org.spin.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.compiere.model.MTimeExpense;
import org.compiere.model.MTimeExpenseLine;
import org.compiere.process.ProcessInfo;
import org.compiere.util.Env;

public class Calendar {

	private int timeExpenseID = 0; 
	/**
     * Set from PO
     * @param processInfo
     */
    public void setFromPO(ProcessInfo processInfo) {
        if(processInfo != null
                && processInfo.getTable_ID() > 0
                && processInfo.getRecord_ID() > 0) {
        	MTimeExpense expense = new MTimeExpense(Env.getCtx(), processInfo.getRecord_ID(), processInfo.getTransactionName());
            timeExpenseID = expense.getS_TimeExpense_ID();        
            }
    }
	// Get Date From Table to Show on Calendar
	public void getData() {
		
	}
	
	// Save Values from Calendar
	public void saveData(BigDecimal hours, Date date, String description, String Note) {
		Timestamp setDate = new Timestamp(date.getTime());
		MTimeExpense expense = new MTimeExpense(Env.getCtx(), timeExpenseID, null);
		expense.setDateReport(setDate);
		MTimeExpenseLine expenseLine = new MTimeExpenseLine(Env.getCtx(), expense.getS_TimeExpense_ID(), null);
		expenseLine.setDescription(description);
		expenseLine.setM_Product_ID(50026);
		expenseLine.setNote(Note);
		expenseLine.setQty(hours);
		
		expenseLine.save();
		expense.save();
	}		
	
}
