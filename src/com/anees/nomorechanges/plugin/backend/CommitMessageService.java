package com.anees.nomorechanges.plugin.backend;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CommitMessageService {

    /**
     * Generates a commit message for the currently staged files in a repository.
     * This method contains the core logic with a progress dialog.
     * @param repository The Git repository.
     * @param shell The active shell to parent the dialogs.
     * @return The generated commit message, or null on failure or if no changes are staged.
     */
    public static String generateMessageForRepository(Repository repository, Shell shell) {
        try {
        	//Get the staged diff
            String stagedDiff;
            try (Git git = new Git(repository); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                git.diff().setCached(true).setOutputStream(out).call();
                stagedDiff = out.toString(StandardCharsets.UTF_8);
            }
            

            if (stagedDiff.trim().isEmpty()) {
            	Display.getDefault().syncExec(new Runnable() {
    				public void run() {
    					MessageDialog.openInformation(shell, "Info", "No changes are staged to be committed.");
    				}
    			});
                return ""; // Return empty string to clear the box
            }

            final String[] commitMessage = new String[1];
            IRunnableWithProgress operation = monitor -> {
                monitor.beginTask("Generating AI commit message...", IProgressMonitor.UNKNOWN);
                try {
                    CommitMessageGenerator generator = new CommitMessageGenerator();
                    commitMessage[0] = generator.generateCommitMessage(stagedDiff);
                } catch (Exception e) {
                    throw new InvocationTargetException(e, "Failed to generate commit message.");
                } finally {
                    monitor.done();
                }
            };

            Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						new ProgressMonitorDialog(shell).run(true, false, operation);
					} catch (InvocationTargetException | InterruptedException e) {
						MessageDialog.openError(shell, "AI Error", "Failed to generate commit message: " + e.getCause());
						e.printStackTrace();
					}
				}
			});
            
            return commitMessage[0];

        } catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(shell, "Error", "Failed to get staged diff: " + e.getMessage());
				}
			});
			
			e.printStackTrace();
        }
        return null;
    }
}