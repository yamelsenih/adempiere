package org.spin.model;

import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.eevolution.model.X_HR_EmployeeInsurance;

public class Insurance_ModelValidator implements ModelValidator{

	
	
	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		// TODO Auto-generated method stub
		engine.addModelChange(X_HR_EmployeeInsurance.Table_Name, this);
	}

	@Override
	public int getAD_Client_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {

		if (po.get_Table_ID()==X_HR_EmployeeInsurance.Table_ID) {
			if (type == TYPE_BEFORE_NEW) {
				if (po.get_ValueAsInt("Ref_BPartner_ID")!=0) {
					MBPartner bPartner = new MBPartner(po.getCtx(), po.get_ValueAsInt("Ref_BPartner_ID"), po.get_TrxName());
					po.set_ValueOfColumn(X_HR_EmployeeInsurance.COLUMNNAME_SponsorName, bPartner.getName());
				}else
					return "@IsMandatory@ @Ref_BPartner_ID@";
			}
		}
		return null;
	}

	@Override
	public String docValidate(PO po, int timing) {
		// TODO Auto-generated method stub
		return null;
	}

}
