package prog;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessorTask implements Callable<String> {
    private final File file;
    private final String[] threadInfo;
    private final Semaphore semaphore;
    private final ThreadInfoUpdater updateThreadInfo;
    private final String wordToFind;
    private final int num;

    public FileProcessorTask(File file,String wordToFind, String[] threadInfo, Semaphore semaphore, ThreadInfoUpdater updateThreadInfo, int num) {
        this.file = file;
        this.wordToFind = wordToFind;
        this.num = num;
        this.threadInfo = threadInfo;
        this.semaphore = semaphore;
        this.updateThreadInfo = updateThreadInfo;
    }

    @Override
    public String call() throws Exception {
        threadInfo[0] = Thread.currentThread().getName();
        threadInfo[1] = "Processing";
        threadInfo[2] = "Pending";
        long startTime = System.currentTimeMillis();

        Platform.runLater(() -> updateThreadInfo.update(threadInfo, num));

        Thread.sleep(10000);

        FileReader r = new FileReader(file);
        BufferedReader reader = new BufferedReader(r);

        int wordCount = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            wordCount += countOccurrences(line, wordToFind);
        }
        reader.close();

        threadInfo[1] = "Completed";
        threadInfo[2] = "Words: " + wordCount;

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
        void update(String[] threadInfo, int taskNumber);
    }
}
