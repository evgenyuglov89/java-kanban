package manager;

import task.Task;

import java.util.List;

public interface TaskManager {

    List<Task> getAll();

    void deleteAll();

    Task getById(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteById(Integer id);

}
