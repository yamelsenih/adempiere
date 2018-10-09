/**************************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                               *
 * This program is free software; you can redistribute it and/or modify it    		  *
 * under the terms version 2 or later of the GNU General Public License as published  *
 * by the Free Software Foundation. This program is distributed in the hope           *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied         *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                   *
 * See the GNU General Public License for more details.                               *
 * You should have received a copy of the GNU General Public License along            *
 * with this program; if not, printLine to the Free Software Foundation, Inc.,        *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                             *
 * For the text or an alternative of this public license, you may reach us            *
 * Copyright (C) 2012-2018 E.R.P. Consultores y Asociados, S.A. All Rights Reserved.  *
 * Contributor: Carlos Parada cparada@erpya.com                                       *
 * See: www.erpya.com                                                                 *
 *************************************************************************************/

package org.spin.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MArchive;
import org.compiere.model.MAttachment;
import org.compiere.model.MClient;
import org.compiere.model.MImage;
import org.compiere.util.DB;

/**
 * @author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 *  	<a href="https://github.com/adempiere/adempiere/issues/2057">
 *		@see FR [ 2057 ] Process for send files from disk to db or db to disk</a>
 */
public class ClientSaveDiskDB  extends ClientSaveDiskDBAbstract {

	/**Store Attachment on File System*/
	private boolean p_StoreFilesOnFileSystem		= false;
	
	/**Windows Attachment Path*/
	private String p_WindowsFilePath				= null; 
	
	/**Unix Attachment Path*/
	private String p_UnixFilePath					= null;
	
	private String m_FileRootPath					= null;
	
	
	@Override
	protected void prepare() {
		p_StoreFilesOnFileSystem = getParameterAsBoolean(STOREFILESONFILESYSTEM);
		p_WindowsFilePath = getParameterAsString(WINDOWSFILEPATH);
		p_UnixFilePath = getParameterAsString(UNIXFILEPATH);
	}

	@Override
	protected String doIt() throws Exception {
	
		
		String result = "@OK@";
		MClient client = MClient.get(getCtx());
		
		//Process Attachments Files
		if (client.isStoreFilesOnFileSystem()!=p_StoreFilesOnFileSystem){
			processAttachmentFiles(client);
			processArchiveFiles(client);
			processImagesFiles();
			
			client.setStoreFilesOnFileSystem(p_StoreFilesOnFileSystem);
			client.setWindowsFilePath(p_WindowsFilePath);
			client.setUnixFilePath(p_UnixFilePath);
			client.save();
		}
		
		
		return result;
	}
	
