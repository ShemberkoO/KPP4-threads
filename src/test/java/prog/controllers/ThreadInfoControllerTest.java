package prog.controllers;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ThreadInfoControllerTest {

    private ThreadInfoController controller;
    private TableView<String[]> tableViewMock;
    private Stage stageMock;
    private Semaphore semaphoreMock;

    @BeforeEach
    void setUp() {
        controller = new ThreadInfoController();
        tableViewMock = mock(TableView.class);
        stageMock = mock(Stage.class);
        semaphoreMock = mock(Semaphore.class);
    }
}