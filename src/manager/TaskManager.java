package manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    List<Task> getAll();

    void deleteAll();

    Task getById(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteById(Integer id);

    HistoryManager getHistoryManager();

    ArrayList<Task> getSubTaskByEpic(int epicId);

    void deleteAllEpics();

    void deleteAllSubTasks();

    void deleteAllTasks();

    List<Task> getAllEpics();

    List<Task> getAllSubTasks();

    List<Task> getAllTasks();
}
