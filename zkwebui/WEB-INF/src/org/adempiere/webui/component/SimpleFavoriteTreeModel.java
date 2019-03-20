/******************************************************************************
 * Product: ADempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2006-2019 ADempiere Foundation, All Rights Reserved.         *
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
package org.adempiere.webui.component;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.webui.panel.MenuPanel;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.WStringEditorDialog;
import org.compiere.model.MTreeFavorite;
import org.compiere.model.MTreeFavoriteNode;
import org.compiere.model.MTreeNode;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.event.TreeDataEvent;

/**
 * A tree model for a simple list of user favorites. 
 * 
 * @author jtrinidad Adaxa - source of the original code
 * @author marcalwestf Mario Calderone, Westfalia
 * 	<li><a href="https://github.com/adempiere/adempiere/issues/911">#911 User Favorite Tree Panel</a> - add Adaxa contribution
 * @author Michael McKay, mckayERP@gmail.com
 *   <li><a href="https://github.com/adempiere/adempiere/issues/2324">#2324 User Favorites will not accept entry without folder</a> 
 *
 */
public class SimpleFavoriteTreeModel extends DefaultTreeModel<Object> implements EventListener, TreeitemRenderer
{

	private static final long		serialVersionUID	= -2948153996477803421L;
	private static final CLogger	logger				= CLogger.getCLogger(SimpleFavoriteTreeModel.class);
	private boolean					itemDraggable;
	private static int				currFolderID		= 0;
	private List<EventListener>		onDropListners		= new ArrayList<EventListener>();
	
	/** "Draggable" identifier used for the tree items */
	public static final String		USER_FAVORITE_DRAGGABLE_TYPE = "favoriteItem";
	
	public static ADTreeFavoriteOnDropListener listener;

	/** 
	 * Constructor
	 * @param root
	 */
	public SimpleFavoriteTreeModel(DefaultTreeNode<Object> root)
	{
		super(root);
	}

	/**
	 * Initialization of Tree. 
	 * 
	 * @param tree
	 * @param AD_Tree_Favorite_ID
	 * @param windowNo
	 * @return
	 */
	public static SimpleFavoriteTreeModel initADTree(Tree tree, int AD_Tree_Favorite_ID, int windowNo)
	{
		return initADTree(tree, AD_Tree_Favorite_ID, windowNo, true, null);
	}

	/**
	 * Initialization of Tree. 
	 * 
	 * @param tree
	 * @param AD_Tree_Favorite_ID
	 * @param windowNo
	 * @param editable
	 * @param trxName
	 * @return
	 */
	private static SimpleFavoriteTreeModel initADTree(Tree tree, int AD_Tree_Favorite_ID, int windowNo,
			boolean editable, String trxName)
	{

		MTreeFavorite mTreeFavorite = new MTreeFavorite(Env.getCtx(), AD_Tree_Favorite_ID, trxName);
		MTreeNode root = mTreeFavorite.getRoot();
		currFolderID = root.getNode_ID();
		SimpleFavoriteTreeModel treeModel = SimpleFavoriteTreeModel.createFrom(root);

		listener = ADTreeFavoriteOnDropListener.create(tree, treeModel, mTreeFavorite, windowNo);
		treeModel.setItemDraggable(true);
		treeModel.addOnDropEventListener(listener);

		tree.setPageSize(-1);
		try
		{
			tree.setTreeitemRenderer(treeModel);
			tree.setModel(treeModel);
			//TODO : Might be need to code here for default expand collapse
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Failed to setup tree");
		}
		
		return treeModel;
	}

