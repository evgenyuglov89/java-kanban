import manager.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;
    private int taskId;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        historyManager = taskManager.getHistoryManager();
        taskId = InMemoryTaskManager.getNewId();
        task1 = new Task("Task 1", "Description 1", taskId);
        task2 = new Task("Task 2", "Description 2", taskId);
        taskId = InMemoryTaskManager.getNewId();
        epic = new Epic("Epic", "Description", taskId);
        taskId = InMemoryTaskManager.getNewId();
        subtask1 = new Subtask("Subtask1", "Description", taskId, epic.getId());
        subtask2 = new Subtask("Subtask2", "Description", taskId, epic.getId());
    }

    @Test
    void shouldBeEqualIfTasksHaveSameId() {
        assertEquals(task1, task2);
    }

    @Test
    void shouldBeEqualIfSubtasksHaveSameId() {
        assertEquals(subtask1, subtask2);
    }

    @Test
    void shouldNotAllowEpicToAddItselfAsSubtask() {
        epic.addSubTask(epic.getId());
        assertFalse(epic.getSubTasks().contains(epic.getId()));
    }

    @Test
    void shouldNotAllowSubtaskToBeItsOwnEpic() {
        subtask1.setEpicId(subtask1.getId());
        assertNotEquals(subtask1.getEpicId(), subtask1.getId());
    }

    @Test
    void utilityClassShouldReturnInitializedManagers() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
    }

    @Test
    void inMemoryTaskManagerShouldAddAndFindTasksById() {
        taskManager.createTask(task1);
        assertEquals(task1, taskManager.getById(task1.getId()));
    }

    @Test
    void tasksWithGivenAndGeneratedIdShouldNotConflict() {
        Task generatedTask = new Task("Task 3", "Description 3", 10);
        taskManager.createTask(task1);
        taskManager.createTask(generatedTask);
        assertNotEquals(task1.getId(), generatedTask.getId());
    }

    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() {
        taskManager.createTask(task1);
        Task newTask = taskManager.getById(task1.getId());

        assertEquals(newTask.getName(), task1.getName(), "Имена задач должны совпадать");
        assertEquals(newTask.getDescription(), task1.getDescription(), "Описания задач должны совпадать");
        assertEquals(newTask.getId(), task1.getId(), "ID задач должны совпадать");
        assertEquals(newTask.getStatus(), task1.getStatus(), "Статусы задач должны совпадать");
    }
}