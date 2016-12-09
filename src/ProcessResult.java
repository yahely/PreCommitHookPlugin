class ProcessResult {

    private InterruptedException exception = null;
    private int exitCode = -1;
    private boolean canceled = false;

    boolean isCanceled() {
        return canceled;
    }

    void setCanceled() {
        canceled = true;
    }

    int getExitCode() {
        return exitCode;
    }

    void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    InterruptedException getException() {
        return exception;
    }

    void setException(InterruptedException exception) {
        this.exception = exception;
    }
}
