package prog.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog.FileProcessorTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadInfoController {

    private ScheduledThreadPoolExecutor executor;
    private ObservableList<String[]> threadInfoList;
    private List<ScheduledFuture<String>> futures;
    private Semaphore semaphore;

    private long startTime;

    @FXML
    private TableView<String[]> threadTable;
    @FXML
    private TableColumn<String[], String> threadIdColumn;
    @FXML
    private TableColumn<String[], String> statusColumn;
    @FXML
    private TableColumn<String[], String> resultColumn;
    @FXML
    private TableColumn<String[], String> outputColumn;
    @FXML
    private Label totalTimeLabel;

    private List<File> filesToProcess;

    private String word;
    private Thread monitorThread;

    @FXML
    public void initialize() {
        threadInfoList = FXCollections.observableArrayList();
        threadTable.setItems(threadInfoList);

        threadIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
        resultColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));
        outputColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));
    }

    public void setData(List<File> files,String word, int maxThreads) {
        this.filesToProcess = files;
        this.word = word;
        startFileProcessing(maxThreads);
    }

    private void startFileProcessing(int threadCount) {
        executor = new ScheduledThreadPoolExecutor(threadCount );
        semaphore = new Semaphore(5);
        futures = new ArrayList<>();
        startTime = System.currentTimeMillis();
        Thread scheduleTasksThread = new Thread(this::scheduleTasks);
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

            String[] threadInfo = new String[4];
            threadInfo[0] = "Waiting";
            threadInfo[1] = "Waiting";
            threadInfo[2] = "Pending";
            threadInfo[3] = "-";


            Platform.runLater(() -> threadInfoList.add(threadInfo));


            FileProcessorTask task = new FileProcessorTask(file, word,  threadInfo, semaphore, this::updateThreadInfo, i);
            ScheduledFuture<String> future = executor.schedule(task, 1, TimeUnit.SECONDS);

            futures.add(future);
        }
    }
    private synchronized void updateThreadInfo(String[] threadInfo, int taskNumber) {
        Platform.runLater(() -> {
            threadInfoList.get(taskNumber)[0] = threadInfo[0];
            threadInfoList.get(taskNumber)[1] = threadInfo[1];
            threadInfoList.get(taskNumber)[2] = threadInfo[2];

            threadTable.refresh();
        });
    }

    private void monitorTasks() {
        try {
        boolean allCompleted = false;


        boolean[] resultArray = new boolean[filesToProcess.size()];

        while (!allCompleted) {
            if (futures.isEmpty()) {
                continue;
            }
            allCompleted = true;
            for (int i = 0; i < futures.size(); i++) {
                ScheduledFuture<String> future = futures.get(i);
                if (!resultArray[i]) {
                    if (!future.isDone()) {
                        allCompleted = false;
                        break;
                    } else {
                        resultArray[i] = true;
                        int finalI = i;
                        Platform.runLater(() -> {
                            try {
                                threadInfoList.get(finalI)[3] = future.get();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                            threadTable.refresh();
                        });
                    }
                }
            }
            if (!allCompleted) {
                    Thread.sleep(1000);
            }else{
                writeResultToFile();
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Platform.runLater(() -> totalTimeLabel.setText("Total Time: " + totalTime + " ms"));

        executor.shutdown();
        }catch(InterruptedException e){

            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void writeResultToFile() throws IOException, ExecutionException, InterruptedException {
        String fileName = "result.txt";
        FileWriter fw = new FileWriter(fileName);

        for ( var future : futures){
            if (future.isDone()){
                fw.write(future.get());
                fw.write("\n");
            }
        }
        fw.close();
    }
    @FXML
    public void handleClose() {
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


        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Stage stage = (Stage) totalTimeLabel.getScene().getWindow();
        stage.close();
    }

}
