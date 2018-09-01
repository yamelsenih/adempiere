/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.									  *
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
package org.spin.process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MMenu;
import org.compiere.model.MProcess;
import org.compiere.model.MTree;
import org.compiere.model.MWindow;
import org.compiere.util.DB;
import org.compiere.util.Util;
import org.spin.util.docs.AbstractTextConverter;
import org.spin.util.docs.AbstractDocumentationSource;
import org.spin.util.docs.FunctionalDocsForProcess;
import org.spin.util.docs.FunctionalDocsForWindow;

/**
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 * reStructuredText converter, it can be used for export a simple String to reStructuredText format
 * @see: https://github.com/adempiere/adempiere/issues/1934
 * For formst reference use: http://www.sphinx-doc.org/en/master/usage/restructuredtext/basics.html
 */
public class GenerateDocsFromMenu extends GenerateDocsFromMenuAbstract {
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	/**	Converter	*/
	private AbstractTextConverter textConverter = null;
	/**	Created	*/
	private int created = 0;

	@Override
	protected String doIt() throws Exception {
		Class<?> clazz = Class.forName(getDocsExportFormat());
		if (!AbstractTextConverter.class.isAssignableFrom(clazz)) {
			throw new AdempiereException("@DocsExportFormat@ @NotMatched@");
		}
		//	Instance
		textConverter = (AbstractTextConverter) clazz.newInstance();
		loadMenu();
		return "@Created@ " + created;
	}
	
	/**
	 * Load Menu for documents
	 * @throws IOException 
	 */
	private void loadMenu() throws IOException {
		StringBuffer whereClause = new StringBuffer("AD_Client_ID = " + getAD_Client_ID() + " AND IsSummary = 'N'");
		if(getRecord_ID() > 0) {
			whereClause.append(" AND ").append("AD_Menu_ID = ").append(getRecord_ID());
		}
		//	Get Result
		MMenu menuList[] = MMenu.get(getCtx(), whereClause.toString(), get_TrxName());
		if(menuList == null
				|| menuList.length == 0) {
			return;
		}
		//	
		for(MMenu menu : menuList) {
			//	Write File
			textConverter.clear();
			//	Add Name
			textConverter.addSection(menu.getName());
			textConverter.newLine();
			//	Description
			if(!Util.isEmpty(menu.getDescription())) {
				textConverter.addText(menu.getDescription());
				textConverter.newLine();
			}
			//	Path
			writePath(menu);
			//	
			if(menu.getAction().equals(MMenu.ACTION_Process)) {
				documentForProcess(MProcess.get(getCtx(), menu.getAD_Process_ID()));
			} else if(menu.getAction().equals(MMenu.ACTION_Window)) {
				documentForWindow((MWindow) menu.getAD_Window());
			}
		}
	}
	
	/**
	 * Write Path from menu
	 * @param menu
	 */
	private void writePath(MMenu menu) {
		List<MMenu> menuList = new ArrayList<MMenu>();
		int currentMenuId = menu.getAD_Menu_ID();
		int parentId = 0;
		int treeId = MTree.getDefaultAD_Tree_ID(getAD_Client_ID(), "AD_Menu_ID");
		//	Add current
		menuList.add(menu);
		do {
			parentId = DB.getSQLValue(get_TrxName(), "SELECT Parent_ID "
					+ "FROM AD_TreeNodeMM "
					+ "WHERE AD_Tree_ID = ? AND Node_ID = ?", treeId, currentMenuId);
			if(parentId > 0) {
				currentMenuId = parentId;
				menuList.add(MMenu.getFromId(getCtx(), currentMenuId));
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
	}
	
	/**
	 * Create Document for process
	 * @param process
	 * @throws IOException 
	 */
	private void documentForProcess(MProcess process) throws IOException {
		AbstractDocumentationSource documentGenerator = new FunctionalDocsForProcess();
		boolean isOk = documentGenerator.createDocumentation(textConverter, process);
		if(!isOk) { 
			return;
		}
		String processFolderName = getDirectory() + File.separator + documentGenerator.getFolderName();
		File exportProcessDir = new File(processFolderName);
		exportProcessDir.mkdirs();
		//	Create File
		File exportProcess = new File(processFolderName + File.separator + getValidName(documentGenerator.getDocumentName()));
		FileWriter writer = new FileWriter(exportProcess);
		writer.write(textConverter.toString());
		writer.flush();
		writer.close();
		//	Add to list
		addLog("@AD_Process_ID@ " + process.getName());
		created++;
	}
	
	/**
	 * Create Document for Window
	 * @param window
	 * @throws IOException 
	 */
	private void documentForWindow(MWindow window) throws IOException {
		AbstractDocumentationSource documentGenerator = new FunctionalDocsForWindow();
		boolean isOk = documentGenerator.createDocumentation(textConverter, window);
		if(!isOk) { 
			return;
		}
		String processFolderName = getDirectory() + File.separator + documentGenerator.getFolderName();
		File exportProcessDir = new File(processFolderName);
		exportProcessDir.mkdirs();
		//	Create File
		File exportProcess = new File(processFolderName + File.separator + getValidName(documentGenerator.getDocumentName()));
		FileWriter writer = new FileWriter(exportProcess);
		writer.write(textConverter.toString());
		writer.flush();
		writer.close();
		//	Add to list
		addLog("@AD_Window_ID@ " + window.getName());
		created++;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private String getValidName(String fileName) {
		fileName = fileName.replace(" ", "").trim();
		fileName = fileName.replaceAll("[+^:&áàäéèëíìïóòöúùñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ$,;*/]", "");
		//	
		return fileName + "." + textConverter.getExtension();
	}
}