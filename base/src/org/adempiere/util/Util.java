/**
 * 
 */
package org.adempiere.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;
import org.compiere.model.MClient;
import org.compiere.model.PO;
import org.compiere.util.DisplayType;
import org.compiere.util.Language;

/**
 * Misc utils
 * @author Teo Sarca, www.arhipac.ro
 * @author Carlos Parada, cparada@erpya.com, ERPCyA http://www.erpya.com
 * 		<li> FR[ 2057 ] Add Method for Set File Path 
 * 		@see https://github.com/adempiere/adempiere/issues/2057
 */
public final class Util
{
	private Util()
	{
		// nothing
	}
	
	/**
	 * @param ctx
	 * @return DateFormat for current AD_Client's language 
	 */
	public static SimpleDateFormat getClientDateFormat(Properties ctx)
	{
		String lang = MClient.get(ctx).getAD_Language();
		return DisplayType.getDateFormat(Language.getLanguage(lang));
	}
	
	/**
	 * Check if strings are equal.
	 * We consider 2 strings equal if they both are null or they both are equal.
	 * @param s1
	 * @param s2
	 * @return true if string are equal
	 */
	public static boolean equals(String s1, String s2)
	{
		return (s1 == null && s2 == null)
			|| (s1 != null && s2 != null && s1.equals(s2));
	}
	
	/**
	 * Returns the archive path (snippet), containing client, org and archive
	 * id. The process, table and record id are only included when they are not
	 * null.
	 * FR[ 2057 ]
	 * @return String
	 */
	public static String getFilePathSnippet(PO po) {
		String path = po.get_TableName() + File.separator + po.getAD_Client_ID() + File.separator + po.getAD_Org_ID()
				 		+ File.separator; 
		if (po.get_ValueAsInt("AD_Process_ID") > 0) {
			path = path + po.get_ValueAsInt("AD_Process_ID") + File.separator;
		}
		if (po.get_ValueAsInt("AD_Table_ID") > 0) {
			path = path + po.get_ValueAsInt("AD_Table_ID") + File.separator;
		}
		if (po.get_ValueAsInt("Record_ID") > 0) {
			path = path + po.get_ValueAsInt("Record_ID") + File.separator;
		}
		return path;
	}

}
