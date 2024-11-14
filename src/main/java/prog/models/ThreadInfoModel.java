package prog.models;

import javafx.beans.property.SimpleStringProperty;

public class ThreadInfoModel {
    private final SimpleStringProperty threadName;
    private final SimpleStringProperty status;
    private final SimpleStringProperty result;
    private final SimpleStringProperty output;

    public ThreadInfoModel(String threadId, String status, String result, String output) {
        this.threadName = new SimpleStringProperty(threadId);
        this.status = new SimpleStringProperty(status);
        this.result = new SimpleStringProperty(result);
        this.output = new SimpleStringProperty(output);
    }
    public SimpleStringProperty threadNameProperty() {
        return threadName;
    }

    public void setThreadNameProperty(String threadId) {
        this.threadName.set(threadId);
    }

    public SimpleStringProperty threadStatusProperty() {
        return status;
    }

    public void setThreadStatusProperty(String status) {
        this.status.set(status);
    }

    public SimpleStringProperty threadResultProperty() {
        return result;
    }

    public void setThreadResultProperty(String result) {
        this.result.set(result);
    }

    public SimpleStringProperty threadOutputProperty() {
        return output;
    }

    public void setThreadOutputProperty(String output) {
        this.output.set(output);
    }
}
