import exception.TaskScheduleConflictException;
import manager.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

class TaskManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;
    private Task task1, task2;
    private Epic epic, epic2;
    private Subtask subtask1, subtask2, subtask3, subtask4;
    private int taskId;
    private LocalDateTime startTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.of(2025, 5, 6, 10, 0);
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
        epic2 = new Epic("Epic2", "Description", InMemoryTaskManager.getNewId());
        subtask3 = new Subtask("Subtask3", "Description", InMemoryTaskManager.getNewId(),
                epic2.getId());
        subtask4 = new Subtask("Subtask4", "Description", InMemoryTaskManager.getNewId(),
                epic2.getId());
        taskManager.createTask(epic2);
        taskManager.createTask(subtask3);
        taskManager.createTask(subtask4);
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
        Epic epic = new Epic("Изучить Java", "Разобраться в основах", InMemoryTaskManager.getNewId());

        epic.addSubTask(new Subtask(epic.getName(), epic.getDescription(), epic.getId(), epic.getId()));

        boolean containsItself = epic.getSubTasks().stream()
                .anyMatch(s -> s.getId() == epic.getId());

        assertFalse(containsItself, "Epic не должен добавлять сам себя как Subtask");
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

    @Test
    void shouldSetEpicStatusToDoneWhenAllSubtasksAreDone() {
        setSubtaskStatuses(TaskStatus.DONE, TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, epic2.getStatus(),
                "Ожидался статус DONE, но был: " + epic2.getStatus());
    }

    @Test
    void shouldSetEpicStatusToInProgressWhenAllSubtasksAreInProgress() {
        setSubtaskStatuses(TaskStatus.IN_PROGRESS, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, epic2.getStatus(),
                "Ожидался статус IN_PROGRESS, но был: " + epic2.getStatus());
    }

    @Test
    void shouldSetEpicStatusToInProgressWhenSubtasksAreNewAndDone() {
        setSubtaskStatuses(TaskStatus.NEW, TaskStatus.DONE);
        assertEquals(TaskStatus.IN_PROGRESS, epic2.getStatus(),
                "Ожидался статус IN_PROGRESS, но был: " + epic2.getStatus());
    }

    private void setSubtaskStatuses(TaskStatus status1, TaskStatus status2) {
        subtask3.setStatus(status1);
        taskManager.updateTask(subtask3);

        subtask4.setStatus(status2);
        taskManager.updateTask(subtask4);
    }

    @Test
    void shouldCalculateEpicStartEndAndDurationFromSubtasks() {
        taskManager.createTask(epic);
        int epicId = epic.getId();

        Subtask subTask1 = new Subtask("Подзадача 1", "Описание 1", InMemoryTaskManager.getNewId(),
                startTime, 30, epicId);
        Subtask subTask2 = new Subtask("Подзадача 2", "Описание 2", InMemoryTaskManager.getNewId(),
                startTime.plusHours(1), 30, epicId);

        taskManager.createTask(subTask1);
        taskManager.createTask(subTask2);

        Epic updatedEpic = (Epic) taskManager.getById(epicId);
        assertNotNull(updatedEpic, "Эпик не найден");

        assertEquals(startTime, updatedEpic.getStartTime(), "Неверное стартовое время эпика");
        assertEquals(subTask2.getEndTime(), updatedEpic.getEndTime(), "Неверное конечное время эпика");

        Duration expectedDuration = subTask1.getDuration().plus(subTask2.getDuration());
        assertEquals(expectedDuration, updatedEpic.getDuration(), "Неверная длительность эпика");
    }

    @Test
    void shouldThrowExceptionIfTasksOverlapInTime() {
        Task task1 = new Task("Проверка 1", "Без пересечений", InMemoryTaskManager.getNewId(),
                startTime, 30);
        Task overlappingTask1 = new Task("Пересечение 1", "Пересекается впереди",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(15), 30);
        Task overlappingTask2 = new Task("Пересечение 2", "Пересекается сзади",
                InMemoryTaskManager.getNewId(), startTime.minusMinutes(15), 30);
        Task validTask = new Task("Без пересечений", "Перед окном",
                InMemoryTaskManager.getNewId(), startTime.minusMinutes(35), 30);

        taskManager.createTask(task1);

        assertThrows(TaskScheduleConflictException.class,
                () -> taskManager.createTask(overlappingTask1),
                "Должно быть выброшено исключение при пересечении по времени (вперёд)");

        assertThrows(TaskScheduleConflictException.class,
                () -> taskManager.createTask(overlappingTask2),
                "Должно быть выброшено исключение при пересечении по времени (назад)");

        assertDoesNotThrow(
                () -> taskManager.createTask(validTask),
                "Задача без пересечений должна быть создана без ошибок");
    }

    @Test
    void shouldSortTasksByStartTimeAndExcludeTasksWithoutTime() {
        Task noTimeTask = new Task("Без времени", "Пропустить в приоритезации",
                InMemoryTaskManager.getNewId());
        Task taskEarly = new Task("Раннее время", "Самое раннее",
                InMemoryTaskManager.getNewId(), startTime.minusHours(1), 30);
        Task taskMiddle = new Task("Среднее время", "В середине",
                InMemoryTaskManager.getNewId(), startTime, 30);
        Task taskLate = new Task("Позднее время", "Последняя",
                InMemoryTaskManager.getNewId(), startTime.plusHours(1), 30);

        taskManager.createTask(noTimeTask);
        taskManager.createTask(taskMiddle);
        taskManager.createTask(taskLate);
        taskManager.createTask(taskEarly);

        List<Task> prioritized = taskManager.getSortedTasksByTime();

        assertFalse(prioritized.contains(noTimeTask),
                "Задача без времени не должна попадать в приоритезированные");

        assertEquals(taskEarly, prioritized.get(0), "Первая задача должна быть с самым ранним временем");
        assertEquals(taskLate, prioritized.get(2), "Последняя задача должна быть с самым поздним временем");
    }
}