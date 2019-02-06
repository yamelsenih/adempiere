package org.spin.form;

import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WLocatorEditor;
import org.adempiere.webui.editor.WNumberEditor;
import org.adempiere.webui.editor.WStringEditor;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.process.ProcessInfo;
import org.zkoss.calendar.*;
import org.zkoss.zul.North;

public class WCalendar implements IFormController {

	@Override
	public ADForm getForm() {
		// TODO Auto-generated method stub
		return form;
	}
	

public ProcessInfo getProcessInfo()
{
	return getForm().getProcessInfo();
}



protected CustomForm form = new CustomForm();
// new panel
/** Grid for components*/

public WCalendar()
{
    super();
    getForm();
	Calendars calendar = new Calendars();
	form.appendChild(calendar);
	
	
}

}
