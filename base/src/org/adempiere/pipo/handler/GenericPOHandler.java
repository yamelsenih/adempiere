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
package org.adempiere.pipo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.PoFiller;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_Element;
import org.compiere.model.I_AD_EntityType;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.POInfo;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generic PO Handler for import and export PO
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 */
public class GenericPOHandler extends AbstractElementHandler {
	private final List<String> list = new ArrayList<String>();
	/**	Static values	*/
	public static final String TABLE_NAME_TAG = "TableNameTag";
	public static final String TABLE_ID_TAG = "TableIdTag";
	public static final String RECORD_ID_TAG = "RecordIdTag";
	public static final String TAG_Name = "GenericPO";
	
	@Override
	public void startElement(Properties ctx, Element element) throws SAXException {
		final String elementValue = element.getElementValue();
		final Attributes atts = element.attributes;
		final String tableName = getStringValue(atts, TABLE_NAME_TAG);
		final int tableId = getIntValue(atts, TABLE_ID_TAG, -1);
		log.info(elementValue + " " + tableName);
		//	Get UUID
		String uuid = getUUIDValue(atts, tableName); 
		if(Util.isEmpty(uuid)
			|| tableId == -1) {
			element.skip = true;
		}
		//	Else
		int recordId = getIdFromUUID(ctx, tableName, uuid);
		PO entity = getCreatePO(ctx, tableId, recordId, getTrxName(ctx));
		//	Fill attributes
		POInfo poInfo = POInfo.getPOInfo(entity.getCtx(), entity.get_Table_ID());
		if(poInfo.isIgnoreMigration()) {
			return;
		}
		
		int backupId;
		String objectStatus;
		if (recordId > 0) {		
			backupId = copyRecord(ctx, I_AD_EntityType.Table_Name, entity);
			objectStatus = "Update";			
		} else {
			objectStatus = "New";
			backupId = 0;
		}
		//	Load filler
		PoFiller filler = new PoFiller(entity, atts);
		for(int index = 0; index < poInfo.getColumnCount(); index++) {
			//	No SQL
			if(poInfo.isVirtualColumn(index)) {
				continue;
			}
			//	No Key if is major that Official ID
			if (poInfo.isKey(index)) {
				if(entity.get_ID() > PackOut.MAX_OFFICIAL_ID) {
					continue;
				} else {
					//entity.setIsDirectLoad(true);
				}
			}
			//	No Encrypted
			if(poInfo.isEncrypted(index)) {
				continue;
			}
			String columnName = poInfo.getColumnName(index);
			//	No user log
			if(columnName.equals(I_AD_Element.COLUMNNAME_Created)
					|| columnName.equals(I_AD_Element.COLUMNNAME_CreatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_Updated)
					|| columnName.equals(I_AD_Element.COLUMNNAME_UpdatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Client_ID)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Org_ID)) {
				continue;
			}
			//	Fill each column
			filler.setAttribute(columnName);
		}
		//	Save
		try {
			entity.save(getTrxName(ctx));
			recordLog (ctx, 1, entity.get_ValueAsString(I_AD_Element.COLUMNNAME_UUID), getTagName(entity), entity.get_ID(),
						backupId, objectStatus,
						I_AD_EntityType.Table_Name, I_AD_EntityType.Table_ID);
		} catch (Exception e) {
			recordLog (ctx, 0, entity.get_ValueAsString(I_AD_Element.COLUMNNAME_UUID), getTagName(entity), entity.get_ID(),
						backupId, objectStatus,
						I_AD_EntityType.Table_Name, I_AD_EntityType.Table_ID);
			throw new POSaveFailedException(e);
		}
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}
	
	/**
	 * With default include parents
	 */
	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		create(ctx, document, null, false, null);
	}
	
	/**
	 * With PO and include parents by default
	 * @param ctx
	 * @param document
	 * @param entity
	 * @throws SAXException
	 */
	public void create(Properties ctx, TransformerHandler document, PO entity) throws SAXException {
		create(ctx, document, entity, false, null);
	}
	
	/**
	 * Create Attributes
	 */
	public void create(Properties ctx, TransformerHandler document, PO entity, boolean includeParents, List<String> excludedParentList) throws SAXException {
		int tableId = 0;
		int recordId = 0;
		if(entity != null) {
			tableId = entity.get_Table_ID();
			recordId = entity.get_ID();
		} else {
			tableId = Env.getContextAsInt(ctx, TABLE_ID_TAG);
			recordId = Env.getContextAsInt(ctx, RECORD_ID_TAG);
		}
		if(tableId <= 0
				|| recordId <= 0) {
			return;
		}
		//	Validate if was processed
		String key = tableId + "|" + recordId;
		if (list.contains(key)) {
			return;
		}
		list.add(key);
		//	Instance PO
		if(entity == null) {
			entity = getCreatePO(ctx, tableId, recordId, null);
		}
		//	Create parents
		if(includeParents) {
			createParents(ctx, document, entity, excludedParentList);
		}
		AttributesImpl atts = createMessageBinding(entity);
		if(atts != null) {
			document.startElement("", "", getTagName(entity), atts);
			document.endElement("", "", getTagName(entity));
		}
	}
	
	/**
	 * Create Parent from lookup columns
	 * @param ctx
	 * @param document
	 * @param entity
	 * @throws SAXException
	 */
	private void createParents(Properties ctx, TransformerHandler document, PO entity, List<String> excludedParentList) throws SAXException {
		POInfo poInfo = POInfo.getPOInfo(entity.getCtx(), entity.get_Table_ID());
		for(int index = 0; index < poInfo.getColumnCount(); index++) {
			int displayType = poInfo.getColumnDisplayType(index);
			if(!DisplayType.isLookup(displayType)) {
				continue;
			}
			//	Validate list
			if(DisplayType.List == displayType) {
				continue;
			}
			//	No SQL
			if(poInfo.isVirtualColumn(index)) {
				continue;
			}
			//	No Key if is major that Official ID
			if (poInfo.isKey(index) && entity.get_ID() > PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	No Encrypted
			if(poInfo.isEncrypted(index)) {
				continue;
			}
			String columnName = poInfo.getColumnName(index);
			//	No user log
			if(columnName.equals(I_AD_Element.COLUMNNAME_Created)
					|| columnName.equals(I_AD_Element.COLUMNNAME_CreatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_Updated)
					|| columnName.equals(I_AD_Element.COLUMNNAME_UpdatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Client_ID)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Org_ID)) {
				continue;
			}
			//	Create Parent
			MLookupInfo info = MLookupFactory.getLookupInfo(ctx, 0, poInfo.getAD_Column_ID(columnName), displayType);
			if(info == null) {
				continue;
			}
			//	Verify Exclusion
			if(excludedParentList!= null) {
				if(excludedParentList.contains(info.TableName)) {
					continue;
				}
			}
			PO parentEntity = MTable.get(ctx, info.TableName).getPO(entity.get_ValueAsInt(columnName), null);
			if(parentEntity == null
					|| parentEntity.get_ID() <= 0) {
				continue;
			}
			//	For others
			Env.setContext(ctx, GenericPOHandler.TABLE_ID_TAG, parentEntity.get_Table_ID());
			Env.setContext(ctx, GenericPOHandler.RECORD_ID_TAG, parentEntity.get_ID());
			create(ctx, document);
			ctx.remove(GenericPOHandler.TABLE_ID_TAG);
			ctx.remove(GenericPOHandler.RECORD_ID_TAG);
		}
	}
	
	/**
	 * Get Tag name from PO
	 * @param entity
	 * @return
	 */
	private String getTagName(PO entity) {
		return TAG_Name + "_" + entity.get_TableName();
	}
	
	/**
	 * Create PO from Table and Record ID
	 * @param ctx
	 * @param tableId
	 * @param recordId
	 * @param trxName
	 * @return
	 */
	private PO getCreatePO(Properties ctx, int tableId, int recordId, String trxName) {
		return MTable.get(ctx, tableId).getPO(recordId, trxName);
	}
	
	/**
	 * Create export from data
	 * @param entity
	 * @return
	 */
	private AttributesImpl createMessageBinding(PO entity) {
		AttributesImpl atts = new AttributesImpl();
		atts.clear();
		//	Fill attributes
		POInfo poInfo = POInfo.getPOInfo(entity.getCtx(), entity.get_Table_ID());
		AttributeFiller filler = new AttributeFiller(atts, entity);
		filler.addUUID();
		filler.addString(TABLE_NAME_TAG,entity.get_TableName());
		filler.addInt(TABLE_ID_TAG,entity.get_Table_ID());
		for(int index = 0; index < poInfo.getColumnCount(); index++) {
			//	No SQL
			if(poInfo.isVirtualColumn(index)) {
				continue;
			}
			//	No Key if is major that Official ID
			if (poInfo.isKey(index) && entity.get_ID() > PackOut.MAX_OFFICIAL_ID) {
				continue;
			}
			//	No Encrypted
			if(poInfo.isEncrypted(index)) {
				continue;
			}
			String columnName = poInfo.getColumnName(index);
			//	No user log
			if(columnName.equals(I_AD_Element.COLUMNNAME_Created)
					|| columnName.equals(I_AD_Element.COLUMNNAME_CreatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_Updated)
					|| columnName.equals(I_AD_Element.COLUMNNAME_UpdatedBy)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Client_ID)
					|| columnName.equals(I_AD_Element.COLUMNNAME_AD_Org_ID)) {
				continue;
			}
			//	
			filler.add(columnName);
		}
		//	Return Attributes
		return atts;
	}
}
