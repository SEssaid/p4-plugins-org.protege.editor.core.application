package org.protege.editor.core.ui.workspace.views;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;

import org.protege.editor.core.ui.action.ProtegeDynamicAction;
import org.protege.editor.core.ui.view.ViewComponentPlugin;
import org.protege.editor.core.ui.view.ViewComponentPluginLoader;
import org.protege.editor.core.ui.workspace.TabbedWorkspace;
import org.protege.editor.core.ui.workspace.WorkspaceViewManager;

public class ViewMenuAction extends ProtegeDynamicAction {
	private static final long serialVersionUID = 7169780321512922057L;


	public void initialise() throws Exception {
	}

	
	public void dispose() throws Exception {

	}

	
	public void actionPerformed(ActionEvent e) {

	}

	
	public void rebuildChildMenuItems(JMenu viewMenu) {
		
        if (!(getWorkspace() instanceof TabbedWorkspace)) {
            // Don't bother to show a view menu for non
            // tabbed workspaces.
            return;
        }

        final TabbedWorkspace workspace = (TabbedWorkspace) getWorkspace();
        
        // First categorise them

        Map<String, List<ViewComponentPlugin>> categoriesMap = new HashMap<String, List<ViewComponentPlugin>>();

        ViewComponentPluginLoader loader = new ViewComponentPluginLoader(workspace);
        for (ViewComponentPlugin plugin : loader.getPlugins()) {
            Set<String> categories = plugin.getCategorisations();
            if (!categories.isEmpty()) {
                for (String category : categories) {
                    List<ViewComponentPlugin> plugins = categoriesMap.get(category);
                    if (plugins == null) {
                        plugins = new ArrayList<ViewComponentPlugin>();
                        categoriesMap.put(category, plugins);
                    }
                    plugins.add(plugin);
                }
            }
            else {
                List<ViewComponentPlugin> plugins = categoriesMap.get("Misc");
                if (plugins == null) {
                    plugins = new ArrayList<ViewComponentPlugin>();
                    categoriesMap.put("Misc", plugins);
                }
                plugins.add(plugin);
            }
        }

        List<String> categories = new ArrayList<String>();
        categories.addAll(categoriesMap.keySet());
        Collections.sort(categories);
        for (String category : categories) {
            JMenu subMenu = new JMenu(category + " views");
            viewMenu.add(subMenu);
            List<ViewComponentPlugin> viewPlugins = new ArrayList<ViewComponentPlugin>(categoriesMap.get(category));
            // Sort them
            Collections.sort(viewPlugins, new Comparator<ViewComponentPlugin>() {
                public int compare(ViewComponentPlugin o1, ViewComponentPlugin o2) {
                    return o1.getLabel().compareTo(o2.getLabel());
                }
            });

            for (final ViewComponentPlugin plugin : viewPlugins) {
                Action action = new AbstractAction(plugin.getLabel()) {
                    /**
                     * 
                     */
                    private static final long serialVersionUID = 282453625948165209L;

                    public void actionPerformed(ActionEvent e) {
            	        WorkspaceViewManager viewManager = workspace.getViewManager();
            	        viewManager.showView(plugin.getId());;
                    }
                };
                action.putValue(AbstractAction.SHORT_DESCRIPTION, plugin.getDocumentation());
                subMenu.add(action);
            }
        }
	}
}
