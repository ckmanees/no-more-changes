package com.anees.nomorechanges.plugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.ICommitMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.anees.nomorechanges.plugin.backend.CommitMessageService;

@SuppressWarnings("restriction")
public class AICommitMessageProvider implements ICommitMessageProvider {

    @Override
    public String getMessage(IResource[] resources) {
        if (resources == null || resources.length == 0) {
            return null;
        }

        RepositoryMapping mapping = RepositoryMapping.getMapping(resources[0]);
        if (mapping == null) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),
                "Error", "Could not find an active Git repository.");
            return null;
        }
        
        Repository repository = mapping.getRepository();
        Shell activeShell = Display.getDefault().getActiveShell();
        

        return CommitMessageService.generateMessageForRepository(repository, activeShell);
    }
}