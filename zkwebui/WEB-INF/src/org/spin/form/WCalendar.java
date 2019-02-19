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

package org.spin.form;

import java.math.BigDecimal;
import java.util.Date;

import org.adempiere.exceptions.ValueChangeEvent;
import org.adempiere.exceptions.ValueChangeListener;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.DatetimeBox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.calendar.Calendars;
import org.zkoss.calendar.event.CalendarsEvent;
import org.zkoss.calendar.impl.SimpleCalendarEvent;
import org.zkoss.calendar.impl.SimpleCalendarModel;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Calendar;
import org.zkoss.zul.Center;
import org.zkoss.zul.East;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.Space;


/**
 * 
 *
 * 
 * 
 * 
 * 
 * 
 *		
 * 		
 */
public class WCalendar extends org.spin.form.Calendar  
	implements IFormController,EventListener, WTableModelListener, ValueChangeListener
{
	
	/**
	 * 
	 */
//	@SuppressWarnings("unused")
//	private static final long serialVersionUID = 7806119329546820204L;
	
	private CustomForm form = new CustomForm(){
		
		public void setProcessInfo(org.compiere.process.ProcessInfo pi) { 
			setFromPO(pi);
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	};

	/**
	 *	Initialize Panel
	 *  @param WindowNo window
	 *  @param frame frame
	 */
	public WCalendar()
	{
//		setWindowNo(getWindowNo());
		try
		{
//			super.dynInit();
//			dynInit();
			zkInit();
			
		}
		catch(Exception e)
		{
//			log.log(Level.SEVERE, "", e);
		}
	}	//	init
	
	SimpleCalendarModel scm = new SimpleCalendarModel();
	SimpleCalendarEvent sce = new SimpleCalendarEvent();		
	//
	private Calendar smallcalendar = new Calendar();
	private Calendars bigcalendar = new Calendars();
	
	private Window calevent = new Window(); 
	
	private Borderlayout mainLayout = new Borderlayout();
	private Borderlayout windowLayout = new Borderlayout();
	
	private Grid northLayout = GridFactory.newGridLayout();
	
	private Grid windowgridLayout = GridFactory.newGridLayout();
	
	private Textbox descriptionField = new Textbox();
	private Textbox dateTitle = new Textbox();
	private Textbox dateDescription = new Textbox();
	
	private Panel NorthPanel = new Panel();
	
	private North north = new North();
	private Center center = new Center();
	private East east = new East();
	private South south = new South();
	
	private Button Today = new Button();
	private Button next = new Button();
	private Button previous = new Button();
	private Button day = new Button();
	private Button week = new Button();
	private Button month = new Button();
	private Button Save = new Button();
	private Button Cancel = new Button();
	private Button Delete = new Button();
	
	private DatetimeBox dateBegin = new DatetimeBox();
	private DatetimeBox dateEnd = new DatetimeBox();
 
	private Listbox resource = new Listbox();
	
	private Checkbox lockDate = new Checkbox();
	

			
	private boolean m_isCalculating;

	/**
	 *  Static Init
	 *  @throws Exception
	 */
	
	
	
	private void zkInit() throws Exception
	{
		
		form.appendChild (mainLayout);
		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");

		//North Panel
		mainLayout.appendChild(north);
		north.setStyle("border: none; overflow: hidden !important");
		north.setHeight("10%");
		north.appendChild(NorthPanel);
		
		Rows rows = northLayout.newRows();
		northLayout.setHeight("10%");
		NorthPanel.appendChild(northLayout);
		NorthPanel.setHeight("10%");

		//Calendar Control Buttons
		Row row = rows.newRow();
		Today.setLabel("Today");
		Today.addEventListener(Events.ON_CLICK, this);
		row.appendChild(Today);
		
		previous.setLabel("Previous");
		previous.addEventListener(Events.ON_CLICK, this);
		row.appendChild(previous);
		
		next.setLabel("Next");
		next.addEventListener(Events.ON_CLICK, this);
		row.appendChild(next);
		
		row.appendChild(new Space());
		day.setLabel("Day");
		day.addEventListener(Events.ON_CLICK, this);
		row.appendChild(day);
		
		week.setLabel("Week");
		week.addEventListener(Events.ON_CLICK, this);
		row.appendChild(week);
		
		month.setLabel("Month");
		month.addEventListener(Events.ON_CLICK, this);
		row.appendChild(month);		
			
		//Center Panel	
		mainLayout.appendChild(center);
		center.appendChild(bigcalendar);
		bigcalendar.setHeight("100%");
		bigcalendar.setWidth("100%");
		bigcalendar.setFirstDayOfWeek("Monday");
		bigcalendar.setBeginTime(8);
		bigcalendar.setEndTime(17);
		bigcalendar.addEventListener(CalendarsEvent.ON_DAY_CLICK, this);
		bigcalendar.addEventListener(CalendarsEvent.ON_EVENT_CREATE, this);
		
		//East Panel
		east.setTitle("Calendar");
		east.setCollapsible(true);
		mainLayout.appendChild(east);
		east.appendChild(smallcalendar);
		smallcalendar.setHeight("30%");
		smallcalendar.setWidth("50%");
		
		//South Panel
//		mainLayout.appendChild(south);
//		south.setStyle("border: none");
//		south.setVisible(true);
//		south.setTitle("Description");
//		south.setCollapsible(true);
//		south.setHeight("10%");
//		south.appendChild(descriptionField);
//		descriptionField.setText("Week");
//		descriptionField.setWidth("100%");
		
		//Input Date Window 
		calevent.setMold("default");
		calevent.setVisible(false);
		calevent.setClosable(true);
		calevent.setWidth("50%");
		calevent.setHeight("50%");
		calevent.setBorder("Normal");
		calevent.setTitle("Input Date Event");
		
		Rows wrows = windowgridLayout.newRows();
		calevent.appendChild(windowgridLayout);
		
		//Title
		Row wrow = wrows.newRow();
		Label inputTitle= new Label();
		inputTitle.setText("Title");
		wrow.appendChild(inputTitle);
		wrow.appendChild(dateTitle);

		//Description		
		wrow = wrows.newRow();
		Label inputNote= new Label();
		inputNote.setText("Description");
		wrow.appendChild(inputNote);
		wrow.appendChild(dateDescription);
		
		//Lock Date		
		wrow = wrows.newRow();
		Label lockD= new Label();
		lockD.setText("Lock Date");
		wrow.appendChild(lockD);
		wrow.appendChild(lockDate);
		
		//Begin Date
		wrow = wrows.newRow();
		Label bDate = new Label();
		bDate.setText("Begin Date");
		wrow.appendChild(bDate);
		wrow.appendChild(dateBegin);
		
		//End Date
		wrow = wrows.newRow();
		Label eDate = new Label();
		eDate.setText("End Date");
		wrow.appendChild(eDate);
		wrow.appendChild(dateEnd);
		
		//Lisboxt
		wrow = wrows.newRow();
		Label resourcelabel = new Label();
		eDate.setText("User");
		wrow.appendChild(resourcelabel);
		wrow.appendChild(resource);

		//Save - Cancel - Delete
		wrow = wrows.newRow();
		Save.setLabel("Save");
		wrow.appendChild(Save);
		Save.addEventListener(Events.ON_CLICK, this);
		Cancel.setLabel("Cancel");
		wrow.appendChild(Cancel);
		Cancel.addEventListener(Events.ON_CLICK, this);
		Delete.setLabel("Delete");
		wrow.appendChild(Delete);
		Delete.addEventListener(Events.ON_CLICK, this);
				
		
	}   //  jbInit

	/**
	 *  Dynamic Init (prepare dynamic fields)
	 *  @throws Exception if Lookups cannot be initialized
	 */
	public void dynInit() throws Exception
	{

	}   //  dynInit
	


	/**
	 *  Table Model Listener.
	 *  - Recalculate Totals
	 *  @param e event
	 */
	public void tableChanged(WTableModelEvent e)
	{
		boolean isUpdate = (e.getType() == WTableModelEvent.CONTENTS_CHANGED);
		//  Not a table update
		if (!isUpdate)
		{

			return;
		}

  
		if(m_isCalculating)
			return;
		m_isCalculating = true;
		Clients.showBusy(Msg.getMsg(Env.getCtx(), "Processing"));
		
		int row = e.getFirstRow();
		int col = e.getColumn();

		
		Clients.showBusy(Msg.getMsg(Env.getCtx(), "Processing"));
		m_isCalculating = false;
	}   //  tableChanged

	/**
	 *  Vetoable Change Listener.
	 *  - Business Partner
	 *  - Currency
	 * 	- Date
	 *  @param e event
	 */
	public void valueChange (ValueChangeEvent e)
	{
		
		String name = e.getPropertyName();
		name="n";
		Object value = e.getNewValue();

	}   //  vetoableChange
	
	/**
	 * Called by org.adempiere.webui.panel.ADForm.openForm(int)
	 * @return
	 */
	public ADForm getForm() {
		return form;
	}

@Override
public void onEvent(Event event) throws Exception {
	
	// Calendar Go today
    if (event.getTarget().equals(Today))
    {    	 
    	Date currentday = new Date();
    	bigcalendar.setCurrentDate(currentday);
    	smallcalendar.setValue(currentday);
    	calevent.setVisible(true);    	
    }
    // Browse on calendar pages
    else if (event.getTarget().equals(previous))
    {
    	bigcalendar.previousPage();

    }
    else if (event.getTarget().equals(next))
    {
    	bigcalendar.nextPage();
    	calevent.setVisible(false);
    }
    // Set Calendar to show Days
    else if (event.getTarget().equals(day))
    {
    	bigcalendar.setMold("default");
    	bigcalendar.setDays(1);
    // Set Calendar to show Weeks
    }else if (event.getTarget().equals(week))
    {
    	bigcalendar.setMold("default");
    	bigcalendar.setDays(7);
    // Set Calendar to show Months
    }else if (event.getTarget().equals(month))
    {
    	bigcalendar.setMold("month");
    // Cancel Even register Window	
    }else if (event.getTarget().equals(Cancel))
    {
    	calevent.dispose();
    //    Save Even register Window
    }else if (event.getTarget().equals(Save))
       {
    	addToCalendar();
    	// Simple Calendar Event
    	// Get Dates From Textbox
//       	sce.setBeginDate(dateBegin.getValue());
//       	sce.setEndDate(dateEnd.getValue());
//       	//The color Strings should only be colors
//       	//that CSS accept
//       	sce.setContentColor("red");
//       	sce.setHeaderColor("blue");
//   
//       	sce.setContent(dateDescription.getValue());
//       	sce.setTitle(dateTitle.getValue());
//   
//       	//is this event locked?
//       	sce.setLocked(false);
//    	// Simple Model Calendar       	
//       	scm = new SimpleCalendarModel();
//       	
//       	scm.add(sce);
//       	// Set Model to Calendar      	
//       	bigcalendar.setModel(scm);
       	// Close Window      	
       	// Close Window      	
       	calevent.dispose();
       }else {	
    	CalendarsEvent Cevent = (CalendarsEvent)event;
    	Cevent.stopClearGhost();
    	// Create Event
    	if(Cevent.ON_EVENT_CREATE!= null) {
    		calevent.setVisible(true);
    		// Set Dates To DateBox
    		dateBegin.setValue(Cevent.getBeginDate());
    		dateEnd.setValue(Cevent.getEndDate());
    		org.adempiere.webui.apps.AEnv.showWindow(calevent);
    		Cevent.clearGhost();
    	}
    	// Day Click Event
    	else if(Cevent.ON_DAY_CLICK != null) 
    	{
    		calevent.setVisible(true);
    		org.adempiere.webui.apps.AEnv.showWindow(calevent);
    		Cevent.clearGhost();
        // Edit Event
    	}else if(Cevent.ON_EVENT_EDIT != null) 
    	{
    		calevent.setVisible(true);
    		org.adempiere.webui.apps.AEnv.showWindow(calevent);
    		Cevent.clearGhost();
    	}  
    	
    }
    
}

/**
 * Set Dates Event on Calendar
 * **/
public void addToCalendar() {
    
	SimpleCalendarEvent sce = new SimpleCalendarEvent();
	
	Date beginDate =  dateBegin.getValue();
	Date endDate =  dateEnd.getValue();
	String description = dateDescription.getValue();
	String note = dateTitle.getValue();
	
	sce.setBeginDate(beginDate);
   	sce.setEndDate(endDate);

    sce.setContentColor("red");
    sce.setHeaderColor("orange");

    sce.setContent(description);
    sce.setTitle(note);
    
    BigDecimal hours = getHours(beginDate, endDate);

    saveData(hours, beginDate, description, note);
 
    //is this event locked?
    sce.setLocked(false);

	scm = new SimpleCalendarModel();

	scm.add(sce);

	bigcalendar.setModel(scm);
}

public BigDecimal getHours(Date beginDate, Date endDate) {
	 // Get hours   
    long diferenceTime = endDate.getTime() - beginDate.getTime();
    long seconds = diferenceTime / 1000;
    long minutes = seconds / 60;
    long hours =minutes / 60;
    BigDecimal sethours = new BigDecimal(hours);
    return sethours;
}

}   //  WCalendar}
