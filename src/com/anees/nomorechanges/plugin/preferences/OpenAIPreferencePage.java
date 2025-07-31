package com.anees.nomorechanges.plugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.anees.nomorechanges.plugin.Activator;

public class OpenAIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String OPENAI_API_KEY = "openai.api.key";

    public OpenAIPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Configure OpenAI API Key for Commit Message Generation");
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(OPENAI_API_KEY, "OpenAI API Key:", getFieldEditorParent()));
    }
}