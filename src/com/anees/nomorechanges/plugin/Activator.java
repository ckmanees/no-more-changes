package com.anees.nomorechanges.plugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.anees.nomorechanges.plugin.preferences.PreferencePage;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "NoMoreChanges";
    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        return super.getPreferenceStore();
    }

    public static String getOpenAIApiKey() {
        return getDefault().getPreferenceStore().getString(PreferencePage.OPENAI_API_KEY);
    }
}