import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public void deleteAll() {
        tasks.clear();
    }

    public Task getById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
       tasks.put(task.getId(), task);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);

        if (task instanceof Subtask subtask) {
            updateEpicStatus(subtask.getEpicId());
        }
    }

    public void updateEpicStatus(int epicId) {
        Epic epic = (Epic) tasks.get(epicId);
        if (epic == null) return;

        List<Integer> subTaskIds = epic.getSubTasks();
        boolean allNew = true;
        boolean allDone = true;

        for (int subTaskId : subTaskIds) {
            Task subTask = tasks.get(subTaskId);
            if (subTask == null) continue;

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
        Task deletedTask = tasks.get(id);
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
        tasks.remove(id);
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
