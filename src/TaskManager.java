import java.util.*;

public class TaskManager {
    private static int taskId = 1;
    private HashMap<Integer, Task> tasks;

    public TaskManager() {
        this.tasks = new HashMap<>();
    }

    public static int getNewId() {
        return taskId++;
    }

    public List<Task> getAll() {
        return new ArrayList<Task>(tasks.values());
    }

    public List<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (!(task instanceof Subtask) && !(task instanceof Epic)) {
                allTasks.add(task);
            }
         }
        return allTasks;
    }

    public List<Task> getAllSubTasks() {
        ArrayList<Task> allSubTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task instanceof Subtask) {
                allSubTasks.add(task);
            }
        }
        return allSubTasks;
    }

    public List<Task> getAllEpics() {
        ArrayList<Task> allEpics = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task instanceof Epic) {
                allEpics.add(task);
            }
        }
        return allEpics;
    }

    public void deleteAll() {
        tasks.clear();
    }

    public void deleteAllTasks() {
        tasks.values().removeIf(task -> !(task instanceof Subtask) && !(task instanceof Epic));
    }

    public void deleteAllSubTasks() {
        Set<Integer> epicIds = new HashSet<>();;
        Iterator<Task> iterator = tasks.values().iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task instanceof Subtask) {
                epicIds.add(((Subtask) task).getEpicId());
                iterator.remove();
            }
        }

        for (int epicId : epicIds) {
            Epic epic = (Epic) tasks.get(epicId);
            if (epic != null) {
                epic.setSubTasks(new ArrayList<Integer>());
                updateEpicStatus(epicId);
            }
        }
    }

    public void deleteAllEpics() {
        tasks.values().removeIf(task -> task instanceof Subtask || task instanceof Epic);
    }

    public Task getById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        tasks.put(task.getId(), task);

        if (task instanceof Subtask) {
            int epicId = ((Subtask) task).getEpicId();
            Epic epic = (Epic) tasks.get(epicId);
            epic.addSubTask(task.getId());
            updateEpicStatus(epicId);
        }
    }

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

    public void deleteById(Integer id) {
        Task deletedTask = tasks.remove(id);
        if (deletedTask instanceof Subtask subtask) {
            Epic epic = (Epic) tasks.get(subtask.getEpicId());
            epic.removeSubTask(id);
            updateEpicStatus(subtask.getEpicId());
        }
        if (deletedTask instanceof Epic epic) {
            List<Integer> subtaskIds = ((Epic) deletedTask).getSubTasks();
            for (Integer subTaskId : subtaskIds) {
                tasks.remove(subTaskId);
            }
        }
    }

    public ArrayList<Task> getSubTaskByEpic(int epicId) {
        ArrayList<Task> subTaskByEpic = new ArrayList<>();
        Epic epic = (Epic) tasks.get(epicId);
        for (Integer subTaskId : epic.getSubTasks()) {
            subTaskByEpic.add(tasks.get(subTaskId));
        }

        return subTaskByEpic;
    }
}
