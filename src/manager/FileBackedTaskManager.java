package manager;

import exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final String SAVE_PATH = "tasks.csv";

    public FileBackedTaskManager() {
        super();
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FileBackedTaskManager.SAVE_PATH))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            if (!tasks.isEmpty()) {
                for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
                    writer.write(toString(entry.getValue()));
                    writer.newLine();
                }
            }
        }
        catch (Exception e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    public List<Task> getAll() {
        List<Task> allTasks = super.getAll();
        save();
        return allTasks;
    }

    public List<Task> getAllSubTasks() {
        List<Task> allSubTasks = super.getAllSubTasks();
        save();
        return allSubTasks;
    }

    public List<Task> getAllEpics() {
        List<Task> allEpics = super.getAllEpics();
        save();
        return allEpics;
    }

    public void deleteAll() {
        super.deleteAll();
        save();
    }

    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    public Task getById(int id) {
        Task task = super.getById(id);
        save();
        return task;
    }

    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    public void deleteById(Integer id) {
        super.deleteById(id);
        save();
    }

    public void setTaskId(int id) {
        super.setTaskId(id);
    }

    public ArrayList<Task> getSubTaskByEpic(int epicId) {
        ArrayList<Task> subTaskByEpic = super.getSubTaskByEpic(epicId);
        save();
        return subTaskByEpic;
    }

    public String toString(Task task) {
        String type = task.getClass().getSimpleName().toUpperCase();

        StringBuilder builder = new StringBuilder();
        builder.append(task.getId()).append(",");
        builder.append(type).append(",");
        builder.append(task.getName()).append(",");
        builder.append(task.getStatus()).append(",");
        builder.append(task.getDescription()).append(",");

        if (task instanceof Subtask subtask) {
            builder.append(subtask.getEpicId());
        }

        return builder.toString();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine(); // пропускаем заголовок

            String line;
            int id = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                Task task = Task.fromString(line);

                switch (task.getType()) {
                    case "TASK" -> manager.tasks.put(task.getId(), task);
                    case "EPIC" -> manager.tasks.put(task.getId(), (Epic) task);
                    case "SUBTASK" -> {
                        Subtask sub = (Subtask) task;
                        manager.tasks.put(task.getId(), sub);

                        Epic epic = (Epic) manager.tasks.get(sub.getEpicId());
                        if (epic != null) {
                            epic.addSubTask(sub.getId());
                        }
                    }
                }
                if (task.getId() > id) {
                    id = task.getId();
                }
            }
            manager.setTaskId(id);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла", e);
        }

        return manager;
    }

    public static void main(String[] args) {
        // Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        Task shoppingTask = new Task("Сходить в магазин", "Купить продуктов на неделю",
                InMemoryTaskManager.getNewId());
        Task cleaningTask = new Task("Убраться дома", "Протереть пыль, помыть полы, убрать вещи",
                InMemoryTaskManager.getNewId());
        Epic understandingEncapsulationInOOP = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());
        Subtask completeFirstLesson = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), understandingEncapsulationInOOP.getId());
        Subtask completeSecondLesson = new Subtask("Выполнить второй урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), understandingEncapsulationInOOP.getId());
        Subtask completeFirstLesson2 = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), understandingEncapsulationInOOP.getId());
        Epic understandingEncapsulationInOOP2 = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());


        List<Integer> subtaskIds = new ArrayList<>();
        subtaskIds.add(completeFirstLesson.getId());
        subtaskIds.add(completeSecondLesson.getId());
        subtaskIds.add(completeFirstLesson2.getId());

        understandingEncapsulationInOOP.setSubTasks(subtaskIds);

        TaskManager manager = Managers.getDefault();
        manager.createTask(shoppingTask);
        manager.createTask(cleaningTask);
        manager.createTask(understandingEncapsulationInOOP);
        manager.createTask(completeFirstLesson);
        manager.createTask(completeSecondLesson);
        manager.createTask(understandingEncapsulationInOOP2);
        manager.createTask(completeFirstLesson2);

        FileBackedTaskManager fileManager =
                FileBackedTaskManager.loadFromFile(new File(FileBackedTaskManager.SAVE_PATH));
        System.out.println(fileManager.getAll());
    }
}
