package task;

public class TaskNode<T extends Task> {
    public T data;
    public TaskNode<T> next;
    public TaskNode<T> prev;

    public TaskNode(TaskNode<T> prev, T current, TaskNode<T> next) {
        this.data = current;
        this.next = next;
        this.prev = prev;
    }

    public T getData() {
        return data;
    }

    public TaskNode<T> getNext() {
        return next;
    }

    public TaskNode<T> getPrev() {
        return prev;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setNext(TaskNode<T> next) {
        this.next = next;
    }

    public void setPrev(TaskNode<T> prev) {
        this.prev = prev;
    }
}
