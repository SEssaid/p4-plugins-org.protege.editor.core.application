package org.protege.editor.core.ui.preferences;

import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 08-Jun-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class PreferencesDialogPanel extends JPanel implements Disposable {


    private EditorKit editorKit;

    private Map<String, PreferencesPanel> map;

    private JTabbedPane tabbedPane;

    private static final String PREFS_HISTORY_PANEL_KEY = "prefs.history.panel";


    public PreferencesDialogPanel(EditorKit editorKit) {
        this.editorKit = editorKit;
        map = new HashMap<String, PreferencesPanel>();
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        PreferencesPanelPluginLoader loader = new PreferencesPanelPluginLoader(editorKit);
        Set<PreferencesPanelPlugin> plugins = new TreeSet<PreferencesPanelPlugin>(new Comparator<PreferencesPanelPlugin>() {
            public int compare(PreferencesPanelPlugin o1, PreferencesPanelPlugin o2) {
                String s1 = o1.getLabel();
                String s2 = o2.getLabel();
                return s1.compareTo(s2);
            }
        });
        plugins.addAll(loader.getPlugins());
        for (PreferencesPanelPlugin plugin : plugins) {
            try {
                PreferencesPanel panel = plugin.newInstance();
                panel.initialise();
                map.put(plugin.getLabel(), panel);
                tabbedPane.addTab(plugin.getLabel(), panel);
            }
            catch (Exception e) {
                ProtegeApplication.getErrorLog().logError(e);
            }
        }
        add(tabbedPane);
    }


    public void dispose() {
        for (PreferencesPanel panel : new ArrayList<PreferencesPanel>(map.values())) {
            try {
                panel.dispose();
            }
            catch (Exception e) {
                ProtegeApplication.getErrorLog().logError(e);
            }
        }
        map.clear();
    }


    public void applyPreferences() {
        for (PreferencesPanel panel : new ArrayList<PreferencesPanel>(map.values())) {
            try {
                panel.applyChanges();
            }
            catch (Exception e) {
                ProtegeApplication.getErrorLog().logError(e);
            }
        }
    }


    protected String getSelectedPanel() {
        final Component c = tabbedPane.getSelectedComponent();
        for (String tabName : map.keySet()){
            if (c.equals(map.get(tabName))){
                return tabName;
            }
        }
        return null;
    }


    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }


    public static void showPreferencesDialog(String selectedPanel, EditorKit editorKit) {
        PreferencesDialogPanel panel = new PreferencesDialogPanel(editorKit);
        final Preferences prefs = PreferencesManager.getInstance().getApplicationPreferences(PreferencesDialogPanel.class);
        if (selectedPanel == null){
            selectedPanel = prefs.getString(PREFS_HISTORY_PANEL_KEY, null);
        }
        Component c = panel.map.get(selectedPanel);
        if (c != null) {
            panel.tabbedPane.setSelectedComponent(c);
        }
        JOptionPane op = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dlg = op.createDialog(editorKit.getWorkspace(), "Preferences");
        dlg.setResizable(true);
        dlg.setVisible(true);
        Object o = op.getValue();
        if (o != null){
            int ret = (Integer)o;
            if (ret == JOptionPane.OK_OPTION) {
                panel.applyPreferences();
            }
        }

        prefs.putString(PREFS_HISTORY_PANEL_KEY, panel.getSelectedPanel());
        panel.dispose();
    }
}
