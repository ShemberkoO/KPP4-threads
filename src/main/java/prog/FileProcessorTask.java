package prog;

import javafx.application.Platform;
import prog.models.ThreadInfoModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessorTask implements Callable<String> {
    private final File file;
    private final ThreadInfoModel threadInfo;
    private final Semaphore semaphore;
    private final ThreadInfoUpdater updateThreadInfo;
    private final String wordToFind;
    private final int num;

    public FileProcessorTask(File file,String wordToFind, ThreadInfoModel threadInfo, Semaphore semaphore, ThreadInfoUpdater updateThreadInfo, int num) {
        this.file = file;
        this.wordToFind = wordToFind;
        this.num = num;
        this.threadInfo = threadInfo;
        this.semaphore = semaphore;
        this.updateThreadInfo = updateThreadInfo;
    }

    @Override
    public String call() throws Exception {
        threadInfo.setThreadNameProperty(Thread.currentThread().getName());
        threadInfo.setThreadStatusProperty("Processing");
        threadInfo.setThreadResultProperty("Pending");
        long startTime = System.currentTimeMillis();

        Platform.runLater(() -> updateThreadInfo.update(threadInfo, num));



        FileReader r = new FileReader(file);
        BufferedReader reader = new BufferedReader(r);

        int wordCount = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            wordCount += countOccurrences(line, wordToFind);
            Thread.sleep(250);
        }
        reader.close();

        threadInfo.setThreadStatusProperty("Completed");
        threadInfo.setThreadResultProperty("Words: " + wordCount);

        Platform.runLater(() -> updateThreadInfo.update(threadInfo, num ));
        synchronized (semaphore) {
            semaphore.release();
        }
        long endTime = System.currentTimeMillis();
        return "Processed " + file.getName() + " in " + (endTime - startTime) + "ms";
    }
    private int countOccurrences(String line, String word) {
        int count = 0;
        String regex = "\\b" + Pattern.quote(word) + "\\b";

        Matcher matcher = Pattern.compile(regex).matcher(line);
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    @FunctionalInterface
    public interface ThreadInfoUpdater {
        void update(ThreadInfoModel threadInfo, int taskNumber);
    }
}
