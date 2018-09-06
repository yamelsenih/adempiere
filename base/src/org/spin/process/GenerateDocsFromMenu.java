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
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.I_AD_Browse;
import org.adempiere.model.MBrowse;
import org.compiere.model.I_AD_Form;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_Window;
import org.compiere.model.MForm;
import org.compiere.model.MMenu;
import org.compiere.model.MProcess;
import org.compiere.model.MWindow;
import org.compiere.model.Query;
import org.spin.util.docs.AbstractDocumentationSource;
import org.spin.util.docs.AbstractTextConverter;
import org.spin.util.docs.FunctionalDocsForForm;
import org.spin.util.docs.FunctionalDocsForMenu;
import org.spin.util.docs.FunctionalDocsForProcess;
import org.spin.util.docs.FunctionalDocsForSmartBrowse;
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
		loadProcess();
		loadWindow();
		loadForm();
		loadSmartBrowse();
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
			//	For Menu
			documentForMenu(menu);
			textConverter.clear();
		}
	}
	
	/**
	 * Load Process for documents
	 * @throws IOException 
	 */
	private void loadProcess() throws IOException {
		List<MProcess> processList = new Query(getCtx(), I_AD_Process.Table_Name, null, get_TrxName())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Get Result
		if(processList == null
				|| processList.size() == 0) {
			return;
		}
		//	
		for(MProcess process : processList) {
			//	For Process
			documentForProcess(process);
			textConverter.clear();
		}
	}
	
	/**
	 * Load window for documents
	 * @throws IOException 
	 */
	private void loadWindow() throws IOException {
		List<MWindow> windowList = new Query(getCtx(), I_AD_Window.Table_Name, null, get_TrxName())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Get Result
		if(windowList == null
				|| windowList.size() == 0) {
			return;
		}
		//	
		for(MWindow window : windowList) {
			//	For Window
			documentForWindow(window);
			textConverter.clear();
		}
	}
	
	/**
	 * Load Form for documents
	 * @throws IOException 
	 */
	private void loadForm() throws IOException {
		List<MForm> formList = new Query(getCtx(), I_AD_Form.Table_Name, null, get_TrxName())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Get Result
		if(formList == null
				|| formList.size() == 0) {
			return;
		}
		//	
		for(MForm form : formList) {
			//	For Window
			documentForForm(form);
			textConverter.clear();
		}
	}
	
	/**
	 * Load Smart Browse for documents
	 * @throws IOException 
	 */
	private void loadSmartBrowse() throws IOException {
		List<MBrowse> smartBrowseList = new Query(getCtx(), I_AD_Browse.Table_Name, null, get_TrxName())
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.list();
		//	Get Result
		if(smartBrowseList == null
				|| smartBrowseList.size() == 0) {
			return;
		}
		//	
		for(MBrowse smartBrowse : smartBrowseList) {
			//	For Window
			documentForSmartBrowse(smartBrowse);
			textConverter.clear();
		}
	}
	
	/**
	 * Create Document for menu
	 * @param process
	 * @throws IOException 
	 */
	private void documentForMenu(MMenu menu) throws IOException {
		AbstractDocumentationSource documentGenerator = new FunctionalDocsForMenu();
		boolean isOk = documentGenerator.createDocumentation(textConverter, menu);
		if(!isOk) { 
			return;
		}
		String menuFolderName = getDirectory() + File.separator + documentGenerator.getFolderName();
		File exportProcessDir = new File(menuFolderName);
		exportProcessDir.mkdirs();
		//	Create File
		File exportProcess = new File(menuFolderName + File.separator + getValidName(documentGenerator.getDocumentName()));
		FileWriter writer = new FileWriter(exportProcess);
		writer.write(textConverter.toString());
		writer.flush();
		writer.close();
		//	Add to list
		addLog("@AD_Menu_ID@ " + menu.getName());
		created++;
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
		String formFolderName = getDirectory() + File.separator + documentGenerator.getFolderName();
		File exportFormDir = new File(formFolderName);
		exportFormDir.mkdirs();
		//	Create File
		File exportProcess = new File(formFolderName + File.separator + getValidName(documentGenerator.getDocumentName()));
		FileWriter writer = new FileWriter(exportProcess);
		writer.write(textConverter.toString());
		writer.flush();
		writer.close();
		//	Add to list
		addLog("@AD_Window_ID@ " + window.getName());
		created++;
	}
	
	/**
	 * Create Document for Form
	 * @param form
	 * @throws IOException 
	 */
	private void documentForForm(MForm form) throws IOException {
		AbstractDocumentationSource documentGenerator = new FunctionalDocsForForm();
		boolean isOk = documentGenerator.createDocumentation(textConverter, form);
		if(!isOk) { 
			return;
		}
		String formFolderName = getDirectory() + File.separator + documentGenerator.getFolderName();
		File exportFormDir = new File(formFolderName);
		exportFormDir.mkdirs();
		//	Create File
		File exportProcess = new File(formFolderName + File.separator + getValidName(documentGenerator.getDocumentName()));
		FileWriter writer = new FileWriter(exportProcess);
		writer.write(textConverter.toString());
		writer.flush();
		writer.close();
		//	Add to list
		addLog("@AD_Form_ID@ " + form.getName());
		created++;
	}
	
	/**
	 * Create Document for Smart Browse
	 * @param smartBrowse
	 * @throws IOException 
	 */
	private void documentForSmartBrowse(MBrowse smartBrowse) throws IOException {
		AbstractDocumentationSource documentGenerator = new FunctionalDocsForSmartBrowse();
		boolean isOk = documentGenerator.createDocumentation(textConverter, smartBrowse);
		if(!isOk) { 
			return;
		}
		String smartBrowseFolderName = getDirectory() + File.separator + documentGenerator.getFolderName();
		File exportSmartBrowseDir = new File(smartBrowseFolderName);
		exportSmartBrowseDir.mkdirs();
		//	Create File
		File exportProcess = new File(smartBrowseFolderName + File.separator + getValidName(documentGenerator.getDocumentName()));
		FileWriter writer = new FileWriter(exportProcess);
		writer.write(textConverter.toString());
		writer.flush();
		writer.close();
		//	Add to list
		addLog("@AD_Browse_ID@ " + smartBrowse.getName());
		created++;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private String getValidName(String fileName) {
		//	
		return fileName + "." + textConverter.getExtension();
	}
}