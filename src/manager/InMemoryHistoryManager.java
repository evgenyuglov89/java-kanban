package manager;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, TaskNode<Task>> historyMap = new HashMap<>();
    private TaskNode<Task> head;
    private TaskNode<Task> tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        int taskId = task.getId();

        if (historyMap.containsKey(taskId)) {
            remove(taskId);
        }

        TaskNode<Task> newNode = linkLast(task);
        historyMap.put(taskId, newNode);
    }

    @Override
    public List<Task> getHistory() {
        ArrayList<Task> result = new ArrayList<>();
        TaskNode<Task> current = head;
        while (current != null) {
            result.add(current.data);
            current = current.next;
        }
        return result;
    }

    @Override
    public void remove(int id) {
        TaskNode<Task> node = historyMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    private TaskNode<Task> linkLast(Task task) {
        TaskNode<Task> newNode = new TaskNode<>(tail, task, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        return newNode;
    }

    private void removeNode(TaskNode<Task> node) {
        TaskNode<Task> prev = node.getPrev();
        TaskNode<Task> next = node.getNext();

        if (prev == null) {
            head = next;
        } else {
            prev.setNext(next);
        }

        if (next == null) {
            tail = prev;
        } else {
            next.setPrev(prev);
        }
    }

    private static class TaskNode<T extends Task> {
        T data;
        TaskNode<T> next;
        TaskNode<T> prev;

        TaskNode(TaskNode<T> prev, T current, TaskNode<T> next) {
            this.data = current;
            this.next = next;
            this.prev = prev;
        }

        T getData() {
            return data;
        }

        TaskNode<T> getNext() {
            return next;
        }

        TaskNode<T> getPrev() {
            return prev;
        }

        void setData(T data) {
            this.data = data;
        }

        void setNext(TaskNode<T> next) {
            this.next = next;
        }

        void setPrev(TaskNode<T> prev) {
            this.prev = prev;
        }
    }
}
