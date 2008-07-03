package org.protege.editor.core.ui.util;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 21-Sep-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class JOptionPaneEx extends JOptionPane {

    private static Logger logger = Logger.getLogger(JOptionPaneEx.class);


    public static int showConfirmDialog(JComponent parent, String title, JComponent content, int messageType,
                                        int optionType, final JComponent defaultFocusedComponent) {

        JOptionPane optionPane = new JOptionPane(content, messageType, optionType);
        JDialog dlg = createDialog(parent, title, optionPane, defaultFocusedComponent);
        dlg.setVisible(true);
        return getReturnValue(optionPane);
    }


    public static int showValidatingConfirmDialog(JComponent parent, String title, JComponent component, int messageType,
                                                  int optionType, final JComponent defaultFocusedComponent) {
        if (component instanceof VerifiedInputEditor){
            final VerifyingOptionPane optionPane = new VerifyingOptionPane(component, messageType, optionType) {
                public void selectInitialValue() {
                    // This is overridden so that the option pane dialog default
                    // button doesn't get the focus.
                }
            };
            final InputVerificationStatusChangedListener verificationListener = new InputVerificationStatusChangedListener() {
                public void verifiedStatusChanged(boolean verified) {
                    optionPane.setOKEnabled(verified);
                }
            };
            ((VerifiedInputEditor) component).addStatusChangedListener(verificationListener);

            final JDialog dlg = createDialog(parent, title, optionPane, defaultFocusedComponent);
            dlg.setModal(true);
            dlg.setVisible(true);
            return getReturnValue(optionPane);
        }
        else{
            logger.warn("Component should implement InputVerificationStatusChangedListener for validating dialog to work. " +
                        "Using normal dialog with no validating");
            return showConfirmDialog(parent, title, component, messageType, optionType, null);
        }
            }


    private static JDialog createDialog(JComponent parent, String title, JOptionPane optionPane, final JComponent defaultFocusedComponent) {
        JDialog dlg = optionPane.createDialog(parent, title);
        dlg.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if (defaultFocusedComponent != null) {
                    defaultFocusedComponent.requestFocusInWindow();
                }
            }
        });
        dlg.setLocationRelativeTo(parent);
        dlg.setResizable(true);
        dlg.pack();
        return dlg;
    }


    private static int getReturnValue(JOptionPane optionPane) {
        Object value = optionPane.getValue();
        return (value != null) ? (Integer) value : JOptionPane.CLOSED_OPTION;
    }

}
