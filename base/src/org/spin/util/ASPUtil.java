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

import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_AD_Process;
import org.compiere.model.MProcess;
import org.compiere.model.PO;
import org.compiere.util.CCache;
import org.compiere.util.Env;

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
	private static CCache<String, MProcess>	processCache = new CCache<String, MProcess>(I_AD_Process.Table_Name, 20);
	/**
	 * Private constructor
	 */
	private ASPUtil(Properties context) {
		if(context == null) {
			throw new AdempiereException("@ContextIsMandatory@");
		}
		this.context = context;
		clientId = Env.getAD_Client_ID(context);
		roleId = Env.getAD_Role_ID(context);
		userId = Env.getAD_User_ID(context);
	}
	
	/**
	 * Get instance for ASP
	 * @param context
	 * @return
	 */
	public static ASPUtil getInstance(Properties context) {
		if(instance == null) {
			instance = new ASPUtil(context);
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
		//	Merge Process for client (ASP)
		MProcess clientProcess = getClientProcess(process);
		//	Save client
		processCache.put(getClientKey(processId, clientId), clientProcess);
		//	Merge Process for role
		MProcess roleProcess = getRoleProcess(clientProcess);
		//	Save role
		processCache.put(getRoleKey(processId, roleId), roleProcess);
		//	Merge Process for user
		MProcess userProcess = getUserProcess(roleProcess);
		//	Save user
		processCache.put(getUserKey(processId, userId), userProcess);
		//	
		return userProcess;
	}
	
	/**
	 * Get client process from dictionary process
	 * @param process
	 * @return
	 */
	private MProcess getClientProcess(MProcess process) {
		MProcess clientProcess = new MProcess(context, 0, null);
		PO.copyValues(process, clientProcess);
		clientProcess.setAD_Process_ID(process.getAD_Process_ID());
		//	return
		return clientProcess;
	}
	
	/**
	 * Get / Merge process for role
	 * @param process
	 * @return
	 */
	private MProcess getRoleProcess(MProcess process) {
		MProcess roleProcess = new MProcess(context, 0, null);
		PO.copyValues(process, roleProcess);
		roleProcess.setAD_Process_ID(process.getAD_Process_ID());
		//	return
		return roleProcess;
	}
	
	/**
	 * Get / Merge user process
	 * @param process
	 * @return
	 */
	private MProcess getUserProcess(MProcess process) {
		MProcess userProcess = new MProcess(context, 0, null);
		PO.copyValues(process, userProcess);
		userProcess.setAD_Process_ID(process.getAD_Process_ID());
		//	return
		return userProcess;
	}
	
	/**
	 * Get Client Key from object Id
	 * @param objectId
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
	 * Merge PO
	 * @param originalPO
	 * @param newLayerPO
	 * @param overwrite
	 */
	private void mergePO(PO originalPO, PO newLayerPO, boolean overwrite) {
		//	Get copy new values
		for(int index = 0; index < newLayerPO.get_ColumnCount(); index++) {
			String columnName = newLayerPO.get_ColumnName(index);
			//	Overwrite values
			//  Ignore Standard Values
			if (columnName.startsWith("Created")
				|| columnName.startsWith("Updated")
				|| columnName.equals("AD_Client_ID")
				|| columnName.equals("AD_Org_ID")
				|| columnName.equals("UUID")
				|| columnName.equals(originalPO.get_TableName() + "_ID")
				|| columnName.equals(newLayerPO.get_TableName() + "_ID")) {
				continue;
			}
			//	
			
		}
	}
}
