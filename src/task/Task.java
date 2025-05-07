package task;

import manager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = TaskStatus.NEW;
        this.startTime = InMemoryTaskManager.UNDEFINED_TIME;
        this.duration = Duration.ZERO;
    }

    public Task(String name, String description, int id, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = InMemoryTaskManager.UNDEFINED_TIME;
        this.duration = Duration.ZERO;
    }

    public Task(String name, String description, int id, LocalDateTime startTime, int durationInMinutes) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = TaskStatus.NEW;
        this.startTime = startTime;
        this.duration = Duration.ofMinutes(durationInMinutes);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String startTimeString = "";
        String durationString = "";

        if (startTime != null && duration != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            startTimeString = startTime.format(formatter);
            durationString = duration.toString();
        }

        return "task.Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status='" + status + '\'' +
                ", start time='" + startTimeString + '\'' +
                ", duration='" + durationString + '\'' +
                '}';
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        int epicId = parts[5].isEmpty() ? -1 : Integer.parseInt(parts[5]);

        LocalDateTime startTime = parts[6].isEmpty() ? null : LocalDateTime.parse(parts[6]);
        Duration duration = parts[7].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[7]));

        return switch (type) {
            case TASK -> {
                Task task = new Task(name, description, id, status);
                task.setStartTime(startTime);
                task.setDuration(duration);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, id, status);
                yield epic;
            }
            case SUBTASK -> {
                Subtask subtask = new Subtask(name, description, id, epicId, status);
                subtask.setStartTime(startTime);
                subtask.setDuration(duration);
                yield subtask;
            }
        };
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