	/**
	 * Process Attachment File or Database
	 * @return void
	 */
	private void processAttachmentFiles(MClient client) throws SQLException{
		if (p_StoreFilesOnFileSystem &&
				((p_WindowsFilePath!=null && p_WindowsFilePath.equals("")) || p_WindowsFilePath == null)
					&& ((p_UnixFilePath!=null && p_UnixFilePath.equals("")) || p_UnixFilePath == null))
			throw new AdempiereException("@Error@ @StoreFilesOnFileSystem@ @IsSelected@ @FillMandatory@ @WindowsFilePath@ @OR@ @UnixFilePath@");
		else{
			ResultSet rs = null;
			PreparedStatement ps = null;
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT AD_Attachment_ID "
					+ " FROM AD_Attachment "
					+ " WHERE AD_Client_ID = ? "
					+ " AND IsActive = 'Y'");
			ps = DB.prepareStatement(sql.toString(), get_TrxName());
			ps.setInt(1, getAD_Client_ID());
			rs = ps.executeQuery();
			
			if (p_StoreFilesOnFileSystem){
				if (p_WindowsFilePath!=null && !p_WindowsFilePath.equals(""))
					m_FileRootPath = p_WindowsFilePath;
				
				if (p_UnixFilePath!=null && !p_UnixFilePath.equals(""))
					m_FileRootPath = p_UnixFilePath;
				
				if (m_FileRootPath!=null && !m_FileRootPath.equals("")){
					while (rs.next()){
						MAttachment att = new MAttachment(getCtx(), rs.getInt("AD_Attachment_ID"), get_TrxName());
						att.loadLOBData();
						att.saveLOBData(p_StoreFilesOnFileSystem, m_FileRootPath);
						att.save();
					}
				}
				else
					log.severe("no attachmentPath defined");
			}else{
				while (rs.next()){
					MAttachment att = new MAttachment(getCtx(), rs.getInt("AD_Attachment_ID"), get_TrxName());
					att.loadLOBData();
					att.saveLOBData(p_StoreFilesOnFileSystem, m_FileRootPath);
					att.save();
				}
			}
			
			if (client.isStoreAttachmentsOnFileSystem()) {
				client.setStoreAttachmentsOnFileSystem(false);
				client.setWindowsAttachmentPath("");
				client.setUnixAttachmentPath("");
			}
		}
	}
	/**
	 * Process Archive File or Database
	 * @throws SQLException
	 * @return void
	 */
	private void processArchiveFiles(MClient client) throws SQLException{
		
		if (p_StoreFilesOnFileSystem &&
				((p_WindowsFilePath!=null && p_WindowsFilePath.equals("")) || p_WindowsFilePath == null)
					&& ((p_UnixFilePath!=null && p_UnixFilePath.equals("")) || p_UnixFilePath == null))
			throw new AdempiereException("@Error@ @StoreFilesOnFileSystem@ @IsSelected@ @FillMandatory@ @WindowsFilePath@ @OR@ @UnixFilePath@");
		else{
			ResultSet rs = null;
			PreparedStatement ps = null;
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT AD_Archive_ID "
					+ " FROM AD_Archive "
					+ " WHERE AD_Client_ID = ? "
					+ " AND IsActive = 'Y'");
			ps = DB.prepareStatement(sql.toString(), get_TrxName());
			ps.setInt(1, getAD_Client_ID());
			rs = ps.executeQuery();
			
			if (p_StoreFilesOnFileSystem){
				if (p_WindowsFilePath!=null && !p_WindowsFilePath.equals(""))
					m_FileRootPath = p_WindowsFilePath;

			if (p_UnixFilePath!=null && !p_UnixFilePath.equals(""))
				m_FileRootPath= p_UnixFilePath;

			if (m_FileRootPath!=null && !m_FileRootPath.equals("")){
				
				while (rs.next()){
					MArchive arch = new MArchive(getCtx(), rs.getInt("AD_Archive_ID"), get_TrxName());
					byte[] data = arch.getBinaryData();
					arch.setBinaryData(data ,p_StoreFilesOnFileSystem, m_FileRootPath);
					arch.save();
				}
			}
			else
				log.severe("no archivePath defined");
			}else{
				while (rs.next()){
					MArchive arch = new MArchive(getCtx(), rs.getInt("AD_Archive_ID"), get_TrxName());
					byte[] data = arch.getBinaryData();
					arch.setBinaryData(data ,p_StoreFilesOnFileSystem, m_FileRootPath);
					arch.save();
				}
			}
			
			if (client.isStoreArchiveOnFileSystem()) {
				client.setStoreArchiveOnFileSystem(false);
				client.setWindowsArchivePath("");
				client.setUnixArchivePath("");
			}
		}
	}
	
	/**
	 * Process Attachment File or Database
	 * @return void
	 */
	private void processImagesFiles() throws SQLException{
		if (p_StoreFilesOnFileSystem &&
				((p_WindowsFilePath!=null && p_WindowsFilePath.equals("")) || p_WindowsFilePath == null)
					&& ((p_UnixFilePath!=null && p_UnixFilePath.equals("")) || p_UnixFilePath == null))
			throw new AdempiereException("@Error@ @StoreFilesOnFileSystem@ @IsSelected@ @FillMandatory@ @WindowsFilePath@ @OR@ @UnixFilePath@");
		else{
			ResultSet rs = null;
			PreparedStatement ps = null;
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT AD_Image_ID "
					+ " FROM AD_Image "
					+ " WHERE AD_Client_ID = ? "
					+ " AND IsActive = 'Y'");

			ps = DB.prepareStatement(sql.toString(), get_TrxName());
			ps.setInt(1, getAD_Client_ID());
			rs = ps.executeQuery();
			
			
			if (p_StoreFilesOnFileSystem){
				if (p_WindowsFilePath!=null && !p_WindowsFilePath.equals(""))
					m_FileRootPath = p_WindowsFilePath;
				
				if (p_UnixFilePath!=null && !p_UnixFilePath.equals(""))
					m_FileRootPath = p_UnixFilePath;
				
				if (m_FileRootPath!=null && !m_FileRootPath.equals("")){
					while (rs.next()){
						MImage image = new MImage(getCtx(), rs.getInt("AD_Image_ID"), get_TrxName());
						byte[] fileImg = image.getData();
						image.saveLOBData(fileImg, p_StoreFilesOnFileSystem, m_FileRootPath);
						image.save();
					}
				}
				else
					log.severe("no attachmentPath defined");
			}else{
				while (rs.next()){
					MImage image = new MImage(getCtx(), rs.getInt("AD_Image_ID"), get_TrxName());
					byte[] fileImg = image.getData();
					image.saveLOBData(fileImg, p_StoreFilesOnFileSystem, m_FileRootPath);
					image.save();
				}
			}
					
		}
	}

}
