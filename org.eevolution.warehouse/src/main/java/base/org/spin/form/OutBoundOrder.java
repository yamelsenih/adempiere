/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2013 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpya.com                                  *
 *****************************************************************************/
package org.spin.form;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.DocTypeNotFoundException;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MDocType;
import org.compiere.model.MOrderLine;
import org.compiere.model.MRefList;
import org.compiere.model.MRole;
import org.compiere.model.MUOM;
import org.compiere.model.X_C_Order;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.compiere.util.ValueNamePair;
import org.eevolution.model.MDDOrderLine;
import org.eevolution.model.MWMInOutBound;
import org.eevolution.model.MWMInOutBoundLine;

/**
 * @author Yamel Senih 24/06/2011, 12:57
 * @author Carlos Parada, cparada@erpcya.com
 * <li> FR [ 1 ] Add Support to generate load order with non stocked product
 * @see https://github.com/erpcya/FTA/issues/1
 *
 */
public class OutBoundOrder {

	/**	Logger									*/
	public static CLogger log = CLogger.getCLogger(OutBoundOrder.class);
	
	public final int SELECT 					= 0;
	public final int ORDER 						= 2;
	/**	Lines									*/
	public final int OL_WAREHOUSE 				= 1;
	public final int ORDER_LINE 				= 2;
	public final int OL_PRODUCT 				= 3;
	public final int OL_UOM 					= 4;
	public final int OL_QTY_ON_HAND 			= 5;
	public final int OL_QTY 					= 6;
	public final int OL_WEIGHT 					= 7;
	public final int OL_VOLUME 					= 8;
	public final int OL_SEQNO 					= 9;
	public final int OL_QTY_ORDERED 			= 10;
	public final int OL_UOM_CONVERSION 			= 11;
	public final int OL_QTY_RESERVERD 			= 12;
	public final int OL_QTY_INVOICED 			= 13;
	public final int OL_QTY_DELIVERED 			= 14;
	public final int OL_QTY_IN_TRANSIT 			= 15;
	public final int OL_DELIVERY_RULE 			= 16;
	/**	Warehouse and Product					*/
	public final int SW_PRODUCT 				= 0;
	public final int SW_UOM 					= 1;
	public final int SW_WAREHOUSE 				= 2;
	public final int SW_QTY_ON_HAND 			= 3;
	public final int SW_QTY_IN_TRANSIT 			= 4;
	public final int SW_QTY_SET 				= 5;
	public final int SW_QTY_AVAILABLE 			= 6;
	
	/**	Buffer				*/
	public Vector<BufferTableSelect> m_BufferSelect = null;
	
	public StringBuffer 		m_Symmary = new StringBuffer();
	public StringBuffer 		m_QueryAdd = new StringBuffer();
	
	/**	Client				*/
	protected int 				m_AD_Client_ID = 0;
	/**	Organization		*/
	protected int 				m_AD_Org_ID = 0;
	/**	Warehouse			*/
	protected int 				m_C_SalesRegion_ID = 0;
	/**	Sales Rep			*/
	protected int 				m_SalesRep_ID = 0;
	/**	Warehouse			*/
	protected int 				warehouseId = 0;
	/**	Operation Type		*/
	protected String 			m_OperationType = null;
	/**	Document Type 		*/
	protected int 				m_C_DocType_ID = 0;
	/**	Document Type Target*/
	protected int 				docTypeTargetId = 0;
	/**	Invoice Rule		*/
	protected String 			invoiceRule = null;
	/**	Delivery Rule		*/
	protected String 			deliveryRule = null;
	/**	Vehicle Type		*/
	protected int 				m_FTA_VehicleType_ID = 0;
	/**	Document Date		*/
	protected Timestamp			m_DateDoc = null;
	/**	Shipment Date		*/
	protected Timestamp			m_ShipDate = null;
	/**	Entry Ticket		*/
	protected int 				m_FTA_EntryTicket_ID = 0;
	/**	Shipper				*/
	protected int 				shipperId = 0;
	/**	Driver				*/
	protected int 				m_FTA_Driver_ID = 0;
	/**	Vehicle				*/
	protected int 				m_FTA_Vehicle_ID = 0;
	/**	Load Capacity		*/
	protected BigDecimal 		m_LoadCapacity = Env.ZERO;
	/**	Volume Capacity		*/
	protected BigDecimal 		m_VolumeCapacity = Env.ZERO;
	/**	Weight Unit Measure	*/
	protected int 				m_C_UOM_Weight_ID = 0;
	/**	Volume Unit Measure	*/
	protected int 				m_C_UOM_Volume_ID = 0;
	/**	Weight Precision	*/
	protected int 				m_WeightPrecision = 0;
	/**	Volume Precision	*/
	protected int 				m_VolumePrecision = 0;
	/**	Rows Selected		*/
	protected int				m_RowsSelected = 0;
	/**	Is Bulk Product		*/
	protected boolean			m_IsBulk = false;
	/**	UOM Weight Symbol	*/
	protected String 			m_UOM_Weight_Symbol = null;
	/**	UOM Volume Symbol	*/
	protected String 			m_UOM_Volume_Symbol = null;
	/**	Product				*/
	protected int				m_M_Product_ID = 0;
	/**	Business Partner	*/
	protected int				m_C_BPartner_ID = 0;
	
	/**	Total Weight		*/
	protected BigDecimal		totalWeight = Env.ZERO;
	/**	Total Volume		*/
	protected BigDecimal		totalVolume = Env.ZERO;
	
	/**	Max Sequence		*/
	protected int				m_MaxSeqNo = 0;
	
	/**	Validate Quantity	*/
	protected boolean 			m_IsValidateQuantity = true;
	
	/**	Load Order			*/
	protected MWMInOutBound  	outBoundOrder = null;	//	outBoundOrder
	
	/**	Material Movement	*/
	private final String 		MATERIAL_MOVEMENT = "MM";
	
