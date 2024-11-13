package prog.models;

import javafx.beans.property.SimpleStringProperty;

public class ThreadInfoModel {
    private final SimpleStringProperty threadId;
    private final SimpleStringProperty status;
    private final SimpleStringProperty result;
    private final SimpleStringProperty output;

    public ThreadInfoModel(String threadId, String status, String result, String output) {
        this.threadId = new SimpleStringProperty(threadId);
        this.status = new SimpleStringProperty(status);
        this.result = new SimpleStringProperty(result);
        this.output = new SimpleStringProperty(output);
    }
    public String getThreadId() {
        return threadId.get();
    }

    public void setThreadId(String threadId) {
        this.threadId.set(threadId);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getResult() {
        return result.get();
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public String getOutput() {
        return output.get();
    }

    public void setOutput(String output) {
        this.output.set(output);
    }
}
