package org.protege.editor.core.ui.action;

import org.java.plugin.registry.Extension;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.plugin.ExtensionInstantiator;
import org.protege.editor.core.plugin.JPFUtil;
import org.protege.editor.core.plugin.PluginProperties;

import javax.swing.*;
/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Mar 28, 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ToolBarActionPluginJPFImpl extends ProtegeActionPluginJPFImpl implements ToolBarActionPlugin {

    public static final String EXTENSION_POINT_ID = "ToolBarAction";

    private static final String GROUP_PARAM = "group";

    public static final String GROUP_INDEX_PARAM = "groupIndex";

    private static final String DEFAULT_GROUP = "Z";

    private static final String DEFAULT_GROUP_INDEX = "Z";


    public ToolBarActionPluginJPFImpl(EditorKit editorKit, Extension extension) {
        super(editorKit, extension);
    }


    public String getGroup() {
        return PluginProperties.getParameterValue(getExtension(), GROUP_PARAM, DEFAULT_GROUP);
    }


    public String getGroupIndex() {
        return PluginProperties.getParameterValue(getExtension(), GROUP_PARAM, DEFAULT_GROUP_INDEX);
    }


    /**
     * Creates an instance of the plugin.  It is expected that
     * this instance will be "setup", but the instance's
     * initialise method will not have been called in the instantiation
     * process.
     */
    public ProtegeAction newInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ExtensionInstantiator<ProtegeAction> instantiator = new ExtensionInstantiator<ProtegeAction>(getExtension());
        ProtegeAction menuAction = instantiator.instantiate();
        menuAction.putValue(AbstractAction.NAME, getName());
        menuAction.putValue(AbstractAction.SHORT_DESCRIPTION, getToolTipText());
        menuAction.setEditorKit(getEditorKit());
        return menuAction;
    }


    public String getDocumentation() {
        return JPFUtil.getDocumentation(getExtension());
    }
}
