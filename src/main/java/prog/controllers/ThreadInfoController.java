package prog.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog.FileProcessorTask;
import prog.models.ThreadInfoModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadInfoController {

    private static final int MAX_SEMAPHORE_PERMITS = 5;
    private static final int MONITOR_INTERVAL_MS = 1000;

    private ScheduledThreadPoolExecutor executor;
    private ObservableList<ThreadInfoModel> threadInfoList;
    private List<Future<String>> futures;
    private Semaphore semaphore;
    private long startTime;

    @FXML
    private TableView<ThreadInfoModel> threadTable;
    @FXML
    private TableColumn<ThreadInfoModel, String> threadIdColumn;
    @FXML
    private TableColumn<ThreadInfoModel, String> statusColumn;
    @FXML
    private TableColumn<ThreadInfoModel, String> resultColumn;
    @FXML
    private TableColumn<ThreadInfoModel, String> outputColumn;
    @FXML
    private Label totalTimeLabel;

    private List<File> filesToProcess;
    private String word;
    private  Thread scheduleTasksThread, monitorThread;

    @FXML
    public void initialize() {
        threadInfoList = FXCollections.observableArrayList();
        threadTable.setItems(threadInfoList);

        threadIdColumn.setCellValueFactory(cellData -> cellData.getValue().threadNameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().threadStatusProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().threadResultProperty());
        outputColumn.setCellValueFactory(cellData -> cellData.getValue().threadOutputProperty());
    }

    public void setData(List<File> files, String word, int maxThreads) {
        this.filesToProcess = files;
        this.word = word;
        startFileProcessing(maxThreads);
    }

    private void startFileProcessing(int threadCount) {
        executor = new ScheduledThreadPoolExecutor(threadCount);
        semaphore = new Semaphore(MAX_SEMAPHORE_PERMITS);
        futures = new ArrayList<>();
        startTime = System.currentTimeMillis();
        scheduleTasksThread = new Thread(this::scheduleTasks);
        scheduleTasksThread.start();
        monitorThread = new Thread(this::monitorTasks);
        monitorThread.start();

    }
    private void scheduleTasks() {
        for ( int i = 0; i < filesToProcess.size(); i++ ) {
            File file = filesToProcess.get(i);
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }
            ThreadInfoModel threadInfo = new ThreadInfoModel("Waiting", "Waiting", "Pending", "-");

            Platform.runLater(() -> threadInfoList.add(threadInfo));

            FileProcessorTask task = new FileProcessorTask(file, word,  threadInfo, semaphore, this::updateThreadInfo, i);
            ScheduledFuture<String> future = executor.schedule(task, 1, TimeUnit.SECONDS);

            futures.add(future);
        }
    }




    private synchronized void updateThreadInfo(ThreadInfoModel threadInfo, int taskNumber) {
        Platform.runLater(() -> {
            threadInfoList.set(taskNumber, threadInfo);
            threadTable.refresh();
        });
    }

    private void monitorTasks() {
        try {
            while (!executor.isTerminated() && !allTasksCompleted()) {
                Thread.sleep(MONITOR_INTERVAL_MS);
            }
            Platform.runLater(() -> totalTimeLabel.setText("Total Time: " + (System.currentTimeMillis() - startTime) + " ms"));
            writeResultsToFile();
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | ExecutionException e) {
            throw new RuntimeException("Error while monitoring tasks.", e);
        }
    }

    private boolean allTasksCompleted() throws ExecutionException, InterruptedException {
        boolean allCompleted = true;
        if(futures.isEmpty()){
            return false;
        }
        for (int i = 0; i < futures.size(); i++) {
            Future<String> future = futures.get(i);
            if (!future.isDone()) {
                allCompleted = false;
            } else if (threadInfoList.get(i).threadOutputProperty().get().equals("-")) {
                String result = future.get();
                threadInfoList.get(i).setThreadOutputProperty(result);
                updateThreadInfo(threadInfoList.get(i), i);
            }
        }
        return allCompleted;
    }

    private void writeResultsToFile() throws IOException, ExecutionException, InterruptedException {
        try (FileWriter writer = new FileWriter("result.txt")) {
            for (Future<String> future : futures) {
                if (future.isDone()) {
                    writer.write(future.get() + System.lineSeparator());
                }
            }
        }
    }

    @FXML
    public void handleClose() {
        shutdownSystemThreads();
        shutdownExecutor();
        Stage stage = (Stage) totalTimeLabel.getScene().getWindow();
        stage.close();
    }

    private void shutdownSystemThreads(){
        if (monitorThread != null && monitorThread.isAlive()) {
            monitorThread.interrupt();
            try {
                monitorThread.join();
            } catch (InterruptedException ignored) {

            }
        }
        if (scheduleTasksThread != null && scheduleTasksThread.isAlive()) {
            scheduleTasksThread.interrupt();
            try {
                scheduleTasksThread.join();
            } catch (InterruptedException ignored) {

            }
        }
    }

    private void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
