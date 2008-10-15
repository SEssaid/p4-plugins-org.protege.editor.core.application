package org.protege.editor.core.editorkit.plugin;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.plugin.ExtensionInstantiator;
import org.protege.editor.core.plugin.JPFUtil;
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
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 15, 2008<br><br>
 */
public class EditorKitHookPluginImpl implements EditorKitHookPlugin {

    private IExtension iExtension;

    private EditorKit editorKit;

    public EditorKitHookPluginImpl(IExtension iExtension, EditorKit editorKit) {
        this.iExtension = iExtension;
        this.editorKit = editorKit;
    }


    public String getId() {
        return EXTENSION_POINT_ID;
    }


    public String getDocumentation() {
        return JPFUtil.getDocumentation(iExtension);
    }


    public EditorKitHook newInstance() throws ClassNotFoundException, IllegalAccessException,
                                                         InstantiationException {
        ExtensionInstantiator<EditorKitHook> instantiator = new ExtensionInstantiator<EditorKitHook>(iExtension);
        EditorKitHook instance = instantiator.instantiate();
        instance.setup(editorKit);
        return instance;
    }
}
