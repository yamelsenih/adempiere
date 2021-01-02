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
 * Copyright (C) 2012-2019 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpya.com				  		                 *
 *************************************************************************************/

package org.adempiere.pos.command;

import org.adempiere.pos.service.CPOS;
import org.adempiere.pos.util.POSTicketHandler;
import org.compiere.model.MPOS;

/**
 * @contributor Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class CommandPrintDocument extends CommandAbstract implements Command {
    public CommandPrintDocument(String command, String event) {
        super.command = command;
        super.event = event;
    }

    @Override
    public void execute(CommandReceiver commandReceiver) {
    	CPOS pos = new CPOS();
    	pos.setM_POS(MPOS.get(commandReceiver.getCtx(), commandReceiver.getPOSId()));
    	pos.setOrder(commandReceiver.getOrderId());
    	POSTicketHandler ticketHandler = POSTicketHandler.getTicketHandler(pos);
		if(ticketHandler == null)
			return;
		//	
		ticketHandler.printTicket();
    }
}