	/** 
	 * Logic for creating Tree hierarchy
	 * @param root
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static SimpleFavoriteTreeModel createFrom(MTreeNode root)
	{
		SimpleFavoriteTreeModel model = null;

		DefaultTreeNode<Object> stRoot = new DefaultTreeNode<Object>(root, new ArrayList<DefaultTreeNode<Object>>());
		
		Enumeration<?> nodeEnum = root.children();

		while (nodeEnum.hasMoreElements())
		{
			MTreeNode childNode = (MTreeNode) nodeEnum.nextElement();
			DefaultTreeNode<Object> stNode = new DefaultTreeNode<Object>(childNode, new ArrayList<DefaultTreeNode<Object>>());
			stRoot.getChildren().add(stNode);
			if (childNode.getChildCount() > 0)
			{
				populate(stNode, childNode);
			}
		}
		model = new SimpleFavoriteTreeModel(stRoot);
		return model;
	}

	/**
	 * Populate Node
	 * 
	 * @param stNode
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	private static void populate(DefaultTreeNode<Object> stNode, MTreeNode root)
	{
		Enumeration<?> nodeEnum = root.children();
		while (nodeEnum.hasMoreElements())
		{
			MTreeNode childNode = (MTreeNode) nodeEnum.nextElement();
			DefaultTreeNode<Object> stChildNode = new DefaultTreeNode<Object>(childNode, new ArrayList<DefaultTreeNode<Object>>());
			stNode.getChildren().add(stChildNode);
			if (childNode.getChildCount() > 0)
			{
				populate(stChildNode, childNode);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.TreeitemRenderer#render(org.zkoss.zul.Treeitem, java.lang.Object)
	 */
	@Override
	public void render(Treeitem ti, Object node, int index) throws Exception
	{
		DefaultTreeNode<Object> stn = (DefaultTreeNode<Object>) node;
		MTreeNode mtn = (MTreeNode) stn.getData();
		Treecell tc;
		if (!mtn.isSummary())
		{
			tc = new Treecell(Objects.toString(node), getIconFile(mtn));
		}
		else
		{
			tc = new Treecell(Objects.toString(node), "images/dark/FolderPlain16.png");
		}
		Treerow tr = null;
		if (ti.getTreerow() == null)
		{
			tr = new Treerow();
			tr.setParent(ti);
			if (isItemDraggable())
			{
				tr.setDraggable(USER_FAVORITE_DRAGGABLE_TYPE);
			}
			if (!onDropListners.isEmpty())
			{
				ti.getTreerow().addEventListener(Events.ON_CLICK, this);
				ti.getTreerow().addEventListener(Events.ON_DOUBLE_CLICK, this);
				// Row items will accept drops from the main menu or 
				// from within this tree
				tr.setDroppable(MenuPanel.MENU_ITEM_DRAGGABLE_TYPE + "," + USER_FAVORITE_DRAGGABLE_TYPE);
				tr.addEventListener(Events.ON_SELECT, this);
				tr.addEventListener(Events.ON_RIGHT_CLICK, this);
				tr.addEventListener(Events.ON_DROP, this);
			}
		}
		else
		{
			tr = ti.getTreerow();
			tr.getChildren().clear();
		}
		tc.setParent(tr);
		ti.setValue(node);
		ti.setTooltiptext(Msg.parseTranslation(Env.getCtx(), mtn.getName() + "\n\n@DPUserFavorite.treeitem.tooltip@"));

	}

	/**
	 * Get Image icon for Menu 
	 * @param mt
	 * @return
	 */
	private String getIconFile(MTreeNode mt)
	{
		if (mt.isWindow())
			return "images/mWindow.png";
		if (mt.isReport())
			return "images/mReport.png";
		if (mt.isProcess())
			return "images/mProcess.png";
		if (mt.isWorkFlow())
			return "images/mWorkFlow.png";
		return "images/mWindow.png";
	}

	/**
	 * Get value of Current Selected Folder in Tree.
	 * @return
	 */
	public static int getSelectedFolderID()
	{
		return currFolderID;
	}

	/**
	 * Set the current selected Menu folder in Tree.
	 * @param mtnID
	 */
	public static void setSelectedFolderID(int mtnID)
	{
		currFolderID = mtnID;
	}

	@Override
	public void onEvent(Event event) throws Exception
	{
		Component comp = event.getTarget();
		String eventName = event.getName();

		if (Events.ON_DROP.equals(eventName) || Events.ON_RIGHT_CLICK.equals(eventName))
		{
			for (EventListener listener : onDropListners)
			{
				listener.onEvent(event);
			}
		}

		/**
		 * On click of menu to open that window
		 */
		if (Events.ON_CLICK.equals(eventName) || Events.ON_SELECT.equals(eventName))
		{
			if (comp instanceof Treerow)
			{
				Treerow treerow = (Treerow) comp;
				Treeitem treeitem = (Treeitem) treerow.getParent();
				Object value = treeitem.getValue();

				DefaultTreeNode<Object> simpleTreeNode = (DefaultTreeNode<Object>) value;
				MTreeNode mtn = (MTreeNode) simpleTreeNode.getData();
				if (!mtn.isSummary())
				{
					int menuId = mtn.getMenu_ID();
					SessionManager.getAppDesktop().onMenuSelected(menuId);
					setSelectedFolderID(mtn.getParent_ID());
				}
				else
				{
					setSelectedFolderID(mtn.getNode_ID());
				}
			}
		}
		else if (Events.ON_DOUBLE_CLICK.equals(eventName))
		{
			if (comp instanceof Treerow)
			{
				Treerow treerow = (Treerow) comp;
				Treeitem treeitem = (Treeitem) treerow.getParent();
				Object value = treeitem.getValue();

				DefaultTreeNode<Object> simpleTreeNode = (DefaultTreeNode<Object>) value;
				MTreeNode mtn = (MTreeNode) simpleTreeNode.getData();
				if (mtn.isSummary())
				{
					WStringEditorDialog dialog = new WStringEditorDialog(Msg.getMsg(Env.getCtx(), "SimpleFavoriteTreeModel.rename.folder"),
							mtn.getName() == null ? "" : mtn.getName(), true, 60);
					
					dialog.setAttribute(Window.MODE_KEY, Window.MODE_MODAL);
					dialog.setLeft(((MouseEvent) event).getX() + "px");
					dialog.setTop(((MouseEvent) event).getX() + "px");
					
					SessionManager.getAppDesktop().showWindow(dialog);
					if (!dialog.isCancelled())
					{
						renameNode(simpleTreeNode, dialog.getText());
					}
				}
			}
		}
	}

