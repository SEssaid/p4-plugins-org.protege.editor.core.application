package org.protege.editor.core.plugin;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.ProtegeApplication;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Mar 17, 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class PluginUtilities {
    private static final Logger log = Logger.getLogger(PluginUtilities.class.getName());

    private static PluginUtilities instance;

    private ProtegeApplication protegeApplication;
    
    private BundleContext context;
    
    private ServiceTracker registryServiceTracker;
    
    private ServiceTracker packageServiceTracker;


    private PluginUtilities() {

    }


    /**
     * Gets the one and only instance of <code>PluginUtilities</code>.
     */
    public static synchronized PluginUtilities getInstance() {
        if (instance == null) {
            instance = new PluginUtilities();
        }
        return instance;
    }

    public void dispose() {
        registryServiceTracker.close();
        packageServiceTracker.close();
    }
    
    public BundleContext getApplicationContext() {
        return context;
    }
    
    public Bundle getApplicationBundle() {
        return context.getBundle();
    }

    /**
     * This method is called by the system to initialise the
     * plugin utilities.  Users should <b>not</b> call this method.
     */
    public void initialise(ProtegeApplication protegeApplication, BundleContext context) {
        this.protegeApplication = protegeApplication;
        this.context = context;
    }
    
    public Bundle getBundle(IExtension extension) {
        IContributor contributor = extension.getContributor();
        return getBundle(contributor);
    }
    
    public Bundle getExtensionPointBundle(IExtension extension) {
        IExtensionRegistry  registry = getExtensionRegistry();
        String extensionPtId = extension.getExtensionPointUniqueIdentifier();
        IExtensionPoint extensionPt = registry.getExtensionPoint(extensionPtId);
        IContributor contributor = extensionPt.getContributor();
        return getBundle(contributor);
    }
    
    public Bundle getBundle(IContributor contributor) {
        String name = contributor.getName();
        PackageAdmin admin = getPackageAdmin();
        Bundle[]  bundles = admin.getBundles(name, null);
        if (bundles == null || bundles.length == 0) return null;
        return bundles[0];  // if there is more than one we need more work...
    }
    
    public IExtensionRegistry getExtensionRegistry() {
        if (registryServiceTracker == null) {
            registryServiceTracker = new ServiceTracker(context, IExtensionRegistry.class.getName(), null);
            registryServiceTracker.open();
        }
        return (IExtensionRegistry) registryServiceTracker.getService();
    }
    
    public PackageAdmin getPackageAdmin() {
        if (packageServiceTracker == null) {
            packageServiceTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
            packageServiceTracker.open();
        }
        return (PackageAdmin) packageServiceTracker.getService();
    }
    
    public static Map<String, String> getAttributes(IExtension ext) {
        Map<String, String> attributes = new HashMap<String, String>();
        for (IConfigurationElement config : ext.getConfigurationElements()) {
            String id = config.getAttribute(PluginProperties.PLUGIN_XML_ID);
            String value = config.getAttribute(PluginProperties.PLUGIN_XML_VALUE);
            attributes.put(id, value);
        }
        return attributes;
    }
    
    public static String getAttribute(IExtension ext, String key) {
        return getAttributes(ext).get(key);
    }
    
    public Object getExtensionObject(IExtension ext, String property) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Bundle b = getBundle(ext);
        return b.loadClass(getAttribute(ext, property)).newInstance();
        // return config.createExecutableExtension(property);
    }
    
    public static Version getBundleVersion(Bundle b) {
        String vn = (String) b.getHeaders().get(Constants.BUNDLE_VERSION);
        if (vn == null) return null;
        return new Version(vn);
    }
    
    public String getDocumentation(IExtension extension) {
        log.error("Don't know how to get documentation yet");
        return "";
    }
    
}

