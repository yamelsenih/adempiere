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

import java.math.BigDecimal;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClientInfo;
import org.compiere.model.MProduct;
import org.compiere.util.Env;
import org.eevolution.model.MDDFreight;
import org.eevolution.model.MDDFreightLine;
import org.eevolution.model.MWMInOutBound;
import org.eevolution.model.MWMInOutBoundLine;

/** Generated Process for (Generate Freight Order from Outbound Order)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.0
 */
public class GenerateFreightOrderFromOutbound extends GenerateFreightOrderFromOutboundAbstract {
	private int lineNo = 10;
	@Override
	protected String doIt() throws Exception {
		MDDFreight freightOrder = new MDDFreight(getCtx(), 0, get_TrxName());
		freightOrder.setWM_InOutBound_ID(getInOutBoundId());
		freightOrder.setDD_Driver_ID(getDriverId());
		freightOrder.setDD_Vehicle_ID(getVehicleId());
		freightOrder.setDateDoc(getDateDoc());
		freightOrder.setM_Shipper_ID(getShipperId());
		if(getDateOrdered() == null) {
			freightOrder.setDateOrdered(getDateDoc());
		} else {
			freightOrder.setDateOrdered(getDateOrdered());
		}
		if(getBPartnerId() > 0) {
			freightOrder.setC_BPartner_ID(getBPartnerId());
		}
		if(getDocTypeTargetId() > 0) {
			freightOrder.setC_DocType_ID(getDocTypeTargetId());
		}
		freightOrder.setDocAction(getDocAction());
		freightOrder.setFreightAmt(getFreightAmt());
		freightOrder.saveEx();
		//	Validate out bound
		if(getInOutBoundId() > 0) {
			addLineFromInOutBound(freightOrder, getInOutBoundId(), 0);
		} else {
			for(Integer inOutBoundLineId : getSelectionKeys()) {
				addLineFromInOutBound(freightOrder, 0, inOutBoundLineId);
			}
		}
		//	Complete
		freightOrder.processIt(getDocAction());
		freightOrder.saveEx();
		addLog(freightOrder.getDD_Freight_ID(), freightOrder.getDateDoc(), null, "@DD_Freight_ID@ @Created@: " + freightOrder.getDocumentInfo());
		return "@Ok@";
	}
	
	/**
	 * Add Line from in out bound order
	 * @param freightOrder
	 * @param inOutBoundId
	 */
	private void addLineFromInOutBound(MDDFreight freightOrder, int inOutBoundId, int inOutBoundLineId) {
		//	Add lines from Out bound
		MDDFreightLine freightLine = new MDDFreightLine(getCtx(), 0, get_TrxName());
		freightLine.setDD_Freight_ID(freightOrder.getDD_Freight_ID());
		freightLine.setLine(lineNo);
		if(inOutBoundLineId == 0) {
			MWMInOutBound outbound = new MWMInOutBound(getCtx(), inOutBoundId, get_TrxName());
			freightLine.setWeight(outbound.getWeight());
			freightLine.setVolume(outbound.getVolume());
		} else {
			MWMInOutBoundLine inOutboundLine = new MWMInOutBoundLine(getCtx(), inOutBoundLineId, get_TrxName());
			BigDecimal weight = Env.ZERO;
			BigDecimal volume = Env.ZERO;
			//	Set values from line
			if(inOutboundLine.getM_Product_ID() != 0) {
				freightLine.setM_Product_ID(inOutboundLine.getM_Product_ID());
				MProduct product = MProduct.get(getCtx(), inOutboundLine.getM_Product_ID());
				//	Weight
				if(product.getWeight() != null) {
					weight = product.getWeight().multiply(inOutboundLine.getMovementQty());
				}
				//	Volume
				if(product.getVolume() != null) {
					weight = product.getVolume().multiply(inOutboundLine.getMovementQty());
				}
			}
			if(inOutboundLine.getC_Charge_ID() != 0) {
				freightLine.setC_Charge_ID(inOutboundLine.getC_Charge_ID());
			}
			
			freightLine.setWeight(weight);
			freightLine.setVolume(volume);
			//	Reference
			freightLine.set_ValueOfColumn("WM_InOutBoundLine_ID", inOutboundLine.getWM_InOutBoundLine_ID());
		}
		//	Set from client
		MClientInfo clientInfo = MClientInfo.get(getCtx());
		//	Weight
		if(clientInfo.getC_UOM_Weight_ID() <= 0) {
			throw new AdempiereException("@C_UOM_Weight_ID@ @NotFound@ @SeeClientInfoConfig@");
		}
		//	Volume
		if(clientInfo.getC_UOM_Volume_ID() <= 0) {
			throw new AdempiereException("@C_UOM_Volume_ID@ @NotFound@ @SeeClientInfoConfig@");
		}
		//	Set values
		freightLine.setWeight_UOM_ID(clientInfo.getC_UOM_Weight_ID());
		freightLine.setVolume_UOM_ID(clientInfo.getC_UOM_Volume_ID());
		freightLine.setM_Freight_ID(getFreightId());
		freightLine.setFreightAmt(getFreightAmt());
		freightLine.saveEx();
		lineNo += 10;
	}
}