/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.compiere.util.ValueNamePair;
import org.spin.model.MADTranslation;

/**
 *  Reference List Value
 *
 *  @author Jorg Janke
 *  @version $Id: MRefList.java,v 1.3 2006/07/30 00:58:18 jjanke Exp $
 *  
 *  @author Teo Sarca, www.arhipac.ro
 *  		<li>BF [ 1748449 ] Info Account - Posting Type is not translated
 *  		<li>FR [ 2694043 ] Query. first/firstOnly usage best practice
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 *		<a href="https://github.com/adempiere/adempiere/issues/1000">
 * 		@see FR [ 1000 ] Add new feature for unique translation table</a>
 */
public class MRefList extends X_AD_Ref_List
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6948532574960232289L;


	/**
	 * 	Get Reference List 
	 *	@param ctx context
	 *	@param AD_Reference_ID reference
	 *	@param Value value
	 *	@param trxName transaction
	 *	@return List or null
	 */
	public static MRefList get (Properties ctx, int AD_Reference_ID, String Value, String trxName) {
		return new Query(ctx, Table_Name, "AD_Reference_ID=? AND Value=?", trxName)
					.setParameters(AD_Reference_ID, Value)
					.firstOnly();
	}	//	get

	/**
	 * Get Reference List Value Name (cached)
	 * @param ctx context
	 * @param referenceId reference
	 * @param Value value
	 * @return List or ""
	 */
	public static String getListName(Properties ctx, int referenceId, String Value) {
		String language = Env.getAD_Language(ctx);
		String key = language + "_" + referenceId + "_" + Value;
		String retValue = (String)cache.get(key);
		if (retValue != null)
			return retValue;
		//	for reload
		if(MADTranslation.isSupported()) {
			String sql = "SELECT getTranslation('AD_Ref_List', 'Name', AD_Ref_List_ID, ?, Name) AS Name "
					+ "FROM AD_Ref_List "
					+ "WHERE AD_Reference_ID = ? "
					+ "AND Value = ?";
			//	Return
			retValue = DB.getSQLValueString(null, sql, language, referenceId, retValue);
		} else {
			retValue = getListName(referenceId, retValue, language);
		}		
		//	Save into Cache
		if (retValue == null) {
			retValue = "";
			s_log.warning("Not found " + key);
		}
		cache.put(key, retValue);
		//
		return retValue;
	}	//	getListName
	
	/**
	 * Get list name (Old method)
	 * @param referenceId
	 * @param value
	 * @param language
	 * @return
	 */
	@Deprecated
	private static String getListName(int referenceId, String value, String language) {
		String retValue = null;
		boolean isBaseLanguage = Env.isBaseLanguage(language, "AD_Ref_List");
		String sql = isBaseLanguage ?
			"SELECT Name FROM AD_Ref_List "
			+ "WHERE AD_Reference_ID=? AND Value=?" :
			"SELECT t.Name FROM AD_Ref_List_Trl t"
			+ " INNER JOIN AD_Ref_List r ON (r.AD_Ref_List_ID=t.AD_Ref_List_ID) "
			+ "WHERE r.AD_Reference_ID=? AND r.Value=? AND t.AD_Language=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, referenceId);
			pstmt.setString(2, value);
			if (!isBaseLanguage)
				pstmt.setString(3, language);
			rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = rs.getString(1);
		} catch (SQLException ex) {
			s_log.log(Level.SEVERE, sql + " -- " + value + " -- " + language, ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//	Default return
		return retValue;
	}

	/**
	 * Get Reference List Value Description (cached)
	 * @param ctx context
	 * @param listName reference
	 * @param value value
	 * @return List or null
	 */
	public static String getListDescription(Properties ctx, String listName, String value) {
		String language = Env.getAD_Language(ctx);
		String key = language + "_" + listName + "_" + value;
		String retValue = cache.get(key);
		if (retValue != null)
			return retValue;
		//	Validate for new method
		if(MADTranslation.isSupported()) {
			String sql = "SELECT getTranslation('AD_Ref_List', 'Description', rl.AD_Ref_List_ID, ?, rl.Description) AS Description "
					+ "FROM AD_Ref_List rl "
					+ "WHERE rl.Value = ? "
					+ "AND EXISTS(SELECT 1 FROM AD_Reference r "
					+ "				WHERE r.AD_Reference_ID = rl.AD_Reference_ID "
					+ "				AND r.Name = ?)";
			retValue = DB.getSQLValueString(null, sql, language, value, listName);
		} else {
			retValue = getListDescription(listName, value, language);
		}
		//	Save into Cache
		if (retValue == null) {
			retValue = "";
			s_log.info("getListDescription - Not found " + key);
		}
		cache.put(key, retValue);
		//
		return retValue;
	}	//	getListDescription
	
	/**
	 * Get Reference List Value Description Old method (cached)
	 * @param listName reference
	 * @param value value
	 * @param language
	 * @return List or null
	 */
	@Deprecated
	private static String getListDescription(String listName, String value, String language) {
		String retValue = null;
		//	
		boolean isBaseLanguage = Env.isBaseLanguage(language, "AD_Ref_List");
		String sql = isBaseLanguage ?
			"SELECT a.Description FROM AD_Ref_List a, AD_Reference b"
			+ " WHERE b.Name=? AND a.Value=?" 
			+ " AND a.AD_Reference_ID = b.AD_Reference_ID"
			: 				
			"SELECT t.Description FROM AD_Reference r"
			+" INNER JOIN AD_Ref_List rl ON (r.AD_Reference_ID=rl.AD_Reference_ID)"
			+" INNER JOIN AD_Ref_List_Trl t ON (t.AD_Ref_List_ID=rl.AD_Ref_List_ID)"
			+" WHERE r.Name=? AND rl.Value=? AND t.AD_Language=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement (sql,null);
			pstmt.setString (1, listName);
			pstmt.setString(2, value);			
			if (!isBaseLanguage)
				pstmt.setString(3, language);
			rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = rs.getString(1);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		} catch (SQLException ex) {
			s_log.log(Level.SEVERE, sql, ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//
		return retValue;
	}	//	getListDescription
	
	/**
	 * Get Reference List (translated)
	 * @param ctx context
	 * @param referenceId reference
	 * @param optional if true add "",""
	 * @return List or null
	 */
	public static ValueNamePair[] getList (Properties ctx, int referenceId, boolean optional) {
		String language = Env.getAD_Language(ctx);
		ValueNamePair[] retValue = null;
		//	Validate for new method
		if(MADTranslation.isSupported()) {
			MReference reference = MReference.getById(Env.getCtx(), referenceId);
			StringBuffer sql = new StringBuffer("SELECT Value, "
					+ "getTranslation('AD_Ref_List', 'Name', AD_Ref_List_ID, ?, Name) AS Name "
					+ "FROM AD_Ref_List "
					+ "WHERE AD_Reference_ID = ? "
					+ "AND IsActive='Y'");
			if(reference.isOrderByValue()) {
				sql.append(" ORDER BY 1");
			} else {
				sql.append(" ORDER BY 2");
			}
			//	Get from DB
			List<Object> params = new ArrayList<Object>();
			params.add(language);
			params.add(referenceId);
			retValue = DB.getValueNamePairs(sql.toString(), optional, params);
		} else {
			retValue = getList(language, referenceId, optional);
		}
		//	Default Return
		return retValue;		
	}	//	getList


	/**
	 * Old method for get list
	 * @param language
	 * @param referenceId
	 * @param optional
	 * @return
	 */
	@Deprecated
	private static ValueNamePair[] getList(String language, int referenceId, boolean optional) {
		boolean isBaseLanguage = Env.isBaseLanguage(language, "AD_Ref_List");
		String sql = isBaseLanguage ?
			"SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=? AND IsActive='Y' ORDER BY Name"
			:
			"SELECT r.Value, t.Name FROM AD_Ref_List_Trl t"
			+ " INNER JOIN AD_Ref_List r ON (r.AD_Ref_List_ID=t.AD_Ref_List_ID)"
			+ " WHERE r.AD_Reference_ID=? AND t.AD_Language=? AND r.IsActive='Y'"
			+ " ORDER BY t.Name"
		;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<ValueNamePair> list = new ArrayList<ValueNamePair>();
		if (optional)
			list.add(new ValueNamePair("", ""));
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, referenceId);
			if (!isBaseLanguage)
				pstmt.setString(2, language);
			rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new ValueNamePair(rs.getString(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		ValueNamePair[] retValue = new ValueNamePair[list.size()];
		list.toArray(retValue);
		return retValue;		
	}	//	getList

	
	
	/**
	 * Get SQL for List
	 * @param language
	 * @param referenceValueId
	 * @return
	 */
	public static String getSQLForList(Language language, int referenceValueId) {
		String sql = null;
		if(MADTranslation.isSupported()) {
			MReference reference = MReference.getById(Env.getCtx(), referenceValueId);
			StringBuffer newSQL = new StringBuffer("SELECT NULL, Value, "
					+ "getTranslation('AD_Ref_List', 'Name', AD_Ref_List_ID, '" + language.getAD_Language() + "', Name) AS Name, "
					+ "AD_Ref_List.IsActive "
					+ "FROM AD_Ref_List "
					+ "WHERE AD_Ref_List.AD_Reference_ID = " + referenceValueId);
			if(reference.isOrderByValue()) {
				newSQL.append(" ORDER BY 2");
			} else {
				newSQL.append(" ORDER BY 3");
			}
			//	
			sql = newSQL.toString();
		} else {
			String byValue = DB.getSQLValueString(null, "SELECT IsOrderByValue FROM AD_Reference WHERE AD_Reference_ID = ? ", referenceValueId);
			StringBuffer realSQL = new StringBuffer ("SELECT NULL, AD_Ref_List.Value,");
			if (Env.isBaseLanguage(language, "AD_Ref_List"))
				realSQL.append("AD_Ref_List.Name,AD_Ref_List.IsActive FROM AD_Ref_List");
			else
				realSQL.append("trl.Name, AD_Ref_List.IsActive "
					+ "FROM AD_Ref_List INNER JOIN AD_Ref_List_Trl trl "
					+ " ON (AD_Ref_List.AD_Ref_List_ID=trl.AD_Ref_List_ID AND trl.AD_Language='")
						.append(language.getAD_Language()).append("')");
			realSQL.append(" WHERE AD_Ref_List.AD_Reference_ID=").append(referenceValueId);
			if ("Y".equals(byValue))
				realSQL.append(" ORDER BY 2");
			else
				realSQL.append(" ORDER BY 3"); // sort by name/translated name - teo_sarca, [ 1672820 ]
			//	Set SQL
			sql = realSQL.toString();
		}
		//	Return
		return sql;
	}
	
	/**	Logger							*/
	private static CLogger		s_log = CLogger.getCLogger (MRefList.class);
	/** Value Cache						*/
	private static CCache<String,String> cache = new CCache<String,String>(Table_Name, 20);


	/**************************************************************************
	 * 	Persistency Constructor
	 *	@param ctx context
	 *	@param AD_Ref_List_ID id
	 *	@param trxName transaction
	 */
	public MRefList (Properties ctx, int AD_Ref_List_ID, String trxName)
	{
		super (ctx, AD_Ref_List_ID, trxName);
		if (AD_Ref_List_ID == 0)
		{
		//	setAD_Reference_ID (0);
		//	setAD_Ref_List_ID (0);
			setEntityType (ENTITYTYPE_UserMaintained);	// U
		//	setName (null);
		//	setValue (null);
		}
	}	//	MRef_List

	/**
	 * 	Load Contructor
	 *	@param ctx context
	 *	@param rs result
	 *	@param trxName transaction
	 */
	public MRefList (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MRef_List

	/**
	 *	String Representation
	 * 	@return Name
	 */
	public String toString()
	{
		return getName();
	}	//	toString


}	//	MRef_List
