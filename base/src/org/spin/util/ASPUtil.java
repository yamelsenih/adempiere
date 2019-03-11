/*************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                              *
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
package org.spin.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MField;
import org.compiere.model.MFieldCustom;
import org.compiere.Adempiere;
import org.compiere.model.I_AD_Field;
import org.compiere.model.I_AD_Process;
import org.compiere.model.I_AD_ProcessCustom;
import org.compiere.model.I_AD_Process_Para;
import org.compiere.model.I_AD_Tab;
import org.compiere.model.I_AD_Window;
import org.compiere.model.I_AD_WindowCustom;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessCustom;
import org.compiere.model.MProcessPara;
import org.compiere.model.MProcessParaCustom;
import org.compiere.model.MTab;
import org.compiere.model.MTabCustom;
import org.compiere.model.MTable;
import org.compiere.model.MWindow;
import org.compiere.model.MWindowCustom;
import org.compiere.model.Query;
import org.compiere.util.CCache;
import org.compiere.util.Env;
import org.compiere.util.Util;

/**
 * Class for handle ASP Util as wrapper for standard process, window and smart browse
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class ASPUtil {
	
	/**	Instance	*/
	private static ASPUtil instance = null;
	/**	Client	*/
	private int clientId;
	/**	Role	*/
	private int roleId;
	/**	User	*/
	private int userId;
	/**	Context	*/
	private Properties context;
	/**	Process	Cache */
	private static CCache<String, MProcess> processCache = new CCache<String, MProcess>(I_AD_Process.Table_Name, 20);
	/**	Process	Parameter Cache */
	private static CCache<String, List<MProcessPara>> processParameterCache = new CCache<String, List<MProcessPara>>(I_AD_Process_Para.Table_Name, 20);
	/**	Window	Cache */
	private static CCache<String, MWindow> windowCache = new CCache<String, MWindow>(I_AD_Window.Table_Name, 20);
	/**	Tab	Cache */
	private static CCache<String, List<MTab>> tabCache = new CCache<String, List<MTab>>(I_AD_Tab.Table_Name, 20);
	/**	Field	Cache */
	private static CCache<String, List<MField>> fieldCache = new CCache<String, List<MField>>(I_AD_Field.Table_Name, 20);
	
	/**	Level	*/
	private final int CLIENT = 1;
	private final int ROLE = 2;
	private final int USER = 3;
	
	/**
	 * Private constructor
	 */
	private ASPUtil(Properties context, int clientId, int roleId, int userId) {
		if(context == null) {
			throw new AdempiereException("@ContextIsMandatory@");
		}
		this.context = context;
		this.clientId = clientId; 
		this.roleId = roleId;
		this.userId = userId;
	}
	
	/**
	 * Get instance from context
	 * @param context
	 * @return
	 */
	public static ASPUtil getInstance(Properties context) {
		return getInstance(context, Env.getAD_Client_ID(context), Env.getAD_Role_ID(context), Env.getAD_User_ID(context));
	}
	
	/**
	 * Get instance for ASP
	 * @param context
	 * @return
	 */
	public static ASPUtil getInstance(Properties context, int clientId, int roleId, int userId) {
		if(instance == null) {
			instance = new ASPUtil(context, clientId, roleId, userId);
		}
		//	
		return instance;
	}
	
	/**
	 * Get instance without context
	 * @return
	 */
	public static ASPUtil getInstance() {
		return getInstance(Env.getCtx());
	}
	
	/**
	 * Get process for User / Role / Client / Dictionary
	 * @param processId
	 * @return
	 */
	public MProcess getProcess(int processId) {
		//	User level
		MProcess process = processCache.get(getUserKey(processId, userId));
		//	Role Level
		if(process == null) {
			process = processCache.get(getRoleKey(processId, roleId));
		}
		//	Client Level (ASP)
		if(process == null) {
			process = processCache.get(getClientKey(processId, clientId));
		}
		//	Dictionary Level Base
		if(process == null) {
			process = processCache.get(getDictionaryKey(processId));
		}
		//	Reload
		if(process == null) {
			process = getProcessForASP(processId);
		}
		return process;
	}
	
	/**
	 * Get window for User / Role / Client / Dictionary
	 * @param windowId
	 * @return
	 */
	public MWindow getWindow(int windowId) {
		//	User level
		MWindow window = windowCache.get(getUserKey(windowId, userId));
		//	Role Level
		if(window == null) {
			window = windowCache.get(getRoleKey(windowId, roleId));
		}
		//	Client Level (ASP)
		if(window == null) {
			window = windowCache.get(getClientKey(windowId, clientId));
		}
		//	Dictionary Level Base
		if(window == null) {
			window = windowCache.get(getDictionaryKey(windowId));
		}
		//	Reload
		if(window == null) {
			window = getWindowForASP(windowId);
		}
		return window;
	}
	
	/**
	 * Get Tabs of window
	 * @param windowId
	 * @return
	 */
	public List<MTab> getTabs(int windowId) {
		//	User level
		if(windowCache.get(getUserKey(windowId, userId)) != null) {
			return tabCache.get(getUserKey(windowId, userId));
		}
		//	Role Level
		if(windowCache.get(getRoleKey(windowId, roleId)) != null) {
			return tabCache.get(getRoleKey(windowId, roleId));
		}
		//	Client Level (ASP)
		if(windowCache.get(getClientKey(windowId, clientId)) != null) {
			return tabCache.get(getClientKey(windowId, clientId));
		}
		//	Dictionary Level Base
		return tabCache.get(getDictionaryKey(windowId));
	}
	
	/**
	 * Get Tabs of window
	 * @param tabId
	 * @return
	 */
	public List<MField> getFields(int tabId) {
		//	User level
		if(windowCache.get(getUserKey(tabId, userId)) != null) {
			return fieldCache.get(getUserKey(tabId, userId));
		}
		//	Role Level
		if(windowCache.get(getRoleKey(tabId, roleId)) != null) {
			return fieldCache.get(getRoleKey(tabId, roleId));
		}
		//	Client Level (ASP)
		if(windowCache.get(getClientKey(tabId, clientId)) != null) {
			return fieldCache.get(getClientKey(tabId, clientId));
		}
		//	Dictionary Level Base
		return fieldCache.get(getDictionaryKey(tabId));
	}
	
	/**
	 * Get Process Parameter
	 * @param processId
	 * @return
	 */
	public List<MProcessPara> getProcessParameters(int processId) {
		//	User level
		if(processCache.get(getUserKey(processId, userId)) != null) {
			return processParameterCache.get(getUserKey(processId, userId));
		}
		//	Role Level
		if(processCache.get(getRoleKey(processId, roleId)) != null) {
			return processParameterCache.get(getRoleKey(processId, roleId));
		}
		//	Client Level (ASP)
		if(processCache.get(getClientKey(processId, clientId)) != null) {
			return processParameterCache.get(getClientKey(processId, clientId));
		}
		//	Dictionary Level Base
		return processParameterCache.get(getDictionaryKey(processId));
	}
	
	/**
	 * Get / Load process for ASP
	 * @param processId
	 * @return
	 */
	private MProcess getProcessForASP(int processId) {
		MProcess process = MProcess.get(context, processId);
		if(process == null) {
			return process;
		}
		//	Save dictionary
		processCache.put(getDictionaryKey(processId), process);
		//	Old compatibility
		MTable newTable = MTable.get(context, I_AD_ProcessCustom.Table_ID);
		if(newTable == null
				|| Util.isEmpty(newTable.getTableName())) {
			loadProcessParameters(process);
			return process;
		}
		//	Merge Process for client (ASP)
		process = getClientProcess(process);
		//	Merge Process for role
		process = getRoleProcess(process);
		//	Merge Process for user
		process = getUserProcess(process);
		//	
		return process;
	}
	
	/**
	 * Get / Load process for ASP
	 * @param windowId
	 * @return
	 */
	private MWindow getWindowForASP(int windowId) {
		MWindow window = MWindow.get(context, windowId);
		//	Save dictionary
		windowCache.put(getDictionaryKey(windowId), window);
		//	Old compatibility
		MTable newTable = MTable.get(context, I_AD_WindowCustom.Table_ID);
		if(newTable == null
				|| Util.isEmpty(newTable.getTableName())) {
			loadWindowTabs(window);
			return window;
		}
		//	Merge Window for client (ASP)
		window = getClientWindow(window);
		//	Merge Window for role
		window = getRoleWindow(window);
		//	Merge Window for user
		window = getUserWindow(window);
		//	
		return window;
	}
	
	/**
	 * Merge parameters with custom parameters
	 * @param processParameters
	 * @param customProcessParameters
	 * @param overwrite
	 */
	private List<MProcessPara> mergeParameters(List<MProcessPara> processParameters, List<MProcessParaCustom> customProcessParameters, boolean overwrite) {
		List<MProcessPara> mergedParameters = null;
		if(overwrite) {
			mergedParameters = new ArrayList<>();
			for(MProcessPara parameter : processParameters) {
				MProcessPara parameterToAdd = parameter.getDuplicated();
				parameterToAdd.setIsActive(false);
				mergedParameters.add(parameterToAdd);
			}
		} else {
			mergedParameters = new ArrayList<>(processParameters);
		}
		//	merge all parameters
		for(int index = 0; index < mergedParameters.size(); index++) {
			MProcessPara parameter = mergedParameters.get(index);
			customProcessParameters.stream()
			.filter(customParameter -> customParameter.getAD_Process_Para_ID() == parameter.getAD_Process_Para_ID())
			.forEach(customParameter -> {
				mergeProcessParameter(parameter, customParameter);
			});
			mergedParameters.set(index, parameter);
		}
		//
		return mergedParameters;
	}
	
	/**
	 * Merge parameters with custom parameters
	 * @param windowTabs
	 * @param customWindowTabs
	 * @param overwrite
	 */
	private List<MTab> mergeTabs(List<MTab> windowTabs, List<MTabCustom> customWindowTabs, boolean overwrite, int level) {
		List<MTab> mergedTabs = null;
		if(overwrite) {
			mergedTabs = new ArrayList<>();
			for(MTab parameter : windowTabs) {
				MTab tabToAdd = parameter.getDuplicated();
				tabToAdd.setIsActive(false);
				mergedTabs.add(tabToAdd);
			}
		} else {
			mergedTabs = new ArrayList<>(windowTabs);
		}
		//	merge all parameters
		for(int index = 0; index < mergedTabs.size(); index++) {
			MTab tab = mergedTabs.get(index);
			customWindowTabs.stream()
			.filter(customTab -> customTab.getAD_Tab_ID() == tab.getAD_Tab_ID())
			.forEach(customTab -> {
				mergeTab(tab, customTab, overwrite, level);
			});
			mergedTabs.set(index, tab);
		}
		//
		return mergedTabs;
	}
	
	/**
	 * Load process parameters
	 * @param process
	 */
	private List<MProcessPara> loadProcessParameters(MProcess process) {
		List<MProcessPara> parameters = processParameterCache.get(getDictionaryKey(process.getAD_Process_ID()));
		if(parameters != null) {
			return parameters;
		}
		processParameterCache.put(getDictionaryKey(process.getAD_Process_ID()), process.getParametersAsList());
		//	ASP Client
		parameters = process.getASPParameters();
		processParameterCache.put(getClientKey(process.getAD_Process_ID(), clientId), parameters);
		processParameterCache.put(getRoleKey(process.getAD_Process_ID(), roleId), parameters);
		processParameterCache.put(getUserKey(process.getAD_Process_ID(), userId), parameters);
		return parameters;
	}
	
	/**
	 * Load window tabs
	 * @param window
	 */
	private List<MTab> loadWindowTabs(MWindow window) {
		List<MTab> tabs = tabCache.get(getDictionaryKey(window.getAD_Window_ID()));
		if(tabs != null) {
			return tabs;
		}
		//	Tab List
		tabs = window.getASPTabs();
		tabCache.put(getDictionaryKey(window.getAD_Window_ID()), tabs);
		//	ASP Client
		//	TODO: tabs = window.getASPParameters();
		tabCache.put(getClientKey(window.getAD_Window_ID(), clientId), tabs);
		tabCache.put(getRoleKey(window.getAD_Window_ID(), roleId), tabs);
		tabCache.put(getUserKey(window.getAD_Window_ID(), userId), tabs);
		return tabs;
	}
	
	/**
	 * Load fields
	 * @param tab
	 */
	private List<MField> loadFields(MTab tab) {
		List<MField> fields = fieldCache.get(getDictionaryKey(tab.getAD_Tab_ID()));
		if(fields != null) {
			return fields;
		}
		//	Tab List
		fields = Arrays.asList(tab.getFields(false, null));
		fieldCache.put(getDictionaryKey(tab.getAD_Tab_ID()), fields);
		//	ASP Client
		//	TODO: tabs = window.getASPParameters();
		fieldCache.put(getClientKey(tab.getAD_Tab_ID(), clientId), fields);
		fieldCache.put(getRoleKey(tab.getAD_Tab_ID(), roleId), fields);
		fieldCache.put(getUserKey(tab.getAD_Tab_ID(), userId), fields);
		return fields;
	}
	
	
	/**
	 * Window for Client of ASP
	 * @param windowId
	 * @return
	 */
	private List<MWindowCustom> getClientWindowList(int windowId) {
		String whereClause = "EXISTS(SELECT 1 FROM ASP_ClientLevel cl "
				+ "WHERE cl.AD_Client_ID = ? "
				+ "AND cl.ASP_Level_ID = AD_WindowCustom.ASP_Level_ID) "
				+ "AND AD_Window_ID = ?";
		//	Get
		return new Query(context, I_AD_WindowCustom.Table_Name, whereClause, null)
				.setParameters(clientId, windowId)
				.setOnlyActiveRecords(true)
				.list();
	}
	
	/**
	 * Get client process list for ASP
	 * @param processId
	 * @return
	 */
	private List<MProcessCustom> getClientProcessList(int processId) {
		String whereClause = "EXISTS(SELECT 1 FROM ASP_ClientLevel cl "
				+ "WHERE cl.AD_Client_ID = ? "
				+ "AND cl.ASP_Level_ID = AD_ProcessCustom.ASP_Level_ID) "
				+ "AND AD_Process_ID = ?";
		//	Get
		return new Query(context, I_AD_ProcessCustom.Table_Name, whereClause, null)
				.setParameters(clientId, processId)
				.setOnlyActiveRecords(true)
				.list();
	}
	
	/**
	 * Get role window list for ASP
	 * @return
	 */
	private List<MWindowCustom> getRoleWindowList() {
		String whereClause = "AD_Role_ID = ?";
		//	Get
		return new Query(context, I_AD_WindowCustom.Table_Name, whereClause, null)
				.setParameters(roleId)
				.setOnlyActiveRecords(true)
				.list();
	}
	
	/**
	 * Get role process list for ASP
	 * @return
	 */
	private List<MProcessCustom> getRoleProcessList() {
		String whereClause = "AD_Role_ID = ?";
		//	Get
		return new Query(context, I_AD_ProcessCustom.Table_Name, whereClause, null)
				.setParameters(roleId)
				.setOnlyActiveRecords(true)
				.list();
	}
	
	/**
	 * Get user process list for ASP
	 * @return
	 */
	private List<MProcessCustom> getUserProcessList() {
		String whereClause = "AD_User_ID = ?";
		//	Get
		return new Query(context, I_AD_ProcessCustom.Table_Name, whereClause, null)
				.setParameters(userId)
				.setOnlyActiveRecords(true)
				.list();
	}
	
	/**
	 * Get user window list for ASP
	 * @return
	 */
	private List<MWindowCustom> getUserWindowList() {
		String whereClause = "AD_User_ID = ?";
		//	Get
		return new Query(context, I_AD_WindowCustom.Table_Name, whereClause, null)
				.setParameters(userId)
				.setOnlyActiveRecords(true)
				.list();
	}
	
	/**
	 * Get client window from dictionary process
	 * @param window
	 * @return
	 */
	private MWindow getClientWindow(MWindow window) {
		List<MTab> clientTabs = loadWindowTabs(window);
		MWindow clientWindow = window.getDuplicated();
		List<MWindowCustom> customWindowList = getClientWindowList(window.getAD_Window_ID());
		if(customWindowList != null
				&& customWindowList.size() > 0) {
			for(MWindowCustom customWindow : customWindowList) {
				mergeWindow(clientWindow, customWindow);
				//	Merge parameters
				clientTabs = mergeTabs(clientTabs, customWindow.getTabs(), customWindow.getHierarchyType().equals(MWindowCustom.HIERARCHYTYPE_Overwrite), CLIENT);
			}
			//	Save client
			windowCache.put(getClientKey(window.getAD_Window_ID(), clientId), clientWindow);
			tabCache.put(getClientKey(window.getAD_Window_ID(), clientId), clientTabs);
		}
		//	return
		return clientWindow;
	}
	
	/**
	 * Get client process from dictionary process
	 * @param process
	 * @return
	 */
	private MProcess getClientProcess(MProcess process) {
		List<MProcessPara> clientParameters = loadProcessParameters(process);
		MProcess clientProcess = process.getDuplicated();
		List<MProcessCustom> customProcessList = getClientProcessList(process.getAD_Process_ID());
		if(customProcessList != null
				&& customProcessList.size() > 0) {
			for(MProcessCustom customProcess : customProcessList) {
				mergeProcess(clientProcess, customProcess);
				//	Merge parameters
				clientParameters = mergeParameters(clientParameters, customProcess.getParameters(), customProcess.getHierarchyType().equals(MProcessCustom.HIERARCHYTYPE_Overwrite));
			}
			//	Save client
			processCache.put(getClientKey(process.getAD_Process_ID(), clientId), clientProcess);
			processParameterCache.put(getClientKey(process.getAD_Process_ID(), clientId), clientParameters);
		}
		//	return
		return clientProcess;
	}
	
	/**
	 * Get / Merge process for role
	 * @param process
	 * @return
	 */
	private MProcess getRoleProcess(MProcess process) {
		MProcess roleProcess = process.getDuplicated();
		List<MProcessPara> roleParameters = processParameterCache.get(getRoleKey(process.getAD_Process_ID(), roleId));
		List<MProcessCustom> customProcessList = getRoleProcessList();
		if(customProcessList != null
				&& customProcessList.size() > 0) {
			for(MProcessCustom customProcess : customProcessList) {
				mergeProcess(roleProcess, customProcess);
				//	Merge parameters
				roleParameters = mergeParameters(roleParameters, customProcess.getParameters(), customProcess.getHierarchyType().equals(MProcessCustom.HIERARCHYTYPE_Overwrite));
			}
			//	Save role
			processCache.put(getRoleKey(process.getAD_Process_ID(), roleId), roleProcess);
			processParameterCache.put(getRoleKey(process.getAD_Process_ID(), roleId), roleParameters);
		}
		//	return
		return roleProcess;
	}
	
	/**
	 * Get / Merge window for role
	 * @param window
	 * @return
	 */
	private MWindow getRoleWindow(MWindow window) {
		MWindow roleWindow = window.getDuplicated();
		List<MTab> roleTabs = tabCache.get(getRoleKey(window.getAD_Window_ID(), roleId));
		List<MWindowCustom> customWindowList = getRoleWindowList();
		if(customWindowList != null
				&& customWindowList.size() > 0) {
			for(MWindowCustom customWindow : customWindowList) {
				mergeWindow(roleWindow, customWindow);
				//	Merge parameters
				roleTabs = mergeTabs(roleTabs, customWindow.getTabs(), customWindow.getHierarchyType().equals(MProcessCustom.HIERARCHYTYPE_Overwrite), ROLE);
			}
			//	Save role
			windowCache.put(getRoleKey(window.getAD_Window_ID(), roleId), roleWindow);
			tabCache.put(getRoleKey(window.getAD_Window_ID(), roleId), roleTabs);
		}
		//	return
		return roleWindow;
	}
	
	
	/**
	 * Get / Merge process for user
	 * @param process
	 * @return
	 */
	private MProcess getUserProcess(MProcess process) {
		MProcess userProcess = process.getDuplicated();
		List<MProcessPara> userParameters = processParameterCache.get(getUserKey(process.getAD_Process_ID(), userId));
		List<MProcessCustom> customProcessList = getUserProcessList();
		if(customProcessList != null
				&& customProcessList.size() > 0) {
			for(MProcessCustom customProcess : customProcessList) {
				mergeProcess(userProcess, customProcess);
				//	Merge parameters
				userParameters = mergeParameters(userParameters, customProcess.getParameters(), customProcess.getHierarchyType().equals(MProcessCustom.HIERARCHYTYPE_Overwrite));
			}
			//	Save user
			processCache.put(getUserKey(process.getAD_Process_ID(), userId), userProcess);
			processParameterCache.put(getUserKey(process.getAD_Process_ID(), userId), userParameters);
		}
		//	return
		return userProcess;
	}
	
	/**
	 * Get / Merge window for user
	 * @param process
	 * @return
	 */
	private MWindow getUserWindow(MWindow process) {
		MWindow userWindow = process.getDuplicated();
		List<MTab> userTabs = tabCache.get(getUserKey(process.getAD_Window_ID(), userId));
		List<MWindowCustom> customProcessList = getUserWindowList();
		if(customProcessList != null
				&& customProcessList.size() > 0) {
			for(MWindowCustom customProcess : customProcessList) {
				mergeWindow(userWindow, customProcess);
				//	Merge parameters
				userTabs = mergeTabs(userTabs, customProcess.getTabs(), customProcess.getHierarchyType().equals(MProcessCustom.HIERARCHYTYPE_Overwrite), USER);
			}
			//	Save user
			windowCache.put(getUserKey(process.getAD_Window_ID(), userId), userWindow);
			tabCache.put(getUserKey(process.getAD_Window_ID(), userId), userTabs);
		}
		//	return
		return userWindow;
	}
	
	/**
	 * Get Client Key from object Id
	 * @param objectIdmergePO
	 * @param clientId
	 * @return
	 */
	private String getClientKey(int objectId, int clientId) {
		return objectId + "|C|" + clientId;
	}
	
	/**
	 * Get Role Key from object Id
	 * @param objectId
	 * @param roleId
	 * @return
	 */
	private String getRoleKey(int objectId, int roleId) {
		return objectId + "|R|" + roleId;
	}
	
	/**
	 * Get User Key from object Id
	 * @param objectId
	 * @param userId
	 * @return
	 */
	private String getUserKey(int objectId, int userId) {
		return objectId + "|U|" + userId;
	}
	
	/**
	 * Get dictionary Key
	 * @param objectId
	 * @return
	 */
	private String getDictionaryKey(int objectId) {
		return String.valueOf(objectId);
	}
	
	/**
	 * Merge Process with custom process
	 * @param process
	 * @param customProcess
	 */
	private void mergeProcess(MProcess process, MProcessCustom customProcess) {
		//	Name
		if(!Util.isEmpty(customProcess.getName())) {
			process.setName(customProcess.getName());
		}
		//	Description
		if(!Util.isEmpty(customProcess.getDescription())) {
			process.setDescription(customProcess.getDescription());
		}
		//	Help
		if(!Util.isEmpty(customProcess.getHelp())) {
			process.setHelp(customProcess.getHelp());
		}
		//	TODO: Language unsupported
		//	Show Help
		if(!Util.isEmpty(customProcess.getShowHelp())) {
			process.setShowHelp(customProcess.getShowHelp());
		}
		//	Report View
		if(customProcess.getAD_ReportView_ID() > 0) {
			process.setAD_ReportView_ID(customProcess.getAD_ReportView_ID());
		}
		//	Print Format
		if(customProcess.getAD_PrintFormat_ID() > 0) {
			process.setAD_PrintFormat_ID(customProcess.getAD_PrintFormat_ID());
		}
		//	Direct Print
		process.setIsDirectPrint(customProcess.isDirectPrint());
		//	Smart Browse
		if(customProcess.getAD_Browse_ID() > 0) {
			process.setAD_Browse_ID(customProcess.getAD_Browse_ID());
		}
		//	Form
		if(customProcess.getAD_Form_ID() > 0) {
			process.setAD_Form_ID(customProcess.getAD_Form_ID());
		}
		//	Workflow
		if(customProcess.getAD_Workflow_ID() > 0) {
			process.setAD_Workflow_ID(customProcess.getAD_Workflow_ID());
		}
	}
	
	/**
	 * Merge Window with custom window
	 * @param window
	 * @param customWindow
	 */
	private void mergeWindow(MWindow window, MWindowCustom customWindow) {
		//	Name
		if(!Util.isEmpty(customWindow.getName())) {
			window.setName(customWindow.getName());
		}
		//	Description
		if(!Util.isEmpty(customWindow.getDescription())) {
			window.setDescription(customWindow.getDescription());
		}
		//	Help
		if(!Util.isEmpty(customWindow.getHelp())) {
			window.setHelp(customWindow.getHelp());
		}
		//	TODO: Language unsupported
		window.setIsDefault(customWindow.isDefault());
		//	Is Read Only
		if(!Util.isEmpty(customWindow.getWindowType())) {
			window.setWindowType(customWindow.getWindowType());
		}
		//	Context Info
		if(customWindow.getAD_ContextInfo_ID() > 0) {
			window.setAD_ContextInfo_ID(customWindow.getAD_ContextInfo_ID());
		}
	}
	
	/**
	 * Merge Tab with custom window
	 * @param tab
	 * @param customTab
	 */
	private void mergeTab(MTab tab, MTabCustom customTab, boolean overwrite, int level) {
		//	Name
		if(!Util.isEmpty(customTab.getName())) {
			tab.setName(customTab.getName());
		}
		//	Description
		if(!Util.isEmpty(customTab.getDescription())) {
			tab.setDescription(customTab.getDescription());
		}
		//	Help
		if(!Util.isEmpty(customTab.getHelp())) {
			tab.setHelp(customTab.getHelp());
		}
		//	Commit Warning
		if(!Util.isEmpty(customTab.getCommitWarning())) {
			tab.setCommitWarning(customTab.getCommitWarning());
		}
		//	TODO: Language unsupported
		tab.setSeqNo(customTab.getSeqNo());
		//	Tab Level
		tab.setTabLevel(customTab.getTabLevel());
		//	Single-Row
		if(!Util.isEmpty(customTab.getIsSingleRow())) {
			tab.setIsSingleRow(customTab.getIsSingleRow().equals("Y"));
		}
		//	Process
		if(customTab.getAD_Process_ID() > 0) {
			tab.setAD_Process_ID(customTab.getAD_Process_ID());
		}		
		//	Read Only
		if(!Util.isEmpty(customTab.getIsReadOnly())) {
			tab.setIsReadOnly(customTab.getIsReadOnly().equals("Y"));
		}
		//	Insert Records
		if(!Util.isEmpty(customTab.getIsInsertRecord())) {
			tab.setIsInsertRecord(customTab.getIsInsertRecord().equals("Y"));
		}
		//	Context Info
		if(customTab.getAD_ContextInfo_ID() > 0) {
			tab.setAD_ContextInfo_ID(customTab.getAD_ContextInfo_ID());
		}
		//	Image
		if(customTab.getAD_Image_ID() > 0) {
			tab.setAD_Image_ID(customTab.getAD_Image_ID());
		}
		//	Display Logic
		if(!Util.isEmpty(customTab.getDisplayLogic())) {
			tab.setDisplayLogic(customTab.getDisplayLogic());
		}
		//	Read Only Logic
		if(!Util.isEmpty(customTab.getReadOnlyLogic())) {
			tab.setReadOnlyLogic(customTab.getReadOnlyLogic());
		}
		//	Where Clause
		if(!Util.isEmpty(customTab.getWhereClause())) {
			tab.setWhereClause(customTab.getWhereClause());
		}
		//	Order By Clause
		if(!Util.isEmpty(customTab.getOrderByClause())) {
			tab.setOrderByClause(customTab.getOrderByClause());
		}
		//	
		List<MField> fields = loadFields(tab);
		if(level == CLIENT) {
			fields = fieldCache.get(getClientKey(tab.getAD_Tab_ID(), clientId));
		} else if(level == ROLE) {
			fields = fieldCache.get(getRoleKey(tab.getAD_Tab_ID(), roleId));
		} else if(level == USER) {
			fields = fieldCache.get(getUserKey(tab.getAD_Tab_ID(), userId));
		}
		fields = mergeFields(fields, customTab.getFields(), overwrite);
		//	Put here
		if(level == CLIENT) {
			fieldCache.put(getClientKey(tab.getAD_Tab_ID(), clientId), fields);
		} else if(level == ROLE) {
			fieldCache.put(getRoleKey(tab.getAD_Tab_ID(), roleId), fields);
		} else if(level == USER) {
			fieldCache.put(getUserKey(tab.getAD_Tab_ID(), userId), fields);
		}
	}
	
	/**
	 * Merge parameters with custom parameters
	 * @param fields
	 * @param customFields
	 * @param overwrite
	 */
	private List<MField> mergeFields(List<MField> fields, List<MFieldCustom> customFields, boolean overwrite) {
		List<MField> mergedFields = null;
		if(overwrite) {
			mergedFields = new ArrayList<>();
			for(MField parameter : fields) {
				MField fieldToAdd = parameter.getDuplicated();
				fieldToAdd.setIsActive(false);
				mergedFields.add(fieldToAdd);
			}
		} else {
			mergedFields = new ArrayList<>(fields);
		}
		//	merge all parameters
		for(int index = 0; index < mergedFields.size(); index++) {
			MField field = mergedFields.get(index);
			customFields.stream()
			.filter(customField -> customField.getAD_Field_ID() == field.getAD_Field_ID())
			.forEach(customField -> {
				mergeField(field, customField);
			});
			mergedFields.set(index, field);
		}
		//
		return mergedFields;
	}
	
	/**
	 * Merge Process Parameter with custom process
	 * @param processParameter
	 * @param customProcessParameter
	 */
	private void mergeProcessParameter(MProcessPara processParameter, MProcessParaCustom customProcessParameter) {
		//	Name
		if(!Util.isEmpty(customProcessParameter.getName())) {
			processParameter.setName(customProcessParameter.getName());
		}
		//	Description
		if(!Util.isEmpty(customProcessParameter.getDescription())) {
			processParameter.setDescription(customProcessParameter.getDescription());
		}
		//	Help
		if(!Util.isEmpty(customProcessParameter.getHelp())) {
			processParameter.setHelp(customProcessParameter.getHelp());
		}
		//	Reference
		if(customProcessParameter.getAD_Reference_ID() > 0) {
			processParameter.setAD_Reference_ID(customProcessParameter.getAD_Reference_ID());
		}
		//	Reference Key
		if(customProcessParameter.getAD_Reference_Value_ID() > 0) {
			processParameter.setAD_Reference_Value_ID(customProcessParameter.getAD_Reference_Value_ID());
		}
		//	Mandatory
		if(!Util.isEmpty(customProcessParameter.getIsMandatory())) {
			processParameter.setIsMandatory(customProcessParameter.getIsMandatory().equals("Y"));
		}
		//	Validation Rule
		if(customProcessParameter.getAD_Val_Rule_ID() > 0) {
			processParameter.setAD_Val_Rule_ID(customProcessParameter.getAD_Val_Rule_ID());
		}
		//	Sequence
		processParameter.setSeqNo(customProcessParameter.getSeqNo());
		//	Active
		processParameter.setIsActive(customProcessParameter.isActive());
		//	Default Logic
		if(!Util.isEmpty(customProcessParameter.getDefaultValue())) {
			processParameter.setDefaultValue(customProcessParameter.getDefaultValue());
		}
		//	Default value to
		if(!Util.isEmpty(customProcessParameter.getDefaultValue2())) {
			processParameter.setDefaultValue2(customProcessParameter.getDefaultValue2());
		}
		//	Range
		if(!Util.isEmpty(customProcessParameter.getIsRange())) {
			processParameter.setIsRange(customProcessParameter.getIsRange().equals("Y"));
		}
		//	Display Logic
		if(!Util.isEmpty(customProcessParameter.getDisplayLogic())) {
			processParameter.setDisplayLogic(customProcessParameter.getDisplayLogic());
		}
		//	Read Only Logic
		if(!Util.isEmpty(customProcessParameter.getReadOnlyLogic())) {
			processParameter.setReadOnlyLogic(customProcessParameter.getReadOnlyLogic());
		}
		//	Information Only
		if(!Util.isEmpty(customProcessParameter.getIsInfoOnly())) {
			processParameter.setIsInfoOnly(customProcessParameter.getIsInfoOnly().equals("Y"));
		}
		//	Value Format
		if(!Util.isEmpty(customProcessParameter.getVFormat())) {
			processParameter.setVFormat(customProcessParameter.getVFormat());
		}
		//	Min Value
		if(!Util.isEmpty(customProcessParameter.getValueMin())) {
			processParameter.setValueMin(customProcessParameter.getValueMin());
		}
		//	Max Value
		if(!Util.isEmpty(customProcessParameter.getValueMax())) {
			processParameter.setValueMax(customProcessParameter.getValueMax());
		}
	}
	
	/**
	 * Merge field with custom process
	 * @param field
	 * @param customField
	 */
	private void mergeField(MField field, MFieldCustom customField) {
		//	Name
		if(!Util.isEmpty(customField.getName())) {
			field.setName(customField.getName());
		}
		//	Description
		if(!Util.isEmpty(customField.getDescription())) {
			field.setDescription(customField.getDescription());
		}
		//	Help
		if(!Util.isEmpty(customField.getHelp())) {
			field.setHelp(customField.getHelp());
		}
		//	Sequence
		field.setSeqNo(customField.getSeqNo());
		//	Grid Sequence
		field.setSeqNoGrid(customField.getSeqNoGrid());
		//	Active
		field.setIsActive(customField.isActive());
		//	Is Displayed
		field.setIsDisplayed(customField.isDisplayed());
		//	Is Embedded
		if(!Util.isEmpty(customField.getIsEmbedded())) {
			field.setIsEmbedded(customField.getIsEmbedded().equals("Y"));
		}
		//	Field Group
		if(customField.getAD_FieldGroup_ID() > 0) {
			field.setAD_FieldGroup_ID(customField.getAD_FieldGroup_ID());
		}
		//	Displayed in grid
		if(!Util.isEmpty(customField.getIsDisplayedGrid())) {
			field.setIsDisplayedGrid(customField.getIsDisplayedGrid().equals("Y"));
		}
		//	Read Only
		if(!Util.isEmpty(customField.getIsReadOnly())) {
			field.setIsReadOnly(customField.getIsReadOnly().equals("Y"));
		}
		//	Updateable
		if(!Util.isEmpty(customField.getIsAllowCopy())) {
			field.setIsAllowCopy(customField.getIsAllowCopy().equals("Y"));
		}
		//	Display Logic
		if(!Util.isEmpty(customField.getDisplayLogic())) {
			field.setDisplayLogic(customField.getDisplayLogic());
		}
		//	Encrypted
		if(!Util.isEmpty(customField.getIsEncrypted())) {
			field.setIsEncrypted(customField.getIsEncrypted().equals("Y"));
		}
		//	Same Line
		if(!Util.isEmpty(customField.getIsSameLine())) {
			field.setIsSameLine(customField.getIsSameLine().equals("Y"));
		}		
		//	Record Sort No
		if(customField.getSortNo() > 0) {
			field.setSortNo(new BigDecimal(customField.getSortNo()));
		}
		//	Obscure
		if(!Util.isEmpty(customField.getObscureType())) {
			field.setObscureType(customField.getObscureType());
		}
		//	Heading
		if(!Util.isEmpty(customField.getIsHeading())) {
			field.setIsHeading(customField.getIsHeading().equals("Y"));
		}
		//	Field Only
		if(!Util.isEmpty(customField.getIsFieldOnly())) {
			field.setIsFieldOnly(customField.getIsFieldOnly().equals("Y"));
		}
		//	Field Definition
		if(customField.getAD_FieldDefinition_ID() > 0) {
			field.setAD_FieldDefinition_ID(customField.getAD_FieldDefinition_ID());
		}
		//	Reference
		if(customField.getAD_Reference_ID() > 0) {
			field.setAD_Reference_ID(customField.getAD_Reference_ID());
		}
		//	Reference Key
		if(customField.getAD_Reference_Value_ID() > 0) {
			field.setAD_Reference_Value_ID(customField.getAD_Reference_Value_ID());
		}
		//	Validation Rule
		if(customField.getAD_Val_Rule_ID() > 0) {
			field.setAD_Val_Rule_ID(customField.getAD_Val_Rule_ID());
		}		
		//	Mandatory
		if(!Util.isEmpty(customField.getIsMandatory())) {
			field.setIsMandatory(customField.getIsMandatory());
		}
		//	Image
		if(customField.getAD_Image_ID() > 0) {
			field.setAD_Image_ID(customField.getAD_Image_ID());
		}
		//	Default Logic
		if(!Util.isEmpty(customField.getDefaultValue())) {
			field.setDefaultValue(customField.getDefaultValue());
		}
		//	Quick Entry
		if(!Util.isEmpty(customField.getIsQuickEntry())) {
			field.setIsQuickEntry(customField.getIsQuickEntry().equals("Y"));
		}
		//	Default Logic
		if(!Util.isEmpty(customField.getInfoFactoryClass())) {
			field.setInfoFactoryClass(customField.getInfoFactoryClass());
		}
		//	Context Info
		if(customField.getAD_ContextInfo_ID() > 0) {
			field.setAD_ContextInfo_ID(customField.getAD_ContextInfo_ID());
		}
	}
	
	/**
	 * Test It
	 * @param args
	 */
	public static void main(String args[]) {
		Adempiere.startup(false);
		int clientId = 11;
		int roleId = 102;
		int userId = 100;
		ASPUtil aspUtil = ASPUtil.getInstance(Env.getCtx(), clientId, roleId, userId);
		//	
		MProcess process = aspUtil.getProcess(54015);
		List<MProcessPara> processParameterList = aspUtil.getProcessParameters(54015);
		//	
		
		System.err.println("Name " + process.getName());
		for(MProcessPara parameter : processParameterList) {
			System.err.println(parameter.getColumnName() + ", " + parameter.isActive() + ", " + parameter.isRange());
		}
		
		MWindow window = aspUtil.getWindow(53553);
		List<MTab> tabs = aspUtil.getTabs(53553);
		System.err.println("Window == " + window.getName());
		for(MTab tab : tabs) {
			System.err.println("Tab == " + tab.getName());
			List<MField> fields = aspUtil.getFields(tab.getAD_Tab_ID());
			for(MField field : fields) {
				System.err.println("Field == " + field.getName() + " Displayed: " + field.isDisplayed() + " Active: " + field.isActive());
			}
		}
	}
}
