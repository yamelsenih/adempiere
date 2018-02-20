package org.adempiere.webui.apps.form;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.GridPanel;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.panel.ADTabPanel;
import org.adempiere.webui.panel.AbstractADWindowPanel;
import org.adempiere.webui.panel.StatusBarPanel;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MRole;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkex.zul.Borderlayout;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.North;
import org.zkoss.zkex.zul.South;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Separator;

/**
 * Quick sheet window
 * 
 * @author Jobrian Trinidad
 * @author <a href="mailto:sachin.bhimani89@gmail.com">Sachin Bhimani</a>
 * @since 2016-06-30
 */
public class WQuickEntrySheet extends Window implements EventListener
{

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 6566077952951760414L;
	private static CLogger			log					= CLogger.getCLogger(WQuickEntrySheet.class);

	public Trx						trx					= null;

	private Borderlayout			selPanel			= new Borderlayout();
	private Grid					selNorthPanel		= GridFactory.newGridLayout();
	private ConfirmPanel			selSouthPanel		= new ConfirmPanel(true, true, false, false, false, false);
	private Button					bDelete				= selSouthPanel.createButton(ConfirmPanel.A_DELETE);
	private Button					bSave				= selSouthPanel.createButton(ConfirmPanel.A_SAVE);
	private Button					bIgnore				= selSouthPanel.createButton(ConfirmPanel.A_IGNORE);

	private GridPanel			gridPanel;
	private GridTab					gridTab;
	private ADTabPanel				tabPanel;
	private AbstractADWindowPanel	abstractADWindowPanel;
	/** store object of GridTab for Grid view form */
	private GridTab					formGridTab;
	private StatusBarPanel statusBar = new StatusBarPanel();
	private Panel southPanel = new Panel();

	public WQuickEntrySheet(GridPanel grid, GridTab gTab, ADTabPanel tPanel, AbstractADWindowPanel abstractPanel,
			int onlyCurrentDays, boolean onlyCurrentRows)
	{
		super();
		gridPanel = grid;
		gridTab = gTab;
		tabPanel = tPanel;
		abstractADWindowPanel = abstractPanel;

		gridTab.addDataStatusListener(abstractADWindowPanel);
		gridTab.addDataStatusListener(tabPanel);
		gridTab.enableEvents();

		gridPanel.setADWindowPanel(abstractADWindowPanel);
		gridTab.setQuickEntry(true);

		formGridTab = tabPanel.getGridTab();
		tabPanel.setGridTab(gridTab);
		tabPanel.getGridTab().setQuickEntry(true);
		tabPanel.query(onlyCurrentRows, onlyCurrentDays, MRole.getDefault().getMaxQueryRecords());

		trx = Trx.get(Trx.createTrxName("QuickEntry"), true);
		gridTab.getMTable().setTrxName(trx.getTrxName());

		gridPanel.init(gridTab);

		southPanel.appendChild(new Separator());
		southPanel.appendChild(statusBar);
		initForm();
		setWidth("70%");
		setHeight("80%");
	}

	protected void initForm()
	{
		initZk();
		createNewRow();
		gridPanel.refresh(gridTab);
	}

	/**
	 * if no any row(s) present then it will create new one.
	 */
	private void createNewRow()
	{
		int row = gridTab.getRowCount();
		if (row <= 0)
		{
			gridTab.dataIgnore();
			if (gridTab.isInsertRecord())
				gridPanel.createNewLine();
			else
			{
//				gridPanel.setStatusLine("Cannot insert records on the tab.", true, true);
//				gridPanel.dispose();
//				SessionManager.closeTab(gridTab.getAD_Tab_ID());

				trx.rollback();
				trx.close();
				trx = null;
				gridTab.getMTable().setTrxName(null);

				throw new AdempiereException("Cannot insert records on the tab.");
			}
		}
	}

	private void initZk()
	{
		selPanel.setWidth("99%");
		selPanel.setHeight("95%");

		North north = new North();
		north.setFlex(true);
		north.setStyle("border: none");
		north.appendChild(selNorthPanel);
		selPanel.appendChild(north);

		Center center = new Center();

		center.appendChild(gridPanel);
		center.setFlex(true);
		selPanel.appendChild(center);

		selSouthPanel.addActionListener(this);

		South south = new South();
		south.appendChild(southPanel);
		southPanel.appendChild(selSouthPanel);
		selPanel.appendChild(south);

		bSave.setEnabled(!gridTab.isReadOnly());
		bDelete.setEnabled(!gridTab.isReadOnly());
		bIgnore.setEnabled(!gridTab.isReadOnly());

		bSave.addEventListener(Events.ON_CLICK, this);
		bDelete.addEventListener(Events.ON_CLICK, this);
		bIgnore.addEventListener(Events.ON_CLICK, this);

		selSouthPanel.addComponentsLeft(bSave);
		selSouthPanel.addComponentsLeft(bDelete);
		selSouthPanel.addComponentsLeft(bIgnore);

		setTitle(gridTab.getName());
		setMaximizable(true);
		setMaximized(false);
		this.appendChild(selPanel);
	}

