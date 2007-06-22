package org.protege.editor.core.ui.util;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.net.URL;
import java.net.URI;
import java.util.ArrayList;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 12-Jun-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class URLPanel extends JPanel {

    private JTextField textField;

    private ArrayList<ChangeListener> listeners;

    public URLPanel() {
        this.textField = ComponentFactory.createTextField();
        listeners = new ArrayList<ChangeListener>();
        setLayout(new BorderLayout(7, 7));
        add(textField, BorderLayout.NORTH);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    public void requestFocus() {
        // Pass on to our text field
        textField.requestFocus();
    }

    public URL getURL() {
        try {
            URI uri = new URI(textField.getText());
            return uri.toURL();
        } catch (Exception e) {
            return null;
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    protected void fireChange() {
        for (ChangeListener listener : new ArrayList<ChangeListener>(listeners)) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

}