	/**
	 * Get Order data from parameters
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 09/12/2013, 14:10:10
	 * @return Vector<Vector<Object>>
	 */
	protected Vector<Vector<Object>> getOrderData(IMiniTable orderTable, String operationType) {
		//	Load Validation Flag
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		if(docTypeTargetId > 0) { 
			MDocType m_DocType = MDocType.get(Env.getCtx(), docTypeTargetId);
			m_IsValidateQuantity = m_DocType.get_ValueAsBoolean("IsValidateQuantity");
		}
		//	
		/**
		 * 2014-12-02 Carlos Parada Add Support to DD_Order
		 */
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		StringBuffer sql = null;
		if (operationType.equals(MATERIAL_MOVEMENT)) {
			//Query for Material Movement
			sql = new StringBuffer("SELECT " +
					"wr.Name Warehouse, ord.DD_Order_ID, ord.DocumentNo, " +	//	1..3
					"ord.DateOrdered, ord.DatePromised, reg.Name, cit.Name, sr.Name SalesRep, " +	//	4..8
					"cp.Name Partner, bploc.Name, " +	//	9..10
					"loc.Address1, loc.Address2, loc.Address3, loc.Address4, ord.C_BPartner_Location_ID, ord.Weight, ord.Volume " +	//	11..17
					"FROM DD_Order ord " +
					"INNER JOIN DD_OrderLine lord ON(lord.DD_Order_ID = ord.DD_Order_ID) " +
					"INNER JOIN M_Product pr ON(pr.M_Product_ID = lord.M_Product_ID) " +
					"INNER JOIN C_BPartner cp ON(cp.C_BPartner_ID = ord.C_BPartner_ID) " +
					"INNER JOIN AD_User sr ON(sr.AD_User_ID = ord.SalesRep_ID) " +
					"INNER JOIN M_Warehouse wr ON(wr.M_Warehouse_ID = ord.M_Warehouse_ID) " +
					"INNER JOIN C_BPartner_Location bploc ON(bploc.C_BPartner_Location_ID = ord.C_BPartner_Location_ID) " +
					"INNER JOIN C_Location loc ON(loc.C_Location_ID = bploc.C_Location_ID) " +
					"LEFT JOIN C_Region reg ON(reg.C_Region_ID = loc.C_Region_ID) " +
					"LEFT JOIN C_City cit ON(cit.C_City_ID = loc.C_City_ID) " +
					"LEFT JOIN (SELECT lord.DD_OrderLine_ID, " +
					"	(COALESCE(lord.QtyOrdered, 0) - " +
					"		SUM(" +
					"				CASE WHEN (c.IsMoved = 'N' AND c.OperationType = 'MOM' AND c.DocStatus = 'CO') " +
					"						THEN COALESCE(lc.ConfirmedQty, lc.Qty, 0) " +
					"						ELSE 0 " +
					"				END" +
					"			)" +
					"	) QtyAvailable " +
					"	FROM DD_OrderLine lord " +
					"	LEFT JOIN WM_InOutBoundLine lc ON(lc.DD_OrderLine_ID = lord.DD_OrderLine_ID) " +
					"	LEFT JOIN WM_InOutBound c ON(c.WM_InOutBound_ID = lc.WM_InOutBound_ID) " +
					"	WHERE lord.M_Product_ID IS NOT NULL " +
					"	GROUP BY lord.DD_Order_ID, lord.DD_OrderLine_ID, lord.QtyOrdered " +
					"	ORDER BY lord.DD_OrderLine_ID ASC) qafl " +
					"	ON(qafl.DD_OrderLine_ID = lord.DD_OrderLine_ID) " +
					"WHERE  wr.IsActive = 'Y' " +
					"AND ord.DocStatus = 'CO' " +
					"AND ord.DocStatus = 'CO' " +
					"AND COALESCE(qafl.QtyAvailable, 0) > 0 " +
					"AND ord.AD_Client_ID=? ");
			if (m_AD_Org_ID != 0)
				sql.append("AND lord.AD_Org_ID=? ");
			if (warehouseId != 0 )
				sql.append("AND lord.M_Warehouse_ID=? ");
			if (m_C_SalesRegion_ID != 0 )
				sql.append("AND bploc.C_SalesRegion_ID=? ");
			if (m_SalesRep_ID != 0 )
				sql.append("AND ord.SalesRep_ID=? ");
			if (m_C_DocType_ID != 0 )
				sql.append("AND ord.C_DocType_ID=? ");
			if(m_IsBulk) {
				sql.append("AND lord.M_Product_ID=? ");
				sql.append("AND ord.C_BPartner_ID=? ");
			}
			
			//	Group By
			sql.append("GROUP BY wr.Name, ord.DD_Order_ID, ord.DocumentNo, ord.DateOrdered, " +
					"ord.DatePromised, ord.Weight, ord.Volume, sr.Name, cp.Name, bploc.Name, " +
					"reg.Name, cit.Name, loc.Address1, loc.Address2, loc.Address3, loc.Address4, ord.C_BPartner_Location_ID ");
		
			//	Having
			sql.append("HAVING (SUM(COALESCE(lord.QtyOrdered, 0)) - SUM(COALESCE(lord.QtyInTransit, 0)) - SUM(COALESCE(lord.QtyDelivered, 0))) > 0 ");
			
			
			//	Order By
			sql.append("ORDER BY ord.DD_Order_ID ASC");
			
			// role security
		} else {//Query for Sales Order
			sql = new StringBuffer("SELECT " +
					"wr.Name Warehouse, ord.C_Order_ID, ord.DocumentNo, " +	//	1..3
					"ord.DateOrdered, ord.DatePromised, reg.Name, cit.Name, sr.Name SalesRep, " +	//	4..8
					"cp.Name Partner, bploc.Name, " +	//	9..10
					"loc.Address1, loc.Address2, loc.Address3, loc.Address4, ord.C_BPartner_Location_ID, ord.Weight, ord.Volume " +	//	11..17
					"FROM C_Order ord " +
					"INNER JOIN C_OrderLine lord ON(lord.C_Order_ID = ord.C_Order_ID) " +
					"INNER JOIN M_Product pr ON(pr.M_Product_ID = lord.M_Product_ID) " +
					"INNER JOIN C_BPartner cp ON(cp.C_BPartner_ID = ord.C_BPartner_ID) " +
					"INNER JOIN M_Warehouse wr ON(wr.M_Warehouse_ID = ord.M_Warehouse_ID) " +
					"INNER JOIN C_BPartner_Location bploc ON(bploc.C_BPartner_Location_ID = ord.C_BPartner_Location_ID) " +
					"INNER JOIN C_Location loc ON(loc.C_Location_ID = bploc.C_Location_ID) " +
					"LEFT JOIN AD_User sr ON(sr.AD_User_ID = ord.SalesRep_ID) " +
					"LEFT JOIN C_Region reg ON(reg.C_Region_ID = loc.C_Region_ID) " +
					"LEFT JOIN C_City cit ON(cit.C_City_ID = loc.C_City_ID) " +
					"LEFT JOIN (SELECT lord.C_OrderLine_ID, " +
					"	(COALESCE(lord.QtyOrdered, 0) - " +
					"		SUM(" +
					"				CASE WHEN (c.IsDelivered = 'N' AND c.OperationType IN('DBM', 'DFP') AND c.DocStatus = 'CO') " +
					"						THEN COALESCE(lc.ConfirmedQty, lc.Qty, 0) " +
					"						ELSE 0 " +
					"				END" +
					"			)" +
					"	) QtyAvailable " +
					"	FROM C_OrderLine lord " +
					"	LEFT JOIN WM_InOutBoundLine lc ON(lc.C_OrderLine_ID = lord.C_OrderLine_ID) " +
					"	LEFT JOIN WM_InOutBound c ON(c.WM_InOutBound_ID = lc.WM_InOutBound_ID) " +
					"	WHERE lord.M_Product_ID IS NOT NULL " +
					"	GROUP BY lord.C_Order_ID, lord.C_OrderLine_ID, lord.QtyOrdered " +
					"	ORDER BY lord.C_OrderLine_ID ASC) qafl " +
					"	ON(qafl.C_OrderLine_ID = lord.C_OrderLine_ID) " +
					"WHERE ord.IsSOTrx = 'Y' " +
					"AND wr.IsActive = 'Y' " +
					"AND ord.DocStatus = 'CO' " +
					//FR [ 1 ]
					//"AND pr.IsStocked = 'Y' " +
					"AND COALESCE(qafl.QtyAvailable, 0) > 0 " +
					"AND ord.AD_Client_ID=? ");
			if (m_AD_Org_ID != 0)
				sql.append("AND lord.AD_Org_ID=? ");
			if (warehouseId != 0 )
				sql.append("AND lord.M_Warehouse_ID=? ");
			if (m_C_SalesRegion_ID != 0 )
				sql.append("AND bploc.C_SalesRegion_ID=? ");
			if (m_SalesRep_ID != 0 )
				sql.append("AND ord.SalesRep_ID=? ");
			if (m_C_DocType_ID != 0 )
				sql.append("AND ord.C_DocType_ID=? ");
			if(m_IsBulk) {
				sql.append("AND lord.M_Product_ID=? ");
				sql.append("AND ord.C_BPartner_ID=? ");
			}
			
			//	Group By
			sql.append("GROUP BY wr.Name, ord.C_Order_ID, ord.DocumentNo, ord.DateOrdered, " +
					"ord.DatePromised, ord.Weight, ord.Volume, sr.Name, cp.Name, bploc.Name, " +
					"reg.Name, cit.Name, loc.Address1, loc.Address2, loc.Address3, loc.Address4, ord.C_BPartner_Location_ID ");
		
			//	Having
			sql.append("HAVING (SUM(COALESCE(lord.QtyOrdered, 0)) - SUM(COALESCE(lord.QtyDelivered, 0))) > 0 ");
			
			
			//	Order By
			sql.append("ORDER BY ord.C_Order_ID ASC");
			
			// role security
		}
		
		/** End Carlos Parada **/
		log.fine("LoadOrderSQL=" + sql.toString());
		//	
		try {
			//	
			int param = 1;
			int column = 1;
			
			pstmt = DB.prepareStatement(sql.toString(), null);
			
			pstmt.setInt(param++, Env.getAD_Client_ID(Env.getCtx()));
			
			if (m_AD_Org_ID != 0)
				pstmt.setInt(param++, m_AD_Org_ID);
			if (warehouseId != 0 )
				pstmt.setInt(param++, warehouseId);
			if (m_C_SalesRegion_ID != 0 )
				pstmt.setInt(param++, m_C_SalesRegion_ID);
			if (m_SalesRep_ID != 0 )
				pstmt.setInt(param++, m_SalesRep_ID);
			if (m_C_DocType_ID != 0 )
				pstmt.setInt(param++, m_C_DocType_ID);
			if(m_IsBulk) {
				pstmt.setInt(param++, m_M_Product_ID);
				pstmt.setInt(param++, m_C_BPartner_ID);
			}
			
			log.fine("AD_Org_ID=" + m_AD_Org_ID);
			log.fine("M_Warehouse_ID=" + warehouseId);
			log.fine("SalesRep_ID=" + m_SalesRep_ID);
			log.fine("C_DocType_ID=" + m_C_DocType_ID);
			log.fine("IsBulk=" + m_IsBulk);
			
			rs = pstmt.executeQuery();
			while (rs.next()) {
				column = 1;
				Vector<Object> line = new Vector<Object>();
				line.add(new Boolean(false));       		//  0-Selection
				line.add(rs.getString(column++));       	//  1-Warehouse
				KeyNamePair pp = new KeyNamePair(rs.getInt(column++), rs.getString(column++));
				line.add(pp);				       			//  2-DocumentNo
				line.add(rs.getTimestamp(column++));      	//  3-DateOrdered
				line.add(rs.getTimestamp(column++));      	//  4-DatePromised
				line.add(rs.getString(column++));			//	5-Region
				line.add(rs.getString(column++));			//	6-City
				line.add(rs.getString(column++));			//	7-Sales Representative
				line.add(rs.getString(column++));			//	8-Business Partner
				line.add(rs.getString(column++));			//	9-Location
				line.add(rs.getString(column++));			//	10-Address 1
				line.add(rs.getString(column++));			//	11-Address 2
				line.add(rs.getString(column++));			//	12-Address 3
				line.add(rs.getString(column++));			//	13-Address 4
				line.add(rs.getBigDecimal(column++));		//	14-Weight
				line.add(rs.getBigDecimal(column++));		//	15-Volume
				//
				data.add(line);
			}
			
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally{
			DB.close(rs, pstmt);
		}
		
		return data;
	}
	
	/**
	 * Get Order Line Data
	 * @param orderTable
	 * @return
	 * @return StringBuffer
	 */
	protected StringBuffer getQueryLine(IMiniTable orderTable,String p_OperationType) {
		StringBuffer sql = null;
				
		log.config("getQueryLine");
		
		/** 2014-12-02 Carlos Parada Add Support to DD_OrderLine */ 
		if (p_OperationType.equals(MATERIAL_MOVEMENT)) {
			int rows = orderTable.getRowCount();
			m_RowsSelected = 0;
			StringBuffer sqlWhere = new StringBuffer("ord.DD_Order_ID IN(0"); 
			for (int i = 0; i < rows; i++) {
				if (((Boolean)orderTable.getValueAt(i, 0)).booleanValue()) {
					int ID = ((KeyNamePair)orderTable.getValueAt(i, ORDER)).getKey();
					sqlWhere.append(",");
					sqlWhere.append(ID);
					m_RowsSelected ++;
				}
			}
			sqlWhere.append(")");
			
			sql = new StringBuffer("SELECT alm.M_Warehouse_ID, alm.Name Warehouse, lord.DD_OrderLine_ID, ord.DocumentNo, lord.M_Product_ID, " + 
					"(pro.Name || COALESCE(' - ' || productattribute(lord.M_AttributeSetInstance_ID), '')) Product, " +
					"pro.C_UOM_ID, uomp.UOMSymbol, s.QtyOnHand, " +
					"lord.QtyOrdered, lord.C_UOM_ID, uom.UOMSymbol, lord.QtyReserved, 0 QtyInvoiced, lord.QtyDelivered, " +
					"SUM(" +
					"		COALESCE(CASE " +
					"			WHEN (c.IsMoved = 'N' AND c.OperationType = 'MOM' AND c.DocStatus = 'CO') " +
					"			THEN lc.Qty " +
					"			ELSE 0 " +
					"		END, 0)" +
					") QtyLoc, " +
					"(COALESCE(lord.QtyOrdered, 0) - COALESCE(lord.QtyInTransit, 0) - COALESCE(lord.QtyDelivered, 0) - " +
					"	SUM(" +
					"		COALESCE(CASE " +
					"			WHEN (c.IsMoved = 'N' AND c.OperationType = 'MOM' AND c.DocStatus = 'CO') " +
					"			THEN lc.Qty " +
					"			ELSE 0 " +
					"		END, 0)" +
					"		)" +
					") Qty, " +
					"pro.Weight, pro.Volume, ord.DeliveryRule, pro.IsStocked " +
					"FROM DD_Order ord " +
					"INNER JOIN DD_OrderLine lord ON(lord.DD_Order_ID = ord.DD_Order_ID) " +
					"INNER JOIN M_Locator l ON(l.M_Locator_ID = lord.M_Locator_ID) " + 
					"INNER JOIN M_Warehouse alm ON(alm.M_Warehouse_ID = l.M_Warehouse_ID) " +
					"INNER JOIN M_Product pro ON(pro.M_Product_ID = lord.M_Product_ID) " +
					"INNER JOIN C_UOM uom ON(uom.C_UOM_ID = lord.C_UOM_ID) " +
					"INNER JOIN C_UOM uomp ON(uomp.C_UOM_ID = pro.C_UOM_ID) " +
					"LEFT JOIN WM_InOutBoundLine lc ON(lc.DD_OrderLine_ID = lord.DD_OrderLine_ID) " +
					"LEFT JOIN WM_InOutBound c ON(c.WM_InOutBound_ID = lc.WM_InOutBound_ID) " +
					"LEFT JOIN (" +
					"				SELECT l.M_Warehouse_ID, st.M_Product_ID, " +
					"					COALESCE(SUM(st.QtyOnHand), 0) QtyOnHand, " +
					"					COALESCE(st.M_AttributeSetInstance_ID, 0) M_AttributeSetInstance_ID " +
					"				FROM M_Storage st " +
					"				INNER JOIN M_Locator l ON(l.M_Locator_ID = st.M_Locator_ID) " +
					"			GROUP BY l.M_Warehouse_ID, st.M_Product_ID, st.M_AttributeSetInstance_ID) s " +
					"														ON(s.M_Product_ID = lord.M_Product_ID " +
					"																AND s.M_Warehouse_ID = l.M_Warehouse_ID " +
					"																AND lord.M_AttributeSetInstance_ID = s.M_AttributeSetInstance_ID) ")
					.append("WHERE "/*FR [ 1 ]
							+ "pro.IsStocked = 'Y' ")
					.append("AND "*/)
					.append(sqlWhere).append(" ");
			//	Add Where
			if(m_IsBulk)
				sql.append("AND lord.M_Product_ID = ?").append(" ");
			//	Group By
			sql.append("GROUP BY alm.M_Warehouse_ID, lord.DD_Order_ID, lord.DD_OrderLine_ID, " +
					"alm.Name, ord.DocumentNo, lord.M_Product_ID, lord.M_AttributeSetInstance_ID, " + 
					"pro.Name, lord.C_UOM_ID, uom.UOMSymbol, lord.QtyEntered, " +
					"pro.C_UOM_ID, uomp.UOMSymbol, lord.QtyOrdered, lord.QtyReserved, " +
					"lord.QtyDelivered, pro.Weight, pro.Volume, ord.DeliveryRule, s.QtyOnHand,pro.IsStocked").append(" ");
			//	Having
			sql.append("HAVING (COALESCE(lord.QtyOrdered, 0) - COALESCE(lord.QtyInTransit, 0) - COALESCE(lord.QtyDelivered, 0) - " + 
					"								SUM(" +
					"									COALESCE(CASE " +
					"										WHEN (c.IsMoved = 'N' AND c.OperationType = 'MOM' AND c.DocStatus = 'CO') " +
					"											THEN lc.Qty " +
					"											ELSE 0 " +
					"										END, 0)" +
					"								)" +
					"			) > 0 OR pro.IsStocked = 'N' ").append(" ");
			//	Order By
				sql.append("ORDER BY lord.DD_Order_ID ASC");
			
		}
		else{

			int rows = orderTable.getRowCount();
			m_RowsSelected = 0;
			StringBuffer sqlWhere = new StringBuffer("ord.C_Order_ID IN(0"); 
			for (int i = 0; i < rows; i++) {
				if (((Boolean)orderTable.getValueAt(i, 0)).booleanValue()) {
					int ID = ((KeyNamePair)orderTable.getValueAt(i, ORDER)).getKey();
					sqlWhere.append(",");
					sqlWhere.append(ID);
					m_RowsSelected ++;
				}
			}
			sqlWhere.append(")");
			
			sql = new StringBuffer("SELECT lord.M_Warehouse_ID, alm.Name Warehouse, lord.C_OrderLine_ID, ord.DocumentNo, lord.M_Product_ID, " + 
					"(pro.Name || COALESCE(' - ' || productattribute(lord.M_AttributeSetInstance_ID), '')) Product, " +
					"pro.C_UOM_ID, uomp.UOMSymbol, s.QtyOnHand, " +
					"lord.QtyOrdered, lord.C_UOM_ID, uom.UOMSymbol, lord.QtyReserved, lord.QtyInvoiced, lord.QtyDelivered, " +
					"SUM(" +
					"		COALESCE(CASE " +
					"			WHEN (c.IsDelivered = 'N' AND c.OperationType IN('DBM', 'DFP') AND c.DocStatus = 'CO') " +
					"			THEN lc.Qty " +
					"			ELSE 0 " +
					"		END, 0)" +
					") QtyLoc, " +
					"(COALESCE(lord.QtyOrdered, 0) - COALESCE(lord.QtyDelivered, 0) - " +
					"	SUM(" +
					"		COALESCE(CASE " +
					"			WHEN (c.IsDelivered = 'N' AND c.OperationType IN('DBM', 'DFP') AND c.DocStatus = 'CO') " +
					"			THEN lc.Qty " +
					"			ELSE 0 " +
					"		END, 0)" +
					"		)" +
					") Qty, " +
					"pro.Weight, pro.Volume, ord.DeliveryRule, pro.IsStocked " +
					"FROM C_Order ord " +
					"INNER JOIN C_OrderLine lord ON(lord.C_Order_ID = ord.C_Order_ID) " +
					"INNER JOIN M_Warehouse alm ON(alm.M_Warehouse_ID = lord.M_Warehouse_ID) " +
					"INNER JOIN M_Product pro ON(pro.M_Product_ID = lord.M_Product_ID) " +
					"INNER JOIN C_UOM uom ON(uom.C_UOM_ID = lord.C_UOM_ID) " +
					"INNER JOIN C_UOM uomp ON(uomp.C_UOM_ID = pro.C_UOM_ID) " +
					"LEFT JOIN WM_InOutBoundLine lc ON(lc.C_OrderLine_ID = lord.C_OrderLine_ID) " +
					"LEFT JOIN WM_InOutBound c ON(c.WM_InOutBound_ID = lc.WM_InOutBound_ID) " +
					"LEFT JOIN (" +
					"				SELECT l.M_Warehouse_ID, st.M_Product_ID, " +
					"					COALESCE(SUM(st.QtyOnHand), 0) QtyOnHand, " +
					"					COALESCE(st.M_AttributeSetInstance_ID, 0) M_AttributeSetInstance_ID " +
					"				FROM M_Storage st " +
					"				INNER JOIN M_Locator l ON(l.M_Locator_ID = st.M_Locator_ID) " +
					"			GROUP BY l.M_Warehouse_ID, st.M_Product_ID, st.M_AttributeSetInstance_ID) s " +
					"														ON(s.M_Product_ID = lord.M_Product_ID " +
					"																AND s.M_Warehouse_ID = lord.M_Warehouse_ID " +
					"																AND lord.M_AttributeSetInstance_ID = s.M_AttributeSetInstance_ID) ")
					.append("WHERE " /* FR [ 1 ]
							+ "pro.IsStocked = 'Y' ")
					.append("AND "*/)
					.append(sqlWhere).append(" ");
			//	Add Where
			if(m_IsBulk)
				sql.append("AND lord.M_Product_ID = ?").append(" ");
			//	Group By
			sql.append("GROUP BY lord.M_Warehouse_ID, lord.C_Order_ID, lord.C_OrderLine_ID, " +
					"alm.Name, ord.DocumentNo, lord.M_Product_ID, lord.M_AttributeSetInstance_ID, " + 
					"pro.Name, lord.C_UOM_ID, uom.UOMSymbol, lord.QtyEntered, " +
					"pro.C_UOM_ID, uomp.UOMSymbol, lord.QtyOrdered, lord.QtyReserved, " + 
					"lord.QtyDelivered, lord.QtyInvoiced, pro.Weight, pro.Volume, ord.DeliveryRule, s.QtyOnHand, pro.IsStocked").append(" ");
			//	Having
			sql.append("HAVING (COALESCE(lord.QtyOrdered, 0) - COALESCE(lord.QtyDelivered, 0) - " + 
					"									SUM(" +
					"										COALESCE(CASE " +
					"											WHEN (c.IsDelivered = 'N' AND c.OperationType IN('DBM', 'DFP') AND c.DocStatus = 'CO') " +
					"											THEN lc.Qty " +
					"											ELSE 0 " +
					"										END, 0)" +
					"									)" +
					"			) > 0  OR pro.IsStocked = 'N' ").append(" ");
			//	Order By
			sql.append("ORDER BY lord.C_Order_ID ASC");
			
		}
		//	
		log.fine("SQL Line Order=" + sql.toString());
		//	Return
		return sql;
	}
	
	/**
	 * Get Order Column Names
	 * @return
	 * @return Vector<String>
	 */
	protected Vector<String> getOrderColumnNames() {	
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.translate(Env.getCtx(), "Select"));
		columnNames.add(Msg.translate(Env.getCtx(), "M_Warehouse_ID"));
		columnNames.add(Util.cleanAmp(Msg.translate(Env.getCtx(), "DocumentNo")));
		columnNames.add(Msg.translate(Env.getCtx(), "DateOrdered"));
		columnNames.add(Msg.translate(Env.getCtx(), "DatePromised"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_Region_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_City_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "SalesRep_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_Location_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "Address1"));
		columnNames.add(Msg.translate(Env.getCtx(), "Address2"));
		columnNames.add(Msg.translate(Env.getCtx(), "Address3"));
		columnNames.add(Msg.translate(Env.getCtx(), "Address4"));
		columnNames.add(Msg.translate(Env.getCtx(), "Weight"));
		columnNames.add(Msg.translate(Env.getCtx(), "Volume"));
		//	
		return columnNames;
	}
	
	/**
	 * Set Order Column Class on Table
	 * @param orderTable
	 * @return void
	 */
	protected void setOrderColumnClass(IMiniTable orderTable) {
		int i = 0;
		orderTable.setColumnClass(i++, Boolean.class, false);		//  0-Selection
		orderTable.setColumnClass(i++, String.class, true);			//  1-Warehouse
		orderTable.setColumnClass(i++, String.class, true);			//  2-DocumentNo
		orderTable.setColumnClass(i++, Timestamp.class, true);		//  3-DateOrdered
		orderTable.setColumnClass(i++, Timestamp.class, true);		//  4-DatePromiset
		orderTable.setColumnClass(i++, String.class, true);			//  5-Region
		orderTable.setColumnClass(i++, String.class, true);			//  6-City
		orderTable.setColumnClass(i++, String.class, true);			//  7-Sales Representative
		orderTable.setColumnClass(i++, String.class, true);			//  8-Business Partner
		orderTable.setColumnClass(i++, String.class, true);			//  9-Location
		orderTable.setColumnClass(i++, String.class, true);			//  10-Address 1
		orderTable.setColumnClass(i++, String.class, true);			//  11-Address 2
		orderTable.setColumnClass(i++, String.class, true);			//  12-Address 3
		orderTable.setColumnClass(i++, String.class, true);			//  13-Address 4
		orderTable.setColumnClass(i++, BigDecimal.class, true);		//  14-Weight
		orderTable.setColumnClass(i++, BigDecimal.class, true);		//  15-Volume
		//	
		//  Table UI
		orderTable.autoSize();
	}
	
	/**
	 * Get Order Line Data
	 * @param orderLineTable
	 * @param sqlPrep
	 * @return
	 * @return Vector<Vector<Object>>
	 */
	protected Vector<Vector<Object>> getOrderLineData(IMiniTable orderLineTable, StringBuffer sqlPrep) {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();		
		PreparedStatement pstmt = null;
		ResultSet rs= null;
		
		log.fine("LoadOrderLineSQL=" + sqlPrep.toString());
		try {
			
			pstmt = DB.prepareStatement(sqlPrep.toString(), null);
			//	Parameter
			int param = 1;
			//	
			if(m_IsBulk)
				pstmt.setInt(param++, m_M_Product_ID);
			//	
			rs = pstmt.executeQuery();
			int column = 1;
			KeyNamePair m_Warehouse = null;
			KeyNamePair m_DocumentNo = null;
			KeyNamePair m_Product = null;
			KeyNamePair m_Product_UOM = null;
			KeyNamePair m_Order_UOM = null;
			BigDecimal m_QtyOnHand = Env.ZERO;
			BigDecimal m_QtyReserved = Env.ZERO;
			BigDecimal m_QtyInvoiced = Env.ZERO;
			BigDecimal m_QtyDelivered = Env.ZERO;
			BigDecimal m_QtyInTransit = Env.ZERO;
			BigDecimal m_QtyOrdered = Env.ZERO;
			BigDecimal m_Qty = Env.ZERO;
			BigDecimal m_Weight = Env.ZERO;
			BigDecimal m_Volume = Env.ZERO;
			String deliveryRuleKey= null;
			
			//FR [ 1 ]
			boolean isStocked =false;
			int precision = 0;
			//	
			while (rs.next()) {
				column = 1;
				m_Warehouse 		= new KeyNamePair(rs.getInt(column++), rs.getString(column++));
				m_DocumentNo 		= new KeyNamePair(rs.getInt(column++), rs.getString(column++));
				m_Product 			= new KeyNamePair(rs.getInt(column++), rs.getString(column++));
				m_Product_UOM 		= new KeyNamePair(rs.getInt(column++), rs.getString(column++));
				m_QtyOnHand 		= rs.getBigDecimal(column++);
				m_QtyOrdered 		= rs.getBigDecimal(column++);
				m_Order_UOM 		= new KeyNamePair(rs.getInt(column++), rs.getString(column++));
				m_QtyReserved 		= rs.getBigDecimal(column++);
				m_QtyInvoiced 		= rs.getBigDecimal(column++);
				m_QtyDelivered 		= rs.getBigDecimal(column++);
				m_QtyInTransit 		= rs.getBigDecimal(column++);
				m_Qty 				= rs.getBigDecimal(column++);
				m_Weight 			= rs.getBigDecimal(column++);
				m_Volume 			= rs.getBigDecimal(column++);
				deliveryRuleKey 	= rs.getString(column++);
				//FR [ 1 ]
				isStocked = (rs.getString("IsStocked")!=null? "": rs.getString("IsStocked")).equals("Y");
				//	Get Precision
				precision = MUOM.getPrecision(Env.getCtx(), m_Product_UOM.getKey());
				//	
				//	Valid Null
				if(m_QtyOnHand == null)
					m_QtyOnHand = Env.ZERO;
				//FR [ 1 ]
				if (!isStocked) {
					m_QtyOnHand = m_Qty;
				}
				if(Util.isEmpty(deliveryRuleKey)) {
					deliveryRuleKey = X_C_Order.DELIVERYRULE_Availability;
				}
				//	Delivery Rule
				ValueNamePair deliveryRule = new ValueNamePair(deliveryRuleKey, 
						MRefList.getListName(Env.getCtx(), 
								X_C_Order.DELIVERYRULE_AD_Reference_ID, deliveryRuleKey));
				//	Valid Quantity On Hand
				if(deliveryRule.getID().equals(X_C_Order.DELIVERYRULE_Availability)
						&&	m_IsValidateQuantity) {
					//FR [ 1 ]
					BigDecimal diff = ((BigDecimal)(isStocked ? Env.ONE : Env.ZERO)).multiply(m_QtyOnHand.subtract(m_Qty).setScale(precision, BigDecimal.ROUND_HALF_UP));
					//	Set Quantity
					if(diff.doubleValue() < 0) {
						m_Qty = m_Qty
							.subtract(diff.abs())
							.setScale(precision, BigDecimal.ROUND_HALF_UP);
					}
					//	Valid Zero
					if(m_Qty.doubleValue() <= 0)
						continue;
				}
				//	Fill Row
				Vector<Object> line = new Vector<Object>();
				line.add(new Boolean(false));       			//  0-Selection
				line.add(m_Warehouse);       					//  1-Warehouse
				line.add(m_DocumentNo);				       		//  2-DocumentNo
				line.add(m_Product);				      		//  3-Product
				line.add(m_Product_UOM);				      	//  4-Unit Product
				line.add(m_QtyOnHand);  						//  5-QtyOnHand
				line.add(m_Qty);								//  6-Quantity
				line.add(m_Weight.multiply(m_Qty));				//	7-Weight
				line.add(m_Volume.multiply(m_Qty));				//	8-Volume
				line.add(Env.ZERO);								//	9-SeqNo
				line.add(m_QtyOrdered);							//	10-QtyOrdered
				line.add(m_Order_UOM);							//	11-UOM-Conversion
				line.add(m_QtyReserved);				      	//  12-QtyReserved
				line.add(m_QtyInvoiced);				      	//  13-QtyInvoiced
				line.add(m_QtyDelivered);				      	//  14-QtyDelivered
				line.add(m_QtyInTransit);				      	//  15-QtyInTransit			
				line.add(deliveryRule);						//	16-Delivery Rule
				//	Add Data
				data.add(line);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sqlPrep.toString(), e);
		}
		finally{
			DB.close(rs, pstmt);
		}
		//	
		return data;
	}
	
	/**
	 * Get Column Name on Order Line
	 * @return
	 * @return Vector<String>
	 */
	protected Vector<String> getOrderLineColumnNames() {	
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Msg.translate(Env.getCtx(), "M_Warehouse_ID"));
		columnNames.add(Util.cleanAmp(Msg.translate(Env.getCtx(), "DocumentNo")));
		columnNames.add(Msg.translate(Env.getCtx(), "M_Product_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_UOM_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyOnHand"));
		columnNames.add(Msg.translate(Env.getCtx(), "Qty"));
		columnNames.add(Msg.translate(Env.getCtx(), "Weight")
				+ " (" + m_UOM_Weight_Symbol + ")");
		columnNames.add(Msg.translate(Env.getCtx(), "Volume")
				+ " (" + m_UOM_Volume_Symbol + ")");
		columnNames.add(Msg.translate(Env.getCtx(), "LoadSeq"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyOrdered"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_UOM_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyReserved"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyInvoiced"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyDelivered"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyInTransit"));
		columnNames.add(Msg.translate(Env.getCtx(), "DeliveryRule"));
		return columnNames;
	}
	
	/**
	 * Get Stock Column Names
	 * @return
	 * @return Vector<String>
	 */
	protected Vector<String> getStockColumnNames() {	
		//  Header Info
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(Msg.translate(Env.getCtx(), "M_Product_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "C_UOM_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "M_Warehouse_ID"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyOnHand"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyInTransit"));
		columnNames.add(Msg.translate(Env.getCtx(), "Qty"));
		columnNames.add(Msg.translate(Env.getCtx(), "QtyAvailableForLoad"));
		return columnNames;
	}

	/**
	 * Set Stock Column Class
	 * @param stockTable
	 * @return void
	 */
	protected void setStockColumnClass(IMiniTable stockTable) {
		int i = 0;
		stockTable.setColumnClass(i++, String.class, true);			//  1-Product
		stockTable.setColumnClass(i++, String.class, true);			//  2-Unit of Measure
		stockTable.setColumnClass(i++, String.class, true);			//  3-Warehouse
		stockTable.setColumnClass(i++, BigDecimal.class, true);		//  4-Quantity On Hand
		stockTable.setColumnClass(i++, BigDecimal.class, true);		//  5-Quantity In Transit
		stockTable.setColumnClass(i++, BigDecimal.class, true);		//  6-Quantity Set
		stockTable.setColumnClass(i++, BigDecimal.class, true);		//  7-Quantity Available For Load
		//  Table UI
		stockTable.autoSize();
	}
	
	
	/**
	 * Set Order Line Class on Table
	 * @param orderLineTable
	 * @return void
	 */
	protected void setOrderLineColumnClass(IMiniTable orderLineTable) {
		int i = 0;
		orderLineTable.setColumnClass(i++, Boolean.class, false);		//  0-Selection
		orderLineTable.setColumnClass(i++, String.class, true);			//  1-Warehouse
		orderLineTable.setColumnClass(i++, String.class, true);			//  2-DocumentNo
		orderLineTable.setColumnClass(i++, String.class, true);			//  3-Product
		orderLineTable.setColumnClass(i++, String.class, true);			//  4-Unit Measure Product
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  5-QtyOnHand
		orderLineTable.setColumnClass(i++, BigDecimal.class, false);	//  6-Quantity
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  7-Weight
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  8-Volume
		orderLineTable.setColumnClass(i++, Integer.class, false);		//  9-Sequence No
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  10-QtyOrdered
		orderLineTable.setColumnClass(i++, String.class, true);			//  11-Unit Measure Conversion
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  12-QtyReserved
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  13-QtyInvoiced
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//  14-QtyDelivered
		orderLineTable.setColumnClass(i++, BigDecimal.class, true);		//	15-QtyInTransit
		orderLineTable.setColumnClass(i++, String.class, true);			//  16-Delivery Rule
		//  Table UI
		orderLineTable.autoSize();
	}
	
	/**
	 * Generate Load Order
	 * @param trxName
	 * @param orderLineTable
	 * @return
	 * @return String
	 */
	public String generateLoadOrder(String trxName, IMiniTable orderLineTable) {
		int quantity = 0;
		int rows = orderLineTable.getRowCount();
		outBoundOrder = new MWMInOutBound(Env.getCtx(), 0, trxName);
		MWMInOutBoundLine outBoundOrderLine = null;
		//	
		BigDecimal totalWeight = Env.ZERO;
		BigDecimal totalVolume = Env.ZERO;
		//	
		outBoundOrder.setAD_Org_ID(m_AD_Org_ID);
//		outBoundOrder.setOperationType(m_OperationType);
//		outBoundOrder.setFTA_VehicleType_ID(m_FTA_VehicleType_ID);
		outBoundOrder.setPickDate(m_DateDoc);
		outBoundOrder.setShipDate(m_ShipDate);
//		outBoundOrder.setLoadCapacity(m_LoadCapacity);
//		outBoundOrder.setVolumeCapacity(m_VolumeCapacity);
//		outBoundOrder.setC_UOM_Weight_ID(m_C_UOM_Weight_ID);
//		outBoundOrder.setC_UOM_Volume_ID(m_C_UOM_Volume_ID);
//		MLocator locator = MLocator.get(getCtx(), getLocatorId());
//        MWMInOutBound outBoundOrder = new MWMInOutBound(getCtx(), 0, get_TrxName());
//        outBoundOrder.setShipDate(getShipDate());
//        outBoundOrder.setPickDate(getPickDate());
//        if (getPOReference() != null)
//            outBoundOrder.setPOReference(getPOReference());
//
//        if (getDeliveryRule() != null)
//            outBoundOrder.setDeliveryRule(getDeliveryRule());
//        if (getDeliveryViaRule() != null)
//            outBoundOrder.setDeliveryViaRule(getDeliveryViaRule());
//
        if (docTypeTargetId > 0) {
        	outBoundOrder.setC_DocType_ID(docTypeTargetId);
        } else {
            int docTypeId = MDocType.getDocType(MDocType.DOCBASETYPE_WarehouseManagementOrder);
            if (docTypeId <= 0) {
            	throw new DocTypeNotFoundException(MDocType.DOCBASETYPE_WarehouseManagementOrder, "");
            } else {
            	outBoundOrder.setC_DocType_ID(docTypeId);
            }
        }
//
//        if (getDocAction() != null)
//            outBoundOrder.setDocAction(getDocAction());
//        else
//            outBoundOrder.setDocAction(MWMInOutBound.ACTION_Prepare);
//
		//	Set Is Handle Record Weight
//		outBoundOrder.setIsHandleRecordWeight(MFTAWeightScale.isWeightScaleOrg(m_AD_Org_ID, trxName));
		//	Set Warehouse
		if(warehouseId != 0)
			outBoundOrder.setM_Warehouse_ID(warehouseId);
		//	Invoice Rule
//		if(invoiceRule != null
//				&& invoiceRule.trim().length() > 0)
//			outBoundOrder.setInvoiceRule(invoiceRule);
		//	Delivery Rule
		if(deliveryRule != null
				&& deliveryRule.trim().length() > 0)
			outBoundOrder.setDeliveryRule(deliveryRule);
		//	Set Shipper
		if(shipperId != 0)
			outBoundOrder.setM_Shipper_ID(shipperId);
//		//	Set Driver
//		if(m_FTA_Driver_ID != 0)
//			outBoundOrder.setFTA_Driver_ID(m_FTA_Driver_ID);
//		//	Set Vehicle
//		if(m_FTA_Vehicle_ID != 0)
//			outBoundOrder.setFTA_Vehicle_ID(m_FTA_Vehicle_ID);
		//	Set Entry Ticket
//		if(m_FTA_EntryTicket_ID != 0)
//			outBoundOrder.setFTA_EntryTicket_ID(m_FTA_EntryTicket_ID);
		//	Set Product
//		if(m_M_Product_ID != 0)
//			outBoundOrder.setM_Product_ID(m_M_Product_ID);
		//	Save Order
      outBoundOrder.setDocStatus(MWMInOutBound.DOCSTATUS_Drafted);
      outBoundOrder.setIsSOTrx(true);
      outBoundOrder.saveEx();
		//	Loop for add Lines
		for (int i = 0; i < rows; i++) {
			if (((Boolean)orderLineTable.getValueAt(i, 0)).booleanValue()) {
				int orderLineId = ((KeyNamePair)orderLineTable.getValueAt(i, ORDER_LINE)).getKey();
				int productId = ((KeyNamePair)orderLineTable.getValueAt(i, OL_PRODUCT)).getKey();
				BigDecimal qty = (BigDecimal) orderLineTable.getValueAt(i, OL_QTY);
				BigDecimal weight = (BigDecimal) orderLineTable.getValueAt(i, OL_WEIGHT);
				BigDecimal volume = (BigDecimal) orderLineTable.getValueAt(i, OL_VOLUME);
				Integer seqNo = (Integer) orderLineTable.getValueAt(i, OL_SEQNO);
				//	New Line
				outBoundOrderLine = new MWMInOutBoundLine(outBoundOrder);
				//	Set Values
				outBoundOrderLine.setAD_Org_ID(m_AD_Org_ID);
				/** 2014-12-02 Carlos Parada Add Support to Distribution Order*/ 
				if (m_OperationType.equals(MATERIAL_MOVEMENT)) {
					outBoundOrderLine.setDD_OrderLine_ID(orderLineId);
					MDDOrderLine line = new MDDOrderLine(Env.getCtx(), orderLineId, trxName);
					outBoundOrderLine.setDD_Order_ID(line.getDD_Order_ID());
				} else {
					outBoundOrderLine.setC_OrderLine_ID(orderLineId);
					MOrderLine line = new MOrderLine(Env.getCtx(), orderLineId, trxName);
					outBoundOrderLine.setDD_Order_ID(line.getC_Order_ID());
				}
				/** End Carlos Parada*/
				outBoundOrderLine.setM_Product_ID(productId);
				outBoundOrderLine.setMovementQty(qty);
//				outBoundOrderLine.setSeqNo(seqNo);
//				outBoundOrderLine.setWeight(weight);
//				outBoundOrderLine.setVolume(volume);
				//	Add Weight
				totalWeight = totalWeight.add(weight);
				//	Add Volume
				totalVolume = totalVolume.add(volume);
				//	Save Line
				outBoundOrderLine.saveEx();
				//	Add Count
				quantity ++;
			}
		}
		//	Set Header Weight
		outBoundOrder.setWeight(totalWeight);
		//	Set Header Volume
		outBoundOrder.setVolume(totalVolume);
		//	Save Header
		outBoundOrder.saveEx();
		//	Complete Order
		outBoundOrder.setDocAction(MWMInOutBound.DOCACTION_Complete);
		outBoundOrder.processIt(MWMInOutBound.DOCACTION_Complete);
		outBoundOrder.saveEx();
		//	Valid Error
		String errorMsg = outBoundOrder.getProcessMsg();
		if(errorMsg != null
				&& outBoundOrder.getDocStatus().equals(MWMInOutBound.DOCSTATUS_Invalid))
			throw new AdempiereException(errorMsg);
		//	Message
		return Msg.parseTranslation(Env.getCtx(), "@Created@ = [" + outBoundOrder.getDocumentNo() 
				+ "] || @LineNo@" + " = [" + quantity + "]" + (errorMsg != null? "\n@Errors@:" + errorMsg: ""));
	}
	
	/**
	 * Load the Default Values
	 * @return void
	 */
	protected void loadDefaultValues() {
		m_C_UOM_Weight_ID = getC_UOM_Weight_ID();
		m_C_UOM_Volume_ID = getC_UOM_Volume_ID();
		//	Get Weight Precision
		if(m_C_UOM_Weight_ID > 0) {
			m_WeightPrecision = MUOM.getPrecision(Env.getCtx(), m_C_UOM_Weight_ID);
		}
		//	Get Volume Precision
		if(m_C_UOM_Volume_ID > 0) {
			m_VolumePrecision = MUOM.getPrecision(Env.getCtx(), m_C_UOM_Volume_ID);
		}
	}
	
	/**
	 * Get Vehicle Type from Vehicle
	 * @param p_FTA_EntryTicket_ID
	 * @return
	 * @return int
	 */
	protected int getFTA_VehicleType_ID(int p_FTA_EntryTicket_ID) {
		return DB.getSQLValue(null, "SELECT v.FTA_VehicleType_ID "
				+ "FROM FTA_EntryTicket et "
				+ "INNER JOIN FTA_Vehicle v ON(v.FTA_Vehicle_ID = et.FTA_Vehicle_ID) "
				+ "AND et.FTA_EntryTicket_ID = ?", p_FTA_EntryTicket_ID);
	}
	
	/**
	 * Get Load Capacity from Vehicle Type
	 * @param p_FTA_VehicleType_ID
	 * @return
	 * @return BigDecimal
	 */
	protected BigDecimal getLoadCapacity(int p_FTA_VehicleType_ID) {
		return DB.getSQLValueBD(null, "SELECT vt.LoadCapacity "
				+ "FROM FTA_VehicleType vt "
				+ "WHERE FTA_VehicleType_ID = ?", p_FTA_VehicleType_ID);
	}
	
	/**
	 * Get Volume Capacity from Vehicle Type
	 * @param p_FTA_VehicleType_ID
	 * @return
	 * @return BigDecimal
	 */
	protected BigDecimal getVolumeCapacity(int p_FTA_VehicleType_ID) {
		return DB.getSQLValueBD(null, "SELECT vt.VolumeCapacity "
				+ "FROM FTA_VehicleType vt "
				+ "WHERE FTA_VehicleType_ID = ?", p_FTA_VehicleType_ID);
	}
	
	/**
	 * Get default UOM
	 * @return
	 * @return int
	 */
	protected int getC_UOM_Weight_ID() {
		return DB.getSQLValue(null, "SELECT ci.C_UOM_Weight_ID "
				+ "FROM AD_ClientInfo ci "
				+ "WHERE ci.AD_Client_ID = ?", m_AD_Client_ID);
	}
	
	/**
	 * Get default Volume UOM
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 19/12/2013, 11:14:38
	 * @return
	 * @return int
	 */
	protected int getC_UOM_Volume_ID() {
		return DB.getSQLValue(null, "SELECT ci.C_UOM_Volume_ID "
				+ "FROM AD_ClientInfo ci "
				+ "WHERE ci.AD_Client_ID = ?", m_AD_Client_ID);
	}
	
	/**
	 * Get Driver Data
	 * @return
	 * @return KeyNamePair[]
	 */
	protected KeyNamePair[] getDataDriver() {
		String sql = "SELECT d.FTA_Driver_ID, d.Value || ' - ' || d.Name " +
				"FROM FTA_EntryTicket et " + 
				"INNER JOIN FTA_Driver d ON(d.FTA_Driver_ID = et.FTA_Driver_ID) " +
				"WHERE et.FTA_EntryTicket_ID = " + m_FTA_EntryTicket_ID + " " +
				"ORDER BY d.Value, d.Name";
		//	
		return DB.getKeyNamePairs(null, sql, false, new Object[]{});
	}
	
	/**
	 * Get Driver Data
	 * @return
	 * @return KeyNamePair[]
	 */
	protected KeyNamePair[] getDataShipper() {
		String sql = "SELECT s.M_Shipper_ID, s.Name " +
				"FROM FTA_EntryTicket et " + 
				"INNER JOIN M_Shipper s ON(s.M_Shipper_ID = et.M_Shipper_ID)  " +
				"WHERE et.FTA_EntryTicket_ID = " + m_FTA_EntryTicket_ID + " " +
				"ORDER BY s.Name";
		//	
		return DB.getKeyNamePairs(null, sql, false, new Object[]{});
	}
	
	/**
	 * Get Vehicle Data
	 * @return
	 * @return KeyNamePair[]
	 */
	protected KeyNamePair[] getVehicleData() {
		String sql = "SELECT v.FTA_Vehicle_ID, v.VehiclePlate || ' - ' || v.Name " +
				"FROM FTA_EntryTicket et " + 
				"INNER JOIN FTA_Vehicle v ON(v.FTA_Vehicle_ID = et.FTA_Vehicle_ID) " +
				"WHERE et.FTA_EntryTicket_ID = " + m_FTA_EntryTicket_ID + " " +
				"ORDER BY v.VehiclePlate, v.Name";
		//	
		return DB.getKeyNamePairs(null, sql, false, new Object[]{});
	}
	
	/**
	 * Get Data for Document Type from Operation Type
	 * @return
	 * @return KeyNamePair[]
	 */
	protected KeyNamePair[] getDataDocumentType() {
		
		if(m_OperationType == null)
			return null;
		
		String docBaseType = (m_OperationType.equals("MOM")? "DOO": "SOO");
		
		String sql = MRole.getDefault().addAccessSQL("SELECT doc.C_DocType_ID, TRIM(doc.Name) " +
				"FROM C_DocType doc " +
				"WHERE doc.AD_Client_ID = " + m_AD_Client_ID + " " + 
				"AND doc.AD_Org_ID = " + m_AD_Org_ID + " " + 
				"AND doc.DocBaseType = '" + docBaseType + "' " +
				"AND doc.OperationType = '" + m_OperationType + "' " + 
				"AND (doc.DocSubTypeSO IS NULL OR doc.DocSubTypeSO NOT IN('RM', 'OB')) " +
				"ORDER BY doc.Name", "doc", MRole.SQL_FULLYQUALIFIED, MRole.SQL_RW);		
		return DB.getKeyNamePairs(null, sql, false, new Object[]{});
	}	
	
	/**
	 * Load the Warehouse from Organization
	 * @return
	 * @return KeyNamePair[]
	 */
	protected KeyNamePair[] getDataWarehouse() {
		String sql = "SELECT w.M_Warehouse_ID, w.Name " +
				"FROM M_Warehouse w " +
				"WHERE w.IsActive = 'Y' " +
				"AND w.AD_Org_ID = " + m_AD_Org_ID + " " + 
				"ORDER BY w.Name";
		return DB.getKeyNamePairs(null, sql, false, new Object[]{});
	}
	
	/**
	 * Get Quantity in Transit
	 * @param p_M_Product_ID
	 * @param p_M_Warehouse_ID
	 * @return
	 * @return BigDecimal
	 */
	protected BigDecimal getQtyInTransit(int p_M_Product_ID, int p_M_Warehouse_ID) {
		//	Valid
		if(p_M_Product_ID == 0 
				|| p_M_Warehouse_ID == 0)
			return Env.ZERO;
		//	
		String sql = "SELECT COALESCE(SUM(lc.Qty), 0) QtyLoc "
				+ "FROM WM_InOutBound c "
				+ "INNER JOIN WM_InOutBoundLine lc ON(lc.WM_InOutBound_ID = c.WM_InOutBound_ID) "
				+ "WHERE lc.M_Product_ID = ? "
				+ "AND lc.M_Warehouse_ID = ? "
				+ "AND c.DocStatus = 'CO' "
				+ "AND ("
				+ "			(c.IsDelivered = 'N' AND c.OperationType IN('DBM', 'DFP')) "
				+ "			OR "
				+ "			(c.IsMoved = 'N' AND c.OperationType = 'MOM')"
				+ "		)";
		//	Query
		BigDecimal m_QtyInTransit = DB.getSQLValueBD(null, sql, new Object[]{p_M_Product_ID, p_M_Warehouse_ID});
		if(m_QtyInTransit == null)
			m_QtyInTransit = Env.ZERO;
		//	Return
		return m_QtyInTransit;
	}
	
	/**
	 * Verifica que un numero de secuencia no exista en la tabla
	 * @param orderLineTable
	 * @param seqNo
	 * @return
	 */
	public boolean exists_seqNo(IMiniTable orderLineTable, int row, int seqNo) {
		log.info("exists_seqNo");
		int rows = orderLineTable.getRowCount();
		int seqNoTable = 0;
		for (int i = 0; i < rows; i++) {
			if (((Boolean)orderLineTable.getValueAt(i, SELECT)).booleanValue() 
					&& i != row) {
				seqNoTable = (Integer) orderLineTable.getValueAt(i, OL_SEQNO);
				if(seqNo == seqNoTable) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Valid Quantity on Stock
	 * @param stockTable
	 * @return
	 * @return String
	 */
	public String validStock(IMiniTable stockTable) {
		log.info("validStock");
		int rows = stockTable.getRowCount();
		StringBuffer msg = new StringBuffer();
		for (int i = 0; i < rows; i++) {
			//	Get Values
			String product 				= ((KeyNamePair) stockTable.getValueAt(i, SW_PRODUCT)).getName();
			String warehouse 			= ((KeyNamePair) stockTable.getValueAt(i, SW_WAREHOUSE)).getName();
			BigDecimal m_QtyOnHand 		= ((BigDecimal) stockTable.getValueAt(i, SW_QTY_ON_HAND));
			BigDecimal m_QtyInTransit 	= ((BigDecimal) stockTable.getValueAt(i, SW_QTY_IN_TRANSIT));
			BigDecimal m_QtySet 		= ((BigDecimal) stockTable.getValueAt(i, SW_QTY_SET));
			BigDecimal m_QtyAvailable 	= ((BigDecimal) stockTable.getValueAt(i, SW_QTY_AVAILABLE));
			//	Valid
			if(m_QtyAvailable.compareTo(Env.ZERO) >= 0)
				continue;
			//	First Row
			if(msg.length() == 0) {
				msg.append("@QtyInsufficient@");
			}
			//	Add to Msg
			msg.append(Env.NL)
				.append("*")
				.append(product)
				.append("[")
				.append("@M_Warehouse_ID@=").append(warehouse)
				.append(" @QtyAvailable@=").append(m_QtyOnHand.subtract(m_QtyInTransit).doubleValue())
				.append(" @QtyToDeliver@=").append(m_QtySet.doubleValue())
				.append(" @QtyAvailableForLoad@=").append(m_QtyAvailable.doubleValue())
				.append("]");
		}
		//	
		return msg.length() > 0
				? msg.toString()
						: null;
	}
	
	/**
	 * Load Buffer
	 * @param orderLineTable
	 * @return void
	 */
	public void loadBuffer(IMiniTable orderLineTable) {
		log.info("Load Buffer");
		int rows = orderLineTable.getRowCount();
		int m_C_OrderLine_ID = 0;
		BigDecimal qty = Env.ZERO;
		Integer seqNo = 0;
		m_BufferSelect = new Vector<BufferTableSelect>();
		
		for (int i = 0; i < rows; i++) {
			if (((Boolean)orderLineTable.getValueAt(i, SELECT)).booleanValue()) {
				m_C_OrderLine_ID = ((KeyNamePair)orderLineTable.getValueAt(i, ORDER_LINE)).getKey();
				qty = (BigDecimal)orderLineTable.getValueAt(i, OL_QTY);
				seqNo = (Integer)orderLineTable.getValueAt(i, OL_SEQNO);
				m_BufferSelect.addElement(
						new BufferTableSelect(m_C_OrderLine_ID, qty, seqNo));
			}
		}
	}
	
	/**
	 * Verify if is Selected
	 * @param m_Record_ID
	 * @return
	 * @return BufferTableSelect
	 */
	private BufferTableSelect isSelect(int m_Record_ID) {
		log.info("Is Select " + m_Record_ID);
		if(m_BufferSelect != null) {
			for(int i = 0; i < m_BufferSelect.size(); i++) {
				if(m_BufferSelect.get(i).getRecord_ID() == m_Record_ID) {
					return m_BufferSelect.get(i);
				}
			}	
		}
		return null;
	}
	
	/**
	 * Set the values from buffer
	 * @param orderLineTable
	 * @return void
	 */
	protected void setValueFromBuffer(IMiniTable orderLineTable) {
		log.info("Set Value From Buffer");
		if(m_BufferSelect != null) {
			int rows = orderLineTable.getRowCount();
			int m_C_OrderLine_ID = 0;
			BufferTableSelect bts = null;
			for (int i = 0; i < rows; i++) {
				m_C_OrderLine_ID = ((KeyNamePair)orderLineTable.getValueAt(i, ORDER_LINE)).getKey();
				bts = isSelect(m_C_OrderLine_ID);
				if(bts != null) {
					orderLineTable.setValueAt(true, i, SELECT);
					orderLineTable.setValueAt(bts.getQty(), i, OL_QTY);
					orderLineTable.setValueAt(bts.getSeqNo(), i, OL_SEQNO);
				}
			}	
		}
	}
	
	/**
	 * Get is Bulk
	 * @return
	 * @return boolean
	 */
	protected boolean isBulk() {
		return false;
//		return (m_OperationType
//				.equals(X_WM_InOutBound.OPERATIONTYPE_DeliveryBulkMaterial));
	}
	
	/**
	 * Verify if more one is selected
	 * @param table
	 * @return
	 * @return boolean
	 */
	protected boolean moreOneSelect(IMiniTable table) {
		int rows = table.getRowCount();
		int cont = 0;
		for (int i = 0; i < rows; i++) {
			if (((Boolean)table.getValueAt(i, SELECT)).booleanValue()) {
				cont++;
				if(cont > 1) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @author Yamel Senih 08/03/2012, 23:46:54
	 *
	 */
	public class BufferTableSelect {
		/**
		 * Default constructor
		 * @param recordId
		 * @param qty
		 * @param seqNo
		 */
		public BufferTableSelect(int recordId, BigDecimal qty, Integer seqNo){
			this.recordId = recordId;
			this.qty = qty;
			this.seqNo = seqNo;
		}
		
		/**
		 * Set Record Identifier
		 * @param recordId
		 */
		public void setRecord_ID(int recordId){
			this.recordId = recordId;
		}
		
		/**
		 * Get Record Identifier
		 * @return
		 */
		public int getRecord_ID(){
			return this.recordId;
		}
		
		/**
		 * Set Quantity
		 * @param qty
		 */
		public void setQty(BigDecimal qty){
			this.qty = qty;
		}
		
		/**
		 * Get Quantity
		 * @return
		 */
		public BigDecimal getQty(){
			return this.qty;
		}
		
		/**
		 * Set Sequence
		 * @param seqNo
		 */
		public void setSeqNo(Integer seqNo){
			this.seqNo = seqNo;
		}
		
		/**
		 * Get Sequence
		 * @return
		 */
		public Integer getSeqNo(){
			return this.seqNo;
		}
		
		public String toString(){
			return "Record_ID = " + recordId 
					+ " qty = " + qty 
					+ "seqNo = " + seqNo;
		}
		
		/**	Record ID	*/
		private int recordId = 0;
		/**	Quantity	*/
		private BigDecimal qty = Env.ZERO;
		/**	Sequence	*/
		private Integer seqNo = 0;
	}

	
}