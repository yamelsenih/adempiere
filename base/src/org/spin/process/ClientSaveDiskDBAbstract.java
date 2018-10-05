/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2017 ADempiere Foundation, All Rights Reserved.         *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * or (at your option) any later version.										*
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

import org.compiere.process.SvrProcess;

/** Generated Process for (Change Save Files Disk <-> DB)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public abstract class ClientSaveDiskDBAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "ClientSaveDiskDB";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Change Save Files Disk <-> DB";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 1000000;
	/**	Parameter Name for Store Files On File System	*/
	public static final String STOREFILESONFILESYSTEM = "StoreFilesOnFileSystem";
	/**	Parameter Name for Windows File Path	*/
	public static final String WINDOWSFILEPATH = "WindowsFilePath";
	/**	Parameter Name for Unix File Path	*/
	public static final String UNIXFILEPATH = "UnixFilePath";
	/**	Parameter Value for Store Files On File System	*/
	private boolean isStoreFilesOnFileSystem;
	/**	Parameter Value for Windows File Path	*/
	private String windowsFilePath;
	/**	Parameter Value for Unix File Path	*/
	private String unixFilePath;

	@Override
	protected void prepare() {
		isStoreFilesOnFileSystem = getParameterAsBoolean(STOREFILESONFILESYSTEM);
		windowsFilePath = getParameterAsString(WINDOWSFILEPATH);
		unixFilePath = getParameterAsString(UNIXFILEPATH);
	}

	/**	 Getter Parameter Value for Store Files On File System	*/
	protected boolean isStoreFilesOnFileSystem() {
		return isStoreFilesOnFileSystem;
	}

	/**	 Setter Parameter Value for Store Files On File System	*/
	protected void setStoreFilesOnFileSystem(boolean isStoreFilesOnFileSystem) {
		this.isStoreFilesOnFileSystem = isStoreFilesOnFileSystem;
	}

	/**	 Getter Parameter Value for Windows File Path	*/
	protected String getWindowsFilePath() {
		return windowsFilePath;
	}

	/**	 Setter Parameter Value for Windows File Path	*/
	protected void setWindowsFilePath(String windowsFilePath) {
		this.windowsFilePath = windowsFilePath;
	}

	/**	 Getter Parameter Value for Unix File Path	*/
	protected String getUnixFilePath() {
		return unixFilePath;
	}

	/**	 Setter Parameter Value for Unix File Path	*/
	protected void setUnixFilePath(String unixFilePath) {
		this.unixFilePath = unixFilePath;
	}

	/**	 Getter Parameter Value for Process ID	*/
	public static final int getProcessId() {
		return ID_FOR_PROCESS;
	}

	/**	 Getter Parameter Value for Process Value	*/
	public static final String getProcessValue() {
		return VALUE_FOR_PROCESS;
	}

	/**	 Getter Parameter Value for Process Name	*/
	public static final String getProcessName() {
		return NAME_FOR_PROCESS;
	}
}