	public void renameNode(DefaultTreeNode<Object> simpleTreeNode, String newName) {

		MTreeNode mtn = (MTreeNode) simpleTreeNode.getData();
		mtn.setName(newName);
		MTreeFavoriteNode treeFavNode = new MTreeFavoriteNode(Env.getCtx(), mtn.getNode_ID(), null);
		treeFavNode.setNodeName(newName);
		treeFavNode.saveEx();

		int path[] = this.getPath(simpleTreeNode);
		if (path != null && path.length > 0)
		{
			DefaultTreeNode<Object> parentNode = getRoot();
			int index = path.length - 1;
			for (int i = 0; i < index; i++)
			{
				parentNode = (DefaultTreeNode<Object>) getChild(parentNode, path[i]);
			}
			fireEvent(parentNode, path[index], path[index], TreeDataEvent.CONTENTS_CHANGED);
		}

		
	}

	public void removeNode(DefaultTreeNode<Object> treeNode)
	{
		int path[] = this.getPath(treeNode);

		if (path != null && path.length > 0)
		{
			DefaultTreeNode<Object> parentNode = getRoot();
			int index = path.length - 1;
			for (int i = 0; i < index; i++)
			{
				parentNode = (DefaultTreeNode<Object>) getChild(parentNode, path[i]);
			}
			parentNode.getChildren().remove(path[index]);
			fireEvent(parentNode, path[index], path[index], TreeDataEvent.INTERVAL_REMOVED);
		}
	}

	@SuppressWarnings("unchecked")
	public void addNode(DefaultTreeNode<Object> newNode)
	{
		DefaultTreeNode<Object> root = (DefaultTreeNode<Object>) getRoot();
		root.getChildren().add(newNode);
		fireEvent(root, root.getChildCount() - 1, root.getChildCount() - 1, TreeDataEvent.INTERVAL_ADDED);
	}

	@SuppressWarnings("unchecked")
	public void addNode(DefaultTreeNode<Object> newParent, DefaultTreeNode<Object> newNode, int index)
	{
		newParent.getChildren().add(index, newNode);
		fireEvent(newParent, index, index, TreeDataEvent.INTERVAL_ADDED);
	}

	public void addOnDropEventListener(EventListener listener)
	{
		onDropListners.add(listener);
	}

	public void setItemDraggable(boolean b)
	{
		itemDraggable = b;
	}

	public boolean isItemDraggable()
	{
		return itemDraggable;
	}

	public DefaultTreeNode<Object> getRoot()
	{
		return (DefaultTreeNode<Object>) super.getRoot();
	}

	public DefaultTreeNode<Object> getParent(DefaultTreeNode<Object> treeNode)
	{
		int path[] = this.getPath(treeNode);

		if (path != null && path.length > 0)
		{
			DefaultTreeNode<Object> parentNode = getRoot();
			int index = path.length - 1;
			for (int i = 0; i < index; i++)
			{
				parentNode =  (DefaultTreeNode<Object>) getChild(parentNode, path[i]);
			}
			return parentNode;
		}
		return null;
	}
	
	public DefaultTreeNode<Object> find(DefaultTreeNode<Object> fromNode, int recordId) {
		if (fromNode == null)
			fromNode = getRoot();
		MTreeNode data = (MTreeNode) fromNode.getData();
		if (data.getNode_ID() == recordId)
			return fromNode;
		if (isLeaf(fromNode))
			return null;
		int cnt = getChildCount(fromNode);
		for (int i = 0; i < cnt; i++) {
			DefaultTreeNode<Object> child = (DefaultTreeNode<Object>)getChild(fromNode, i);
			DefaultTreeNode<Object> treeNode = find(child, recordId);
			if (treeNode != null)
				return treeNode;
		}
		return null;
	}
	
//	
//	@Override
//	public DefaultTreeNode<Object> getChild(Object parent, int index) {
//		return (DefaultTreeNode<Object>) super.getChild(parent, index);
//	}

}