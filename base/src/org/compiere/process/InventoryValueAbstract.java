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

package org.compiere.process;

import java.sql.Timestamp;

/** Generated Process for (Inventory Valuation Report)
 *  @author ADempiere (generated) 
 *  @version Release 3.9.1
 */
public abstract class InventoryValueAbstract extends SvrProcess {
	/** Process Value 	*/
	private static final String VALUE_FOR_PROCESS = "InventoryValue";
	/** Process Name 	*/
	private static final String NAME_FOR_PROCESS = "Inventory Valuation Report";
	/** Process Id 	*/
	private static final int ID_FOR_PROCESS = 180;
	/**	Parameter Name for Warehouse	*/
	public static final String M_WAREHOUSE_ID = "M_Warehouse_ID";
	/**	Parameter Name for Currency	*/
	public static final String C_CURRENCY_ID = "C_Currency_ID";
	/**	Parameter Name for Price List Version	*/
	public static final String M_PRICELIST_VERSION_ID = "M_PriceList_Version_ID";
	/**	Parameter Name for Valuation Date	*/
	public static final String DATEVALUE = "DateValue";
	/**	Parameter Name for Cost Element	*/
	public static final String M_COSTELEMENT_ID = "M_CostElement_ID";
	/**	Parameter Value for Warehouse	*/
	private int warehouseId;
	/**	Parameter Value for Currency	*/
	private int currencyId;
	/**	Parameter Value for Price List Version	*/
	private int priceListVersionId;
	/**	Parameter Value for Valuation Date	*/
	private Timestamp dateValue;
	/**	Parameter Value for Cost Element	*/
	private int costElementId;

	@Override
	protected void prepare() {
		warehouseId = getParameterAsInt(M_WAREHOUSE_ID);
		currencyId = getParameterAsInt(C_CURRENCY_ID);
		priceListVersionId = getParameterAsInt(M_PRICELIST_VERSION_ID);
		dateValue = getParameterAsTimestamp(DATEVALUE);
		costElementId = getParameterAsInt(M_COSTELEMENT_ID);
	}

	/**	 Getter Parameter Value for Warehouse	*/
	protected int getWarehouseId() {
		return warehouseId;
	}

	/**	 Setter Parameter Value for Warehouse	*/
	protected void setWarehouseId(int warehouseId) {
		this.warehouseId = warehouseId;
	}

	/**	 Getter Parameter Value for Currency	*/
	protected int getCurrencyId() {
		return currencyId;
	}

	/**	 Setter Parameter Value for Currency	*/
	protected void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}

	/**	 Getter Parameter Value for Price List Version	*/
	protected int getPriceListVersionId() {
		return priceListVersionId;
	}

	/**	 Setter Parameter Value for Price List Version	*/
	protected void setPriceListVersionId(int priceListVersionId) {
		this.priceListVersionId = priceListVersionId;
	}

	/**	 Getter Parameter Value for Valuation Date	*/
	protected Timestamp getDateValue() {
		return dateValue;
	}

	/**	 Setter Parameter Value for Valuation Date	*/
	protected void setDateValue(Timestamp dateValue) {
		this.dateValue = dateValue;
	}

	/**	 Getter Parameter Value for Cost Element	*/
	protected int getCostElementId() {
		return costElementId;
	}

	/**	 Setter Parameter Value for Cost Element	*/
	protected void setCostElementId(int costElementId) {
		this.costElementId = costElementId;
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