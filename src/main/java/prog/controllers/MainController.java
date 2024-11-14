package prog.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import prog.Application;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    private TextField threadCountInput;

    @FXML
    private TextField wordInput ;

    private List<File> filesToProcess;

    @FXML
    private Label welcomeText;

    private  String wordToFind;
    @FXML
    public void onStartButtonClick() {

        int threadCount = Integer.parseInt(threadCountInput.getText());
        wordToFind = wordInput.getText().trim();

        if(filesToProcess != null && !filesToProcess.isEmpty() && isValid(wordToFind.trim())) {
            openThreadInfoWindow(threadCount);
        }
    }

    private boolean isValid(String wordToFind) {

        return wordToFind != null && !wordToFind.isEmpty() && !wordToFind.contains(" ");
    }


    @FXML
    protected void onChooseDirectoryButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Виберіть директорію");

        File selectedDirectory = directoryChooser.showDialog(new Stage());

        if (selectedDirectory != null && selectedDirectory.isDirectory()) {

            List<File> textFiles = getTextFilesFromDirectory(selectedDirectory);


            welcomeText.setText("Знайдено " + textFiles.size() + " текстових файлів.");
            filesToProcess = textFiles;
        } else {
            welcomeText.setText("Директорію не вибрано або це не директорія.");
        }
    }

    private List<File> getTextFilesFromDirectory(File directory) {
        return Arrays.stream(directory.listFiles())
                .filter(file -> file.isFile() && file.getName().endsWith(".txt"))
                .collect(Collectors.toList());
    }

    private void openThreadInfoWindow(int maxFilesToProcess ) {
        try {

            FXMLLoader loader = new FXMLLoader(Application.class.getResource("thread-info-window.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            ThreadInfoController controller = loader.getController();
            controller.setData(filesToProcess, wordToFind,  maxFilesToProcess);
            Stage secondStage = new Stage();
            secondStage.setTitle("Інформація про потоки");
            secondStage.setScene(scene);
            secondStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
