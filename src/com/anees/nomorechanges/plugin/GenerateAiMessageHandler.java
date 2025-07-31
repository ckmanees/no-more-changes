package com.anees.nomorechanges.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.ui.internal.staging.StagingView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.anees.nomorechanges.plugin.backend.CommitMessageService;

@SuppressWarnings("restriction")
public class GenerateAiMessageHandler extends AbstractHandler {

    private static final long TIMEOUT_SECONDS = 30;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null) {
            showError("Error", "No active workbench window found.");
            return null;
        }
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            showError("Error", "No active workbench page found.");
            return null;
        }

        IViewPart view = page.findView("org.eclipse.egit.ui.StagingView");
        if (!(view instanceof StagingView)) {
            showError("Error", "Please run this command from the Git Staging View.");
            return null;
        }

        StagingView stagingView = (StagingView) view;
        Shell shell = HandlerUtil.getActiveShell(event);

        StyledText commitMessageText = null;
        try {
            Control viewControl = null;
            try {
                Field controlField = StagingView.class.getDeclaredField("commitMessageSection");
                controlField.setAccessible(true);
                viewControl = (Control) controlField.get(stagingView);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();            }

            if (viewControl != null && viewControl instanceof Composite) {
                commitMessageText = findStyledText((Composite) viewControl);
            } else {
                // Fallback to commitMessageComponent reflection
                Field cmcField = StagingView.class.getDeclaredField("commitMessageComponent");
                cmcField.setAccessible(true);
                Object cmc = cmcField.get(stagingView);
                try {
                    Field textField = cmc.getClass().getDeclaredField("commitMessageText"); // Try the Text field
                    textField.setAccessible(true);
                    Object textControl = textField.get(cmc);
                    if (textControl instanceof Text) {
                        commitMessageText = findStyledTextInText((Text) textControl);
                    } else {
                        System.out.println("commitMessageText is not Text, found: " + (textControl != null ? textControl.getClass().getName() : "null"));
                    }
                } catch (NoSuchFieldException e) {
                    System.out.println("commitMessageText not found: " + e.getMessage());
                    Field[] cmcFields = cmc.getClass().getDeclaredFields();
                    System.out.println("Available fields in commitMessageComponent: " + Arrays.toString(cmcFields));
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            System.out.println("Reflection failed: " + e.getMessage());
            showError("Reflection Error", "Failed to access commit message field: " + e.getMessage());
            return null;
        }

        if (commitMessageText == null || commitMessageText.isDisposed()) {
            System.out.println("Commit message text is null or disposed.");
            showError("Error", "Could not find commit message text field.");
            return null;
        }

        final String[] newMessage = { null };
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        IRunnableWithProgress operation = monitor -> {
            monitor.beginTask("Generating AI commit message...", IProgressMonitor.UNKNOWN);
            try {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    if (monitor.isCanceled()) {
                        throw new RuntimeException("Operation canceled");
                    }
                    return CommitMessageService.generateMessageForRepository(stagingView.getCurrentRepository(), shell);
                });
                newMessage[0] = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.out.println("Timeout occurred: " + e.getMessage());
                throw new InvocationTargetException(e, "Commit message generation timed out after " + TIMEOUT_SECONDS + " seconds");
            } catch (Exception e) {
                System.out.println("Exception in operation: " + e.getMessage());
                e.printStackTrace();
                throw new InvocationTargetException(e, "Failed to generate commit message");
            } finally {
                monitor.done();
            }
        };

        dialog.setCancelable(true);
        dialog.setBlockOnOpen(false);
        try {
            dialog.run(true, true, operation);
            if (newMessage[0] != null) {
                final StyledText finalText = commitMessageText;
                Display.getDefault().asyncExec(() -> {
                    finalText.setText(newMessage[0]);
                    finalText.setCaretOffset(0);
                });
            }
        } catch (InvocationTargetException e) {
            System.out.println("Dialog error: " + e.getCause().getMessage());
            e.getCause().printStackTrace(); 
            Display.getDefault().asyncExec(() -> {
                showError("AI Error", e.getCause().getMessage());
            });
        } catch (InterruptedException e) {
            System.out.println("Dialog interrupted: " + e.getMessage());
            e.printStackTrace();
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openInformation(shell, "Info", "Commit message generation canceled.");
            });
        } catch (Exception e) {
            System.out.println("Unexpected dialog exception: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private StyledText findStyledText(Composite composite) {
        for (Control control : composite.getChildren()) {
            if (control instanceof StyledText) {
                return (StyledText) control;
            }
            if (control instanceof Composite) {
                StyledText found = findStyledText((Composite) control);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private StyledText findStyledTextInText(Text text) {
        if (text == null) return null;
        Control parent = text.getParent();
        if (parent instanceof Composite) {
            return findStyledText((Composite) parent);
        }
        return null;
    }

    private void showError(String title, String message) {
        Display.getDefault().asyncExec(() -> {
            MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
        });
    }
}