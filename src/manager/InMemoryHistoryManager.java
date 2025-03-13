package manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> history;

    public InMemoryHistoryManager() {
        this.history = new ArrayList<>(10);
    }

    @Override
    public void add(Task task) {
        if (history.size() == 10) {
            history.remove(0); // Удаляем самый старый просмотр
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
