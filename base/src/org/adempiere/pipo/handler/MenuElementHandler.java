/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 Adempiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *
 * Copyright (C) 2005 Robert Klein. robeklein@hotmail.com
 * Contributor(s): Low Heng Sin hengsin@avantz.com
 *****************************************************************************/
package org.adempiere.pipo.handler;

import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.PackOut;
import org.compiere.model.I_AD_Menu;
import org.compiere.model.MMenu;
import org.compiere.model.MTree;
import org.compiere.model.MTree_NodeMM;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.xml.sax.SAXException;

/**
 * Improve Export Menu
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class MenuElementHandler extends GenericPOHandler {
	@Override
	public void create(Properties ctx, TransformerHandler document)
			throws SAXException {
		int menuId = Env.getContextAsInt(ctx, "AD_Menu_ID");
		createApplication(ctx, document, menuId);
	}

	/**
	 * Create Application
	 * @param ctx
	 * @param document
	 * @param menuId
	 * @throws SAXException
	 */
	private void createApplication(Properties ctx, TransformerHandler document, int menuId) throws SAXException {
		PackOut packOut = (PackOut)ctx.get("PackOutProcess");
		MMenu menu = MMenu.getFromId(ctx, menuId);
		int defaultTreeId = MTree.getDefaultTreeIdFromTableId(menu.getAD_Client_ID(), I_AD_Menu.Table_ID);
		MTree_NodeMM node = MTree_NodeMM.get(MTree.get(Env.getCtx(), defaultTreeId, null), menu.getAD_Menu_ID());
		if(menu.isSummary()) {
			//	Create Menu
			packOut.createGenericPO(document, menu);
			//	Create Node
			packOut.createGenericPO(document, node);
			String childSQL = "SELECT m.AD_Menu_ID "
					+ "FROM AD_Menu m "
					+ "WHERE EXISTS(SELECT 1 FROM AD_TreeNodeMM tnm "
					+ "			WHERE tnm.Node_ID = m.AD_Menu_ID "
					+ "			AND tnm.AD_Tree_ID = " + defaultTreeId + " "
					+ "			AND tnm.Parent_ID = ?)";
			int [] ids = DB.getIDsEx(null, childSQL, menu.getAD_Menu_ID());
			for(int id : ids) {
				//	Recursive call
				createApplication(ctx, document, id);
			}
		} else if (menu.getAD_Window_ID() > 0
				|| menu.getAD_Workflow_ID() > 0
				|| menu.getAD_Task_ID() > 0
				|| menu.getAD_Process_ID() > 0
				|| menu.getAD_Form_ID() > 0
				|| menu.getAD_Browse_ID() > 0
				|| menu.getAD_Workbench_ID() > 0) {
			// Call CreateWindow.
			if (menu.getAD_Window_ID() > 0) {
				packOut.createWindow(menu.getAD_Window_ID(), document);
			}
			// Call CreateProcess.
			else if (menu.getAD_Process_ID() > 0) {
				packOut.createProcess(menu.getAD_Process_ID(), document);
			}
			// Call CreateTask.
			else if (menu.getAD_Task_ID() > 0) {
				packOut.createTask(menu.getAD_Task_ID(), document);
			}
			// Call CreateForm.
			else if (menu.getAD_Form_ID() > 0) {
				packOut.createForm(menu.getAD_Form_ID(), document);
			}
			// Call CreateBrowse.
			else if (menu.getAD_Browse_ID() > 0) {
				packOut.createBrowse(menu.getAD_Browse_ID(), document);
			}
			// Call CreateWorkflow
			else if (menu.getAD_Workflow_ID() > 0) {
				packOut.createWorkflow(menu.getAD_Workflow_ID(), 
						document);
			}
			//	Create Menu
			packOut.createGenericPO(document, menu);
			//	Create Node
			packOut.createGenericPO(document, node);
			// Call CreateModule because entry is a summary menu
		}
	}
}
