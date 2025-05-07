import manager.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private TaskManager taskManager;

    private Task task1, task2, task3, task4, task5, task6, task7, task8;
    private Epic epic1, epic2;
    private Subtask subtask1, subtask2, subtask3, subtask4;
    private LocalDateTime startTime;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        historyManager = taskManager.getHistoryManager();
        initTestData();
    }

    private void initTestData() {
        startTime = LocalDateTime.of(2025, 5, 6, 10, 0);
        task1 = createTask("Task1", "1", startTime, 20);
        task2 = createTask("Task2", "2", startTime.plusMinutes(30), 20);
        task3 = createTask("Task3", "3", startTime.plusMinutes(60), 20);
        task4 = createTask("Task4", "4", startTime.plusMinutes(90), 20);
        task5 = createTask("Task5", "5", startTime.plusMinutes(120), 20);
        task6 = createTask("Task6", "6", startTime.plusMinutes(150), 20);
        task7 = createTask("Task7", "7", startTime.plusMinutes(180), 20);
        task8 = createTask("Task8", "8", startTime.plusMinutes(210), 20);

        epic1 = new Epic("Epic1", "1", InMemoryTaskManager.getNewId());
        epic2 = new Epic("Epic2", "2", InMemoryTaskManager.getNewId());

        subtask1 = createSubtask("Subtask1", "1", epic1.getId(), startTime.plusMinutes(240), 20);
        subtask2 = createSubtask("Subtask2", "2", epic1.getId(), startTime.plusMinutes(270), 20);
        subtask3 = createSubtask("Subtask3", "3", epic2.getId(), startTime.plusMinutes(300), 20);
        subtask4 = createSubtask("Subtask4", "4", epic2.getId(), startTime.plusMinutes(330), 20);

        List<Task> allTasks = List.of(task1, task2, task3, task4, task5, task6, task7, task8);
        allTasks.forEach(taskManager::createTask);

        taskManager.createTask(epic1);
        taskManager.createTask(epic2);

        List<Subtask> allSubtasks = List.of(subtask1, subtask2, subtask3, subtask4);
        allSubtasks.forEach(taskManager::createTask);

        epic1.setSubTasks(List.of(subtask1, subtask2));
        epic2.setSubTasks(List.of(subtask3, subtask4));
    }

    private Task createTask(String title, String description, LocalDateTime startTime, int duration) {
        return new Task(title, description, InMemoryTaskManager.getNewId(), startTime, duration);
    }

    private Subtask createSubtask(String title, String description, int epicId, LocalDateTime startTime, int duration) {
        return new Subtask(title, description, InMemoryTaskManager.getNewId(), startTime, duration, epicId);
    }

    @Test
    void getHistory_WhenEmpty_ShouldReturnEmptyList() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой при инициализации");
    }

    @Test
    void getHistory_WhenTasksViewed_ShouldReturnCorrectOrder() {
        taskManager.getById(task2.getId());
        taskManager.getById(task5.getId());
        taskManager.getById(subtask4.getId());
        taskManager.getById(subtask1.getId());
        taskManager.getById(epic1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task2, task5, subtask4, subtask1, epic1), history,
                "История должна содержать задачи в правильном порядке");
    }

    @Test
    void getHistory_WhenMoreThan10TasksViewed_ShouldPreserveLastViewOrder() {
        List<Task> tasksToAccess = List.of(
                task2, task5, subtask4, subtask1, epic1,
                task1, task4, task3, task8, subtask2, epic2
        );

        tasksToAccess.forEach(task -> taskManager.getById(task.getId()));

        List<Task> history = historyManager.getHistory();

        List<Task> expectedHistory = List.of(
                task2, task5, subtask4, subtask1, epic1,
                task1, task4, task3, task8, subtask2, epic2
        );

        assertEquals(expectedHistory, history,
                "История должна содержать последние уникальные задачи в порядке просмотра");
        assertEquals(11, history.size(), "История должна содержать 11 задач");
    }

    @Test
    void getHistory_ShouldMoveRevisitedTaskToEnd() {
        taskManager.getById(task1.getId());
        taskManager.getById(task2.getId());
        taskManager.getById(task1.getId());

        List<Task> history = historyManager.getHistory();
        List<Task> expected = List.of(task2, task1);

        assertEquals(expected, history);
    }
}
