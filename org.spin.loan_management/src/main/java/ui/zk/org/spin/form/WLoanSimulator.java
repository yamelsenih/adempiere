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
package org.spin.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.ValueChangeEvent;
import org.adempiere.exceptions.ValueChangeListener;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.editor.WDateEditor;
import org.adempiere.webui.editor.WNumberEditor;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.adempiere.webui.panel.StatusBarPanel;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.TimeUtil;
import org.compiere.util.Trx;
import org.compiere.util.TrxRunnable;
import org.spin.model.MFMProduct;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkex.zul.Borderlayout;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.North;
import org.zkoss.zkex.zul.South;
import org.zkoss.zul.Separator;

/**
 * Financial Management
 *
 * @author Yamel Senih, ysenih@erpya.com , http://www.erpya.com
 *      <li> FR [ 1585 ] Loan Simulator
 *		@see https://github.com/adempiere/adempiere/issues/1585
 */
public class WLoanSimulator extends LoanSimulator
	implements IFormController, EventListener, ValueChangeListener {
	
	private CustomForm form = new CustomForm();

	//
	private Panel mainPanel = new Panel();
	private Borderlayout mainLayout = new Borderlayout();
	private Panel parameterPanel = new Panel();
	private Label businessPartnerLabel = new Label();
	private WSearchEditor businessPartnerField;
	private Label financialProductLabel = new Label();
	private WTableDirEditor financialProductField;
	private Label startDateLabel = new Label();
	private WDateEditor startDateField;
	private Label currencyLabel = new Label();
	private WTableDirEditor currencyField;
	private Label feesAmtLabel = new Label();
	private WNumberEditor feesAmtField;
	private Label interestAmtLabel = new Label();
	private WNumberEditor interestAmtField;
	private Label taxAmtLabel = new Label();
	private WNumberEditor taxAmtField;
	private Label grandTotalLabel = new Label();
	private WNumberEditor grandTotalField;
	private Label capitalAmtLabel = new Label();
	private WNumberEditor capitalAmtField;
	private Label feeAmtLabel = new Label();
	private WNumberEditor feeAmtField;
	private Grid parameterLayout = GridFactory.newGridLayout();
	private Panel southPanel = new Panel();
	private Button calculateButton = null;
	private ConfirmPanel confirmPanel = new ConfirmPanel(true, false, false, false, false, false, false);
	private StatusBarPanel statusBar = new StatusBarPanel();


	/**
	 *	Initialize Panel
	 */
	public WLoanSimulator() {
		log.info("");
		try {
			dynParameter();
			zkInit();
			dynInit();			
		} catch(Exception ex) {
			log.log(Level.SEVERE, "", ex);
		}
	}	//	init
	
	/**
	 *  Static Init
	 *  @throws Exception
	 */
	void zkInit() throws Exception
	{
		form.appendChild(mainPanel);
		mainPanel.setStyle("width: 99%; height: 100%; border: none; padding: 0; margin: 0");
		mainPanel.appendChild(mainLayout);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		parameterPanel.appendChild(parameterLayout);
		//	
		businessPartnerLabel.setText(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		financialProductLabel.setText(Msg.translate(Env.getCtx(), "FM_Product_ID"));
		capitalAmtLabel.setText(Msg.translate(Env.getCtx(), "CapitalAmt"));
		startDateLabel.setText(Msg.translate(Env.getCtx(), "StartDate"));
		feesAmtLabel.setText(Msg.translate(Env.getCtx(), "FeesAmt"));
		feeAmtLabel.setText(Msg.translate(Env.getCtx(), "FeeAmt"));
		interestAmtLabel.setText(Msg.translate(Env.getCtx(), "InterestAmt"));
		taxAmtLabel.setText(Msg.translate(Env.getCtx(), "TaxAmt"));
		currencyLabel.setText(Msg.translate(Env.getCtx(), "C_Currency_ID"));
		grandTotalLabel.setText(Msg.translate(Env.getCtx(), "GrandTotal"));
		//
		North north = new North();
		mainLayout.appendChild(north);
		north.appendChild(parameterPanel);
		
		Rows rows = parameterLayout.newRows();
		Row row = rows.newRow();
		row.appendChild(businessPartnerLabel.rightAlign());
		row.appendChild(businessPartnerField.getComponent());
		row.appendChild(financialProductLabel.rightAlign());
		row.appendChild(financialProductField.getComponent());
		row = rows.newRow();
		row.appendChild(capitalAmtLabel.rightAlign());
		row.appendChild(capitalAmtField.getComponent());
		row.appendChild(feesAmtLabel.rightAlign());
		row.appendChild(feesAmtField.getComponent());
		row = rows.newRow();
		row.appendChild(startDateLabel.rightAlign());
		row.appendChild(startDateField.getComponent());
		row = rows.newRow();
		row.appendChild(currencyLabel.rightAlign());
		row.appendChild(currencyField.getComponent());
		row.appendChild(feeAmtLabel.rightAlign());
		row.appendChild(feeAmtField.getComponent());
		row = rows.newRow();
		row.appendChild(interestAmtLabel.rightAlign());
		row.appendChild(interestAmtField.getComponent());
		row.appendChild(taxAmtLabel.rightAlign());
		row.appendChild(taxAmtField.getComponent());
		row = rows.newRow();
		row.appendChild(grandTotalLabel.rightAlign());
		row.appendChild(grandTotalField.getComponent());
		//	
		row.appendChild(new Separator());
		row.appendChild(calculateButton);
		
		southPanel.appendChild(confirmPanel);
		southPanel.appendChild(new Separator());
		southPanel.appendChild(statusBar);
		South south = new South();
		south.setStyle("border: none");
		mainLayout.appendChild(south);
		south.appendChild(southPanel);
		
		LayoutUtils.addSclass("status-border", statusBar);
	}   //  jbInit

	/**
	 *  Initialize Parameter fields
	 *  @throws Exception if Lookups cannot be initialized
	 */
	private void dynParameter() throws Exception {
		Properties ctx = Env.getCtx();
		//  Business Partner
		MLookup businessPartnerLookup = MLookupFactory.get (ctx, windowNo, 0, 87269, DisplayType.Search);
		businessPartnerField = new WSearchEditor("C_BPartner_ID", true, false, true, businessPartnerLookup);
		businessPartnerField.addValueChangeListener(this);
		//	Financial Product
		MLookup financialProductLookup = MLookupFactory.get (ctx, windowNo, 0, 87268, DisplayType.TableDir);
		financialProductField = new WTableDirEditor("FM_Product_ID", true, false, true, financialProductLookup);
		financialProductField.addValueChangeListener(this);
		//	Capital Amount
		capitalAmtField = new WNumberEditor();
		capitalAmtField.setMandatory(true);
		//	Fees Amount
		feesAmtField = new WNumberEditor("FeesAmt", true, false, true, DisplayType.Integer, "");
		feesAmtField.setMandatory(true);
		//	Start Date
		startDateField = new WDateEditor("StartDate", true, false, true, "");
		//	Fee Amount
		feeAmtField = new WNumberEditor();
		feeAmtField.setReadWrite(false);
		//	Currency
		MLookup currencyLookup = MLookupFactory.get (ctx, windowNo, 0, 87162, DisplayType.TableDir);
		currencyField = new WTableDirEditor("C_Currency_ID", true, true, true, currencyLookup);
		//	Interest Amount
		interestAmtField = new WNumberEditor();
		interestAmtField.setReadWrite(false);
		//	Tax Amount
		taxAmtField = new WNumberEditor();
		taxAmtField.setReadWrite(false);
		//	Tax Amount
		grandTotalField = new WNumberEditor();
		grandTotalField.setReadWrite(false);
		calculateButton = confirmPanel.createButton(ConfirmPanel.A_PROCESS);
		calculateButton.addEventListener(Events.ON_CLICK, this);
		//	
		confirmPanel.addActionListener(this);
		statusBar.setStatusLine("");
	}   //  dynParameter

	/**
	 *  Dynamic Layout (Grid).
	 * 	Based on AD_Window: Material Transactions
	 */
	private void dynInit() {
		super.dynInit(statusBar);
		//
		Center center = new Center();
		mainLayout.appendChild(center);
		center.setFlex(true);
	}   //  dynInit


	/**
	 * 	Dispose
	 */
	public void dispose() {
		SessionManager.getAppDesktop().closeActiveWindow();
	}	//	dispose

	
	/**************************************************************************
	 *  Action Listener
	 *  @param e event
	 */
	public void onEvent (Event e) {
		if(e.getTarget().getId().equals(ConfirmPanel.A_CANCEL)) {
			dispose();
		} else if(e.getTarget().getId().equals(ConfirmPanel.A_PROCESS)) {
			simulate();
		}
	}   //  actionPerformed

	
	/**************************************************************************
	 *  Property Listener
	 *  @param e event
	 */
	public void valueChange (ValueChangeEvent e) {
		if (e.getPropertyName().equals("C_BPartner_ID")) {
			Env.setContext(Env.getCtx(), windowNo, "C_BPartner_ID", ((Integer)e.getNewValue()).intValue());
			businessPartnerId = ((Integer)e.getNewValue()).intValue();
			financialProductField.actionRefresh();
		} else if(e.getPropertyName().equals("FM_Product_ID")) {
			MFMProduct financialProduct = MFMProduct.getById(Env.getCtx(), ((Integer)e.getNewValue()).intValue());
			financialProductId = ((Integer)e.getNewValue()).intValue();
			if(financialProduct != null) {
				int graceDays = financialProduct.get_ValueAsInt("GraceDays");
				Timestamp startDate = (Timestamp) (startDateField.getValue() != null
						? startDateField.getValue()
								: new Timestamp(System.currentTimeMillis()));
				//	
				startDateField.setValue(TimeUtil.addDays(startDate, graceDays));
				if(financialProduct.get_ValueAsInt("C_Currency_ID") != 0) {
					currencyId = financialProduct.get_ValueAsInt("C_Currency_ID");
					currencyField.setValue(currencyId);
				}
			}
		}
	}   //  vetoableChange

	/**
	 * Get Values from fields
	 */
	private void getValues() {
		businessPartnerId = (int) (businessPartnerField.getValue() != null? businessPartnerField.getValue(): 0);
		financialProductId = (int) (financialProductField.getValue() != null? financialProductField.getValue(): 0);
		currencyId = (int) (currencyField.getValue() != null? currencyField.getValue(): 0);
		capitalAmt = (BigDecimal) capitalAmtField.getValue();
		feesAmt = ((BigDecimal) (feesAmtField.getValue() != null? feesAmtField.getValue(): Env.ZERO)).intValue();
		feeAmt = (BigDecimal) feeAmtField.getValue();
		interestAmt = (BigDecimal) interestAmtField.getValue();
		taxAmt = (BigDecimal) taxAmtField.getValue();
		startDate = (Timestamp) (startDateField.getValue() != null? startDateField.getValue(): new Timestamp(System.currentTimeMillis()));
	}
	
	/**************************************************************************
	 *  Save Data
	 */
	public void simulate() {
		getValues();
		try {
			Trx.run(new TrxRunnable() {
				public void run(String trxName) {
					statusBar.setStatusLine(simulateData(trxName));
				}
			});
		} catch (Exception e) {
			FDialog.error(windowNo, form , "Error", e.getLocalizedMessage());
		} finally {
			confirmPanel.getOKButton().setEnabled(true);
		}
	}   //  saveData
	
	public ADForm getForm() {
		return form;
	}

	@Override
	public void setFeeAmt(BigDecimal feeAmt) {
		feeAmtField.setValue(feeAmt);
	}

	@Override
	public void setInterestFeeAmt(BigDecimal interestFeeAmt) {
		interestAmtField.setValue(interestFeeAmt);
	}

	@Override
	public void setTaxFeeAmt(BigDecimal taxFeeAmt) {
		taxAmtField.setValue(taxFeeAmt);
	}

	@Override
	public void setGrandToral(BigDecimal grandTotal) {
		grandTotalField.setValue(grandTotal);
	}

}   //  VTrxMaterial
