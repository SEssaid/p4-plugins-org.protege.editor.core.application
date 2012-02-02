package org.protege.editor.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.protege.editor.core.editorkit.EditorKitDescriptor;
import org.protege.editor.core.editorkit.EditorKitFactoryPlugin;
import org.protege.editor.core.editorkit.RecentEditorKitManager;
import org.protege.editor.core.ui.OpenFromRepositoryPanel;
import org.protege.editor.core.ui.OpenFromURLPanel;
import org.protege.editor.core.ui.action.start.AltStartupActionPlugin;
import org.protege.editor.core.ui.action.start.AltStartupActionPluginLoader;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.util.LinkLabel;


/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Medical Informatics Group<br> Date: Mar 27,
 * 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br> www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ProtegeWelcomeFrame extends JFrame {

	private static final long serialVersionUID = 3174444631540765155L;

    private ProtegeWelcomeFrame.ProtegeWelcomePanel welcomePanel;
    
    private static final int MAX_LENGTH = 75;
    
    public ProtegeWelcomeFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setName("ProtegeWelcome");
        welcomePanel = new ProtegeWelcomePanel();
        setContentPane(welcomePanel);
        pack();
        centre();
    }

    public void setVisible(boolean b) {
        if (b){
            welcomePanel.refresh();
            pack();
            centre();
        }
        super.setVisible(b);
    }

    private void centre() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);
    }

    private class ProtegeWelcomePanel extends JPanel {

        private static final long serialVersionUID = 6744371623503619618L;

        private Icon background;

        private Box box;

        private Box recentLinkBox;

        public ProtegeWelcomePanel() {
            setBackground(Color.WHITE);
            background = Icons.getIcon("logo.wizard.png");

            final ProtegeManager manager = ProtegeManager.getInstance();

            int strutHeight = 10;

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            add(panel);

            JLabel label = new JLabel("Welcome to Prot\u00E9g\u00E9");
            label.setFont(getFont().deriveFont(Font.BOLD, 35.0f));
            label.setBorder(BorderFactory.createEmptyBorder(30, 0, 50, 0));
            label.setForeground(Color.LIGHT_GRAY);
            panel.add(label, BorderLayout.NORTH);

            box = new Box(BoxLayout.Y_AXIS);
            box.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
            panel.add(box, BorderLayout.CENTER);
            
            panel.add(createOtherActions(), BorderLayout.SOUTH);

            for (final EditorKitFactoryPlugin plugin : manager.getEditorKitFactoryPlugins()) {

                LinkLabel createLink = new LinkLabel("Create new " + plugin.getLabel(), new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (ProtegeManager.getInstance().createAndSetupNewEditorKit(plugin)) {
                                dispose();
                            }
                        }
                        catch (Throwable e1) {
                            ErrorLogPanel.showErrorDialog(e1);
                        }
                    }
                });
                createLink.setName("Create new " + plugin.getId());
                box.add(createLink);

                box.add(Box.createVerticalStrut(strutHeight));

                LinkLabel openLink = new LinkLabel("Open " + plugin.getLabel(), new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            if (ProtegeManager.getInstance().openAndSetupEditorKit(plugin)) {
                                dispose();
                            }
                        }
                        catch (Throwable e1) {
                            ErrorLogPanel.showErrorDialog(e1);
                        }
                    }
                });
                openLink.setName("Open " + plugin.getId());
                box.add(openLink);

                box.add(Box.createVerticalStrut(strutHeight));

                LinkLabel openFromURILink = new LinkLabel("Open " + plugin.getLabel() + " from URI",
                                                          new ActionListener() {
                                                              public void actionPerformed(ActionEvent e) {
                                                                  handleOpenFromURI(plugin);
                                                              }
                                                          });
                openFromURILink.setName("OpenfromURI " + plugin.getId());
                box.add(openFromURILink);

                box.add(Box.createVerticalStrut(strutHeight));
            }

            // Repositories
            
            OntologyBuilderPluginLoader ontologyBuilderManager = new OntologyBuilderPluginLoader();
            for (final OntologyBuilderPlugin builder : ontologyBuilderManager.getPlugins()) {
                box.add(new LinkLabel(builder.getLabel(), new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (ProtegeManager.getInstance().handleOpenFromBuilder(builder)) {
                            dispose();
                        }
                    }
                }));
            }

            OntologyRepositoryManager repMan = OntologyRepositoryManager.getManager();

            for (final OntologyRepository repository : repMan.getOntologyRepositories()) {
                box.add(new LinkLabel("Open from the " + repository.getName() + " repository", new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        handleOpenFromRepository(repository);
                    }
                }));
            }


            box.add(Box.createVerticalStrut(2 * strutHeight));
            refresh();
        }
        
        private JComponent createOtherActions() {
        	Color fontColor = PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.PROPERTY_COLOR_KEY),
                    							    Color.GRAY);
            JPanel otherActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            otherActionsPanel.setOpaque(false);
            JMenuBar littleMenuBar = new JMenuBar();
            littleMenuBar.setOpaque(false);
            littleMenuBar.setBorder(null);
            final JMenu dropDownMenu = new JMenu("More...");
            dropDownMenu.setFont(getFont().deriveFont(Font.BOLD, 10.0f));
            dropDownMenu.setForeground(fontColor);
            littleMenuBar.add(dropDownMenu);
            for (AltStartupActionPlugin plugin : new AltStartupActionPluginLoader(ProtegeWelcomeFrame.this).getPlugins()) {
            	try {
            		AbstractAction a = plugin.newInstance();
            		JMenuItem subMenu = new JMenuItem(a);
            		dropDownMenu.add(subMenu);
            	}
            	catch (Exception e) {
            		ProtegeApplication.getErrorLog().logError(e);
            	}
            }
            otherActionsPanel.add(littleMenuBar);
            Box southBox = new Box(BoxLayout.Y_AXIS);
            southBox.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
            southBox.add(otherActionsPanel);
            southBox.setOpaque(false);
            return southBox;
        }

        private void handleOpenFromURI(EditorKitFactoryPlugin plugin) {
            try {
                URI uri = OpenFromURLPanel.showDialog();
                if (uri != null) {
                    if (ProtegeManager.getInstance().loadAndSetupEditorKitFromURI(plugin, uri)) {
                        dispose();
                    }
                }
            }
            catch (Exception e1) {
                ErrorLogPanel.showErrorDialog(e1);
            }
        }

        private void handleOpenFromRepository(OntologyRepository repository) {
            try {
                OntologyRepositoryEntry entry = OpenFromRepositoryPanel.showDialog(repository);
                if(entry == null) {
                    return;
                }
                if (ProtegeManager.getInstance().loadAndSetupEditorKitFromRepository(repository, entry)) {
                    dispose();
                }
            }
            catch (Exception e) {
                ErrorLogPanel.showErrorDialog(e);
            }
        }

        public Dimension getPreferredSize() {
            Dimension prefSize = super.getPreferredSize();
            prefSize.width += 30;
            prefSize.height += 30;
            return prefSize;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            background.paintIcon(this, g2, 0, 0);
            Color oldColor = g2.getColor();
            g2.setColor(new Color(255, 255, 255, 240));
            g2.fillRect(0, 0, background.getIconWidth(), background.getIconHeight());

            g2.setColor(oldColor);
        }

        public void refresh() {
            if (recentLinkBox != null) {
                box.remove(recentLinkBox);
                recentLinkBox = null;
            }

            if (RecentEditorKitManager.getInstance().getDescriptors().size() > 0) {
                recentLinkBox = new Box(BoxLayout.Y_AXIS);

                Color color = PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.PROPERTY_COLOR_KEY),
                                                    Color.GRAY);
                Color classColor = PropertyUtil.getColor(ProtegeProperties.getInstance().getProperty(ProtegeProperties.CLASS_COLOR_KEY),
                                                         Color.GRAY);

                recentLinkBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, classColor),
                        " Open recent ",
                        0,
                        0,
                        getFont().deriveFont(Font.BOLD),
                        color), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
                
                for (final EditorKitDescriptor desc : RecentEditorKitManager.getInstance().getDescriptors()) {

                	String label = desc.getLabel();
                	if (label.length() > MAX_LENGTH) {
                		ArrayList<String> list = buildList(label);
                		list = reduce(list);
                		label = rebuildPath(list);
                	}
                	
                   	LinkLabel linkLabel = new LinkLabel(label, new ActionListener() {
						@Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                if (ProtegeManager.getInstance().openAndSetupRecentEditorKit(desc)) {
                                    dispose();
                                }
                            }
                            catch (Exception e1) {
                                ErrorLogPanel.showErrorDialog(e1);
                            }
                        }
					});
                	linkLabel.setToolTipText(desc.getLabel());
                    recentLinkBox.add(linkLabel);
                }

                box.add(recentLinkBox);
            }

            revalidate();
        }
    }

    private static ArrayList<String> buildList(String path) {
        ArrayList<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(path, "/");
        for (int i = 0; st.hasMoreElements(); i++) {
            list.add(i, (String) st.nextElement());
        }
        return list;
    }

    private ArrayList<String> reduce(ArrayList<String> list) {
        // sanity checks
        if (list == null) {
            return null;
        }
        if (list.size() == 0) {
            return list;
        }
        if (getLength(list) <= MAX_LENGTH) {
            return list;
        }

        // size is 1
        if (list.size() == 1) {
            String fname = (String) list.get(0);
            fname = fname.substring(0, (MAX_LENGTH - 3)) + "...";
            list.set(0, fname);
        }
        // size is 2
        else if (list.size() == 2) {
            if (list.get(0).equals("...")) {
                list.remove(0);
            } else {
                list.set(0, "...");
            }
        }
        // size is 3 or more
        else {
            if (!list.contains("...")) {
                int mid = list.size() / 2;
                list.set(mid, "...");
            } else {
                int idxDot = list.indexOf("...");
                int before = idxDot;
                int after = (list.size() - 2) - idxDot;

                if (before > after) {
                    list.remove(idxDot - 1);
                } else {
                    list.remove(idxDot + 1);
                }
            }
        }

        if (getLength(list) <= MAX_LENGTH) {
            // list is short enough
            return list;
        }
        // list is too long - recurse
        return reduce(list);
    }

    private static int getLength(ArrayList<String> list) {
        String path = "";
        String sep = "/";
        for (int i = 0; i < list.size(); i++) {
            path += list.get(i) + sep;
        }
        return path.length() - 1;
    }

    private static String rebuildPath(ArrayList<String> list) {
        String path = "";
        String sep = "/";
        for (int i = 0; i < list.size(); i++) {
            path += list.get(i) + sep;
        }
        path = path.substring(0, (path.length() - 1));
        return path;
    }
}