	public void onEvent(Event event) throws Exception
	{
		if (Events.ON_CLICK.equals(event.getName()))
		{
			if (event.getTarget() == selSouthPanel.getButton(ConfirmPanel.A_OK))
			{
				onSave(true);
				dispose();
			}
			else if (event.getTarget() == selSouthPanel.getButton(ConfirmPanel.A_CANCEL))
			{
				onIgnore();
				dispose();
			}
			else if (event.getTarget() == selSouthPanel.getButton(ConfirmPanel.A_SAVE))
			{
				onSave(true);
			}
			else if (event.getTarget() == selSouthPanel.getButton(ConfirmPanel.A_DELETE))
			{
				onDelete();
			}
			else if (event.getTarget() == selSouthPanel.getButton(ConfirmPanel.A_REFRESH))
			{
				onRefresh();
			}
			else if (event.getTarget() == selSouthPanel.getButton(ConfirmPanel.A_IGNORE))
			{
				onIgnore();
			}
		}
		else
		{
			dispose();
		}
	}

	

	private void onSave(boolean isShowError)
	{
		ArrayList<Integer> rows = gridTab.getMTable().getRowChanged();
		if (rows.size() > 0)
		{
			//if (gridPanel.isNecessaryDataFill(rows.get(0), isShowError))
			//{
				gridPanel.dataSave(0);
			//}
		}

		trx.commit();
//		gridPanel.setStatusLine("Saved", false, true);
		gridTab.dataRefreshAll();
	}

	private void onRefresh()
	{
		gridTab.dataRefreshAll();
		//gridPanel.isNewLineSaved = true;
		//gridPanel.getRenderer().setCurrentCell(0, 1, KeyEvent.RIGHT);
		gridPanel.updateListIndex();
	}

	private void onIgnore()
	{
		trx.rollback();
//		gridPanel.setStatusLine("Changes rolled back", false, true);
		gridTab.dataIgnore();
		gridTab.dataRefreshAll();
//		gridPanel.isNewLineSaved = true;
		if (gridTab.getRowCount() <= 0)
			gridPanel.createNewLine();
		gridPanel.updateListIndex();
	}

	private void onDelete()
	{
		if (gridTab == null)
			return;

//		if (!gridPanel.isNewLineSaved)
//		{
//			gridPanel.setStatusLine("First, Save new record!", true, true);
//			return;
//		}

		final int[] indices = gridTab.getSelection();
		if (indices.length > 0)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(Env.getContext(Env.getCtx(), gridTab.getWindowNo(), "_WinInfo_WindowName", false)).append(" - ")
					.append(indices.length).append(" ").append(Msg.getMsg(Env.getCtx(), "Selected"));
			boolean istrue = FDialog.ask(gridTab.getWindowNo(), bDelete, "DeleteSelection", sb.toString());
			if (istrue)
			{
				gridTab.clearSelection();
				Arrays.sort(indices);
				int offset = 0;
				int count = 0;
				for (int i = 0; i < indices.length; i++)
				{
					gridTab.navigate(indices[i] - offset);
					if (gridTab.dataDelete())
					{
						offset++;
						count++;
					}
				}
				gridTab.dataRefresh(true);
				log.info("DELETED : " + count);
//				gridPanel.setStatusLine(count + " Record(s) deleted.", false, true);
			}
			else if (gridTab.getCurrentRow() != 0)
			{
				gridTab.dataDelete();
			}

			// if all records is deleted then it will show default with new
			// record.
			if (gridTab.getRowCount() <= 0)
				gridPanel.createNewLine();
			gridPanel.updateListIndex();

		}
	}

	@Override
	public void dispose()
	{
		onIgnore();
		super.dispose();

		gridTab.setQuickEntry(false);
		tabPanel.getGridTab().setQuickEntry(false);
		gridPanel.removeKeyListener();
		tabPanel.getListPanel().addKeyListener();
		gridPanel.detach();
//		gridPanel.dispose();
		tabPanel.setGridTab(formGridTab);
//		formGridTab.dataRefreshAll();
//		gridPanel.refresh(formGridTab);
//		gridPanel.updateListIndex();
//		SessionManager.closeTab(gridTab.getAD_Tab_ID());

		trx.rollback();
		trx.close();
		trx = null;
		gridTab.getMTable().setTrxName(null);
		
//		String tabInfo = abstractADWindowPanel.getToolbar().getQuickHrchyTabInfo();
//		abstractADWindowPanel.setGridTab(formGridTab);
//		if (tabInfo.length() > 0)
//		{
//			abstractADWindowPanel.getToolbar().setQuickHrchyTabInfo(tabInfo.substring(0, tabInfo.length() - 1));
//			abstractADWindowPanel.onParentRecord();
//		}
//		abstractADWindowPanel.onRefreshFromQuickForm();
	}
}