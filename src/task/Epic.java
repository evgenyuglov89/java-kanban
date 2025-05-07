package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Subtask> subTasks;

    public Epic(String name, String description, int id) {
        super(name, description, id);
        this.subTasks = new ArrayList<Subtask>();
        this.type = TaskType.EPIC;
    }

    public Epic(String name, String description, int id, List<Subtask> subTasks, TaskStatus status) {
        super(name, description, id, status);
        this.subTasks = subTasks;
        this.type = TaskType.EPIC;
    }

    public Epic(String name, String description, int id, TaskStatus status) {
        super(name, description, id, status);
        this.subTasks = new ArrayList<>();
        this.type = TaskType.EPIC;
    }

    public List<Subtask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<Subtask> subTasks) {
        this.subTasks = subTasks;
    }

    public void addSubTask(Subtask subtask) {
        if (subtask == null || subtask.getId() == this.getId()) {
            return;
        }

        boolean alreadyExists = subTasks.stream()
                .anyMatch(s -> s.getId() == subtask.getId());

        if (!alreadyExists) {
            subTasks.add(subtask);
        }
    }

    public void modifySubTask(Subtask subTask) {
        int index = -1;
        for (int i = 0; i < subTasks.size(); i++) {
            if (subTasks.get(i).getId() == subTask.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            subTasks.set(index, subTask);
        } else {
            subTasks.add(subTask);
        }
        System.out.println(this.getSubTasks());
    }

    public void removeSubTask(int subtaskId) {
        subTasks.removeIf(subtask -> subtask.getId() == subtaskId);
    }

    @Override
    public String toString() {
        return "task.Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", subTasks=" + subTasks +
                '}';
    }

    @Override
    public LocalDateTime getStartTime() {
        return subTasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public Duration getDuration() {
        return subTasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime getEndTime() {
        return subTasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
