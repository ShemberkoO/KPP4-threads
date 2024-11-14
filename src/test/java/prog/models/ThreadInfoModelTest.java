package prog.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadInfoModelTest {
    private ThreadInfoModel threadInfoModel;

    @BeforeEach
    public void setUp() {
        threadInfoModel = new ThreadInfoModel("Thread-1", "Running", "Success", "Output Text");
    }

    @Test
    public void testInitialValues() {
        assertEquals("Thread-1", threadInfoModel.threadNameProperty().get(), "Thread name should match");
        assertEquals("Running", threadInfoModel.threadStatusProperty().get(), "Status should match");
        assertEquals("Success", threadInfoModel.threadResultProperty().get(), "Result should match");
        assertEquals("Output Text", threadInfoModel.threadOutputProperty().get(), "Output should match");
    }

    @Test
    public void testSetThreadNameProperty() {
        threadInfoModel.setThreadNameProperty("NewThread-2");
        assertEquals("NewThread-2", threadInfoModel.threadNameProperty().get(), "Thread name should be updated");
    }

    @Test
    public void testSetThreadStatusProperty() {
        threadInfoModel.setThreadStatusProperty("Stopped");
        assertEquals("Stopped", threadInfoModel.threadStatusProperty().get(), "Status should be updated");
    }

    @Test
    public void testSetThreadResultProperty() {
        threadInfoModel.setThreadResultProperty("Failed");
        assertEquals("Failed", threadInfoModel.threadResultProperty().get(), "Result should be updated");
    }

    @Test
    public void testSetThreadOutputProperty() {
        threadInfoModel.setThreadOutputProperty("New Output Text");
        assertEquals("New Output Text", threadInfoModel.threadOutputProperty().get(), "Output should be updated");
    }
}
