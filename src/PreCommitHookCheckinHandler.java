import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

class PreCommitHookCheckinHandler extends CheckinHandler {

    private static final String title = "Pre Commit Hook Plugin";
    private static final String doYouWantToCommitMessage ="Do you want to commit? \nCommit without running pre-commit-hook is not recommended.";
    private final Project project;
    private final CheckinProjectPanel checkinProjectPanel;

    PreCommitHookCheckinHandler(final CheckinProjectPanel checkinProjectPanel) {
        this.project = checkinProjectPanel.getProject();
        this.checkinProjectPanel = checkinProjectPanel;
    }

    public ReturnResult beforeCheckin(CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (DumbService.getInstance(project).isDumb()) {
            Messages.showErrorDialog(project, "Cannot commit right now because IDE updates the indices " +
                            "of the project in the background. Please try again later.",
                    title);
            return ReturnResult.CANCEL;
        }

        VirtualFile hook = project.getBaseDir().findChild("pre-commit-hook.sh");
        if (hook != null && hook.exists()) {
            try {
                final String[] changes = getChanges();
                final String command = hook.getCanonicalPath();

                final String[] commandWithArguments = new String[1 + changes.length];
                commandWithArguments[0] = command;
                System.arraycopy(changes, 0, commandWithArguments, 1, changes.length);

                final Process process = Runtime.getRuntime().exec(commandWithArguments,
                        null,
                        new File(project.getBaseDir().getCanonicalPath()));

                final Barrier waitEvent = new Barrier(false);
                final ProcessResult result = new ProcessResult();

                final Task.Modal taskModal = new Task.Modal(project, title, true) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        progressIndicator.setIndeterminate(true);
                        progressIndicator.setText("Running pre commit hook script...");
                        try {
                            while(process.isAlive()){
                                progressIndicator.checkCanceled();
                                Thread.sleep(10);
                            }
                            result.setExitCode(process.waitFor());
                        } catch (InterruptedException e) {
                            result.setException(e);
                        }
                        catch(ProcessCanceledException e){
                            result.setCanceled();
                        }
                        waitEvent.done();
                    }
                };
                taskModal.setCancelText("Stop");

                ProgressManager.getInstance().run(taskModal);

                waitEvent.waitForDone();
                if( result.isCanceled()){
                    return onUserCancel();
                }
                if (result.getException() != null) {
                    throw result.getException();
                }
                if (result.getExitCode() == 0) {
                    return ReturnResult.COMMIT;
                } else {
                    String input = readInputStream(process.getInputStream());
                    return showDialogToUser("Pre commit hook exited with error: \n" + input+ doYouWantToCommitMessage);
                }
            } catch (IOException | InterruptedException e) {
                return onException(e);
            }
        } else {
            return ReturnResult.COMMIT;
        }
    }

    private String[] getChanges() {
        return checkinProjectPanel.getSelectedChanges()
                .stream()
                .flatMap(this::getAllRevisionPaths)
                .distinct()
                .toArray(String[]::new);
    }

    private Stream<String> getAllRevisionPaths(Change change) {
        ContentRevision[] revisions = new ContentRevision[2];
        revisions[0] = change.getBeforeRevision();
        revisions[1] = change.getAfterRevision();

        return Arrays.stream(revisions)
                .filter(Objects::nonNull)
                .map(ContentRevision::getFile)
                .map(FilePath::getPath)
                .distinct();
    }

    @NotNull
    private String readInputStream(InputStream stream)
            throws IOException {
        final int bufferSize = 2048;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream, "UTF-8");
        while (true) {
            int size = in.read(buffer, 0, buffer.length);
            if (size < 0) {
                break;
            }
            out.append(buffer, 0, size);
        }
        return out.toString();
    }

    private ReturnResult onException(Exception e) {
        String message = "Exception while running pre-commit hook : " + e.getMessage() + "\nWould you like to commit?";
        return showDialogToUser(message);
    }

    private ReturnResult onUserCancel() {
        return showDialogToUser(doYouWantToCommitMessage);
    }

    private ReturnResult showDialogToUser(String text) {
        if (Messages.showDialog(project,
                text,
                title,
                new String[]{"Yes", "No"},
                1,
                null) == 0) {
            return ReturnResult.COMMIT;
        } else {
            return ReturnResult.CANCEL;
        }
    }

}

