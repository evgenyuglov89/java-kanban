import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        
        Task shoppingTask = new Task("Сходить в магазин", "Купить продуктов на неделю",
                TaskManager.getNewId());
        Task cleaningTask = new Task("Убраться дома", "Протереть пыль, помыть полы, убрать вещи",
                TaskManager.getNewId());
        Epic understandingEncapsulationInOOP = new Epic("Пройти тему ООП. Инкапсуляция",
                "Пройти все уроки и выполнить все упражнения", TaskManager.getNewId());
        Subtask completeFirstLesson = new Subtask("Выполнить первый урок",
                "Прочитать теорию и выполнить практику",
                TaskManager.getNewId(), understandingEncapsulationInOOP.getId());
        Subtask completeSecondLesson = new Subtask("Выполнить второй урок",
                "Прочитать теорию и выполнить практику",
                TaskManager.getNewId(), understandingEncapsulationInOOP.getId());

        List<Integer> subtaskIds = new ArrayList<>();
        subtaskIds.add(completeFirstLesson.getId());
        subtaskIds.add(completeSecondLesson.getId());

        understandingEncapsulationInOOP.setSubTasks(subtaskIds);

        System.out.println(understandingEncapsulationInOOP);
        System.out.println(completeFirstLesson);
        System.out.println(completeSecondLesson);
        System.out.println(shoppingTask);
        System.out.println(cleaningTask);

        shoppingTask = new Task(shoppingTask.getName(), shoppingTask.getDescription(),
                shoppingTask.getId(), TaskStatus.IN_PROGRESS);

        TaskManager manager = new TaskManager();
        manager.createTask(shoppingTask);
        manager.createTask(cleaningTask);
        manager.createTask(understandingEncapsulationInOOP);
        manager.createTask(completeFirstLesson);
        manager.createTask(completeSecondLesson);

        manager.updateTask(new Subtask(completeFirstLesson.getName(),
                completeFirstLesson.getDescription(), completeFirstLesson.getId(),
                completeFirstLesson.getEpicId(), TaskStatus.DONE));
        manager.updateTask(new Subtask(completeSecondLesson.getName(),
                completeSecondLesson.getDescription(), completeSecondLesson.getId(),
                completeSecondLesson.getEpicId(), TaskStatus.DONE));

//        manager.deleteById(4);
//        System.out.println(manager.getSubTaskByEpic(3));
        System.out.println(manager.getAll());
        manager.createTask(new Subtask("Выполнить третий урок",
                "Прочитать теорию и выполнить практику",
                TaskManager.getNewId(), understandingEncapsulationInOOP.getId()));
//        manager.deleteAllTasks();
        System.out.println(manager.getAll());
//        manager.deleteAllSubTasks();
//        System.out.println(manager.getAll());
//        manager.deleteAllEpics();
//        System.out.println(manager.getAll());
//        System.out.println(manager.getAllEpics());
//        System.out.println(manager.getAllTasks());
//        System.out.println(manager.getAllSubTasks());
    }
}
