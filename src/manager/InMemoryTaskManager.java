package manager;

import exception.TaskScheduleConflictException;
import task.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected static int taskId = 1;
    protected HashMap<Integer, Task> tasks;
    private final HistoryManager historyManager;

    private final Set<Task> tasksSortedByStartTime = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    public static final LocalDateTime UNDEFINED_TIME =
            LocalDateTime.of(1, 1, 1, 0, 0);
    private static final String OVERLAP_WARNING_MESSAGE =
            "Задача пересекается по времени с другой задачей в расписании";

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    public static int getNewId() {
        return InMemoryTaskManager.taskId++;
    }

    @Override
    public List<Task> getAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream()
                .filter(task -> task.getType() == TaskType.TASK)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getAllSubTasks() {
        return tasks.values().stream()
                .filter(task -> task.getType() == TaskType.SUBTASK)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getAllEpics() {
        return tasks.values().stream()
                .filter(task -> task.getType() == TaskType.EPIC)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        tasks.clear();
    }

    @Override
    public void deleteAllTasks() {
        tasks.entrySet().removeIf(entry -> {
            Task task = entry.getValue();
            if (task.getType() == TaskType.TASK) {
                historyManager.remove(task.getId());
                return true;
            }
            return false;
        });
    }

    @Override
    public void deleteAllSubTasks() {
        Set<Integer> epicIds = tasks.values().stream()
                .filter(task -> task.getType() == TaskType.SUBTASK)
                .map(task -> {
                    historyManager.remove(task.getId());
                    return ((Subtask) task).getEpicId();
                })
                .collect(Collectors.toSet());

        tasks.entrySet().removeIf(entry -> entry.getValue().getType() == TaskType.SUBTASK);

        epicIds.forEach(epicId -> {
            Epic epic = (Epic) tasks.get(epicId);
            if (epic != null) {
                epic.setSubTasks(new ArrayList<>());
                updateEpicStatus(epicId);
            }
        });
    }

    @Override
    public void deleteAllEpics() {
        tasks.entrySet().removeIf(entry -> {
            Task task = entry.getValue();
            TaskType type = task.getType();
            if (type == TaskType.EPIC || type == TaskType.SUBTASK) {
                historyManager.remove(task.getId());
                return true;
            }
            return false;
        });
    }

    @Override
    public Task getById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public void createTask(Task task) throws TaskScheduleConflictException {
        TaskType type = task.getType();

        if (type != TaskType.EPIC && !isTimeSlotAvailable(task)) {
            throw new TaskScheduleConflictException(OVERLAP_WARNING_MESSAGE);
        }

        tasks.put(task.getId(), task);

        if (type == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            Epic epic = (Epic) tasks.get(subtask.getEpicId());
            epic.modifySubTask(subtask);
            updateEpicStatus(epic.getId());
        }

        if (type != TaskType.EPIC) {
            addTaskIfHasTime(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        TaskType type = task.getType();

        if (type != TaskType.EPIC && !isTimeSlotAvailable(task)) {
            throw new TaskScheduleConflictException(OVERLAP_WARNING_MESSAGE);
        }

        tasks.put(task.getId(), task);
        if (type == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            Epic epic = (Epic) tasks.get(subtask.getEpicId());
            epic.modifySubTask(subtask);
            updateEpicStatus(epic.getId());
        }

        if (type != TaskType.EPIC) {
            removeTaskIfPresent(task);
            addTaskIfHasTime(task);
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        if (epic == null) return;

        List<Subtask> subTasks = epic.getSubTasks().stream()
                .filter(Objects::nonNull)
                .toList();

        if (subTasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = subTasks.stream().allMatch(st -> st.getStatus() == TaskStatus.NEW);
        boolean allDone = subTasks.stream().allMatch(st -> st.getStatus() == TaskStatus.DONE);

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

    }

    @Override
    public void deleteById(Integer id) {
        Task deletedTask = tasks.remove(id);
        if (deletedTask == null) return;

        historyManager.remove(id);
        TaskType type = deletedTask.getType();

        if (type == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) deletedTask;
            Epic epic = (Epic) tasks.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubTask(id);
                updateEpicStatus(subtask.getEpicId());
            }
        }

        if (type == TaskType.EPIC) {
            Epic epic = (Epic) deletedTask;
            epic.getSubTasks().stream()
                    .map(Subtask::getId)
                    .forEach(subtaskId -> {
                        tasks.remove(subtaskId);
                        historyManager.remove(subtaskId);
                    });
        }
    }

    @Override
    public ArrayList<Task> getSubTaskByEpic(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        if (epic == null) return new ArrayList<>();

        return epic.getSubTasks().stream()
                .map(sub -> tasks.get(sub.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public void setTaskId(int id) {
        taskId = id;
    }

    private boolean isTimeSlotAvailable(Task task) {
        LocalDateTime newStart = task.getStartTime();
        LocalDateTime newEnd = task.getEndTime();

        if (newStart.isEqual(UNDEFINED_TIME)) {
            return true;
        }

        return tasksSortedByStartTime.stream()
                .filter(existing -> !existing.getStartTime().isEqual(UNDEFINED_TIME))
                .filter(existing -> existing.getId() != task.getId())
                .noneMatch(existing -> {
                    LocalDateTime existStart = existing.getStartTime();
                    LocalDateTime existEnd = existing.getEndTime();
                    return newStart.isBefore(existEnd) && newEnd.isAfter(existStart);
                });
    }

    public List<Task> getSortedTasksByTime() {
        return List.copyOf(tasksSortedByStartTime);
    }

    protected void addTaskIfHasTime(Task task) {
        if (!task.getStartTime().isEqual(UNDEFINED_TIME)) {
            tasksSortedByStartTime.add(task);
        }
    }

    private void removeTaskIfPresent(Task task) {
        tasksSortedByStartTime.removeIf(existingTask -> existingTask.getId() == task.getId());
    }
}
