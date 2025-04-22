import manager.FileBackedTaskManager;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager taskManager;
    private Task task1;
    private Epic epic2;
    private Subtask subtask3;

    @BeforeEach
    public void setup() {
        taskManager = new FileBackedTaskManager("tasks.csv");

        task1 = createTask("Task1", "1");
        epic2 = new Epic("Epic1", "1", InMemoryTaskManager.getNewId());
        subtask3 = createSubtask("Subtask3", "3", epic2.getId());
    }

    private Task createTask(String title, String description) {
        return new Task(title, description, InMemoryTaskManager.getNewId());
    }

    private Subtask createSubtask(String title, String description, int epicId) {
        return new Subtask(title, description, InMemoryTaskManager.getNewId(), epicId);
    }

    @Test
    public void save_EmptyTaskManagerTest() throws IOException {
        taskManager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(new File(taskManager.getSavePath()));

        assertTrue(loaded.getAll().isEmpty());
    }

    @Test
    public void saveAndLoad_CasualTaskManagerTest() {
        taskManager.createTask(task1);
        taskManager.createTask(epic2);
        taskManager.createTask(subtask3);

        FileBackedTaskManager loadedManager =
                FileBackedTaskManager.loadFromFile(new File(taskManager.getSavePath()));

        assertEquals(task1, loadedManager.getById(task1.getId()));
        assertEquals(epic2, loadedManager.getById(epic2.getId()));
        assertEquals(subtask3, loadedManager.getById(subtask3.getId()));
    }
}
