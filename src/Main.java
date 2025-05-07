import manager.*;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        LocalDateTime baseTime = LocalDateTime.of(2025, 5, 6, 10, 0);

        // Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        Task shoppingTask = new Task("Сходить в магазин", "Купить продуктов на неделю",
                InMemoryTaskManager.getNewId(), baseTime, 10);
        Task cleaningTask = new Task("Убраться дома", "Протереть пыль, помыть полы, убрать вещи",
                InMemoryTaskManager.getNewId(), baseTime.plusHours(1), 20);
        Epic understandingEncapsulationInOOP = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());
        Subtask completeFirstLesson = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), baseTime.plusHours(3), 60,
                understandingEncapsulationInOOP.getId());
        Subtask completeSecondLesson = new Subtask("Выполнить второй урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), baseTime.plusHours(4), 40,
                understandingEncapsulationInOOP.getId());
        Subtask completeFirstLesson2 = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                InMemoryTaskManager.getNewId(), baseTime.plusHours(5), 80,
                understandingEncapsulationInOOP.getId());
        Epic understandingEncapsulationInOOP2 = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", InMemoryTaskManager.getNewId());


        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(completeFirstLesson);
        subtasks.add(completeSecondLesson);
        subtasks.add(completeFirstLesson2);

        understandingEncapsulationInOOP.setSubTasks(subtasks);

        TaskManager manager = Managers.getDefault();
        HistoryManager historyManager = manager.getHistoryManager();

        manager.createTask(shoppingTask);
        manager.createTask(cleaningTask);
        manager.createTask(understandingEncapsulationInOOP);
        manager.createTask(completeFirstLesson);
        manager.createTask(completeSecondLesson);
        manager.createTask(understandingEncapsulationInOOP2);
        manager.createTask(completeFirstLesson2);

        // Запросите созданные задачи несколько раз в разном порядке.
        // После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
        manager.getById(1);
        printHistory(historyManager.getHistory());
        manager.getById(2);
        printHistory(historyManager.getHistory());
        manager.getById(3);
        printHistory(historyManager.getHistory());
        manager.getById(4);
        printHistory(historyManager.getHistory());
        manager.getById(1);
        printHistory(historyManager.getHistory());
        manager.getById(2);
        printHistory(historyManager.getHistory());
        manager.getById(3);
        printHistory(historyManager.getHistory());
        manager.getById(4);
        printHistory(historyManager.getHistory());
        manager.getById(3);
        printHistory(historyManager.getHistory());
        manager.getById(2);
        printHistory(historyManager.getHistory());
        manager.getById(4);
        printHistory(historyManager.getHistory());
        manager.getById(5);
        printHistory(historyManager.getHistory());
        manager.getById(6);
        printHistory(historyManager.getHistory());
        manager.getById(7);
        printHistory(historyManager.getHistory());

        // Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        manager.deleteById(2);
        printHistory(historyManager.getHistory());
    }

    static void printHistory(List<Task> history) {
        for (Task task : history) {
            System.out.println(task); // каждый task на новой строке
        }
        System.out.println(); // пустая строка после каждого вызова
    }
}
