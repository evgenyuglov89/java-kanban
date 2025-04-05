package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static int taskId = 1;
    private HashMap<Integer, Task> tasks;
    private HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    public static int getNewId() {
        return InMemoryTaskManager.taskId++;
    }

    @Override
    public List<Task> getAll() {
        return new ArrayList<Task>(tasks.values());
    }

    @Override
    public List<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (!(task instanceof Subtask) && !(task instanceof Epic)) {
                allTasks.add(task);
            }
         }
        return allTasks;
    }

    @Override
    public List<Task> getAllSubTasks() {
        ArrayList<Task> allSubTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task instanceof Subtask) {
                allSubTasks.add(task);
            }
        }
        return allSubTasks;
    }

    @Override
    public List<Task> getAllEpics() {
        ArrayList<Task> allEpics = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task instanceof Epic) {
                allEpics.add(task);
            }
        }
        return allEpics;
    }

    @Override
    public void deleteAll() {
        tasks.clear();
    }

    @Override
    public void deleteAllTasks() {
        Iterator<Map.Entry<Integer, Task>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Task> entry = iterator.next();
            Task task = entry.getValue();

            if (!(task instanceof Subtask) && !(task instanceof Epic)) {
                historyManager.remove(task.getId()); // Удаляем из истории
                iterator.remove();
            }
        }
    }

    @Override
    public void deleteAllSubTasks() {
        Set<Integer> epicIds = new HashSet<>();
        Iterator<Map.Entry<Integer, Task>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Task> entry = iterator.next();
            Task task = entry.getValue();
            if (task instanceof Subtask) {
                epicIds.add(((Subtask) task).getEpicId());
                historyManager.remove(task.getId()); // Удаляем из истории
                iterator.remove();
            }
        }

        for (int epicId : epicIds) {
            Epic epic = (Epic) tasks.get(epicId);
            if (epic != null) {
                epic.setSubTasks(new ArrayList<>());
                updateEpicStatus(epicId);
            }
        }
    }

    @Override
    public void deleteAllEpics() {
        Iterator<Map.Entry<Integer, Task>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Task> entry = iterator.next();
            Task task = entry.getValue();
            if (task instanceof Epic || task instanceof Subtask) {
                historyManager.remove(task.getId()); // Удаляем из истории
                iterator.remove();
            }
        }
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
    public void createTask(Task task) {
        tasks.put(task.getId(), task);

        if (task instanceof Subtask) {
            int epicId = ((Subtask) task).getEpicId();
            Epic epic = (Epic) tasks.get(epicId);
            epic.addSubTask(task.getId());
            updateEpicStatus(epicId);
        }
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);

        if (task instanceof Subtask subtask) {
            updateEpicStatus(subtask.getEpicId());
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        if (epic == null) return;

        List<Integer> subTaskIds = epic.getSubTasks();
        boolean allNew = true;
        boolean allDone = true;

        for (int subTaskId : subTaskIds) {
            Task subTask = tasks.get(subTaskId);
            if (subTask == null) continue;

            if (subTask.getStatus() == TaskStatus.IN_PROGRESS) {
                allDone = false;
                allNew = false;
                break;
            }
            if (subTask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subTask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        TaskStatus status;
        if (allNew) {
            status = TaskStatus.NEW;
        } else if (allDone) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
        Epic newEpic = new Epic(epic.getName(), epic.getDescription(), epic.getId(), epic.getSubTasks(), status);
        tasks.put(newEpic.getId(), newEpic);
    }

    @Override
    public void deleteById(Integer id) {
        Task deletedTask = tasks.remove(id);
        historyManager.remove(id);
        if (deletedTask instanceof Subtask subtask) {
            Epic epic = (Epic) tasks.get(subtask.getEpicId());
            epic.removeSubTask(id);
            updateEpicStatus(subtask.getEpicId());
        }
        if (deletedTask instanceof Epic epic) {
            List<Integer> subtaskIds = ((Epic) deletedTask).getSubTasks();
            for (Integer subTaskId : subtaskIds) {
                tasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
        }
    }

    @Override
    public ArrayList<Task> getSubTaskByEpic(int epicId) {
        ArrayList<Task> subTaskByEpic = new ArrayList<>();
        Epic epic = (Epic) tasks.get(epicId);
        for (Integer subTaskId : epic.getSubTasks()) {
            subTaskByEpic.add(tasks.get(subTaskId));
        }

        return subTaskByEpic;
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }
}
