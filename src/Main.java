import manager.HistoryManager;
import manager.InMemoryTaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        
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
        Epic understandingEncapsulationInOOP2 = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());
        Subtask completeFirstLesson2 = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), understandingEncapsulationInOOP2.getId());
        Subtask completeSecondLesson2 = new Subtask("Выполнить второй урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), understandingEncapsulationInOOP2.getId());

        List<Integer> subtaskIds = new ArrayList<>();
        subtaskIds.add(completeFirstLesson.getId());
        subtaskIds.add(completeSecondLesson.getId());

        understandingEncapsulationInOOP.setSubTasks(subtaskIds);

        List<Integer> subtaskIds2 = new ArrayList<>();
        subtaskIds2.add(completeFirstLesson2.getId());
        subtaskIds2.add(completeSecondLesson2.getId());

        understandingEncapsulationInOOP2.setSubTasks(subtaskIds2);

        System.out.println(understandingEncapsulationInOOP);
        System.out.println(completeFirstLesson);
        System.out.println(completeSecondLesson);
        System.out.println(understandingEncapsulationInOOP2);
        System.out.println(completeFirstLesson2);
        System.out.println(completeSecondLesson2);
        System.out.println(shoppingTask);
        System.out.println(cleaningTask);

        shoppingTask = new Task(shoppingTask.getName(), shoppingTask.getDescription(),
                shoppingTask.getId(), TaskStatus.IN_PROGRESS);

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createTask(shoppingTask);
        manager.createTask(cleaningTask);
        manager.createTask(understandingEncapsulationInOOP);
        manager.createTask(completeFirstLesson);
        manager.createTask(completeSecondLesson);
        manager.createTask(understandingEncapsulationInOOP2);
        manager.createTask(completeFirstLesson2);
        manager.createTask(completeSecondLesson2);

        manager.updateTask(new Subtask(completeFirstLesson.getName(),
                completeFirstLesson.getDescription(), completeFirstLesson.getId(),
                completeFirstLesson.getEpicId(), TaskStatus.DONE));
        manager.updateTask(new Subtask(completeSecondLesson.getName(),
                completeSecondLesson.getDescription(), completeSecondLesson.getId(),
                completeSecondLesson.getEpicId(), TaskStatus.DONE));

//        manager.deleteById(4);
//        System.out.println(manager.getSubTaskByEpic(3));
        System.out.println(manager.getAll());
       /*
       manager.createTask(new task.Subtask("Выполнить третий урок",
                "Прочитать теорию и выполнить практику",
                manager.TaskManager.getNewId(), understandingEncapsulationInOOP.getId()));
        */
//        manager.deleteAllTasks();
//        System.out.println(manager.getAll());
//        manager.deleteAllSubTasks();
//        System.out.println(manager.getAll());
//        manager.deleteAllEpics();
//        System.out.println(manager.getAll());
//        System.out.println(manager.getAllEpics());
//        System.out.println(manager.getAllTasks());
//        System.out.println(manager.getAllSubTasks());

        manager.getById(1);
        manager.getById(2);
        manager.getById(3);
        manager.getById(4);
        manager.getById(1);
        manager.getById(2);
        manager.getById(3);
        manager.getById(4);
        manager.getById(3);
        manager.getById(2);
        manager.getById(4);
        manager.getById(5);
        HistoryManager historyManager = manager.getHistoryManager();
        System.out.println(historyManager.getHistory());
    }
}
