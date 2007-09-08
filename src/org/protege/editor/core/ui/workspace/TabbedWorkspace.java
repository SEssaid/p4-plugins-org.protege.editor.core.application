package org.protege.editor.core.ui.workspace;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.Resettable;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Mar 17, 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 * <p/>
 * A <code>TabbedWorkspace</code> represents a
 * <code>Workspace</code> that contains tabs.  The tabs
 * are loaded automatically depending on the <code>Workspace</code>
 * clsdescriptioneditor kit.
 */
public abstract class TabbedWorkspace extends Workspace {

    public static final String TABS_MENU_NAME = "Tabs";

    private JTabbedPane tabbedPane;

    private JMenu tabMenu;

    private Set<WorkspaceTab> workspaceTabs;

    private static final Logger logger = Logger.getLogger(TabbedWorkspace.class);


    /**
     * Override of the <code>Workspace</code> initialise method.
     */
    public void initialise() {
        JPanel tabHolder = new JPanel(new BorderLayout());

        workspaceTabs = new HashSet<WorkspaceTab>();

        // Create the tabs.
        tabbedPane = new JTabbedPane();

        tabHolder.add(tabbedPane);
        setContent(tabbedPane);

        // Here we either need to load the default tabs, or
        // restore a set of tabs
        TabbedWorkspaceStateManager man = new TabbedWorkspaceStateManager();

        WorkspaceTabPluginLoader loader = new WorkspaceTabPluginLoader(this);
        List<WorkspaceTabPlugin> orderedPlugins = new ArrayList<WorkspaceTabPlugin>(loader.getPlugins());
        Collections.sort(orderedPlugins, new Comparator<WorkspaceTabPlugin>() {
            public int compare(WorkspaceTabPlugin o1, WorkspaceTabPlugin o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        });
        for (WorkspaceTabPlugin plugin : orderedPlugins) {
            if (!man.getTabs().isEmpty()) {
                if (!man.getTabs().contains(plugin.getId())) {
                    continue;
                }
            }
            WorkspaceTab tab = null;
            try {
                tab = plugin.newInstance();
                addTab(tab);
            }
            catch (Throwable e) {
                if (tab != null) {
                    String msg = "An error occurred when creating the " + plugin.getLabel() + " tab.";
                    tab.setLayout(new BorderLayout());
                    tab.add(ComponentFactory.createExceptionComponent(msg, e, null));
                }
                Logger.getLogger(getClass().getName()).warn(e);
            }
        }
    }


    public void save() {
        try {
            super.save();
            // Save out tabs
            TabbedWorkspaceStateManager man = new TabbedWorkspaceStateManager(this);
            man.save();
        }
        catch (Exception e) {
            Logger.getLogger(getClass()).error("Exception caught doing save", e);
        }
    }


    protected void initialiseExtraMenuItems(JMenuBar menuBar) {
        super.initialiseExtraMenuItems(menuBar);
        // Add in menus for tabs
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu.getText() != null) {
                if (menu.getText().equals(TABS_MENU_NAME)) {
                    tabMenu = menu;
                    break;
                }
            }
        }
        if (tabMenu == null) {
            tabMenu = new JMenu(TABS_MENU_NAME);
            menuBar.add(tabMenu);
        }
        tabMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                rebuildTabMenu();
            }


            public void menuDeselected(MenuEvent e) {
            }


            public void menuCanceled(MenuEvent e) {
            }
        });
        rebuildTabMenu();
    }


    private void rebuildTabMenu() {
        tabMenu.removeAll();
        WorkspaceTabPluginLoader loader = new WorkspaceTabPluginLoader(this);
        List<WorkspaceTabPlugin> plugins = new ArrayList<WorkspaceTabPlugin>(loader.getPlugins());
        Collections.sort(plugins, new Comparator<WorkspaceTabPlugin>() {
            public int compare(WorkspaceTabPlugin o1, WorkspaceTabPlugin o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        });
        for (final WorkspaceTabPlugin plugin : plugins) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractAction(plugin.getLabel()) {
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (containsTab(plugin.getId()) == false) {
                            WorkspaceTab tab = plugin.newInstance();
                            addTab(tab);
                        }
                        else {
                            WorkspaceTab tab = getWorkspaceTab(plugin.getId());
                            removeTab(tab);
                            tab.dispose();
                        }
                    }
                    catch (Exception ex) {
                        logger.error("Exception caught (re) building tab menu", ex);
                    }
                }
            });
            item.setSelected(containsTab(plugin.getId()));
            tabMenu.add(item);
        }
        tabMenu.addSeparator();
        tabMenu.add(new AbstractAction("Save current layout") {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        tabMenu.addSeparator();
        Action resetTabAction = new AbstractAction("Reset selected tab to default state") {

            public void actionPerformed(ActionEvent e) {
                Resettable tab = (Resettable) tabbedPane.getSelectedComponent();
                tab.reset();
                tabbedPane.getSelectedComponent().validate();
            }
        };
        resetTabAction.setEnabled(tabbedPane.getSelectedComponent() instanceof Resettable);
        tabMenu.add(resetTabAction);
    }


    /**
     * Convenience method to add a workspace tab.  This
     * method initialises the tab.
     */
    public void addTab(WorkspaceTab workspaceTab) {
        tabbedPane.addTab(workspaceTab.getLabel(), workspaceTab.getIcon(), workspaceTab);
        workspaceTabs.add(workspaceTab);
        try {
            workspaceTab.initialise();
        }
        catch (Exception e) {
            tabbedPane.remove(workspaceTab);
            tabbedPane.addTab(workspaceTab.getLabel(), workspaceTab.getIcon(), createErrorPanel(e));
        }
    }


    public boolean containsTab(String tabId) {
        for (WorkspaceTab tab : getWorkspaceTabs()) {
            if (tab.getId().equals(tabId)) {
                return true;
            }
        }
        return false;
    }


    public int getTabCount() {
        return tabbedPane.getTabCount();
    }


    public void setSelectedTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }


    private JComponent createErrorPanel(Exception e) {
        return ComponentFactory.createExceptionComponent(e.getMessage(), e, null);
    }


    /**
     * Convenience method to remove a workspace tab.
     * If the tab is discarded then its <code>dispose()</code>
     * method must be called.
     * @param workspaceTab The tab to be removed.
     */
    public void removeTab(WorkspaceTab workspaceTab) {
        tabbedPane.remove(workspaceTab);
        workspaceTabs.remove(workspaceTab);
    }


    public void setSelectedTab(WorkspaceTab workspaceTab) {
        tabbedPane.setSelectedComponent(workspaceTab);
    }


    public WorkspaceTab getSelectedTab() {
        return (WorkspaceTab) tabbedPane.getSelectedComponent();
    }


    public Set<WorkspaceTab> getWorkspaceTabs() {
        return Collections.unmodifiableSet(workspaceTabs);
    }


    public WorkspaceTab getWorkspaceTab(String id) {
        for (WorkspaceTab tab : getWorkspaceTabs()) {
            if (tab.getId().equals(id)) {
                return tab;
            }
        }
        return null;
    }


    public abstract WorkspaceTab createWorkspaceTab(String name);


    /**
     * Disposes of the tabbed workspace.  This removes any tabs
     * in the workspace and disposes of them.
     */
    public void dispose() {
        save();
        // Remove the tabs and call their dispose method
        int count = tabbedPane.getComponentCount();
        for (int i = 0; i < count; i++) {
            WorkspaceTab tab = (WorkspaceTab) tabbedPane.getComponentAt(0);
            try {
                tab.dispose();
            }
            catch (Exception e) {
                logger.warn("BAD TAB: " + tab.getClass().getSimpleName() + " - Exception during dispose: " + e.getMessage());
            }
            removeTab(tab);
        }
    }
}
