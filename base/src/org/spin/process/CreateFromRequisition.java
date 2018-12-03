package org.spin.process;

import org.compiere.model.MRequisitionLine;
import org.compiere.model.MRfQ;
import org.compiere.model.MRfQLine;
import org.compiere.model.MRfQLineQty;

public class CreateFromRequisition extends CreateFromRequisitionAbstract {
	
	private int lines = 0;
	
	@Override
	protected void prepare() {
//		super.prepare();
		}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
				
		MRequisitionLine rl = null;
		int line = 10;
		for(Integer key : getSelectionKeys()) {
			lines++;	
			
			rl = new MRequisitionLine(getCtx(), getSelectionAsInt(key, "L_M_RequisitionLine_ID"), get_TrxName());
			System.out.println(getSelectionValues());
			

			MRfQ rfq = new MRfQ(getCtx(),getRecord_ID(),get_TrxName());
			MRfQLine rfqline = new MRfQLine(rfq);
			rfqline.setM_Product_ID(rl.getM_Product_ID());
			rfqline.setDescription(rl.getDescription());
			rfqline.setLine(line*lines);
			rfqline.setM_AttributeSetInstance_ID(rl.getM_AttributeSetInstance_ID());;
			rfqline.saveEx();
		
			MRfQLineQty rfqlineqty = new MRfQLineQty(rfqline);
			rfqlineqty.setQty(rl.getQty());
			
			rfqlineqty.setC_UOM_ID(rl.getC_UOM_ID());
			rfqlineqty.setIsPurchaseQty(true);
			rfqlineqty.set_ValueOfColumn("M_RequisitionLine_ID", rl.get_ID());
			rfqlineqty.saveEx(get_TrxName());
		}
				
		return "@Created@ " + lines;
	}
}
