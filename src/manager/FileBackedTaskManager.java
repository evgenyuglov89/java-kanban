package manager;

import exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskType;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String savePath;

    public FileBackedTaskManager(String savePath) {
        super();
        this.savePath = savePath;
    }

    public String getSavePath() {
        return savePath;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(savePath))) {
            writer.write("id,type,name,status,description,epic,start_time,duration");
            writer.newLine();

            if (!tasks.isEmpty()) {
                for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
                    writer.write(toString(entry.getValue()));
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Task getById(int id) {
        Task task = super.getById(id);
        save();
        return task;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteById(Integer id) {
        super.deleteById(id);
        save();
    }

    @Override
    public void setTaskId(int id) {
        super.setTaskId(id);
    }

    @Override
    public ArrayList<Task> getSubTaskByEpic(int epicId) {
        ArrayList<Task> subTaskByEpic = super.getSubTaskByEpic(epicId);
        save();
        return subTaskByEpic;
    }

    public String toString(Task task) {
        String type = task.getType().name();

        StringBuilder builder = new StringBuilder();
        builder.append(task.getId()).append(",");
        builder.append(type).append(",");
        builder.append(task.getName()).append(",");
        builder.append(task.getStatus()).append(",");
        builder.append(task.getDescription()).append(",");

        builder.append(task.getType() == TaskType.SUBTASK ? ((Subtask) task).getEpicId() : "").append(",");

        LocalDateTime startTime = task.getStartTime();
        Duration duration = task.getDuration();

        builder.append(startTime != null ? startTime : "").append(",");
        builder.append(duration != null ? duration.toMinutes() : "");

        return builder.toString();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager("tasks.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();

            String line;
            int id = 0;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                Task task = Task.fromString(line);

                switch (task.getType()) {
                    case TASK -> manager.tasks.put(task.getId(), task);
                    case EPIC -> manager.tasks.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        Subtask sub = (Subtask) task;
                        manager.tasks.put(task.getId(), sub);

                        Epic epic = (Epic) manager.tasks.get(sub.getEpicId());
                        if (epic != null) {
                            epic.addSubTask(sub);
                        }
                    }
                }
                if (task.getId() > id) {
                    id = task.getId();
                }
            }
            manager.setTaskId(id);

        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        return manager;
    }

    public static void main(String[] args) {
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 6, 10, 0);
        // Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        Task shoppingTask = new Task("Сходить в магазин", "Купить продуктов на неделю",
                InMemoryTaskManager.getNewId(), startTime, 20);
        Task cleaningTask = new Task("Убраться дома", "Протереть пыль",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(40), 20);
        Epic understandingEncapsulationInOOP = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());
        Subtask completeFirstLesson = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(65), 30,
                understandingEncapsulationInOOP.getId());
        Subtask completeSecondLesson = new Subtask("Выполнить второй урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(100), 30,
                understandingEncapsulationInOOP.getId());
        Subtask completeFirstLesson2 = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), startTime.plusMinutes(140), 30,
                understandingEncapsulationInOOP.getId());
        Epic understandingEncapsulationInOOP2 = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());


        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(completeFirstLesson);
        subtasks.add(completeSecondLesson);
        subtasks.add(completeFirstLesson2);

        understandingEncapsulationInOOP.setSubTasks(subtasks);

        TaskManager manager = Managers.getDefault();
        manager.createTask(shoppingTask);
        manager.createTask(cleaningTask);
        manager.createTask(understandingEncapsulationInOOP);
        manager.createTask(completeFirstLesson);
        manager.createTask(completeSecondLesson);
        manager.createTask(understandingEncapsulationInOOP2);
        manager.createTask(completeFirstLesson2);

        FileBackedTaskManager fileBacked = (FileBackedTaskManager) manager;
        String savePath = fileBacked.getSavePath();

        FileBackedTaskManager fileManager =
                FileBackedTaskManager.loadFromFile(new File(savePath));
        System.out.println(fileManager.getAll());
    }
}
