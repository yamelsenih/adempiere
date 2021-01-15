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

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.util.MailTextWrapper;

/**
 * 	Request Mail Template Model.
 *	Cannot be cached as it holds PO/BPartner/User to parse
 *  @author Jorg Janke
 *  @version $Id: MMailText.java,v 1.3 2006/07/30 00:51:03 jjanke Exp $
 *  @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 * 		@see Add support to multiple Entities fos parse values</a>
 */
public class MMailText extends X_R_MailText
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9121875595478208460L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param R_MailText_ID id
	 *	@param trxName transaction
	 */
	public MMailText(Properties ctx, int R_MailText_ID, String trxName)
	{
		super (ctx, R_MailText_ID, trxName);
		wrapper = MailTextWrapper.newInstance(this);
	}	//	MMailText

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MMailText (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
		wrapper = MailTextWrapper.newInstance(this);
	}	//	MMailText

	private MailTextWrapper wrapper;
	
	/**
	 * 	Get parsed/translated Mail Text
	 *	@param all concatinate all
	 *	@return parsed/translated text
	 */
	public String getMailText(boolean all)
	{
		return wrapper.getMailText(all);
	}	//	getMailText

	/**
	 * 	Get parsed/translated Mail Text
	 *	@return parsed/translated text
	 */
	public String getMailText() {
		return wrapper.getMailText();
	}	//	getMailText
	
	/**
	 * 	Get parsed/translated Mail Text 2
	 *	@return parsed/translated text
	 */
	public String getMailText2() {
		return wrapper.getMailText2();
	}	//	getMailText2

	/**
	 * 	Get parsed/translated Mail Text 2
	 *	@return parsed/translated text
	 */
	public String getMailText3() {
		return wrapper.getMailText3();
	}	//	getMailText3

	/**
	 * 	Get parsed/translated Mail Header
	 *	@return parsed/translated text
	 */
	public String getMailHeader() {
		return wrapper.getMailHeader();
	}	//	getMailHeader
	
	/**
	 * 	Set User for parse
	 *	@param AD_User_ID user
	 */
	public void setUser (int AD_User_ID) {
		setPO(MUser.get(getCtx(), AD_User_ID));
	}	//	setUser
	
	/**
	 * 	Set User for parse
	 *	@param user user
	 */
	public void setUser (MUser user) {
		setPO(user);
	}	//	setUser
	
	/**
	 * 	Set BPartner for parse
	 *	@param C_BPartner_ID bp
	 */
	public void setBPartner (int C_BPartner_ID) {
		setPO(new MBPartner (getCtx(), C_BPartner_ID, get_TrxName()));
	}	//	setBPartner
	
	/**
	 * 	Set BPartner for parse
	 *	@param bpartner bp
	 */
	public void setBPartner (MBPartner bpartner) {
		setPO(bpartner);
	}	//	setBPartner

	/**
	 * 	Set PO for parse
	 *	@param entity po
	 */
	public void setPO (PO entity) {
		wrapper.setPO(entity);
	}	//	setPO

	/**
	 * 	Set PO for parse
	 *	@param po po
	 *	@param analyse if set to true, search for BPartner/User
	 */
	public void setPO (PO po, boolean analyse) {
		wrapper.setPO(po, analyse);
	}	//	setPO
	
	@Override
	public String toString() {
		return "MMailText [getMailHeader()=" + getMailHeader() + ", getR_MailText_ID()=" + getR_MailText_ID() + "]";
	}
}	//	MMailText
