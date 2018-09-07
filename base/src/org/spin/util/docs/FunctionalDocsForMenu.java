/*************************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                              *
 * This program is free software; you can redistribute it and/or modify it    		 *
 * under the terms version 2 or later of the GNU General Public License as published *
 * by the Free Software Foundation. This program is distributed in the hope   		 *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 		 *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           		 *
 * See the GNU General Public License for more details.                       		 *
 * You should have received a copy of the GNU General Public License along    		 *
 * with this program; if not, write to the Free Software Foundation, Inc.,    		 *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     		 *
 * For the text or an alternative of this public license, you may reach us    		 *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com				  		                 *
 *************************************************************************************/
package org.spin.util.docs;

import java.util.ArrayList;
import java.util.List;

import org.compiere.model.MMenu;
import org.compiere.model.MProcess;
import org.compiere.model.MRefList;
import org.compiere.model.MTree;
import org.compiere.model.MWindow;
import org.compiere.model.PO;
import org.compiere.util.DB;
import org.compiere.util.Util;

/**
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 * Documentation generator for Menu entity
 * @see: https://github.com/adempiere/adempiere/issues/1934
 * For formst reference use: http://www.sphinx-doc.org/en/master/usage/restructuredtext/basics.html
 */
public class FunctionalDocsForMenu extends AbstractDocumentationSource {

	public FunctionalDocsForMenu() {
		//	 Constructor
	}

	/**	Document	*/
	private MMenu menu;
	/**	Folder Name	*/
	public static final String FOLDER_NAME = "menu";
	
	@Override
	public boolean createDocumentation(AbstractTextConverter textConverter, PO source) {
		menu = (MMenu) source;
		//	Add link from internal reference
		textConverter.addHeaderIndexName((getFolderName() + "-" + getDocumentName()).toLowerCase());
		//	Add Name
		textConverter.addSection(menu.getName());
		textConverter.newLine();
		//	Description
		if(!Util.isEmpty(menu.getDescription())) {
			textConverter.addText(menu.getDescription());
			textConverter.newLine();
		}
		List<MMenu> menuList = new ArrayList<MMenu>();
		int currentMenuId = menu.getAD_Menu_ID();
		int parentId = 0;
		int treeId = MTree.getDefaultAD_Tree_ID(menu.getAD_Client_ID(), "AD_Menu_ID");
		//	Add current
		menuList.add(menu);
		do {
			parentId = DB.getSQLValue(menu.get_TrxName(), "SELECT Parent_ID "
					+ "FROM AD_TreeNodeMM "
					+ "WHERE AD_Tree_ID = ? AND Node_ID = ?", treeId, currentMenuId);
			if(parentId > 0) {
				currentMenuId = parentId;
				menuList.add(MMenu.getFromId(menu.getCtx(), currentMenuId));
			}
		} while(parentId > 0);
		//	Write Path
		textConverter.addSubSection("Menu Path");
		for(int index = menuList.size() -1, level = 0; index >= 0; index--, level++) {
			MMenu menuItem = menuList.get(index);
			textConverter.addQuote(menuItem.getName(), level);
		}
		//	
		textConverter.newLine();
		//	Window Type
		textConverter.addSubSubSection("Menu Type");
		textConverter.addBold(MRefList.getListName(source.getCtx(), MMenu.ACTION_AD_Reference_ID, menu.getAction()));
		textConverter.newLine();
		//	Sales Transaction
		if(menu.isSOTrx()) {
			textConverter.addNote(getFeature(MWindow.COLUMNNAME_IsSOTrx));
		}
		textConverter.newLine();
		String internalReference = null;
		if(menu.getAction().equals(MMenu.ACTION_Process)) {
			internalReference = FunctionalDocsForProcess.FOLDER_NAME + "-" + MProcess.get(menu.getCtx(), menu.getAD_Process_ID()).getValue();
		} else if(menu.getAction().equals(MMenu.ACTION_Window)) {
			internalReference = FunctionalDocsForWindow.FOLDER_NAME + "-" + menu.getAD_Window().getName();
		}
		//	Validate null
		if(!Util.isEmpty(internalReference)) {
			textConverter.addSeeAlso(getValidValue(internalReference.toLowerCase()));
		}
		return true;
	}

	@Override
	public boolean createDocumentation(AbstractTextConverter textConverter) {
		return false;
	}
	
	@Override
	public boolean addIndex(AbstractTextConverter textConverter, PO source) {
		menu = (MMenu) source;
		textConverter.newLine();
		textConverter.addText(getDocumentName().toLowerCase(), 4);
		return true;
	}

	@Override
	public String getFolderName() {
		return FOLDER_NAME;
	}

	@Override
	public String getDocumentName() {
		if(menu != null) {
			return getValidValue(menu.getName());
		}
		//	
		return null;
	}

}
