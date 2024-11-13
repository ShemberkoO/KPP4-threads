package prog;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class FileProcessorTaskTest {

    private File testFile;
    private String[] threadInfo;
    private Semaphore semaphore;

    @Mock
    private FileProcessorTask.ThreadInfoUpdater threadInfoUpdaterMock;
    private FileProcessorTask fileProcessorTask;

    @BeforeAll
    public static void setupBeforeAllTests() {
        Platform.startup(() -> {});
        // Створюємо тимчасовий файл з деяким вмістом для тестування
    }

    @BeforeEach
    public void setup() throws Exception {

        testFile = File.createTempFile("testFile", ".txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Hello world\n");
            writer.write("world Hello\n");
            writer.write("Hello again\n");
        }

        threadInfo = new String[3];
        semaphore = new Semaphore(1);

        threadInfoUpdaterMock = mock(FileProcessorTask.ThreadInfoUpdater.class);

        // Створюємо об'єкт FileProcessorTask з мок-методом
        fileProcessorTask = new FileProcessorTask(
                testFile, "Hello", threadInfo, semaphore, threadInfoUpdaterMock, 1);
    }

    @Test
    public void testCallProcessesFileCorrectly() throws Exception {
        semaphore.acquire();
        String result = fileProcessorTask.call();

        assertEquals("Processed " + testFile.getName() + " in ",
                result.substring(0,testFile.getName().length() + 4 + 10));

        // Перевірка контенту threadInfo після обробки
        assertEquals("Completed", threadInfo[1]);
        assertEquals("Words: 3", threadInfo[2]);


        verify(threadInfoUpdaterMock, times(2)).update(any(String[].class), eq(1));
    }

   @Test
    public void testCallUpdatesThreadInfoOnStartAndCompletion() throws Exception {
        semaphore.acquire();

        fileProcessorTask.call();
        verify(threadInfoUpdaterMock, times(2)).update(any(), eq(1));

    }
}
